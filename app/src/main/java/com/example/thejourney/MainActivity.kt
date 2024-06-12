package com.example.thejourney

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thejourney.model.TabBarItem
import com.example.thejourney.ui.screens.ChatScreen
import com.example.thejourney.ui.screens.EventScreen
import com.example.thejourney.ui.screens.HomeScreen
import com.example.thejourney.ui.theme.TheJourneyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            // setting up the individual tabs
            val homeTab = TabBarItem(title = "Home", selectedIcon = Icons.Filled.Home, unselectedIcon = Icons.Outlined.Home)
            val eventTab = TabBarItem(title = "Events", selectedIcon = Icons.Filled.DateRange, unselectedIcon = Icons.Outlined.DateRange, badgeAmount = 7)
            val chatTab = TabBarItem(title = "Chat", selectedIcon = Icons.Filled.Send, unselectedIcon = Icons.Outlined.Send)

            // setting up the list of tabs
            val tabBarItems = listOf(homeTab, eventTab, chatTab)

            // creating navController
            val navController = rememberNavController()


            TheJourneyTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize()) {
                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent =
                        {
                            Surface(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(280.dp)
                            ) {
                                DrawerContents(
                                    onItemClick = {
                                            item ->
                                        scope.launch { drawerState.close() }
                                        navController.navigate(item)
                                    }
                                )
                            }

                        }
                    ) {


                    Scaffold(
                    topBar = {
                        JourneyTopAppBar(
                            onNavigationIconClick = {scope.launch { drawerState.open() }}

                    )
                             },

                        bottomBar = {
                                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                                    TabView(tabBarItems, navController)
                                }
                                    } ,
                    modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.padding(top = it.calculateTopPadding())) {
                    NavHost(navController = navController, startDestination = homeTab.title) {
                        composable(homeTab.title){
                            HomeScreen()
                        }
                        composable(eventTab.title){
                            EventScreen()
                        }
                        composable(chatTab.title){
                            ChatScreen()
                        }

                    }
                }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyTopAppBar(
    onNavigationIconClick : () -> Unit,
    modifier: Modifier = Modifier
){
    TopAppBar(title = {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.app_name),
                modifier = Modifier.align(Alignment.CenterVertically)

            )
        }
    },
        navigationIcon = {
            IconButton(onClick =  onNavigationIconClick ) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        modifier = modifier
    )

}
// ----------------------------------------
// This is a wrapper view that allows us to easily and cleanly
// reuse this component in any future project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContents(
    headerTitle : String = "The Journey",
    onItemClick: (String) -> Unit)
{
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {

    }
    Column(
        modifier = Modifier.padding(16.dp)) {
        Image(
            painter = painterResource(id = R.drawable.icon_frame),
            contentDescription = "Logo",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = headerTitle,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(text = "Menu", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        DrawerItem("Home", onItemClick)
        DrawerItem("Events", onItemClick)
        DrawerItem("Chat", onItemClick)
    }
}

@Composable
fun DrawerItem(title: String, onClick: (String) -> Unit) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(title) }
            .padding(vertical = 8.dp)
    )
}


@Composable
fun TabView(tabBarItems: List<TabBarItem>, navController: NavController, modifier: Modifier = Modifier) {
    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .background(color = Color.DarkGray, shape = RoundedCornerShape(64.dp))
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(64.dp))
    ){
        NavigationBar {
            tabBarItems.forEachIndexed { index, tabBarItem ->
                NavigationBarItem(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        navController.navigate(tabBarItem.title)
                    },
                    icon = {
                        TabBarIconView(
                            isSelected = selectedTabIndex == index,
                            selectedIcon = tabBarItem.selectedIcon,
                            unselectedIcon = tabBarItem.unselectedIcon,
                            title = tabBarItem.title,
                            badgeAmount = tabBarItem.badgeAmount
                        )
                    },
                    label = {Text(tabBarItem.title)})
            }
        }

    }
    // looping over each tab to generate the views and navigation for each item

}


// This component helps to clean up the API call from our TabView above,
// but could just as easily be added inside the TabView without creating this custom component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String,
    badgeAmount: Int? = null
) {
    BadgedBox(badge = { TabBarBadgeView(badgeAmount) }) {
        Icon(
            imageVector = if (isSelected) {selectedIcon} else {unselectedIcon},
            contentDescription = title
        )
    }
}

// This component helps to clean up the API call from our TabBarIconView above,
// but could just as easily be added inside the TabBarIconView without creating this custom component
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TabBarBadgeView(count: Int? = null) {
    if (count != null) {
        Badge {
            Text(count.toString())
        }
    }
}
// end of the reusable components that can be copied over to any new projects
// ----------------------------------------

// This was added to demonstrate that we are in fact changing views when we click a new tab
@Composable
fun MoreView() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Thing 1")
        Text("Thing 2")
        Text("Thing 3")
        Text("Thing 4")
        Text("Thing 5")
    }
}

@Preview(showBackground = true)
@Composable
fun TheJourneyPreview() {
    TheJourneyTheme {
        JourneyTopAppBar({})
    }
}
