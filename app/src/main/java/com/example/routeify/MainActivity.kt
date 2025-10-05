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
import com.example.routeify.ui.screens.SplashScreen
import com.example.routeify.ui.screens.MapScreen
import com.example.routeify.ui.screens.ProfileScreen
import com.example.routeify.ui.screens.SettingsScreen
import com.example.routeify.ui.screens.LoginScreen
import com.example.routeify.ui.screens.RegisterScreen
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
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Configure Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            val email = account.email
            val name = account.displayName
            if (email != null) {
                authViewModel.ssoSignIn(email, name)
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google sign-in failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    val isAuthRoute = currentRoute in listOf("login", "register", "splash") && !authState.isAuthenticated

    val content: @Composable () -> Unit = {
        Scaffold(
            topBar = {
                if (!isAuthRoute) {
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
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("splash") {
                    SplashScreen {
                        if (authState.isAuthenticated) {
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
                composable("home") { HomeScreen() }
                composable("profile") { ProfileScreen() }
                composable("map") { MapScreen() }
                composable("settings") { SettingsScreen() }
                composable("google-features") { GoogleFeaturesScreen() }
                composable("favorites") {
                    ScreenPlaceholder("Favorites")
                }
                composable("notifications") {
                    ScreenPlaceholder("Notifications")
                }
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
                composable("register") {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate("home") {
                                popUpTo("register") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate("login")
                        },
                        onGoogleSignInClick = {
                            googleSignInLauncher.launch(googleClient.signInIntent)
                        },
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }

    if (isAuthRoute) {
        content()
    } else {
        AppNavigationDrawer(
            drawerState = drawerState,
            selectedRoute = currentRoute,
            onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onDismiss = {
                scope.launch { drawerState.close() }
            },
            onLogout = {
                authViewModel.logout()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
            username = authState.username,
            email = authState.email
        ) {
            content()
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