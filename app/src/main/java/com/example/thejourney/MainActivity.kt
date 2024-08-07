package com.example.thejourney

import WelcomeScreen
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thejourney.domain.CommunityRepository
import com.example.thejourney.domain.SpaceRepository
import com.example.thejourney.domain.UserRepository
import com.example.thejourney.presentation.admin.AdminCommunityView
import com.example.thejourney.presentation.admin.AdminDashboard
import com.example.thejourney.presentation.admin.AdminViewModel
import com.example.thejourney.presentation.communities.CommunitiesScreen
import com.example.thejourney.presentation.communities.CommunityDetails
import com.example.thejourney.presentation.communities.CommunityViewModel
import com.example.thejourney.presentation.communities.RequestCommunityScreen
import com.example.thejourney.presentation.home.HomeScreen
import com.example.thejourney.presentation.profile.ProfileScreen
import com.example.thejourney.presentation.sign_in.EmailSignInScreen
import com.example.thejourney.presentation.sign_in.EmailSignUpScreen
import com.example.thejourney.presentation.sign_in.GoogleAuthUiClient
import com.example.thejourney.presentation.sign_in.SignInViewModel
import com.example.thejourney.presentation.spaces.ApproveSpacesScreen
import com.example.thejourney.presentation.spaces.RequestSpaceScreen
import com.example.thejourney.presentation.spaces.SpacesViewModel
import com.example.thejourney.ui.theme.TheJourneyTheme
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val googleAuthUiClient by lazy {
            GoogleAuthUiClient(
                context = applicationContext,
                oneTapClient = Identity.getSignInClient(applicationContext)
            )
        }
        val coroutineScope = CoroutineScope(Dispatchers.IO) // Define CoroutineScope
        val storage = FirebaseStorage.getInstance()
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userRepository = UserRepository(db, auth, coroutineScope)
        val communityRepository by lazy { CommunityRepository(applicationContext , db, userRepository, storage, coroutineScope) }
        val spaceRepository by lazy { SpaceRepository(context = applicationContext ,userRepository = userRepository, communityRepository = communityRepository, coroutineScope= coroutineScope) }
        // Create viewmodel instances
        val signInViewModel = SignInViewModel()
        val communityViewModel by lazy { CommunityViewModel(communityRepository, userRepository)}
        val adminViewModel by lazy { AdminViewModel(userRepository = userRepository , communityRepository = communityRepository) }
        val spacesViewModel by lazy { SpacesViewModel(userRepository = userRepository, spaceRepository = spaceRepository) }

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

                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if (state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Signin successful",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("home") {
                                        popUpTo("emailSignIn") { inclusive = true }
                                    }
                                    signInViewModel.resetState()
                                }

                            }

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

                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if (state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Signin successful",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("home") {
                                        popUpTo("emailSignUp") { inclusive = true }
                                    }
                                    signInViewModel.resetState()
                                }

                            }

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
                                userRepository = userRepository,
                                onNavigateToProfile = { navController.navigate("profile") },
                                onNavigateToAdmin = { navController.navigate("admin_console") },
                                adminViewModel = adminViewModel,
                                navigateToCommunities = { navController.navigate("community_list") },
                                )
                        }

                        composable("admin_console") {
                            AdminDashboard(
                                onNavigateToCommunities = { navController.navigate("approve_community_screen") },
                                navigateBack = {navController.popBackStack()},
                                viewModel = adminViewModel
                            )
                        }

                        composable("approve_community_screen") {
                            val pendingCount by adminViewModel.pendingCount

                            AdminCommunityView(
                                pendingCount = pendingCount,
                                viewModel = adminViewModel,
                                navigateBack = {navController.popBackStack()}
                            )
                        }

                        composable("community_list"){
                            CommunitiesScreen(
                                navigateBack = {navController.popBackStack()},
                                navigateToAddCommunity = {navController.navigate("request_community")},
                                communityViewModel = communityViewModel,
                                navigateToCommunityDetails = { community ->
                                    navController.navigate("community_details/${community.id}")
                                }
                            )
                        }

                        composable("request_community"){
                            RequestCommunityScreen(
                                viewModel = communityViewModel,
                                navigateBack = {navController.popBackStack()},
                            )
                        }

                        composable("community_details/{communityId}") { backStackEntry ->
                            val communityId = backStackEntry.arguments?.getString("communityId")
                            val community = communityId?.let {
                                communityViewModel.getCommunityById(
                                    it
                                )
                            }
                            if (community != null) {
                                CommunityDetails(
                                    community = community,
                                    userRepository = userRepository,
                                    communityViewModel = communityViewModel,
                                    navigateBack = { navController.popBackStack() },
                                    spacesViewModel = spacesViewModel,
                                    onNavigateToSpace = {space -> navController.navigate("space_details/${space.id}")},
                                    onNavigateToAddSpace = {navController.navigate("request_space/${community.id}")},
                                    onNavigateToApproveSpaces = {navController.navigate("approve_spaces_screen")}
                                )
                            }
                        }

                        composable("request_space/{communityId}") { backStackEntry ->
                            val communityId = backStackEntry.arguments?.getString("communityId")
                            val community = communityId?.let {
                                communityViewModel.getCommunityById(
                                    it
                                )
                            }
                            if (community != null) {
                                RequestSpaceScreen(
                                    viewModel = spacesViewModel,
                                    navigateBack = { navController.popBackStack() },
                                    community = community
                                )
                            }else {
                                Log.e("CommunityDetails", "Community is null for communityId: $communityId")
                            }
                        }

                        composable("approve_spaces_screen"){
                            val pendingCount by spacesViewModel.pendingCount

                            ApproveSpacesScreen(
                                viewModel = spacesViewModel,
                                pendingCount = pendingCount,
                                navigateBack = {navController.popBackStack()}
                            )
                        }
                    }
                }
            }
        }
    }
}

