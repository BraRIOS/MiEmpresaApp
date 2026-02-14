package com.brios.miempresa.catalog.domain

import com.brios.miempresa.core.data.local.entities.Company

interface ClientCatalogRepository {
    suspend fun syncPublicSheet(publicSheetId: String): Result<Company>

    suspend fun refreshCatalog(
        companyId: String,
        publicSheetId: String,
    ): Result<Unit>
}
