package com.example.thejourney.presentation.communities

data class Community(
    val name: String,
    val type: String,
    val status: String = "Pending",
    val requestedBy: String
)