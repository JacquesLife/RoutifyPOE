package com.example.routeify.data.api

import com.example.routeify.data.model.MyCiTiApiResponse
import com.example.routeify.data.model.RailwayApiResponse
import com.example.routeify.data.model.RailwayLinesApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CapeTownOpenDataApi {
    
    /**
     * Get MyCiTi bus stops from Cape Town Open Data API
     * 
     * @param where SQL-like query to filter results (default: get all active stops)
     * @param outFields Which fields to return (* = all fields)
     * @param returnGeometry Whether to include coordinate geometry
     * @param format Response format (json, geojson, etc.)
     * @param resultRecordCount Maximum number of records to return
     * @param resultOffset Starting record offset for pagination
     * @param orderByFields Fields to order results by
     */
    @GET("96/query")
    suspend fun getBusStops(
        @Query("where") where: String = "STOP_STS='Active'",
        @Query("outFields") outFields: String = "*",
        @Query("returnGeometry") returnGeometry: Boolean = true,
        @Query("f") format: String = "json",
        @Query("resultRecordCount") resultRecordCount: Int = 100,
        @Query("resultOffset") resultOffset: Int = 0,
        @Query("orderByFields") orderByFields: String? = null
    ): MyCiTiApiResponse
    
    /**
     * Get bus stops within a specific geographic boundary (viewport-based loading)
     */
    @GET("96/query")
    suspend fun getMajorHubs(
        @Query("where") where: String = "STOP_STS='Active' AND STOP_TYPE='IRT Station'",
        @Query("outFields") outFields: String = "*", 
        @Query("returnGeometry") returnGeometry: Boolean = true,
        @Query("f") format: String = "json"
    ): MyCiTiApiResponse
    
    /**
     * Get railway stations from Cape Town Open Data API (Layer 235)
     * 
     * @param where SQL-like query to filter results
     * @param outFields Which fields to return (* = all fields)
     * @param returnGeometry Whether to include coordinate geometry
     * @param format Response format (json, geojson, etc.)
     * @param resultRecordCount Maximum number of records to return
     * @param resultOffset Starting record offset for pagination
     */
    @GET("235/query")
    suspend fun getRailwayStations(
        @Query("where") where: String = "1=1", // Get all railway stations
        @Query("outFields") outFields: String = "*",
        @Query("returnGeometry") returnGeometry: Boolean = true,
        @Query("f") format: String = "json",
        @Query("resultRecordCount") resultRecordCount: Int = 100,
        @Query("resultOffset") resultOffset: Int = 0
    ): RailwayApiResponse
    
    /**
     * Get railway lines from Cape Town Open Data API (Layer 233)
     * 
     * @param where SQL-like query to filter results
     * @param outFields Which fields to return (* = all fields)
     * @param returnGeometry Whether to include coordinate geometry
     * @param format Response format (json, geojson, etc.)
     */
    @GET("233/query")
    suspend fun getRailwayLines(
        @Query("where") where: String = "1=1", // Get all railway lines
        @Query("outFields") outFields: String = "*",
        @Query("returnGeometry") returnGeometry: Boolean = true,
        @Query("f") format: String = "json"
    ): RailwayLinesApiResponse
}