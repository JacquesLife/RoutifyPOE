package com.example.routeify.data.repository

import android.util.Log
import com.example.routeify.data.api.GooglePlacesExtendedApi
import com.example.routeify.domain.model.TravelTime
import com.example.routeify.data.model.TransitStop
import com.example.routeify.data.model.TransitStopType
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
     * Search for transit places by text query
     * Returns sample Cape Town transit stops for demo
     */
    suspend fun searchTransitPlaces(
        query: String,
        location: LatLng? = null
    ): Result<List<TransitStop>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Searching for transit places: $query")
            
            // Return sample Cape Town transit stops
            val transitStops = listOf(
                TransitStop(
                    id = "cape_town_station",
                    name = "Cape Town Railway Station",
                    latitude = -33.9175,
                    longitude = 18.4292,
                    stopType = TransitStopType.TRAIN_STATION,
                    vicinity = "Strand Street, Cape Town City Centre",
                    rating = 4.1
                ),
                TransitStop(
                    id = "civic_centre_station",
                    name = "Civic Centre Station", 
                    latitude = -33.9198,
                    longitude = 18.4234,
                    stopType = TransitStopType.BUS_STATION,
                    vicinity = "Hertzog Boulevard, Cape Town",
                    rating = 3.8
                ),
                TransitStop(
                    id = "waterfront_shuttle",
                    name = "V&A Waterfront Shuttle Stop",
                    latitude = -33.9032,
                    longitude = 18.4168,
                    stopType = TransitStopType.BUS_STATION,
                    vicinity = "V&A Waterfront, Cape Town",
                    rating = 4.3
                ),
                TransitStop(
                    id = "greenmarket_square_stop",
                    name = "Greenmarket Square Bus Stop",
                    latitude = -33.9226,
                    longitude = 18.4193,
                    stopType = TransitStopType.BUS_STATION,
                    vicinity = "Greenmarket Square, Cape Town City Centre", 
                    rating = 3.9
                ),
                TransitStop(
                    id = "bellville_station",
                    name = "Bellville Railway Station",
                    latitude = -33.8953,
                    longitude = 18.6224,
                    stopType = TransitStopType.TRAIN_STATION,
                    vicinity = "Charlie Hofmeyr Street, Bellville",
                    rating = 4.0
                )
            )
            
            Log.d(TAG, "Found ${transitStops.size} transit stops")
            Result.success(transitStops)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error searching transit places", e)
            Result.failure(e)
        }
    }

    suspend fun getQuickTravelTime(
        origin: LatLng,
        destination: LatLng
    ): Result<TravelTime> = withContext(Dispatchers.IO) {
        try {
            val result = getTravelTimes(
                origins = listOf(origin),
                destinations = listOf(destination)
            )
            
            result.map { travelTimes ->
                travelTimes.firstOrNull() ?: throw Exception("No travel time found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting quick travel time", e)
            Result.failure(e)
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