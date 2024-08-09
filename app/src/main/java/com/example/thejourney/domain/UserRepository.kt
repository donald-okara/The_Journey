package com.example.thejourney.domain

import android.util.Log
import com.example.thejourney.data.model.Community
import com.example.thejourney.data.model.Space
import com.example.thejourney.data.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    coroutineScope: CoroutineScope // Add CoroutineScope for coroutine management

) {
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> get() = _isAdmin

    init {
        // Initialize the admin status when the repository is created
        coroutineScope.launch{
            fetchAdminStatus()
        }

    }

    /**
     * CREATE
     */
    //Create user functionality is in sign_in package

    /**
     * READ
     */

    /**
     * Fetch admin status
     */
    private suspend fun fetchAdminStatus() {
        val userId = getCurrentUserId()
        if (userId != null) {
            withContext(Dispatchers.IO) {
                try {
                    val userDoc = db.collection("users").document(userId).get().await()
                    val userData = userDoc.toObject(UserData::class.java)
                    _isAdmin.value = userData?.admin ?: false
                } catch (e: Exception) {
                    Log.e("UserRepository", "Error fetching admin status", e)
                    _isAdmin.value = false
                }
            }
        } else {
            _isAdmin.value = false
        }
    }

    suspend fun refreshAdminStatus() {
        fetchAdminStatus()
    }

    suspend fun fetchUsersByCommunity(communityId: String): List<UserData> {
        return try {
            // Fetch all users
            val usersSnapshot = db.collection("users")
                .whereNotEqualTo("userId", getCurrentUserId())
                .get()
                .await()
            val users = usersSnapshot.toObjects(UserData::class.java)

            // Filter users who belong to the specified community
            users.filter { user ->
                user.communities.any { community -> community["communityId"] == communityId }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching users by community: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetch users
     */
    suspend fun fetchUsers(): List<UserData> {
        return try {
            val snapshot = db.collection("users")
                .whereNotEqualTo("userId", getCurrentUserId())
                .get()
                .await()
            snapshot.toObjects(UserData::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching users", e)
            emptyList()
        }
    }

    suspend fun fetchUsersByIds(userIds: List<String>): List<UserData> {
        return try {
            val users = db.collection("users")
                .whereIn("userId", userIds)
                .get()
                .await()
            users.toObjects(UserData::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching users by IDs", e)
            emptyList()
        }
    }

    /**
     * Get current user
     */
    fun getCurrentUser(): UserData? {
        Log.d("UserRepository", "Fetching current user data")
        val user = auth.currentUser
        return user?.let {
            UserData(
                userId = it.uid,
                username = it.displayName ?: "Default Username",
                alias = null,
                profilePictureUrl = it.photoUrl?.toString(),
                headerImageUrl = null,
                dateOfBirth = null,
                biography = null,
                biographyBackgroundImageUrl = null,
                communities = emptyList(),
                spaces = emptyList(),
                spacesApproval = emptyList()
            )
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * UPDATE
     */

    /**
     * Update user communities and space
     */
    suspend fun updateUserCommunities(userId: String, communityId: String, role: String) {
        try {
            val userDocRef = db.collection("users").document(userId)
            val document = userDocRef.get().await()
            if (document.exists()) {
                val userData = document.toObject(UserData::class.java)
                userData?.let {
                    val updatedCommunities = it.communities.toMutableList()
                    updatedCommunities.add(mapOf(communityId to role))
                    it.communities = updatedCommunities

                    userDocRef.set(it).await()
                    Log.d("UserRepository", "User data updated successfully for userId: $userId")
                } ?: Log.w("UserRepository", "No user data found for userId: $userId")
            } else {
                Log.w("UserRepository", "No such user data found for userId: $userId")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user data", e)
        }
    }

    suspend fun updateUserSpaces(userId: String, spaceId: String, role: String) {
        try {
            // Fetch the current space document
            val spaceDoc = db.collection("spaces").document(spaceId).get().await()
            val space = spaceDoc.toObject(Space::class.java)

            space?.let {
                if (it.membersRequireApproval) {
                    // Check if the user is approved
                    val isApproved = it.membersApprovalStatus.any { member ->
                        member.keys.first() == userId && member.values.first() == "approved"
                    }

                    if (!isApproved) {
                        Log.d("UserRepository", "User $userId is not yet approved for space $spaceId")
                        return
                    }
                }

                // Fetch the current user document
                val userDocRef = db.collection("users").document(userId)
                val document = userDocRef.get().await()

                if (document.exists()) {
                    val userData = document.toObject(UserData::class.java)
                    userData?.let {
                        val updatedSpaces = it.spaces.toMutableList()

                        // Check if the user is already a member of the space
                        if (updatedSpaces.none { space -> space.keys.first() == spaceId }) {
                            updatedSpaces.add(mapOf(spaceId to role))
                            it.spaces = updatedSpaces

                            userDocRef.set(it).await()
                            Log.d("UserRepository", "User data updated successfully for userId: $userId")
                        } else {
                            Log.d("UserRepository", "User $userId is already a member of space $spaceId")
                        }
                    } ?: Log.w("UserRepository", "No user data found for userId: $userId")
                } else {
                    Log.w("UserRepository", "No such user data found for userId: $userId")
                }
            } ?: run {
                Log.e("UserRepository", "Space $spaceId not found")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user data", e)
        }
    }

    /**
     * Update user role in community
     */
    suspend fun updateUserRoleInCommunity(
        userId: String,
        communityId: String,
        newRole: String) {
        try {
            val userDocRef = db.collection("users").document(userId)
            val document = userDocRef.get().await()

            if (document.exists()) {
                val userData = document.toObject(UserData::class.java)
                userData?.let {
                    val updatedCommunities = it.communities.toMutableList()

                    // Check if the user already has a role in the community
                    val communityIndex = updatedCommunities.indexOfFirst { community -> community.keys.first() == communityId }

                    if (communityIndex != -1) {
                        // Update the existing role
                        updatedCommunities[communityIndex] = mapOf(communityId to newRole)
                    } else {
                        // Add a new role for the community
                        updatedCommunities.add(mapOf(communityId to newRole))
                    }

                    it.communities = updatedCommunities

                    // Save the updated user data
                    userDocRef.set(it).await()
                    Log.d("UserRepository", "User role updated successfully for userId: $userId in community: $communityId")
                } ?: Log.w("UserRepository", "No user data found for userId: $userId")
            } else {
                Log.w("UserRepository", "No such user data found for userId: $userId")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user role in community", e)
        }
    }

    /**
     * DELETE
     */

}
