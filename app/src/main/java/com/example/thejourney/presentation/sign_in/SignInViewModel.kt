package com.example.thejourney.presentation.sign_in

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInViewModel : ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onSignInResult(result: SignInResult){
        _state.update {
            it.copy(
                isSignInSuccessful = result.data != null,
                signInError = result.errorMessage
            )
        }
    }

    fun setLoading(isLoading: Boolean) {
        _state.update {
            it.copy(isLoading = isLoading)
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                _state.update { it.copy(isLoading = false, isSignInSuccessful = user != null) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, signInError = e.message) }
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // Check if the email is already in use
                val signInMethods = firebaseAuth.fetchSignInMethodsForEmail(email).await()
                if (signInMethods.signInMethods?.isNotEmpty() == true) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            signUpError = "Email is already in use."
                        )
                    }
                    return@launch
                }

                // Proceed with sign-up
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    addToFirestore(user)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            signUpError = null,
                            isSignInSuccessful = true
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            signUpError = "Sign up failed",
                            isSignInSuccessful = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, signUpError = e.message) }
            }
        }
    }

    private suspend fun addToFirestore(user: FirebaseUser) {
        val userData = UserData(
            userId = user.uid,
            username = user.displayName ?: "Default Username",
            alias = null,
            profilePictureUrl = user.photoUrl?.toString(),
            headerImageUrl = null, // Set default or user-provided value
            dateOfBirth = null, // Set default or user-provided value
            biography = null, // Set default or user-provided value
            biographyBackgroundImageUrl = null // Set default or user-provided value,

        )
        try {
            firestore.collection("users").document(user.uid).set(userData).await()
        } catch (e: Exception) {
            Log.e("SignInViewModel", "Error adding user to Firestore: ${e.message}", e)
            _state.update { it.copy(isLoading = false, signUpError = "Failed to save user data.") }
        }
    }

    fun getSignedInUser(): UserData? {
        val user = firebaseAuth.currentUser
        return user?.let {
            UserData(
                userId = it.uid,
                username = it.displayName ?: "Default Username",
                alias = null,
                profilePictureUrl = it.photoUrl?.toString(),
                headerImageUrl = null,
                dateOfBirth = null,
                biography = null,
                biographyBackgroundImageUrl = null
            )
        }
    }

    fun resetState(){
        _state.update { SignInState() }
    }

}
