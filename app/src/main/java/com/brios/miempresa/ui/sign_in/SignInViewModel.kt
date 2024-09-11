package com.brios.miempresa.ui.sign_in

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val googleAuthUiClient: GoogleAuthUiClient
) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun signIn() = viewModelScope.launch {
        val signInResult : SignInResult = googleAuthUiClient.signIn()
        _state.update {
            it.copy(
                isSignInSuccessful = signInResult.data != null,
                signInError = if (signInResult.data != null) null else "Sign in failed"
            )
        }
    }

    fun getSignedInUser() = googleAuthUiClient.getSignedInUser()

    fun signOut() = viewModelScope.launch {
        googleAuthUiClient.signOut()
        _state.update { SignInState() }
    }

    fun resetState() {
        _state.update { SignInState() }
    }
}