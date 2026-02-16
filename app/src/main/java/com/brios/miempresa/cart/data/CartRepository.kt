package com.brios.miempresa.cart.data

import android.util.Log
import com.brios.miempresa.cart.domain.PriceChange
import com.brios.miempresa.cart.domain.PriceValidationResult
import com.brios.miempresa.cart.domain.UnavailableProduct
import com.brios.miempresa.core.api.sheets.SpreadsheetsApi
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.network.NetworkMonitor
import com.brios.miempresa.products.data.ProductDao
import com.brios.miempresa.products.data.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.cancellation.CancellationException
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
        private val networkMonitor: NetworkMonitor,
    ) {
        suspend fun addItem(
            companyId: String,
            productId: String,
            quantity: Int,
        ): Long {
            val existing = cartItemDao.getByProductId(companyId, productId)
            return if (existing != null) {
                cartItemDao.update(existing.copy(quantity = existing.quantity + quantity))
                existing.id
            } else {
                val item =
                    CartItemEntity(
                        companyId = companyId,
                        productId = productId,
                        quantity = quantity,
                    )
                cartItemDao.insert(item)
            }
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

        fun observeCartItems(companyId: String): Flow<List<CartItemWithProduct>> =
            cartItemDao.observeAllWithProducts(companyId)

        fun observeCartCount(companyId: String): Flow<Int> =
            cartItemDao.observeItemCount(companyId)

        fun observeOnlineStatus(): Flow<Boolean> = networkMonitor.observeOnlineStatus()

        fun isOnlineNow(): Boolean = networkMonitor.isOnlineNow()

        suspend fun validateCartPrices(
            companyId: String,
            spreadsheetId: String,
        ): PriceValidationResult {
            // Require a valid company and cart before validating.
            if (companyDao.getCompanyById(companyId) == null) {
                return PriceValidationResult.Blocked
            }
            val cartItems = cartItemDao.getAll(companyId)
            if (cartItems.isEmpty()) {
                return PriceValidationResult.AllValid
            }

            // Cart validation always performs a partial sync for cart items.
            if (!isOnline()) {
                return PriceValidationResult.Blocked
            }

            return try {
                val productIds = cartItems.map { it.productId }
                val localProducts = productDao.getByIds(productIds, companyId)

                // Fetch updated products from Sheets (PARTIAL SYNC)
                val updatedProducts = sheetsApi.getProductsByIds(spreadsheetId, productIds, companyId)

                // Detect partial API response
                if (updatedProducts.size != productIds.size) {
                    Log.w("CartRepository", "Partial API response: expected ${productIds.size}, got ${updatedProducts.size}")
                    // Continue processing - detectPriceChanges will mark missing as unavailable
                }

                if (updatedProducts.isNotEmpty()) {
                    // Update Room with latest public prices for instant UX feedback
                    productDao.upsertAll(updatedProducts)
                    companyDao.updateLastSyncedAt(companyId, System.currentTimeMillis())
                }

                // Detect changes
                detectPriceChanges(
                    cartItems = cartItems,
                    localProducts = localProducts,
                    updatedProducts = updatedProducts,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("CartRepository", "Price validation failed", e)
                PriceValidationResult.Blocked
            }
        }

        private fun isOnline(): Boolean {
            return networkMonitor.isOnlineNow()
        }

        private fun detectPriceChanges(
            cartItems: List<CartItemEntity>,
            localProducts: List<ProductEntity>,
            updatedProducts: List<ProductEntity>,
        ): PriceValidationResult {
            val updatedMap = updatedProducts.associateBy { it.id }
            val localMap = localProducts.associateBy { it.id }

            val priceChanges = mutableListOf<PriceChange>()
            val unavailable = mutableListOf<UnavailableProduct>()

            cartItems.forEach { cartItem ->
                val updated = updatedMap[cartItem.productId]
                val local = localMap[cartItem.productId]

                when {
                    // Product not found in Sheets (deleted/hidden)
                    updated == null || updated.deleted -> {
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
