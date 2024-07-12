package com.example.thejourney.presentation.sign_in

data class SignInState(
    val isSignInSuccessful : Boolean = false,
    val signInError : String? = null,
    val signUpError: String? = null,
    val isLoading: Boolean = false
)