package com.brios.miempresa.config.domain

import com.brios.miempresa.core.data.local.entities.Company
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    fun observeCompany(companyId: String): Flow<Company?>

    suspend fun updateCompanyInfo(company: Company)

    suspend fun syncCompanyInfoToSheets(companyId: String)

    suspend fun uploadCompanyLogo(
        companyId: String,
        localImagePath: String,
        companyName: String,
    ): String?
}
