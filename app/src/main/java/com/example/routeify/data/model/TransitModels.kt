package com.example.routeify.data.model

/**
 * Unified transit stop model for our app
 * Clean and simple - no Google API dependencies
 */
data class TransitStop(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val stopType: TransitStopType,
    val vicinity: String? = null,
    val rating: Double? = null
)

/**
 * Transit stop types based on Google Places API types
 */
enum class TransitStopType {
    BUS_STATION,        // "bus_station"
    SUBWAY_STATION,     // "subway_station"  
    TRAIN_STATION,      // "train_station"
    TRANSIT_STATION,    // "transit_station"
    LIGHT_RAIL_STATION  // "light_rail_station"
}