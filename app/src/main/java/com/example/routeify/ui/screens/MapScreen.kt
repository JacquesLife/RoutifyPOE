package com.example.routeify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routeify.data.model.TransitStopType
import com.example.routeify.ui.viewmodel.MapViewModel
import com.example.routeify.utils.BusStopClusterItem
import com.example.routeify.utils.ClusterManagerEffect
import com.example.routeify.utils.rememberClusterManager
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState


@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapScreen() {
    LocalContext.current
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
                

            }
        }

        // Map with clustering
        val capeTabCity = LatLng(-33.9249, 18.4241) // Cape Town coordinates
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(capeTabCity, 11f)
        }

        // Track current zoom level and load data accordingly
        val currentZoom by remember { derivedStateOf { cameraPositionState.position.zoom } }
        
        // Update zoom level for display purposes
        LaunchedEffect(currentZoom) {
            viewModel.updateZoom(currentZoom)
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

        // Create cluster items from transit stops
        val clusterItems = remember(uiState.transitStops) {
            uiState.transitStops.map { transitStop ->
                BusStopClusterItem(transitStop)
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
                            val totalStops = uiState.transitStops.size
                            val busStops = uiState.transitStops.count { it.stopType == TransitStopType.BUS_STATION }
                            val railStops = uiState.transitStops.count { 
                                it.stopType in listOf(
                                    TransitStopType.TRAIN_STATION,
                                    TransitStopType.SUBWAY_STATION,
                                    TransitStopType.LIGHT_RAIL_STATION
                                )
                            }
                            val transitHubs = uiState.transitStops.count { it.stopType == TransitStopType.TRANSIT_STATION }
                            
                            appendLine("🚌 $busStops bus stations")
                            appendLine("🚂 $railStops rail stations")
                            appendLine("� $transitHubs transit hubs")
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
                        appendLine("• Real-time data from Google Places API")
                        appendLine("• Automatic caching and optimization")
                        appendLine("• 🔵 Clustered points • Tap to expand")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
