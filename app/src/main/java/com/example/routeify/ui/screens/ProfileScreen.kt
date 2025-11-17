/*
 * ============================================================================
 * PROFILE SCREEN - User Account Management Interface
 * ============================================================================
 * 
 * Compose screen for displaying and editing user profile information.
 * Shows account details, recent activity, and profile customization options.
 * 
 * UPDATED: All hardcoded strings replaced with string resources
 *
 * ============================================================================
 */

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routeify.R
import com.example.routeify.ui.viewmodel.AuthViewModel
import com.example.routeify.ui.theme.RouteifyBlue500
import com.example.routeify.ui.theme.RouteifyGreen500

@Composable
fun ProfileScreen(authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.authState.collectAsState()
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
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            // Profile picture and user info
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(80.dp)
                ) {
                    // Placeholder for profile image initials
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Generate initials from username or default to "GU"
                        val initials = (authState.username ?: "").split(" ")
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .joinToString("")
                            .take(2)
                            .ifBlank { stringResource(R.string.profile_guest).take(2).uppercase() }
                        Text(
                            text = initials,
                            color = RouteifyBlue500,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Profile name
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    authState.username ?: stringResource(R.string.profile_guest),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                // Profile email
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    authState.email ?: "",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }

        // Settings sections with saved routes
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(stringResource(R.string.profile_saved_routes))

            RouteCard(
                title = stringResource(R.string.profile_home_to_work),
                subtitle = stringResource(R.string.profile_home_to_work_subtitle),
                leading = Icons.Default.Route
            )
            RouteCard(
                title = stringResource(R.string.profile_university_route),
                subtitle = stringResource(R.string.profile_university_subtitle),
                leading = Icons.Default.Star
            )
            RouteCard(
                title = stringResource(R.string.profile_weekend_shopping),
                subtitle = stringResource(R.string.profile_weekend_subtitle),
                leading = Icons.Default.LocationOn
            )


            SectionTitle(stringResource(R.string.profile_account))
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
                        Text(stringResource(R.string.button_logout), color = MaterialTheme.colorScheme.error)
                    }
                )
            }
        }
    }
}

// Section title composable
@Composable
private fun SectionTitle(text: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(12.dp))
}

// Card for saved routes
@Composable
private fun RouteCard(title: String, subtitle: String, leading: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Route card content
        ListItem(
            leadingContent = {
                Surface(
                    shape = CircleShape,
                    color = RouteifyGreen500.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            leading,
                            contentDescription = null,
                            tint = RouteifyGreen500,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            // Placeholder for route card content
            headlineContent = {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            },
            supportingContent = {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }
}

// Preference switch card
@Composable
private fun PreferenceSwitch(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        // Preference switch card
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with circular background
            Surface(
                shape = CircleShape,
                color = RouteifyBlue500.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                // Icon content
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = RouteifyBlue500,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            // Spacer between icon and text
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Toggle switch
            Switch(
                checked = false,
                onCheckedChange = { },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = RouteifyBlue500,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

// --------------------------------------------------End of File----------------------------------------------------------------