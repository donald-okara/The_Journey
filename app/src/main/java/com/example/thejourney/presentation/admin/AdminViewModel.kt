package com.example.thejourney.presentation.admin

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thejourney.presentation.communities.model.Community
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _pendingState = MutableStateFlow<CommunityState>(CommunityState.Loading)
    val pendingState: StateFlow<CommunityState> = _pendingState

    private val _liveState = MutableStateFlow<CommunityState>(CommunityState.Loading)
    val liveState: StateFlow<CommunityState> = _liveState

    private val _rejectedState = MutableStateFlow<CommunityState>(CommunityState.Loading)
    val rejectedState: StateFlow<CommunityState> = _rejectedState
    val pendingCount = mutableIntStateOf(0)

    init {
        fetchPendingRequests()
        fetchLiveCommunities()
        fetchRejectedCommunities()
    }


    private fun fetchPendingRequests() {
        viewModelScope.launch {
            _pendingState.value = CommunityState.Loading
            db.collection("communities")
                .whereEqualTo("status", "Pending")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        _pendingState.value = CommunityState.Error("Failed to fetch pending requests: ${e.message}")
                        return@addSnapshotListener
                    }
                    val requests = snapshots?.documents?.mapNotNull { it.toObject(Community::class.java) }
                    _pendingState.value = CommunityState.Success(requests ?: emptyList())
                    if (requests != null) {
                        pendingCount.intValue = requests.size
                    } // Update count based on the list size
                }
        }
    }

    private fun fetchLiveCommunities() {
        viewModelScope.launch {
            _liveState.value = CommunityState.Loading
            db.collection("communities")
                .whereEqualTo("status", "Live")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        _liveState.value = CommunityState.Error("Failed to fetch live communities: ${e.message}")
                        return@addSnapshotListener
                    }
                    val communities = snapshots?.documents?.mapNotNull { it.toObject(Community::class.java) }
                    _liveState.value = CommunityState.Success(communities ?: emptyList())
                }
        }
    }

    private fun fetchRejectedCommunities() {
        viewModelScope.launch {
            _rejectedState.value = CommunityState.Loading
            db.collection("communities")
                .whereEqualTo("status", "Rejected")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        _rejectedState.value = CommunityState.Error("Failed to fetch rejected communities: ${e.message}")
                        return@addSnapshotListener
                    }
                    val communities = snapshots?.documents?.mapNotNull { it.toObject(Community::class.java) }
                    _rejectedState.value = CommunityState.Success(communities ?: emptyList())
                }
        }
    }

    fun approveCommunity(request: Community) {
        viewModelScope.launch {
            try {
                Log.d("AdminViewModel", "Attempting to approve community with ID: ${request.name}")

                db.collection("communities").document(request.name)
                    .update("status", "Live")
                    .await()

                // Refresh the live and pending states
                fetchPendingRequests()
                fetchLiveCommunities()
            } catch (e: Exception) {
                Log.w("AdminViewModel", "Error approving community", e)
                _pendingState.value = CommunityState.Error("Error approving community: ${e.message}")
            }
        }
    }

    fun rejectCommunity(request: Community) {
        viewModelScope.launch {
            try {
                Log.d("AdminViewModel", "Attempting to reject community with ID: ${request.name}")

                db.collection("communities").document(request.name)
                    .update("status", "Rejected")
                    .await()

                // Refresh the rejected and pending states
                fetchPendingRequests()
                fetchRejectedCommunities()
            } catch (e: Exception) {
                Log.w("AdminViewModel", "Error rejecting community", e)
                _pendingState.value = CommunityState.Error("Error rejecting community: ${e.message}")
            }
        }
    }
}

sealed class CommunityState {
    data object Loading : CommunityState()
    data class Success(val communities: List<Community>) : CommunityState()
    data class Error(val message: String) : CommunityState()
}
