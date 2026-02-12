package com.brios.miempresa.auth.ui

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.R
import com.brios.miempresa.auth.domain.AuthRepository
import com.brios.miempresa.auth.domain.AuthState
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.domain.LogoutUseCase
import com.brios.miempresa.core.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PostAuthDestination {
    data object Onboarding : PostAuthDestination()

    data object Home : PostAuthDestination()

    data object CompanySelector : PostAuthDestination()
}

@HiltViewModel
class SignInViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val authRepository: AuthRepository,
        private val companyDao: CompanyDao,
        private val logoutUseCase: LogoutUseCase,
        private val syncManager: SyncManager,
    ) : ViewModel() {
        private val _signInState = MutableStateFlow(SignInState())
        val signInStateFlow = _signInState.asStateFlow()
        private val _authState = MutableStateFlow<AuthState?>(null)
        val authStateFlow = _authState.asStateFlow()
        private val _postAuthDestination = MutableStateFlow<PostAuthDestination?>(null)
        val postAuthDestination = _postAuthDestination.asStateFlow()

        fun signIn(activity: Activity) =
            viewModelScope.launch {
                val signInResult = authRepository.signIn(activity)
                val signInSucceed = signInResult.data != null
                if (!signInSucceed) {
                    println("\u001B${signInResult.errorMessage}\u001B")
                }
                _signInState.update {
                    it.copy(
                        isSignInSuccessful = signInSucceed,
                        signInError = if (signInSucceed) null else activity.getString(R.string.sign_in_error),
                    )
                }
            }

        fun getSignedInUser() = authRepository.getSignedInUser()?.data

        fun signOut(activity: Activity) =
            viewModelScope.launch {
                // Reset navigation state FIRST (synchronously) to avoid race conditions
                _postAuthDestination.value = null
                _authState.value = null
                _signInState.value = SignInState()
                // Then clean up data (can be async)
                logoutUseCase(activity)
            }

        fun resetSignInState() {
            _signInState.update { SignInState() }
        }

        fun determinePostAuthDestination() {
            // Always route through Onboarding — it handles Drive discovery,
            // company selection, and workspace validation
            _postAuthDestination.value = PostAuthDestination.Onboarding
        }

        fun consumePostAuthDestination() {
            _postAuthDestination.value = null
        }

        suspend fun checkDriveAuthorization(): AuthState {
            return try {
                authRepository.authorizeDriveAndSheets()
            } catch (e: Exception) {
                AuthState.Unauthorized
            }
        }

        fun authorizeDriveAndSheets(activity: Activity) =
            viewModelScope.launch {
                val authState = authRepository.authorizeDriveAndSheets()
                when (authState) {
                    is AuthState.PendingAuth -> {
                        authState.intentSender?.let { intentSender ->
                            _authState.update { AuthState.PendingAuth(intentSender) }
                        } ?: run {
                            Toast.makeText(activity, activity.getString(R.string.authorization_failed), Toast.LENGTH_SHORT).show()
                            _authState.update { AuthState.Unauthorized }
                        }
                    }
                    is AuthState.Authorized -> {
                        Toast.makeText(
                            activity,
                            activity.getString(R.string.authorization_success),
                            Toast.LENGTH_SHORT,
                        ).show()
                        _authState.update { AuthState.Authorized }
                    }
                    else -> {
                        _authState.update { AuthState.Unauthorized }
                    }
                }
            }

        fun updateAuthState(state: AuthState) {
            _authState.update { state }
        }

        fun getCompanies() = companyDao.getCompanies()

        fun getSelectedCompany() = companyDao.getSelectedCompany()
    }
