package com.example.thejourney.presentation.communities

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.thejourney.presentation.sign_in.UserData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestCommunityScreen(
    viewModel: CommunityViewModel = CommunityViewModel(),
    navigateBack: () -> Unit
) {
    var selectedLeaders by remember { mutableStateOf(emptyList<UserData>()) }
    var selectedEditors by remember { mutableStateOf(emptyList<UserData>()) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var roleToAdd by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Community request") },
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        CommunityRequestForm(
            modifier = Modifier.padding(innerPadding),
            viewModel = viewModel,
            navigateBack = navigateBack,
            selectedLeaders = selectedLeaders,
            selectedEditors = selectedEditors,
            onLeadersChanged = { updatedLeaders -> selectedLeaders = updatedLeaders },
            onEditorsChanged = { updatedEditors -> selectedEditors = updatedEditors },
            onAddLeader = {
                roleToAdd = "Leader"
                showBottomSheet = true
            },
            onAddEditor = {
                roleToAdd = "Editor"
                showBottomSheet = true
            }
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState
        ) {
            UserSelectionBottomSheet(
                viewModel = viewModel,
                onDismiss = { showBottomSheet = false },
                roleToAdd = roleToAdd, // Pass the role to add
                onUserSelected = { user ->
                    when (roleToAdd) {
                        "Leader" -> {
                            selectedLeaders = selectedLeaders + user
                            Log.d("RequestCommunityScreen", "Selected Leader: ${user.username}")
                        }
                        "Editor" -> {
                            selectedEditors = selectedEditors + user
                            Log.d("RequestCommunityScreen", "Selected Editor: ${user.username}")
                        }
                    }
                    showBottomSheet = false
                }
            )
        }
    }
}




@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CommunityRequestForm(
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel,
    navigateBack: () -> Unit,
    selectedLeaders: List<UserData>,
    selectedEditors: List<UserData>,
    onLeadersChanged: (List<UserData>) -> Unit,
    onEditorsChanged: (List<UserData>) -> Unit,
    onAddLeader: () -> Unit,
    onAddEditor: () -> Unit,
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
                    modifier = Modifier
                        .fillMaxSize()
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
            label = { Text("Community Name (Cannot be changed later)") },
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

        // Chips for Leaders
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            selectedLeaders.forEach { user ->
                UserInputChip(user = user, onRemove = { onLeadersChanged(selectedLeaders.filter { it != user }) })
            }
            Button(
                onClick = { onAddLeader() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Add Leader"
                    )
                    Text("Add Leader")
                }
            }
        }

        HorizontalDivider()

        // Chips for Editors
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            selectedEditors.forEach { user ->
                UserInputChip(user = user, onRemove = { onEditorsChanged(selectedEditors.filter { it != user }) })

            }
            Button(
                onClick = { onAddEditor() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Add Editor"
                    )
                    Text("Add Editor")
                }
            }
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
                                profileImageUri,
                                selectedLeaders,
                                selectedEditors
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


@Composable
fun UserSelectionBottomSheet(
    viewModel: CommunityViewModel,
    onDismiss: () -> Unit,
    roleToAdd: String, // Pass the role to add
    onUserSelected: (UserData) -> Unit
) {
    val users by viewModel.users.collectAsState(emptyList())

    Column {
        // No need for role selection UI in the bottom sheet
        LazyColumn {
            items(users) { user ->
                UserItem(user = user) {
                    onUserSelected(user) // Pass the user and role
                }
            }
        }
    }
}



@Composable
fun UserItem(user: UserData, onUserSelected: (UserData) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .clickable { onUserSelected(user) }
            .padding(16.dp)
    ) {
        if (user.profilePictureUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(user.profilePictureUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp) // Specify a fixed size to avoid layout recalculations
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Text(
            text = user.username ?: "Unknown",
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}


@Composable
fun UserInputChip(
    modifier: Modifier = Modifier,
    user: UserData,
    onRemove: (UserData) -> Unit
) {
    val displayName = user.username?.take(3) ?: "Unknown"

    InputChip(
        selected = false,
        onClick = { onRemove(user) },
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = user.profilePictureUrl,
                    ),
                    contentDescription = user.username,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = displayName,
                )
            }
        },
        trailingIcon = {
            IconButton(onClick = { onRemove(user) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        colors = InputChipDefaults.inputChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            trailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}


