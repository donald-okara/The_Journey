@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.thejourney.presentation.communities

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.thejourney.R
import com.example.thejourney.domain.UserRepository
import com.example.thejourney.data.model.Community
import com.example.thejourney.data.model.Space
import com.example.thejourney.data.model.UserData
import com.example.thejourney.presentation.spaces.SpaceState
import com.example.thejourney.presentation.spaces.SpacesViewModel
import com.example.thejourney.ui.theme.TheJourneyTheme
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CommunityDetails(
    community: Community,
    navigateBack: () -> Unit,
    onNavigateToAddSpace: (Community) -> Unit,
    onNavigateToSpace: (Space) -> Unit,
    communityViewModel: CommunityViewModel,
    spacesViewModel: SpacesViewModel,
    userRepository: UserRepository,
    onNavigateToApproveSpaces: () -> Unit
) {
    val user = userRepository.getCurrentUser()
    val isLeader = community.members.any { it.containsKey(user?.userId) && it[user?.userId] == "leader" }
    var refreshTrigger by remember { mutableStateOf(false) } // Trigger for refreshing data

    LaunchedEffect(community.id) {
        communityViewModel.startObservingCommunityMembers(community.id)
        spacesViewModel.fetchLiveSpacesByCommunity(community.id)
        spacesViewModel.fetchPendingSpacesByCommunity(community.id)
        spacesViewModel.fetchRejectedSpacesByCommunity(community.id)
    }

    Scaffold(
        topBar = {
            CommunityTopBar(
                community = community,
                navigateBack = { navigateBack() },
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        CommunityDetailsContent(
            community = community,
            user = user,
            communityViewModel = communityViewModel,
            onNavigateToAddSpace = onNavigateToAddSpace,
            spacesViewModel = spacesViewModel,
            onNavigateToSpace = onNavigateToSpace,
            onNavigateToApproveSpaces = onNavigateToApproveSpaces,
            onRefresh = { refreshTrigger = !refreshTrigger },
            isLeader = isLeader
        )
    }
}

@Composable
fun CommunityDetailsContent(
    modifier: Modifier = Modifier,
    community: Community,
    isLeader : Boolean,
    onNavigateToAddSpace: (Community) -> Unit,
    onNavigateToSpace: (Space) -> Unit,
    user: UserData?,
    onRefresh: () -> Unit,
    communityViewModel: CommunityViewModel,
    spacesViewModel: SpacesViewModel,
    onNavigateToApproveSpaces : () -> Unit,
) {
    var isJoined by remember {
        mutableStateOf(community.members.any { it.containsKey(user?.userId) })
    }

    Column(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        CommunityHeader(
            community = community,
            user = user,
            isLeader = isLeader,
            onRefresh = onRefresh,
            communityViewModel = communityViewModel,
            spacesViewModel = spacesViewModel,
            onJoinStatusChanged = { newIsJoined ->
                isJoined = newIsJoined
            }
        )

        HorizontalDivider()

        if(isLeader){
            SpacesDashboardContent(
                spacesViewModel = spacesViewModel,
                onNavigateToApproveSpaces = { onNavigateToApproveSpaces() }
            )
        }

        HorizontalDivider()

        SpacesRow(
            onNavigateToAddSpace = { onNavigateToAddSpace(community) },
            navigateToSpace = {},
            community = community,
            spacesViewModel = spacesViewModel,
            isJoined = isJoined
        )
    }
}
/**
 * Community Leader dashboard
 */

@Composable
fun SpacesDashboardContent(
    modifier: Modifier = Modifier,
    spacesViewModel: SpacesViewModel,
    onNavigateToApproveSpaces: () -> Unit,
){
    val pendingState by spacesViewModel.pendingSpacesState.collectAsState()
    val rejectedState by spacesViewModel.rejectedSpacesState.collectAsState()
    val liveState by spacesViewModel.liveSpacesState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.Start
    ){
        Text(
            modifier = modifier.fillMaxWidth(),
            text = "Community Leader Dashboard",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Start,
        )
        Card(
            modifier = modifier
                .padding(16.dp)
                .clickable { onNavigateToApproveSpaces() }
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Spaces Overview",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = modifier.padding(8.dp))

                when (liveState) {
                    is SpaceState.Success ->
                        Row {
                            Text(
                                text = "${(liveState as SpaceState.Success).spaces.size}",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )

                            Spacer(modifier = modifier.weight(1f))

                            Text(
                                text = "Live",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                    is SpaceState.Error -> Text("Error: ${(liveState as SpaceState.Error).message}")
                    else -> CircularProgressIndicator()
                }

                Spacer(modifier = modifier.padding(8.dp))

                when (pendingState) {
                    is SpaceState.Success ->
                        Row {
                            Text(
                                text = "${(pendingState as SpaceState.Success).spaces.size}",
                                style = MaterialTheme.typography.labelLarge,
                            )

                            Spacer(modifier = modifier.weight(1f))

                            Text(
                                text = "Pending",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                    is SpaceState.Error -> Text("Error: ${(pendingState as SpaceState.Error).message}")
                    else -> CircularProgressIndicator()
                }

                Spacer(modifier = modifier.padding(8.dp))

                when (rejectedState) {
                    is SpaceState.Success ->
                        Row {
                            Text(
                                text = "${(rejectedState as SpaceState.Success).spaces.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = modifier.weight(1f))

                            Text(
                                text = "Rejected",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                    is SpaceState.Error -> Text("Error: ${(rejectedState as SpaceState.Error).message}")
                    else -> CircularProgressIndicator()
                }
            }
        }
    }

}


/**
* Spaces segment
*/

@Composable
fun SpacesRow(
    modifier: Modifier = Modifier,
    community: Community,
    onNavigateToAddSpace: (Community) -> Unit,
    navigateToSpace: (Space) -> Unit,
    spacesViewModel: SpacesViewModel,
    isJoined: Boolean
) {
    val cardWidth = 150.dp // Set a fixed width for the cards
    val spacesState by spacesViewModel.liveSpacesState.collectAsState()
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.Start
    ){
        Text(
            modifier = modifier.fillMaxWidth(),
            text = "${community.name} Spaces",
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        Row {
            if (isJoined) {
                Card(
                    modifier = modifier
                        .width(cardWidth)
                        .clickable {
                            onNavigateToAddSpace(community)
                        }
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "Add spaces",
                                modifier = modifier
                                    .align(Alignment.Center),
                            )
                        }
                        Text(
                            text = "Add space",
                            textAlign = TextAlign.Center,
                        )
                    }

                }
            }
            //SpacesList
            when (spacesState) {
                is SpaceState.Loading -> {
                    CircularProgressIndicator()
                }

                is SpaceState.Error -> {
                    Text(text = (spacesState as SpaceState.Error).message)
                }

                is SpaceState.Success -> {
                    val spaces = (spacesState as SpaceState.Success).spaces
                    SpaceList(
                        spaces = spaces,
                        navigateToSpace = navigateToSpace
                    )
                }

            }

        }
    }
}

@Composable
fun SpaceList(
    modifier: Modifier = Modifier,
    spaces: List<Space>,
    navigateToSpace: (Space) -> Unit
){
    LazyRow {
        items(spaces){space->
            SpaceCard(
                navigateToSpace = { navigateToSpace(space) },
                space = space
            )

        }
    }
}

@Composable
fun SpaceCard(
    navigateToSpace: (Space) -> Unit,
    space: Space
) {
    val cardWidth = 200.dp // Set a fixed width for the cards
    Card(
        modifier = Modifier
            .padding(8.dp)
            .width(cardWidth)
            .clickable { navigateToSpace(space) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(150.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                // AsyncImage with placeholder and error handling
                if(space.bannerUri != null){
                    AsyncImage(
                        model = space.bannerUri,
                        contentDescription = "Space Banner",
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.placeholder), // Replace with your placeholder drawable resource
                        error = painterResource(R.drawable.error), // Replace with your error drawable resource
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Semi-transparent overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Image with placeholder and error handling

                        if(space.profileUri != null){
                            AsyncImage(
                                model = space.profileUri,
                                contentDescription = "Community Profile",
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.placeholder), // Replace with your placeholder drawable resource
                                error = painterResource(R.drawable.error),
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                            )

                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = space.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    if (space.description != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = space.description!!,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHeader(
    modifier: Modifier = Modifier,
    community: Community,
    isLeader: Boolean,
    user: UserData?,
    onRefresh: () -> Unit,
    communityViewModel: CommunityViewModel,
    spacesViewModel: SpacesViewModel,
    onJoinStatusChanged: (Boolean) -> Unit // New parameter for propagating `isJoined`

) {
    var isJoined by remember {
    mutableStateOf(community.members.any { it.containsKey(user?.userId) })
    }
    val spacesState by spacesViewModel.liveSpacesState.collectAsState()
    val communityMembers by communityViewModel.communityMembers.collectAsState()

    //Modal bottom sheet
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(256.dp)
    ) {
        // AsyncImage with placeholder and error handling
        AsyncImage(
            model = community.communityBannerUrl,
            contentDescription = "Community Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Semi-transparent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )
        Column(
            modifier = modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Profile Image
                if (community.profileUrl != null) {
                    AsyncImage(
                        model = community.profileUrl,
                        contentDescription = "Profile picture",
                        modifier = modifier
                            .size(128.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.GroupWork,
                        contentDescription = "Profile picture",
                        modifier = modifier
                            .size(128.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )
                }

                Spacer(modifier = modifier.weight(1f))

                /**
                 * Community size
                 */

                Column(
                    modifier.padding(8.dp)
                ) {
                    when (spacesState) {
                        is SpaceState.Loading -> {
                            Text(
                                text = "0",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Spaces",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = modifier.alpha(0.8f)
                            )
                        }

                        is SpaceState.Success -> {
                            Text(
                                text = "${(spacesState as SpaceState.Success).spaces.size}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Spaces",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = modifier.alpha(0.8f)
                            )
                        }

                        is SpaceState.Error -> {
                            Text(
                                text = 0.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Red
                            )
                            Text(
                                text = "Spaces",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = modifier.alpha(0.8f)
                            )
                        }
                    }
                }

                Column(
                    modifier.padding(8.dp)
                ) {
                    Text(
                        text = "${communityMembers.size}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Members",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = modifier.alpha(0.8f)
                    )
                }

            }
            if (!isJoined) {
                Button(
                    onClick = {
                        if (user != null) {
                            communityViewModel.onJoinCommunity(user, community)
                            isJoined = true
                            onJoinStatusChanged(true)
                            onRefresh()
                        }
                    },
                    modifier = modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Join")
                }
            }else{
                OutlinedButton(
                    modifier = modifier.fillMaxWidth(),
                    onClick = { showBottomSheet = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ){
                        Text(text = "Joined")

                        Spacer(modifier = modifier.width(16.dp))

                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "More")
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            CommunityBottomSheet(isLeader = isLeader, community = community)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityTopBar(
    modifier: Modifier = Modifier,
    community: Community,
    navigateBack: () -> Unit,

) {
    TopAppBar(
        title = {
            Text(
                text = community.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        navigationIcon = {
            IconButton(onClick = { navigateBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.Transparent)
    )
}

@Composable
fun CommunityBottomSheet(
    modifier: Modifier = Modifier,
    isLeader: Boolean,
    community: Community,
){
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = community.name,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        HorizontalDivider()
        if (isLeader){
            CommunityBottomSheetItem(
                text = "Edit",
                isDangerZone = false,
                leadingIcon = Icons.Default.Edit,
                onClick = { TODO() }
            )
        }
        CommunityBottomSheetItem(
            text = "Report",
            isDangerZone = true,
            leadingIcon = Icons.Default.Flag,
            onClick = { TODO() }
        )
        CommunityBottomSheetItem(
            text = "Leave",
            isDangerZone = true,
            leadingIcon = Icons.AutoMirrored.Filled.Logout,
            onClick = { TODO() }
        )
        if(isLeader){
            CommunityBottomSheetItem(
                text = "Delete",
                isDangerZone = true,
                leadingIcon = Icons.Default.Delete,
                onClick = { TODO() }
            )
        }

    }
}

@Composable
fun CommunityBottomSheetItem(
    modifier: Modifier = Modifier,
    text: String,
    isDangerZone: Boolean = false,
    leadingIcon: ImageVector,
    onClick: () -> Unit
){

    val contentColor = if (isDangerZone) {
        MaterialTheme.colorScheme.onSurface.copy(
            red = (MaterialTheme.colorScheme.onSurface.red + MaterialTheme.colorScheme.onError.red) / 2,
            green = (MaterialTheme.colorScheme.onSurface.green + MaterialTheme.colorScheme.onError.green) / 2,
            blue = (MaterialTheme.colorScheme.onSurface.blue + MaterialTheme.colorScheme.onError.blue) / 2
        )
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
        modifier = modifier.alpha(0.8f),
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(color = contentColor),
    )
        Spacer(modifier = modifier.weight(1f))

        Icon(
            modifier = modifier.alpha(0.8f),
            imageVector = leadingIcon,
            contentDescription = text,
            tint = contentColor

        )

    }
}

@Preview
@Composable
fun CommunityBottomSheetItemPreview(){
    TheJourneyTheme {
        Column (
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            CommunityBottomSheetItem(
                text = "Edit",
                isDangerZone = false,
                leadingIcon = Icons.Default.Edit,
                onClick = { TODO() }
            )
            CommunityBottomSheetItem(
                text = "Report",
                isDangerZone = true,
                leadingIcon = Icons.Default.Flag,
                onClick = { TODO() }
            )

            CommunityBottomSheetItem(
                text = "Leave",
                isDangerZone = true,
                leadingIcon = Icons.AutoMirrored.Filled.Logout,
                onClick = { TODO() }
            )
            CommunityBottomSheetItem(
                text = "Delete",
                isDangerZone = true,
                leadingIcon = Icons.Default.Delete,
                onClick = { TODO() }
            )
        }

    }

}