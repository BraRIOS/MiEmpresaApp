package com.brios.miempresa.products.data

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
            val dirtyProducts = productDao.getDirty(companyId)
            if (dirtyProducts.isEmpty()) return
            // TODO: Upload to Sheets + Drive in Sprint 2 task A2
            productDao.markSynced(
                ids = dirtyProducts.map { it.id },
                timestamp = System.currentTimeMillis(),
                companyId = companyId,
            )
        }
    }
