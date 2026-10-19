package com.example.data.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class GoogleSignInHelper(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private val _userEmail = MutableStateFlow<String?>(auth.currentUser?.email)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _isSignedIn = MutableStateFlow(auth.currentUser != null)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    suspend fun onGoogleSignInClicked(serverClientId: String? = null, onResult: (Boolean, String?) -> Unit) {
        try {
            if (serverClientId.isNullOrBlank()) {
                onResult(false, "Google Web Client ID is missing. Please use Email & Password below.")
                return
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user

                if (user != null) {
                    _userEmail.value = user.email
                    _isSignedIn.value = true
                    onResult(true, "Signed in successfully as ${user.email ?: user.uid}")
                } else {
                    onResult(false, "Firebase authentication failed. Please try again.")
                }
                return
            }
            onResult(false, "Unrecognized credential received. Please use Email & Password.")
        } catch (e: GetCredentialCancellationException) {
            onResult(false, "Sign-in cancelled.")
        } catch (e: NoCredentialException) {
            Log.i("GoogleSignIn", "No Google account configured on device: ${e.message}")
            onResult(false, "No Google account configured on this device. Please sign in using your Email & Password below, or add an account in Android Settings.")
        } catch (e: GetCredentialException) {
            Log.w("GoogleSignIn", "CredentialManager: ${e.message}")
            val msg = if (e.message?.contains("No credentials", ignoreCase = true) == true) {
                "No Google account configured on this device. Please sign in using your Email & Password below, or add an account in Android Settings."
            } else {
                e.localizedMessage ?: "Google Sign-In is unavailable. Please use Email & Password."
            }
            onResult(false, msg)
        } catch (e: Exception) {
            Log.w("GoogleSignIn", "Sign-in error", e)
            onResult(false, e.localizedMessage ?: "Sign-in failed. Please use Email & Password.")
        }
    }

    suspend fun signOut(onComplete: () -> Unit = {}) {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w("GoogleSignIn", "Sign out clear error", e)
        }
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.w("GoogleSignIn", "FirebaseAuth signOut error", e)
        }
        _isSignedIn.value = false
        _userEmail.value = null
        onComplete()
    }
}
