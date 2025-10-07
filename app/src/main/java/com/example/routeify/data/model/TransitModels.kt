/*
 * ============================================================================
 * TRANSIT MODELS - Public Transportation Data Structures
 * ============================================================================
 * 
 * Simple data classes for representing transit stops and transportation types.
 * Used for mapping Google Places API responses to app-specific models.
 * 
 * ============================================================================
 */

package com.example.routeify.data.model

// Data class representing a transit stop
data class TransitStop(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val stopType: TransitStopType,
    val vicinity: String? = null,
    val rating: Double? = null
)

// Enum representing different types of transit stops
enum class TransitStopType {
    BUS_STATION,        
    SUBWAY_STATION,
    TRAIN_STATION,
    TRANSIT_STATION,
    LIGHT_RAIL_STATION
}

// --------------------------------------------------End of File----------------------------------------------------------------