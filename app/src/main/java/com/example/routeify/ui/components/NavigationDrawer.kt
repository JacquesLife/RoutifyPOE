package com.example.routeify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routeify.ui.theme.RouteifyBlue500
import com.example.routeify.ui.theme.RouteifyGreen500

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationDrawer(
    drawerState: DrawerState,
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    username: String?,
    email: String?,
    content: @Composable () -> Unit
) {
    val items = listOf(
        NavigationItem("Home", Icons.Default.Home, "home"),
        NavigationItem("Map", Icons.Default.Map, "map"),
        NavigationItem("Google Features", Icons.Default.Api, "google-features"),
        NavigationItem("Profile", Icons.Default.Person, "profile"),
        NavigationItem("Settings", Icons.Default.Settings, "settings"),
        NavigationItem("Favorites", Icons.Default.Favorite, "favorites"),
        NavigationItem("Notifications", Icons.Default.Notifications, "notifications")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Drawer Header
                DrawerHeader(username = username, email = email)

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Navigation Items
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontWeight = if (selectedRoute == item.route) FontWeight.SemiBold else FontWeight.Normal) },
                        selected = selectedRoute == item.route,
                        onClick = {
                            onNavigate(item.route)
                            onDismiss()
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = RouteifyBlue500.copy(alpha = 0.15f),
                            selectedIconColor = RouteifyBlue500,
                            selectedTextColor = RouteifyBlue500
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer items
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "About") },
                    label = { Text("About") },
                    selected = false,
                    onClick = { /* Handle about */ },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        onLogout()
                        onDismiss()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        content = content
    )
}

@Composable
private fun DrawerHeader(username: String?, email: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(RouteifyBlue500, RouteifyGreen500)))
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initials = (username ?: "").split(" ")
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .joinToString("")
                        .take(2)
                        .ifBlank { "GU" }
                    Text(
                        text = initials,
                        color = RouteifyBlue500,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = username ?: "Guest",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}