package com.example.thejourney.domain

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import com.example.thejourney.data.model.Community
import com.example.thejourney.data.model.UserData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CommunityRepository(
    private val context: Context,
    val db: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val storage: FirebaseStorage,
    coroutineScope: CoroutineScope
) {
    private val pendingCount = mutableIntStateOf(0)
    val firestore = FirebaseFirestore.getInstance()

    /**
     * CREATE
     */

    /**
     * Request new community
     */
    suspend fun requestNewCommunity(
        communityName: String,
        communityType: String,
        bannerUri: Uri?,
        profileUri: Uri?,
        aboutUs: String?,
        selectedLeaders: List<UserData>,
        selectedEditors: List<UserData>
    ) {
        val currentUser = userRepository.getCurrentUser()
        currentUser?.let { user ->
            val members = mutableListOf<Map<String, String>>(
                mapOf(user.userId to "leader")
            ).apply {
                addAll(selectedLeaders.map { mapOf(it.userId to "leader") })
                addAll(selectedEditors.map { mapOf(it.userId to "editor") })
            }

            // Upload images and get URLs
            val bannerUrl = bannerUri?.let { uploadImageToStorage(it, "communities/banners/${communityName}.jpg") }
            val profileUrl = profileUri?.let { uploadImageToStorage(it, "communities/profileImages/${communityName}.jpg") }

            val communityId = db.collection("communities").document().id // Generate a new ID

            val community = Community(
                id = communityId, // Populate the id field
                name = communityName,
                type = communityType,
                members = members,
                communityBannerUrl = bannerUrl,
                profileUrl = profileUrl,
                aboutUs = aboutUs
            )

            try {
                db.collection("communities").document(communityId)
                    .set(community)
                    .await()

                members.forEach { member ->
                    val (userId, role) = member.entries.first()
                    userRepository.updateUserCommunities(userId, communityId, role)
                }
            } catch (e: Exception) {
                Log.w("CommunityRepository", "Error adding document", e)
            }
        } ?: run {
            Log.e("CommunityRepository", "User not authenticated or username is null")
        }
    }

    /**
     * READ
     */

    /**
     *Observe community details
     */

    fun observeCommunityMembers(communityId: String): Query {
        return firestore.collection("communities")
            .document(communityId)
            .collection("members")
    }

    fun observePendingRequests(): Flow<List<Community>> = callbackFlow {
        val listenerRegistration = firestore.collection("communities")
            .whereEqualTo("status", "Pending")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }

                val communities = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Community::class.java)
                } ?: emptyList()

                trySend(communities).isSuccess
            }

        awaitClose { listenerRegistration.remove() }
    }

    fun observeLiveRequests(): Flow<List<Community>> = callbackFlow {
        val listenerRegistration = firestore.collection("communities")
            .whereEqualTo("status", "Live")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }

                val communities = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Community::class.java)
                } ?: emptyList()

                trySend(communities).isSuccess
            }

        awaitClose { listenerRegistration.remove() }
    }

    fun observeRejectedRequests(): Flow<List<Community>> = callbackFlow {
        val listenerRegistration = firestore.collection("communities")
            .whereEqualTo("status", "Rejected")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }

                val communities = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Community::class.java)
                } ?: emptyList()

                trySend(communities).isSuccess
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * UPDATE
     */

    /**
     * Add members
     */

    suspend fun addMembers(
        userId: String,
        communityId: String,
        role: String = "member"
    ){
        try {
            // Fetch the current community document
            val communityDoc = db.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)

            community?.let {
                // Update the members list
                val updatedMembers = it.members.toMutableList().apply {
                    // Add the user with the specified role if not already a member
                    if (none { member -> member.keys.first() == userId }) {
                        add(mapOf(userId to role))
                    }
                }

                // Update the community document with the modified members list
                db.collection("communities").document(communityId)
                    .update("members", updatedMembers)
                    .await()

                Log.d("CommunityRepository", "Added member $userId to community $communityId with role $role")
            } ?: run {
                Log.e("CommunityRepository", "Community $communityId not found")
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error adding member $userId to community $communityId", e)
        }
    }

    /**
     * Upload image
     */

    suspend fun uploadImageToStorage(uri: Uri, path: String): String? {
        return try {
            Log.d("CommunityRepository", "Uploading image with URI: $uri")

            val file = uriToFile(uri)
            val storageRef = storage.reference.child(path)

            file?.let {
                val inputStream = file.inputStream()
                val uploadTask = storageRef.putStream(inputStream).await()

                // Verify upload success
                val downloadUrl = storageRef.downloadUrl.await().toString()
                Log.d("CommunityRepository", "Uploaded image to $path: $downloadUrl")
                downloadUrl
            } ?: run {
                Log.e("CommunityRepository", "Failed to convert URI to File: $uri")
                null
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Failed to upload image: ${e.message}", e)
            null
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.let {
                val file = File(context.cacheDir, "tempImage")
                FileOutputStream(file).use { outputStream ->
                    it.copyTo(outputStream)
                }
                file
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Failed to convert URI to File", e)
            null
        }
    }

    /**
     * Demote user to member
     */

    suspend fun demoteMember(
        userId: String,
        communityId: String
    ) {
        /**
         * Can be used by community leader to demote user to member or by member to remove themselves as leader or editor
         */
        try {
            // Fetch the current community document
            val communityDoc = db.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)

            community?.let {
                // Update the members list
                val updatedMembers = it.members.map { member ->
                    val (id, role) = member.entries.first()
                    if (id == userId && (role == "leader" || role == "editor")) {
                        mapOf(id to "member") // Demote to member
                    } else {
                        member // Keep the existing role for others
                    }
                }

                // Update the community document with the modified members list
                db.collection("communities").document(communityId)
                    .update("members", updatedMembers)
                    .await()

                userRepository.updateUserRoleInCommunity(
                    userId =userId,
                    communityId =communityId,
                    newRole = "member"
                )

                Log.d("CommunityRepository", "Demoted member $userId in community $communityId to member")
            } ?: run {
                Log.e("CommunityRepository", "Community $communityId not found")
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error demoting member $userId in community $communityId", e)
        }
    }

    /**
     * Add leaders or editors
     */

    suspend fun addLeadersOrEditors(
        communityId: String,
        newLeaders: List<UserData>? = null,
        newEditors: List<UserData>? = null
    ) {
        try {
            // Fetch the current community document
            val communityDoc = db.collection("communities").document(communityId).get().await()
            val community = communityDoc.toObject(Community::class.java)

            community?.let {
                // Update the members list
                val updatedMembers = it.members.toMutableList().apply {
                    newLeaders?.forEach { leader ->
                        if (none { member -> member.keys.first() == leader.userId }) {
                            add(mapOf(leader.userId to "leader"))
                        }
                    }
                    newEditors?.forEach { editor ->
                        if (none { member -> member.keys.first() == editor.userId }) {
                            add(mapOf(editor.userId to "editor"))
                        }
                    }
                }

                // Update the community document with the modified members list
                db.collection("communities").document(communityId)
                    .update("members", updatedMembers)
                    .await()

                // Update users' community roles
                newLeaders?.forEach { leader ->
                    userRepository.updateUserRoleInCommunity(leader.userId, communityId, "leader")
                }
                newEditors?.forEach { editor ->
                    userRepository.updateUserRoleInCommunity(editor.userId, communityId, "editor")
                }

                Log.d("CommunityRepository", "Added new leaders/editors to community $communityId")
            } ?: run {
                Log.e("CommunityRepository", "Community $communityId not found")
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error adding leaders/editors to community $communityId", e)
        }
    }


    /**
     * Update community fields
     */

    suspend fun updateCommunityFields(
        communityId: String,
        updatedFields: Map<String, Any?>
    ) {
        try {
            // Update the community document with the modified fields
            db.collection("communities").document(communityId)
                .update(updatedFields)
                .await()

            Log.d("CommunityRepository", "Updated community $communityId with new fields: $updatedFields")
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error updating community $communityId", e)
        }
    }


    /**
     * DELETE
     */

    /**
     * LEGACY
     */

    /**
     *Legacy update community counts
     */

    init {
        // Initialize community states when the repository is created
//        coroutineScope.launch {
//            updateCommunityCounts()
//        }
    }

    /**
     *Legacy fetch community details functions
     */

//    suspend fun getCommunityMembers(communityId: String): List<UserData> {
//        return try {
//            val communityDoc = db.collection("communities").document(communityId).get().await()
//            val community = communityDoc.toObject(Community::class.java)
//            community?.let {
//                val userIds = it.members.map { member -> member.keys.first() }
//                userRepository.fetchUsersByIds(userIds)
//            } ?: emptyList()
//        } catch (e: Exception) {
//            Log.e("CommunityRepository", "Error fetching community members for $communityId", e)
//            emptyList()
//        }
//    }
//
//    private suspend fun updateCommunityCounts() {
//        // Fetch the pending communities and update the count
//        val pendingCommunities = getPendingCommunities()
//        pendingCount.intValue = pendingCommunities.size
//    }
//
//    suspend fun getPendingCommunities(): List<Community> {
//        return try {
//            val snapshot = db.collection("communities")
//                .whereEqualTo("status", "Pending")
//                .get()
//                .await()
//            snapshot.toObjects(Community::class.java)
//        } catch (e: Exception) {
//            Log.e("CommunityRepository", "Error fetching pending communities", e)
//            emptyList()
//        }
//    }
//
//    suspend fun getLiveCommunities(): List<Community> {
//        return try {
//            val snapshot = db.collection("communities")
//                .whereEqualTo("status", "Live")
//                .get()
//                .await()
//            snapshot.toObjects(Community::class.java)
//        } catch (e: Exception) {
//            Log.e("CommunityRepository", "Error fetching live communities", e)
//            emptyList()
//        }
//    }
//
//    suspend fun getRejectedCommunities(): List<Community> {
//        return try {
//            val snapshot = db.collection("communities")
//                .whereEqualTo("status", "Rejected")
//                .get()
//                .await()
//            snapshot.toObjects(Community::class.java)
//        } catch (e: Exception) {
//            Log.e("CommunityRepository", "Error fetching rejected communities", e)
//            emptyList()
//        }
//    }
}
