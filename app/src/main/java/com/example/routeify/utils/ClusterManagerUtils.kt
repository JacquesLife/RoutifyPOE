package com.example.routeify.utils

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.clustering.ClusterManager

/**
 * Creates and manages a cluster manager for bus stops
 */
@Composable
fun rememberClusterManager(
    map: GoogleMap?
): ClusterManager<BusStopClusterItem>? {
    val context = LocalContext.current
    
    return remember(map) {
        map?.let { googleMap ->
            ClusterManager<BusStopClusterItem>(context, googleMap).apply {
                // Set custom renderer
                renderer = BusStopClusterRenderer(context, googleMap, this)
                
                // Set up click listeners
                googleMap.setOnCameraIdleListener(this)
                googleMap.setOnMarkerClickListener(this)
                googleMap.setOnInfoWindowClickListener(this)
            }
        }
    }
}

/**
 * Effect to update cluster items when bus stops change
 */
@Composable
fun ClusterManagerEffect(
    clusterManager: ClusterManager<BusStopClusterItem>?,
    items: List<BusStopClusterItem>
) {
    LaunchedEffect(clusterManager, items) {
        clusterManager?.let { manager ->
            manager.clearItems()
            manager.addItems(items)
            manager.cluster()
        }
    }
}