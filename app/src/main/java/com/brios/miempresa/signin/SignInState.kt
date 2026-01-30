package com.brios.miempresa.signin

data class SignInState(
    val isSignedIn: Boolean = false,
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
)
