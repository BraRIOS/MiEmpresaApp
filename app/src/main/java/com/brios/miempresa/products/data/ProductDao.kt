package com.brios.miempresa.products.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    @Upsert
    suspend fun upsert(product: ProductEntity)

    @Query("SELECT * FROM products WHERE id IN (:productIds) AND companyId = :companyId")
    suspend fun getByIds(
        productIds: List<String>,
        companyId: String,
    ): List<ProductEntity>

    @Query("SELECT * FROM products WHERE companyId = :companyId")
    suspend fun getAllByCompany(companyId: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE companyId = :companyId AND deleted = 0")
    fun getAllByCompanyFlow(companyId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id AND companyId = :companyId")
    suspend fun getById(
        id: String,
        companyId: String,
    ): ProductEntity?

    @Query("SELECT * FROM products WHERE dirty = 1 AND companyId = :companyId")
    suspend fun getDirty(companyId: String): List<ProductEntity>

    suspend fun markSynced(
        ids: List<String>,
        timestamp: Long,
        companyId: String,
    ) {
        if (ids.isEmpty()) return
        markSyncedInternal(ids, timestamp, companyId)
    }

    @Query(
        "UPDATE products SET dirty = 0, lastSyncedAt = :timestamp " +
            "WHERE id IN (:ids) AND companyId = :companyId",
    )
    suspend fun markSyncedInternal(
        ids: List<String>,
        timestamp: Long,
        companyId: String,
    )

    @Query(
        "SELECT COUNT(*) FROM products " +
            "WHERE categoryId = :categoryId AND companyId = :companyId AND deleted = 0",
    )
    suspend fun countByCategory(
        categoryId: String,
        companyId: String,
    ): Int

    @Query("DELETE FROM products WHERE companyId = :companyId")
    suspend fun deleteAll(companyId: String)

    @Query("DELETE FROM products WHERE id = :id AND companyId = :companyId")
    suspend fun deleteById(
        id: String,
        companyId: String,
    )
}
