package com.brios.miempresa.core.sync.domain

import com.brios.miempresa.categories.domain.CategoriesRepository
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.products.domain.ProductsRepository
import javax.inject.Inject
import javax.inject.Singleton

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
                syncCategories(companyId)
                syncProducts(companyId)
                companyDao.updateLastSyncedAt(companyId, System.currentTimeMillis())
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun syncProducts(): Result<Unit> {
            val companyId = getActiveCompanyId() ?: return Result.success(Unit)
            return try {
                syncProducts(companyId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun syncCategories(): Result<Unit> {
            val companyId = getActiveCompanyId() ?: return Result.success(Unit)
            return try {
                syncCategories(companyId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        private suspend fun syncCategories(companyId: String) {
            categoriesRepository.syncPendingChanges(companyId)
        }

        private suspend fun syncProducts(companyId: String) {
            productsRepository.syncPendingChanges(companyId)
        }

        private suspend fun getActiveCompanyId(): String? {
            return companyDao.getSelectedOwnedCompany()?.id
        }
    }
