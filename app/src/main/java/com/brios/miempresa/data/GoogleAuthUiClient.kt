package com.brios.miempresa.data

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.brios.miempresa.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthUiClient(
) {
    private val auth = Firebase.auth

    suspend fun signIn(activity: Activity): SignInResult {
        val nonce = generateNonce()
        val credentialManager: CredentialManager = CredentialManager.create(activity)
//      val googleIdOption = GetGoogleIdOption.Builder()
//        .setFilterByAuthorizedAccounts(true)
//        .setServerClientId(WEB_CLIENT_ID)
//        .setAutoSelectEnabled(true)
//        .setNonce(nonce)
//        .build()
        val googleSignInOption = GetSignInWithGoogleOption.Builder(activity.getString(R.string.web_client_id))
            .setNonce(nonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleSignInOption)
            .build()

        return try {
            val result = credentialManager.getCredential(activity, request)
            handleSignInResult(result)
        } catch (e: Exception) {
            e.printStackTrace()
            println("\u001B${e.message}\u001B")
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse): SignInResult {
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                // Authenticate with Firebase using the ID token
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val user = auth.signInWithCredential(firebaseCredential).await().user
                SignInResult(
                    data = user?.run {
                        UserData(
                            userId = uid,
                            username = displayName,
                            profilePictureUrl = photoUrl?.toString()
                        )
                    },
                    errorMessage = null
                )
            } catch (e: GoogleIdTokenParsingException) {
                e.printStackTrace()
                SignInResult(
                    data = null,
                    errorMessage = e.message
                )
            }
        }else return SignInResult(
                data = null,
                errorMessage = "Invalid credential type: ${credential.type}"
            )
    }

    suspend fun signOut(activity: Activity) {
        val credentialManager: CredentialManager = CredentialManager.create(activity)
        try {
            auth.signOut()
            credentialManager.clearCredentialState(
                ClearCredentialStateRequest())
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            email = email,
            profilePictureUrl = photoUrl?.toString()
        )
    }

    private fun generateNonce(length: Int = 32): String {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}