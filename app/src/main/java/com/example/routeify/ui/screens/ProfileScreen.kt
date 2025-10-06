package com.example.routeify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routeify.ui.theme.RouteifyBlue500
import com.example.routeify.ui.theme.RouteifyGreen500

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Gradient header with profile summary
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(colors = listOf(RouteifyBlue500, RouteifyGreen500))
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.25f)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JD",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("RudolphRedNose", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("North.pole@gmail.com", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle("Saved Routes")

            RouteCard(
                title = "Home to Work",
                subtitle = "Rondebosch to Claremont",
                leading = Icons.Default.Route
            )
            RouteCard(
                title = "University Route",
                subtitle = "UCT Main Campus",
                leading = Icons.Default.Star
            )
            RouteCard(
                title = "Weekend Shopping",
                subtitle = "V&A Waterfront",
                leading = Icons.Default.LocationOn
            )

            SectionTitle("Preferences")

            PreferenceSwitch(
                title = "Push Notifications",
                subtitle = "Get alerts for delays and updates",
                icon = Icons.Default.Notifications
            )
            PreferenceSwitch(
                title = "Dark Mode",
                subtitle = "Switch to dark theme",
                icon = Icons.Default.DarkMode
            )
            PreferenceSwitch(
                title = "Accessibility",
                subtitle = "High contrast and large text",
                icon = Icons.Default.Star
            )

            SectionTitle("Account")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    leadingContent = {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    },
                    headlineContent = {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = text, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun RouteCard(title: String, subtitle: String, leading: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        ListItem(
            leadingContent = {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        Icon(leading, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            },
            headlineContent = { Text(title) },
            supportingContent = { Text(subtitle) },
            trailingContent = {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        )
    }
}

@Composable
private fun PreferenceSwitch(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = false, onCheckedChange = { })
        }
    }
}