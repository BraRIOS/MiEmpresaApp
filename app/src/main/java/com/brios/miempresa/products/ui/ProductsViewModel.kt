package com.brios.miempresa.products.ui

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.categories.domain.CategoriesRepository
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.sync.SyncManager
import com.brios.miempresa.core.sync.SyncType
import com.brios.miempresa.products.domain.ProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductsViewModel
    @Inject
    constructor(
        private val productsRepository: ProductsRepository,
        private val categoriesRepository: CategoriesRepository,
        private val companyDao: CompanyDao,
        private val syncManager: SyncManager,
        private val connectivityManager: ConnectivityManager,
    ) : ViewModel() {
        private val _filters = MutableStateFlow(ProductFilters())
        val filters: StateFlow<ProductFilters> = _filters

        private val _companyId = MutableStateFlow<String?>(null)

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing

        val isOffline: Boolean
            get() {
                val network = connectivityManager.activeNetwork ?: return true
                val capabilities =
                    connectivityManager.getNetworkCapabilities(network)
                        ?: return true
                return !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }

        val uiState: StateFlow<ProductsUiState> =
            _companyId
                .flatMapLatest { companyId ->
                    if (companyId == null) {
                        flowOf(ProductsUiState.Loading)
                    } else {
                        combine(
                            productsRepository.getAll(companyId),
                            categoriesRepository.getAll(companyId),
                            _filters,
                        ) { products, categories, filters ->
                            val filtered = applyFilters(products, filters)
                            Triple(filtered, categories, filters)
                        }.map { (filtered, categories, filters) ->
                            when {
                                filtered.isEmpty() && filters == ProductFilters() ->
                                    ProductsUiState.Empty
                                filtered.isEmpty() ->
                                    ProductsUiState.EmptyFiltered(filters, categories)
                                else ->
                                    ProductsUiState.Success(filtered, categories, filters)
                            }
                        }.catch { e ->
                            emit(
                                ProductsUiState.Error(
                                    e.message ?: "Error loading products",
                                ),
                            )
                        }
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = ProductsUiState.Loading,
                )

        init {
            loadCompanyId()
        }

        private fun loadCompanyId() {
            viewModelScope.launch {
                val company = companyDao.getSelectedOwnedCompany()
                _companyId.value = company?.id
            }
        }

        private fun applyFilters(
            products: List<com.brios.miempresa.core.data.local.entities.ProductEntity>,
            filters: ProductFilters,
        ): List<com.brios.miempresa.core.data.local.entities.ProductEntity> {
            var result = products
            if (filters.searchQuery.isNotBlank()) {
                result =
                    result.filter {
                        it.name.contains(filters.searchQuery, ignoreCase = true)
                    }
            }
            if (filters.categoryId != null) {
                result = result.filter { it.categoryId == filters.categoryId }
            }
            when (filters.publicFilter) {
                PublicFilter.PUBLIC -> result = result.filter { it.isPublic }
                PublicFilter.PRIVATE -> result = result.filter { !it.isPublic }
                PublicFilter.ALL -> {}
            }
            return result
        }

        fun onSearchQueryChanged(query: String) {
            _filters.value = _filters.value.copy(searchQuery = query)
        }

        fun onCategoryFilterChanged(categoryId: String?) {
            _filters.value = _filters.value.copy(categoryId = categoryId)
        }

        fun onPublicFilterChanged(filter: PublicFilter) {
            _filters.value = _filters.value.copy(publicFilter = filter)
        }

        fun clearFilters() {
            _filters.value = ProductFilters()
        }

        fun deleteProduct(productId: String) {
            val companyId = _companyId.value ?: return
            viewModelScope.launch {
                productsRepository.delete(productId, companyId)
            }
        }

        fun togglePublic(
            productId: String,
            isPublic: Boolean,
        ) {
            val companyId = _companyId.value ?: return
            viewModelScope.launch {
                productsRepository.togglePublic(productId, companyId, isPublic)
            }
        }

        fun refresh() {
            _isRefreshing.value = true
            syncManager.syncNow(SyncType.ALL)
            _isRefreshing.value = false
        }
    }
