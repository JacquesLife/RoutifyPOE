package com.example.routeify.data.repository

import com.example.routeify.data.api.CapeTownOpenDataApi
import com.example.routeify.data.model.RealBusStop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BusStopRepository {
    
    private val api: CapeTownOpenDataApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://citymaps.capetown.gov.za/agsext/rest/services/Theme_Based/Open_Data_Service/FeatureServer/96/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CapeTownOpenDataApi::class.java)
    }
    
    /**
     * Smart loading based on zoom level - solves the 770 stops problem!
     */
    suspend fun getBusStopsForZoom(zoomLevel: Float): Result<List<RealBusStop>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = when {
                    zoomLevel < 12f -> {
                        // Low zoom: Get only major stations/hubs
                        api.getMajorHubs()
                    }
                    zoomLevel < 15f -> {
                        // Medium zoom: Get limited number of stops
                        api.getBusStops(resultRecordCount = 50)
                    }
                    else -> {
                        // High zoom: Get more stops but still manageable
                        api.getBusStops(resultRecordCount = 150)
                    }
                }
                
                val busStops = response.features.map { feature ->
                    RealBusStop.fromApiFeature(feature)
                }.filter { stop ->
                    // Filter out stops with invalid coordinates
                    stop.latitude != 0.0 && stop.longitude != 0.0
                }
                
                Result.success(busStops)
            } catch (e: Exception) {
                Result.failure(e)
            }
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