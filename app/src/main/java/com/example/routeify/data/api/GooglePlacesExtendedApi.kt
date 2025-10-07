/*
 * ============================================================================
 * GOOGLE PLACES EXTENDED API - Comprehensive Maps Platform Integration
 * ============================================================================
 * 
 * This file defines the primary Retrofit interface for accessing Google Maps
 * Platform APIs, providing comprehensive location and routing services.
 * 
 * PRIMARY PURPOSE:
 * - Centralizes all Google Maps Platform API endpoints in one interface
 * - Provides travel time calculations, geocoding, and route planning
 * - Enables advanced place search and detailed location information
 * - Powers the core functionality of the Routeify navigation app
 * 
 * GOOGLE APIS INTEGRATED:
 * - Distance Matrix API: Multi-point travel time and distance calculations
 * - Geocoding API: Address to coordinate conversion and reverse geocoding
 * - Places Autocomplete API: Real-time search suggestions and place discovery
 * - Place Details API: Comprehensive place information with business data
 * - Directions API: Turn-by-turn navigation with transit route optimization
 * 
 * KEY CAPABILITIES:
 * - Multi-modal transportation support (driving, walking, transit, cycling)
 * - Real-time traffic and transit schedule integration
 * - Comprehensive place database with ratings, photos, and contact info
 * - Smart route optimization with alternative path suggestions
 * - Location-aware search with context-sensitive results
 * 
 * AUTHENTICATION:
 * All endpoints require a valid Google Places API key configured in the
 * application.
 * 
 * USAGE:
 * This interface is implemented by GooglePlacesEnhancedRepository and serves
 * as the backbone for all location-based features in the application.
 * 
 * ============================================================================
 */


package com.example.routeify.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesExtendedApi {

    companion object {
        // Base URL for all Google Maps Platform APIs
        const val BASE_URL = "https://maps.googleapis.com/maps/api/"
    }


     //Calculate travel times and distances between multiple origins and destinations
    @GET("distancematrix/json")
    suspend fun getDistanceMatrix(
        @Query("origins") origins: String,
        @Query("destinations") destinations: String,
        @Query("mode") mode: String = "transit",
        @Query("key") apiKey: String
    ): GoogleDistanceMatrixResponse

    
    //Convert a human-readable address into geographic coordinates
    @GET("geocode/json")
    suspend fun geocodeAddress(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): GoogleGeocodingResponse

    
    // Get place predictions for autocomplete functionality
    @GET("place/autocomplete/json")
    suspend fun getPlaceAutocomplete(
        @Query("input") input: String,
        @Query("key") apiKey: String,
        @Query("location") location: String? = null,
        @Query("radius") radius: Int? = null,
        @Query("components") components: String? = null
    ): GoogleAutocompleteResponse


    // Get detailed information about a specific place using its place ID
    @GET("place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String,
        @Query("fields") fields: String = "geometry,formatted_address,name"
    ): GooglePlaceDetailsResponse


    // Generate turn-by-turn directions between two locations with transit options
    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "transit",
        @Query("alternatives") alternatives: Boolean = true,
        @Query("key") apiKey: String,
        @Query("departure_time") departureTime: String? = null,
        @Query("transit_mode") transitMode: String? = null
    ): GoogleDirectionsResponse
}

// ============================================================================
// SHARED MODELS - Core data structures used across multiple API responses
// ============================================================================

 // Geographic coordinates representing a specific location
data class GoogleLocation(
    val lat: Double,
    val lng: Double
)


 // Precise positioning data for mapping and navigation
data class GoogleGeometry(
    val location: GoogleLocation,
    @SerializedName("location_type")
    val locationType: String? = null,
    val bounds: GoogleBounds? = null
)


 // viewport regions and place boundaries (NE and SW corners)
data class GoogleBounds(
    val northeast: GoogleLocation,
    val southwest: GoogleLocation
)


// Distance measurement with human-readable text and precise value
data class GoogleDistance(
    val text: String,
    val value: Int
)


// Accounts for traffic conditions and transit schedules form travel time
data class GoogleDuration(
    val text: String,
    val value: Int
)

// Encoded polyline string representing a path or route geometry
data class GooglePolyline(
    /** Encoded polyline string */
    val points: String
)

// ============================================================================
// DISTANCE MATRIX API - Calculate travel times between multiple locations
// ============================================================================


// Response from Distance Matrix API containing travel data for origin-destination pairs
data class GoogleDistanceMatrixResponse(
    @SerializedName("destination_addresses")
    val destinationAddresses: List<String>,
    @SerializedName("origin_addresses")
    val originAddresses: List<String>,
    val rows: List<GoogleDistanceMatrixRow>,
    val status: String,
    @SerializedName("error_message")
    val errorMessage: String? = null
)

// Single row in distance matrix representing one origin point
data class GoogleDistanceMatrixRow(
    val elements: List<GoogleDistanceMatrixElement>
)

// Individual element containing travel data between one origin-destination pair
data class GoogleDistanceMatrixElement(
    val distance: GoogleDistance? = null,
    val duration: GoogleDuration? = null,
    val status: String
)

// ============================================================================
// GEOCODING API - Convert addresses to coordinates and vice versa
// ============================================================================

// Response from Geocoding API containing location results for an address query
data class GoogleGeocodingResponse(
    val results: List<GoogleGeocodingResult>,
    val status: String,
    @SerializedName("error_message")
    val errorMessage: String? = null
)

// Individual geocoding result containing location data for an address
data class GoogleGeocodingResult(
    @SerializedName("formatted_address")
    val formattedAddress: String,
    val geometry: GoogleGeometry,
    @SerializedName("place_id")
    val placeId: String,
    val types: List<String>
)

// ============================================================================
// PLACES AUTOCOMPLETE API - Provide search suggestions as user types
// ============================================================================

// Response from Places Autocomplete API containing place predictions
data class GoogleAutocompleteResponse(
    val status: String,
    val predictions: List<GoogleAutocompletePrediction>,
    @SerializedName("error_message")
    val errorMessage: String? = null
)

// Individual place prediction with structured formatting for display
data class GoogleAutocompletePrediction(
    val description: String,
    @SerializedName("place_id")
    val placeId: String,
    @SerializedName("structured_formatting")
    val structuredFormatting: GoogleStructuredFormatting? = null,
    val terms: List<GoogleTerm>? = null,
    val types: List<String>? = null
)

// Structured formatting for displaying autocomplete results with emphasis
data class GoogleStructuredFormatting(
    @SerializedName("main_text")
    val mainText: String,
    @SerializedName("secondary_text")
    val secondaryText: String
)

// Individual term within an autocomplete prediction
data class GoogleTerm(
    val offset: Int,
    val value: String
)

// ============================================================================
// PLACE DETAILS API - Get comprehensive information about specific places
// ============================================================================

// Response from Place Details API containing complete place information
data class GooglePlaceDetailsResponse(
    val status: String,
    val result: GooglePlaceDetailsResult? = null,
    @SerializedName("error_message")
    val errorMessage: String? = null
)


// Essential place details result containing basic location information
data class GooglePlaceDetailsResult(
    val geometry: GoogleGeometry,
    @SerializedName("formatted_address")
    val formattedAddress: String,
    val name: String
)

// ============================================================================
// DIRECTIONS API - Calculate routes and navigation instructions
// ============================================================================

// Response from Directions API containing route options and metadata
data class GoogleDirectionsResponse(
    val status: String,
    val routes: List<GoogleRoute>,
    @SerializedName("error_message")
    val errorMessage: String? = null,
    @SerializedName("available_travel_modes")
    val availableTravelModes: List<String>? = null
)

// Individual route option with summary, legs, and overview polyline
data class GoogleRoute(
    val summary: String,
    val legs: List<GoogleLeg>,
    @SerializedName("overview_polyline")
    val overviewPolyline: GooglePolyline? = null,
    val bounds: GoogleBounds? = null,
    val warnings: List<String>? = null
)

// Single leg of a route containing distance, duration, and navigation steps
data class GoogleLeg(
    val distance: GoogleDistance,
    val duration: GoogleDuration,
    @SerializedName("start_address")
    val startAddress: String,
    @SerializedName("end_address")
    val endAddress: String,
    @SerializedName("start_location")
    val startLocation: GoogleLocation,
    @SerializedName("end_location")
    val endLocation: GoogleLocation,
    val steps: List<GoogleStep>
)

// Individual navigation step within a route leg
data class GoogleStep(
    @SerializedName("travel_mode")
    val travelMode: String,
    val distance: GoogleDistance,
    val duration: GoogleDuration,
    @SerializedName("html_instructions")
    val htmlInstructions: String,
    @SerializedName("start_location")
    val startLocation: GoogleLocation,
    @SerializedName("end_location")
    val endLocation: GoogleLocation,
    @SerializedName("transit_details")
    val transitDetails: GoogleTransitDetails? = null,
    val polyline: GooglePolyline? = null
)


 // Transit-specific information for public transportation steps
data class GoogleTransitDetails(

    @SerializedName("departure_stop")
    val departureStop: GoogleTransitStop,
    @SerializedName("arrival_stop")
    val arrivalStop: GoogleTransitStop,
    @SerializedName("departure_time")
    val departureTime: GoogleTransitTime,
    @SerializedName("arrival_time")
    val arrivalTime: GoogleTransitTime,
    val line: GoogleTransitLine,
    @SerializedName("num_stops")
    val numStops: Int,
    val headsign: String? = null
)

// Basic information about a transit stop or station
data class GoogleTransitStop(
    val name: String,
    val location: GoogleLocation
)

// Scheduled time information for transit departures and arrivals
data class GoogleTransitTime(
    val text: String,
    val value: Long,
    @SerializedName("time_zone")
    val timeZone: String
)

// Detailed information about a transit line including vehicle and agency data
data class GoogleTransitLine(
    val name: String,
    @SerializedName("short_name")
    val shortName: String? = null,
    val color: String? = null,
    @SerializedName("text_color")
    val textColor: String? = null,
    val vehicle: GoogleTransitVehicle,
    val agencies: List<GoogleTransitAgency>? = null
)

// Vehicle type and branding information for a transit line
data class GoogleTransitVehicle(
    val name: String,
    val type: String,
    val icon: String? = null
)

// Information about a transit agency operating a transit line
data class GoogleTransitAgency(
    val name: String,
    val url: String? = null,
    val phone: String? = null
)