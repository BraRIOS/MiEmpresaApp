package com.brios.miempresa.cart.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.cart.data.CartRepository
import com.brios.miempresa.cart.domain.CartEvent
import com.brios.miempresa.cart.domain.CartUiState
import com.brios.miempresa.core.data.local.daos.CompanyDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel
    @Inject
    constructor(
        private val cartRepository: CartRepository,
        private val companyDao: CompanyDao,
    ) : ViewModel() {
        // Get selected company ID
        private val companyIdFlow: StateFlow<String?> =
            companyDao.getSelectedCompany()
                .asFlow()
                .map { it?.id }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null,
                )

        // UI State (note: actual CartRepository doesn't have Flow methods, so we'll simplify)
        val uiState: StateFlow<CartUiState> =
            companyIdFlow
                .filterNotNull()
                .map { companyId ->
                    try {
                        val cartItems = cartRepository.getCartItems(companyId)
                        if (cartItems.isEmpty()) {
                            CartUiState.Empty
                        } else {
                            // Note: We don't have Product data yet (Task 2 incomplete)
                            // For now, just return entity data
                            // TODO: Map CartItemEntity to CartItem in Task 5
                            // TODO: Calculate from Product prices
                            CartUiState.Success(
                                items = emptyList(),
                                totalItems = cartItems.sumOf { it.quantity },
                                totalPrice = 0.0,
                                companyName = companyDao.getCompanyById(companyId)?.name ?: "Unknown",
                            )
                        }
                    } catch (e: Exception) {
                        CartUiState.Error(e.message ?: "Unknown error")
                    }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = CartUiState.Loading,
                )

        // One-off events
        private val _events = MutableSharedFlow<CartEvent>(replay = 0)
        val events: SharedFlow<CartEvent> = _events.asSharedFlow()

        // Badge count
        @OptIn(ExperimentalCoroutinesApi::class)
        val cartCount: StateFlow<Int> =
            companyIdFlow
                .filterNotNull()
                .flatMapLatest { companyId ->
                    cartRepository.observeCartCount(companyId)
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = 0,
                )

        fun addProduct(
            productId: String,
            quantity: Int = 1,
        ) {
            viewModelScope.launch {
                val companyId = companyIdFlow.value ?: return@launch
                try {
                    cartRepository.addItem(companyId, productId, quantity)
                    _events.emit(CartEvent.ShowSnackbar("Product added to cart"))
                } catch (e: Exception) {
                    _events.emit(CartEvent.ShowError(e.message ?: "Failed to add product"))
                }
            }
        }

        fun updateQuantity(
            cartItemId: Long,
            newQuantity: Int,
        ) {
            viewModelScope.launch {
                val companyId = companyIdFlow.value ?: return@launch
                try {
                    cartRepository.updateQuantity(cartItemId, companyId, newQuantity)
                    if (newQuantity <= 0) {
                        _events.emit(CartEvent.ShowSnackbar("Item removed from cart"))
                    }
                } catch (e: Exception) {
                    _events.emit(CartEvent.ShowError(e.message ?: "Failed to update quantity"))
                }
            }
        }

        fun removeItem(cartItemId: Long) {
            viewModelScope.launch {
                val companyId = companyIdFlow.value ?: return@launch
                try {
                    cartRepository.removeItem(cartItemId, companyId)
                    _events.emit(CartEvent.ShowSnackbar("Item removed from cart"))
                } catch (e: Exception) {
                    _events.emit(CartEvent.ShowError(e.message ?: "Failed to remove item"))
                }
            }
        }

        fun clearCart() {
            viewModelScope.launch {
                val companyId = companyIdFlow.value ?: return@launch
                try {
                    cartRepository.clearCart(companyId)
                    _events.emit(CartEvent.CartCleared)
                } catch (e: Exception) {
                    _events.emit(CartEvent.ShowError(e.message ?: "Failed to clear cart"))
                }
            }
        }

        fun checkout() {
            viewModelScope.launch {
                _events.emit(CartEvent.NavigateToCheckout)
            }
        }
    }
