package com.example.thejourney.presentation.communities

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _communityRequests = MutableStateFlow<List<CommunityRequest>>(emptyList())
    val communityRequests: StateFlow<List<CommunityRequest>> = _communityRequests

    init {
        fetchPendingRequests()
    }

    private fun fetchPendingRequests() {
        db.collection("communityRequests")
            .whereEqualTo("status", "Pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ApproveCommunity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val requests = snapshots?.documents?.mapNotNull { it.toObject(CommunityRequest::class.java) }
                _communityRequests.value = requests ?: emptyList()
            }
    }

    fun approveCommunity(request: CommunityRequest) {
        val updatedRequest = request.copy(status = "Approved")

        db.collection("communityRequests").document(request.name)
            .set(updatedRequest)
            .addOnSuccessListener {
                db.collection("communities").add(request.copy(status = "Approved"))
            }
            .addOnFailureListener { e ->
                Log.w("ApproveCommunity", "Error updating document", e)
            }
    }

    fun rejectCommunity(request: CommunityRequest) {
        val updatedRequest = request.copy(status = "Rejected")

        db.collection("communityRequests").document(request.name)
            .set(updatedRequest)
            .addOnFailureListener { e ->
                Log.w("RejectCommunity", "Error updating document", e)
            }
    }
}