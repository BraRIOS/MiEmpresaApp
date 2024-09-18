package com.brios.miempresa.sign_in

data class SignInState(
    val isSignedIn: Boolean = false,
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)