package com.example.thejourney.domain

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import com.example.thejourney.data.model.Community
import com.example.thejourney.data.model.Space
import com.example.thejourney.data.model.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SpaceRepository(
    private val context : Context,
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository,
    coroutineScope: CoroutineScope
) {

    init {
        coroutineScope.launch{
            getLiveSpaces()
            getPendingSpaces()
            getRejectedSpaces()
            updateCommunityCounts()
        }
    }

    private val pendingCount = mutableIntStateOf(0)
    private val db = communityRepository.db

    suspend fun getLiveSpacesByCommunity(communityId: String): List<Space> {
        return try {
            val snapshot = db.collection("spaces")
                .whereEqualTo("parentCommunity", communityId)
                .whereEqualTo("status", "Live")
                .get()
                .await()
            snapshot.toObjects(Space::class.java)
        } catch (e: Exception) {
            Log.e("SpacesRepository", "Error fetching live spaces for community $communityId", e)
            emptyList()
        }
    }

    suspend fun requestNewSpace(
        parentCommunityId : String,
        spaceName : String,
        profilePictureUri : Uri?,
        bannerUri : Uri?,
        description : String?,
        membersRequireApproval : Boolean,
        selectedLeaders : List<UserData>,
    ) {
        val currentUser = userRepository.getCurrentUser()
        currentUser?.let { user ->
            val members = mutableListOf<Map<String, String>>(
                mapOf(user.userId to "leader")
            ).apply {
                addAll(selectedLeaders.map { mapOf(it.userId to "leader") })
            }


            val bannerUrl = bannerUri?.let {
                communityRepository.uploadImageToStorage(
                    it,
                    "spaces/banners/${parentCommunityId + spaceName}.jpg"
                )
            }
            val profileUrl = profilePictureUri?.let {
                communityRepository.uploadImageToStorage(
                    it,
                    "spaces/profileImages/${parentCommunityId + spaceName}.jpg"
                )
            }

            val spaceId =
                db.collection("spaces").document().id // Generate a new ID

            val space = Space(
                id = spaceId,
                name = spaceName,
                parentCommunity = parentCommunityId,
                bannerUri = bannerUrl,
                profileUri = profileUrl,
                description = description,
                approvalStatus = "pending",
                membersRequireApproval = membersRequireApproval,
                members = members,
                membersApprovalStatus = emptyList()
            )

            try {
                db.collection("spaces").document(spaceId)
                    .set(space)
                    .await()
                members.forEach{member->
                    val (userId, role) = member.entries.first()
                    userRepository.updateUserSpaces(userId, spaceId, role)
                }
            }catch (e:Exception){
                Log.w("SpacesRepository", "Error adding document", e)

            }
        } ?: run{
            Log.e("SpacesRepository", "User not authenticated or username is null")
        }
    }

    suspend fun addMembers(
        userId: String,
        spaceId: String,
        role: String = "member"
    ) {
        try {
            // Fetch the current space document
            val spaceDoc = db.collection("spaces").document(spaceId).get().await()
            val space = spaceDoc.toObject(Space::class.java)

            space?.let {
                val updatedMembers = it.members.toMutableList()
                val updatedMembersApprovalStatus = it.membersApprovalStatus.toMutableList()

                if (it.membersRequireApproval) {
                    // Check if the user is already in the approval list
                    if (updatedMembersApprovalStatus.none { member -> member.keys.first() == userId }) {
                        // Add user with pending approval status
                        updatedMembersApprovalStatus.add(mapOf(userId to "pending"))
                    }
                } else {
                    // Add the user directly to the members list if not already a member
                    if (updatedMembers.none { member -> member.keys.first() == userId }) {
                        updatedMembers.add(mapOf(userId to role))
                    }
                }

                // Update the space document with the modified lists
                db.collection("spaces").document(spaceId)
                    .update(
                        mapOf(
                            "members" to updatedMembers,
                            "membersApprovalStatus" to updatedMembersApprovalStatus
                        )
                    ).await()

                Log.d("SpacesRepository", "Added member $userId to space $spaceId with role $role")
            } ?: run {
                Log.e("SpacesRepository", "Space $spaceId not found")
            }
        } catch (e: Exception) {
            Log.e("SpacesRepository", "Error adding member $userId to space $spaceId", e)
        }
    }

    private suspend fun updateCommunityCounts() {
        // Fetch the pending communities and update the count
        val pendingSpaces = getPendingSpaces()
        val liveSpaces = getLiveSpaces()
        val rejectedSpaces = getRejectedSpaces()

        pendingCount.intValue = pendingSpaces.size
        pendingCount.intValue = liveSpaces.size
        pendingCount.intValue = rejectedSpaces.size
    }

    private suspend fun getPendingSpaces(): List<Space> {
        return try {
            val snapshot = db.collection("spaces")
                .whereEqualTo("status", "Pending")
                .get()
                .await()
            snapshot.toObjects(Space::class.java)
        } catch (e: Exception) {
            Log.e("SpacesRepository", "Error fetching pending spaces", e)
            emptyList()
        }
    }

    suspend fun getLiveSpaces(): List<Space> {
        return try {
            val snapshot = db.collection("spaces")
                .whereEqualTo("status", "Live")
                .get()
                .await()
            snapshot.toObjects(Space::class.java)
        } catch (e: Exception) {
            Log.e("SpacesRepository", "Error fetching live spaces", e)
            emptyList()
        }
    }

    private suspend fun getRejectedSpaces(): List<Community> {
        return try {
            val snapshot = db.collection("spaces")
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