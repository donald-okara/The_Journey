package com.example.thejourney

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.thejourney.domain.CommunityRepository
import com.example.thejourney.domain.SpaceRepository
import com.example.thejourney.domain.UserRepository
import com.example.thejourney.presentation.admin.AdminViewModel
import com.example.thejourney.presentation.communities.CommunityViewModel
import com.example.thejourney.presentation.spaces.SpacesViewModel

class ViewModelFactory(
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository,
    private val spaceRepository: SpaceRepository
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
            modelClass.isAssignableFrom(SpacesViewModel::class.java)-> {
                SpacesViewModel( userRepository = userRepository,spaceRepository = spaceRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
