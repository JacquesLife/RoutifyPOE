package com.example.routeify.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.routeify.screens.HomeScreen
import com.example.routeify.screens.LiveMapScreen
import com.example.routeify.screens.LiveNavigationScreen
import com.example.routeify.screens.ProfileScreen
import com.example.routeify.screens.RouteDetailsScreen
import com.example.routeify.screens.RouteOptionsScreen
import com.example.routeify.screens.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object RouteOptions : Screen("route_options")
    object RouteDetails : Screen("route_details")
    object Profile : Screen("profile")
    object Navigation : Screen("navigation")
    object Map : Screen("map")
}

@Composable
fun RouteifyNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToRouteOptions = {
                    navController.navigate(Screen.RouteOptions.route)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        
        composable(Screen.Map.route) {
            LiveMapScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route)
                },
                onNavigateToRouteOptions = {
                    navController.navigate(Screen.RouteOptions.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        
        composable(Screen.Navigation.route) {
            LiveNavigationScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Navigation.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route)
                },
                onNavigateToRouteOptions = {
                    navController.navigate(Screen.RouteOptions.route)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                }
            )
        }
        
        composable(Screen.RouteOptions.route) {
            RouteOptionsScreen(
                onRouteSelect = { route ->
                    navController.navigate(Screen.RouteDetails.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.RouteDetails.route) {
            RouteDetailsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartNavigation = {
                    navController.navigate(Screen.Navigation.route)
                }
            )
        }
        
        // TODO: Add other screens as we implement them
    }
}
