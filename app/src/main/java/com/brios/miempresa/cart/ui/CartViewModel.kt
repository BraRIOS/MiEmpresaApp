package com.brios.miempresa.cart.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.cart.data.CartRepository
import com.brios.miempresa.cart.domain.CartEvent
import com.brios.miempresa.cart.domain.CartItem
import com.brios.miempresa.cart.domain.CartUiState
import com.brios.miempresa.cart.domain.PriceValidationResult
import com.brios.miempresa.cart.domain.WhatsAppHelper
import com.brios.miempresa.core.data.local.daos.CompanyDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
        private var validationJob: Job? = null
        private val routeCompanyId: String = savedStateHandle.get<String>("companyId").orEmpty()

        private val companyIdFlow: StateFlow<String?> =
            if (routeCompanyId.isNotBlank()) {
                MutableStateFlow(routeCompanyId)
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

        private val validationResult = MutableStateFlow<PriceValidationResult?>(null)
        private val isValidating = MutableStateFlow(false)
        private val isOnlineFlow =
            cartRepository.observeOnlineStatus()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = cartRepository.isOnlineNow(),
                )

        val uiState: StateFlow<CartUiState> =
            companyIdFlow
                .flatMapLatest { companyId ->
                    if (companyId.isNullOrBlank()) {
                        flowOf(CartUiState.Empty)
                    } else {
                        combine(
                            cartRepository.observeCartItems(companyId),
                            companyDao.observeCompanyById(companyId),
                            validationResult,
                            isValidating,
                            isOnlineFlow,
                        ) { cartItems, company, validation, validating, isOnline ->
                            val items =
                                cartItems.map { item ->
                                    CartItem(
                                        id = item.id,
                                        companyId = companyId,
                                        productId = item.productId,
                                        productName = item.productName ?: "Producto",
                                        productPrice = item.productPrice ?: 0.0,
                                        productHidePrice = item.productHidePrice,
                                        productImageUrl = item.productImageUrl,
                                        quantity = item.quantity,
                                        addedAt = item.addedAt,
                                    )
                                }

                            when {
                                validating -> CartUiState.Validating
                                items.isEmpty() -> CartUiState.Empty
                                company == null -> CartUiState.Error("No pudimos cargar la tienda")
                                else -> {
                                    val effectiveValidation =
                                        if (!isOnline) {
                                            PriceValidationResult.Blocked
                                        } else {
                                            validation
                                        }
                                    val blocked =
                                        !isOnline ||
                                            effectiveValidation is PriceValidationResult.Blocked ||
                                            effectiveValidation is PriceValidationResult.ItemsUnavailable
                                    CartUiState.Success(
                                        items = items,
                                        totalItems = items.sumOf { it.quantity },
                                        totalPrice = items.filterNot { it.productHidePrice }.sumOf { it.subtotal },
                                        hasHiddenPrices = items.any { it.productHidePrice },
                                        companyName = company.name,
                                        validationResult = effectiveValidation,
                                        blocked = blocked,
                                    )
                                }
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

        init {
            var wasOnline = isOnlineFlow.value
            viewModelScope.launch {
                isOnlineFlow.collect { isOnline ->
                    if (isOnline && !wasOnline) {
                        runCartValidation(showLoading = false)
                    }
                    wasOnline = isOnline
                }
            }
        }

        fun addProduct(
            productId: String,
            quantity: Int = 1,
        ) {
            viewModelScope.launch {
                val companyId = getCompanyIdOrNull() ?: return@launch
                try {
                    cartRepository.addItem(companyId, productId, quantity)
                    validationResult.value = null
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
                    validationResult.value = null
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
                    validationResult.value = null
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
                    validationResult.value = null
                    _events.emit(CartEvent.CartCleared)
                } catch (error: Throwable) {
                    if (error is CancellationException) throw error
                    _events.emit(CartEvent.ShowError(error.message ?: "No pudimos vaciar el carrito"))
                }
            }
        }

        fun validateOnEnter() {
            runCartValidation(showLoading = false)
        }

        fun retryValidation() {
            runCartValidation(showLoading = true)
        }

        private fun runCartValidation(showLoading: Boolean) {
            if (validationJob?.isActive == true) return
            validationJob =
                viewModelScope.launch {
                val companyId = companyIdFlow.value
                if (companyId.isNullOrBlank()) return@launch

                val company = companyDao.getCompanyById(companyId) ?: return@launch
                val publicSheetId = company.publicSheetId
                if (cartRepository.getCartItems(companyId).isEmpty()) {
                    validationResult.value = null
                    return@launch
                }

                if (publicSheetId.isNullOrBlank()) {
                    validationResult.value = PriceValidationResult.Blocked
                    return@launch
                }

                if (showLoading) {
                    isValidating.value = true
                }
                try {
                    validationResult.value = cartRepository.validateCartPrices(companyId, publicSheetId)
                } catch (error: Throwable) {
                    if (error is CancellationException) throw error
                } finally {
                    if (showLoading) {
                        isValidating.value = false
                    }
                }
                }
        }

        fun validateAndCheckout() {
            viewModelScope.launch {
                val companyId = getCompanyIdOrNull() ?: return@launch
                val company =
                    companyDao.getCompanyById(companyId) ?: run {
                        _events.emit(CartEvent.ShowError("No pudimos cargar los datos de la tienda"))
                        return@launch
                    }
                val publicSheetId = company.publicSheetId
                val normalizedPhone = normalizeWhatsAppPhone("${company.whatsappCountryCode}${company.whatsappNumber.orEmpty()}")
                if (normalizedPhone == null) {
                    _events.emit(CartEvent.ShowError("Esta tienda no tiene WhatsApp configurado"))
                    return@launch
                }
                if (publicSheetId.isNullOrBlank()) {
                    validationResult.value = PriceValidationResult.Blocked
                    _events.emit(CartEvent.ShowError("No pudimos verificar los precios de esta tienda"))
                    return@launch
                }

                isValidating.value = true
                try {
                    val result = cartRepository.validateCartPrices(companyId, publicSheetId)
                    validationResult.value = result

                    when (result) {
                        PriceValidationResult.AllValid -> proceedToWhatsApp(companyId, company.name, normalizedPhone)
                        is PriceValidationResult.PricesUpdated ->
                            _events.emit(CartEvent.ShowSnackbar("Algunos precios se actualizaron"))

                        is PriceValidationResult.ItemsUnavailable ->
                            _events.emit(CartEvent.ShowError("Algunos productos ya no están disponibles"))

                        PriceValidationResult.Blocked ->
                            _events.emit(CartEvent.ShowError("Conectate para verificar precios antes de enviar"))
                    }
                } catch (error: Throwable) {
                    if (error is CancellationException) throw error
                    _events.emit(CartEvent.ShowError(error.message ?: "No pudimos validar los precios"))
                } finally {
                    isValidating.value = false
                }
            }
        }

        private suspend fun proceedToWhatsApp(
            companyId: String,
            companyName: String,
            phoneNumber: String,
        ) {
            val items =
                cartRepository.getCartItemsWithProducts(companyId).map { item ->
                    CartItem(
                        id = item.id,
                        companyId = companyId,
                        productId = item.productId,
                        productName = item.productName ?: "Producto",
                        productPrice = item.productPrice ?: 0.0,
                        productHidePrice = item.productHidePrice,
                        productImageUrl = item.productImageUrl,
                        quantity = item.quantity,
                        addedAt = item.addedAt,
                    )
                }

            if (items.isEmpty()) {
                _events.emit(CartEvent.ShowError("Tu carrito está vacío"))
                return
            }

            val message = WhatsAppHelper.buildMessage(items = items, companyName = companyName)
            _events.emit(
                CartEvent.ProceedToWhatsApp(
                    phoneNumber = phoneNumber,
                    message = message,
                ),
            )
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

        private fun normalizeWhatsAppPhone(value: String): String? {
            val normalized = value.replace(Regex("\\D"), "")
            return normalized.takeIf { it.isNotBlank() }
        }
    }
