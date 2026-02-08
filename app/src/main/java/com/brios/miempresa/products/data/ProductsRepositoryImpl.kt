package com.brios.miempresa.products.data

import com.brios.miempresa.core.api.sheets.SpreadsheetsApi
import com.brios.miempresa.core.data.local.daos.CategoryDao
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.data.local.daos.ProductDao
import com.brios.miempresa.core.data.local.entities.ProductEntity
import com.brios.miempresa.products.domain.ProductsRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class ProductsRepositoryImpl
    @Inject
    constructor(
        private val productDao: ProductDao,
        private val categoryDao: CategoryDao,
        private val companyDao: CompanyDao,
        private val sheetsApi: SpreadsheetsApi,
    ) : ProductsRepository {
        override fun getAll(companyId: String): Flow<List<ProductEntity>> = productDao.getAllByCompanyFlow(companyId)

        override suspend fun getById(
            id: String,
            companyId: String,
        ): ProductEntity? = productDao.getById(id, companyId)

        override suspend fun create(product: ProductEntity) {
            val newProduct =
                product.copy(
                    id = UUID.randomUUID().toString(),
                    dirty = true,
                )
            productDao.upsert(newProduct)
        }

        override suspend fun update(product: ProductEntity) {
            productDao.upsert(product.copy(dirty = true))
        }

        override suspend fun delete(
            id: String,
            companyId: String,
        ) {
            val existing = productDao.getById(id, companyId) ?: return
            productDao.upsert(existing.copy(deleted = true, dirty = true))
        }

        override suspend fun togglePublic(
            id: String,
            companyId: String,
            isPublic: Boolean,
        ) {
            val existing = productDao.getById(id, companyId) ?: return
            productDao.upsert(existing.copy(publico = isPublic, dirty = true))
        }

        override suspend fun syncPendingChanges(companyId: String) {
            val company = companyDao.getCompanyById(companyId) ?: return
            val privateSheetId = company.privateSheetId ?: return
            val publicSheetId = company.publicSheetId

            val allProducts = productDao.getAllByCompany(companyId).filter { !it.deleted }

            val categoryCache =
                categoryDao.getAll(companyId).associateBy { it.id }

            // Write ALL products to private sheet
            val privateRows =
                allProducts.map { p ->
                    listOf<Any>(
                        p.name,
                        p.description ?: "",
                        p.price,
                        categoryCache[p.categoryId]?.name ?: "",
                        p.driveImageId ?: p.imageUrl ?: "",
                    )
                }

            sheetsApi.clearAndWriteAll(
                spreadsheetId = privateSheetId,
                tabName = PRODUCTS_TAB,
                headers = PRODUCTS_HEADERS,
                rows = privateRows,
            )

            // Write only public products to public sheet
            if (publicSheetId != null) {
                val publicRows =
                    allProducts.filter { it.publico }.map { p ->
                        listOf<Any>(
                            p.name,
                            p.description ?: "",
                            p.price,
                            categoryCache[p.categoryId]?.name ?: "",
                            p.driveImageId ?: p.imageUrl ?: "",
                        )
                    }

                sheetsApi.clearAndWriteAll(
                    spreadsheetId = publicSheetId,
                    tabName = PRODUCTS_TAB,
                    headers = PRODUCTS_HEADERS,
                    rows = publicRows,
                )
            }

            val dirtyIds = productDao.getDirty(companyId).map { it.id }
            if (dirtyIds.isNotEmpty()) {
                productDao.markSynced(
                    ids = dirtyIds,
                    timestamp = System.currentTimeMillis(),
                    companyId = companyId,
                )
            }
        }

        companion object {
            private const val PRODUCTS_TAB = "Products"
            private val PRODUCTS_HEADERS = listOf("Name", "Description", "Price", "Category", "ImageId")
        }
    }
