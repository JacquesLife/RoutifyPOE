package com.example.routeify.utils

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

/**
 * Custom cluster renderer for bus stops with custom icons
 */
class BusStopClusterRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<BusStopClusterItem>
) : DefaultClusterRenderer<BusStopClusterItem>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(
        item: BusStopClusterItem,
        markerOptions: MarkerOptions
    ) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        
        // Set custom icon based on stop type
        val icon: BitmapDescriptor = MapIconUtils.getTransportIcon(context, item.getTransitStop().stopType)
        markerOptions.icon(icon)
        
        // Set title and snippet for info window
        markerOptions.title(item.title)
        markerOptions.snippet(item.snippet)
        
        // Make markers slightly more visible
        markerOptions.anchor(0.5f, 1.0f) // Anchor at bottom center
    }

    override fun onClusterItemUpdated(item: BusStopClusterItem, marker: Marker) {
        super.onClusterItemUpdated(item, marker)
        
        // Update icon if needed
        val icon: BitmapDescriptor = MapIconUtils.getTransportIcon(context, item.getTransitStop().stopType)
        marker.setIcon(icon)
    }
    
    override fun getColor(clusterSize: Int): Int {
        // Custom cluster colors based on size
        return when {
            clusterSize < 5 -> android.graphics.Color.rgb(34, 139, 34)   // Forest green for small clusters
            clusterSize < 10 -> android.graphics.Color.rgb(255, 140, 0)  // Dark orange for medium clusters  
            else -> android.graphics.Color.rgb(220, 20, 60)              // Crimson for large clusters
        }
    }
}