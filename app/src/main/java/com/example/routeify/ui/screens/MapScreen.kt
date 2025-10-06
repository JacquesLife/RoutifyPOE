package com.example.routeify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routeify.data.model.TransitStopType
import com.example.routeify.ui.components.TransportLegend
import com.example.routeify.ui.viewmodel.MapViewModel
import com.example.routeify.utils.BusStopClusterItem
import com.example.routeify.utils.ClusterManagerEffect
import com.example.routeify.utils.rememberClusterManager
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState


@OptIn(MapsComposeExperimentalApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    LocalContext.current
    val viewModel: MapViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    
    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            // Bottom sheet content will be added here
            BottomSheetContent()
        },
        sheetPeekHeight = 80.dp,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
        // Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cape Town Transport Network",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (uiState.isLoading) "Loading transport data..." else "Custom icons for bus stops & train stations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                

            }
        }

        // Route Overview Bar (when route is selected)
        val selectedArgs = remember { viewModel.getSelectedRouteArgs() }
        if (selectedArgs != null) {
            RouteOverviewBar(
                selectedArgs = selectedArgs,
                onStartNavigation = {
                    // TODO: Implement navigation start
                }
            )
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
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
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

        // Selected route rendering from SavedStateHandle via ViewModel
        val selectedArgs = remember { viewModel.getSelectedRouteArgs() }
        val polylinePoints = remember(selectedArgs) {
            selectedArgs?.encodedPolyline?.let { encoded ->
                try {
                    com.google.maps.android.PolyUtil.decode(encoded)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }

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

            if (polylinePoints.isNotEmpty()) {
                // Enhanced polyline with thicker width and primary color
                Polyline(
                    points = polylinePoints,
                    color = MaterialTheme.colorScheme.primary,
                    width = 10f,
                    geodesic = true
                )

                selectedArgs?.origin?.let { start ->
                    Marker(
                        state = MarkerState(start), 
                        title = "Start",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }
                selectedArgs?.destination?.let { end ->
                    Marker(
                        state = MarkerState(end), 
                        title = "End",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
            }
        }

        // Move camera to fit route when available
        LaunchedEffect(polylinePoints) {
            if (polylinePoints.isNotEmpty()) {
                map?.let { gMap ->
                    val builder = com.google.android.gms.maps.model.LatLngBounds.Builder()
                    polylinePoints.forEach { builder.include(it) }
                    val bounds = builder.build()
                    gMap.animateCamera(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(bounds, 80)
                    )
                }
            }
        }
        
        // Update cluster items when data changes
        ClusterManagerEffect(clusterManager, clusterItems)

        // Bottom info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Transport Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
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
        
        // Controls Cluster (FAB Group)
        ControlsCluster()
    }
}

@Composable
private fun RouteOverviewBar(
    selectedArgs: com.example.routeify.ui.viewmodel.SelectedRouteArgs,
    onStartNavigation: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Route info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Selected Route",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ETA placeholder (would come from route data)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "~25 min", // Placeholder
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Distance placeholder
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Straighten,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "~8.5 km", // Placeholder
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Transfers placeholder
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.TransferWithinAStation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "2 transfers", // Placeholder
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share button
                IconButton(
                    onClick = {
                        // TODO: Implement share functionality
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val shareIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, generateRouteDeepLink(selectedArgs))
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Route"))
                    }
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share route",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Start button
                Button(
                    onClick = onStartNavigation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Navigation,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Start")
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomSheetContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Step-by-Step Directions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Placeholder steps (in a real app, these would come from the route data)
        val steps = listOf(
            "Walk to Cape Town Station (5 min)",
            "Take Southern Line to Claremont (15 min)",
            "Walk to destination (3 min)"
        )
        
        steps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Step number
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Step description
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                // Step time
                Text(
                    text = when (index) {
                        0 -> "5 min"
                        1 -> "15 min"
                        2 -> "3 min"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (index < steps.lastIndex) {
                Divider(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ControlsCluster() {
    var expanded by remember { mutableStateOf(false) }
    var mapStyle by remember { mutableStateOf(false) } // false = Day, true = Night
    var showTransitLines by remember { mutableStateOf(true) }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Control buttons positioned in bottom-right
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Recenter button
            FloatingActionButton(
                onClick = {
                    // TODO: Implement recenter functionality
                },
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "Recenter",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            // Re-route button (only show if route is selected)
            val selectedArgs = remember { com.example.routeify.ui.viewmodel.MapViewModel().getSelectedRouteArgs() }
            if (selectedArgs != null) {
                FloatingActionButton(
                    onClick = {
                        // TODO: Implement re-route functionality
                    },
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Re-route",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
            
            // Map style toggle
            FloatingActionButton(
                onClick = { mapStyle = !mapStyle },
                modifier = Modifier.size(56.dp),
                containerColor = if (mapStyle) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    if (mapStyle) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (mapStyle) "Day mode" else "Night mode",
                    tint = if (mapStyle) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Layers toggle
            FloatingActionButton(
                onClick = { showTransitLines = !showTransitLines },
                modifier = Modifier.size(56.dp),
                containerColor = if (showTransitLines) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    Icons.Default.Layers,
                    contentDescription = if (showTransitLines) "Hide transit lines" else "Show transit lines",
                    tint = if (showTransitLines) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun generateRouteDeepLink(selectedArgs: com.example.routeify.ui.viewmodel.SelectedRouteArgs): String {
    val fromLat = selectedArgs.origin?.latitude ?: 0.0
    val fromLng = selectedArgs.origin?.longitude ?: 0.0
    val toLat = selectedArgs.destination?.latitude ?: 0.0
    val toLng = selectedArgs.destination?.longitude ?: 0.0
    val poly = selectedArgs.encodedPolyline ?: ""
    
    return "routeify://map?fromLat=$fromLat&fromLng=$fromLng&toLat=$toLat&toLng=$toLng&poly=$poly"
}
