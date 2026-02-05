package com.brios.miempresa.auth.domain

import android.app.Activity
import com.brios.miempresa.core.auth.SignInResult

interface AuthRepository {
    suspend fun signIn(activity: Activity): SignInResult

    suspend fun authorizeDriveAndSheets(): AuthState

    suspend fun signOut(activity: Activity)

    fun getSignedInUser(): SignInResult?
}

sealed class AuthState {
    data object Authorized : AuthState()

    data object Unauthorized : AuthState()

    data class PendingAuth(val intentSender: android.content.IntentSender?) : AuthState()

    data object Failed : AuthState()
}
