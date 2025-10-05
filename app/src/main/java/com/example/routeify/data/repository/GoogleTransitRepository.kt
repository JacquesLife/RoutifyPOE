package com.example.routeify.data.repository

import android.location.Location
import android.util.Log
import com.example.routeify.data.api.GoogleTransitApi
import com.example.routeify.data.api.GooglePlace
import com.example.routeify.data.model.TransitStop
import com.example.routeify.data.model.TransitStopType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GoogleTransitRepository {
    
    private val api: GoogleTransitApi by lazy {
        Retrofit.Builder()
            .baseUrl(GoogleTransitApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleTransitApi::class.java)
    }
    
    // Use the same API key as Google Maps (they're both Google Maps Platform)
    private val apiKey = com.example.routeify.BuildConfig.GOOGLE_PLACES_API_KEY

    suspend fun getTransitStops(
        centerLat: Double = -33.9249,
        centerLng: Double = 18.4241,
        radiusMeters: Int = 10000
    ): Result<List<TransitStop>> {
        return withContext(Dispatchers.IO) {
            try {
                // Validate radius (Google Places API max is 50,000 meters)
                val validRadius = radiusMeters.coerceIn(1, 50000)
                if (validRadius != radiusMeters) {
                    Log.w("GoogleTransit", "⚠️ Radius adjusted from $radiusMeters to $validRadius meters")
                }
                
                Log.d("GoogleTransit", "🚀 Starting Google Places API request...")
                Log.d("GoogleTransit", "📍 Location: $centerLat,$centerLng")
                Log.d("GoogleTransit", "📏 Search Radius: ${validRadius}m (${validRadius/1000.0}km)")
                Log.d("GoogleTransit", "🔑 API Key available: ${apiKey.take(10)}...")
                
                val location = "$centerLat,$centerLng"
                
                // Get all types of transit stops with detailed logging
                Log.d("GoogleTransit", "🚌 Fetching bus stations...")
                val busStops = api.getNearbyTransitStops(location, validRadius, "bus_station", apiKey)
                Log.d("GoogleTransit", "✅ Bus stations response: ${busStops.results.size} results, status: ${busStops.status}")
                
                Log.d("GoogleTransit", "🚇 Fetching subway stations...")
                val subwayStops = api.getNearbyTransitStops(location, validRadius, "subway_station", apiKey)
                Log.d("GoogleTransit", "✅ Subway stations response: ${subwayStops.results.size} results, status: ${subwayStops.status}")
                
                Log.d("GoogleTransit", "🚏 Fetching transit stations...")
                val transitStops = api.getNearbyTransitStops(location, validRadius, "transit_station", apiKey)
                Log.d("GoogleTransit", "✅ Transit stations response: ${transitStops.results.size} results, status: ${transitStops.status}")
                
                // Check for API errors
                listOf(busStops, subwayStops, transitStops).forEach { response ->
                    if (response.status != "OK" && response.status != "ZERO_RESULTS") {
                        Log.e("GoogleTransit", "❌ API Error: ${response.status}")
                        return@withContext Result.failure(Exception("Google Places API error: ${response.status}"))
                    }
                }
                
                // Combine all stops and convert to TransitStop
                val allApiResults = (busStops.results + subwayStops.results + transitStops.results)
                    .distinctBy { it.placeId } // Remove duplicates
                
                Log.d("GoogleTransit", "📊 Total API results before filtering: ${allApiResults.size}")
                
                val processedStops = allApiResults.mapNotNull { googlePlace ->
                    // Calculate distance from center point for verification
                    val distance = calculateDistance(
                        centerLat, centerLng,
                        googlePlace.geometry.location.lat, googlePlace.geometry.location.lng
                    )
                    
                    // Convert distance to meters for comparison
                    val distanceMeters = (distance * 1000).toInt()
                    
                    // Client-side radius filtering as backup
                    if (distanceMeters <= validRadius) {
                        Log.d("GoogleTransit", "✅ Including: ${googlePlace.name}")
                        Log.d("GoogleTransit", "   📏 Distance: ${String.format("%.2f", distance)}km (${distanceMeters}m) - WITHIN radius")
                        Log.d("GoogleTransit", "   🏷️ Types: ${googlePlace.types}")
                        
                        TransitStop(
                            id = googlePlace.placeId,
                            name = googlePlace.name,
                            latitude = googlePlace.geometry.location.lat,
                            longitude = googlePlace.geometry.location.lng,
                            stopType = determineStopType(googlePlace.types),
                            vicinity = googlePlace.vicinity,
                            rating = googlePlace.rating
                        )
                    } else {
                        Log.d("GoogleTransit", "❌ Excluding: ${googlePlace.name}")
                        Log.d("GoogleTransit", "   📏 Distance: ${String.format("%.2f", distance)}km (${distanceMeters}m) - OUTSIDE radius (${validRadius}m)")
                        null
                    }
                }
                
                Log.d("GoogleTransit", "📊 Final filtered results: ${processedStops.size} (from ${allApiResults.size} API results)")
                
                Log.d("GoogleTransit", "🎉 Successfully returning ${processedStops.size} total transit stops")
                Result.success(processedStops)
                
            } catch (e: Exception) {
                Log.e("GoogleTransit", "💥 Failed to fetch transit stops", e)
                Log.e("GoogleTransit", "💥 Error details: ${e.message}")
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
    

    private fun determineStopType(types: List<String>): TransitStopType {
        return when {
            types.contains("bus_station") -> TransitStopType.BUS_STATION
            types.contains("subway_station") -> TransitStopType.SUBWAY_STATION
            types.contains("train_station") -> TransitStopType.TRAIN_STATION
            types.contains("light_rail_station") -> TransitStopType.LIGHT_RAIL_STATION
            types.contains("transit_station") -> TransitStopType.TRANSIT_STATION
            else -> TransitStopType.BUS_STATION // Default fallback
        }
    }

    /**
     * Calculate distance between two points using Android's built-in method
     * Returns distance in kilometers
     */
    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return (results[0] / 1000.0) // Convert meters to kilometers
    }

}
