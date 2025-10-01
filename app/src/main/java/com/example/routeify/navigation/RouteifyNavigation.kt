package com.example.routeify.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.routeify.screens.HomeScreen
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
                }
            )
        }
        
        // TODO: Add other screens as we implement them
    }
}
