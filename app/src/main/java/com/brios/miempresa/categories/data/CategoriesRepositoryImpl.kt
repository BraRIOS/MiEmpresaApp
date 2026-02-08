package com.brios.miempresa.categories.data

import com.brios.miempresa.categories.domain.CategoriesRepository
import com.brios.miempresa.core.api.sheets.SpreadsheetsApi
import com.brios.miempresa.core.data.local.daos.CategoryDao
import com.brios.miempresa.core.data.local.daos.CompanyDao
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
        private val companyDao: CompanyDao,
        private val sheetsApi: SpreadsheetsApi,
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
            val company = companyDao.getCompanyById(companyId) ?: return
            val privateSheetId = company.privateSheetId ?: return
            val allCategories = categoryDao.getAll(companyId)

            val rows =
                allCategories.map { cat ->
                    listOf<Any>(
                        cat.name,
                        cat.icon,
                        productDao.countByCategory(cat.id, companyId),
                    )
                }

            sheetsApi.clearAndWriteAll(
                spreadsheetId = privateSheetId,
                tabName = CATEGORIES_TAB,
                headers = CATEGORIES_HEADERS,
                rows = rows,
            )

            val dirtyIds = categoryDao.getDirty(companyId).map { it.id }
            if (dirtyIds.isNotEmpty()) {
                categoryDao.markSynced(
                    ids = dirtyIds,
                    timestamp = System.currentTimeMillis(),
                    companyId = companyId,
                )
            }
        }

        companion object {
            private const val CATEGORIES_TAB = "Categories"
            private val CATEGORIES_HEADERS = listOf("Name", "Icon", "ProductCount")
        }
    }
