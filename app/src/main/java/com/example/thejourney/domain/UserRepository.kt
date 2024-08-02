package com.example.thejourney.domain

import android.util.Log
import com.example.thejourney.presentation.sign_in.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
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

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Function to check if the current user is an admin
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

    suspend fun fetchUsers(): List<UserData> {
        return try {
            val snapshot = db.collection("users").get().await()
            snapshot.toObjects(UserData::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching users", e)
            emptyList()
        }
    }

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
}
