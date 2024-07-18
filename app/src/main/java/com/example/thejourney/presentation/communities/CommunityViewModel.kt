package com.example.thejourney.presentation.communities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun requestNewCommunity(communityName: String, communityType: String) {
        val user = auth.currentUser
        val userId = user?.uid
        val username = user?.displayName // Assuming displayName is the username

        if (userId != null && username != null) {
            Log.d("CommunityViewModel", "User ID: $userId")
            Log.d("CommunityViewModel", "Community Request: $communityName")

            val community = Community(
                name = communityName,
                type = communityType,
                requestedBy = username
            )

            viewModelScope.launch {
                db.collection("communities").add(community)
                    .addOnSuccessListener {
                        Log.d("CommunityViewModel", "DocumentSnapshot added with ID: ${it.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("CommunityViewModel", "Error adding document", e)
                    }
            }
        } else {
            Log.e("CommunityViewModel", "User not authenticated or username is null")
        }
    }
}