package com.example.thejourney.presentation.communities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thejourney.presentation.admin.AdminViewModel
import com.example.thejourney.presentation.admin.CommunityState
import com.example.thejourney.presentation.communities.model.Community
import com.example.thejourney.ui.theme.TheJourneyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitiesScreen(
    adminViewModel: AdminViewModel = viewModel(),
    navigateBack: () -> Unit
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
                            contentDescription = "Open drawer"
                        )
                    }
                }

            )
        },
        modifier = Modifier.fillMaxSize()
    ) {innerPadding->
        CommunitiesScreenContent(
            modifier = Modifier.padding(innerPadding),
            adminViewModel = adminViewModel
        )
    }
}

@Composable
fun CommunitiesScreenContent(
    modifier: Modifier = Modifier,
    adminViewModel: AdminViewModel
){
    val liveState by adminViewModel.liveState.collectAsState()

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
                CommunitiesList(communities = communities)
            }
        }
    }
}

@Composable
fun CommunitiesList(
    communities: List<Community>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(communities) { community ->
            CommunityItem(community = community)
        }
    }
}

@Composable
fun CommunityItem(community: Community) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row{
                Text(
                    text = community.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = community.type, style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "Leader: ${community.requestedBy}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

