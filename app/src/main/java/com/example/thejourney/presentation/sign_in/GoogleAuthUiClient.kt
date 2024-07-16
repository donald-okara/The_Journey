package com.example.thejourney.presentation.sign_in

import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.example.thejourney.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseUser

import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
    private val context : android.content.Context,
    private val oneTapClient : SignInClient
) {
    private val auth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun signIn() : IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        }catch (e : Exception){
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun getSignInWithIntent(intent : Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken , null)

        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            addToFirestore(user)

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
                        biographyBackgroundImageUrl = null
                    )
                },
                errorMessage = null
            )
        }
        catch (e : Exception){
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    private suspend fun addToFirestore(user: FirebaseUser?) {
        user?.let {
            val userData = UserData(
                userId = it.uid,
                username = it.displayName ?: "Default Username",
                alias = null,
                profilePictureUrl = it.photoUrl?.toString(),
                headerImageUrl = null, // Set default or user-provided value
                dateOfBirth = null, // Set default or user-provided value
                biography = null, // Set default or user-provided value
                biographyBackgroundImageUrl = null // Set default or user-provided value
            )
            try {
                firestore.collection("users").document(it.uid).set(userData).await()
            } catch (e: Exception) {
                Log.e("GoogleAuthUiClient", "Error adding user to Firestore: ${e.message}", e)
            }
        }
    }

    suspend fun signOut(){
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        }catch (e: Exception){
            e.printStackTrace()
            if(e is CancellationException) throw e

        }
    }

    fun getSignedInUser() : UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            alias = null,
            profilePictureUrl = photoUrl.toString(),
            headerImageUrl = null,
            dateOfBirth = null,
            biography = null,
            biographyBackgroundImageUrl = null
        )
    }

    private fun buildSignInRequest () : BeginSignInRequest {
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