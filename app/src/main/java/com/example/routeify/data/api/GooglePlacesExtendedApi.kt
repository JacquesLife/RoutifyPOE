package com.example.routeify.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesExtendedApi {

    companion object {
        const val BASE_URL = "https://maps.googleapis.com/maps/api/"
    }

    @GET("distancematrix/json")
    suspend fun getDistanceMatrix(
        @Query("origins") origins: String,
        @Query("destinations") destinations: String,
        @Query("mode") mode: String = "transit",
        @Query("key") apiKey: String
    ): GoogleDistanceMatrixResponse

    @GET("geocode/json")
    suspend fun geocodeAddress(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): GoogleGeocodingResponse

    @GET("place/autocomplete/json")
    suspend fun getPlaceAutocomplete(
        @Query("input") input: String,
        @Query("key") apiKey: String,
        @Query("location") location: String? = null,
        @Query("radius") radius: Int? = null,
        @Query("components") components: String? = null
    ): GoogleAutocompleteResponse

    @GET("place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String,
        @Query("fields") fields: String = "geometry,formatted_address,name"
    ): GooglePlaceDetailsResponse

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
// SHARED MODELS
// ============================================================================

data class GoogleLocation(
    val lat: Double,
    val lng: Double
)

data class GoogleGeometry(
    val location: GoogleLocation,
    @SerializedName("location_type")
    val locationType: String? = null,
    val bounds: GoogleBounds? = null
)

data class GoogleBounds(
    val northeast: GoogleLocation,
    val southwest: GoogleLocation
)

data class GoogleDistance(
    val text: String,
    val value: Int
)

data class GoogleDuration(
    val text: String,
    val value: Int
)

data class GooglePolyline(
    val points: String
)

// ============================================================================
// DISTANCE MATRIX
// ============================================================================

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

data class GoogleDistanceMatrixRow(
    val elements: List<GoogleDistanceMatrixElement>
)

data class GoogleDistanceMatrixElement(
    val distance: GoogleDistance? = null,
    val duration: GoogleDuration? = null,
    val status: String
)

// ============================================================================
// GEOCODING
// ============================================================================

data class GoogleGeocodingResponse(
    val results: List<GoogleGeocodingResult>,
    val status: String,
    @SerializedName("error_message")
    val errorMessage: String? = null
)

data class GoogleGeocodingResult(
    @SerializedName("formatted_address")
    val formattedAddress: String,
    val geometry: GoogleGeometry,
    @SerializedName("place_id")
    val placeId: String,
    val types: List<String>
)

// ============================================================================
// AUTOCOMPLETE
// ============================================================================

data class GoogleAutocompleteResponse(
    val status: String,
    val predictions: List<GoogleAutocompletePrediction>,
    @SerializedName("error_message")
    val errorMessage: String? = null
)

data class GoogleAutocompletePrediction(
    val description: String,
    @SerializedName("place_id")
    val placeId: String,
    @SerializedName("structured_formatting")
    val structuredFormatting: GoogleStructuredFormatting? = null,
    val terms: List<GoogleTerm>? = null,
    val types: List<String>? = null
)

data class GoogleStructuredFormatting(
    @SerializedName("main_text")
    val mainText: String,
    @SerializedName("secondary_text")
    val secondaryText: String
)

data class GoogleTerm(
    val offset: Int,
    val value: String
)

// ============================================================================
// PLACE DETAILS
// ============================================================================

data class GooglePlaceDetailsResponse(
    val status: String,
    val result: GooglePlaceDetailsResult? = null,
    @SerializedName("error_message")
    val errorMessage: String? = null
)

data class GooglePlaceDetailsResult(
    val geometry: GoogleGeometry,
    @SerializedName("formatted_address")
    val formattedAddress: String,
    val name: String
)

// ============================================================================
// DIRECTIONS
// ============================================================================

data class GoogleDirectionsResponse(
    val status: String,
    val routes: List<GoogleRoute>,
    @SerializedName("error_message")
    val errorMessage: String? = null,
    @SerializedName("available_travel_modes")
    val availableTravelModes: List<String>? = null
)

data class GoogleRoute(
    val summary: String,
    val legs: List<GoogleLeg>,
    @SerializedName("overview_polyline")
    val overviewPolyline: GooglePolyline? = null,
    val bounds: GoogleBounds? = null,
    val warnings: List<String>? = null
)

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

data class GoogleTransitStop(
    val name: String,
    val location: GoogleLocation
)

data class GoogleTransitTime(
    val text: String,
    val value: Long,
    @SerializedName("time_zone")
    val timeZone: String
)

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

data class GoogleTransitVehicle(
    val name: String,
    val type: String,
    val icon: String? = null
)

data class GoogleTransitAgency(
    val name: String,
    val url: String? = null,
    val phone: String? = null
)