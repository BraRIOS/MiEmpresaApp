package com.brios.miempresa.orders.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.orders.data.OrderEntity
import com.brios.miempresa.orders.data.OrderItemEntity
import com.brios.miempresa.orders.domain.OrdersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderDetailState(
    val order: OrderEntity? = null,
    val items: List<OrderItemEntity> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class OrderDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val ordersRepository: OrdersRepository,
        private val companyDao: CompanyDao,
    ) : ViewModel() {
        private val orderId: String = checkNotNull(savedStateHandle["orderId"])

        private val _state = MutableStateFlow(OrderDetailState())
        val state: StateFlow<OrderDetailState> = _state.asStateFlow()

        val items: StateFlow<List<OrderItemEntity>> =
            ordersRepository.getOrderItems(orderId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        init {
            viewModelScope.launch {
                val company = companyDao.getSelectedOwnedCompany()
                val companyId = company?.id ?: return@launch
                val order = ordersRepository.getOrderById(orderId, companyId)
                _state.value = _state.value.copy(order = order, isLoading = false)
            }
        }
    }
