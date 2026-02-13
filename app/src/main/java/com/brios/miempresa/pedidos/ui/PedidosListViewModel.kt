package com.brios.miempresa.pedidos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.pedidos.data.OrderEntity
import com.brios.miempresa.pedidos.domain.OrdersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PedidosListUiState {
    data object Loading : PedidosListUiState
    data class Success(val orders: List<OrderEntity>) : PedidosListUiState
    data object Empty : PedidosListUiState
    data class Error(val message: String) : PedidosListUiState
}

@HiltViewModel
class PedidosListViewModel
    @Inject
    constructor(
        private val ordersRepository: OrdersRepository,
        private val companyDao: CompanyDao,
    ) : ViewModel() {
        private val _companyId = MutableStateFlow<String?>(null)

        private val _uiState = MutableStateFlow<PedidosListUiState>(PedidosListUiState.Loading)
        val uiState: StateFlow<PedidosListUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val company = companyDao.getSelectedOwnedCompany()
                _companyId.value = company?.id
            }
            viewModelScope.launch {
                _companyId
                    .flatMapLatest { companyId ->
                        if (companyId == null) flowOf(emptyList())
                        else ordersRepository.getAllOrders(companyId)
                    }
                    .map { orders ->
                        if (orders.isEmpty()) PedidosListUiState.Empty
                        else PedidosListUiState.Success(orders)
                    }
                    .collect { _uiState.value = it }
            }
        }
    }
