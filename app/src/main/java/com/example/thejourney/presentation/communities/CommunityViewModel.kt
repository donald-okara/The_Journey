package com.example.thejourney.presentation.communities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thejourney.presentation.communities.model.Community
import com.example.thejourney.presentation.sign_in.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun requestNewCommunity(communityName: String, communityType: String, bannerUri: String?, profileUri: String?) {
        val user = auth.currentUser
        val userId = user?.uid
        val username = user?.displayName // Assuming displayName is the username

        if (userId != null && username != null) {
            Log.d("CommunityViewModel", "User ID: $userId")
            Log.d("CommunityViewModel", "Community Request: $communityName")

            val community = Community(
                name = communityName,
                type = communityType,
                members = listOf(mapOf(userId to "leader")),
                communityBannerUrl = bannerUri,
                profileUrl = profileUri
            )

            viewModelScope.launch {
                db.collection("communities").document(communityName)
                    .set(community)

                    .addOnSuccessListener {
                        Log.d("CommunityViewModel", "DocumentSnapshot added with ID: ${communityName}")
                        updateUserCommunity(userId, communityName, "leader")

                    }
                    .addOnFailureListener { e ->
                        Log.w("CommunityViewModel", "Error adding document", e)
                    }
            }
        } else {
            Log.e("CommunityViewModel", "User not authenticated or username is null")
        }
    }

    private fun updateUserCommunity(userId: String, communityId: String, role: String) {
        // Get the user's document reference
        val userDocRef = db.collection("users").document(userId)

        // Fetch the current user data
        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Parse the user data
                val userData = document.toObject(UserData::class.java)

                // Update the user's communities list
                userData?.let {
                    val updatedCommunities = it.communities.toMutableList()
                    updatedCommunities.add(mapOf(communityId to role))
                    it.communities = updatedCommunities

                    // Save the updated user data back to Firestore
                    userDocRef.set(it)
                        .addOnSuccessListener {
                            Log.d("CommunityViewModel", "User data updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.w("CommunityViewModel", "Error updating user data", e)
                        }
                }
            } else {
                Log.w("CommunityViewModel", "No such user data found")
            }
        }.addOnFailureListener { e ->
            Log.w("CommunityViewModel", "Error fetching user data", e)
        }
    }
}