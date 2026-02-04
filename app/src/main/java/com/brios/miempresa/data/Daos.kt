package com.brios.miempresa.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface CompanyDao {
    @Insert
    suspend fun insert(friend: Company)

    @Update
    suspend fun update(friend: Company)

    @Delete
    suspend fun delete(friend: Company)

    @Query("SELECT * FROM companies WHERE id = :id")
    suspend fun getCompanyById(id: String): Company?

    @Query("UPDATE companies SET selected = 0")
    suspend fun unselectAllCompanies()

    @Query("SELECT * FROM companies WHERE selected = 1 LIMIT 1")
    fun getSelectedCompany(): LiveData<Company?>

    @Query("SELECT * FROM companies")
    fun getCompanies(): LiveData<List<Company>>

    @Query("DELETE FROM companies")
    suspend fun clear()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE companyId = :companyId")
    suspend fun getAll(companyId: String): List<Category>

    @Upsert
    suspend fun upsertAll(categories: List<Category>)

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
}
