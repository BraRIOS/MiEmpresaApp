package com.brios.miempresa.sign_in

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.R
import com.brios.miempresa.domain.GoogleAuthClient
import com.brios.miempresa.domain.SignInResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {
    private val googleAuthClient = GoogleAuthClient(context)
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun signIn(activity: Activity) = viewModelScope.launch {
        val signInResult : SignInResult = googleAuthClient.signIn(activity)
        val signInSucceed = signInResult.data != null
        if (!signInSucceed) {
            println("\u001B${signInResult.errorMessage}\u001B")
        }
        _state.update {
            it.copy(
                isSignInSuccessful = signInSucceed,
                signInError = if (signInSucceed) null else activity.getString(R.string.sign_in_error)
            )
        }
    }

    fun getSignedInUser() = googleAuthClient.getSignedInUser()

    fun signOut(activity: Activity) = viewModelScope.launch {
        googleAuthClient.signOut(activity)
        _state.update { SignInState() }
    }

    fun resetState() {
        _state.update { SignInState() }
    }
}