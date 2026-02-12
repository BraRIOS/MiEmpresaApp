package com.brios.miempresa.pedidos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.pedidos.data.OrderEntity
import com.brios.miempresa.pedidos.data.OrderItemEntity
import com.brios.miempresa.pedidos.domain.OrdersRepository
import com.brios.miempresa.products.data.ProductDao
import com.brios.miempresa.products.data.ProductEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class OrderFormItem(
    val productId: String?,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val thumbnailUrl: String? = null,
) {
    val subtotal: Double get() = price * quantity
}

data class OrderFormState(
    val customerName: String = "",
    val customerPhone: String = "",
    val notes: String = "",
    val items: List<OrderFormItem> = emptyList(),
) {
    val total: Double get() = items.sumOf { it.subtotal }
    val isValid: Boolean get() = customerPhone.matches(Regex("^\\d{6,15}$")) && items.isNotEmpty()
}

sealed interface PedidoManualEvent {
    data object OrderCreated : PedidoManualEvent
    data class ShowError(val message: String) : PedidoManualEvent
}

@HiltViewModel
class PedidoManualViewModel
    @Inject
    constructor(
        private val ordersRepository: OrdersRepository,
        private val productDao: ProductDao,
        private val companyDao: CompanyDao,
    ) : ViewModel() {
        private val _companyId = MutableStateFlow<String?>(null)

        private val _form = MutableStateFlow(OrderFormState())
        val form: StateFlow<OrderFormState> = _form.asStateFlow()

        private val _events = MutableSharedFlow<PedidoManualEvent>(replay = 0)
        val events: SharedFlow<PedidoManualEvent> = _events.asSharedFlow()

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        val products: StateFlow<List<ProductEntity>> =
            _companyId
                .flatMapLatest { companyId ->
                    if (companyId == null) flowOf(emptyList())
                    else productDao.getAllByCompanyFlow(companyId)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        init {
            viewModelScope.launch {
                val company = companyDao.getSelectedOwnedCompany()
                _companyId.value = company?.id
            }
        }

        fun updateCustomerName(name: String) {
            _form.value = _form.value.copy(customerName = name)
        }

        fun updateCustomerPhone(phone: String) {
            _form.value = _form.value.copy(customerPhone = phone)
        }

        fun updateNotes(notes: String) {
            _form.value = _form.value.copy(notes = notes)
        }

        fun addProduct(product: ProductEntity, quantity: Int) {
            val current = _form.value.items.toMutableList()
            val existing = current.indexOfFirst { it.productId == product.id }
            if (existing >= 0) {
                current[existing] = current[existing].copy(
                    quantity = current[existing].quantity + quantity,
                )
            } else {
                current.add(
                    OrderFormItem(
                        productId = product.id,
                        productName = product.name,
                        price = product.price,
                        quantity = quantity,
                        thumbnailUrl = product.imageUrl,
                    ),
                )
            }
            _form.value = _form.value.copy(items = current)
        }

        fun removeItem(index: Int) {
            val current = _form.value.items.toMutableList()
            if (index in current.indices) {
                current.removeAt(index)
                _form.value = _form.value.copy(items = current)
            }
        }

        fun createOrder() {
            val companyId = _companyId.value ?: return
            val formValue = _form.value
            if (!formValue.isValid) return

            viewModelScope.launch {
                _isSaving.value = true
                try {
                    val orderId = UUID.randomUUID().toString()
                    val order = OrderEntity(
                        id = orderId,
                        companyId = companyId,
                        customerName = formValue.customerName.ifBlank { "Sin nombre" },
                        customerPhone = formValue.customerPhone,
                        notes = formValue.notes.ifBlank { null },
                        totalAmount = formValue.total,
                        dirty = true,
                    )
                    val items = formValue.items.mapIndexed { index, item ->
                        OrderItemEntity(
                            id = "${orderId}_$index",
                            orderId = orderId,
                            productId = item.productId,
                            productName = item.productName,
                            priceAtOrder = item.price,
                            quantity = item.quantity,
                            thumbnailUrl = item.thumbnailUrl,
                        )
                    }
                    ordersRepository.createOrder(order, items)
                    _events.emit(PedidoManualEvent.OrderCreated)
                } catch (e: Exception) {
                    _events.emit(PedidoManualEvent.ShowError(e.message ?: "Error al crear pedido"))
                } finally {
                    _isSaving.value = false
                }
            }
        }
    }
