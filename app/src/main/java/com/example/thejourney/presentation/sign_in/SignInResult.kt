package com.example.thejourney.presentation.sign_in

import com.google.firebase.Timestamp

data class SignInResult(
    val data : UserData?,
    val errorMessage : String?
)

data class UserData(
    val userId: String,
    var username: String?,
    var alias : String?,
    var admin : Boolean = false,
    var campus : String? = null,
    var profilePictureUrl: String?,
    var headerImageUrl: String?,
    var dateOfBirth: Timestamp?, // You can use a specific date format
    var biography: String?,
    var biographyBackgroundImageUrl: String?
)

