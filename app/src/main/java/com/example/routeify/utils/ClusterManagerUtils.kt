/*
 * ============================================================================
 * CLUSTER MANAGER UTILS - Compose Map Clustering Integration
 * ============================================================================
 * 
 * Utility functions for integrating ClusterManager with Jetpack Compose.
 * Handles marker clustering lifecycle within Compose environment.
 *
 * REFERENCES:
 * https://developers.google.com/maps/documentation/android-sdk/utility/marker-clustering
 * ============================================================================
 */

package com.example.routeify.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.clustering.ClusterManager

// Utility functions for managing ClusterManager in a Compose environment
@Composable
fun rememberClusterManager(
    map: GoogleMap?
): ClusterManager<BusStopClusterItem>? {
    val context = LocalContext.current
    
    // Remember the ClusterManager instance
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

// Effect to update ClusterManager with new items
@Composable
fun ClusterManagerEffect(
    clusterManager: ClusterManager<BusStopClusterItem>?,
    items: List<BusStopClusterItem>
) {
    // Update the ClusterManager whenever items change
    LaunchedEffect(clusterManager, items) {
        clusterManager?.let { manager ->
            manager.clearItems()
            manager.addItems(items)
            manager.cluster()
        }
    }
}

// --------------------------------------------------End of File----------------------------------------------------------------