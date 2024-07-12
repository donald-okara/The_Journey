package com.example.thejourney.sign_in

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thejourney.sign_in.SignInResult
import com.example.thejourney.sign_in.SignInState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInViewModel : ViewModel() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onSignInResult(result: SignInResult){
        _state.update {it.copy(
            isSignInSuccessful = result.data != null,
            signInError = result.errorMessage
        )}
    }

    fun setLoading(isLoading: Boolean) {
        _state.update {
            it.copy(isLoading = isLoading)
        }
    }

    fun resetState(){
        _state.update { SignInState() }
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
                _state.update {
                    it.copy(
                        isLoading = false,
                        signInError = if (user == null) "Sign up failed" else null,
                        isSignInSuccessful = user != null
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, signUpError = e.message) }
            }
        }
    }
}