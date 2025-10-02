package com.example.routeify.utils

import android.content.Context
import com.example.routeify.data.model.StopType
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
        val icon: BitmapDescriptor = MapIconUtils.getTransportIcon(context, item.getBusStop().stopType)
        markerOptions.icon(icon)
        
        // Set title and snippet
        markerOptions.title(item.title)
        markerOptions.snippet(item.snippet)
    }

    override fun onClusterItemUpdated(item: BusStopClusterItem, marker: Marker) {
        super.onClusterItemUpdated(item, marker)
        
        // Update icon if needed
        val icon: BitmapDescriptor = MapIconUtils.getTransportIcon(context, item.getBusStop().stopType)
        marker.setIcon(icon)
    }
}