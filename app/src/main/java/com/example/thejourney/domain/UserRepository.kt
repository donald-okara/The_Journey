package com.example.thejourney.domain

import android.util.Log
import com.example.thejourney.presentation.sign_in.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUsername(): String? {
        return auth.currentUser?.displayName
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
            Log.w("UserRepository", "Error updating user data", e)
        }
    }
}
