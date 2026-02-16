package com.brios.miempresa.catalog.data

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.room.withTransaction
import com.brios.miempresa.BuildConfig
import com.brios.miempresa.cart.data.CartItemDao
import com.brios.miempresa.catalog.domain.CatalogAccessError
import com.brios.miempresa.catalog.domain.CatalogSyncException
import com.brios.miempresa.catalog.domain.ClientCatalogRepository
import com.brios.miempresa.core.api.sheets.PublicSheetHttpException
import com.brios.miempresa.core.api.sheets.SpreadsheetsApi
import com.brios.miempresa.core.data.local.MiEmpresaDatabase
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.di.IoDispatcher
import com.brios.miempresa.core.domain.model.defaultCountryCodes
import com.brios.miempresa.products.data.ProductDao
import com.brios.miempresa.products.data.ProductEntity
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class ClientCatalogRepositoryImpl
    @Inject
    constructor(
        private val spreadsheetsApi: SpreadsheetsApi,
        private val companyDao: CompanyDao,
        private val productDao: ProductDao,
        private val cartItemDao: CartItemDao,
        private val database: MiEmpresaDatabase,
        private val connectivityManager: ConnectivityManager,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ClientCatalogRepository {
        override suspend fun syncPublicSheet(publicSheetId: String): Result<Company> =
            withContext(ioDispatcher) {
                syncCatalog(
                    companyId = publicSheetId,
                    publicSheetId = publicSheetId,
                    updateLastVisited = true,
                )
            }

        override suspend fun refreshCatalog(
            companyId: String,
            publicSheetId: String,
        ): Result<Unit> =
            withContext(ioDispatcher) {
                syncCatalog(
                    companyId = companyId,
                    publicSheetId = publicSheetId,
                    updateLastVisited = false,
                ).fold(
                    onSuccess = { Result.success(Unit) },
                    onFailure = { Result.failure(it) },
                )
            }

        private suspend fun syncCatalog(
            companyId: String,
            publicSheetId: String,
            updateLastVisited: Boolean,
        ): Result<Company> {
            if (!isOnline()) {
                return Result.failure(
                    CatalogSyncException(
                        CatalogAccessError.NO_INTERNET_FIRST_VISIT,
                        "No internet connection available for first catalog sync.",
                    ),
                )
            }

            return try {
                val apiKey = BuildConfig.SHEETS_API_KEY.takeIf { it.isNotBlank() }
                val infoRows = spreadsheetsApi.readPublicRange(publicSheetId, INFO_RANGE, apiKey)
                val productRows = spreadsheetsApi.readPublicRange(publicSheetId, PRODUCTS_RANGE, apiKey)
                val now = System.currentTimeMillis()
                val existingCompany = companyDao.getCompanyById(companyId)

                val company =
                    buildCompany(
                        companyId = companyId,
                        publicSheetId = publicSheetId,
                        infoRows = infoRows,
                        existingCompany = existingCompany,
                        now = now,
                        updateLastVisited = updateLastVisited,
                    )
                val products =
                    buildProducts(
                        companyId = companyId,
                        rows = productRows,
                        now = now,
                    )
                val protectedCartProductIds =
                    cartItemDao
                        .getAll(companyId)
                        .map { it.productId }
                        .toSet()

                database.withTransaction {
                    companyDao.insertCompany(company)
                    val incomingIds = products.map(ProductEntity::id).toSet()
                    if (products.isNotEmpty()) {
                        productDao.upsertAll(products)
                    }
                    val staleIds =
                        productDao
                            .getPublicIdsByCompany(companyId)
                            .filterNot(incomingIds::contains)
                    if (staleIds.isNotEmpty()) {
                        val preserveForCartIds = staleIds.filter(protectedCartProductIds::contains)
                        val deleteIds = staleIds.filterNot(protectedCartProductIds::contains)

                        if (preserveForCartIds.isNotEmpty()) {
                            productDao.markPublicDeletedByIds(
                                companyId = companyId,
                                ids = preserveForCartIds,
                            )
                        }
                        if (deleteIds.isNotEmpty()) {
                            productDao.deletePublicByIds(
                                companyId = companyId,
                                ids = deleteIds,
                            )
                        }
                    }
                }

                Result.success(company)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(mapSyncError(e))
            }
        }

        private fun buildCompany(
            companyId: String,
            publicSheetId: String,
            infoRows: List<List<Any>>,
            existingCompany: Company?,
            now: Long,
            updateLastVisited: Boolean,
        ): Company {
            val infoMap =
                infoRows.associate { row ->
                    val key = row.getOrNull(0)?.toString()?.trim().orEmpty().lowercase()
                    val value = row.getOrNull(1)?.toString()?.trim().orEmpty()
                    key to value
                }

            val rawWhatsapp = infoMap["whatsapp_number"]?.takeIf { it.isNotBlank() }
            val (whatsappCountryCode, whatsappNumber) =
                parseWhatsapp(
                    rawWhatsapp = rawWhatsapp,
                    fallbackCountryCode = existingCompany?.whatsappCountryCode ?: DEFAULT_COUNTRY_CODE,
                    fallbackNumber = existingCompany?.whatsappNumber,
                )

            return Company(
                id = companyId,
                name = infoMap["name"]?.takeIf { it.isNotBlank() } ?: existingCompany?.name ?: publicSheetId,
                isOwned = false,
                selected = existingCompany?.selected ?: false,
                lastVisited =
                    if (updateLastVisited) {
                        now
                    } else {
                        existingCompany?.lastVisited
                    },
                lastSyncedAt = now,
                logoUrl = normalizeImageUrl(infoMap["logo_url"]) ?: existingCompany?.logoUrl,
                whatsappNumber = whatsappNumber,
                whatsappCountryCode = whatsappCountryCode,
                address = infoMap["address"]?.takeIf { it.isNotBlank() } ?: existingCompany?.address,
                businessHours = infoMap["business_hours"]?.takeIf { it.isNotBlank() } ?: existingCompany?.businessHours,
                publicSheetId = publicSheetId,
                privateSheetId = existingCompany?.privateSheetId,
                driveFolderId = existingCompany?.driveFolderId,
                productsFolderId = existingCompany?.productsFolderId,
                specialization = infoMap["specialization"]?.takeIf { it.isNotBlank() } ?: existingCompany?.specialization,
            )
        }

        private fun buildProducts(
            companyId: String,
            rows: List<List<Any>>,
            now: Long,
        ): List<ProductEntity> {
            return rows.mapIndexedNotNull { index, row ->
                val name = row.getOrNull(0)?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapIndexedNotNull null
                val description = row.getOrNull(1)?.toString()?.trim()?.takeIf { it.isNotEmpty() }
                val price = parsePrice(row.getOrNull(2)?.toString())
                val categoryName = row.getOrNull(3)?.toString()?.trim()?.takeIf { it.isNotEmpty() }
                val imageUrl = normalizeImageUrl(row.getOrNull(4)?.toString()?.trim())

                ProductEntity(
                    id = buildProductId(companyId, index, name, categoryName),
                    name = name,
                    description = description,
                    price = price,
                    categoryId = null,
                    categoryName = categoryName,
                    isPublic = true,
                    imageUrl = imageUrl,
                    companyId = companyId,
                    dirty = false,
                    deleted = false,
                    lastSyncedAt = now,
                )
            }
        }

        private fun parseWhatsapp(
            rawWhatsapp: String?,
            fallbackCountryCode: String,
            fallbackNumber: String?,
        ): Pair<String, String?> {
            if (rawWhatsapp.isNullOrBlank()) {
                return fallbackCountryCode to fallbackNumber
            }

            if (!rawWhatsapp.startsWith("+")) {
                return fallbackCountryCode to rawWhatsapp
            }

            val matchedCode =
                defaultCountryCodes
                    .map { it.dialCode }
                    .sortedByDescending { it.length }
                    .firstOrNull { rawWhatsapp.startsWith(it) }
                    ?: fallbackCountryCode

            return matchedCode to rawWhatsapp.removePrefix(matchedCode)
        }

        private fun normalizeImageUrl(value: String?): String? {
            if (value.isNullOrBlank()) return null
            if (value.startsWith("http://") || value.startsWith("https://")) return value
            return "https://lh3.googleusercontent.com/d/$value"
        }

        private fun parsePrice(rawPrice: String?): Double {
            if (rawPrice.isNullOrBlank()) return 0.0
            val normalized = rawPrice.replace(PRICE_CLEAN_REGEX, "").replace(',', '.')
            return normalized.toDoubleOrNull() ?: 0.0
        }

        private fun buildProductId(
            companyId: String,
            rowIndex: Int,
            name: String,
            categoryName: String?,
        ): String {
            val raw = "$companyId|$rowIndex|${name.lowercase()}|${categoryName.orEmpty().lowercase()}"
            return UUID.nameUUIDFromBytes(raw.toByteArray()).toString()
        }

        private fun mapSyncError(throwable: Throwable): CatalogSyncException {
            if (throwable is CatalogSyncException) return throwable

            if (throwable is GoogleJsonResponseException) {
                return when (throwable.statusCode) {
                    404 ->
                        CatalogSyncException(
                            CatalogAccessError.CATALOG_NOT_FOUND,
                            "Catalog spreadsheet was not found.",
                            throwable,
                        )

                    403 ->
                        CatalogSyncException(
                            CatalogAccessError.CATALOG_NOT_AVAILABLE,
                            "Catalog spreadsheet is not publicly available.",
                            throwable,
                        )

                    else ->
                        CatalogSyncException(
                            CatalogAccessError.CATALOG_NOT_AVAILABLE,
                            "Failed to read catalog spreadsheet.",
                            throwable,
                        )
                }
            }

            if (throwable is PublicSheetHttpException) {
                return when (throwable.statusCode) {
                    404 ->
                        CatalogSyncException(
                            CatalogAccessError.CATALOG_NOT_FOUND,
                            "Catalog spreadsheet was not found.",
                            throwable,
                        )

                    401, 403 ->
                        CatalogSyncException(
                            CatalogAccessError.CATALOG_NOT_AVAILABLE,
                            "Catalog spreadsheet is not publicly available.",
                            throwable,
                        )

                    else ->
                        CatalogSyncException(
                            CatalogAccessError.CATALOG_NOT_AVAILABLE,
                            "Failed to read catalog spreadsheet.",
                            throwable,
                        )
                }
            }

            if (
                throwable is UnknownHostException ||
                throwable is SocketTimeoutException ||
                throwable is ConnectException
            ) {
                return CatalogSyncException(
                    CatalogAccessError.NO_INTERNET_FIRST_VISIT,
                    "No internet connection available for first catalog sync.",
                    throwable,
                )
            }

            return CatalogSyncException(
                CatalogAccessError.CATALOG_NOT_AVAILABLE,
                "Catalog is currently unavailable.",
                throwable,
            )
        }

        private fun isOnline(): Boolean {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        companion object {
            private const val INFO_RANGE = "Info!A:B"
            private const val PRODUCTS_RANGE = "Products!A2:E"
            private const val DEFAULT_COUNTRY_CODE = "+54"
            private val PRICE_CLEAN_REGEX = Regex("[^0-9,.-]")
        }
    }
