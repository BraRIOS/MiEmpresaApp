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
            productDao.upsert(existing.copy(isPublic = isPublic, dirty = true))
        }

        override suspend fun syncPendingChanges(companyId: String) {
            val company = companyDao.getCompanyById(companyId) ?: return
            val privateSheetId = company.privateSheetId ?: return
            val publicSheetId = company.publicSheetId

            val allProducts = productDao.getAllByCompany(companyId).filter { !it.deleted }

            // Write ALL products to private sheet (with IDs)
            val privateRows =
                allProducts.map { p ->
                    listOf<Any>(
                        p.id,
                        p.name,
                        p.description ?: "",
                        p.price,
                        p.categoryId ?: "",
                        p.isPublic.toString().uppercase(),
                        p.imageUrl ?: "",
                    )
                }

            sheetsApi.clearAndWriteAll(
                spreadsheetId = privateSheetId,
                tabName = PRODUCTS_TAB,
                headers = PRIVATE_PRODUCTS_HEADERS,
                rows = privateRows,
            )
            // Hide ProductID (A=0) and CategoryID (E=4) columns for clean admin UX
            sheetsApi.hideColumns(privateSheetId, PRODUCTS_TAB, listOf(0, 4))

            // Write only public products to public sheet (resolved names, no IDs)
            if (publicSheetId != null) {
                val categoryCache = categoryDao.getAll(companyId).associateBy { it.id }
                val publicRows =
                    allProducts.filter { it.isPublic }.map { p ->
                        listOf<Any>(
                            p.name,
                            p.description ?: "",
                            p.price,
                            categoryCache[p.categoryId]?.name ?: "",
                            p.imageUrl ?: "",
                        )
                    }

                sheetsApi.clearAndWriteAll(
                    spreadsheetId = publicSheetId,
                    tabName = PRODUCTS_TAB,
                    headers = PUBLIC_PRODUCTS_HEADERS,
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

        override suspend fun downloadFromSheets(companyId: String) {
            val company = companyDao.getCompanyById(companyId) ?: return
            val privateSheetId = company.privateSheetId ?: return

            val sheetRows = sheetsApi.readRange(privateSheetId, "$PRODUCTS_TAB!A2:G") ?: return
            val sheetProductIds = mutableSetOf<String>()

            for (row in sheetRows) {
                if (row.size < 4) continue
                val id = row[0]?.toString() ?: continue
                val name = row[1]?.toString() ?: continue
                val description = row.getOrNull(2)?.toString() ?: ""
                val price = row[3]?.toString()?.toDoubleOrNull() ?: 0.0
                val categoryId = row.getOrNull(4)?.toString()?.takeIf { it.isNotBlank() }
                val isPublic = row.getOrNull(5)?.toString()?.equals("TRUE", ignoreCase = true) ?: true
                val imageUrl = row.getOrNull(6)?.toString()?.takeIf { it.isNotBlank() }
                sheetProductIds.add(id)

                val existing = productDao.getById(id, companyId)
                if (existing != null) {
                    if (!existing.dirty) {
                        productDao.upsert(
                            existing.copy(
                                name = name,
                                description = description,
                                price = price,
                                categoryId = categoryId,
                                isPublic = isPublic,
                                imageUrl = imageUrl,
                                lastSyncedAt = System.currentTimeMillis(),
                            ),
                        )
                    }
                } else {
                    productDao.upsert(
                        ProductEntity(
                            id = id,
                            name = name,
                            description = description,
                            price = price,
                            categoryId = categoryId,
                            isPublic = isPublic,
                            imageUrl = imageUrl,
                            companyId = companyId,
                            lastSyncedAt = System.currentTimeMillis(),
                        ),
                    )
                }
            }

            // Delete products in Room that are NOT in Sheet (and not dirty locally)
            val roomProducts = productDao.getAllByCompany(companyId)
            for (product in roomProducts) {
                if (product.id !in sheetProductIds && !product.dirty) {
                    productDao.deleteById(product.id, companyId)
                }
            }
        }

        companion object {
            private const val PRODUCTS_TAB = "Products"
            private val PRIVATE_PRODUCTS_HEADERS =
                listOf("ProductID", "Name", "Description", "Price", "CategoryID", "Publico", "ImageUrl")
            private val PUBLIC_PRODUCTS_HEADERS =
                listOf("Name", "Description", "Price", "Category", "ImageUrl")
        }
    }
