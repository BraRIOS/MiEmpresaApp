package com.brios.miempresa.products.domain

import com.brios.miempresa.products.data.ProductEntity
import kotlinx.coroutines.flow.Flow

interface ProductsRepository {
    fun getAll(companyId: String): Flow<List<ProductEntity>>

    fun getFiltered(
        companyId: String,
        searchQuery: String,
        categoryId: String?,
        isPublicFilter: Boolean?,
    ): Flow<List<ProductEntity>>

    fun getCategoryCountsByFilter(
        companyId: String,
        searchQuery: String,
        isPublicFilter: Boolean?,
    ): Flow<Map<String, Int>>

    suspend fun getById(
        id: String,
        companyId: String,
    ): ProductEntity?

    suspend fun create(product: ProductEntity)

    suspend fun update(product: ProductEntity)

    suspend fun delete(
        id: String,
        companyId: String,
    )

    suspend fun togglePublic(
        id: String,
        companyId: String,
        isPublic: Boolean,
    )

    suspend fun syncPendingChanges(companyId: String)

    suspend fun downloadFromSheets(companyId: String)

    suspend fun uploadProductImage(
        companyId: String,
        localImagePath: String,
        productName: String,
    ): String?
}
