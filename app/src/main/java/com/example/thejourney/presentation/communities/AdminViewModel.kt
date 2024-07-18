package com.example.thejourney.presentation.communities

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _communities = MutableStateFlow<List<Community>>(emptyList())
    val communities: StateFlow<List<Community>> = _communities

    init {
        fetchPendingRequests()
    }

    private fun fetchPendingRequests() {
        db.collection("communities")
            .whereEqualTo("status", "Pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ApproveCommunity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val requests = snapshots?.documents?.mapNotNull { it.toObject(Community::class.java) }
                _communities.value = requests ?: emptyList()
            }
    }

    fun approveCommunity(request: Community) {
        val updatedRequest = request.copy(status = "Approved")

        db.collection("communities").document(request.name)
            .set(updatedRequest)
            .addOnSuccessListener {
                db.collection("communities").add(request.copy(status = "Approved"))
            }
            .addOnFailureListener { e ->
                Log.w("ApproveCommunity", "Error updating document", e)
            }
    }

    fun rejectCommunity(request: Community) {
        val updatedRequest = request.copy(status = "Rejected")

        db.collection("communities").document(request.name)
            .set(updatedRequest)
            .addOnFailureListener { e ->
                Log.w("RejectCommunity", "Error updating document", e)
            }
    }
}