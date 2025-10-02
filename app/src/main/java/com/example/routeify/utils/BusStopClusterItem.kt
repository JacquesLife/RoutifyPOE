package com.example.routeify.utils

import com.example.routeify.data.model.RealBusStop
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * Cluster item wrapper for bus stops
 */
data class BusStopClusterItem(
    private val busStop: RealBusStop
) : ClusterItem {
    
    override fun getPosition(): LatLng {
        return LatLng(busStop.latitude, busStop.longitude)
    }
    
    override fun getTitle(): String? {
        return busStop.name
    }
    
    override fun getSnippet(): String? {
        return buildString {
            if (busStop.area != null) appendLine("Area: ${busStop.area}")
            if (busStop.routes.isNotEmpty()) {
                appendLine("Routes: ${busStop.routes.joinToString(", ")}")
            }
            if (busStop.direction != null) appendLine("Direction: ${busStop.direction}")
            if (busStop.description != null) appendLine("Type: ${busStop.description}")
            appendLine("Status: ${busStop.status}")
        }.trim()
    }
    
    override fun getZIndex(): Float? {
        return 0.0f
    }
    
    fun getBusStop(): RealBusStop = busStop
}