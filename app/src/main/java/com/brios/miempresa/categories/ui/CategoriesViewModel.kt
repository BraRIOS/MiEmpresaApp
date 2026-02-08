package com.brios.miempresa.categories.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.categories.domain.CategoriesRepository
import com.brios.miempresa.core.data.local.daos.CompanyDao
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
class CategoriesViewModel
    @Inject
    constructor(
        private val categoriesRepository: CategoriesRepository,
        private val companyDao: CompanyDao,
    ) : ViewModel() {
        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery

        private val _companyId = MutableStateFlow<String?>(null)

        val uiState: StateFlow<CategoriesUiState> =
            _companyId
                .flatMapLatest { companyId ->
                    if (companyId == null) {
                        flowOf(CategoriesUiState.Loading)
                    } else {
                        combine(
                            categoriesRepository.getAll(companyId),
                            _searchQuery,
                        ) { categories, query ->
                            val filtered =
                                if (query.isBlank()) {
                                    categories
                                } else {
                                    categories.filter {
                                        it.name.contains(query, ignoreCase = true)
                                    }
                                }
                            filtered.map { category ->
                                CategoryWithCount(
                                    category = category,
                                    productCount =
                                        categoriesRepository.getProductCount(
                                            category.id,
                                            companyId,
                                        ),
                                )
                            }
                        }.map { categoriesWithCount ->
                            if (categoriesWithCount.isEmpty() && _searchQuery.value.isBlank()) {
                                CategoriesUiState.Empty
                            } else {
                                CategoriesUiState.Success(
                                    categories = categoriesWithCount,
                                    searchQuery = _searchQuery.value,
                                )
                            }
                        }.catch { e ->
                            emit(CategoriesUiState.Error(e.message ?: "Error loading categories"))
                        }
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = CategoriesUiState.Loading,
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
            _searchQuery.value = query
        }

        fun deleteCategory(categoryId: String) {
            val companyId = _companyId.value ?: return
            viewModelScope.launch {
                categoriesRepository.delete(categoryId, companyId)
            }
        }
    }
