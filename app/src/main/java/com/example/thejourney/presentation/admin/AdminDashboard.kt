package com.example.thejourney.presentation.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AdminDashboard(
    modifier: Modifier = Modifier,
    viewModel: AdminViewModel = AdminViewModel(),
    onNavigateToCommunities: () -> Unit
) {
    val pendingState by viewModel.pendingState.collectAsState()
    val rejectedState by viewModel.rejectedState.collectAsState()
    val liveState by viewModel.liveState.collectAsState()

    Surface {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Admin Dashboard",
                style = MaterialTheme.typography.headlineLarge,
            )

            Spacer(modifier = modifier.weight(1f))

            Card(
                modifier = Modifier.padding(16.dp)
                    .clickable { onNavigateToCommunities() }
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Community Overview",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.padding(8.dp))

                    when (liveState) {
                        is CommunityState.Success ->
                            Row {
                                Text(
                                    text = "${(liveState as CommunityState.Success).communities.size}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.Green
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Text(
                                    text = "Live",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Green
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
}
