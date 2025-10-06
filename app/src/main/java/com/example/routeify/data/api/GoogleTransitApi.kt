package com.example.routeify.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleTransitApi {

    @GET("nearbysearch/json")
    suspend fun getNearbyTransitStops(
        @Query("location") location: String,
        @Query("radius") radius: Int = 5000,
        @Query("type") type: String = "transit_station",
        @Query("key") apiKey: String
    ): GooglePlacesNearbyResponse

    companion object {
        const val BASE_URL = "https://maps.googleapis.com/maps/api/place/"
    }
}

data class GooglePlacesNearbyResponse(
    val results: List<GooglePlace>,
    val status: String,
    @SerializedName("next_page_token")
    val nextPageToken: String?
)

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

data class GooglePlaceGeometry(
    val location: GooglePlaceLatLng
)

data class GooglePlaceLatLng(
    val lat: Double,
    val lng: Double
)