import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.thejourney.R
import com.example.thejourney.presentation.sign_in.SignInState

@Composable
fun WelcomeScreen(
    state: SignInState,
    onSignInWithGoogle: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.pattern_gold),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .scale(1.3f),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.welcome),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Button(
                    onClick = onSignInWithGoogle,
                    shape = RoundedCornerShape(64.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.google_logo),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp) // Adjusted size for better UI
                        )

                        Spacer(Modifier.width(24.dp))

                        Text("Continue with Google")
                    }

                }

                //Spacer(modifier = Modifier.height(16.dp))

                Text("or")

                //Spacer(modifier = Modifier.height(16.dp))


                OutlinedButton(
                    onClick = onNavigateToSignUp,
                    shape = RoundedCornerShape(64.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(

                            imageVector = Icons.Default.Email,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Sign in with Email",
                            modifier = Modifier.size(24.dp) // Adjusted size for better UI
                        )

                        Spacer(Modifier.width(24.dp))

                        Text("Sign Up with Email")

                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Already have an account? "
                    )
                    TextButton(
                        onClick = { onNavigateToSignIn() }
                    ) {
                        Text(
                            text = "Log in",
                            color = MaterialTheme.colorScheme.primary)

                    }

                }

            }
        }
    }
}
