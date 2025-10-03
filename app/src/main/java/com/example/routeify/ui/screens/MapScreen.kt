package com.example.routeify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routeify.data.model.StopType
import com.example.routeify.ui.viewmodel.MapViewModel
import com.example.routeify.utils.BusStopClusterItem
import com.example.routeify.utils.ClusterManagerEffect
import com.example.routeify.utils.rememberClusterManager
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.abs

@OptIn(MapsComposeExperimentalApi::class)
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
                    text = "Cape Town Transport Network",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (uiState.isLoading) "Loading transport data..." else "Live bus stops & railway stations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                // Debug: Force load button
                if (!uiState.isLoading) {
                    Button(
                        onClick = { viewModel.loadBusStopsForZoom(11f) }, // Use default zoom for now
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Force Reload Data")
                    }
                }
            }
        }

        // Map with clustering
        val capeTabCity = LatLng(-33.9249, 18.4241) // Cape Town coordinates
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(capeTabCity, 11f)
        }

        // Track current zoom level and load data accordingly
        val currentZoom by remember { derivedStateOf { cameraPositionState.position.zoom } }
        
        // PERFORMANCE: Load data when zoom level changes (reduced threshold for better responsiveness)
        LaunchedEffect(currentZoom) {
            val previousZoom = uiState.currentZoom
            // Load if zoom changed OR if it's the first load (no bus stops)
            if (abs(currentZoom - previousZoom) > 0.3f || uiState.busStops.isEmpty()) {
                viewModel.loadBusStopsForZoom(currentZoom)
            }
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
                // Initial load when map is ready
                viewModel.loadBusStopsForZoom(cameraPositionState.position.zoom)
            }
        ) {
            MapEffect(Unit) { googleMap ->
                map = googleMap
            }
            
            // Draw railway lines as polylines
            if (uiState.railwayLines.isNotEmpty()) {
                android.util.Log.d("MapScreen", "Drawing ${uiState.railwayLines.size} railway lines")
                uiState.railwayLines.forEach { railwayLine ->
                    android.util.Log.d("MapScreen", "Railway line '${railwayLine.name}' has ${railwayLine.paths.size} paths")
                    railwayLine.paths.forEach { path ->
                        if (path.isNotEmpty()) {
                            android.util.Log.d("MapScreen", "Drawing path with ${path.size} points - first point: ${path.first()}")
                            Polyline(
                                points = path,
                                color = androidx.compose.ui.graphics.Color.Magenta, // Bright magenta for maximum visibility
                                width = 16f, // Very thick line for visibility
                                clickable = false
                            )
                        }
                    }
                }
            } else {
                android.util.Log.w("MapScreen", "No railway lines to display - uiState.railwayLines is empty")
            }
            
            // TEST: Draw a hardcoded railway line for testing
            Polyline(
                points = listOf(
                    LatLng(-33.9249, 18.4241), // Cape Town Station
                    LatLng(-33.9569, 18.4683), // Woodstock
                    LatLng(-33.9707, 18.4847), // Salt River
                    LatLng(-34.0042, 18.5205)  // Observatory
                ),
                color = androidx.compose.ui.graphics.Color.Yellow,
                width = 8f,
                clickable = false
            )
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
                    text = "Cape Town Transport Network",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildString {
                        if (uiState.isLoading) {
                            appendLine("⏳ Optimizing transport data load...")
                        } else {
                            val busStops = uiState.busStops.count { it.stopType != StopType.RAILWAY }
                            val railStops = uiState.busStops.count { it.stopType == StopType.RAILWAY }
                            val railLines = uiState.railwayLines.size
                            val totalStops = uiState.busStops.size
                            
                            appendLine("🚌 $busStops MyCiTi bus stops")
                            appendLine("🚂 $railStops railway stations")
                            appendLine("🛤️ $railLines railway lines")
                            appendLine("� Total items: $totalStops")
                            appendLine("�🔍 Zoom: ${String.format("%.1f", currentZoom)}")
                            
                            // Debug information
                            if (totalStops == 0) {
                                appendLine("⚠️ No data loaded - check API")
                            }
                            
                            // Balanced zoom thresholds for better visibility
                            when {
                                currentZoom < 9f -> appendLine("🔵 Major hubs only (20 rail stations)")
                                currentZoom < 11f -> appendLine("🟡 Default view (25 bus + 15 rail)")
                                currentZoom < 13f -> appendLine("🟠 Medium detail (40 bus + 20 rail)")
                                currentZoom < 15f -> appendLine("🟢 High detail (60 bus + 25 rail)")
                                else -> appendLine("🔴 Maximum detail (80 bus + 30 rail)")
                            }
                        }
                        appendLine("• Memory-optimized with aggressive caching")
                        appendLine("• OutOfMemory protection enabled")
                        appendLine("• 🔵 Clustered points • Tap to expand")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
