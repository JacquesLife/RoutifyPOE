package com.example.routeify.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Google Places API extensions for enhanced features
 * Only includes actively used endpoints
 */
interface GooglePlacesExtendedApi {

    /**
     * Get distance matrix between multiple points
     * This provides travel time and distance information
     */
    @GET("distancematrix/json")
    suspend fun getDistanceMatrix(
        @Query("origins") origins: String,
        @Query("destinations") destinations: String,
        @Query("mode") mode: String = "transit",
        @Query("key") apiKey: String
    ): GoogleDistanceMatrixResponse

    /**
     * Geocoding API to convert addresses to coordinates
     */
    @GET("geocode/json")
    suspend fun geocodeAddress(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): GoogleGeocodingResponse

    companion object {
        const val BASE_URL = "https://maps.googleapis.com/maps/api/"
    }
}

/**
 * Response models for distance matrix
 */
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

data class GoogleDistance(
    val text: String,
    val value: Int // meters
)

data class GoogleDuration(
    val text: String,
    val value: Int // seconds
)

/**
 * Geocoding response models
 */
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