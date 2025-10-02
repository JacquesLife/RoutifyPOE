package com.example.routeify.data.model

import com.google.gson.annotations.SerializedName

// Data models for Cape Town Open Data API
data class MyCiTiApiResponse(
    @SerializedName("features")
    val features: List<BusStopFeature>
)

data class BusStopFeature(
    @SerializedName("attributes")
    val attributes: BusStopAttributes,
    @SerializedName("geometry")
    val geometry: BusStopGeometry
)

data class BusStopAttributes(
    @SerializedName("OBJECTID")
    val objectId: Int,
    @SerializedName("STOP_NAME")
    val stopName: String?,
    @SerializedName("STOP_TYPE")
    val stopType: String?,
    @SerializedName("STOP_STS")
    val stopStatus: String?,
    @SerializedName("RT_NMBR")
    val routeNumber: String?,
    @SerializedName("STOP_DSCR")
    val stopDescription: String?,
    @SerializedName("CMNT")
    val comments: String?,
    @SerializedName("STOP_DRCT")
    val stopDirection: String?,
    @SerializedName("ADTN_RT_NMBR")
    val additionalRouteNumber: String?
)

data class BusStopGeometry(
    @SerializedName("x")
    val x: Double,
    @SerializedName("y")
    val y: Double
)

// Convert Web Mercator to Lat/Lng
data class LatLng(val latitude: Double, val longitude: Double)

fun BusStopGeometry.toLatLng(): LatLng {
    // Convert Web Mercator (EPSG:3857) to WGS84 (EPSG:4326)
    val longitude = x * 180.0 / 20037508.34
    val latitude = kotlin.math.atan(kotlin.math.exp(y * kotlin.math.PI / 20037508.34)) * 360.0 / kotlin.math.PI - 90.0
    return LatLng(latitude, longitude)
}

// Enhanced BusStop model that works with real API data
data class RealBusStop(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val routes: List<String>,
    val stopType: StopType,
    val status: String,
    val direction: String?,
    val description: String?,
    val area: String? = null
) {
    companion object {
        fun fromApiFeature(feature: BusStopFeature): RealBusStop {
            val latLng = feature.geometry.toLatLng()
            val routes = feature.attributes.routeNumber?.split(",")?.map { it.trim() } ?: emptyList()
            val additionalRoutes = feature.attributes.additionalRouteNumber?.split(",")?.map { it.trim() } ?: emptyList()
            val allRoutes = (routes + additionalRoutes).filter { it.isNotBlank() }
            
            val stopType = when {
                feature.attributes.stopType?.contains("Station", ignoreCase = true) == true -> StopType.MAJOR_HUB
                feature.attributes.stopType?.contains("IRT", ignoreCase = true) == true -> StopType.REGULAR
                else -> StopType.REGULAR
            }
            
            return RealBusStop(
                id = feature.attributes.objectId.toString(),
                name = feature.attributes.stopName ?: "Unknown Stop",
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                routes = allRoutes,
                stopType = stopType,
                status = feature.attributes.stopStatus ?: "Unknown",
                direction = feature.attributes.stopDirection,
                description = feature.attributes.stopDescription,
                area = null // Could be derived from coordinates or added later
            )
        }
    }
}