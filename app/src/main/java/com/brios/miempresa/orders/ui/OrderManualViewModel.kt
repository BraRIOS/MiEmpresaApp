package com.brios.miempresa.orders.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.R
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.orders.data.OrderEntity
import com.brios.miempresa.orders.data.OrderItemEntity
import com.brios.miempresa.orders.domain.OrdersRepository
import com.brios.miempresa.products.data.ProductDao
import com.brios.miempresa.products.data.ProductEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    val customerPhoneCountryCode: String = "+54", // Default to Argentina or get from locale
    val customerPhone: String = "",
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val items: List<OrderFormItem> = emptyList(),
) {
    val total: Double get() = items.sumOf { it.subtotal }
    val isValid: Boolean get() = customerPhone.matches(Regex("^\\d{6,15}$")) && items.isNotEmpty()

    val fullPhoneNumber: String get() = "$customerPhoneCountryCode$customerPhone"
}

sealed interface OrderManualEvent {
    data object OrderCreated : OrderManualEvent
    data class ShowError(val message: String) : OrderManualEvent
}

@HiltViewModel
class OrderManualViewModel
    @Inject
    constructor(
        @ApplicationContext private val appContext: Context,
        private val ordersRepository: OrdersRepository,
        private val productDao: ProductDao,
        private val companyDao: CompanyDao,
    ) : ViewModel() {
        private val _companyId = MutableStateFlow<String?>(null)

        private val _form = MutableStateFlow(OrderFormState())
        val form: StateFlow<OrderFormState> = _form.asStateFlow()

        private val _events = MutableSharedFlow<OrderManualEvent>(replay = 0)
        val events: SharedFlow<OrderManualEvent> = _events.asSharedFlow()

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        @OptIn(ExperimentalCoroutinesApi::class)
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

        fun updateCustomerPhoneCountryCode(code: String) {
            _form.value = _form.value.copy(customerPhoneCountryCode = code)
        }

        fun updateDate(date: Long) {
            _form.value = _form.value.copy(date = date)
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

        fun updateItemQuantity(index: Int, newQuantity: Int) {
            val current = _form.value.items.toMutableList()
            if (index in current.indices) {
                 if (newQuantity <= 0) {
                     removeItem(index)
                 } else {
                     current[index] = current[index].copy(quantity = newQuantity)
                     _form.value = _form.value.copy(items = current)
                 }
            }
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
                        customerName = formValue.customerName.ifBlank { "" },
                        customerPhone = formValue.fullPhoneNumber,
                        notes = formValue.notes.ifBlank { null },
                        totalAmount = formValue.total,
                        orderDate = formValue.date,
                        dirty = true,
                    )
                    val items = formValue.items.mapIndexed { index, item ->
                        OrderItemEntity(
                            id = "${orderId}_$index",
                            orderId = orderId,
                            companyId = companyId,
                            productId = item.productId,
                            productName = item.productName,
                            priceAtOrder = item.price,
                            quantity = item.quantity,
                            thumbnailUrl = item.thumbnailUrl,
                        )
                    }
                    ordersRepository.createOrder(order, items)
                    _events.emit(OrderManualEvent.OrderCreated)
                } catch (e: Exception) {
                    _events.emit(
                        OrderManualEvent.ShowError(
                            e.message ?: appContext.getString(R.string.error_create_order),
                        ),
                    )
                } finally {
                    _isSaving.value = false
                }
            }
        }
    }
