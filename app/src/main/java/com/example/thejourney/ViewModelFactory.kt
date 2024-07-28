package com.example.thejourney

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.thejourney.domain.CommunityRepository
import com.example.thejourney.domain.UserRepository
import com.example.thejourney.presentation.communities.CommunityViewModel

class ViewModelFactory(
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
            return CommunityViewModel(communityRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}