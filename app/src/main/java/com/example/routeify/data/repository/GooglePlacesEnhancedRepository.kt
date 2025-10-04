package com.example.routeify.data.repository

import android.util.Log
import com.example.routeify.data.api.GooglePlacesExtendedApi
import com.example.routeify.domain.model.TravelTime
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GooglePlacesEnhancedRepository {
    
    private val api: GooglePlacesExtendedApi by lazy {
        Retrofit.Builder()
            .baseUrl(GooglePlacesExtendedApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GooglePlacesExtendedApi::class.java)
    }
    
    // Google Places API key
    private val apiKey = com.example.routeify.BuildConfig.GOOGLE_PLACES_API_KEY
    
    companion object {
        private const val TAG = "GooglePlacesEnhancedRepository"
    }

    /**
     * Get travel times between multiple locations
     */
    suspend fun getTravelTimes(
        origins: List<LatLng>,
        destinations: List<LatLng>,
        mode: String = "transit"
    ): Result<List<TravelTime>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting travel times for ${origins.size} origins to ${destinations.size} destinations")
            
            val originsString = origins.joinToString("|") { "${it.latitude},${it.longitude}" }
            val destinationsString = destinations.joinToString("|") { "${it.latitude},${it.longitude}" }
            
            val response = api.getDistanceMatrix(
                origins = originsString,
                destinations = destinationsString,
                mode = mode,
                apiKey = apiKey
            )
            
            if (response.status == "OK") {
                val travelTimes = mutableListOf<TravelTime>()
                
                response.rows.forEachIndexed { originIndex, row ->
                    row.elements.forEachIndexed { destIndex, element ->
                        if (element.status == "OK") {
                            val travelTime = TravelTime(
                                origin = origins[originIndex],
                                destination = destinations[destIndex],
                                originAddress = response.originAddresses.getOrNull(originIndex) ?: "",
                                destinationAddress = response.destinationAddresses.getOrNull(destIndex) ?: "",
                                distance = element.distance?.text ?: "Unknown",
                                distanceValue = element.distance?.value ?: 0,
                                duration = element.duration?.text ?: "Unknown",
                                durationValue = element.duration?.value ?: 0,
                                mode = mode
                            )
                            travelTimes.add(travelTime)
                        }
                    }
                }
                
                Log.d(TAG, "Successfully retrieved ${travelTimes.size} travel times")
                Result.success(travelTimes)
            } else {
                Log.e(TAG, "Distance matrix API error: ${response.status} - ${response.errorMessage}")
                Result.failure(Exception("Distance matrix API error: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting travel times", e)
            Result.failure(e)
        }
    }

    /**
     * Get travel time between a single origin and destination
     */
    suspend fun getQuickTravelTime(
        origin: LatLng,
        destination: LatLng
    ): Result<TravelTime> {
        return getTravelTimes(
            origins = listOf(origin),
            destinations = listOf(destination)
        ).map { travelTimes ->
            travelTimes.firstOrNull() ?: throw Exception("No travel time found")
        }
    }

    /**
     * Convert address to coordinates using Google Geocoding API
     */
    suspend fun geocodeAddress(address: String): Result<LatLng> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Geocoding address: $address")
            
            val response = api.geocodeAddress(
                address = address,
                apiKey = apiKey
            )
            
            if (response.status == "OK" && response.results.isNotEmpty()) {
                val location = response.results.first().geometry.location
                val latLng = LatLng(location.lat, location.lng)
                
                Log.d(TAG, "Geocoded $address to ${latLng.latitude}, ${latLng.longitude}")
                Result.success(latLng)
            } else {
                val error = response.errorMessage ?: "No results found for address: $address"
                Log.e(TAG, "Geocoding failed: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error geocoding address: $address", e)
            Result.failure(e)
        }
    }
}