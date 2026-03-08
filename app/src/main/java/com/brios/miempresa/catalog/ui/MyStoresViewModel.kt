package com.brios.miempresa.catalog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.cart.data.CartItemDao
import com.brios.miempresa.cart.data.CartRepository
import com.brios.miempresa.catalog.domain.CatalogAccessError
import com.brios.miempresa.catalog.domain.CatalogSyncException
import com.brios.miempresa.catalog.domain.ClientCatalogRepository
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.util.normalizeSheetId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyStoresUiState(
    val isLoading: Boolean = true,
    val isAdminHybridContext: Boolean = false,
    val searchQuery: String = "",
    val stores: List<Company> = emptyList(),
    val filteredStores: List<Company> = emptyList(),
    val isAddingStore: Boolean = false,
)

sealed interface MyStoresEvent {
    data class NavigateToCatalog(val companyId: String) : MyStoresEvent

    data class ConfirmCartReplacement(
        val targetCompanyId: String,
        val cartCompanyId: String,
        val cartCompanyName: String,
    ) : MyStoresEvent

    data class ShowSnackbar(val message: String) : MyStoresEvent
}

@HiltViewModel
class MyStoresViewModel
    @Inject
    constructor(
        private val companyDao: CompanyDao,
        private val clientCatalogRepository: ClientCatalogRepository,
        private val cartItemDao: CartItemDao,
        private val cartRepository: CartRepository,
    ) : ViewModel() {
        private val searchQuery = MutableStateFlow("")
        private val isAdminHybridContext = MutableStateFlow(false)
        private val isAddingStore = MutableStateFlow(false)

        private val _events = MutableSharedFlow<MyStoresEvent>(replay = 0)
        val events: SharedFlow<MyStoresEvent> = _events.asSharedFlow()

        val uiState: StateFlow<MyStoresUiState> =
            combine(
                companyDao.getVisitedCompanies(),
                searchQuery,
                isAdminHybridContext,
                isAddingStore,
            ) { stores, query, isAdminHybrid, isAdding ->
                val normalizedQuery = query.trim()
                val filteredStores =
                    if (normalizedQuery.isBlank()) {
                        stores
                    } else {
                        stores.filter { company ->
                            company.name.contains(normalizedQuery, ignoreCase = true)
                        }
                    }
                MyStoresUiState(
                    isLoading = false,
                    isAdminHybridContext = isAdminHybrid,
                    searchQuery = query,
                    stores = stores,
                    filteredStores = filteredStores,
                    isAddingStore = isAdding,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MyStoresUiState(),
            )

        init {
            viewModelScope.launch {
                isAdminHybridContext.value = companyDao.getOwnedCompanyCount() > 0
            }
        }

        fun onSearchQueryChange(value: String) {
            searchQuery.value = value
        }

        fun clearSearch() {
            searchQuery.value = ""
        }

        fun openStore(companyId: String) {
            viewModelScope.launch {
                emitStoreNavigation(companyId)
            }
        }

        fun addStoreBySheetId(rawSheetId: String) {
            val sheetId = normalizeSheetId(rawSheetId)
            if (sheetId.isNullOrBlank()) {
                viewModelScope.launch {
                    _events.emit(MyStoresEvent.ShowSnackbar("Ingresá un código válido"))
                }
                return
            }

            viewModelScope.launch {
                isAddingStore.value = true
                runCatching {
                    clientCatalogRepository.syncPublicSheet(sheetId).getOrThrow()
                }.onSuccess { company ->
                    emitStoreNavigation(company.id)
                }.onFailure { throwable ->
                    _events.emit(MyStoresEvent.ShowSnackbar(mapError(throwable)))
                }
                isAddingStore.value = false
            }
        }

        fun confirmStoreSwitch(
            targetCompanyId: String,
            cartCompanyId: String,
        ) {
            viewModelScope.launch {
                runCatching {
                    cartRepository.clearCart(cartCompanyId)
                }.onSuccess {
                    _events.emit(MyStoresEvent.NavigateToCatalog(targetCompanyId))
                }.onFailure {
                    _events.emit(MyStoresEvent.ShowSnackbar("No pudimos cambiar de tienda"))
                }
            }
        }

        private suspend fun emitStoreNavigation(targetCompanyId: String) {
            val cartCompanyId =
                cartItemDao
                    .getCompaniesWithItems()
                    .firstOrNull { it != targetCompanyId }

            if (cartCompanyId == null) {
                _events.emit(MyStoresEvent.NavigateToCatalog(targetCompanyId))
                return
            }

            val cartCompanyName = companyDao.getCompanyById(cartCompanyId)?.name ?: "otra tienda"
            _events.emit(
                MyStoresEvent.ConfirmCartReplacement(
                    targetCompanyId = targetCompanyId,
                    cartCompanyId = cartCompanyId,
                    cartCompanyName = cartCompanyName,
                ),
            )
        }

        private fun mapError(error: Throwable): String {
            if (error is CatalogSyncException) {
                return when (error.error) {
                    CatalogAccessError.NO_INTERNET_FIRST_VISIT -> "Sin internet para agregar esta tienda"
                    CatalogAccessError.CATALOG_NOT_FOUND -> "No encontramos esa tienda"
                    CatalogAccessError.CATALOG_NOT_AVAILABLE -> "Esta tienda no está disponible"
                    CatalogAccessError.UNKNOWN -> error.message ?: "No pudimos agregar la tienda"
                }
            }
            return error.message ?: "No pudimos agregar la tienda"
        }
    }
