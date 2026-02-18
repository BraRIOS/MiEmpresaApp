package com.brios.miempresa.catalog.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.cart.data.CartItemDao
import com.brios.miempresa.cart.data.CartRepository
import com.brios.miempresa.catalog.domain.CatalogAccessError
import com.brios.miempresa.catalog.domain.CatalogSyncException
import com.brios.miempresa.catalog.domain.ClientCatalogRepository
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.network.NetworkMonitor
import com.brios.miempresa.products.data.ProductDao
import com.brios.miempresa.products.data.ProductEntity
import com.brios.miempresa.products.data.PublicCategoryCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class CatalogSnapshot(
    val company: Company?,
    val products: List<ProductEntity>,
    val categories: List<String>,
    val categoryProductCount: Map<String, Int>,
    val totalPublicProducts: Int,
    val cartCount: Int,
    val query: String,
    val selectedCategory: String?,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ClientCatalogViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val companyDao: CompanyDao,
        private val productDao: ProductDao,
        private val cartItemDao: CartItemDao,
        private val cartRepository: CartRepository,
        private val clientCatalogRepository: ClientCatalogRepository,
        private val networkMonitor: NetworkMonitor,
    ) : ViewModel() {
        private val _events = MutableSharedFlow<ClientCatalogEvent>(replay = 0)
        val events: SharedFlow<ClientCatalogEvent> = _events.asSharedFlow()
        private val companyId: String = savedStateHandle.get<String>("companyId").orEmpty()
        private val searchQuery = MutableStateFlow("")
        private val selectedCategory = MutableStateFlow<String?>(null)
        private val refreshErrorMessage = MutableStateFlow<String?>(null)
        private val isAdminHybrid = MutableStateFlow(false)
        private val _isRefreshing = MutableStateFlow(false)
        private val isOnlineFlow =
            networkMonitor.observeOnlineStatus()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = networkMonitor.isOnlineNow(),
                )

        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private val companyFlow =
            if (companyId.isBlank()) {
                flowOf(null)
            } else {
                companyDao.observeCompanyById(companyId)
            }

        private val filteredProductsFlow =
            if (companyId.isBlank()) {
                flowOf(emptyList())
            } else {
                combine(searchQuery, selectedCategory) { query, category ->
                    query.trim() to category
                }.flatMapLatest { (query, category) ->
                    productDao.getPublicFiltered(
                        companyId = companyId,
                        searchQuery = query,
                        categoryName = category,
                    )
                }
            }

        private val categoryCountsFlow =
            if (companyId.isBlank()) {
                flowOf(emptyList())
            } else {
                searchQuery
                    .map { it.trim() }
                    .flatMapLatest { query ->
                        productDao.getPublicCategoryCounts(
                            companyId = companyId,
                            searchQuery = query,
                        )
                    }
            }

        private val totalPublicProductsFlow =
            if (companyId.isBlank()) {
                flowOf(0)
            } else {
                productDao.observePublicCount(companyId)
            }

        private val cartCountFlow =
            if (companyId.isBlank()) {
                flowOf(0)
            } else {
                cartItemDao.observeItemCount(companyId)
            }

        private val snapshotFlow =
            combine(
                combine(
                    companyFlow,
                    filteredProductsFlow,
                    categoryCountsFlow,
                    totalPublicProductsFlow,
                    cartCountFlow,
                ) { company, products, categoryCounts, totalPublicProducts, cartCount ->
                    val categories = categoryCounts.map(PublicCategoryCount::categoryName)
                    CatalogSnapshot(
                        company = company,
                        products = products,
                        categories = categories,
                        categoryProductCount = categoryCounts.associate { it.categoryName to it.productCount },
                        totalPublicProducts = totalPublicProducts,
                        cartCount = cartCount,
                        query = "",
                        selectedCategory = null,
                    )
                },
                searchQuery,
                selectedCategory,
            ) { snapshot, query, category ->
                snapshot.copy(
                    query = query,
                    selectedCategory = category,
                )
            }

        val uiState: StateFlow<ClientCatalogState> =
            combine(
                snapshotFlow,
                refreshErrorMessage,
                isAdminHybrid,
                _isRefreshing,
                isOnlineFlow,
            ) { snapshot, errorMessage, adminHybrid, refreshing, isOnline ->
                if (companyId.isBlank()) {
                    return@combine ClientCatalogState.Error("No pudimos abrir este catálogo")
                }

                val company = snapshot.company
                if (company == null) {
                    return@combine when {
                        refreshing -> ClientCatalogState.Loading
                        errorMessage != null -> ClientCatalogState.Error(errorMessage)
                        else -> ClientCatalogState.Loading
                    }
                }

                if (errorMessage != null && snapshot.totalPublicProducts == 0 && !refreshing) {
                    return@combine ClientCatalogState.Error(errorMessage)
                }

                val commonData =
                    ClientCatalogUiData(
                        company = company,
                        products = snapshot.products,
                        categories = snapshot.categories,
                        categoryProductCount = snapshot.categoryProductCount,
                        selectedCategory = snapshot.selectedCategory,
                        searchQuery = snapshot.query,
                        cartCount = snapshot.cartCount,
                        isOffline = !isOnline,
                        isAdminHybrid = adminHybrid,
                    )

                if (snapshot.products.isNotEmpty()) {
                    ClientCatalogState.Success(data = commonData)
                } else {
                    val hasActiveFilters = snapshot.selectedCategory != null || snapshot.query.isNotBlank()
                    if (commonData.isOffline && snapshot.totalPublicProducts == 0) {
                        ClientCatalogState.Offline(data = commonData)
                    } else {
                        ClientCatalogState.Empty(
                            data = commonData,
                            hasActiveFilters = hasActiveFilters,
                        )
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ClientCatalogState.Loading,
            )

        init {
            viewModelScope.launch {
                isAdminHybrid.value = companyDao.getOwnedCompanyCount() > 0
                if (companyId.isNotBlank() && productDao.getPublicCount(companyId) == 0) {
                    refreshCatalog()
                }
            }
        }

        fun onSearchQueryChange(value: String) {
            searchQuery.value = value
        }

        fun onCategoryToggle(category: String) {
            selectedCategory.value =
                if (selectedCategory.value == category) {
                    null
                } else {
                    category
                }
        }

        fun clearCategoryFilter() {
            selectedCategory.value = null
        }

        fun clearFilters() {
            searchQuery.value = ""
            selectedCategory.value = null
        }

        fun refreshCatalog() {
            if (companyId.isBlank()) return

            viewModelScope.launch {
                _isRefreshing.value = true
                try {
                    val company = companyDao.getCompanyById(companyId)
                    val publicSheetId = company?.publicSheetId
                    if (publicSheetId.isNullOrBlank()) {
                        refreshErrorMessage.value = "No encontramos el ID público de este catálogo"
                        return@launch
                    }

                    val result =
                        clientCatalogRepository.refreshCatalog(
                            companyId = companyId,
                            publicSheetId = publicSheetId,
                        )
                    result.onSuccess {
                        refreshErrorMessage.value = null
                    }.onFailure { error ->
                        refreshErrorMessage.value = mapError(error)
                    }
                } finally {
                    _isRefreshing.value = false
                }
            }
        }

        fun addProductToCart(productId: String) {
            viewModelScope.launch {
                val currentQuantity = cartRepository.getCurrentQuantityForProduct(companyId, productId)
                if (currentQuantity >= MAX_CART_QUANTITY_PER_PRODUCT) {
                    _events.emit(ClientCatalogEvent.ShowSnackbar("No podés agregar más de 99 unidades por producto"))
                    return@launch
                }
                cartRepository.addItem(
                    companyId = companyId,
                    productId = productId,
                    quantity = 1,
                )
            }
        }

        private fun mapError(error: Throwable): String {
            if (error is CatalogSyncException) {
                return when (error.error) {
                    CatalogAccessError.NO_INTERNET_FIRST_VISIT -> "Sin conexión para cargar este catálogo por primera vez"
                    CatalogAccessError.CATALOG_NOT_FOUND -> "Este catálogo no existe o fue eliminado"
                    CatalogAccessError.CATALOG_NOT_AVAILABLE -> "Este catálogo no está disponible para lectura pública"
                    CatalogAccessError.UNKNOWN -> error.message ?: "No pudimos sincronizar el catálogo"
                }
            }
            return error.message ?: "No pudimos sincronizar el catálogo"
        }

        companion object {
            private const val MAX_CART_QUANTITY_PER_PRODUCT = 99
        }

    }

sealed interface ClientCatalogEvent {
    data class ShowSnackbar(val message: String) : ClientCatalogEvent
}
