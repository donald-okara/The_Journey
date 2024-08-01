package com.example.thejourney.domain

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import com.example.thejourney.presentation.communities.model.Community
import com.example.thejourney.presentation.sign_in.UserData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
    val pendingCount = mutableIntStateOf(0)

    init {
        // Initialize community states when the repository is created
        coroutineScope.launch {
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
            val bannerUrl = bannerUri?.let { uploadImageToStorage(it, "communities/banners/$communityName.jpg") }
            val profileUrl = profileUri?.let { uploadImageToStorage(it, "communities/profileImages/$communityName.jpg") }

            val community = Community(
                name = communityName,
                type = communityType,
                members = members,
                communityBannerUrl = bannerUrl,
                profileUrl = profileUrl,
                aboutUs = aboutUs
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

    private suspend fun uploadImageToStorage(uri: Uri, path: String): String? {
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
