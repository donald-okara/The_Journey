package com.example.thejourney.presentation.communities

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestCommunityScreen(
    viewModel: CommunityViewModel = CommunityViewModel(),
    navigateBack: () -> Unit
) {
    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Community request",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Pop Back"
                        )
                    }
                }

            )
        },
        modifier = Modifier.fillMaxSize()
    ) {innerPadding->
        CommunityRequestForm(
            modifier = Modifier.padding(innerPadding),
            viewModel = viewModel,
            navigateBack = navigateBack
        )
    }

}

@Composable
fun CommunityRequestForm(
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel,
    navigateBack: () -> Unit
){
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Campus") }
    var bannerImageUri by remember { mutableStateOf<String?>(null) }
    var profileImageUri by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope() // Define the CoroutineScope
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    val launcherBanner = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { bannerImageUri = it.toString() }
    }
    val launcherProfile = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { profileImageUri = it.toString() }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image Section

        Box(
            modifier = modifier
                .size(256.dp)
        ) {
            if (profileImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(profileImageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop

                )
                IconButton(
                    onClick = { profileImageUri = null },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 6.dp, y = (-12).dp) // Offset to overflow the image
                        .size(48.dp) // Size of the button
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ){
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                Column {
                    Button(
                        onClick = { launcherProfile.launch("image/*") },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            modifier = Modifier.fillMaxSize(),
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Add profile picture"
                        )
                    }
                    Text(text = "Add profile image")
                }
            }
        }
        if (profileImageUri != null) {
            Text("Looking good!")
        }else{
            Text("Select profile image")

        }

        TextField(
            modifier = modifier.fillMaxWidth(),
            value = name,
            onValueChange = { name = it },
            label = { Text("Community Name") },
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        )

        HorizontalDivider()
        Text(
            "Community Type",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            RadioButton(
                selected = type == "Campus",
                onClick = { type = "Campus" }
            )
            Text(text = "Campus", modifier = Modifier.padding(start = 8.dp))

            Spacer(modifier = modifier.weight(1f))

            RadioButton(
                selected = type == "Church",
                onClick = { type = "Church" }
            )
            Text(text = "Church", modifier = Modifier.padding(start = 8.dp))
        }

        // Community Banner Section
        HorizontalDivider()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
        ) {
            if (bannerImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(bannerImageUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )
                IconButton(
                    onClick = { bannerImageUri = null },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 16.dp, y = 16.dp) // Adjust the offset as needed
                        .size(48.dp) // Size of the button
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ){
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

            } else {
                Button(
                    onClick = { launcherBanner.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        modifier = Modifier.fillMaxSize(),
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Select community banner"
                    )
                }
            }
        }

        if (bannerImageUri != null) {
            Text("Looking better!")
        }else{
            Text("Select community banner")

        }


        HorizontalDivider()


        Button(
            modifier = modifier.fillMaxWidth(),
            onClick = {
                if (name.isNotEmpty()) {
                    coroutineScope.launch {
                        try {
                            viewModel.requestNewCommunity(
                                name,
                                type,
                                bannerImageUri,
                                profileImageUri
                            )
                            navigateBack() // Navigate back on success
                        } catch (e: Exception) {
                            // Handle the error, e.g., show a toast or snackbar
                            Toast.makeText(
                                context,
                                "Request failed: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    // Handle validation error (e.g., show a toast or snackbar)
                    Toast.makeText(context, "Please enter a community name", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        ) {
            Text("Request Community")
        }
    }
}