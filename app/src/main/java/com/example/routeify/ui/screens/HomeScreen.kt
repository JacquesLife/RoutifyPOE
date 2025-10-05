package com.example.routeify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.routeify.ui.theme.RouteifyBlue500
import com.example.routeify.ui.theme.RouteifyGreen500

@Composable
fun HomeScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with gradient, app name, search, and location shortcut
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(RouteifyBlue500, RouteifyGreen500)))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NearMe, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Routeify", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Where to?") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                FilledIconButton(onClick = { /* current location */ }) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Recent Destinations", style = MaterialTheme.typography.titleMedium)

            DestinationCard("Claremont Station", "Train Station")
            DestinationCard("V&A Waterfront", "Shopping Centre")
            DestinationCard("University of Cape Town", "University")
            DestinationCard("Cape Town International Airport", "Airport")
            DestinationCard("Table Mountain", "Tourist Attraction")
        }
    }
}

@Composable
private fun DestinationCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        ListItem(
            leadingContent = {
                FilledIconButton(onClick = {}, enabled = false) {
                    Icon(Icons.Default.Place, contentDescription = null)
                }
            },
            headlineContent = { Text(title) },
            supportingContent = { Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        )
    }
}