package com.example.thejourney

import WelcomeScreen
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thejourney.presentation.admin.AdminCommunityView
import com.example.thejourney.presentation.admin.AdminDashboard
import com.example.thejourney.presentation.communities.CommunitiesScreen
import com.example.thejourney.presentation.home.HomeScreen
import com.example.thejourney.presentation.profile.ProfileScreen
import com.example.thejourney.presentation.sign_in.EmailSignInScreen
import com.example.thejourney.presentation.sign_in.EmailSignUpScreen
import com.example.thejourney.presentation.sign_in.GoogleAuthUiClient
import com.example.thejourney.presentation.sign_in.SignInViewModel
import com.example.thejourney.ui.theme.TheJourneyTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Create SignInViewModel instance
        val signInViewModel = SignInViewModel()

        // Determine the start destination
        val startDestination = if (signInViewModel.getSignedInUser() != null) "home" else "welcome"

        setContent {
            TheJourneyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("welcome") {
                            val state by signInViewModel.state.collectAsStateWithLifecycle()
                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    signInViewModel.setLoading(false)  // Reset loading state when result is received
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.getSignInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            signInViewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )

                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if (state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Signin successful",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                    signInViewModel.resetState()
                                }

                            }

                            WelcomeScreen(
                                state = state,
                                onSignInWithGoogle = {
                                    lifecycleScope.launch {
                                        signInViewModel.setLoading(true) // Set loading state to true when sign-in starts
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                },
                                onNavigateToSignIn = {
                                    navController.navigate("emailSignIn")
                                },
                                onNavigateToSignUp = {
                                    navController.navigate("emailSignUp")
                                }
                            )
                        }

                        composable("emailSignIn") {
                            val state by signInViewModel.state.collectAsStateWithLifecycle()

                            EmailSignInScreen(
                                state = state,
                                onSignInWithEmail = { email, password ->
                                    signInViewModel.signInWithEmail(email, password)
                                },
                                onNavigateToSignUp = { navController.navigate("emailSignUp") }
                            )
                        }

                        composable("emailSignUp") {
                            val state by signInViewModel.state.collectAsStateWithLifecycle()

                            EmailSignUpScreen(
                                state = state,
                                onSignUpWithEmail = { email, password ->
                                    signInViewModel.signUpWithEmail(email, password)
                                },
                                onNavigateToSignIn = { navController.navigate("emailSignIn") }
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                userData = signInViewModel.getSignedInUser(),
                                onSignOut = {
                                    lifecycleScope.launch {
                                        googleAuthUiClient.signOut()
                                        Toast.makeText(
                                            applicationContext,
                                            "Signed out",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        navController.navigate("welcome") {
                                            popUpTo("profile") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                onNavigateToProfile = {navController.navigate("profile") },
                                onNavigateToAdmin = { navController.navigate("admin_console") },
                                navigateToCommunities = {navController.navigate("community_list")}
                            )
                        }

                        composable("admin_console") {
                            AdminDashboard(
                                onNavigateToCommunities = { navController.navigate("approve_community_screen") },
                                navigateBack = {navController.popBackStack()}
                            )
                        }

                        composable("approve_community_screen") {
                            AdminCommunityView(
                                navigateBack = {navController.popBackStack()}
                            )
                        }

                        composable("community_list"){
                            CommunitiesScreen(
                                navigateBack = {navController.popBackStack()}
                            )
                        }
                    }
                }
            }
        }
    }
}
