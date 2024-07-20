package com.example.thejourney.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ApproveCommunityScreen(
    viewModel: AdminViewModel = AdminViewModel()
) {
    val pendingState by viewModel.pendingState.collectAsState()
    val liveState by viewModel.liveState.collectAsState()
    val rejectedState by viewModel.rejectedState.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Live", "Pending", "Rejected")

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Communities",
            style = MaterialTheme.typography.headlineLarge
        )
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        when (selectedTabIndex) {
            0 -> CommunityList(state = liveState)
            1 -> CommunityList(state = pendingState, viewModel = viewModel)
            2 -> CommunityList(state = rejectedState)
        }
    }
}

@Composable
fun CommunityList(state: CommunityState, viewModel: AdminViewModel? = null) {
    when (state) {
        is CommunityState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is CommunityState.Success -> {
            val communities = (state as CommunityState.Success).communities
            LazyColumn {
                items(communities) { request ->
                    Card(modifier = Modifier.padding(8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Name: ${request.name}")
                            Text("Type: ${request.type}")
                            Text("Requested By: ${request.requestedBy}")
                            Text("Status: ${request.status}")

                            if (viewModel != null) { // Only show buttons if viewModel is passed
                                Row {
                                    Button(onClick = { viewModel.approveCommunity(request) }) {
                                        Text("Approve")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = { viewModel.rejectCommunity(request) }) {
                                        Text("Reject")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        is CommunityState.Error -> {
            val errorMessage = (state as CommunityState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Dashboard failed to load: $errorMessage")
            }
        }
    }
}
