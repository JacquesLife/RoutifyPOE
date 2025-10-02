package com.example.routeify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.routeify.ui.components.AppNavigationDrawer
import com.example.routeify.ui.screens.HomeScreen
import com.example.routeify.ui.screens.MapScreen
import com.example.routeify.ui.screens.ProfileScreen
import com.example.routeify.ui.screens.SettingsScreen
import com.example.routeify.ui.theme.RouteifyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RouteifyTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "home"

    AppNavigationDrawer(
        drawerState = drawerState,
        selectedRoute = currentRoute,
        onNavigate = { route ->
            navController.navigate(route) {
                // Pop up to the start destination to avoid building up a large stack
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        onDismiss = {
            scope.launch {
                drawerState.close()
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(getCurrentTitle(currentRoute)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("home") { 
                    HomeScreen(
                        onNavigateToMap = {
                            navController.navigate("map")
                        }
                    )
                }
                composable("map") { MapScreen() }
                composable("profile") { ProfileScreen() }
                composable("settings") { SettingsScreen() }
                composable("favorites") {
                    ScreenPlaceholder("Favorites")
                }
                composable("notifications") {
                    ScreenPlaceholder("Notifications")
                }
            }
        }
    }
}

@Composable
fun ScreenPlaceholder(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = "$title Screen",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

fun getCurrentTitle(route: String): String {
    return when (route) {
        "home" -> "Routeify"
        "map" -> "Transport Map"
        "profile" -> "Profile"
        "settings" -> "Settings"
        "favorites" -> "Favorites"
        "notifications" -> "Notifications"
        else -> "Routeify"
    }
}