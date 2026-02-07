package com.brios.miempresa.onboarding.domain

import com.brios.miempresa.core.data.local.entities.Company
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    val stepProgress: Flow<WorkspaceStep>

    suspend fun createWorkspace(request: WorkspaceSetupRequest): WorkspaceCreationResult

    suspend fun saveCompanyToRoom(company: Company)

    suspend fun getOwnedCompanyCount(): Int
}
