package com.example.thejourney.presentation.sign_in

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class SignInViewModel : ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    init {
        fetchAdminStatus()
    }

    private fun fetchAdminStatus() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            _isAdmin.value = document.getBoolean("admin") == true
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("UserViewModel", "Error fetching admin status", exception)
                        // Set default value for _isAdmin
                    }
            }
        }
    }

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

                user?.let {
                    try {
                        updateFirestoreUser(it)
                    } catch (e: Exception) {
                        Log.e("SignInWithEmail", "Error adding/updating user in Firestore: ${e.message}", e)
                    }
                }

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

    private suspend fun addToFirestore(user: FirebaseUser?) {
        user?.let {
            try {
                updateFirestoreUser(it)
            } catch (e: Exception) {
                Log.e("GoogleAuthUiClient", "Error adding/updating user in Firestore: ${e.message}", e)
            }
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
            biographyBackgroundImageUrl = null
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


    fun resetState(){
        _state.update { SignInState() }
    }

}
