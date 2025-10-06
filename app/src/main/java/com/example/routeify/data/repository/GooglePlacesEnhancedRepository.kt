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
        departureTime: Long? = null
    ): Result<List<TransitRoute>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting transit directions from $origin to $destination")

            val departureTimeStr = departureTime?.let { (it / 1000).toString() } ?: "now"

            val response = api.getDirections(
                origin = origin,
                destination = destination,
                mode = "transit",
                alternatives = true,
                apiKey = apiKey,
                departureTime = departureTimeStr
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
                        segments = segments
                    )
                }

                Log.d(TAG, "Found ${transitRoutes.size} transit routes")
                Result.success(transitRoutes)
            } else {
                val errorMsg = response.errorMessage ?: "No transit routes found"
                Log.e(TAG, "Directions API error: ${response.status} - $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting directions", e)
            Result.failure(e)
        }
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