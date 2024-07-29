package com.example.thejourney.presentation.communities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thejourney.domain.CommunityRepository
import com.example.thejourney.domain.UserRepository
import com.example.thejourney.presentation.admin.CommunityState
import com.example.thejourney.presentation.sign_in.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunityViewModel(
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _liveState = MutableStateFlow<CommunityState>(CommunityState.Loading)
    val liveState: StateFlow<CommunityState> = _liveState

    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users


    private val _requestStatus = MutableStateFlow<RequestStatus>(RequestStatus.Idle)
    val requestStatus: StateFlow<RequestStatus> = _requestStatus

    init {
        fetchUsers()
        fetchLiveCommunities()
    }

    fun clearRequestStatus() {
        _requestStatus.value = RequestStatus.Idle
    }
    private fun fetchUsers() {
        viewModelScope.launch {
            _users.value = userRepository.fetchUsers()
        }
    }

    private fun fetchLiveCommunities() {
        viewModelScope.launch {
            _liveState.value = CommunityState.Loading
            try {
                val communities = communityRepository.getLiveCommunities()
                _liveState.value = CommunityState.Success(communities)
            } catch (e: Exception) {
                _liveState.value = CommunityState.Error("Failed to fetch live communities: ${e.message}")
            }
        }
    }

    fun requestNewCommunity(
        communityName: String,
        communityType: String,
        bannerUri: String?,
        profileUri: String?,
        selectedLeaders: List<UserData>,
        selectedEditors: List<UserData>
    ) {
        viewModelScope.launch {
            try {
                communityRepository.requestNewCommunity(
                    communityName,
                    communityType,
                    bannerUri,
                    profileUri,
                    selectedLeaders,
                    selectedEditors
                )
                _requestStatus.value = RequestStatus.Success
            } catch (e: Exception) {
                _requestStatus.value = RequestStatus.Error(e.message ?: "Check your internet and try again")

                Log.e("CommunityViewModel", "Community request failed: ${e.message}")
            }
        }
    }
}

sealed class RequestStatus {
    data object Idle : RequestStatus()
    data object Loading : RequestStatus()
    data object Success : RequestStatus()
    data class Error(val message: String) : RequestStatus()
}