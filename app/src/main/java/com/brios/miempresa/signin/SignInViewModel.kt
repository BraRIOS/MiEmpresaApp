package com.brios.miempresa.signin

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.R
import com.brios.miempresa.data.MiEmpresaDatabase
import com.brios.miempresa.data.PreferencesKeys
import com.brios.miempresa.data.removeValueFromDataStore
import com.brios.miempresa.domain.GoogleAuthClient
import com.brios.miempresa.domain.SignInResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {
    private val googleAuthClient = GoogleAuthClient(context)
    private val _signInState = MutableStateFlow(SignInState())
    val signInStateFlow = _signInState.asStateFlow()
    private val _authState = MutableStateFlow<AuthState?>(null)
    val authStateFlow = _authState.asStateFlow()

    fun signIn(activity: Activity) = viewModelScope.launch {
        val signInResult : SignInResult = googleAuthClient.signIn(activity)
        val signInSucceed = signInResult.data != null
        if (!signInSucceed) {
            println("\u001B${signInResult.errorMessage}\u001B")
        }
        _signInState.update {
            it.copy(
                isSignInSuccessful = signInSucceed,
                signInError = if (signInSucceed) null else activity.getString(R.string.sign_in_error)
            )
        }
    }

    fun getSignedInUser() = googleAuthClient.getSignedInUser()

    fun signOut(activity: Activity) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            val database = MiEmpresaDatabase.getDatabase(activity)
            database.companyDao().clear()
            removeValueFromDataStore(activity, PreferencesKeys.SPREADSHEET_ID_KEY)
            googleAuthClient.signOut(activity)
        }
    }

    fun resetSignInState() {
        _signInState.update { SignInState() }
    }

    fun authorizeDriveAndSheets(activity: Activity) = viewModelScope.launch {
        val authorizationResult = googleAuthClient.authorizeDriveAndSheets()
        if (authorizationResult.hasResolution()) {
            authorizationResult.pendingIntent?.let { intent ->
                _authState.update { AuthState.PendingAuth(intent.intentSender) }
            } ?: run {
                Toast.makeText(activity, activity.getString(R.string.authorization_failed), Toast.LENGTH_SHORT).show()
                _authState.update {
                    AuthState.Unauthorized
                }
            }
        } else {
            Toast.makeText(activity, activity.getString(R.string.authorization_success), Toast.LENGTH_SHORT).show()
            _authState.update { AuthState.Authorized }
        }
    }

    fun updateAuthState(state: AuthState) {
        _authState.update { state }
    }
}