package com.brios.miempresa.products.ui

import com.brios.miempresa.products.data.ProductEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProductDetailImageSourceResolverTest {
    @Test
    fun resolvesLocalImagePathFirst() {
        val product =
            ProductEntity(
                id = "product-1",
                name = "Product",
                price = 100.0,
                companyId = "company-1",
                localImagePath = "/data/user/0/com.brios.miempresa/files/product.jpg",
                imageUrl = "https://example.com/product.jpg",
            )

        val source = resolveProductDetailImageSource(product)

        assertEquals("/data/user/0/com.brios.miempresa/files/product.jpg", source)
    }

    @Test
    fun resolvesRemoteImageWhenLocalPathIsBlank() {
        val product =
            ProductEntity(
                id = "product-1",
                name = "Product",
                price = 100.0,
                companyId = "company-1",
                localImagePath = "   ",
                imageUrl = "https://example.com/product.jpg",
            )

        val source = resolveProductDetailImageSource(product)

        assertEquals("https://example.com/product.jpg", source)
    }

    @Test
    fun returnsNullWhenSourcesAreEmpty() {
        val product =
            ProductEntity(
                id = "product-1",
                name = "Product",
                price = 100.0,
                companyId = "company-1",
                localImagePath = "",
                imageUrl = " ",
            )

        val source = resolveProductDetailImageSource(product)

        assertNull(source)
    }
}
