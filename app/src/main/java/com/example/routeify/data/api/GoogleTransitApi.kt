package com.example.routeify.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit interface for Google Places API to fetch nearby transit stops
interface GoogleTransitApi {

// Fetch nearby transit stops of a specific type within a given radius
    @GET("nearbysearch/json")
    suspend fun getNearbyTransitStops(
        @Query("location") location: String,
        @Query("radius") radius: Int = 5000,
        @Query("type") type: String = "transit_station",
        @Query("key") apiKey: String
    ): GooglePlacesNearbyResponse

    // Base URL for Google Places API
    companion object {
        const val BASE_URL = "https://maps.googleapis.com/maps/api/place/"
    }
}

// Data classes for parsing Google Places API responses
data class GooglePlacesNearbyResponse(
    val results: List<GooglePlace>,
    val status: String,
    @SerializedName("next_page_token")
    val nextPageToken: String?
)

// Represents a place returned by the Google Places API
data class GooglePlace(
    @SerializedName("place_id")
    val placeId: String,
    val name: String,
    val geometry: GooglePlaceGeometry,
    val types: List<String>,
    val vicinity: String?,
    val rating: Double?,
    @SerializedName("user_ratings_total")
    val userRatingsTotal: Int?
)

// Geometry information including location coordinates
data class GooglePlaceGeometry(
    val location: GooglePlaceLatLng
)

// Latitude and longitude of a place
data class GooglePlaceLatLng(
    val lat: Double,
    val lng: Double
)