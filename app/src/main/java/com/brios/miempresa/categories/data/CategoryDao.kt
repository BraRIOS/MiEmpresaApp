package com.brios.miempresa.categories.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE companyId = :companyId AND deleted = 0")
    suspend fun getAll(companyId: String): List<Category>

    @Query("SELECT * FROM categories WHERE companyId = :companyId AND deleted = 0")
    fun getAllFlow(companyId: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id AND companyId = :companyId")
    suspend fun getById(
        id: String,
        companyId: String,
    ): Category?

    @Upsert
    suspend fun upsertAll(categories: List<Category>)

    @Upsert
    suspend fun upsert(category: Category)

    @Query("SELECT * FROM categories WHERE dirty = 1 AND companyId = :companyId")
    suspend fun getDirty(companyId: String): List<Category>

    suspend fun markSynced(
        ids: List<String>,
        timestamp: Long,
        companyId: String,
    ) {
        if (ids.isEmpty()) return
        markSyncedInternal(ids, timestamp, companyId)
    }

    @Query("UPDATE categories SET dirty = 0, lastSyncedAt = :timestamp WHERE id IN (:ids) AND companyId = :companyId")
    suspend fun markSyncedInternal(
        ids: List<String>,
        timestamp: Long,
        companyId: String,
    )

    @Query("DELETE FROM categories WHERE companyId = :companyId")
    suspend fun deleteAll(companyId: String)

    @Query("DELETE FROM categories WHERE id = :id AND companyId = :companyId")
    suspend fun deleteById(
        id: String,
        companyId: String,
    )
}
