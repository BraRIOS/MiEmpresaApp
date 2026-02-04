package com.brios.miempresa.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository
    @Inject
    constructor(
        private val cartItemDao: CartItemDao,
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
    }
