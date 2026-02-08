package com.brios.miempresa.categories.data

import com.brios.miempresa.categories.domain.CategoriesRepository
import com.brios.miempresa.core.data.local.daos.CategoryDao
import com.brios.miempresa.core.data.local.daos.ProductDao
import com.brios.miempresa.core.data.local.entities.Category
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class CategoriesRepositoryImpl
    @Inject
    constructor(
        private val categoryDao: CategoryDao,
        private val productDao: ProductDao,
    ) : CategoriesRepository {
        override fun getAll(companyId: String): Flow<List<Category>> = categoryDao.getAllFlow(companyId)

        override suspend fun getById(
            id: String,
            companyId: String,
        ): Category? = categoryDao.getById(id, companyId)

        override suspend fun create(category: Category) {
            val newCategory =
                category.copy(
                    id = UUID.randomUUID().toString(),
                    dirty = true,
                )
            categoryDao.upsert(newCategory)
        }

        override suspend fun update(category: Category) {
            categoryDao.upsert(category.copy(dirty = true))
        }

        override suspend fun delete(
            id: String,
            companyId: String,
        ) {
            val existing = categoryDao.getById(id, companyId) ?: return
            categoryDao.upsert(existing.copy(dirty = true))
        }

        override suspend fun getProductCount(
            categoryId: String,
            companyId: String,
        ): Int = productDao.countByCategory(categoryId, companyId)

        override suspend fun syncPendingChanges(companyId: String) {
            val dirtyCategories = categoryDao.getDirty(companyId)
            if (dirtyCategories.isEmpty()) return
            // TODO: Upload to Sheets in Sprint 2 task A2
            categoryDao.markSynced(
                ids = dirtyCategories.map { it.id },
                timestamp = System.currentTimeMillis(),
                companyId = companyId,
            )
        }
    }
