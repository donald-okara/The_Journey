package com.example.thejourney.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.material3.Badge
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.thejourney.R
import com.example.thejourney.domain.UserRepository
import com.example.thejourney.presentation.admin.AdminViewModel
import com.example.thejourney.data.model.UserData
import com.example.thejourney.ui.theme.TheJourneyTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToProfile: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    userRepository: UserRepository,
    adminViewModel: AdminViewModel,
    navigateToCommunities: () -> Unit,
) {
    val pendingCount by adminViewModel.pendingCount
    val userData = userRepository.getCurrentUser()
    val isAdmin by adminViewModel.isAdmin.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    if (userData != null) {
        LaunchedEffect(userData.userId) {
            scope.launch {
                adminViewModel.fetchAdminStatus()
                adminViewModel.fetchPendingRequests()
                adminViewModel.fetchLiveCommunities()
                adminViewModel.fetchRejectedCommunities()
            }
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                HomeDrawerContent(
                    modifier = Modifier,
                    onNavigateToProfile = { onNavigateToProfile() },
                    onNavigateToAdmin = { onNavigateToAdmin() },
                    navigateToCommunities = navigateToCommunities,
                    pendingCount = pendingCount,
                    isAdmin = isAdmin,
                    drawerState = drawerState,
                    userData = userData
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),

            topBar = {
                HomeTopBar(
                    scrollBehavior = scrollBehavior,
                    onShowDrawer = { scope.launch { drawerState.open() } }
                )
            }
        ) {innerPadding->
            PageInDevelopment(modifier = modifier
                .padding(innerPadding))
        }
    }
}

@Composable
fun HomeDrawerContent(
    userData: UserData?,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false,
    onNavigateToProfile: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    navigateToCommunities: () -> Unit,
    drawerState: DrawerState,
    pendingCount : Int
){
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        DrawerHeader(
            userData = userData,
            modifier = modifier.clickable { onNavigateToProfile() }
        )

        Spacer(modifier = modifier.height(16.dp))

        HorizontalDivider()

        Spacer(modifier = modifier.height(16.dp))

        DrawerItem(
            itemIcon = Icons.Outlined.Workspaces,
            label = R.string.communities,
            modifier = modifier.clickable {
                coroutineScope.launch {
                    navigateToCommunities()
                    drawerState.close()
                }
            }
        )

        if (isAdmin){
            DrawerItem(
                itemIcon = Icons.Outlined.Shield,
                label = R.string.admin_panel,
                badgeCount = pendingCount,
                modifier = modifier.clickable {
                    coroutineScope.launch {
                        drawerState.close()
                        onNavigateToAdmin()
                    }
                }
            )
        }

    }
}

@Composable
fun DrawerHeader(
    modifier: Modifier = Modifier,
    userData: UserData?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        if (userData?.profilePictureUrl != null) {
            AsyncImage(
                model = userData.profilePictureUrl,
                contentDescription = "Profile picture",
                modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profile picture",
                modifier
                    .size(64.dp)
                    .clip(CircleShape),
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        if (userData?.username != null) {
            Text(
                text = userData.username!!,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

}


@Composable
fun DrawerItem(
    modifier: Modifier = Modifier,
    itemIcon: ImageVector,
    label: Int,
    badgeCount: Int? = null,

) {
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = itemIcon,
            contentDescription = stringResource(id = label),
            modifier.size(24.dp) // Size the icon appropriately
        )

        Spacer(modifier = Modifier.width(16.dp)) // Adjust space between the icon and text

        // Box for text and badge
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = label),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.weight(1f))
            // Add badge only if badgeCount is not null and greater than 0
            badgeCount?.takeIf { it > 0 }?.let {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp) // Padding to avoid overlap
                ) {
                    Badge(
                        content = { Text(it.toString()) },
                        modifier = Modifier.size(24.dp) // Fixed badge size
                    )
                }
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    modifier: Modifier = Modifier,
    scrollBehavior : TopAppBarScrollBehavior,
    onShowDrawer : () -> Unit
){
    CenterAlignedTopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier
                    .clickable { TODO() }
                    .fillMaxHeight()
                )
        },
        navigationIcon = {
            IconButton(onClick = { onShowDrawer() }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Open drawer"
                )
            }
        },
        actions = {
            IconButton(onClick = { /* do something */ }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search"
                )
            }
        },
        scrollBehavior = scrollBehavior,

        )
}

@Composable
fun PageInDevelopment(modifier: Modifier = Modifier){
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Page is still in development",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
@Preview
fun PageInDevelopmentPreview(){
    TheJourneyTheme {
        PageInDevelopment()
    }
}

