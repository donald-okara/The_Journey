package com.example.thejourney.presentation.communities.model

import com.google.firebase.firestore.PropertyName


data class Community(
    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("type")
    @set:PropertyName("type")
    var type: String = "",

    @get:PropertyName("requestedBy")
    @set:PropertyName("requestedBy")
    var requestedBy: String = "",

    @get:PropertyName("status")
    @set:PropertyName("status")
    var status: String = "Pending"
) {
    // Default no-argument constructor required by Firestore
    constructor() : this("", "", "", "")
}