package com.example.thejourney.presentation.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ApproveCommunityScreen(viewModel: AdminViewModel) {
    val state by viewModel.state.collectAsState()

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
        is CommunityState.Error -> {
            val errorMessage = (state as CommunityState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Dashboard failed to load: $errorMessage")
            }
        }
    }
}
