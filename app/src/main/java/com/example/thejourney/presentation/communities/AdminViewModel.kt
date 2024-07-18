package com.example.thejourney.presentation.communities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow<CommunityState>(CommunityState.Loading)
    val state: StateFlow<CommunityState> = _state

    init {
        fetchPendingRequests()
    }

    private fun fetchPendingRequests() {
        try {
            viewModelScope.launch {
                _state.value = CommunityState.Loading
                db.collection("communities")
                    .whereEqualTo("status", "Pending")
                    .addSnapshotListener { snapshots, e ->
                        if (e != null) {
                            Log.w("ApproveCommunity", "Listen failed.", e)
                            _state.value = CommunityState.Error("Failed to fetch pending requests: ${e.message}")
                            return@addSnapshotListener
                        }

                        val requests = snapshots?.documents?.mapNotNull { it.toObject(Community::class.java) }
                        _state.value = CommunityState.Success(requests ?: emptyList())
                    }
            }
        } catch (e: Exception) {
            Log.w("ApproveCommunity", "Error fetching pending requests", e)
            _state.value = CommunityState.Error("Error fetching pending requests: ${e.message}")
            TODO("Not yet implemented")
        }
    }

    fun approveCommunity(request: Community) {
        viewModelScope.launch {
            _state.value = CommunityState.Loading
            val updatedRequest = request.copy(status = "Approved")

            try {
                db.collection("communities").document(request.name).set(updatedRequest).await()
                _state.value = CommunityState.Success(listOf(updatedRequest))
            } catch (e: Exception) {
                Log.w("ApproveCommunity", "Error updating document", e)
                _state.value = CommunityState.Error("Error updating document: ${e.message}")
            }
        }
    }

    fun rejectCommunity(request: Community) {
        viewModelScope.launch {
            _state.value = CommunityState.Loading
            val updatedRequest = request.copy(status = "Rejected")

            try {
                db.collection("communities").document(request.name).set(updatedRequest).await()
                _state.value = CommunityState.Success(listOf(updatedRequest))
            } catch (e: Exception) {
                Log.w("RejectCommunity", "Error updating document", e)
                _state.value = CommunityState.Error("Error updating document: ${e.message}")
            }
        }
    }
}

sealed class CommunityState {
    data object Loading : CommunityState()
    data class Success(val communities: List<Community>) : CommunityState()
    data class Error(val message: String) : CommunityState()
}
