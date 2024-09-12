package com.brios.miempresa.ui.sign_in

data class SignInState(
    val isSignedIn: Boolean = false,
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)