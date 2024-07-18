package com.example.thejourney.presentation.communities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
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
    var type by remember { mutableStateOf("Campus") }  // Default type is Campus

    val currentUserId = viewModel.getCurrentUserId()  // Fetch current user ID from ViewModel

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Community Name") }
        )

        Text("Community Type")
        Row {
            RadioButton(
                selected = type == "Campus",
                onClick = { type = "Campus" }
            )
            Text(text = "Campus", modifier = Modifier.padding(start = 8.dp))

            RadioButton(
                selected = type == "Church",
                onClick = { type = "Church" }
            )
            Text(text = "Church", modifier = Modifier.padding(start = 8.dp))
        }

        Button(
            onClick = {
                viewModel.requestNewCommunity(name, type)
            }
        ) {
            Text("Request Community")
        }
    }
}

