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
        private val maxQuantityPerProduct = 99

        suspend fun getCurrentQuantityForProduct(
            companyId: String,
            productId: String,
        ): Int = cartItemDao.getQuantityByProductId(companyId, productId) ?: 0

        suspend fun addItem(
            companyId: String,
            productId: String,
            quantity: Int,
        ): Long {
            val quantityToAdd = quantity.coerceAtLeast(1)
            val existing = cartItemDao.getByProductId(companyId, productId)
            return if (existing != null) {
                val updatedQuantity = (existing.quantity + quantityToAdd).coerceAtMost(maxQuantityPerProduct)
                cartItemDao.update(existing.copy(quantity = updatedQuantity))
                existing.id
            } else {
                val item =
                    CartItemEntity(
                        companyId = companyId,
                        productId = productId,
                        quantity = quantityToAdd.coerceAtMost(maxQuantityPerProduct),
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
            val clampedQuantity = newQuantity.coerceIn(1, maxQuantityPerProduct)
            cartItemDao.update(item.copy(quantity = clampedQuantity))
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
                val updatedProductsById = sheetsApi.getProductsByIds(spreadsheetId, productIds, companyId)
                val updatedProducts =
                    if (updatedProductsById.size == productIds.distinct().size) {
                        updatedProductsById
                    } else {
                        val publicProducts = sheetsApi.readPublicProducts(spreadsheetId, companyId)
                        recoverMissingProductsByName(
                            requestedIds = productIds.toSet(),
                            localProducts = localProducts,
                            syncedProducts = updatedProductsById,
                            publicProducts = publicProducts,
                        )
                    }

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

internal fun recoverMissingProductsByName(
    requestedIds: Set<String>,
    localProducts: List<ProductEntity>,
    syncedProducts: List<ProductEntity>,
    publicProducts: List<ProductEntity>,
): List<ProductEntity> {
    if (requestedIds.isEmpty()) return syncedProducts

    val resolvedProducts = syncedProducts.associateBy { it.id }.toMutableMap()
    val localProductsById = localProducts.associateBy { it.id }
    val publicProductsByName = publicProducts.groupBy { normalizeProductName(it.name) }
    val missingIds = requestedIds.filterNot(resolvedProducts::containsKey)

    missingIds.forEach { missingId ->
        val localProduct = localProductsById[missingId] ?: return@forEach
        val nameMatches = publicProductsByName[normalizeProductName(localProduct.name)].orEmpty()
        val categoryMatches =
            nameMatches.filter { candidate ->
                normalizeCategoryName(candidate.categoryName) == normalizeCategoryName(localProduct.categoryName)
            }
        val resolvedCandidate =
            when {
                categoryMatches.size == 1 -> categoryMatches.first()
                nameMatches.size == 1 -> nameMatches.first()
                else -> null
            }
        if (resolvedCandidate != null) {
            resolvedProducts[missingId] = resolvedCandidate.copy(id = missingId)
        }
    }

    return resolvedProducts.values.toList()
}

private fun normalizeProductName(value: String): String = value.trim().lowercase()

private fun normalizeCategoryName(value: String?): String = value?.trim()?.lowercase().orEmpty()
