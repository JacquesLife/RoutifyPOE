package com.example.routeify.utils

import com.example.routeify.data.model.TransitStop
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

// Cluster item representing a bus stop on the map
data class BusStopClusterItem(
    private val transitStop: TransitStop
) : ClusterItem {
    
    // Position of the cluster item
    override fun getPosition(): LatLng {
        return LatLng(transitStop.latitude, transitStop.longitude)
    }
    
    // Title for the info window
    override fun getTitle(): String? {
        return transitStop.name
    }
    
    // Snippet for the info window
    override fun getSnippet(): String? {
        return buildString {
            if (transitStop.vicinity != null) appendLine("Area: ${transitStop.vicinity}")
            appendLine("Type: ${transitStop.stopType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}")
            if (transitStop.rating != null) appendLine("Rating: ${transitStop.rating}★")
        }.trim()
    }
    // Z-index for layering on the map
    override fun getZIndex(): Float? {
        return 0.0f
    }
    
    // Accessor for the underlying TransitStop
    fun getTransitStop(): TransitStop = transitStop
}