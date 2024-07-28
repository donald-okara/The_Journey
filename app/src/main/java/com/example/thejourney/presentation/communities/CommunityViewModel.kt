package com.example.thejourney.presentation.communities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thejourney.domain.CommunityRepository
import com.example.thejourney.domain.UserRepository
import com.example.thejourney.presentation.sign_in.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunityViewModel(
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            _users.value = userRepository.fetchUsers()
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
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Community request failed: ${e.message}")
            }
        }
    }
}
