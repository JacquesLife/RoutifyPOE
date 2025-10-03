package com.example.routeify.utils

import com.example.routeify.data.model.TransitStop
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * Cluster item wrapper for transit stops
 */
data class BusStopClusterItem(
    private val transitStop: TransitStop
) : ClusterItem {
    
    override fun getPosition(): LatLng {
        return LatLng(transitStop.latitude, transitStop.longitude)
    }
    
    override fun getTitle(): String? {
        return transitStop.name
    }
    
    override fun getSnippet(): String? {
        return buildString {
            if (transitStop.vicinity != null) appendLine("Area: ${transitStop.vicinity}")
            appendLine("Type: ${transitStop.stopType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}")
            if (transitStop.rating != null) appendLine("Rating: ${transitStop.rating}★")
        }.trim()
    }
    
    override fun getZIndex(): Float? {
        return 0.0f
    }
    
    fun getTransitStop(): TransitStop = transitStop
}