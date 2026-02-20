package com.brios.miempresa.config.domain

import com.brios.miempresa.core.data.local.entities.Company
import javax.inject.Inject

data class SaveCompanyConfigRequest(
    val companyId: String,
    val originalCompany: Company,
    val companyName: String,
    val whatsappCountryCode: String,
    val whatsappNumber: String,
    val specialization: String,
    val address: String,
    val businessHours: String,
    val localLogoUri: String?,
)

class SaveCompanyConfigUseCase
    @Inject
    constructor(
        private val configRepository: ConfigRepository,
    ) {
        suspend operator fun invoke(request: SaveCompanyConfigRequest): Company {
            val updatedCompany =
                request.originalCompany.copy(
                    name = request.companyName.trim(),
                    whatsappCountryCode = request.whatsappCountryCode,
                    whatsappNumber = request.whatsappNumber.trim(),
                    specialization = request.specialization.trim().takeIf { it.isNotEmpty() },
                    address = request.address.trim().takeIf { it.isNotEmpty() },
                    businessHours = request.businessHours.trim().takeIf { it.isNotEmpty() },
                )

            val finalCompany =
                if (request.localLogoUri != null) {
                    val uploadedLogoId =
                        configRepository.uploadCompanyLogo(
                            request.companyId,
                            request.localLogoUri,
                            request.companyName,
                        )
                    if (uploadedLogoId != null) {
                        updatedCompany.copy(logoUrl = uploadedLogoId)
                    } else {
                        updatedCompany
                    }
                } else {
                    updatedCompany
                }

            configRepository.updateCompanyInfo(finalCompany)
            try {
                configRepository.syncCompanyInfoToSheets(request.companyId)
            } catch (_: Exception) {
                // Non-blocking sync by design.
            }

            return finalCompany
        }
    }

