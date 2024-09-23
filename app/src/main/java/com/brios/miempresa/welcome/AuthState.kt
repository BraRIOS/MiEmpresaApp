package com.brios.miempresa.welcome

import android.content.IntentSender

sealed class AuthState {
    data object Authorized : AuthState()
    data object Unauthorized : AuthState()
    data class PendingAuth(val intentSender: IntentSender) : AuthState()
}
