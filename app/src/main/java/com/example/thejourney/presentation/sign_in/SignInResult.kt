package com.example.thejourney.presentation.sign_in

import com.google.firebase.Timestamp

data class SignInResult(
    val data : UserData?,
    val errorMessage : String?
)

data class UserData(
    val userId: String,
    val username: String?,
    val alias : String?,
    val admin : Boolean = false,
    val campus : String? = null,
    val profilePictureUrl: String?,
    val headerImageUrl: String?,
    val dateOfBirth: Timestamp?, // You can use a specific date format
    val biography: String?,
    val biographyBackgroundImageUrl: String?
)
