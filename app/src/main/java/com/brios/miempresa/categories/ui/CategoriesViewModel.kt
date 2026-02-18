package com.brios.miempresa.categories.ui

import android.content.Context
import androidx.work.WorkInfo
import com.brios.miempresa.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.categories.domain.CategoriesRepository
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.network.NetworkMonitor
import com.brios.miempresa.core.sync.SyncManager
import com.brios.miempresa.core.sync.SyncType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel
    @Inject
    constructor(
        private val categoriesRepository: CategoriesRepository,
        private val companyDao: CompanyDao,
        private val syncManager: SyncManager,
        private val networkMonitor: NetworkMonitor,
        @param:ApplicationContext private val appContext: Context,
    ) : ViewModel() {
        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery

        private val _companyId = MutableStateFlow<String?>(null)

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing

        private val _syncMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)
        val syncMessages: SharedFlow<String> = _syncMessages.asSharedFlow()

        val isOffline: StateFlow<Boolean> =
            networkMonitor.observeOnlineStatus()
                .map { isOnline -> !isOnline }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = !networkMonitor.isOnlineNow(),
                )

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
                syncAndNotify(SyncType.CATEGORIES, showInProgress = false)
            }
        }

        fun refresh() {
            viewModelScope.launch {
                if (_isRefreshing.value) return@launch
                _isRefreshing.value = true
                syncAndNotify(SyncType.CATEGORIES)
                _isRefreshing.value = false
            }
        }

        fun syncAndNotifyAfterFormEdit() {
            viewModelScope.launch {
                syncAndNotify(SyncType.CATEGORIES)
            }
        }

        private suspend fun syncAndNotify(
            type: SyncType,
            showInProgress: Boolean = true,
        ) {
            if (showInProgress) {
                _syncMessages.emit(appContext.getString(R.string.sync_in_progress))
            }
            val syncWorkId = syncManager.syncNow(type)
            val finalState =
                withTimeoutOrNull(SYNC_RESULT_TIMEOUT_MS) {
                    syncManager
                        .observeWorkState(syncWorkId)
                        .filterNotNull()
                        .first { state -> state.isFinished }
                }
            val messageRes =
                when (finalState) {
                    WorkInfo.State.SUCCEEDED -> R.string.sync_completed
                    WorkInfo.State.FAILED,
                    WorkInfo.State.CANCELLED,
                    -> R.string.sync_failed
                    null -> R.string.sync_scheduled
                    else -> R.string.sync_completed
                }
            _syncMessages.emit(appContext.getString(messageRes))
        }

        companion object {
            private const val SYNC_RESULT_TIMEOUT_MS = 30_000L
        }
    }
