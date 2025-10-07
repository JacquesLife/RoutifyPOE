package com.example.routeify.data.repository

import android.util.Log
import com.example.routeify.data.api.GooglePlacesExtendedApi
import com.example.routeify.domain.model.*
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

    private val apiKey = com.example.routeify.BuildConfig.GOOGLE_PLACES_API_KEY

    companion object {
        private const val TAG = "GooglePlacesEnhancedRepo"
    }

    suspend fun getPlaceAutocomplete(
        input: String,
        location: LatLng? = null,
        radiusMeters: Int = 50000
    ): Result<List<PlaceSuggestion>> = withContext(Dispatchers.IO) {
        try {
            if (input.length < 2) {
                return@withContext Result.success(emptyList())
            }

            Log.d(TAG, "Getting autocomplete for: $input")

            val locationString = location?.let { "${it.latitude},${it.longitude}" }

            val response = api.getPlaceAutocomplete(
                input = input,
                apiKey = apiKey,
                location = locationString,
                radius = radiusMeters
            )

            if (response.status == "OK") {
                val suggestions = response.predictions.map { prediction ->
                    PlaceSuggestion(
                        placeId = prediction.placeId,
                        description = prediction.description,
                        mainText = prediction.structuredFormatting?.mainText ?: prediction.description,
                        secondaryText = prediction.structuredFormatting?.secondaryText ?: ""
                    )
                }

                Log.d(TAG, "Found ${suggestions.size} suggestions")
                Result.success(suggestions)
            } else {
                Log.e(TAG, "Autocomplete API error: ${response.status}")
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting autocomplete", e)
            Result.success(emptyList())
        }
    }

    suspend fun getPlaceDetails(placeId: String): Result<LatLng> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting place details for: $placeId")

            val response = api.getPlaceDetails(
                placeId = placeId,
                apiKey = apiKey
            )

            if (response.status == "OK" && response.result != null) {
                val location = response.result.geometry.location
                val latLng = LatLng(location.lat, location.lng)

                Log.d(TAG, "Got coordinates: ${latLng.latitude}, ${latLng.longitude}")
                Result.success(latLng)
            } else {
                Result.failure(Exception("Could not get place details"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting place details", e)
            Result.failure(e)
        }
    }

    suspend fun getTransitDirections(
        origin: String,
        destination: String,
        departureTime: Long? = null,
        transitModes: String? = null,
        wheelchairAccessible: Boolean = false
    ): Result<List<TransitRoute>> = withContext(Dispatchers.IO) {
        // First try direct transit routing
        val directResult = getTransitDirectionsDirect(origin, destination, departureTime, transitModes, wheelchairAccessible)
        
        // If direct routing fails, try with nearest stops
        if (directResult.isFailure || directResult.getOrNull()?.isEmpty() == true) {
            Log.d(TAG, "Direct transit routing failed, trying with nearest stops...")
            return@withContext getTransitDirectionsWithNearestStops(origin, destination, departureTime, transitModes, wheelchairAccessible)
        }
        
        return@withContext directResult
    }

    private suspend fun getTransitDirectionsDirect(
        origin: String,
        destination: String,
        departureTime: Long? = null,
        transitModes: String? = null,
        wheelchairAccessible: Boolean = false
    ): Result<List<TransitRoute>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting direct transit directions from $origin to $destination")

            val departureTimeStr = departureTime?.let { (it / 1000).toString() } ?: "now"

            val response = api.getDirections(
                origin = origin,
                destination = destination,
                mode = "transit",
                alternatives = true,
                apiKey = apiKey,
                departureTime = departureTimeStr,
                transitMode = transitModes
            )

            if (response.status == "OK" && response.routes.isNotEmpty()) {
                val transitRoutes = response.routes.mapNotNull { route ->
                    val leg = route.legs.firstOrNull() ?: return@mapNotNull null

                    val segments = leg.steps.map { step ->
                        RouteSegment(
                            instruction = cleanHtmlInstructions(step.htmlInstructions),
                            distance = step.distance.text,
                            duration = step.duration.text,
                            travelMode = step.travelMode,
                            transitInfo = step.transitDetails?.let { details ->
                                TransitInfo(
                                    lineName = details.line.name,
                                    lineShortName = details.line.shortName,
                                    vehicleType = details.line.vehicle.type,
                                    vehicleIcon = details.line.vehicle.icon,
                                    lineColor = details.line.color,
                                    departureStop = details.departureStop.name,
                                    arrivalStop = details.arrivalStop.name,
                                    numStops = details.numStops,
                                    headsign = details.headsign
                                )
                            }
                        )
                    }

                    TransitRoute(
                        summary = route.summary,
                        totalDistance = leg.distance.text,
                        totalDuration = leg.duration.text,
                        durationValue = leg.duration.value,
                        segments = segments,
                        overviewPolyline = route.overviewPolyline?.points,
                        startLocation = com.google.android.gms.maps.model.LatLng(leg.startLocation.lat, leg.startLocation.lng),
                        endLocation = com.google.android.gms.maps.model.LatLng(leg.endLocation.lat, leg.endLocation.lng)
                    )
                }

                Log.d(TAG, "Found ${transitRoutes.size} direct transit routes")
                Result.success(transitRoutes)
            } else {
                val errorMsg = response.errorMessage ?: "No direct transit routes found"
                Log.e(TAG, "Direct directions API error: ${response.status} - $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting direct directions", e)
            Result.failure(e)
        }
    }

    private suspend fun getTransitDirectionsWithNearestStops(
        origin: String,
        destination: String,
        departureTime: Long? = null,
        transitModes: String? = null,
        wheelchairAccessible: Boolean = false
    ): Result<List<TransitRoute>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting transit directions with nearest stops from $origin to $destination")

            // First, geocode both addresses to get coordinates
            val originCoords = geocodeAddress(origin).getOrNull()
            val destinationCoords = geocodeAddress(destination).getOrNull()

            if (originCoords == null || destinationCoords == null) {
                return@withContext Result.failure(Exception("Could not find coordinates for one or both locations"))
            }

            Log.d(TAG, "Origin: ${originCoords.latitude},${originCoords.longitude}")
            Log.d(TAG, "Destination: ${destinationCoords.latitude},${destinationCoords.longitude}")

            // Find nearest transit stops
            val transitRepository = GoogleTransitRepository()
            val originStopsResult = transitRepository.findNearestTransitStops(
                originCoords.latitude, 
                originCoords.longitude, 
                maxDistanceMeters = 5000, // 1km max walk
                maxResults = 3
            )
            val destinationStopsResult = transitRepository.findNearestTransitStops(
                destinationCoords.latitude, 
                destinationCoords.longitude, 
                maxDistanceMeters = 5000, // 1km max walk
                maxResults = 3
            )

            if (originStopsResult.isFailure || destinationStopsResult.isFailure) {
                return@withContext Result.failure(Exception("Could not find nearby transit stops"))
            }

            val originStops = originStopsResult.getOrThrow()
            val destinationStops = destinationStopsResult.getOrThrow()

            if (originStops.isEmpty() || destinationStops.isEmpty()) {
                return@withContext Result.failure(Exception("No transit stops found within walking distance"))
            }

            Log.d(TAG, "Found ${originStops.size} origin stops and ${destinationStops.size} destination stops")

            // Try different combinations of stops
            val allRoutes = mutableListOf<TransitRoute>()
            
            for (originStop in originStops) {
                for (destinationStop in destinationStops) {
                    val stopOrigin = "${originStop.latitude},${originStop.longitude}"
                    val stopDestination = "${destinationStop.latitude},${destinationStop.longitude}"
                    
                    Log.d(TAG, "Trying route: ${originStop.name} -> ${destinationStop.name}")
                    
                    // Get transit route between stops
                    val transitResult = getTransitDirectionsDirect(
                        stopOrigin, 
                        stopDestination, 
                        departureTime, 
                        transitModes, 
                        wheelchairAccessible
                    )
                    
                    if (transitResult.isSuccess) {
                        val routes = transitResult.getOrThrow()
                        
                        // Add walking segments to each route (async)
                        val enhancedRoutes = mutableListOf<TransitRoute>()
                        for (route in routes) {
                            val walkingToStop = createWalkingSegment(
                                from = origin,
                                to = originStop.name,
                                fromCoords = originCoords,
                                toCoords = com.google.android.gms.maps.model.LatLng(originStop.latitude, originStop.longitude)
                            )
                            
                            val walkingFromStop = createWalkingSegment(
                                from = destinationStop.name,
                                to = destination,
                                fromCoords = com.google.android.gms.maps.model.LatLng(destinationStop.latitude, destinationStop.longitude),
                                toCoords = destinationCoords
                            )
                            
                            // Create enhanced route with walking segments
                            val enhancedRoute = route.copy(
                                segments = listOf(walkingToStop) + route.segments + walkingFromStop,
                                summary = "Walk to ${originStop.name}, then ${route.summary}, then walk to destination"
                            )
                            enhancedRoutes.add(enhancedRoute)
                        }
                        
                        allRoutes.addAll(enhancedRoutes)
                        Log.d(TAG, "✅ Found ${enhancedRoutes.size} routes via ${originStop.name} -> ${destinationStop.name}")
                    }
                }
            }

            if (allRoutes.isNotEmpty()) {
                Log.d(TAG, "🎉 Found ${allRoutes.size} total routes with nearest stops")
                Result.success(allRoutes)
            } else {
                Log.d(TAG, "❌ No routes found even with nearest stops")
                Result.failure(Exception("No transit routes found, even when including nearby stops"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error getting directions with nearest stops", e)
            Result.failure(e)
        }
    }

    private suspend fun createWalkingSegment(
        from: String,
        to: String,
        fromCoords: com.google.android.gms.maps.model.LatLng,
        toCoords: com.google.android.gms.maps.model.LatLng
    ): RouteSegment {
        // Try to get accurate walking directions from Google
        return try {
            val walkingResult = getWalkingDirections(
                "${fromCoords.latitude},${fromCoords.longitude}",
                "${toCoords.latitude},${toCoords.longitude}"
            )
            
            if (walkingResult.isSuccess) {
                val walkingResponse = walkingResult.getOrThrow()
                val route = walkingResponse.routes.firstOrNull()
                val leg = route?.legs?.firstOrNull()
                
                if (leg != null) {
                    RouteSegment(
                        instruction = "Walk from $from to $to",
                        distance = leg.distance.text,
                        duration = leg.duration.text,
                        travelMode = "WALKING"
                    )
                } else {
                    createEstimatedWalkingSegment(from, to, fromCoords, toCoords)
                }
            } else {
                createEstimatedWalkingSegment(from, to, fromCoords, toCoords)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get walking directions, using estimate", e)
            createEstimatedWalkingSegment(from, to, fromCoords, toCoords)
        }
    }

    private fun createEstimatedWalkingSegment(
        from: String,
        to: String,
        fromCoords: com.google.android.gms.maps.model.LatLng,
        toCoords: com.google.android.gms.maps.model.LatLng
    ): RouteSegment {
        // Calculate walking distance and time
        val distance = calculateDistance(fromCoords.latitude, fromCoords.longitude, toCoords.latitude, toCoords.longitude)
        val distanceMeters = (distance * 1000).toInt()
        val walkingTimeMinutes = (distanceMeters / 80.0).toInt() // Assume 80m/min walking speed
        
        return RouteSegment(
            instruction = "Walk from $from to $to",
            distance = if (distanceMeters < 1000) "${distanceMeters}m" else "${String.format("%.1f", distance)}km",
            duration = "${walkingTimeMinutes} min",
            travelMode = "WALKING"
        )
    }

    private suspend fun getWalkingDirections(
        origin: String,
        destination: String
    ): Result<com.example.routeify.data.api.GoogleDirectionsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDirections(
                origin = origin,
                destination = destination,
                mode = "walking",
                alternatives = false,
                apiKey = apiKey
            )
            
            if (response.status == "OK" && response.routes.isNotEmpty()) {
                Result.success(response)
            } else {
                Result.failure(Exception("Walking directions failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return (results[0] / 1000.0) // Convert meters to kilometers
    }

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
                Log.e(TAG, "Distance matrix API error: ${response.status}")
                Result.failure(Exception("Distance matrix API error: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting travel times", e)
            Result.failure(e)
        }
    }

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

    private fun cleanHtmlInstructions(html: String): String {
        return html
            .replace("<div[^>]*>", " ")
            .replace("</div>", " ")
            .replace("<b>", "")
            .replace("</b>", "")
            .replace("&nbsp;", " ")
            .replace(Regex("<[^>]*>"), "")
            .trim()
    }
}