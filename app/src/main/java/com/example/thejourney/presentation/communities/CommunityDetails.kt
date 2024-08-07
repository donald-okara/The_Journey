package com.example.thejourney.presentation.communities

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.thejourney.R
import com.example.thejourney.domain.UserRepository
import com.example.thejourney.data.model.Community
import com.example.thejourney.data.model.Space
import com.example.thejourney.data.model.UserData
import com.example.thejourney.presentation.admin.AdminViewModel
import com.example.thejourney.presentation.admin.CommunityState
import com.example.thejourney.presentation.spaces.SpaceState
import com.example.thejourney.presentation.spaces.SpacesViewModel
import kotlinx.coroutines.launch

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
    val isLeader =
        community.members.any { it.containsKey(user?.userId) && it[user?.userId] == "leader" }

    LaunchedEffect(community.id) {
        spacesViewModel.fetchLiveSpacesByCommunity(community.id)
        spacesViewModel.fetchPendingSpacesByCommunity(community.id)
        spacesViewModel.fetchRejectedSpacesByCommunity(community.id)
    }

    Scaffold(
        topBar = {
            CommunityTopBar(
                community = community,
                user = user,
                navigateBack = { navigateBack() },
                communityViewModel = communityViewModel
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        CommunityDetailsContent(
            modifier = Modifier.padding(innerPadding),
            community = community,
            user = user,
            communityViewModel = communityViewModel,
            onNavigateToAddSpace = onNavigateToAddSpace,
            spacesViewModel = spacesViewModel,
            onNavigateToSpace = onNavigateToSpace,
            onNavigateToApproveSpaces = onNavigateToApproveSpaces
        )
    }
}

@Composable
fun CommunityDetailsContent(
    modifier: Modifier = Modifier,
    community: Community,
    onNavigateToAddSpace: (Community) -> Unit,
    onNavigateToSpace: (Space) -> Unit,
    user: UserData?,
    communityViewModel: CommunityViewModel,
    spacesViewModel: SpacesViewModel,
    onNavigateToApproveSpaces : () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        SpacesDashboardContent(
            spacesViewModel = spacesViewModel,
            onNavigateToApproveSpaces = { onNavigateToApproveSpaces() }
        )

        HorizontalDivider()

        SpacesRow(
            onNavigateToAddSpace = { onNavigateToAddSpace(community) },
            navigateToSpace = {},
            community = community,
            spacesViewModel = spacesViewModel
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


/**
* Spaces segment
*/

@Composable
fun SpacesRow(
    modifier: Modifier = Modifier,
    community: Community,
    onNavigateToAddSpace: (Community) -> Unit,
    navigateToSpace: (Space) -> Unit,
    spacesViewModel: SpacesViewModel
) {
    val spacesState by spacesViewModel.liveSpacesState.collectAsState()

    Row {
        Card(
            modifier = modifier
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
                        imageVector = Icons.Default.Groups, contentDescription = "Add spaces",
                        modifier = modifier
                            .align(Alignment.Center),
                    )
                }
                Text(text = "Add space")
            }

            //SpacesList
            when (spacesState){
                is SpaceState.Loading -> {
                    CircularProgressIndicator()
                }

                is SpaceState.Error -> {
                    Text(text = (spacesState as SpaceState.Error).message)
                }

                is SpaceState.Success-> {
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navigateToSpace(space) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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

@Composable
fun CommunityHeader(
    modifier: Modifier = Modifier,
    community: Community,
    user: UserData?,
    communityViewModel: CommunityViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var isJoined by remember {
        mutableStateOf(community.members.any { it.containsKey(user?.userId) })
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background Banner Image
        if (community.communityBannerUrl != null) {
            AsyncImage(
                model = community.communityBannerUrl,
                contentDescription = "Banner",
                alpha = 0.6f,
                modifier = modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.pattern_gold),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.6f,
                modifier = modifier
                    .scale(1.3f)
                    .fillMaxSize()
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
                .fillMaxWidth()
                .offset(y = 80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            if (community.profileUrl != null) {
                AsyncImage(
                    model = community.profileUrl,
                    contentDescription = "Profile picture",
                    modifier = modifier
                        .size(128.dp)
                        .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
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
                        .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }

            Spacer(modifier = modifier.weight(1f))

            if (!isJoined) {
                Button(onClick = {
                    coroutineScope.launch {
                        if (user != null) {
                            communityViewModel.onJoinCommunity(user, community)
                        }
                        isJoined = true
                    }
                }) {
                    Text(text = "Join")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityTopBar(
    modifier: Modifier = Modifier,
    community: Community,
    user: UserData?,
    navigateBack : () -> Unit,
    communityViewModel: CommunityViewModel
){
    Box{
        CommunityHeader(
            modifier= modifier.padding(bottom = 16.dp),
            community = community,
            user = user,
            communityViewModel = communityViewModel,
        )

        Spacer(modifier = modifier.padding(16.dp))

        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "",
                    style = MaterialTheme.typography.titleLarge
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
}