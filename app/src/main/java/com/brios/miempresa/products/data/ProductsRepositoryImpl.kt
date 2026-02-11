package com.brios.miempresa.products.data

import com.brios.miempresa.categories.data.CategoryDao
import com.brios.miempresa.core.api.drive.DriveApi
import com.brios.miempresa.core.api.sheets.SpreadsheetsApi
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.di.IoDispatcher
import com.brios.miempresa.products.domain.ProductsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class ProductsRepositoryImpl
    @Inject
    constructor(
        private val productDao: ProductDao,
        private val categoryDao: CategoryDao,
        private val companyDao: CompanyDao,
        private val sheetsApi: SpreadsheetsApi,
        private val driveApi: DriveApi,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
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

        override suspend fun syncPendingChanges(companyId: String) =
            withContext(ioDispatcher) {
                val company = companyDao.getCompanyById(companyId) ?: return@withContext
                val privateSheetId = company.privateSheetId ?: return@withContext
                val publicSheetId = company.publicSheetId

                val allProducts = productDao.getAllByCompany(companyId).filter { !it.deleted }

                val privateRows =
                    allProducts.mapIndexed { index, p ->
                        val rowNum = index + 2
                        val vlookupFormula = "=VLOOKUP(E$rowNum,Categories!A:D,4,FALSE)&\" \"&VLOOKUP(E$rowNum,Categories!A:B,2,FALSE)"
                        listOf<Any>(
                            p.id,
                            p.name,
                            p.description ?: "",
                            p.price,
                            p.categoryId ?: "",
                            vlookupFormula,
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
                sheetsApi.hideColumns(privateSheetId, PRODUCTS_TAB, listOf(0, 4))

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

        override suspend fun downloadFromSheets(companyId: String) =
            withContext(ioDispatcher) {
                val company = companyDao.getCompanyById(companyId) ?: return@withContext
                val privateSheetId = company.privateSheetId ?: return@withContext

                val sheetRows = sheetsApi.readRange(privateSheetId, "$PRODUCTS_TAB!A2:H") ?: return@withContext
                val sheetProductIds = mutableSetOf<String>()

                for (row in sheetRows) {
                    if (row.size < 4) continue
                    val id = row[0]?.toString() ?: continue
                    val name = row[1]?.toString() ?: continue
                    val description = row.getOrNull(2)?.toString() ?: ""
                    val price = row[3]?.toString()?.toDoubleOrNull() ?: 0.0
                    val categoryId = row.getOrNull(4)?.toString()?.takeIf { it.isNotBlank() }
                    // row[5] is CategoryName formula (skip)
                    val isPublic = row.getOrNull(6)?.toString()?.equals("TRUE", ignoreCase = true) ?: true
                    val imageUrl = row.getOrNull(7)?.toString()?.takeIf { it.isNotBlank() }
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

        override suspend fun uploadProductImage(
            companyId: String,
            localImagePath: String,
            productName: String,
        ): String? =
            withContext(ioDispatcher) {
                try {
                    val file = File(localImagePath)
                    if (!file.exists()) return@withContext null

                    // Get or create Images/Products/ folder
                    val productsFolderId = getOrCreateProductsFolder(companyId)
                        ?: return@withContext null

                    // Upload to Drive
                    val fileName = "${sanitizeFilename(productName)}_${System.currentTimeMillis()}.jpg"
                    val fileId = driveApi.uploadFile(
                        file = file,
                        mimeType = "image/jpeg",
                        parentFolderId = productsFolderId,
                        fileName = fileName,
                    )

                    // Make file publicly readable and delete local file only if both succeed
                    if (fileId != null) {
                        val isPublic = driveApi.makeFilePublic(fileId)
                        if (isPublic) {
                            file.delete()
                            fileId
                        } else {
                            null // Return null so product stays dirty for retry
                        }
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

        private suspend fun getOrCreateProductsFolder(companyId: String): String? {
            val company = companyDao.getCompanyById(companyId) ?: return null

            // Return cached folder ID if exists
            if (company.productsFolderId != null) {
                return company.productsFolderId
            }

            // Find or create Images/ folder, then Products/ subfolder
            val driveFolderId = company.driveFolderId ?: return null
            val imagesFolder = driveApi.createCompanyFolder(
                parentFolderId = driveFolderId,
                companyName = "Images",
            ) ?: return null

            val productsFolder = driveApi.createCompanyFolder(
                parentFolderId = imagesFolder.id,
                companyName = "Products",
            ) ?: return null

            // Cache folder ID in Company entity
            companyDao.update(company.copy(productsFolderId = productsFolder.id))

            return productsFolder.id
        }

        private fun sanitizeFilename(name: String): String {
            return name
                .replace(Regex("[^a-zA-Z0-9áéíóúñÁÉÍÓÚÑ ]"), "")
                .replace(" ", "_")
                .take(50)
        }

        companion object {
            private const val PRODUCTS_TAB = "Products"
            private val PRIVATE_PRODUCTS_HEADERS =
                listOf("ProductID", "Name", "Description", "Price", "CategoryID", "CategoryName", "Publico", "ImageUrl")
            private val PUBLIC_PRODUCTS_HEADERS =
                listOf("Name", "Description", "Price", "Category", "ImageUrl")
        }
    }
