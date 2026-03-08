package com.brios.miempresa.orders.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.orders.data.OrderEntity
import com.brios.miempresa.orders.domain.OrdersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OrdersListUiState {
    data object Loading : OrdersListUiState
    data class Success(val orders: List<OrderEntity>) : OrdersListUiState
    data object Empty : OrdersListUiState
    data class Error(val message: String) : OrdersListUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrdersListViewModel
    @Inject
    constructor(
        private val ordersRepository: OrdersRepository,
        private val companyDao: CompanyDao,
    ) : ViewModel() {
        private val _companyId = MutableStateFlow<String?>(null)

        private val _uiState = MutableStateFlow<OrdersListUiState>(OrdersListUiState.Loading)
        val uiState: StateFlow<OrdersListUiState> = _uiState.asStateFlow()

        fun refresh() {
            viewModelScope.launch {
                val company = companyDao.getSelectedOwnedCompany()
                _companyId.value = company?.id
            }
        }

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
                        if (orders.isEmpty()) OrdersListUiState.Empty
                        else OrdersListUiState.Success(orders)
                    }
                    .collect { _uiState.value = it }
            }
        }
    }
