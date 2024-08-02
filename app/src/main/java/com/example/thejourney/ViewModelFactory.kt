package com.example.thejourney

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.thejourney.domain.CommunityRepository
import com.example.thejourney.domain.UserRepository
import com.example.thejourney.presentation.admin.AdminViewModel
import com.example.thejourney.presentation.communities.CommunityViewModel

class ViewModelFactory(
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CommunityViewModel::class.java) -> {
                CommunityViewModel(communityRepository, userRepository) as T
            }
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> {
                AdminViewModel(userRepository, communityRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
