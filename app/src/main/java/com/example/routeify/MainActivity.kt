package com.example.routeify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routeify.ui.viewmodel.AuthViewModel
import com.example.routeify.presentation.screen.GoogleFeaturesScreen
import com.example.routeify.ui.theme.RouteifyTheme
import android.widget.Toast
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.navArgument
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.activity.ComponentActivity
import com.example.routeify.data.preferences.LanguageManager

// Main activity hosting the entire app - extends FragmentActivity for biometric support
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved language preference
        val languageManager = LanguageManager.getInstance(this)
        val savedLanguage = languageManager.getSavedLanguage()
        languageManager.updateLocale(savedLanguage)

        enableEdgeToEdge()
        setContent {
            val authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val authState by authViewModel.authState.collectAsState()
            
            RouteifyTheme(darkTheme = authState.darkModeEnabled) {
                MainApp()
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        // Apply language to base context
        val languageManager = LanguageManager.getInstance(newBase)
        val savedLanguage = languageManager.getSavedLanguage()

        val locale = java.util.Locale(savedLanguage)
        java.util.Locale.setDefault(locale)

        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    companion object {
        fun restart(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
            if (context is ComponentActivity) {
                context.finish()
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

    // Language change monitoring - restart activity when language changes
    val languageManager = remember { LanguageManager.getInstance(context) }
    val languageChangeTrigger = languageManager.languageChangeTrigger

    // Track the previous trigger value to detect actual changes
    var previousTrigger by remember { mutableIntStateOf(languageChangeTrigger) }

    LaunchedEffect(languageChangeTrigger) {
        // Only restart if the trigger actually changed
        if (languageChangeTrigger != previousTrigger && previousTrigger > 0) {
            MainActivity.restart(context)
        }
        previousTrigger = languageChangeTrigger
    }

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
            Toast.makeText(
                context,
                "Google sign-in failed: ${e.statusCode}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val isAuthRoute = currentRoute in listOf("login", "register", "splash") &&
            !authState.isAuthenticated

    val content: @Composable () -> Unit = {
        Scaffold(
            topBar = {
                if (!isAuthRoute) {
                    // Check if we should show back button:
                    // 1. Map with single location (from recent destinations)
                    // 2. Map with route (from/to destinations)
                    // 3. Google-features/route-planner screens
                    val hasRouteParams = currentBackStackEntry?.arguments?.getString("fromLat") != null ||
                                        currentBackStackEntry?.arguments?.getString("toLat") != null
                    val hasSingleLocation = currentBackStackEntry?.arguments?.getString("lat") != null
                    
                    val showBackButton = (currentRoute.startsWith("map") && (hasSingleLocation || hasRouteParams)) ||
                                        currentRoute.startsWith("google-features") ||
                                        currentRoute.startsWith("route-planner")
                    
                    TopAppBar(
                        title = { Text(getCurrentTitle(currentRoute, context)) },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (showBackButton) {
                                    // For google-features/route-planner, navigate to home
                                    // For map views, go back to previous screen
                                    if (currentRoute.startsWith("google-features") || 
                                        currentRoute.startsWith("route-planner")) {
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        // Map with route or single location - go back
                                        navController.popBackStack()
                                    }
                                } else {
                                    // Open drawer
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = if (showBackButton) 
                                        Icons.AutoMirrored.Filled.ArrowBack 
                                    else 
                                        Icons.Default.Menu,
                                    contentDescription = if (showBackButton)
                                        stringResource(R.string.back)
                                    else
                                        stringResource(R.string.content_description_menu)
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
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = if (authState.isAuthenticated) "home" else "splash",
                modifier = Modifier.padding(padding)
            ) {
                composable("splash") {
                    SplashScreen {
                        navController.navigate(if (authState.isAuthenticated) "home" else "login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }

                composable("home") {
                    HomeScreen(
                        onRouteClick = { route ->
                            // Navigate to map showing the full route
                            val encodedPoly = route.polyline?.let { 
                                java.net.URLEncoder.encode(it, "UTF-8") 
                            }
                            val encodedFromName = java.net.URLEncoder.encode(route.fromName, "UTF-8")
                            val encodedToName = java.net.URLEncoder.encode(route.toName, "UTF-8")
                            navController.navigate(
                                buildString {
                                    append("map?")
                                    append("fromLat=${route.fromLat}&")
                                    append("fromLng=${route.fromLng}&")
                                    append("toLat=${route.toLat}&")
                                    append("toLng=${route.toLng}&")
                                    append("fromName=$encodedFromName&")
                                    append("toName=$encodedToName")
                                    if (encodedPoly != null) {
                                        append("&poly=$encodedPoly")
                                    }
                                }
                            )
                        }
                    )
                }

                composable("profile") { ProfileScreen() }

                composable(
                    route = "map?fromLat={fromLat}&fromLng={fromLng}&toLat={toLat}&toLng={toLng}&poly={poly}&lat={lat}&lng={lng}&name={name}&address={address}&fromName={fromName}&toName={toName}",
                    arguments = listOf(
                        navArgument("fromLat") { type = NavType.StringType; nullable = true },
                        navArgument("fromLng") { type = NavType.StringType; nullable = true },
                        navArgument("toLat") { type = NavType.StringType; nullable = true },
                        navArgument("toLng") { type = NavType.StringType; nullable = true },
                        navArgument("poly") { type = NavType.StringType; nullable = true },
                        navArgument("lat") { type = NavType.StringType; nullable = true },
                        navArgument("lng") { type = NavType.StringType; nullable = true },
                        navArgument("name") { type = NavType.StringType; nullable = true },
                        navArgument("address") { type = NavType.StringType; nullable = true },
                        navArgument("fromName") { type = NavType.StringType; nullable = true },
                        navArgument("toName") { type = NavType.StringType; nullable = true }
                    )
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
                        ScreenPlaceholder(stringResource(R.string.error_generic))
                    }
                }

                composable(
                    route = "route-planner?destination={destination}",
                    arguments = listOf(
                        navArgument("destination") { type = NavType.StringType; nullable = true }
                    )
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
                        ScreenPlaceholder(stringResource(R.string.error_generic))
                    }
                }

                composable("favorites") {
                    ScreenPlaceholder(stringResource(R.string.title_favorites))
                }

                composable("notifications") {
                    ScreenPlaceholder(stringResource(R.string.title_notifications))
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

// Updated to use string resources
fun getCurrentTitle(route: String, context: Context): String {
    return when (route) {
        "home" -> context.getString(R.string.title_home)
        "map" -> context.getString(R.string.title_map)
        "profile" -> context.getString(R.string.title_profile)
        "settings" -> context.getString(R.string.title_settings)
        "favorites" -> context.getString(R.string.title_favorites)
        "notifications" -> context.getString(R.string.title_notifications)
        else -> context.getString(R.string.title_home)
    }
}