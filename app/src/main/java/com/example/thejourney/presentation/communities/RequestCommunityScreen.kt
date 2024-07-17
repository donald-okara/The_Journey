package com.example.thejourney.presentation.communities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RequestCommunityScreen(viewModel: CommunityViewModel) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Community Name") }
        )
        TextField(
            value = type,
            onValueChange = { type = it },
            label = { Text("Community Type") }
        )
        Button(
            onClick = {
                val communityRequest = CommunityRequest(
                    name = name,
                    type = type,
                    requestedBy = "currentUserId",  // Replace with the actual user ID
                    status = "Pending"
                )
                viewModel.requestNewCommunity(communityRequest)
            }
        ) {
            Text("Request Community")
        }
    }
}
