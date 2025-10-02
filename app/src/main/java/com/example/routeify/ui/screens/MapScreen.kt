package com.example.routeify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routeify.ui.viewmodel.MapViewModel
import com.example.routeify.utils.BusStopClusterItem
import com.example.routeify.utils.ClusterManagerEffect
import com.example.routeify.utils.rememberClusterManager
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val viewModel: MapViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header Card
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
                    text = if (uiState.isLoading) "Loading real MyCiTi data..." else "Live MyCiTi bus stops with clustering",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Map with clustering
        val capeTabCity = LatLng(-33.9249, 18.4241) // Cape Town coordinates
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(capeTabCity, 11f)
        }

        // Track current zoom level and load data accordingly
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

        // Create cluster items from bus stops
        val clusterItems = remember(uiState.busStops) {
            uiState.busStops.map { busStop ->
                BusStopClusterItem(busStop)
            }
        }

        // Track the GoogleMap instance
        var map by remember { mutableStateOf<com.google.android.gms.maps.GoogleMap?>(null) }
        val clusterManager = rememberClusterManager(map)

        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            cameraPositionState = cameraPositionState,
            onMapLoaded = {
                // Map is ready, clustering will be set up via clusterManager
            }
        ) {
            MapEffect(Unit) { googleMap ->
                map = googleMap
            }
        }
        
        // Update cluster items when data changes
        ClusterManagerEffect(clusterManager, clusterItems)

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
                    text = "Real MyCiTi Data with Clustering",
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
                            appendLine("🔍 Zoom: ${String.format("%.1f", currentZoom)}")
                            when {
                                currentZoom < 12f -> appendLine("🔵 Zoom in to see more stops")
                                currentZoom < 15f -> appendLine("🟠 Medium detail view with clustering")
                                else -> appendLine("🟢 High detail view with clustering")
                            }
                        }
                        appendLine("• Data from City of Cape Town")
                        appendLine("• 🔵 = Clustered stops, tap to expand")
                        appendLine("• Custom icons for different stop types")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
