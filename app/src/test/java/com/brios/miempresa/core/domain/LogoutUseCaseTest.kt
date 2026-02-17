package com.brios.miempresa.core.domain

import android.app.Activity
import com.brios.miempresa.auth.domain.AuthRepository
import com.brios.miempresa.core.data.local.MiEmpresaDatabase
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.data.local.entities.Company
import com.brios.miempresa.core.sync.SyncManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking

class LogoutUseCaseTest {
    @Test
    fun `invoke keeps visited companies after logout cleanup`() =
        runTest {
            val syncManager = mock<SyncManager>()
            val database = mock<MiEmpresaDatabase>()
            val authRepository = mock<AuthRepository>()
            val companyDao =
                mock<CompanyDao> {
                    onBlocking { getVisitedCompaniesList() } doReturn
                        listOf(
                            Company(id = "visited-1", name = "Store A", selected = true, isOwned = false),
                            Company(id = "visited-2", name = "Store B", selected = false, isOwned = false),
                        )
                }

            val useCase =
                LogoutUseCase(
                    syncManager = syncManager,
                    database = database,
                    companyDao = companyDao,
                    authRepository = authRepository,
                )

            useCase(activity = mock<Activity>())

            verifyBlocking(authRepository) { signOut(any()) }
            verify(syncManager).cancelAll()
            verify(database).clearAllTables()

            val companiesCaptor = argumentCaptor<List<Company>>()
            verifyBlocking(companyDao) { upsertCompanies(companiesCaptor.capture()) }
            assertEquals(listOf("visited-1", "visited-2"), companiesCaptor.firstValue.map { it.id })
            assertFalse(companiesCaptor.firstValue.any { it.selected })
        }

    @Test
    fun `invoke does not restore when no visited companies`() =
        runTest {
            val syncManager = mock<SyncManager>()
            val database = mock<MiEmpresaDatabase>()
            val authRepository = mock<AuthRepository>()
            val companyDao =
                mock<CompanyDao> {
                    onBlocking { getVisitedCompaniesList() } doReturn emptyList()
                }

            val useCase =
                LogoutUseCase(
                    syncManager = syncManager,
                    database = database,
                    companyDao = companyDao,
                    authRepository = authRepository,
                )

            useCase(activity = mock<Activity>())

            verify(database).clearAllTables()
            verifyBlocking(companyDao, times(0)) { upsertCompanies(any()) }
        }
}
