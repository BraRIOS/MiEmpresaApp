package com.brios.miempresa.domain

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val username: String?,
    val email: String? = null,
    val profilePictureUrl: String?
)