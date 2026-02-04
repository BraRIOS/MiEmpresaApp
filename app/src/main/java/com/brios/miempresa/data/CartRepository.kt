package com.brios.miempresa.data

import android.net.ConnectivityManager
import android.util.Log
import com.brios.miempresa.cart.domain.PriceChange
import com.brios.miempresa.cart.domain.PriceValidationResult
import com.brios.miempresa.cart.domain.UnavailableProduct
import com.brios.miempresa.domain.SpreadsheetsApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository
    @Inject
    constructor(
        private val cartItemDao: CartItemDao,
        private val companyDao: CompanyDao,
        private val productDao: ProductDao,
        private val sheetsApi: SpreadsheetsApi,
        private val connectivityManager: ConnectivityManager,
    ) {
        suspend fun addItem(
            companyId: String,
            productId: String,
            quantity: Int,
        ): Long {
            val item =
                CartItemEntity(
                    companyId = companyId,
                    productId = productId,
                    quantity = quantity,
                )
            return cartItemDao.insert(item)
        }

        suspend fun updateQuantity(
            id: Long,
            companyId: String,
            newQuantity: Int,
        ) {
            val item = cartItemDao.getById(id, companyId) ?: return
            cartItemDao.update(item.copy(quantity = newQuantity))
        }

        suspend fun removeItem(
            id: Long,
            companyId: String,
        ) {
            val item = cartItemDao.getById(id, companyId) ?: return
            cartItemDao.delete(item)
        }

        suspend fun getCartItems(companyId: String): List<CartItemEntity> = cartItemDao.getAll(companyId)

        suspend fun getCartItemsWithProducts(companyId: String): List<CartItemWithProduct> = cartItemDao.getAllWithProducts(companyId)

        suspend fun clearCart(companyId: String) {
            cartItemDao.deleteAll(companyId)
        }

        fun observeCartCount(companyId: String): Flow<Int> =
            flow {
                emit(cartItemDao.getAll(companyId).size)
            }

        suspend fun validateCartPrices(
            companyId: String,
            spreadsheetId: String,
        ): PriceValidationResult {
            // 1. Get last sync timestamp
            val company = companyDao.getCompanyById(companyId) ?: return PriceValidationResult.Blocked
            val lastSynced = company.lastSyncedAt ?: 0L
            val ageHours = (System.currentTimeMillis() - lastSynced) / (1000 * 60 * 60)

            // 2. Case 1: Fresh (<24h)
            if (ageHours <= 24) {
                return PriceValidationResult.AllValid
            }

            // 3. Case 3: Stale + Offline
            if (!isOnline()) {
                return PriceValidationResult.Blocked
            }

            // 4. Case 2: Stale + Online - Perform partial sync
            return try {
                val cartItems = cartItemDao.getAll(companyId)
                if (cartItems.isEmpty()) {
                    return PriceValidationResult.AllValid
                }

                val productIds = cartItems.map { it.productId }

                // Fetch updated products from Sheets (PARTIAL SYNC)
                val updatedProducts = sheetsApi.getProductsByIds(spreadsheetId, productIds, companyId)

                // Update Room (decision: always update for instant UX)
                productDao.upsertAll(updatedProducts)

                // Detect changes
                detectPriceChanges(cartItems, updatedProducts)
            } catch (e: Exception) {
                Log.e("CartRepository", "Price validation failed", e)
                PriceValidationResult.Blocked
            }
        }

        private fun isOnline(): Boolean {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        private suspend fun detectPriceChanges(
            cartItems: List<CartItemEntity>,
            updatedProducts: List<ProductEntity>,
        ): PriceValidationResult {
            val updatedMap = updatedProducts.associateBy { it.id }
            val localProducts = productDao.getByIds(cartItems.map { it.productId }, cartItems.first().companyId)
            val localMap = localProducts.associateBy { it.id }

            val priceChanges = mutableListOf<PriceChange>()
            val unavailable = mutableListOf<UnavailableProduct>()

            cartItems.forEach { cartItem ->
                val updated = updatedMap[cartItem.productId]
                val local = localMap[cartItem.productId]

                when {
                    // Product not found in Sheets (deleted/hidden)
                    updated == null || !updated.isAvailable -> {
                        unavailable.add(
                            UnavailableProduct(
                                productId = cartItem.productId,
                                productName = local?.name ?: "Unknown",
                                lastKnownPrice = local?.price ?: 0.0,
                            ),
                        )
                    }
                    // Price changed
                    local != null && updated.price != local.price -> {
                        priceChanges.add(
                            PriceChange(
                                productId = cartItem.productId,
                                productName = updated.name,
                                oldPrice = local.price,
                                newPrice = updated.price,
                                difference = updated.price - local.price,
                            ),
                        )
                    }
                }
            }

            return when {
                unavailable.isNotEmpty() -> {
                    val availableTotal =
                        cartItems
                            .filter { it.productId !in unavailable.map { u -> u.productId } }
                            .sumOf { item ->
                                val product = updatedMap[item.productId]
                                (product?.price ?: 0.0) * item.quantity
                            }
                    PriceValidationResult.ItemsUnavailable(unavailable, availableTotal)
                }
                priceChanges.isNotEmpty() -> {
                    val newTotal =
                        cartItems.sumOf { item ->
                            val product = updatedMap[item.productId]
                            (product?.price ?: 0.0) * item.quantity
                        }
                    PriceValidationResult.PricesUpdated(priceChanges, newTotal)
                }
                else -> PriceValidationResult.AllValid
            }
        }
    }
