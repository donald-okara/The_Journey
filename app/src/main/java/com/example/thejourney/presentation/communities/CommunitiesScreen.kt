package com.example.thejourney.presentation.communities

import android.media.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.thejourney.R
import com.example.thejourney.presentation.admin.AdminViewModel
import com.example.thejourney.presentation.admin.CommunityState
import com.example.thejourney.presentation.communities.model.Community
import com.example.thejourney.ui.theme.TheJourneyTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitiesScreen(
    communityViewModel: CommunityViewModel,
    navigateBack: () -> Unit,
    navigateToAddCommunity: () -> Unit,
    navigateToCommunityDetails: (Community) -> Unit // Add this parameter
) {
    Scaffold (
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
                },

                actions = {
                    IconButton(onClick = { navigateToAddCommunity() }) {
                        Icon(
                            imageVector = Icons.Filled.GroupWork,
                            contentDescription = "Add group"
                        )
                    }
                }

            )
        },
        modifier = Modifier.fillMaxSize()
    ) {innerPadding->
        CommunitiesScreenContent(
            modifier = Modifier.padding(innerPadding),
            communityViewModel = communityViewModel,
            navigateToCommunityDetails = navigateToCommunityDetails
        )
    }
}

@Composable
fun CommunitiesScreenContent(
    modifier: Modifier = Modifier,
    navigateToCommunityDetails: (Community) -> Unit, // Add this parameter
    communityViewModel: CommunityViewModel
){
    val liveState by communityViewModel.liveState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()

    ){
        when (liveState) {
            is CommunityState.Loading -> {
                CircularProgressIndicator()
            }

            is CommunityState.Error -> {
                Text(text = (liveState as CommunityState.Error).message)
            }

            is CommunityState.Success -> {
                val communities = (liveState as CommunityState.Success).communities
                CommunitiesList(
                    communities = communities,
                    navigateToCommunityDetails = navigateToCommunityDetails
                )
            }
        }
    }
}

@Composable
fun CommunitiesList(
    modifier: Modifier = Modifier,
    communities: List<Community>,
    navigateToCommunityDetails: (Community) -> Unit // Add this parameter
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(communities) { community ->
            CommunityItem(
                navigateToCommunityDetails = navigateToCommunityDetails,
                community = community
            )
        }
    }
}

@Composable
fun CommunityItem(
    navigateToCommunityDetails: (Community) -> Unit, // Add this parameter
    community: Community
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navigateToCommunityDetails(community) },
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
                    model = community.communityBannerUrl,
                    contentDescription = "Community Banner",
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
                            model = community.profileUrl,
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
                                text = community.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = community.type,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                            )
                        }
                    }

                    if (community.aboutUs != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = community.aboutUs!!,
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


