package com.example.routeify.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Google Places API interface for transit data
 * Free tier: 5,000 requests/month
 */
interface GoogleTransitApi {
    
    /**
     * Find nearby transit stations using Google Places API
     * 
     * @param location Center point as "lat,lng"
     * @param radius Search radius in meters (max 50,000)
     * @param type Place type (bus_station, subway_station, transit_station)
     * @param androidx.compose.runtime.key Google API key
     */
    @GET("nearbysearch/json")
    suspend fun getNearbyTransitStops(
        @Query("location") location: String,
        @Query("radius") radius: Int = 5000,
        @Query("type") type: String = "transit_station",
        @Query("key") apiKey: String
    ): GooglePlacesResponse

    companion object {
        const val BASE_URL = "https://maps.googleapis.com/maps/api/place/"
    }
}

// Data models for Google Places API response
data class GooglePlacesResponse(
    val results: List<GooglePlace>,
    val status: String,
    @SerializedName("next_page_token")
    val nextPageToken: String?
)

data class GooglePlace(
    @SerializedName("place_id")
    val placeId: String,
    val name: String,
    val geometry: GoogleGeometry,
    val types: List<String>,
    val vicinity: String?,
    val rating: Double?,
    @SerializedName("user_ratings_total")
    val userRatingsTotal: Int?
)

data class GoogleGeometry(
    val location: GoogleLatLng
)

data class GoogleLatLng(
    val lat: Double,
    val lng: Double
)

