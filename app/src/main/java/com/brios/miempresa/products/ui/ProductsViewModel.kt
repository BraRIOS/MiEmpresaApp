package com.brios.miempresa.products.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.categories.domain.CategoriesRepository
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.network.NetworkMonitor
import com.brios.miempresa.core.sync.SyncManager
import com.brios.miempresa.core.sync.SyncType
import com.brios.miempresa.products.domain.ProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        private val networkMonitor: NetworkMonitor,
    ) : ViewModel() {
        private val _filters = MutableStateFlow(ProductFilters())
        val filters: StateFlow<ProductFilters> = _filters.asStateFlow()

        private val _companyId = MutableStateFlow<String?>(null)

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing

        val isOffline: StateFlow<Boolean> =
            networkMonitor.observeOnlineStatus()
                .map { isOnline -> !isOnline }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = !networkMonitor.isOnlineNow(),
                )

        val uiState: StateFlow<ProductsUiState> =
            combine(_companyId, _filters) { companyId, filters ->
                companyId to filters
            }.flatMapLatest { (companyId, filters) ->
                if (companyId == null) {
                    flowOf(ProductsUiState.Loading)
                } else {
                    combine(
                        productsRepository.getFiltered(
                            companyId = companyId,
                            searchQuery = filters.searchQuery.trim(),
                            categoryId = filters.categoryId,
                            isPublicFilter = filters.publicFilter.toNullableBoolean(),
                        ),
                        categoriesRepository.getAll(companyId),
                    ) { products, categories ->
                        when {
                            products.isEmpty() && filters == ProductFilters() ->
                                ProductsUiState.Empty
                            products.isEmpty() ->
                                ProductsUiState.EmptyFiltered(filters, categories)
                            else ->
                                ProductsUiState.Success(products, categories, filters)
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

        val productCountByCategory: StateFlow<Map<String, Int>> =
            combine(_companyId, _filters) { companyId, filters ->
                companyId to filters
            }.flatMapLatest { (companyId, filters) ->
                if (companyId == null) {
                    flowOf(emptyMap())
                } else {
                    productsRepository.getCategoryCountsByFilter(
                        companyId = companyId,
                        searchQuery = filters.searchQuery.trim(),
                        isPublicFilter = filters.publicFilter.toNullableBoolean(),
                    )
                }
            }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyMap(),
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
            viewModelScope.launch {
                _isRefreshing.value = true
                syncManager.syncNow(SyncType.ALL)
                kotlinx.coroutines.delay(1500)
                _isRefreshing.value = false
            }
        }

        private fun PublicFilter.toNullableBoolean(): Boolean? =
            when (this) {
                PublicFilter.ALL -> null
                PublicFilter.PUBLIC -> true
                PublicFilter.PRIVATE -> false
            }
    }
