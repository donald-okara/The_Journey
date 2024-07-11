package com.example.thejourney.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun JourneyNavigation(
    modifier: Modifier = Modifier
){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome", builder =  {
        composable("welcome"){

        }
    })
}