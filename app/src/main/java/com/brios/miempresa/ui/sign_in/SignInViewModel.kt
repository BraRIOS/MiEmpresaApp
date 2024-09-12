package com.brios.miempresa.ui.sign_in

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.R
import com.brios.miempresa.data.GoogleAuthUiClient
import com.brios.miempresa.data.SignInResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
) : ViewModel() {
    private val googleAuthUiClient = GoogleAuthUiClient()
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun signIn(activity: Activity) = viewModelScope.launch {
        val signInResult : SignInResult = googleAuthUiClient.signIn(activity)
        val signInSucceded = signInResult.data != null
        if (!signInSucceded) {
            println("\u001B[31m${signInResult.errorMessage}\u001B[0m")
        }
        _state.update {
            it.copy(
                isSignInSuccessful = signInSucceded,
                signInError = if (signInSucceded) null else activity.getString(R.string.sign_in_error)
            )
        }
    }

    fun getSignedInUser() = googleAuthUiClient.getSignedInUser()

    fun signOut(activity: Activity) = viewModelScope.launch {
        googleAuthUiClient.signOut(activity)
        _state.update { SignInState() }
    }

    fun resetState() {
        _state.update { SignInState() }
    }
}