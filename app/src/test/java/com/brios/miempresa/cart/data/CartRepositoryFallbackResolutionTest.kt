package com.brios.miempresa.cart.data

import com.brios.miempresa.products.data.ProductEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CartRepositoryFallbackResolutionTest {
    @Test
    fun `recoverMissingProductsByName maps missing id when there is a unique category match`() {
        val requestedIds = setOf("legacy-cart-id")
        val localProducts =
            listOf(
                product(
                    id = "legacy-cart-id",
                    name = "Producto de prueba",
                    category = "MESA DULCE",
                    price = 400.0,
                ),
            )
        val syncedProducts = emptyList<ProductEntity>()
        val publicProducts =
            listOf(
                product(
                    id = "new-sheet-id",
                    name = "Producto de prueba",
                    category = "MESA DULCE",
                    price = 500.0,
                ),
            )

        val recovered =
            recoverMissingProductsByName(
                requestedIds = requestedIds,
                localProducts = localProducts,
                syncedProducts = syncedProducts,
                publicProducts = publicProducts,
            ).associateBy { it.id }

        assertTrue(recovered.containsKey("legacy-cart-id"))
        assertEquals(500.0, recovered["legacy-cart-id"]?.price)
    }

    @Test
    fun `recoverMissingProductsByName does not map when candidates are ambiguous`() {
        val requestedIds = setOf("legacy-cart-id")
        val localProducts =
            listOf(
                product(
                    id = "legacy-cart-id",
                    name = "Producto de prueba",
                    category = "MESA DULCE",
                    price = 400.0,
                ),
            )
        val syncedProducts = emptyList<ProductEntity>()
        val publicProducts =
            listOf(
                product(
                    id = "new-sheet-id-1",
                    name = "Producto de prueba",
                    category = "MESA DULCE",
                    price = 500.0,
                ),
                product(
                    id = "new-sheet-id-2",
                    name = "Producto de prueba",
                    category = "MESA DULCE",
                    price = 520.0,
                ),
            )

        val recovered =
            recoverMissingProductsByName(
                requestedIds = requestedIds,
                localProducts = localProducts,
                syncedProducts = syncedProducts,
                publicProducts = publicProducts,
            )

        assertFalse(recovered.any { it.id == "legacy-cart-id" })
    }

    @Test
    fun `recoverMissingProductsByName keeps already synced ids unchanged`() {
        val requestedIds = setOf("legacy-cart-id", "already-synced")
        val localProducts =
            listOf(
                product(
                    id = "legacy-cart-id",
                    name = "Producto de prueba",
                    category = "MESA DULCE",
                    price = 400.0,
                ),
                product(
                    id = "already-synced",
                    name = "Arrollado",
                    category = "MESA SALADA",
                    price = 12800.0,
                ),
            )
        val syncedProducts =
            listOf(
                product(
                    id = "already-synced",
                    name = "Arrollado",
                    category = "MESA SALADA",
                    price = 13000.0,
                ),
            )
        val publicProducts =
            listOf(
                product(
                    id = "new-sheet-id",
                    name = "Producto de prueba",
                    category = "MESA DULCE",
                    price = 500.0,
                ),
                product(
                    id = "already-synced",
                    name = "Arrollado",
                    category = "MESA SALADA",
                    price = 13000.0,
                ),
            )

        val recovered =
            recoverMissingProductsByName(
                requestedIds = requestedIds,
                localProducts = localProducts,
                syncedProducts = syncedProducts,
                publicProducts = publicProducts,
            ).associateBy { it.id }

        assertEquals(13000.0, recovered["already-synced"]?.price)
        assertEquals(500.0, recovered["legacy-cart-id"]?.price)
    }

    private fun product(
        id: String,
        name: String,
        category: String?,
        price: Double,
    ): ProductEntity =
        ProductEntity(
            id = id,
            name = name,
            price = price,
            companyId = "company-1",
            categoryName = category,
            isPublic = true,
            deleted = false,
            dirty = false,
        )
}
