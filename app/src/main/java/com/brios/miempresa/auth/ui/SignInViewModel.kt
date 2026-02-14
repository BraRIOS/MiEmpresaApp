package com.brios.miempresa.auth.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brios.miempresa.R
import com.brios.miempresa.auth.domain.AuthRepository
import com.brios.miempresa.auth.domain.AuthState
import com.brios.miempresa.core.data.local.daos.CompanyDao
import com.brios.miempresa.core.domain.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
        private val authRepository: AuthRepository,
        private val companyDao: CompanyDao,
        private val logoutUseCase: LogoutUseCase,
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
                // User cancelled — no error message returned, do nothing
                if (!signInSucceed && signInResult.errorMessage == null) return@launch
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

        fun resetStates() {
            _postAuthDestination.value = null
            _authState.value = null
            _signInState.value = SignInState()
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
                try {
                    val authState = authRepository.authorizeDriveAndSheets()
                    when (authState) {
                        is AuthState.PendingAuth -> {
                            authState.intentSender?.let { intentSender ->
                                _authState.update { AuthState.PendingAuth(intentSender) }
                            } ?: run {
                                _authState.update { AuthState.Unauthorized }
                            }
                        }
                        is AuthState.Authorized -> {
                            _authState.update { AuthState.Authorized }
                        }
                        else -> {
                            _authState.update { AuthState.Unauthorized }
                        }
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (_: Exception) {
                    _authState.update { AuthState.Unauthorized }
                }
            }

        fun updateAuthState(state: AuthState) {
            _authState.update { state }
        }

        fun getCompanies() = companyDao.getCompanies()

        fun getSelectedCompany() = companyDao.getSelectedCompany()
    }
