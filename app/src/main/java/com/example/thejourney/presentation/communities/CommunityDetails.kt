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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.thejourney.presentation.spaces.SpacesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetails(
    community: Community,
    navigateBack: () -> Unit,
    onNavigateToAddSpace: (Community) -> Unit,
    onNavigateToSpace: (Space) -> Unit,
    communityViewModel: CommunityViewModel,
    spacesViewModel: SpacesViewModel,
    userRepository: UserRepository,
) {
    val spacesState by spacesViewModel.spacesState.collectAsState()
    val user = userRepository.getCurrentUser()
    val isLeader =
        community.members.any { it.containsKey(user?.userId) && it[user?.userId] == "leader" }

    LaunchedEffect(community.id) {
        spacesViewModel.fetchSpacesByCommunity(community.id)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Communities",
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
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        CommunityDetailsContent(
            modifier = Modifier.padding(innerPadding),
            community = community,
            user = user,
            communityViewModel = communityViewModel,
            spaces = spacesState,
            onNavigateToAddSpace = onNavigateToAddSpace,
            onNavigateToSpace = onNavigateToSpace
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
    spaces: List<Space>,
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommunityHeader(
            community = community,
            user = user,
            communityViewModel = communityViewModel,
        )

        SpacesRow(
            spaces = spaces,
            onNavigateToAddSpace = { onNavigateToAddSpace(community) },
            navigateToSpace = {},
            community = community
        )
    }
}

@Composable
fun SpacesRow(
    modifier: Modifier = Modifier,
    community: Community,
    spaces: List<Space>,
    onNavigateToAddSpace: (Community) -> Unit,
    navigateToSpace: (Space) -> Unit
) {
    LazyRow {
        item {
            Card(
                modifier = modifier.clickable {
                    onNavigateToAddSpace(community)
                }
            ) {
                Column {
                    Box(modifier = modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = Icons.Default.Groups, contentDescription = "Add spaces",
                            modifier = modifier
                                .align(Alignment.Center),
                        )
                    }
                    Text(text = "Add space")
                }
            }
        }

        items(spaces) { space ->
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
                AsyncImage(
                    model = space.bannerUri,
                    contentDescription = "Space Banner",
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.placeholder), // Replace with your placeholder drawable resource
                    error = painterResource(R.drawable.error), // Replace with your error drawable resource
                    modifier = Modifier.fillMaxSize()
                )


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
                        Column {
                            Text(
                                text = space.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
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
    communityViewModel: CommunityViewModel,

    ) {
    val coroutineScope = rememberCoroutineScope()
    var isJoined by remember {
        mutableStateOf(community.members.any { it.containsKey(user?.userId) })
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        if (community.communityBannerUrl != null) {
            AsyncImage(
                model = community.communityBannerUrl,
                contentDescription = "Banner",
                alpha = 0.8f,
                modifier = modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.pattern_gold),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.8f,
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
        ) {// Profile image
            if (community.profileUrl != null) {
                AsyncImage(
                    model = community.profileUrl,
                    contentDescription = "Profile picture",
                    modifier
                        .size(128.dp)
                        .size(48.dp)
                        .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.GroupWork,
                    contentDescription = "Profile picture",
                    modifier
                        .size(128.dp)
                        .offset(y = 40.dp)
                        .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),

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