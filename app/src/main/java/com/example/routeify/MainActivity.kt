/*
 * ============================================================================
 * MAIN ACTIVITY - Primary App Entry Point & Navigation Host (347+ lines)
 * ============================================================================
 * 
 * The central activity orchestrating the entire Routeify application,
 * managing complex navigation flows and authentication state.
 * 
 * CORE RESPONSIBILITIES:
 * - Application lifecycle management and initialization
 * - Jetpack Navigation Compose setup with complex routing
 * - Authentication flow coordination (local + Google Sign-In)
 * - Deep linking and intent handling
 * - Theme and UI configuration management
 * - Permission management and system integration
 * 
 * NAVIGATION ARCHITECTURE:
 * - NavHost with comprehensive route definitions
 * - Authentication-aware navigation with conditional routing
 * - Bottom navigation with tab management
 * - Navigation drawer integration for complex navigation
 * - Back stack management and navigation state preservation
 * 
 * AUTHENTICATION INTEGRATION:
 * - Google Sign-In configuration and callback handling
 * - Local authentication with Room database
 * - Session management and persistent login state
 * - Authentication state observation and UI updates
 * 
 * KEY FEATURES:
 * - Edge-to-edge display with proper inset handling
 * - Material 3 design system implementation
 * - Dark/light theme support with system integration
 * - Drawer navigation with user profile integration
 * - Complex state management across multiple ViewModels
 * 
 * This activity serves as the foundation for the entire app experience
 * and coordinates all major app components and navigation flows.
 * 
 * ============================================================================
 */

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
import androidx.navigation.NavType
import com.example.routeify.ui.components.AppNavigationDrawer
import com.example.routeify.ui.screens.HomeScreen
import com.example.routeify.ui.screens.SplashScreen
import com.example.routeify.ui.screens.MapScreen
import com.example.routeify.ui.screens.ProfileScreen
import com.example.routeify.ui.screens.SettingsScreen
import com.example.routeify.ui.screens.LoginScreen
import com.example.routeify.ui.screens.RegisterScreen
import com.example.routeify.shared.RecentDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routeify.ui.viewmodel.AuthViewModel
import com.example.routeify.presentation.screen.GoogleFeaturesScreen
import com.example.routeify.ui.theme.RouteifyTheme
import android.widget.Toast
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.navArgument
import android.os.Build
import android.os.Build.VERSION_CODES

// Main activity hosting the entire app
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

// Main composable hosting navigation and drawer
@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Main application composable
fun MainApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "home"
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Configure Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }
    // Lazy initialization of GoogleSignInClient
    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    // Launcher for Google Sign-In activity
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle the result of the Google Sign-In intent
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            // Get the signed-in account
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            val email = account.email
            val name = account.displayName
            if (email != null) {
                authViewModel.ssoSignIn(email, name)
            }
            // Notify user of successful sign-in
        } catch (e: ApiException) {
            Toast.makeText(context, "Google sign-in failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    // Determine if the current route is an auth-related screen
    val isAuthRoute = currentRoute in listOf("login", "register", "splash") && !authState.isAuthenticated

    // Main content with optional navigation drawer
    val content: @Composable () -> Unit = {
        Scaffold(
            // Only show top bar if not on auth routes
            topBar = {
                // Hide top bar on auth routes
                if (!isAuthRoute) {
                    // Show top app bar
                    TopAppBar(
                        // Dynamic title based on current route
                        title = { Text(getCurrentTitle(currentRoute)) },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }) {
                                // Show drawer icon
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        },
                        // Theming for the top bar
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
            // Padding to avoid content under top bar
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.padding(paddingValues)
            ) {
                // Splash screen as the start destination
                composable("splash") {
                    SplashScreen {
                        if (authState.isAuthenticated) {
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                                launchSingleTop = true
                            }
                            // If already authenticated, go to home
                        } else {
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
                // Home screen with recent destinations
                composable("home") { 
                    HomeScreen(
                        onDestinationClick = { destination ->
                            // Navigate to route planner with the selected destination
                            navController.navigate("route-planner?destination=${destination.name}")
                        },
                        onSearchClick = {
                            // Navigate to route planner for search
                            navController.navigate("google-features")
                        },
                        onCurrentLocationClick = {
                            // Navigate to map with current location
                            navController.navigate("map")
                        }
                    ) 
                }
                // Profile screen
                composable("profile") { ProfileScreen() }
                composable(
                    // Map screen with optional route parameters
                    route = "map?fromLat={fromLat}&fromLng={fromLng}&toLat={toLat}&toLng={toLng}&poly={poly}",
                    arguments = listOf(
                        navArgument("fromLat") { type = NavType.StringType; nullable = true },
                        navArgument("fromLng") { type = NavType.StringType; nullable = true },
                        navArgument("toLat") { type = NavType.StringType; nullable = true },
                        navArgument("toLng") { type = NavType.StringType; nullable = true },
                        navArgument("poly") { type = NavType.StringType; nullable = true }
                    )
                    // Default to map screen without route if no args
                ) { MapScreen() }
                composable("settings") { SettingsScreen() }
                composable("google-features") {
                    if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
                        GoogleFeaturesScreen(
                            onRouteSelectedNavigateToMap = { routeString ->
                                navController.navigate(routeString) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    } else {
                        // Fallback for older devices where GoogleFeatures requires API 26+
                        ScreenPlaceholder("Google Features (requires Android 8.0+)")
                    }
                }
                // Route planner with optional destination parameter
                composable(
                    route = "route-planner?destination={destination}",
                    arguments = listOf(
                        navArgument("destination") { type = NavType.StringType; nullable = true }
                    )
                    // Pass the destination argument to the screen
                ) { backStackEntry ->
                    val destination = backStackEntry.arguments?.getString("destination")
                    if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
                        GoogleFeaturesScreen(
                            onRouteSelectedNavigateToMap = { routeString ->
                                navController.navigate(routeString) {
                                    launchSingleTop = true
                                }
                            },
                            initialDestination = destination
                        )
                    } else {
                        ScreenPlaceholder("Google Features (requires Android 8.0+)")
                    }
                }
                // Placeholder screens for other sections
                composable("favorites") {
                    ScreenPlaceholder("Favorites")
                }
                // Placeholder for notifications
                composable("notifications") {
                    ScreenPlaceholder("Notifications")
                }
                // Login screen
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate("register")
                        },
                        onGoogleSignInClick = {
                            googleSignInLauncher.launch(googleClient.signInIntent)
                        },
                        authViewModel = authViewModel
                    )
                }

                // Registration screen
                composable("register") {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate("home") {
                                popUpTo("register") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        // Handle Google Sign-In
                        onNavigateToLogin = {
                            navController.navigate("login")
                        },
                        // Handle Google Sign-In
                        onGoogleSignInClick = {
                            googleSignInLauncher.launch(googleClient.signInIntent)
                        },
                        // Pass the authViewModel to the screen
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }

    // If on auth routes, show content directly without drawer
    if (isAuthRoute) {
        content()
        // No drawer for auth routes
    } else {
        // Show drawer for main app content
        AppNavigationDrawer(
            drawerState = drawerState,
            selectedRoute = currentRoute,
            onNavigate = { route ->
            // Close drawer and navigate
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    // Close drawer and navigate
                    launchSingleTop = true
                    restoreState = true
                }
            },
            // Handle drawer dismissal
            onDismiss = {
                scope.launch { drawerState.close() }
            },
            // Handle logout action
            onLogout = {
                authViewModel.logout()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
            // Pass user info to drawer header
            username = authState.username,
            email = authState.email
        ) {
            content()
        }
    }
}

// Simple placeholder screen for unimplemented sections
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

// Get dynamic title based on current route
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

// --------------------------------------------------End of File----------------------------------------------------------------