package com.brios.miempresa.core.domain

import android.app.Activity
import com.brios.miempresa.auth.domain.AuthRepository
import com.brios.miempresa.core.data.local.MiEmpresaDatabase
import com.brios.miempresa.core.sync.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogoutUseCase
    @Inject
    constructor(
        private val syncManager: SyncManager,
        private val database: MiEmpresaDatabase,
        private val authRepository: AuthRepository,
    ) {
        suspend operator fun invoke(activity: Activity) {
            // Sign out Firebase FIRST (synchronous) so getSignedInUser() returns null immediately.
            // This prevents race conditions where navigation checks auth state before it's cleared.
            authRepository.signOut(activity)
            withContext(Dispatchers.IO) {
                syncManager.cancelAll()
                database.clearAllTables()
            }
        }
    }
