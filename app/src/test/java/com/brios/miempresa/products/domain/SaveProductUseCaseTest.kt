package com.brios.miempresa.products.domain

import com.brios.miempresa.products.data.ProductEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking

class SaveProductUseCaseTest {
    @Test
    fun `create mode uploads image and persists product with drive url`() =
        runTest {
            val repository =
                mock<ProductsRepository> {
                    onBlocking { uploadProductImage(any(), any(), any()) } doReturn "drive-123"
                }
            val useCase = SaveProductUseCase(repository)

            useCase(
                SaveProductRequest(
                    mode = ProductSaveMode.Create,
                    companyId = "company-1",
                    name = "Alfajor",
                    price = 1500.0,
                    hidePrice = false,
                    description = "Relleno ddl",
                    categoryId = "cat-1",
                    isPublic = true,
                    localImagePath = "C:\\tmp\\image.jpg",
                    imageRemoved = false,
                ),
            )

            val productCaptor = argumentCaptor<ProductEntity>()
            verifyBlocking(repository) { create(productCaptor.capture()) }
            val createdProduct = productCaptor.firstValue

            assertEquals("https://lh3.googleusercontent.com/d/drive-123", createdProduct.imageUrl)
            assertNull(createdProduct.localImagePath)
            assertFalse(createdProduct.dirty)
        }

    @Test
    fun `update mode removes existing drive image when image is removed`() =
        runTest {
            val repository = mock<ProductsRepository>()
            val useCase = SaveProductUseCase(repository)
            val existing =
                ProductEntity(
                    id = "prod-1",
                    companyId = "company-1",
                    name = "Producto",
                    price = 200.0,
                    categoryId = "cat-1",
                    imageUrl = "https://lh3.googleusercontent.com/d/old-drive",
                    driveImageId = "old-drive",
                    localImagePath = null,
                )

            useCase(
                SaveProductRequest(
                    mode = ProductSaveMode.Update,
                    companyId = "company-1",
                    name = "Producto editado",
                    price = 250.0,
                    hidePrice = false,
                    description = null,
                    categoryId = "cat-1",
                    isPublic = true,
                    localImagePath = null,
                    imageRemoved = true,
                    existingProduct = existing,
                ),
            )

            val productCaptor = argumentCaptor<ProductEntity>()
            verifyBlocking(repository) { update(productCaptor.capture()) }
            verifyBlocking(repository) { deleteProductImage("old-drive") }

            val updated = productCaptor.firstValue
            assertNull(updated.imageUrl)
            assertNull(updated.driveImageId)
        }
}

