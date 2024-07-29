package com.example.thejourney.presentation.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    modifier: Modifier = Modifier,
    viewModel: AdminViewModel,
    onNavigateToCommunities: () -> Unit,
    navigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                Text(
                    text = "Admin Dashboard",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
              navigationIcon = {
                  IconButton(onClick = { navigateBack() }) {
                      Icon(
                          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription = "Pop Back"
                      )
                  }
              }

            )
        },
        modifier = Modifier.fillMaxSize()
    ) {innerPadding->
        DashboardContent(
            modifier
                .padding(innerPadding)
                .fillMaxSize(),
            viewModel = viewModel,
            onNavigateToCommunities = onNavigateToCommunities
        )
    }
}

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    viewModel: AdminViewModel,
    onNavigateToCommunities: () -> Unit,
){
    val pendingState by viewModel.pendingState.collectAsState()
    val rejectedState by viewModel.rejectedState.collectAsState()
    val liveState by viewModel.liveState.collectAsState()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .clickable { onNavigateToCommunities() }
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Community Overview",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.padding(8.dp))

                when (liveState) {
                    is CommunityState.Success ->
                        Row {
                            Text(
                                text = "${(liveState as CommunityState.Success).communities.size}",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = "Live",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    is CommunityState.Error -> Text("Error: ${(liveState as CommunityState.Error).message}")
                    else -> CircularProgressIndicator()
                }

                Spacer(modifier = Modifier.padding(8.dp))

                when (pendingState) {
                    is CommunityState.Success ->
                        Row {
                            Text(
                                text = "${(pendingState as CommunityState.Success).communities.size}",
                                style = MaterialTheme.typography.labelLarge,
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = "Pending",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    is CommunityState.Error -> Text("Error: ${(pendingState as CommunityState.Error).message}")
                    else -> CircularProgressIndicator()
                }

                Spacer(modifier = Modifier.padding(8.dp))

                when (rejectedState) {
                    is CommunityState.Success ->
                        Row {
                            Text(
                                text = "${(rejectedState as CommunityState.Success).communities.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = "Rejected",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    is CommunityState.Error -> Text("Error: ${(rejectedState as CommunityState.Error).message}")
                    else -> CircularProgressIndicator()
                }
            }
        }

        Spacer(modifier = modifier.weight(1f))
    }
}