/*
 * ============================================================================
 * NEARBY TRANSIT SCREEN - Public Transportation Discovery
 * ============================================================================
 * 
 * Compose screen for finding and displaying nearby transit stops.
 * Shows bus stops, train stations, and other public transport options.
 * 
 * ============================================================================
 */

package com.example.routeify.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routeify.data.model.TransitStop
import com.example.routeify.presentation.viewmodel.GoogleFeaturesViewModel

// Screen to search and display nearby transit stops
@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Composable function for the Nearby Transit screen
fun NearbyTransitScreen(
    viewModel: GoogleFeaturesViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onStopClick: (TransitStop) -> Unit = {}
) {
    // Observe ViewModel state
    val searchResults by viewModel.searchResults
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    
    // Local UI state
    var searchRadius by remember { mutableIntStateOf(1000) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            // Title
            Text(
                text = "🚌 Nearby Transit",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Controls
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    // Search radius header with expand/collapse
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        // Search radius label
                        text = "Search Radius: ${searchRadius}m",
                        style = MaterialTheme.typography.titleMedium
                    )
                    // Expand/collapse button
                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded }
                    ) {
                        Icon(
                            if (isSearchExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand search options"
                        )
                    }
                }
                // Expanded search options
                if (isSearchExpanded) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Radius Slider
                    Text(
                        text = "Search within ${searchRadius}m",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    // Radius Slider
                    Slider(
                        value = searchRadius.toFloat(),
                        onValueChange = { searchRadius = it.toInt() },
                        valueRange = 200f..5000f,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Quick radius buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quick select buttons
                        listOf(500, 1000, 2000, 5000).forEach { radius ->
                            FilterChip(
                                onClick = { searchRadius = radius },
                                label = { Text("${radius}m") },
                                selected = searchRadius == radius,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Button
                Button(
                    onClick = {
                        // Search for nearby transit using selected radius
                        viewModel.searchTransitPlaces(
                            query = "transit station bus stop",
                            radiusMeters = searchRadius
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        Row(
                            // Loading state
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text("Searching...")
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Search icon
                            Icon(Icons.Default.Search, contentDescription = null)
                            Text("Find Nearby Transit")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error Message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    // Error message content
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        // Error icon
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        // Error text
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Results
        if (searchResults.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    // Results header
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        // Results header
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            // Location icon
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            // Results count
                            text = "Found ${searchResults.size} transit stops",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Results description
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        // Instructional text
                        text = "Tap on any stop to see details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                // List of transit stops
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults) { stop ->
                    TransitStopCard(
                        stop = stop,
                        onClick = { onStopClick(stop) }
                    )
                }
            }
        } else if (!isLoading && errorMessage == null) {
            // Empty state
            Card(
                // Empty state card
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    // Empty state content
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        // Bus icon
                        Icons.Default.DirectionsBus,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        // No results text
                        text = "No results yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        // Prompt to search
                        text = "Search for nearby transit stops to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Card to display individual transit stop info
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitStopCard(
    stop: TransitStop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Clickable transit stop
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            // Stop info row
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                // Stop icon
                Icon(
                    Icons.Default.DirectionsBus,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Stop Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stop.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                stop.vicinity?.let { vicinity ->
                    Text(
                        text = vicinity,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Rating if available
                stop.rating?.let { rating ->
                    if (rating > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$rating",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Distance/Actions
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --------------------------------------------------End of File----------------------------------------------------------------