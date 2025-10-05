package com.example.routeify.ui.screens

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
                    text = if (uiState.isLoading) "Loading transport data..." else "Custom icons for bus stops & train stations",
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

        // Legend Card - Calculate actual counts from data
        val busStops = uiState.transitStops.count { 
            it.stopType == TransitStopType.TRANSIT_STATION 
        }
        
        val railStops = uiState.transitStops.count {
            it.stopType in listOf(
                TransitStopType.TRAIN_STATION,
                TransitStopType.SUBWAY_STATION,
                TransitStopType.LIGHT_RAIL_STATION
            )
        }
        
        val busStations = uiState.transitStops.count { 
            it.stopType == TransitStopType.BUS_STATION 
        }

        TransportLegend(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            busStationCount = busStops,
            trainStationCount = railStops,
            transitHubCount = busStations,
            currentZoom = currentZoom,
            isLoading = uiState.isLoading
        )
    }
}