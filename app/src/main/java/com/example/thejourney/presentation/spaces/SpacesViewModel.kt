package com.example.thejourney.presentation.spaces

import android.net.Uri
import android.util.Log
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

class SpacesViewModel(
    private val spaceRepository: SpaceRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _liveState = MutableStateFlow<SpaceState>(SpaceState.Loading)
    val liveState: StateFlow<SpaceState> = _liveState

    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users

    private val _spacesState = MutableStateFlow<List<Space>>(emptyList())
    val spacesState: StateFlow<List<Space>> = _spacesState

    private val _requestStatus = MutableStateFlow<RequestStatus>(RequestStatus.Idle)
    val requestStatus: StateFlow<RequestStatus> = _requestStatus

    init {
        fetchUsers()
        fetchLiveSpaces()
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

    private fun fetchLiveSpaces() {
        viewModelScope.launch {
            _liveState.value = SpaceState.Loading
            try {
                val spaces = spaceRepository.getLiveSpaces()
                _liveState.value = SpaceState.Success(spaces)
            } catch (e: Exception) {
                _liveState.value = SpaceState.Error("Failed to fetch live spaces: ${e.message}")
            }
        }
    }

    fun fetchSpacesByCommunity(communityId: String) {
        viewModelScope.launch {
            try {
                val spaces = spaceRepository.getLiveSpacesByCommunity(communityId)
                _spacesState.value = spaces
            } catch (e: Exception) {
                Log.e("SpacesViewModel", "Error fetching spaces for community $communityId: ${e.message}")
                _spacesState.value = emptyList()
            }
        }
    }

    fun getSpaceById(communityId: String, spaceId: String?): Space? {
        if (spaceId == null) return null

        val state = _liveState.value
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

                Log.e("CommunityViewModel", "Community request failed: ${e.message}")
            }
        }
    }
}

sealed class SpaceState {
    data object Loading : SpaceState()
    data class Success(val spaces: List<Space>) : SpaceState()
    data class Error(val message: String) : SpaceState()
}

