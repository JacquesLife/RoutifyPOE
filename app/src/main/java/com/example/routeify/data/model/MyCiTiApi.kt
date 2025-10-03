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

// Railway Station Data Models
data class RailwayApiResponse(
    @SerializedName("features")
    val features: List<RailwayStationFeature>
)

data class RailwayStationFeature(
    @SerializedName("attributes")
    val attributes: RailwayStationAttributes,
    @SerializedName("geometry")
    val geometry: BusStopGeometry // Same geometry structure as bus stops
)

data class RailwayStationAttributes(
    @SerializedName("OBJECTID")
    val objectId: Int,
    @SerializedName("STN_NMBR")
    val stationNumber: String?,
    @SerializedName("NAME")
    val name: String?,
    @SerializedName("STN_NAME")
    val stationName: String?,
    @SerializedName("SBRB")
    val suburb: String?,
    @SerializedName("ALL_DY_SE")
    val allDayService: Int?,
    @SerializedName("PK_SRVC")
    val peakService: Int?,
    @SerializedName("CM_SRVC")
    val commuterService: Int?,
    @SerializedName("WC_STS")
    val westernCapeStatus: String?,
    @SerializedName("CMTR_STS")
    val commuterStatus: String?,
    @SerializedName("OFC")
    val hasOffice: String?,
    @SerializedName("MNT_AUTH")
    val maintenanceAuthority: String?,
    @SerializedName("OWNR")
    val owner: String?
)

// Railway Lines Data Models
data class RailwayLinesApiResponse(
    @SerializedName("features")
    val features: List<RailwayLineFeature>
)

data class RailwayLineFeature(
    @SerializedName("attributes")
    val attributes: RailwayLineAttributes,
    @SerializedName("geometry")
    val geometry: RailwayLineGeometry
)

data class RailwayLineAttributes(
    @SerializedName("OBJECTID")
    val objectId: Int,
    @SerializedName("NAME")
    val name: String?,
    @SerializedName("TYPE")
    val type: String?,
    @SerializedName("STATUS")
    val status: String?
)

data class RailwayLineGeometry(
    @SerializedName("paths")
    val paths: List<List<List<Double>>>
)

// Railway line data structure for map display
data class RailwayLine(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val paths: List<List<com.google.android.gms.maps.model.LatLng>>
)

// Extension function to convert railway lines API response to RailwayLine objects
fun RailwayLinesApiResponse.toRailwayLines(): List<RailwayLine> {
    android.util.Log.d("RailwayConversion", "Converting ${features.size} raw railway features")
    return features.mapNotNull { feature ->
        val attrs = feature.attributes
        val geometry = feature.geometry
        
        android.util.Log.d("RailwayConversion", "Feature: name=${attrs.name}, paths=${geometry.paths.size}")
        
        if (attrs.name != null && geometry.paths.isNotEmpty()) {
            val convertedPaths = geometry.paths.map { path ->
                android.util.Log.d("RailwayConversion", "Converting path with ${path.size} coordinates")
                path.map { coord ->
                    if (coord.size >= 2) {
                        // Convert Web Mercator to WGS84
                        val longitude = coord[0] / 20037508.34 * 180
                        val latitude = 180 / Math.PI * (2 * Math.atan(Math.exp(coord[1] / 20037508.34 * Math.PI)) - Math.PI / 2)
                        
                        // Check if coordinates are in Cape Town area
                        if (latitude in -35.0..-33.0 && longitude in 18.0..19.0) {
                            com.google.android.gms.maps.model.LatLng(latitude, longitude)
                        } else {
                            android.util.Log.w("RailwayConversion", "Invalid coordinates: lat=$latitude, lng=$longitude")
                            null
                        }
                    } else {
                        null
                    }
                }.filterNotNull()
            }.filter { it.isNotEmpty() }
            
            if (convertedPaths.isNotEmpty()) {
                android.util.Log.d("RailwayConversion", "Created railway line: ${attrs.name} with ${convertedPaths.size} paths")
                RailwayLine(
                    id = "rail_line_${attrs.objectId}",
                    name = attrs.name,
                    type = attrs.type ?: "Railway",
                    status = attrs.status ?: "Active",
                    paths = convertedPaths
                )
            } else {
                android.util.Log.w("RailwayConversion", "No valid paths for ${attrs.name}")
                null
            }
        } else {
            android.util.Log.w("RailwayConversion", "Skipping feature: name=${attrs.name}, paths=${geometry.paths.size}")
            null
        }
    }
}
fun RailwayApiResponse.toRealBusStops(): List<RealBusStop> {
    return features.mapNotNull { feature ->
        val geometry = feature.geometry
        val attrs = feature.attributes
        
        // Convert Web Mercator coordinates to WGS84
        val longitude = geometry.x / 20037508.34 * 180
        val latitude = 180 / Math.PI * (2 * Math.atan(Math.exp(geometry.y / 20037508.34 * Math.PI)) - Math.PI / 2)
        
        // Only include stations with valid names
        val stationName = attrs.stationName ?: attrs.name
        if (stationName != null && latitude != 0.0 && longitude != 0.0) {
            RealBusStop(
                id = "rail_${attrs.objectId}",
                name = stationName,
                latitude = latitude,
                longitude = longitude,
                routes = buildList {
                    // Add service type information as "routes"
                    when {
                        attrs.allDayService == 1 -> add("All Day Service")
                        attrs.peakService == 1 -> add("Peak Service") 
                        attrs.commuterService == 1 -> add("Commuter Service")
                    }
                    if (attrs.hasOffice == "Yes") add("Service Office")
                },
                status = attrs.commuterStatus ?: attrs.westernCapeStatus ?: "Active",
                stopType = StopType.RAILWAY, // Railway stations
                direction = null,
                description = buildString {
                    if (attrs.suburb != null) append("${attrs.suburb} ")
                    append("Railway Station")
                    if (attrs.maintenanceAuthority != null) append(" (${attrs.maintenanceAuthority})")
                },
                area = attrs.suburb
            )
        } else null
    }
}