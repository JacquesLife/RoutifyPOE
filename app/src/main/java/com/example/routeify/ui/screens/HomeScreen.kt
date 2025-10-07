/*
 * ============================================================================
 * HOME SCREEN - Main Dashboard Interface
 * ============================================================================
 * 
 * Primary Compose screen with app navigation and feature overview.
 * Provides quick access to core features and recent destinations.
 * 
 * ============================================================================
 */

package com.example.routeify.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.routeify.ui.theme.RouteifyBlue500
import com.example.routeify.ui.theme.RouteifyGreen500
import com.example.routeify.shared.RecentDestinationsStore
import com.example.routeify.shared.RecentDestination

@Composable
fun HomeScreen(
    onDestinationClick: (RecentDestination) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with gradient, app name, search, and location shortcut
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(RouteifyBlue500, RouteifyGreen500)))
                .padding(24.dp)
        ) {
            /// App Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.NearMe,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                // App icon
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Routeify",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

        }

        // Recent destinations section
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Section title and subtitle
            Text(
                "Recent Destinations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Section subtitle
            Text(
                "Your frequently visited places",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic list of recent destinations
            val recentDestinations by RecentDestinationsStore.recentDestinations.collectAsState()
            
            if (recentDestinations.isEmpty()) {
                // Show empty state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Empty state icon and text
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Empty state text
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No recent destinations",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Empty state description
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Start planning routes to see your recent destinations here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // List of recent destinations
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(recentDestinations) { destination ->
                        DestinationCard(
                            destination = destination,
                            onClick = { onDestinationClick(destination) }
                        )
                    }
                }
            }
        }
    }
}

// Card for individual recent destination
@Composable
private fun DestinationCard(
    destination: RecentDestination,
    onClick: () -> Unit
) {
    // Get appropriate icon based on destination type
    val icon = RecentDestinationsStore.getIconForType(destination.iconType)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // List item layout
        ListItem(
            leadingContent = {
                Surface(
                    shape = CircleShape,
                    color = RouteifyBlue500.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = RouteifyBlue500,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            // Destination name, address, and visit count
            headlineContent = {
                Text(
                    destination.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            },
            // Address and visit count
            supportingContent = {
                Column {
                    Text(
                        destination.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Show visit count if more than once
                    if (destination.visitCount > 1) {
                        Text(
                            "Visited ${destination.visitCount} times",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            // Trailing chevron icon
            trailingContent = {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Navigate to ${destination.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }
}

// --------------------------------------------------End of File----------------------------------------------------------------