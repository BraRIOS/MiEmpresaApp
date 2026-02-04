package com.brios.miempresa.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CartRepositoryTest {
    private lateinit var cartItemDao: CartItemDao
    private lateinit var repository: CartRepository

    private val testCompanyId = "company-123"
    private val testProductId = "product-456"

    @Before
    fun setup() {
        cartItemDao = mock()
        repository = CartRepository(cartItemDao)
    }

    @Test
    fun `addItem creates new cart item with correct data`() =
        runTest {
            val expectedId = 1L
            whenever(cartItemDao.insert(any())).thenReturn(expectedId)

            val resultId = repository.addItem(testCompanyId, testProductId, 2)

            assertEquals(expectedId, resultId)
            verify(cartItemDao).insert(any())
        }

    @Test
    fun `updateQuantity updates existing item`() =
        runTest {
            val existingItem =
                CartItemEntity(
                    id = 1L,
                    companyId = testCompanyId,
                    productId = testProductId,
                    quantity = 1,
                )
            whenever(cartItemDao.getById(1L, testCompanyId)).thenReturn(existingItem)

            repository.updateQuantity(1L, testCompanyId, 3)

            verify(cartItemDao).update(existingItem.copy(quantity = 3))
        }

    @Test
    fun `updateQuantity does nothing when item not found`() =
        runTest {
            whenever(cartItemDao.getById(999L, testCompanyId)).thenReturn(null)

            repository.updateQuantity(999L, testCompanyId, 3)

            verify(cartItemDao).getById(999L, testCompanyId)
        }

    @Test
    fun `removeItem deletes existing item`() =
        runTest {
            val existingItem =
                CartItemEntity(
                    id = 1L,
                    companyId = testCompanyId,
                    productId = testProductId,
                    quantity = 1,
                )
            whenever(cartItemDao.getById(1L, testCompanyId)).thenReturn(existingItem)

            repository.removeItem(1L, testCompanyId)

            verify(cartItemDao).delete(existingItem)
        }

    @Test
    fun `getCartItems returns all items for company`() =
        runTest {
            val items =
                listOf(
                    CartItemEntity(1L, testCompanyId, "prod-1", 1),
                    CartItemEntity(2L, testCompanyId, "prod-2", 2),
                )
            whenever(cartItemDao.getAll(testCompanyId)).thenReturn(items)

            val result = repository.getCartItems(testCompanyId)

            assertEquals(2, result.size)
            assertEquals(items, result)
        }

    @Test
    fun `getCartItemsWithProducts returns JOIN query results`() =
        runTest {
            val itemsWithProducts =
                listOf(
                    CartItemWithProduct(1L, "prod-1", 1, 123456L, "Product 1", 10.0),
                    CartItemWithProduct(2L, "prod-2", 2, 123457L, "Product 2", 20.0),
                )
            whenever(cartItemDao.getAllWithProducts(testCompanyId)).thenReturn(itemsWithProducts)

            val result = repository.getCartItemsWithProducts(testCompanyId)

            assertEquals(2, result.size)
            assertNotNull(result[0].productName)
            assertEquals("Product 1", result[0].productName)
        }

    @Test
    fun `clearCart deletes all items for company`() =
        runTest {
            repository.clearCart(testCompanyId)

            verify(cartItemDao).deleteAll(testCompanyId)
        }

    @Test
    fun `observeCartCount emits current cart size`() =
        runTest {
            val items =
                listOf(
                    CartItemEntity(1L, testCompanyId, "prod-1", 1),
                    CartItemEntity(2L, testCompanyId, "prod-2", 2),
                )
            whenever(cartItemDao.getAll(testCompanyId)).thenReturn(items)

            var emittedCount = 0
            repository.observeCartCount(testCompanyId).collect { count ->
                emittedCount = count
            }

            assertEquals(2, emittedCount)
        }

    @Test
    fun `multitenancy - operations filter by companyId`() =
        runTest {
            val company1 = "company-1"
            val company2 = "company-2"

            val items1 = listOf(CartItemEntity(1L, company1, "prod-1", 1))
            val items2 = listOf(CartItemEntity(2L, company2, "prod-2", 2))

            whenever(cartItemDao.getAll(company1)).thenReturn(items1)
            whenever(cartItemDao.getAll(company2)).thenReturn(items2)

            val result1 = repository.getCartItems(company1)
            val result2 = repository.getCartItems(company2)

            assertEquals(1, result1.size)
            assertEquals(company1, result1[0].companyId)

            assertEquals(1, result2.size)
            assertEquals(company2, result2[0].companyId)
        }
}
