@file:Suppress("DEPRECATION")

package com.example.thejourney.presentation.sign_in

import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.example.thejourney.R
import com.example.thejourney.data.model.SignInResult
import com.example.thejourney.data.model.UserData
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseUser

import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class GoogleAuthUiClient(
    private val context : android.content.Context,
    private val oneTapClient : SignInClient
) {
    private val auth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun getSignInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)

        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user

            user?.let {
                try {
                    updateFirestoreUser(it)
                } catch (e: Exception) {
                    Log.e("GoogleAuthUiClient", "Error adding/updating user in Firestore: ${e.message}", e)
                }
            }

            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        alias = null,
                        profilePictureUrl = photoUrl?.toString(),
                        headerImageUrl = null,
                        dateOfBirth = null,
                        biography = null,
                        biographyBackgroundImageUrl = null,
                        communities = emptyList(),
                        spaces = emptyList(),
                        spacesApproval = emptyList()
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    private suspend fun updateFirestoreUser(user: FirebaseUser) {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("users").document(user.uid)
        val documentSnapshot = docRef.get().await()

        val userData = UserData(
            userId = user.uid,
            username = user.displayName,
            alias = null,
            profilePictureUrl = user.photoUrl?.toString(),
            headerImageUrl = null,
            dateOfBirth = null,
            biography = null,
            biographyBackgroundImageUrl = null,
            communities = emptyList(),
            spaces = emptyList(),
            spacesApproval = emptyList()
        )

        if (!documentSnapshot.exists()) {
            // Document doesn't exist, perform initial write
            docRef.set(userData).await()
        } else {
            // Document exists, perform updates as needed (if any)
            val updates = mutableMapOf<String, Any?>()

            UserData::class.memberProperties.forEach { property ->
                property.isAccessible = true
                val fieldName = property.name
                val fieldValue = property.get(userData)

                if (!documentSnapshot.contains(fieldName)) {
                    updates[fieldName] = fieldValue
                }
            }

            if (updates.isNotEmpty()) {
                docRef.update(updates).await()
            } else {
                Log.d("UpdateFirestore", "No updates necessary for document")
            }
        }
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}