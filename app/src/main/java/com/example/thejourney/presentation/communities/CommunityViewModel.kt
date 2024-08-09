package com.example.thejourney.presentation.communities

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thejourney.domain.CommunityRepository
import com.example.thejourney.domain.UserRepository
import com.example.thejourney.presentation.admin.CommunityState
import com.example.thejourney.data.model.Community
import com.example.thejourney.data.model.UserData
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _communityMembers = MutableStateFlow<List<UserData>>(emptyList())
    val communityMembers: StateFlow<List<UserData>> = _communityMembers

    private var communityId: String? = null
    private var listenerRegistration: ListenerRegistration? = null

    init {
        fetchUsers()
        fetchLiveCommunities()
    }

    /**
     * CREATE
     */

    /**
     * Function to request a new community
     */
    fun requestNewCommunity(
        communityName: String,
        communityType: String,
        bannerUri: Uri?,
        aboutUs : String?,
        profileUri: Uri?,
        selectedLeaders: List<UserData>,
        selectedEditors: List<UserData>
    ) {

        viewModelScope.launch {
            try {
                communityRepository.requestNewCommunity(
                    communityName = communityName,
                    communityType = communityType,
                    bannerUri = bannerUri,
                    profileUri = profileUri,
                    selectedLeaders = selectedLeaders,
                    selectedEditors = selectedEditors,
                    aboutUs = aboutUs
                )
                _requestStatus.value = RequestStatus.Success
            } catch (e: Exception) {
                _requestStatus.value = RequestStatus.Error(e.message ?: "Check your internet and try again")

                Log.e("CommunityViewModel", "Community request failed: ${e.message}")
            }
        }
    }

    fun clearRequestStatus() {
        _requestStatus.value = RequestStatus.Idle
    }

    /**
     * READ
     */

    /**
    * Observe community members
     */
    fun startObservingCommunityMembers(communityId: String) {
        this.communityId = communityId
        listenerRegistration?.remove() // Remove any existing listener
        listenerRegistration = communityRepository.observeCommunityMembers(communityId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CommunityViewModel", "Error observing community members: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val members = snapshot.toObjects(UserData::class.java)
                    _communityMembers.value = members
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove() // Clean up the listener when ViewModel is cleared
    }

    /**
     * Fetch users from the repository
     */
    private fun fetchUsers() {
        viewModelScope.launch {
            _users.value = userRepository.fetchUsers()
        }
    }

    /**
     * Fetch live communities
     */
    private fun fetchLiveCommunities() {
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

    /**
     * Function to get a community by its ID for navigation
     */
    fun getCommunityById(communityId: String): Community? {

        val state = _liveState.value
        return if (state is CommunityState.Success) {
            state.communities.find { it.id == communityId }
        } else {
            null
        }
    }

    /**
     * UPDATE
     */

    /**
     * Function to join a community
     */
    fun onJoinCommunity(user : UserData, community: Community){
        viewModelScope.launch {
            try {
                user.userId.let {
                    userRepository.updateUserCommunities(
                        userId = it,
                        communityId = community.id,
                        role = "member"
                    )
                }

                communityRepository.addMembers(
                    userId = user.userId,
                    communityId = community.id
                )

                //fetchCommunityMembers(communityId = community.id)

            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Community join failed: ${e.message}")
            }

        }

    }

    /**
     * Demote user to member
     */
    suspend fun demoteMember(
        userId : String,
        communityId: String
    ){
        communityRepository.demoteMember(userId = userId,communityId = communityId)
    }

    /**
     * Add leaders or editors
      */
    suspend fun addLeadersOrEditors(
        communityId: String,
        newLeaders: List<UserData>? = null,
        newEditors: List<UserData>? = null
    ){
        communityRepository.addLeadersOrEditors(
            communityId = communityId,
            newLeaders = newLeaders,
            newEditors = newEditors
        )
    }

    /**
     * update community fields
     */
    suspend fun updateCommunityFields(
        communityId: String,
        updatedFields: Map<String, Any>
    ){
        communityRepository.updateCommunityFields(
            communityId = communityId,
            updatedFields = updatedFields
        )
    }

    /**
     * DELETE
     */


    /**
     * LEGACY
     */

    /**
     * Fetch community members legacy code
     */
//    private fun fetchCommunityMembers(communityId: String) {
//        viewModelScope.launch {
//            try {
//                val members = communityRepository.getCommunityMembers(communityId)
//                _communityMembers.value = members
//            } catch (e: Exception) {
//                Log.e("CommunityViewModel", "Error fetching community members: ${e.message}")
//            }
//        }
//    }
}





sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}


sealed class RequestStatus {
    data object Idle : RequestStatus()
    data object Loading : RequestStatus()
    data object Success : RequestStatus()
    data class Error(val message: String) : RequestStatus()
}