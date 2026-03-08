package com.brios.miempresa.products.ui

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.cart.data.CartRepository
import com.brios.miempresa.cart.domain.ResolveCartQuantityAdditionUseCase
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.products.data.ProductEntity
import com.brios.miempresa.products.domain.ProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

enum class ProductDetailMode(val routeValue: String) {
    ADMIN("admin"),
    CLIENT("client"),
    ;

    companion object {
        fun fromRoute(value: String?): ProductDetailMode {
            return entries.firstOrNull { it.routeValue == value } ?: ADMIN
        }
    }
}

data class ProductDetailUiData(
    val product: ProductEntity,
    val company: Company,
    val mode: ProductDetailMode,
    val quantity: Int = 1,
    val showOfflineWarning: Boolean = false,
)

sealed interface ProductDetailUiState {
    data object Loading : ProductDetailUiState

    data class Success(
        val data: ProductDetailUiData,
    ) : ProductDetailUiState

    data class Error(
        val message: String,
    ) : ProductDetailUiState
}

sealed interface ProductDetailEvent {
    data class ShowSnackbar(val message: String) : ProductDetailEvent

    data object ProductDeleted : ProductDetailEvent
}

@HiltViewModel
class ProductDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val productsRepository: ProductsRepository,
        private val companyDao: CompanyDao,
        private val cartRepository: CartRepository,
        private val resolveCartQuantityAdditionUseCase: ResolveCartQuantityAdditionUseCase,
        private val connectivityManager: ConnectivityManager,
    ) : ViewModel() {
        private val productId: String = savedStateHandle.get<String>("productId").orEmpty()
        private val requestedCompanyId: String = savedStateHandle.get<String>("companyId").orEmpty()
        private val mode: ProductDetailMode = ProductDetailMode.fromRoute(savedStateHandle.get("mode"))
        private var resolvedCompanyId: String? = null
        private var cartCountJob: Job? = null

        private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
        val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()
        private val _cartCount = MutableStateFlow(0)
        val cartCount: StateFlow<Int> = _cartCount.asStateFlow()

        private val _events = MutableSharedFlow<ProductDetailEvent>(replay = 0)
        val events: SharedFlow<ProductDetailEvent> = _events.asSharedFlow()

        init {
            refresh()
        }

        fun refresh() {
            viewModelScope.launch {
                _uiState.value = ProductDetailUiState.Loading
                val companyId = resolveCompanyId()
                if (companyId == null || productId.isBlank()) {
                    _cartCount.value = 0
                    _uiState.value = ProductDetailUiState.Error("No pudimos abrir este producto")
                    return@launch
                }
                resolvedCompanyId = companyId

                val company = companyDao.getCompanyById(companyId)
                val product = productsRepository.getById(productId, companyId)
                if (company == null || product == null || product.deleted) {
                    _cartCount.value = 0
                    _uiState.value = ProductDetailUiState.Error("No encontramos este producto")
                    return@launch
                }

                observeCartCount(companyId)
                _uiState.value =
                    ProductDetailUiState.Success(
                        ProductDetailUiData(
                            product = product,
                            company = company,
                            mode = mode,
                            quantity = 1,
                            showOfflineWarning = shouldShowOfflineWarning(company),
                        ),
                    )
            }
        }

        private fun observeCartCount(companyId: String) {
            cartCountJob?.cancel()
            if (mode != ProductDetailMode.CLIENT) {
                _cartCount.value = 0
                return
            }

            cartCountJob =
                viewModelScope.launch {
                    cartRepository.observeCartCount(companyId).collect { count ->
                        _cartCount.value = count
                    }
                }
        }

        fun onQuantityChange(newQuantity: Int) {
            val current = _uiState.value as? ProductDetailUiState.Success ?: return
            if (current.data.mode != ProductDetailMode.CLIENT) return
            _uiState.value =
                current.copy(
                    data = current.data.copy(quantity = newQuantity.coerceIn(1, 99)),
                )
        }

        fun addToCart() {
            val current = _uiState.value as? ProductDetailUiState.Success ?: return
            if (current.data.mode != ProductDetailMode.CLIENT) return
            val companyId = resolvedCompanyId ?: return

            viewModelScope.launch {
                val currentQuantity =
                    cartRepository.getCurrentQuantityForProduct(
                        companyId = companyId,
                        productId = current.data.product.id,
                    )
                val quantityDecision =
                    resolveCartQuantityAdditionUseCase(
                        currentQuantity = currentQuantity,
                        requestedQuantity = current.data.quantity,
                    )
                if (!quantityDecision.canAdd) {
                    _events.emit(ProductDetailEvent.ShowSnackbar("No podés agregar más de 99 unidades por producto"))
                    return@launch
                }

                runCatching {
                    cartRepository.addItem(
                        companyId = companyId,
                        productId = current.data.product.id,
                        quantity = quantityDecision.quantityToAdd,
                    )
                }.onSuccess {
                    val message =
                        if (quantityDecision.reachedLimit) {
                            "No podés agregar más de 99 unidades por producto"
                        } else {
                            "Agregado al carrito ✓"
                        }
                    _events.emit(ProductDetailEvent.ShowSnackbar(message))
                }.onFailure {
                    _events.emit(ProductDetailEvent.ShowSnackbar("No pudimos agregar el producto"))
                }
            }
        }

        fun deleteProduct() {
            val current = _uiState.value as? ProductDetailUiState.Success ?: return
            if (current.data.mode != ProductDetailMode.ADMIN) return
            val companyId = resolvedCompanyId ?: return

            viewModelScope.launch {
                runCatching {
                    productsRepository.delete(current.data.product.id, companyId)
                }.onSuccess {
                    _events.emit(ProductDetailEvent.ProductDeleted)
                }.onFailure {
                    _events.emit(ProductDetailEvent.ShowSnackbar("No pudimos eliminar el producto"))
                }
            }
        }

        private suspend fun resolveCompanyId(): String? {
            if (requestedCompanyId.isNotBlank()) return requestedCompanyId
            return if (mode == ProductDetailMode.ADMIN) {
                companyDao.getSelectedOwnedCompany()?.id
            } else {
                null
            }
        }

        private fun shouldShowOfflineWarning(company: Company): Boolean {
            if (isOnline()) return false
            val lastSyncedAt = company.lastSyncedAt ?: return false
            val ageMillis = System.currentTimeMillis() - lastSyncedAt
            return ageMillis > HOURS_24_IN_MILLIS
        }

        private fun isOnline(): Boolean {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        companion object {
            private const val HOURS_24_IN_MILLIS = 24L * 60L * 60L * 1000L
        }
    }
