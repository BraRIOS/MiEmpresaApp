package com.brios.miempresa.onboarding.domain

import com.brios.miempresa.core.data.local.entities.Company
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    val stepProgress: Flow<WorkspaceStep>

    suspend fun createWorkspace(request: WorkspaceSetupRequest): WorkspaceCreationResult

    suspend fun validateExistingWorkspace(): WorkspaceValidationResult

    suspend fun syncCompaniesFromDrive(): List<Company>

    suspend fun getOwnedCompanies(): List<Company>

    suspend fun selectCompany(company: Company)

    suspend fun deleteLocalCompany(company: Company)

    suspend fun saveCompanyToRoom(company: Company)

    suspend fun getOwnedCompanyCount(): Int
}
