package com.example.thejourney.domain

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import com.example.thejourney.presentation.communities.model.Community
import com.example.thejourney.presentation.sign_in.UserData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CommunityRepository(
    val db: FirebaseFirestore,
    private val userRepository: UserRepository,
    coroutineScope: CoroutineScope
) {
    val pendingCount = mutableIntStateOf(0)

    init {
        // Initialize the community states when the repository is created
        coroutineScope.launch{
            updateCommunityCounts()
            getPendingCommunities()
            getRejectedCommunities()
            getLiveCommunities()
        }
    }
    private suspend fun updateCommunityCounts() {
        // Fetch the pending communities and update the count
        val pendingCommunities = getPendingCommunities()
        pendingCount.intValue = pendingCommunities.size
    }

    suspend fun requestNewCommunity(
        communityName: String,
        communityType: String,
        bannerUri: String?,
        profileUri: String?,
        selectedLeaders: List<UserData>,
        selectedEditors: List<UserData>
    ) {
        val currentUser = userRepository.getCurrentUser()
        currentUser?.let { user ->
            Log.d("CommunityRepository", "User ID: ${user.userId}")
            Log.d("CommunityRepository", "Community Request: $communityName")

            val members = mutableListOf<Map<String, String>>(
                mapOf(user.userId to "leader")
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
        } ?: run {
            Log.e("CommunityRepository", "User not authenticated or username is null")
        }
    }

    suspend fun getPendingCommunities(): List<Community> {
        return try {
            val snapshot = db.collection("communities")
                .whereEqualTo("status", "Pending")
                .get()
                .await()
            snapshot.toObjects(Community::class.java)
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error fetching pending communities", e)
            emptyList()
        }
    }

    suspend fun getLiveCommunities(): List<Community> {
        return try {
            val snapshot = db.collection("communities")
                .whereEqualTo("status", "Live")
                .get()
                .await()
            snapshot.toObjects(Community::class.java)
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error fetching live communities", e)
            emptyList()
        }
    }

    suspend fun getRejectedCommunities(): List<Community> {
        return try {
            val snapshot = db.collection("communities")
                .whereEqualTo("status", "Rejected")
                .get()
                .await()
            snapshot.toObjects(Community::class.java)
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error fetching rejected communities", e)
            emptyList()
        }
    }
}
