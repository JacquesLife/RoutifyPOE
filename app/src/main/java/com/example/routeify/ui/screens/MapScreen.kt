package com.example.routeify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routeify.data.model.BusStopManager
import com.example.routeify.data.model.StopType
import com.example.routeify.ui.viewmodel.MapViewModel
import com.example.routeify.utils.MapIconUtils
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val viewModel: MapViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cape Town Transport Map",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (uiState.isLoading) "Loading real MyCiTi data..." else "Live MyCiTi bus stops",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Smart Google Map with real Cape Town data
        val capeTabCity = LatLng(-33.9249, 18.4241) // Cape Town coordinates
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(capeTabCity, 11f) // Start zoomed out
        }

        // Track current zoom level and load real data accordingly
        val currentZoom by remember { derivedStateOf { cameraPositionState.position.zoom } }
        
        // Load data when zoom level changes
        LaunchedEffect(currentZoom) {
            viewModel.loadBusStopsForZoom(currentZoom)
        }

        // Show error message if any
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            cameraPositionState = cameraPositionState
        ) {
            // Add markers for real Cape Town bus stops
            uiState.busStops.forEach { busStop ->
                val markerIcon = MapIconUtils.getTransportIcon(context, busStop.stopType)
                
                Marker(
                    state = MarkerState(
                        position = LatLng(busStop.latitude, busStop.longitude)
                    ),
                    title = busStop.name,
                    snippet = buildString {
                        if (busStop.area != null) appendLine("Area: ${busStop.area}")
                        if (busStop.routes.isNotEmpty()) {
                            appendLine("Routes: ${busStop.routes.joinToString(", ")}")
                        }
                        if (busStop.direction != null) appendLine("Direction: ${busStop.direction}")
                        if (busStop.description != null) appendLine("Type: ${busStop.description}")
                        appendLine("Status: ${busStop.status}")
                        when (busStop.stopType) {
                            StopType.MAJOR_HUB -> appendLine("🚇 Major Transport Hub")
                            StopType.RAILWAY -> appendLine("🚊 Railway Station") 
                            StopType.REGULAR -> appendLine("🚌 Bus Stop")
                        }
                    },
                    icon = markerIcon
                )
            }
        }

        // Bottom info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Real MyCiTi Data",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildString {
                        if (uiState.isLoading) {
                            appendLine("⏳ Loading official Cape Town data...")
                        } else {
                            appendLine("📍 Showing ${uiState.busStops.size} real MyCiTi stops")
                            appendLine("🔴 Zoom: ${String.format("%.1f", currentZoom)}")
                            when {
                                currentZoom < 12f -> appendLine("� Zoom in to see more stops")
                                currentZoom < 15f -> appendLine("🟠 Medium detail view")
                                else -> appendLine("🟢 High detail view")
                            }
                        }
                        appendLine("• Data from City of Cape Town")
                        appendLine("• 🔴 = Major Hub, 🟢 = Railway, 🔵 = Bus Stop")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}