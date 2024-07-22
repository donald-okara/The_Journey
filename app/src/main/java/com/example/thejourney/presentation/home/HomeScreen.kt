package com.example.thejourney.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.CenterAlignedTopAppBar
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
import com.example.thejourney.presentation.sign_in.SignInViewModel
import com.example.thejourney.presentation.sign_in.UserData
import com.example.thejourney.ui.theme.TheJourneyTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    signInViewModel: SignInViewModel = SignInViewModel(),
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                HomeDrawerContent(
                    modifier = Modifier,
                    onNavigateToProfile = { onNavigateToProfile() },
                    onNavigateToAdmin = { onNavigateToAdmin() },
                    signInViewModel = signInViewModel
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
                .fillMaxSize()
                .padding(innerPadding))
        }
    }
}

@Composable
fun HomeDrawerContent(
    modifier: Modifier = Modifier,
    onNavigateToProfile: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    signInViewModel: SignInViewModel
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        val isAdmin by signInViewModel.isAdmin.collectAsState()

        DrawerHeader(
            userData = signInViewModel.getSignedInUser(),
            modifier = modifier.clickable { onNavigateToProfile() }
        )

        HorizontalDivider()
        DrawerItem(
            itemIcon = Icons.Outlined.Groups,
            label = R.string.communities,
            modifier = modifier.clickable { TODO() }
        )

        if (isAdmin){
            DrawerItem(
                itemIcon = Icons.Outlined.Shield,
                label = R.string.admin_panel,
                modifier = modifier.clickable { onNavigateToAdmin() })
        }

    }
}

@Composable
fun DrawerHeader(
    modifier: Modifier = Modifier,
    userData: UserData?
) {
    Column {
        if (userData?.profilePictureUrl != null) {
            AsyncImage(
                model = userData.profilePictureUrl,
                contentDescription = "Profile picture",
                modifier
                    .size(128.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profile picture",
                modifier
                    .size(200.dp)
                    .clip(CircleShape),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
    itemIcon: ImageVector,
    label: Int,
    modifier: Modifier = Modifier
){
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

        Text(
            text = stringResource(id = label),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge, // Use the appropriate text style
            modifier = Modifier.align(Alignment.CenterVertically) // Center the text vertically
        )
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
    Text(text = "Page is still in development", style = MaterialTheme.typography.headlineMedium)
}

@Composable
@Preview
fun PageInDevelopmentPreview(){
    TheJourneyTheme {
        PageInDevelopment()
    }
}

