package com.example.routeify.domain.model

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng

/**
 * Represents a complete transit route with multiple segments
 */
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
    fun getDurationInMinutes(): Int = durationValue / 60

    val primaryTransitMode: String
        get() = segments.firstOrNull { it.transitInfo != null }?.transitInfo?.vehicleType ?: "WALKING"

    val isQuickTrip: Boolean
        get() = getDurationInMinutes() < 30

    val numberOfTransfers: Int
        get() = segments.count { it.transitInfo != null } - 1
}

/**
 * Represents a segment of a route
 */
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

/**
 * Transit-specific information
 */
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

    fun getStopsSummary(): String {
        return when (numStops) {
            0 -> "Direct"
            1 -> "1 stop"
            else -> "$numStops stops"
        }
    }
}

/**
 * Place suggestion from autocomplete
 */
data class PlaceSuggestion(
    val placeId: String,
    val description: String,
    val mainText: String,
    val secondaryText: String
) {
    val isMajorPlace: Boolean
        get() = secondaryText.contains("South Africa", ignoreCase = true) ||
                secondaryText.contains("Cape Town", ignoreCase = true)
}