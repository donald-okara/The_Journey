package com.example.thejourney.presentation.communities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thejourney.presentation.communities.model.Community
import com.example.thejourney.presentation.sign_in.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CommunityViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // StateFlow to hold users
    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").get().await()
                val userList = snapshot.toObjects(UserData::class.java)
                _users.value = userList
            } catch (e: Exception) {
                // Handle the exception as needed
                Log.e("RequestCommunityViewModel", "Error fetching users", e)
            }
        }
    }

    fun requestNewCommunity(
        communityName: String,
        communityType: String,
        bannerUri: String?,
        profileUri: String?,
        selectedLeaders: List<UserData>,
        selectedEditors: List<UserData>
    ) {
        val user = auth.currentUser
        val userId = user?.uid
        val username = user?.displayName // Assuming displayName is the username

        if (userId != null && username != null) {
            Log.d("CommunityViewModel", "User ID: $userId")
            Log.d("CommunityViewModel", "Community Request: $communityName")


            // Construct the members list
            val members = mutableListOf<Map<String, String>>(
                mapOf(userId to "leader")
            ).apply {
                addAll(selectedLeaders.map { mapOf(it.userId to "leader") })
                addAll(selectedEditors.map { mapOf(it.userId to "editor") })
            }

            val community = Community(
                name = communityName,
                type = communityType,
                members = members,
                communityBannerUrl = bannerUri,
                profileUrl = profileUri
            )

            viewModelScope.launch {
                db.collection("communities").document(communityName)
                    .set(community)
                    .addOnSuccessListener {
                        Log.d(
                            "CommunityViewModel",
                            "DocumentSnapshot added with ID: $communityName"
                        )
                        updateUserCommunities(communityName,members)

                    }
                    .addOnFailureListener { e ->
                        Log.w("CommunityViewModel", "Error adding document", e)
                    }
            }
        } else {
            Log.e("CommunityViewModel", "User not authenticated or username is null")
        }
    }


    private fun updateUserCommunities(communityId: String, members: List<Map<String, String>>) {
        viewModelScope.launch {
            try {
                // Iterate through all members and update their community lists
                for (member in members) {
                    val (userId, role) = member.entries.first() // Extract userId and role
                    val userDocRef = db.collection("users").document(userId)

                    val document = userDocRef.get().await()
                    if (document.exists()) {
                        val userData = document.toObject(UserData::class.java)
                        userData?.let {
                            val updatedCommunities = it.communities.toMutableList()
                            updatedCommunities.add(mapOf(communityId to role)) // Add the community ID with role
                            it.communities = updatedCommunities

                            userDocRef.set(it).await() // Use await for coroutine suspension
                            Log.d(
                                "CommunityViewModel",
                                "User data updated successfully for userId: $userId"
                            )
                        } ?: Log.w("CommunityViewModel", "No user data found for userId: $userId")
                    } else {
                        Log.w("CommunityViewModel", "No such user data found for userId: $userId")
                    }
                }
            } catch (e: Exception) {
                Log.w("CommunityViewModel", "Error updating user data", e)
            }
        }
    }
}