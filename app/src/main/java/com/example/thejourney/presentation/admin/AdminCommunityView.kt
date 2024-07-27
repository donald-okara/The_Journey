package com.example.thejourney.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCommunityView(
    viewModel: AdminViewModel = AdminViewModel(),
    navigateBack : () -> Unit
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
                        contentDescription = "Pop Back"
                    )
                }
            }

        )
    },
    modifier = Modifier.fillMaxSize()
    ) {innerPadding->
        AdminCommunityViewContent(
            modifier = Modifier.padding(innerPadding),
            viewModel = viewModel
        )

    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AdminCommunityViewContent(
    modifier: Modifier = Modifier,
    viewModel: AdminViewModel
){
    val pendingState by viewModel.pendingState.collectAsState()
    val liveState by viewModel.liveState.collectAsState()
    val rejectedState by viewModel.rejectedState.collectAsState()
    val pendingCount by remember { viewModel.pendingCount }

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
                0 -> CommunityList(state = liveState)
                1 -> CommunityList(state = pendingState, viewModel = viewModel)
                2 -> CommunityList(state = rejectedState)
            }
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
            val communities = state.communities
            LazyColumn {
                items(communities) { request ->
                    Card(
                        modifier = Modifier.padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Name: ${request.name}")
                            Text("Type: ${request.type}")
                            //Text("Requested By: ${request.requestedBy}")
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
            val errorMessage = state.message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Dashboard failed to load: $errorMessage")
            }
        }
    }
}
