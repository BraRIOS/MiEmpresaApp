package com.brios.miempresa.config.data

import com.brios.miempresa.config.domain.ConfigRepository
import com.brios.miempresa.core.api.drive.DriveApi
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ConfigRepositoryImpl
    @Inject
    constructor(
        private val companyDao: CompanyDao,
        private val driveApi: DriveApi,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ConfigRepository {
        override fun observeCompany(companyId: String): Flow<Company?> =
            companyDao.observeCompanyById(companyId)

        override suspend fun updateCompanyInfo(company: Company) {
            companyDao.update(company)
        }

        override suspend fun syncCompanyInfoToSheets(companyId: String) =
            withContext(ioDispatcher) {
                val company = companyDao.getCompanyById(companyId) ?: return@withContext
                val fullWhatsapp = "${company.whatsappCountryCode}${company.whatsappNumber}"

                // Write to private sheet Info tab
                company.privateSheetId?.let { privateSheetId ->
                    val privateInfoData = listOf(
                        listOf("company_id", company.id),
                        listOf("name", company.name),
                        listOf("specialization", company.specialization ?: ""),
                        listOf("whatsapp_number", fullWhatsapp),
                        listOf("logo_url", company.logoUrl ?: ""),
                        listOf("address", company.address ?: ""),
                        listOf("business_hours", company.businessHours ?: ""),
                    )
                    driveApi.writeInfoTab(privateSheetId, privateInfoData)
                }

                // Write to public sheet Info tab
                company.publicSheetId?.let { publicSheetId ->
                    val publicInfoData = listOf(
                        listOf("name", company.name),
                        listOf("specialization", company.specialization ?: ""),
                        listOf("whatsapp_number", fullWhatsapp),
                        listOf("logo_url", company.logoUrl ?: ""),
                        listOf("address", company.address ?: ""),
                        listOf("business_hours", company.businessHours ?: ""),
                    )
                    driveApi.writeInfoTab(publicSheetId, publicInfoData)
                }

                companyDao.updateLastSyncedAt(companyId, System.currentTimeMillis())
            }

        override suspend fun uploadCompanyLogo(
            companyId: String,
            localImagePath: String,
            companyName: String,
        ): String? =
            withContext(ioDispatcher) {
                try {
                    val file = File(localImagePath)
                    if (!file.exists()) return@withContext null

                    val company = companyDao.getCompanyById(companyId) ?: return@withContext null
                    val driveFolderId = company.driveFolderId ?: return@withContext null

                    val fileName = "logo_${System.currentTimeMillis()}.jpg"
                    val fileId = driveApi.uploadFile(
                        file = file,
                        mimeType = "image/jpeg",
                        parentFolderId = driveFolderId,
                        fileName = fileName,
                    )

                    if (fileId != null) {
                        val isPublic = driveApi.makeFilePublic(fileId)
                        if (isPublic) {
                            file.delete()
                            fileId
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
    }
