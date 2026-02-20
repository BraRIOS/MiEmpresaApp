package com.brios.miempresa.products.domain

import com.brios.miempresa.products.data.ProductEntity
import javax.inject.Inject

enum class ProductSaveMode {
    Create,
    Update,
}

data class SaveProductRequest(
    val mode: ProductSaveMode,
    val companyId: String,
    val name: String,
    val price: Double,
    val hidePrice: Boolean,
    val description: String?,
    val categoryId: String?,
    val isPublic: Boolean,
    val localImagePath: String?,
    val imageRemoved: Boolean,
    val existingProduct: ProductEntity? = null,
)

data class SaveProductResult(
    val product: ProductEntity,
    val uploadFailed: Boolean,
)

class SaveProductUseCase
    @Inject
    constructor(
        private val productsRepository: ProductsRepository,
    ) {
        suspend operator fun invoke(request: SaveProductRequest): SaveProductResult? {
            val existing = request.existingProduct
            if (request.mode == ProductSaveMode.Update && existing == null) return null

            var finalImageUrl: String? = null
            var finalDriveImageId: String? = null
            var uploadFailed = false
            var driveImageIdToDelete: String? = null

            if (request.localImagePath != null) {
                val uploadedDriveImageId =
                    productsRepository.uploadProductImage(
                        companyId = request.companyId,
                        localImagePath = request.localImagePath,
                        productName = request.name,
                    )

                if (uploadedDriveImageId != null) {
                    finalDriveImageId = uploadedDriveImageId
                    finalImageUrl = "https://lh3.googleusercontent.com/d/$uploadedDriveImageId"
                    val originalDriveImageId = existing?.driveImageId
                    if (!originalDriveImageId.isNullOrBlank() && originalDriveImageId != uploadedDriveImageId) {
                        driveImageIdToDelete = originalDriveImageId
                    }
                } else {
                    uploadFailed = true
                }
            } else if (request.mode == ProductSaveMode.Update) {
                if (request.imageRemoved) {
                    finalImageUrl = null
                    finalDriveImageId = null
                    driveImageIdToDelete = existing?.driveImageId
                } else {
                    finalImageUrl = existing?.imageUrl
                    finalDriveImageId = existing?.driveImageId
                }
            }

            val product =
                if (request.mode == ProductSaveMode.Update) {
                    existing!!.copy(
                        name = request.name,
                        price = request.price,
                        hidePrice = request.hidePrice,
                        description = request.description,
                        categoryId = request.categoryId,
                        isPublic = request.isPublic,
                        imageUrl = finalImageUrl,
                        driveImageId = finalDriveImageId,
                        localImagePath = if (uploadFailed) request.localImagePath else null,
                        dirty = uploadFailed || existing.dirty,
                    )
                } else {
                    ProductEntity(
                        id = "",
                        name = request.name,
                        price = request.price,
                        companyId = request.companyId,
                        description = request.description,
                        categoryId = request.categoryId,
                        isPublic = request.isPublic,
                        hidePrice = request.hidePrice,
                        imageUrl = finalImageUrl,
                        driveImageId = finalDriveImageId,
                        localImagePath = if (uploadFailed) request.localImagePath else null,
                        dirty = uploadFailed,
                    )
                }

            if (request.mode == ProductSaveMode.Update) {
                productsRepository.update(product)
            } else {
                productsRepository.create(product)
            }

            if (!driveImageIdToDelete.isNullOrBlank()) {
                productsRepository.deleteProductImage(driveImageIdToDelete)
            }

            return SaveProductResult(
                product = product,
                uploadFailed = uploadFailed,
            )
        }
    }

