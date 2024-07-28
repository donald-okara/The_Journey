package com.example.thejourney.domain

import android.util.Log
import com.example.thejourney.presentation.communities.model.Community
import com.example.thejourney.presentation.sign_in.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CommunityRepository(
    private val db: FirebaseFirestore,
    private val userRepository: UserRepository
) {
    suspend fun requestNewCommunity(
        communityName: String,
        communityType: String,
        bannerUri: String?,
        profileUri: String?,
        selectedLeaders: List<UserData>,
        selectedEditors: List<UserData>
    ) {
        val userId = userRepository.getCurrentUserId()
        val username = userRepository.getCurrentUsername()

        if (userId != null && username != null) {
            Log.d("CommunityRepository", "User ID: $userId")
            Log.d("CommunityRepository", "Community Request: $communityName")

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

            try {
                db.collection("communities").document(communityName)
                    .set(community)
                    .await()

                members.forEach { member ->
                    val (userId, role) = member.entries.first()
                    userRepository.updateUserCommunities(userId, communityName, role)
                }
            } catch (e: Exception) {
                Log.w("CommunityRepository", "Error adding document", e)
            }
        } else {
            Log.e("CommunityRepository", "User not authenticated or username is null")
        }
    }
}
