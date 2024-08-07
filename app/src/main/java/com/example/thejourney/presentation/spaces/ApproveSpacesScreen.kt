package com.example.thejourney.presentation.spaces

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.thejourney.presentation.admin.CommunityState
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApproveSpacesScreen(
    viewModel: SpacesViewModel,
    pendingCount: Int,
    navigateBack : () -> Unit
) {
    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Spaces",
                        style = MaterialTheme.typography.titleLarge
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
        ApproveSpacesContent(
            modifier = Modifier.padding(innerPadding),
            viewModel = viewModel,
            pendingCount = pendingCount
        )

    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ApproveSpacesContent(
    modifier: Modifier = Modifier,
    pendingCount : Int,
    viewModel: SpacesViewModel
){
    val pendingState by viewModel.pendingSpacesState.collectAsState()
    val liveState by viewModel.liveSpacesState.collectAsState()
    val rejectedState by viewModel.rejectedSpacesState.collectAsState()

    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf("Live", "Pending", "Rejected")

    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                if (index == 1) {
                    BadgedBox(
                        badge = {
                            if (pendingCount > 0){
                                Badge {
                                    Text("$pendingCount")
                                }
                            }
                        }
                    ){
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }else{
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }
        }
        HorizontalPager(
            state = pagerState,
            count = tabs.size,
            modifier = modifier.fillMaxSize() // Make sure the pager takes up the remaining space
        ) { page ->
            when (page) {
                0 -> SpacesList(state = liveState)
                1 -> SpacesList(state = pendingState, viewModel = viewModel)
                2 -> SpacesList(state = rejectedState)
            }
        }
    }
}

@Composable
fun SpacesList(
    state: SpaceState,
    viewModel: SpacesViewModel? = null
) {
    val coroutineScope = rememberCoroutineScope()

    when (state) {
        is SpaceState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is SpaceState.Success -> {
            val spaces = state.spaces
            LazyColumn {
                items(spaces) { request ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Name: ${request.name}")
                            //Text("Requested By: ${request.requestedBy}")
                            Text("Status: ${request.approvalStatus}")

                            if (viewModel != null) { // Only show buttons if viewModel is passed
                                Row {
                                    Button(onClick = {
                                        coroutineScope.launch {
                                            viewModel.approveSpace(request)
                                        }

                                    }) {
                                        Text("Approve")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = {
                                        coroutineScope.launch {
                                            viewModel.rejectSpace(request)
                                        }
                                    }) {
                                        Text("Reject")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        is SpaceState.Error -> {
            val errorMessage = state.message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Dashboard failed to load: $errorMessage")
            }
        }
    }
}
