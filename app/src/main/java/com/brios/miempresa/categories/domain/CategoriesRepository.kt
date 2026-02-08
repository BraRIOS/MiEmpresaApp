package com.brios.miempresa.categories.domain

import com.brios.miempresa.core.data.local.entities.Category
import kotlinx.coroutines.flow.Flow

interface CategoriesRepository {
    fun getAll(companyId: String): Flow<List<Category>>

    suspend fun getById(
        id: String,
        companyId: String,
    ): Category?

    suspend fun create(category: Category)

    suspend fun update(category: Category)

    suspend fun delete(
        id: String,
        companyId: String,
    )

    suspend fun getProductCount(
        categoryId: String,
        companyId: String,
    ): Int

    suspend fun syncPendingChanges(companyId: String)
}
