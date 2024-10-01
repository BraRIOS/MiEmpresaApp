package com.brios.miempresa.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

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

    @Query("SELECT * FROM companies WHERE selected = 1 LIMIT 1")
    fun getSelectedCompany(): LiveData<Company?>


    @Query("SELECT * FROM companies")
    fun getCompanies(): LiveData<List<Company>>

    @Query("DELETE FROM companies")
    suspend fun clear()

}