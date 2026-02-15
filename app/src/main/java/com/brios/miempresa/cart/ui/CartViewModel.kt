package com.brios.miempresa.cart.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.cart.data.CartRepository
import com.brios.miempresa.cart.domain.CartEvent
import com.brios.miempresa.cart.domain.CartItem
import com.brios.miempresa.cart.domain.CartUiState
import com.brios.miempresa.core.data.local.daos.CompanyDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val cartRepository: CartRepository,
        private val companyDao: CompanyDao,
    ) : ViewModel() {
        private val routeCompanyId: String = savedStateHandle.get<String>("companyId").orEmpty()

        private val companyIdFlow: StateFlow<String?> =
            if (routeCompanyId.isNotBlank()) {
                MutableStateFlow<String?>(routeCompanyId).asStateFlow()
            } else {
                companyDao.getSelectedCompany()
                    .asFlow()
                    .map { it?.id }
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5_000),
                        initialValue = null,
                    )
            }

        val uiState: StateFlow<CartUiState> =
            companyIdFlow
                .flatMapLatest { companyId ->
                    if (companyId.isNullOrBlank()) {
                        flowOf(CartUiState.Empty)
                    } else {
                        combine(
                            cartRepository.observeCartItems(companyId),
                            companyDao.observeCompanyById(companyId),
                        ) { cartItems, company ->
                            val items =
                                cartItems.map { item ->
                                    CartItem(
                                        id = item.id,
                                        companyId = companyId,
                                        productId = item.productId,
                                        productName = item.productName ?: "Producto",
                                        productPrice = item.productPrice ?: 0.0,
                                        productImageUrl = item.productImageUrl,
                                        quantity = item.quantity,
                                        addedAt = item.addedAt,
                                    )
                                }

                            if (items.isEmpty()) {
                                CartUiState.Empty
                            } else {
                                CartUiState.Success(
                                    items = items,
                                    totalItems = items.sumOf { it.quantity },
                                    totalPrice = items.sumOf { it.subtotal },
                                    companyName = company?.name.orEmpty(),
                                )
                            }
                        }.onStart { emit(CartUiState.Loading) }
                    }
                }.catch { error ->
                    emit(CartUiState.Error(error.message ?: "No pudimos cargar el carrito"))
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = CartUiState.Loading,
                )

        private val _events = MutableSharedFlow<CartEvent>(replay = 0)
        val events: SharedFlow<CartEvent> = _events.asSharedFlow()

        val cartCount: StateFlow<Int> =
            companyIdFlow
                .flatMapLatest { companyId ->
                    if (companyId.isNullOrBlank()) {
                        flowOf(0)
                    } else {
                        cartRepository.observeCartCount(companyId)
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = 0,
                )

        fun addProduct(
            productId: String,
            quantity: Int = 1,
        ) {
            viewModelScope.launch {
                val companyId = getCompanyIdOrNull() ?: return@launch
                try {
                    cartRepository.addItem(companyId, productId, quantity)
                    _events.emit(CartEvent.ShowSnackbar("Producto agregado al carrito"))
                } catch (error: Throwable) {
                    if (error is CancellationException) throw error
                    _events.emit(CartEvent.ShowError(error.message ?: "No pudimos agregar el producto"))
                }
            }
        }

        fun updateQuantity(
            cartItemId: Long,
            newQuantity: Int,
        ) {
            viewModelScope.launch {
                val companyId = getCompanyIdOrNull() ?: return@launch
                try {
                    if (newQuantity <= 0) {
                        cartRepository.removeItem(cartItemId, companyId)
                        _events.emit(CartEvent.ShowSnackbar("Producto eliminado del carrito"))
                    } else {
                        cartRepository.updateQuantity(cartItemId, companyId, newQuantity)
                    }
                } catch (error: Throwable) {
                    if (error is CancellationException) throw error
                    _events.emit(CartEvent.ShowError(error.message ?: "No pudimos actualizar la cantidad"))
                }
            }
        }

        fun removeItem(cartItemId: Long) {
            viewModelScope.launch {
                val companyId = getCompanyIdOrNull() ?: return@launch
                try {
                    cartRepository.removeItem(cartItemId, companyId)
                    _events.emit(CartEvent.ShowSnackbar("Producto eliminado del carrito"))
                } catch (error: Throwable) {
                    if (error is CancellationException) throw error
                    _events.emit(CartEvent.ShowError(error.message ?: "No pudimos eliminar el producto"))
                }
            }
        }

        fun clearCart() {
            viewModelScope.launch {
                val companyId = getCompanyIdOrNull() ?: return@launch
                try {
                    cartRepository.clearCart(companyId)
                    _events.emit(CartEvent.CartCleared)
                } catch (error: Throwable) {
                    if (error is CancellationException) throw error
                    _events.emit(CartEvent.ShowError(error.message ?: "No pudimos vaciar el carrito"))
                }
            }
        }

        fun checkout() {
            viewModelScope.launch {
                _events.emit(CartEvent.NavigateToCheckout)
            }
        }

        private suspend fun getCompanyIdOrNull(): String? {
            val companyId = companyIdFlow.value
            return if (companyId.isNullOrBlank()) {
                _events.emit(CartEvent.ShowError("No pudimos identificar la tienda"))
                null
            } else {
                companyId
            }
        }
    }
