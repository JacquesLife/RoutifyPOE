/*
 * ============================================================================
 * TRAVEL TIME MODEL - Journey Distance & Duration Data
 * ============================================================================
 * 
 * Data class for representing travel calculations between two locations.
 * Stores origin/destination coordinates, addresses, and journey metrics.
 * 
 * ============================================================================
 */

package com.example.routeify.domain.model

import com.google.android.gms.maps.model.LatLng

// Data class representing travel time and distance information between two locations
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
    // Convert duration from seconds to minutes
    fun getDurationInMinutes(): Int {
        return durationValue / 60
    }
    
    // Determine if the trip is considered a quick trip (less than 30 minutes)
    val isQuickTrip: Boolean
        get() = getDurationInMinutes() < 30
}

// --------------------------------------------------End of File----------------------------------------------------------------