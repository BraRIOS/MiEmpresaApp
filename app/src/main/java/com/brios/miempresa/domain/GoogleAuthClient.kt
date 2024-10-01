package com.brios.miempresa.domain

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.brios.miempresa.R
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scopes = listOf(Scope(DriveScopes.DRIVE), Scope(SheetsScopes.SPREADSHEETS))
    private val authorizationRequest = AuthorizationRequest.builder().setRequestedScopes(scopes).build()
    private val authorizationClient = Identity.getAuthorizationClient(context)
    private val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(
        context,
        listOf(DriveScopes.DRIVE, SheetsScopes.SPREADSHEETS)
    )
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
                errorMessage = context.getString(R.string.invalid_credential_type, credential.type)
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

    suspend fun authorizeDriveAndSheets(): AuthorizationResult {
        return authorizationClient.authorize(authorizationRequest).await()
    }


    suspend fun getGoogleDriveService(): Drive? = withContext(Dispatchers.IO){
        if (auth.currentUser != null) {
            credential.selectedAccountName = auth.currentUser?.email
            return@withContext Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName(
                context.resources.getResourceName(R.string.app_name)
            ).build()
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(context, context.getString(R.string.not_authorized), Toast.LENGTH_SHORT).show()
        }
        return@withContext null
    }

    suspend fun getGoogleSheetsService(): Sheets? = withContext(Dispatchers.IO) {
        if (auth.currentUser != null) {
            credential.selectedAccountName = auth.currentUser?.email
            return@withContext Sheets.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName(
                context.resources.getString(R.string.app_name)
            ).build()
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(context, context.getString(R.string.not_authorized), Toast.LENGTH_SHORT).show()
        }
        return@withContext null
    }


    private fun generateNonce(length: Int = 32): String {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}