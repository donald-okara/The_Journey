package com.example.thejourney.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.thejourney.presentation.communities.AdminViewModel
import com.example.thejourney.presentation.communities.ApproveCommunityScreen
import com.example.thejourney.presentation.communities.CommunityViewModel
import com.example.thejourney.presentation.communities.RequestCommunityScreen
import com.example.thejourney.presentation.sign_in.SignInViewModel

@Composable
fun HomeScreen(
    signInViewModel: SignInViewModel,
    onNavigateToProfile: () -> Unit
) {
    val isAdmin by signInViewModel.isAdmin.collectAsState()
    
    Column {
        
        Button(onClick = { onNavigateToProfile() }) {
            Text(text = "Profile")
        }
        // Common UI components for all users
        Text(text = "Welcome to the app!")

        // Conditionally render admin UI components
        if (isAdmin) {
            AdminDashboard()
        }else{
            RequestCommunityScreen(viewModel = CommunityViewModel())
        }
        
    }
}

@Composable
fun AdminDashboard() {
    // Admin-specific UI components
    Text(text = "Admin Dashboard")
    // Include other admin functionalities here
    ApproveCommunityScreen(viewModel = AdminViewModel())
}
