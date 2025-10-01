package com.example.routeify.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routeify.ui.theme.*

data class SavedRoute(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToRouteOptions: () -> Unit = {},
    onNavigateToMap: () -> Unit = {}
) {
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var accessibilityEnabled by remember { mutableStateOf(false) }
    
    val savedRoutes = remember {
        listOf(
            SavedRoute("1", "Home to Work", "Rondebosch to Claremont", Icons.Default.Work),
            SavedRoute("2", "University Route", "UCT Main Campus", Icons.Default.School),
            SavedRoute("3", "Weekend Shopping", "V&A Waterfront", Icons.Default.ShoppingCart)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)) // bg-gray-50
    ) {
        // Header with gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            GradientStart,
                            GradientEnd
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 32.dp, )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Card(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JD",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RouteifyBlue
                        )
                    }
                }
                
                // User Info
                Column {
                    Text(
                        text = "RudolphRedNose",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "North.pole@gmail.com",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
        
        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Saved Routes Section
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Saved Routes",
                            tint = Color(0xFFEF4444), // text-red-500
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Saved Routes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        savedRoutes.forEach { route ->
                            SavedRouteCard(
                                route = route,
                                onClick = { /* Handle route selection */ }
                            )
                        }
                    }
                }
            }
            
            // Preferences Section
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Preferences",
                            tint = Color(0xFF6B7280), // text-gray-600
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Preferences",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Push Notifications
                            PreferenceItem(
                                icon = Icons.Default.Notifications,
                                title = "Push Notifications",
                                description = "Get alerts for delays and updates",
                                isChecked = pushNotificationsEnabled,
                                onCheckedChange = { pushNotificationsEnabled = it }
                            )
                            
                            Divider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = Color(0xFFE5E7EB) // border-gray-200
                            )
                            
                            // Dark Mode
                            PreferenceItem(
                                icon = Icons.Default.DarkMode,
                                title = "Dark Mode",
                                description = "Switch to dark theme",
                                isChecked = darkModeEnabled,
                                onCheckedChange = { darkModeEnabled = it }
                            )
                            
                            Divider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = Color(0xFFE5E7EB) // border-gray-200
                            )
                            
                            // Accessibility
                            PreferenceItem(
                                icon = Icons.Default.Accessibility,
                                title = "Accessibility",
                                description = "High contrast and large text",
                                isChecked = accessibilityEnabled,
                                onCheckedChange = { accessibilityEnabled = it }
                            )
                        }
                    }
                }
            }
            
            // Account Section
            item {
                Column {
                    Text(
                        text = "Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Handle logout */ }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = Color(0xFFDC2626), // text-red-600
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Logout",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFDC2626) // text-red-600
                            )
                        }
                    }
                }
            }
            
            // Add some bottom padding for the navigation bar
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        
        // Bottom Navigation
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
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = false,
                    onClick = onNavigateToHome
                )
                BottomNavItem(
                    icon = Icons.Default.Route,
                    label = "Routes",
                    isSelected = false,
                    onClick = onNavigateToRouteOptions
                )
                BottomNavItem(
                    icon = Icons.Default.Map,
                    label = "Map",
                    isSelected = false,
                    onClick = onNavigateToMap
                )
                BottomNavItem(
                    icon = Icons.Default.Person,
                    label = "Profile",
                    isSelected = true,
                    onClick = { /* Already on profile */ }
                )
            }
        }
    }
}

@Composable
fun SavedRouteCard(
    route: SavedRoute,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RouteifyBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = route.icon,
                    contentDescription = route.name,
                    tint = RouteifyBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = route.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = route.description,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun PreferenceItem(
    icon: ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF6B7280), // text-gray-600
                modifier = Modifier.size(20.dp)
            )
            
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = RouteifyBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBCED4) // switch-background
            )
        )
    }
}

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

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    RouteifyTheme {
        ProfileScreen()
    }
}
