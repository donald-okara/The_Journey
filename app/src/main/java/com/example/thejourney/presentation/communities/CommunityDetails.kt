package com.example.thejourney.presentation.communities

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.thejourney.R
import com.example.thejourney.presentation.communities.model.Community

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetails(
    community : Community,
    navigateBack: () -> Unit
){
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
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {innerPadding->
        ProfileHeader(modifier = Modifier.padding(innerPadding), community = community)
    }
}

@Composable
fun ProfileHeader(
    modifier: Modifier = Modifier,
    community: Community
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        if (community.communityBannerUrl != null){
            AsyncImage(
                model = community.communityBannerUrl,
                contentDescription = "Banner",
                alpha = 0.8f,
                modifier = modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }else{
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

        // Profile image
        if (community.profileUrl != null) {
            AsyncImage(
                model = community.profileUrl,
                contentDescription = "Profile picture",
                modifier
                    .size(128.dp)
                    .align(Alignment.BottomStart)
                    .offset(y = 40.dp)
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
                    .align(Alignment.BottomStart)
                    .offset(y = 40.dp)
                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),

                )
        }

//        // Additional buttons (like Follow, More, etc.)
//        Row(
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(end = 16.dp, bottom = 16.dp),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            Button(
//                onClick = { /* TODO: Handle follow */ },
//                shape = RoundedCornerShape(64.dp)
//            ) {
//                Text(text = "Join", style = MaterialTheme.typography.bodyMedium)
//            }
//
//            IconButton(
//                onClick = { /* TODO: Handle more options */ }
//            ) {
//                Icon(Icons.Default.MoreVert, contentDescription = null)
//            }
//        }



    }
}