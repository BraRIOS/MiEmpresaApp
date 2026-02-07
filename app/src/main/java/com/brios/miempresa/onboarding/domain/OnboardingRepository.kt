package com.brios.miempresa.onboarding.domain

import com.brios.miempresa.core.data.local.entities.Company

interface OnboardingRepository {
    suspend fun createWorkspace(request: WorkspaceSetupRequest): WorkspaceCreationResult

    suspend fun saveCompanyToRoom(company: Company)

    suspend fun getOwnedCompanyCount(): Int
}
