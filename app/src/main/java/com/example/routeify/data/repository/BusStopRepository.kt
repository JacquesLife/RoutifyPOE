package com.example.routeify.data.repository

import com.example.routeify.data.api.CapeTownOpenDataApi
import com.example.routeify.data.model.RealBusStop
import com.example.routeify.data.model.RailwayLine
import com.example.routeify.data.model.toRealBusStops
import com.example.routeify.data.model.toRailwayLines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BusStopRepository {
    
    // Cache for performance - avoid repeated API calls
    private var cachedBusStops: List<RealBusStop>? = null
    private var cachedRailStops: List<RealBusStop>? = null
    private var cachedRailwayLines: List<RailwayLine>? = null
    private var lastCacheTime: Long = 0
    private val cacheValidityMs = 5 * 60 * 1000 // 5 minutes
    
    private val api: CapeTownOpenDataApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://citymaps.capetown.gov.za/agsext/rest/services/Theme_Based/Open_Data_Service/FeatureServer/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CapeTownOpenDataApi::class.java)
    }
    
    /**
     * PERFORMANCE-OPTIMIZED: Load stops based on zoom with aggressive limits
     */
    suspend fun getBusStopsForZoom(zoomLevel: Float): Result<List<RealBusStop>> {
        println("🔍 Repository: Loading bus stops for zoom $zoomLevel")
        return withContext(Dispatchers.IO) {
            try {
                // Check if we can use cached data
                val now = System.currentTimeMillis()
                val isCacheValid = (now - lastCacheTime) < cacheValidityMs
                
                // BALANCED: More visible at normal zoom levels
                val busStops = when {
                    zoomLevel < 9f -> {
                        // Very low zoom: Only major hubs (railway stations)
                        println("🔍 Loading: Very low zoom - railway stations only")
                        getCachedOrFetchRailStops().take(20) // Limit even rail stops
                    }
                    zoomLevel < 11f -> {
                        // Low zoom: Major hubs + bus stops for visibility (DEFAULT ZOOM 11f)
                        println("🔍 Loading: Low zoom - 25 bus stops + 15 rail")
                        val railStops = getCachedOrFetchRailStops().take(15) // Max 15 rail stops
                        val limitedBusStops = getBusStopsWithLimit(25) // 25 bus stops for visibility
                        railStops + limitedBusStops
                    }
                    zoomLevel < 13f -> {
                        // Medium zoom: Good amount
                        println("🔍 Loading: Medium zoom - 40 bus stops + 20 rail")
                        val railStops = getCachedOrFetchRailStops().take(20) // Max 20 rail stops
                        val mediumBusStops = getBusStopsWithLimit(40) // 40 bus stops
                        railStops + mediumBusStops
                    }
                    zoomLevel < 15f -> {
                        // High zoom: More detail
                        println("🔍 Loading: High zoom - 60 bus stops + 25 rail")
                        val railStops = getCachedOrFetchRailStops().take(25) // Max 25 rail stops
                        val moreBusStops = getBusStopsWithLimit(60) // 60 bus stops
                        railStops + moreBusStops
                    }
                    else -> {
                        // Very high zoom: Maximum safe amount
                        println("🔍 Loading: Very high zoom - 80 bus stops + 30 rail")
                        val railStops = getCachedOrFetchRailStops().take(30) // Max 30 rail stops
                        val maxBusStops = getBusStopsWithLimit(80) // 80 bus stops max
                        railStops + maxBusStops
                    }
                }
                
                println("✅ Repository: Loaded ${busStops.size} total stops")
                Result.success(busStops)
            } catch (e: Exception) {
                println("❌ Repository: Error loading stops - ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    /**
     * MEMORY-OPTIMIZED: Load railway lines with aggressive limits
     */
    suspend fun getRailwayLines(): Result<List<RailwayLine>> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("BusStopRepository", "Loading railway lines...")
                // Check if we can use cached data
                val now = System.currentTimeMillis()
                val isCacheValid = (now - lastCacheTime) < cacheValidityMs
                
                val railwayLines = if (isCacheValid && cachedRailwayLines != null) {
                    android.util.Log.d("BusStopRepository", "Using cached railway lines: ${cachedRailwayLines!!.size}")
                    cachedRailwayLines!!
                } else {
                    try {
                        android.util.Log.d("BusStopRepository", "Fetching railway lines from API...")
                        val railwayLinesResponse = api.getRailwayLines()
                        android.util.Log.d("BusStopRepository", "API returned ${railwayLinesResponse.features.size} raw railway features")
                        val lines = railwayLinesResponse.toRailwayLines()
                            .take(10) // MEMORY SAFETY: Max 10 railway lines
                            .map { line ->
                                // Simplify paths to reduce memory usage
                                line.copy(
                                    paths = line.paths.map { path ->
                                        // Reduce coordinate density for memory
                                        path.filterIndexed { index, _ -> index % 2 == 0 } // Every other point
                                            .take(50) // Max 50 points per path
                                    }.filter { it.isNotEmpty() }
                                )
                            }
                        android.util.Log.d("BusStopRepository", "Processed ${lines.size} railway lines with ${lines.sumOf { it.paths.size }} total paths")
                        cachedRailwayLines = lines
                        lastCacheTime = now
                        lines
                    } catch (e: Exception) {
                        android.util.Log.e("BusStopRepository", "Failed to load railway lines: ${e.message}")
                        cachedRailwayLines ?: emptyList()
                    }
                }
                
                Result.success(railwayLines)
            } catch (e: Exception) {
                android.util.Log.e("BusStopRepository", "Error in getRailwayLines: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get railway stations with caching for performance
     */
    private suspend fun getCachedOrFetchRailStops(): List<RealBusStop> {
        val now = System.currentTimeMillis()
        val isCacheValid = (now - lastCacheTime) < cacheValidityMs
        
        return if (isCacheValid && cachedRailStops != null) {
            cachedRailStops!!
        } else {
            try {
                val railwayResponse = api.getRailwayStations(resultRecordCount = 50) // Limit rail stations too
                val railStops = railwayResponse.toRealBusStops()
                cachedRailStops = railStops
                lastCacheTime = now
                railStops
            } catch (e: Exception) {
                cachedRailStops ?: emptyList()
            }
        }
    }
    
    /**
     * Get bus stops with specific limit and caching
     */
    private suspend fun getBusStopsWithLimit(limit: Int): List<RealBusStop> {
        try {
            val busResponse = api.getBusStops(
                where = "STOP_STS='Active'", // Only active stops
                resultRecordCount = limit,
                orderByFields = "OBJECTID" // Consistent ordering
            )
            
            return busResponse.features.map { feature ->
                RealBusStop.fromApiFeature(feature)
            }.filter { stop ->
                // Filter out stops with invalid coordinates or poor names
                stop.latitude != 0.0 && 
                stop.longitude != 0.0 && 
                stop.name.isNotBlank() && 
                stop.name != "Unknown Stop"
            }
        } catch (e: Exception) {
            return emptyList()
        }
    }
    
    /**
     * Get stops within map viewport - ultimate performance optimization
     */
    suspend fun getBusStopsInViewport(
        northLat: Double,
        southLat: Double, 
        eastLng: Double,
        westLng: Double
    ): Result<List<RealBusStop>> {
        return withContext(Dispatchers.IO) {
            try {
                // Convert lat/lng bounds to Web Mercator for the API
                val geometry = "${westLng * 111319.4908},${southLat * 111319.4908},${eastLng * 111319.4908},${northLat * 111319.4908}"
                
                val response = api.getBusStopsInBounds(geometry = geometry)
                
                val busStops = response.features.map { feature ->
                    RealBusStop.fromApiFeature(feature)
                }
                
                Result.success(busStops)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Search for bus stops by name or route
     */
    suspend fun searchBusStops(query: String): Result<List<RealBusStop>> {
        return withContext(Dispatchers.IO) {
            try {
                val whereClause = "STOP_STS='Active' AND (STOP_NAME LIKE '%$query%' OR RT_NMBR LIKE '%$query%')"
                val response = api.getBusStops(
                    where = whereClause,
                    resultRecordCount = 50
                )
                
                val busStops = response.features.map { feature ->
                    RealBusStop.fromApiFeature(feature) 
                }
                
                Result.success(busStops)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}