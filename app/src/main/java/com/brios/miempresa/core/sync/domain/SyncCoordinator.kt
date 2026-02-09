package com.brios.miempresa.core.sync.domain

import android.util.Log
import com.brios.miempresa.categories.domain.CategoriesRepository
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.products.domain.ProductsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class SyncCoordinator
    @Inject
    constructor(
        private val productsRepository: ProductsRepository,
        private val categoriesRepository: CategoriesRepository,
        private val companyDao: CompanyDao,
    ) {
        suspend fun syncAll(): Result<Unit> {
            val companyId = getActiveCompanyId() ?: return Result.success(Unit)
            return try {
                try {
                    categoriesRepository.downloadFromSheets(companyId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to download categories", e)
                }
                try {
                    productsRepository.downloadFromSheets(companyId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to download products", e)
                }

                try {
                    categoriesRepository.syncPendingChanges(companyId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to upload categories", e)
                }
                try {
                    productsRepository.syncPendingChanges(companyId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to upload products", e)
                }

                companyDao.updateLastSyncedAt(companyId, System.currentTimeMillis())
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun syncProducts(): Result<Unit> {
            val companyId = getActiveCompanyId() ?: return Result.success(Unit)
            return try {
                try {
                    productsRepository.downloadFromSheets(companyId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to download products", e)
                }
                productsRepository.syncPendingChanges(companyId)
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun syncCategories(): Result<Unit> {
            val companyId = getActiveCompanyId() ?: return Result.success(Unit)
            return try {
                try {
                    categoriesRepository.downloadFromSheets(companyId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to download categories", e)
                }
                categoriesRepository.syncPendingChanges(companyId)
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        private suspend fun getActiveCompanyId(): String? {
            return companyDao.getSelectedOwnedCompany()?.id
        }

        companion object {
            private const val TAG = "SyncCoordinator"
        }
    }
