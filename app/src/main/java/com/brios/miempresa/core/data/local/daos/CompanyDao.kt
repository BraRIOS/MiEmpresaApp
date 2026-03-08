package com.brios.miempresa.core.data.local.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.Update
import com.brios.miempresa.core.data.local.entities.Company
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM companies WHERE selected = 1 LIMIT 1")
    fun observeSelectedCompany(): Flow<Company?>

    @Query("SELECT * FROM companies WHERE selected = 1 AND isOwned = 1 LIMIT 1")
    suspend fun getSelectedOwnedCompany(): Company?

    @Query("SELECT * FROM companies WHERE isOwned = 1 ORDER BY selected DESC, name")
    suspend fun getOwnedCompaniesList(): List<Company>

    @Query("SELECT * FROM companies")
    fun getCompanies(): LiveData<List<Company>>

    @Query("DELETE FROM companies")
    suspend fun clear()

    @Query("UPDATE companies SET lastSyncedAt = :timestamp WHERE id = :companyId")
    suspend fun updateLastSyncedAt(
        companyId: String,
        timestamp: Long,
    )

    @Query("SELECT * FROM companies WHERE isOwned = 1 ORDER BY selected DESC, name")
    fun getOwnedCompanies(): Flow<List<Company>>

    @Query("SELECT * FROM companies WHERE id = :id")
    fun observeCompanyById(id: String): Flow<Company?>

    @Query("SELECT COUNT(*) FROM companies WHERE isOwned = 1")
    suspend fun getOwnedCompanyCount(): Int

    @Upsert
    suspend fun insertCompany(company: Company)

    // Client flow queries
    @Query("SELECT * FROM companies WHERE publicSheetId = :sheetId LIMIT 1")
    suspend fun getByPublicSheetId(sheetId: String): Company?

    @Query("SELECT * FROM companies WHERE publicSheetId = :sheetId AND isOwned = 0 LIMIT 1")
    suspend fun getVisitedByPublicSheetId(sheetId: String): Company?

    @Query("SELECT * FROM companies WHERE isOwned = 0 ORDER BY lastVisited DESC")
    fun getVisitedCompanies(): Flow<List<Company>>

    @Query("SELECT * FROM companies WHERE isOwned = 0 ORDER BY lastVisited DESC")
    suspend fun getVisitedCompaniesList(): List<Company>

    @Query("SELECT COUNT(*) FROM companies WHERE isOwned = 0")
    suspend fun countVisited(): Int

    @Query("UPDATE companies SET lastVisited = :timestamp WHERE id = :id")
    suspend fun updateLastVisited(
        id: String,
        timestamp: Long,
    )

    @Upsert
    suspend fun upsertCompanies(companies: List<Company>)
}
