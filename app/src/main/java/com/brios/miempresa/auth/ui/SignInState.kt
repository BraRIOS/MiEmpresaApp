package com.brios.miempresa.auth.ui

data class SignInState(
    val isSignedIn: Boolean = false,
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
)
