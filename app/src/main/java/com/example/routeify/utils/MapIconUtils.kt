package com.example.routeify.utils

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object MapIconUtils {
    
    /**
     * Get marker icon based on Google Transit stop type using Google's default markers
     * Simple and reliable - perfect for now, easy to replace with custom icons later!
     */
    fun getTransportIcon(stopType: com.example.routeify.data.model.TransitStopType): BitmapDescriptor {
        return when (stopType) {
            com.example.routeify.data.model.TransitStopType.BUS_STATION -> {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE) // Blue for buses
            }
            com.example.routeify.data.model.TransitStopType.TRAIN_STATION -> {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN) // Green for trains
            }
            com.example.routeify.data.model.TransitStopType.SUBWAY_STATION -> {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE) // Orange for subway
            }
            com.example.routeify.data.model.TransitStopType.LIGHT_RAIL_STATION -> {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW) // Yellow for light rail
            }
            com.example.routeify.data.model.TransitStopType.TRANSIT_STATION -> {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED) // Red for major transit hubs
            }
        }
    }
}