package com.example.thejourney.presentation.spaces

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.thejourney.data.model.Community
import com.example.thejourney.data.model.UserData
import com.example.thejourney.presentation.communities.RequestStatus
import com.example.thejourney.presentation.communities.UserInputChip
import com.example.thejourney.presentation.communities.UserItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestSpaceScreen(
    viewModel: SpacesViewModel,
    navigateBack: () -> Unit,
    community : Community
) {
    val communityId = community.id
    var selectedLeaders by remember { mutableStateOf(emptyList<UserData>()) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var roleToAdd by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Space request") },
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ){innerPadding ->
        RequestSpaceForm(
            modifier = Modifier.padding(innerPadding),
            spacesViewModel = viewModel,
            parentCommunityId = communityId,
            navigateBack = navigateBack,
            //onSpaceRequested = { viewModel.requestNewSpace() },
            onLeadersChanged = { updatedLeaders -> selectedLeaders = updatedLeaders },
            onAddLeader = {
                roleToAdd = "Leader"
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
                selectedLeaders = selectedLeaders,
                communityId = communityId,
                onUserSelected = { user ->
                    selectedLeaders = selectedLeaders + user
                    Log.d("RequestSpaceScreen", "Selected Leader: ${user.username}")
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RequestSpaceForm(
    modifier: Modifier = Modifier,
    spacesViewModel: SpacesViewModel,
    navigateBack: () -> Unit,
    parentCommunityId: String,
    onLeadersChanged: (List<UserData>) -> Unit,
    onAddLeader: () -> Unit,
    ) {
    val scrollState = rememberScrollState()
    val requestStatus by spacesViewModel.requestStatus.collectAsState()

    var spaceName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var bannerUri by remember { mutableStateOf<Uri?>(null) }
    var membersRequireApproval by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var selectedLeaders by remember { mutableStateOf<List<UserData>>(emptyList()) }
    val context = LocalContext.current
    val launcherBanner = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { bannerUri = it }
        }
    )
    val launcherProfile = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { profilePictureUri = it }
        }
    )
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyRow {
            item{
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = modifier
                        .fillMaxHeight()
                        .width(320.dp) // Uniform width for each item
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = modifier
                            .size(256.dp)
                    ) {
                        if (profilePictureUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(profilePictureUri),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop

                            )

                            IconButton(
                                onClick = { profilePictureUri = null },
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
                    if (profilePictureUri != null) {
                        Text("Looking good!")
                    }else{
                        Text("Select profile image")

                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        modifier = modifier.fillMaxWidth(),
                        value = spaceName,
                        onValueChange = { spaceName = it },
                        label = { Text("Space Name") },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }

            item{
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier
                        .width(320.dp) // Uniform width for each item
                        .padding(8.dp) // Optional padding
                        .fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                    ) {
                        if (bannerUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(bannerUri),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                            IconButton(
                                onClick = { bannerUri = null },
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

                    if (bannerUri != null) {
                        Text("Looking better!")
                    } else {
                        Text("Select community banner")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        modifier = modifier.fillMaxWidth(),
                        value = description,
                        onValueChange = { description = it },
                        maxLines = 10,
                        label = { Text("Space description") },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = membersRequireApproval,
                            onCheckedChange = { membersRequireApproval = it }
                        )
                        Text(text = "Members require approval to join")
                    }

                }
            }

            item {
                // Chips for Leaders
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier
                        .fillMaxHeight()
                        .width(320.dp) // Uniform width for each item
                        .padding(8.dp) // Optional padding
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        selectedLeaders.forEach { user ->
                            UserInputChip(
                                user = user,
                                onRemove = { onLeadersChanged(selectedLeaders.filter { it != user }) })
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
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

       // TODO: Add UI for selecting leaders (selectedLeaders)
        Text("Swipe to continue >>")

        HorizontalDivider()

        Button(
            onClick = {
                coroutineScope.launch {
                    spacesViewModel.requestNewSpace(
                        parentCommunityId = parentCommunityId,
                        spaceName = spaceName,
                        profilePictureUri = profilePictureUri,
                        bannerUri = bannerUri,
                        description = description,
                        membersRequireApproval = membersRequireApproval,
                        selectedLeaders = selectedLeaders
                    )
                    navigateBack()
                }

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Request Space")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (requestStatus) {
            RequestStatus.Success -> {
                Text(text = "Space requested successfully", color = MaterialTheme.colorScheme.primary)
                spacesViewModel.clearRequestStatus()
            }
            is RequestStatus.Error -> {
                Text(text = (requestStatus as RequestStatus.Error).message, color = MaterialTheme.colorScheme.error)
            }
            else -> { /* Do nothing */ }
        }
    }
}

@Composable
fun UserSelectionBottomSheet(
    viewModel: SpacesViewModel,
    communityId: String,
    onDismiss: () -> Unit,
    selectedLeaders: List<UserData>,
    onUserSelected: (UserData) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Fetch users when the bottom sheet is displayed
    LaunchedEffect(Unit) {
        viewModel.fetchCommunityUsers(communityId)
    }

    val users by viewModel.users.collectAsState(emptyList())
    val filteredUsers = users.filterNot { user ->
        selectedLeaders.contains(user)
    }

    LazyColumn {
        items(filteredUsers) { user ->
            UserItem(user = user) {
                onUserSelected(user)
            }
        }
    }
}
