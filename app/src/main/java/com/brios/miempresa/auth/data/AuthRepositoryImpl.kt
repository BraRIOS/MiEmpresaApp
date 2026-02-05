package com.brios.miempresa.auth.data

import android.app.Activity
import com.brios.miempresa.auth.domain.AuthRepository
import com.brios.miempresa.auth.domain.AuthState
import com.brios.miempresa.core.auth.GoogleAuthClient
import com.brios.miempresa.core.auth.SignInResult
import com.google.android.gms.auth.api.identity.AuthorizationResult
import javax.inject.Inject

class AuthRepositoryImpl
    @Inject
    constructor(
        private val googleAuthClient: GoogleAuthClient,
    ) : AuthRepository {
        override suspend fun signIn(activity: Activity): SignInResult = googleAuthClient.signIn(activity)

        override suspend fun authorizeDriveAndSheets(): AuthState {
            val authorizationResult: AuthorizationResult = googleAuthClient.authorizeDriveAndSheets()
            return if (authorizationResult.hasResolution()) {
                AuthState.PendingAuth(authorizationResult.pendingIntent?.intentSender)
            } else {
                AuthState.Authorized
            }
        }

        override suspend fun signOut(activity: Activity) {
            googleAuthClient.signOut(activity)
        }

        override fun getSignedInUser() =
            googleAuthClient.getSignedInUser()?.let {
                SignInResult(data = it, errorMessage = null)
            }
    }
