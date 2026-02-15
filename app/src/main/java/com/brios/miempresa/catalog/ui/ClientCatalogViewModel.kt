package com.brios.miempresa.catalog.ui

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import com.brios.miempresa.products.data.ProductDao
import com.brios.miempresa.products.data.ProductEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class CatalogSnapshot(
    val company: Company?,
    val products: List<ProductEntity>,
    val cartCount: Int,
    val query: String,
    val selectedCategory: String?,
)

@HiltViewModel
class ClientCatalogViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val companyDao: CompanyDao,
        private val productDao: ProductDao,
        private val cartItemDao: CartItemDao,
        private val cartRepository: CartRepository,
        private val clientCatalogRepository: ClientCatalogRepository,
        private val connectivityManager: ConnectivityManager,
    ) : ViewModel() {
        private val companyId: String = savedStateHandle.get<String>("companyId").orEmpty()
        private val searchQuery = MutableStateFlow("")
        private val selectedCategory = MutableStateFlow<String?>(null)
        private val refreshErrorMessage = MutableStateFlow<String?>(null)
        private val isAdminHybrid = MutableStateFlow(false)
        private val _isRefreshing = MutableStateFlow(false)

        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private val companyFlow =
            if (companyId.isBlank()) {
                flowOf(null)
            } else {
                companyDao.observeCompanyById(companyId)
            }

        private val productsFlow =
            if (companyId.isBlank()) {
                flowOf(emptyList())
            } else {
                productDao.getByCompanyIdPublic(companyId)
            }

        private val cartCountFlow =
            if (companyId.isBlank()) {
                flowOf(0)
            } else {
                cartItemDao.observeItemCount(companyId)
            }

        private val snapshotFlow =
            combine(
                companyFlow,
                productsFlow,
                cartCountFlow,
                searchQuery,
                selectedCategory,
            ) { company, products, cartCount, query, category ->
                CatalogSnapshot(
                    company = company,
                    products = products,
                    cartCount = cartCount,
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
            ) { snapshot, errorMessage, adminHybrid, refreshing ->
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

                if (errorMessage != null && snapshot.products.isEmpty() && !refreshing) {
                    return@combine ClientCatalogState.Error(errorMessage)
                }

                val normalizedQuery = snapshot.query.trim()
                val categories =
                    snapshot.products
                        .mapNotNull { it.categoryName?.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .sorted()

                val filteredProducts =
                    snapshot.products.filter { product ->
                        val matchesCategory =
                            snapshot.selectedCategory == null ||
                                product.categoryName.equals(snapshot.selectedCategory, ignoreCase = true)
                        val matchesQuery =
                            normalizedQuery.isBlank() ||
                                product.name.contains(normalizedQuery, ignoreCase = true) ||
                                product.description?.contains(normalizedQuery, ignoreCase = true) == true
                        matchesCategory && matchesQuery
                    }

                val commonData =
                    ClientCatalogUiData(
                        company = company,
                        products = filteredProducts,
                        categories = categories,
                        selectedCategory = snapshot.selectedCategory,
                        searchQuery = snapshot.query,
                        cartCount = snapshot.cartCount,
                        isOffline = !isOnline(),
                        isAdminHybrid = adminHybrid,
                    )

                if (filteredProducts.isNotEmpty()) {
                    ClientCatalogState.Success(data = commonData)
                } else {
                    val hasActiveFilters = snapshot.selectedCategory != null || snapshot.query.isNotBlank()
                    if (commonData.isOffline && snapshot.products.isEmpty()) {
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
            }
            refreshCatalog()
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

        private fun isOnline(): Boolean {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }
