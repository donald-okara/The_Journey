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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
        setContent {
            TheJourneyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "welcome"){
                        
                        composable("welcome"){
                            val viewModel = viewModel<SignInViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    viewModel.setLoading(false)  // Reset loading state when result is received
                                    if (result.resultCode == RESULT_OK){
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.getSignInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
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
                                }

                            }

                            WelcomeScreen(
                                state = state,
                                onSignInWithGoogle = {
                                    lifecycleScope.launch {
                                        viewModel.setLoading(true) // Set loading state to true when sign-in starts
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
                            val viewModel = viewModel<SignInViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            EmailSignInScreen(
                                state = state,
                                onSignInWithEmail = { email, password ->
                                    viewModel.signInWithEmail(email, password)
                                },
                                onNavigateToSignUp = { navController.navigate("emailSignUp") }
                            )
                        }

                        composable("emailSignUp") {
                            val viewModel = viewModel<SignInViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            EmailSignUpScreen(
                                state = state,
                                onSignUpWithEmail = { email, password ->
                                    viewModel.signUpWithEmail(email, password)
                                },
                                onNavigateToSignIn = { navController.navigate("emailSignIn") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TheJourneyTheme {
        Greeting("Android")
    }
}