package com.example.thejourney.presentation.spaces

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thejourney.data.model.Community
import com.example.thejourney.data.model.Space
import com.example.thejourney.data.model.UserData
import com.example.thejourney.domain.CommunityRepository
import com.example.thejourney.domain.SpaceRepository
import com.example.thejourney.domain.UserRepository
import com.example.thejourney.presentation.admin.CommunityState
import com.example.thejourney.presentation.communities.RequestStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SpacesViewModel(
    private val spaceRepository: SpaceRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
//    private val _liveState = MutableStateFlow<SpaceState>(SpaceState.Loading)
//    val liveState: StateFlow<SpaceState> = _liveState

    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users

    private val _liveSpacesState = MutableStateFlow<SpaceState>(SpaceState.Loading)
    val liveSpacesState: StateFlow<SpaceState> = _liveSpacesState

    private val _pendingSpacesState = MutableStateFlow<SpaceState>(SpaceState.Loading)
    val pendingSpacesState: StateFlow<SpaceState> = _pendingSpacesState

    private val _rejectedSpacesState = MutableStateFlow<SpaceState>(SpaceState.Loading)
    val rejectedSpacesState: StateFlow<SpaceState> = _rejectedSpacesState

    private val _requestStatus = MutableStateFlow<RequestStatus>(RequestStatus.Idle)
    val requestStatus: StateFlow<RequestStatus> = _requestStatus
    val pendingCount = mutableIntStateOf(0)

    init {
        fetchUsers()
        //fetchLiveSpaces()
    }

    fun clearRequestStatus() {
        _requestStatus.value = RequestStatus.Idle
    }
    private fun fetchUsers() {
        viewModelScope.launch {
            _users.value = userRepository.fetchUsers()
        }
    }

    fun fetchCommunityUsers(communityId: String) {
        viewModelScope.launch {
            try {
                val communityUsers = userRepository.fetchUsersByCommunity(communityId)
                _users.value = communityUsers
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Error fetching community users: ${e.message}")
            }
        }
    }

//    private fun fetchLiveSpaces() {
//        viewModelScope.launch {
//            _liveState.value = SpaceState.Loading
//            try {
//                val spaces = spaceRepository.getLiveSpaces()
//                _liveState.value = SpaceState.Success(spaces)
//            } catch (e: Exception) {
//                _liveState.value = SpaceState.Error("Failed to fetch live spaces: ${e.message}")
//            }
//        }
//    }

    fun fetchLiveSpacesByCommunity(communityId: String) {
        viewModelScope.launch {
            spaceRepository.observeLiveSpacesByCommunity(communityId)
                .collect { spaces ->
                    _liveSpacesState.value = SpaceState.Success(spaces)
                }
        }
    }

    fun fetchPendingSpacesByCommunity(communityId: String) {
        viewModelScope.launch {
            _pendingSpacesState.value = SpaceState.Loading
            try {
                val spaces = spaceRepository.getPendingSpacesByCommunity(communityId)
                _pendingSpacesState.value = SpaceState.Success(spaces)
                pendingCount.intValue = spaces.size
            } catch (e: Exception) {
                Log.e("SpacesViewModel", "Error pending fetching spaces for community $communityId: ${e.message}")
                _pendingSpacesState.value = SpaceState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchRejectedSpacesByCommunity(communityId: String) {
        viewModelScope.launch {
            _rejectedSpacesState.value = SpaceState.Loading
            try {
                val spaces = spaceRepository.getRejectedSpacesByCommunity(communityId)
                _rejectedSpacesState.value = SpaceState.Success(spaces)
            } catch (e: Exception) {
                Log.e("SpacesViewModel", "Error rejected fetching spaces for community $communityId: ${e.message}")
                _rejectedSpacesState.value = SpaceState.Error(e.message ?: "Unknown error")
            }
        }
    }


    fun getSpaceById(communityId: String, spaceId: String?): Space? {
        if (spaceId == null) return null

        val state = _liveSpacesState.value
        return if (state is SpaceState.Success) {
            state.spaces
                .filter { it.parentCommunity == communityId }  // Filter spaces by communityId
                .find { it.id == spaceId }                 // Find the specific space by spaceId
        } else {
            null
        }
    }


    fun onJoinSpace(user : UserData, space: Space){
        viewModelScope.launch {
            try {
                user.userId.let {
                    userRepository.updateUserSpaces(
                        userId = it,
                        spaceId = space.id,
                        role = "member"
                    )
                }

                spaceRepository.addMembers(
                    userId = user.userId,
                    spaceId = space.id
                )
            } catch (e: Exception) {
                Log.e("SpaceViewModel", "Space join failed: ${e.message}")
            }

        }

    }

    fun requestNewSpace(
        parentCommunityId : String,
        spaceName : String,
        profilePictureUri : Uri?,
        bannerUri : Uri?,
        description : String?,
        membersRequireApproval : Boolean,
        selectedLeaders : List<UserData>,
    ) {

        viewModelScope.launch {
            try {
                spaceRepository.requestNewSpace(
                    parentCommunityId = parentCommunityId,
                    spaceName = spaceName,
                    bannerUri = bannerUri,
                    profilePictureUri = profilePictureUri,
                    selectedLeaders = selectedLeaders,
                    membersRequireApproval = membersRequireApproval,
                    description = description
                )
                _requestStatus.value = RequestStatus.Success
            } catch (e: Exception) {
                _requestStatus.value = RequestStatus.Error(e.message ?: "Check your internet and try again")

                Log.e("SpacesViewModel", "Space request failed: ${e.message}")
            }
        }
    }

    fun approveSpace(request: Space) {
        viewModelScope.launch {
            try {
                Log.d("SpacesViewModel", "Attempting to approve space with ID: ${request.id}")

                spaceRepository.db.collection("spaces").document(request.id)
                    .update("approvalStatus", "Live")
                    .await()
                fetchLiveSpacesByCommunity(request.parentCommunity)
                fetchPendingSpacesByCommunity(request.parentCommunity)
            } catch (e: Exception) {
                Log.w("SpacesViewModel", "Error approving space", e)
                _pendingSpacesState.value = SpaceState.Error("Error approving space: ${e.message}")
            }
        }
    }

    fun rejectSpace(request: Space) {
        viewModelScope.launch {
            try {
                Log.d("AdminViewModel", "Attempting to reject community with ID: ${request.name}")

                spaceRepository.db.collection("spaces").document(request.name)
                    .update("status", "Rejected")
                    .await()
                fetchPendingSpacesByCommunity(request.parentCommunity)
                fetchRejectedSpacesByCommunity(request.parentCommunity)
            } catch (e: Exception) {
                Log.w("AdminViewModel", "Error rejecting community", e)
                _pendingSpacesState.value = SpaceState.Error("Error rejecting space: ${e.message}")
            }
        }
    }
}

sealed class SpaceState {
    data object Loading : SpaceState()
    data class Success(val spaces: List<Space>) : SpaceState()
    data class Error(val message: String) : SpaceState()
}

