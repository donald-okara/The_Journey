package com.example.thejourney.presentation.admin

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thejourney.domain.CommunityRepository
import com.example.thejourney.domain.UserRepository
import com.example.thejourney.data.model.Community
import com.example.thejourney.data.model.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AdminViewModel(
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository
) : ViewModel() {
    private val db = communityRepository.db

    private val _pendingState = MutableStateFlow<CommunityState>(CommunityState.Loading)
    val pendingState: StateFlow<CommunityState> = _pendingState

    private val _liveState = MutableStateFlow<CommunityState>(CommunityState.Loading)
    val liveState: StateFlow<CommunityState> = _liveState

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> get() = _isAdmin

    private val _rejectedState = MutableStateFlow<CommunityState>(CommunityState.Loading)
    val rejectedState: StateFlow<CommunityState> = _rejectedState
    val pendingCount = mutableIntStateOf(0)

    init {
        fetchPendingRequests()
        fetchLiveCommunities()
        fetchRejectedCommunities()
        viewModelScope.launch {
            fetchAdminStatus()
        }
    }


    // Function to check if the current user is an admin
    suspend fun fetchAdminStatus() {
        val userId = userRepository.getCurrentUserId()
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

    fun fetchPendingRequests() {
        viewModelScope.launch {
            _pendingState.value = CommunityState.Loading
            try {
                communityRepository.observePendingRequests()
                    .collect { requests ->
                        _pendingState.value = CommunityState.Success(requests)
                        pendingCount.intValue = requests.size // Update count based on the list size
                    }
            } catch (e: Exception) {
                _pendingState.value = CommunityState.Error("Failed to fetch pending requests: ${e.message}")
            }
        }
    }

    fun fetchLiveCommunities() {
        viewModelScope.launch {
            _liveState.value = CommunityState.Loading
            try {
                communityRepository.observeLiveRequests()
                    .collect { requests ->
                        _liveState.value = CommunityState.Success(requests)
                    }
            } catch (e: Exception) {
                _liveState.value = CommunityState.Error("Failed to fetch live communities: ${e.message}")
            }
        }
    }

    fun fetchRejectedCommunities() {
        viewModelScope.launch {
            _rejectedState.value = CommunityState.Loading
            try {
                communityRepository.observeRejectedRequests()
                    .collect { requests ->
                        _rejectedState.value = CommunityState.Success(requests)
                    }
            }catch (e: Exception) {
                _rejectedState.value = CommunityState.Error("Failed to fetch rejected communities: ${e.message}")
            }
        }
    }


    fun approveCommunity(request: Community) {
        viewModelScope.launch {
            try {
                Log.d("AdminViewModel", "Attempting to approve community with ID: ${request.id}")

                db.collection("communities").document(request.id)
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
                Log.d("AdminViewModel", "Attempting to reject community with ID: ${request.id}")

                db.collection("communities").document(request.id)
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
