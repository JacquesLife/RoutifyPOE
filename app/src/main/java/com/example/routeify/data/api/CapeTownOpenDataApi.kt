package com.example.routeify.data.api

import com.example.routeify.data.model.MyCiTiApiResponse
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
    @GET("query")
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
    @GET("query") 
    suspend fun getBusStopsInBounds(
        @Query("where") where: String = "STOP_STS='Active'",
        @Query("geometry") geometry: String, // Format: xmin,ymin,xmax,ymax
        @Query("geometryType") geometryType: String = "esriGeometryEnvelope",
        @Query("spatialRel") spatialRel: String = "esriSpatialRelIntersects",
        @Query("outFields") outFields: String = "*",
        @Query("returnGeometry") returnGeometry: Boolean = true,
        @Query("f") format: String = "json"
    ): MyCiTiApiResponse
    
    /**
     * Get major transport hubs only (for low zoom levels)
     */
    @GET("query")
    suspend fun getMajorHubs(
        @Query("where") where: String = "STOP_STS='Active' AND STOP_TYPE='IRT Station'",
        @Query("outFields") outFields: String = "*", 
        @Query("returnGeometry") returnGeometry: Boolean = true,
        @Query("f") format: String = "json"
    ): MyCiTiApiResponse
}