package com.brios.miempresa.config.domain

import com.brios.miempresa.core.data.local.entities.Company
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

class SaveCompanyConfigUseCaseTest {
    @Test
    fun `invoke uploads logo updates company and syncs`() =
        runTest {
            val repository =
                mock<ConfigRepository> {
                    onBlocking { uploadCompanyLogo(any(), any(), any()) } doReturn "new-logo-id"
                }
            val useCase = SaveCompanyConfigUseCase(repository)
            val original =
                Company(
                    id = "company-1",
                    name = "Old Name",
                    whatsappNumber = "1111111111",
                    whatsappCountryCode = "+54",
                    logoUrl = "old-logo-id",
                )

            val result =
                useCase(
                    SaveCompanyConfigRequest(
                        companyId = "company-1",
                        originalCompany = original,
                        companyName = "  New Name  ",
                        whatsappCountryCode = "+54",
                        whatsappNumber = " 1122334455 ",
                        specialization = "  Panadería  ",
                        address = "  Calle 123 ",
                        businessHours = " 8 a 18 ",
                        localLogoUri = "C:\\tmp\\logo.jpg",
                    ),
                )

            assertEquals("New Name", result.name)
            assertEquals("1122334455", result.whatsappNumber)
            assertEquals("Panadería", result.specialization)
            assertEquals("Calle 123", result.address)
            assertEquals("8 a 18", result.businessHours)
            assertEquals("new-logo-id", result.logoUrl)

            val companyCaptor = argumentCaptor<Company>()
            verifyBlocking(repository) { updateCompanyInfo(companyCaptor.capture()) }
            verifyBlocking(repository) { syncCompanyInfoToSheets("company-1") }
            assertEquals("new-logo-id", companyCaptor.firstValue.logoUrl)
        }

    @Test
    fun `invoke swallows sync errors and still returns updated company`() =
        runTest {
            val repository =
                mock<ConfigRepository> {
                    onBlocking { uploadCompanyLogo(any(), any(), any()) } doReturn null
                }
            whenever(repository.syncCompanyInfoToSheets("company-1")).thenThrow(RuntimeException("sync failed"))
            val useCase = SaveCompanyConfigUseCase(repository)
            val original =
                Company(
                    id = "company-1",
                    name = "Old Name",
                    whatsappNumber = "1111111111",
                    whatsappCountryCode = "+54",
                    logoUrl = "old-logo-id",
                )

            val result =
                useCase(
                    SaveCompanyConfigRequest(
                        companyId = "company-1",
                        originalCompany = original,
                        companyName = "New Name",
                        whatsappCountryCode = "+54",
                        whatsappNumber = "1122334455",
                        specialization = "",
                        address = "",
                        businessHours = "",
                        localLogoUri = null,
                    ),
                )

            assertEquals("New Name", result.name)
            assertNull(result.specialization)
            assertNull(result.address)
            assertNull(result.businessHours)
            assertEquals("old-logo-id", result.logoUrl)
            verifyBlocking(repository) { updateCompanyInfo(any()) }
            verifyBlocking(repository, times(1)) { syncCompanyInfoToSheets("company-1") }
        }
}

