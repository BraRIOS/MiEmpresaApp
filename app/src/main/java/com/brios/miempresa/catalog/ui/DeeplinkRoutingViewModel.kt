package com.brios.miempresa.catalog.ui

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.catalog.domain.CatalogAccessError
import com.brios.miempresa.catalog.domain.CatalogSyncException
import com.brios.miempresa.catalog.domain.ClientCatalogRepository
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.di.IoDispatcher
import com.brios.miempresa.core.util.normalizeSheetId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface DeeplinkNavigationEvent {
    data class NavigateClientCatalog(
        val companyId: String,
        val consumedSheetId: String,
    ) : DeeplinkNavigationEvent

    data class NavigateError(
        val error: CatalogAccessError,
        val sheetId: String,
    ) : DeeplinkNavigationEvent

    data class NavigateHome(
        val consumedSheetId: String? = null,
    ) : DeeplinkNavigationEvent

    data object NavigateMyStores : DeeplinkNavigationEvent
}

@HiltViewModel
class DeeplinkRoutingViewModel
    @Inject
    constructor(
        private val companyDao: CompanyDao,
        private val clientCatalogRepository: ClientCatalogRepository,
        private val connectivityManager: ConnectivityManager,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _navigationEvents = MutableSharedFlow<DeeplinkNavigationEvent>(extraBufferCapacity = 1)
        val navigationEvents = _navigationEvents.asSharedFlow()

        fun handleDeeplink(sheetId: String) {
            viewModelScope.launch {
                val normalizedSheetId = normalizeSheetId(sheetId)
                if (normalizedSheetId == null) return@launch

                val event = resolveDeeplink(normalizedSheetId)
                _navigationEvents.emit(event)
            }
        }

        fun retryDeeplink(sheetId: String) {
            handleDeeplink(sheetId)
        }

        fun routeToMyStoresIfVisited() {
            viewModelScope.launch {
                val hasVisited =
                    withContext(ioDispatcher) {
                        companyDao.countVisited() > 0
                    }
                if (hasVisited) {
                    _navigationEvents.emit(DeeplinkNavigationEvent.NavigateMyStores)
                }
            }
        }

        fun isOnlineNow(): Boolean {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        private suspend fun resolveDeeplink(sheetId: String): DeeplinkNavigationEvent =
            withContext(ioDispatcher) {
                val now = System.currentTimeMillis()
                val existingVisitedCompany = companyDao.getVisitedByPublicSheetId(sheetId)

                if (existingVisitedCompany != null) {
                    companyDao.updateLastVisited(existingVisitedCompany.id, now)
                    return@withContext DeeplinkNavigationEvent.NavigateClientCatalog(
                        companyId = existingVisitedCompany.id,
                        consumedSheetId = sheetId,
                    )
                }

                if (!isOnlineNow()) {
                    return@withContext DeeplinkNavigationEvent.NavigateError(
                        error = CatalogAccessError.NO_INTERNET_FIRST_VISIT,
                        sheetId = sheetId,
                    )
                }

                val syncResult = clientCatalogRepository.syncPublicSheet(sheetId)
                syncResult.fold(
                    onSuccess = { company ->
                        DeeplinkNavigationEvent.NavigateClientCatalog(
                            companyId = company.id,
                            consumedSheetId = sheetId,
                        )
                    },
                    onFailure = { throwable ->
                        if (throwable is CancellationException) throw throwable
                        DeeplinkNavigationEvent.NavigateError(
                            error = mapSyncFailure(throwable),
                            sheetId = sheetId,
                        )
                    },
                )
            }

        private fun mapSyncFailure(throwable: Throwable): CatalogAccessError {
            return if (throwable is CatalogSyncException) {
                throwable.error
            } else {
                CatalogAccessError.CATALOG_NOT_AVAILABLE
            }
        }
    }
