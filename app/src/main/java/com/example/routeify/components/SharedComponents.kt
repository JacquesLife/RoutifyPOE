package com.example.routeify.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routeify.ui.theme.*

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) RouteifyBlue else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) RouteifyBlue else TextSecondary
        )
    }
}

@Composable
fun BottomNavigationBar(
    onNavigateToHome: () -> Unit = {},
    onNavigateToRouteOptions: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    currentScreen: String = "home"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem(
                icon = androidx.compose.material.icons.Icons.Default.Home,
                label = "Home",
                isSelected = currentScreen == "home",
                onClick = onNavigateToHome
            )
            BottomNavItem(
                icon = androidx.compose.material.icons.Icons.Default.Route,
                label = "Routes",
                isSelected = currentScreen == "routes",
                onClick = onNavigateToRouteOptions
            )
            BottomNavItem(
                icon = androidx.compose.material.icons.Icons.Default.Map,
                label = "Map",
                isSelected = currentScreen == "map",
                onClick = onNavigateToMap
            )
            BottomNavItem(
                icon = androidx.compose.material.icons.Icons.Default.Person,
                label = "Profile",
                isSelected = currentScreen == "profile",
                onClick = onNavigateToProfile
            )
        }
    }
}
