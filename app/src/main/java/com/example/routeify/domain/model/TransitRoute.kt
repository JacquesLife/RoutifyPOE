/*
 * ============================================================================
 * TRANSIT ROUTE MODEL - Multi-Modal Journey Representation
 * ============================================================================
 * 
 * Comprehensive data classes for representing complex transit routes.
 * Handles multi-segment journeys with walking, driving, and public transport.
 * 
 * ============================================================================
 */

package com.example.routeify.domain.model

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng

// Data class representing a transit route with multiple segments
data class TransitRoute(
    val summary: String,
    val totalDistance: String,
    val totalDuration: String,
    val durationValue: Int, // in seconds
    val segments: List<RouteSegment>,
    val overviewPolyline: String? = null,
    val startLocation: LatLng? = null,
    val endLocation: LatLng? = null
) {
    // Convert duration from seconds to minutes
    fun getDurationInMinutes(): Int = durationValue / 60

    val primaryTransitMode: String
        get() = segments.firstOrNull { it.transitInfo != null }?.transitInfo?.vehicleType ?: "WALKING"

    val isQuickTrip: Boolean
        get() = getDurationInMinutes() < 30

    val numberOfTransfers: Int
        get() = segments.count { it.transitInfo != null } - 1
}

// Data class representing a single segment of a transit route
data class RouteSegment(
    val instruction: String,
    val distance: String,
    val duration: String,
    val travelMode: String,
    val transitInfo: TransitInfo? = null
) {
    val isWalking: Boolean
        get() = travelMode.equals("WALKING", ignoreCase = true)

    val isTransit: Boolean
        get() = transitInfo != null
}

// Data class representing detailed transit information for a segment
data class TransitInfo(
    val lineName: String,
    val lineShortName: String?,
    val vehicleType: String,
    val vehicleIcon: String?,
    val lineColor: String?,
    val departureStop: String,
    val arrivalStop: String,
    val numStops: Int,
    val headsign: String?
) {
    // Get a user-friendly display name for the transit line
    fun getDisplayName(): String {
        return lineShortName?.takeIf { it.isNotBlank() } ?: lineName
    }

    fun getColorInt(): Int? {
        return lineColor?.let {
            try {
                val colorString = if (it.startsWith("#")) it else "#$it"
                Color.parseColor(colorString)
            } catch (e: Exception) {
                null
            }
        }
    }
    // Get a user-friendly vehicle type name
    fun getVehicleDisplayName(): String {
        return when (vehicleType.uppercase()) {
            "BUS" -> "Bus"
            "TRAIN", "HEAVY_RAIL", "RAIL" -> "Train"
            "SUBWAY", "METRO_RAIL" -> "Subway"
            "TRAM", "LIGHT_RAIL" -> "Tram"
            "FERRY" -> "Ferry"
            "CABLE_CAR" -> "Cable Car"
            "GONDOLA" -> "Gondola"
            "FUNICULAR" -> "Funicular"
            else -> vehicleType.replace("_", " ").lowercase()
                .replaceFirstChar { char -> char.uppercase() }
        }
    }

    // Get a summary of the number of stops
    fun getStopsSummary(): String {
        return when (numStops) {
            0 -> "Direct"
            1 -> "1 stop"
            else -> "$numStops stops"
        }
    }
}

// Data class representing a place suggestion from autocomplete
data class PlaceSuggestion(
    val placeId: String,
    val description: String,
    val mainText: String,
    val secondaryText: String
) {
    // Simple heuristic to identify major places (e.g., cities)
    val isMajorPlace: Boolean
        get() = secondaryText.contains("South Africa", ignoreCase = true) ||
                secondaryText.contains("Cape Town", ignoreCase = true)
}

// --------------------------------------------------End of File----------------------------------------------------------------