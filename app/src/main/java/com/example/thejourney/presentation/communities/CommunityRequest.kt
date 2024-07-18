package com.example.thejourney.presentation.communities

data class CommunityRequest(
    val name: String,
    val type: String,
    val requestedBy: String,
    val status: String
)