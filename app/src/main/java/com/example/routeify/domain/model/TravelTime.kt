package com.example.routeify.domain.model

import com.google.android.gms.maps.model.LatLng

/**
 * Represents travel time information between two locations
 */
data class TravelTime(
    val origin: LatLng,
    val destination: LatLng,
    val originAddress: String,
    val destinationAddress: String,
    val distance: String,
    val distanceValue: Int, // in meters
    val duration: String,
    val durationValue: Int, // in seconds
    val mode: String = "transit"
) {
    /**
     * Get duration in minutes for easier display
     */
    fun getDurationInMinutes(): Int {
        return durationValue / 60
    }
    
    /**
     * Check if this is a quick trip (under 30 minutes)
     */
    val isQuickTrip: Boolean
        get() = getDurationInMinutes() < 30
}