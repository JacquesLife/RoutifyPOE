package com.example.routeify.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.routeify.R

@Composable
fun TransportLegend(
    modifier: Modifier = Modifier,
    busStationCount: Int = 0,        // Regular bus stops (transit_station type)
    trainStationCount: Int = 0,      // Train/subway/light rail stations
    transitHubCount: Int = 0,        // Major bus terminals (bus_station type)
    currentZoom: Float = 11f,
    isLoading: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Map Legend",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse legend" else "Expand legend",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Legend content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = "Loading transport data...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        // Bus Stops
                        LegendItem(
                            iconRes = R.drawable.bus_stop,
                            label = "Bus Stops",
                            count = busStationCount,
                            description = "Regular bus stops"
                        )

                        // Train Station
                        LegendItem(
                            iconRes = R.drawable.train_station,
                            label = "Train Stations",
                            count = trainStationCount,
                            description = "Railway & metro stops"
                        )

                        // Bus Stations (Transit Hubs)
                        LegendItem(
                            iconRes = R.drawable.bus_station,
                            label = "Bus Stations",
                            count = transitHubCount,
                            description = "Major bus terminals"
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )

                        // Current zoom info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🔍 Zoom:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = String.format("%.1f", currentZoom),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Helper text
                        Text(
                            text = "💡 Tap clustered markers to zoom in and view individual stops",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    iconRes: Int,
    label: String,
    count: Int,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon
        Surface(
            modifier = Modifier.size(40.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.padding(8.dp),
                tint = androidx.compose.ui.graphics.Color.Unspecified
            )
        }

        // Text content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (count > 0) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = count.toString(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}