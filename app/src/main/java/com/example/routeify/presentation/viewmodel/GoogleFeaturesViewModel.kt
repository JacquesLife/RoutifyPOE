package com.example.routeify.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routeify.data.repository.GooglePlacesEnhancedRepository
import com.example.routeify.data.repository.GoogleTransitRepository
import com.example.routeify.data.model.TransitStop
import com.example.routeify.domain.model.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GoogleFeaturesViewModel : ViewModel() {

    private val repository = GooglePlacesEnhancedRepository()
    private val transitRepository = GoogleTransitRepository()

    var travelTimes = mutableStateOf<List<TravelTime>>(emptyList())
        private set

    var searchResults = mutableStateOf<List<TransitStop>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    var fromSuggestions = mutableStateOf<List<PlaceSuggestion>>(emptyList())
        private set

    var toSuggestions = mutableStateOf<List<PlaceSuggestion>>(emptyList())
        private set

    var isLoadingFromSuggestions = mutableStateOf(false)
        private set

    var isLoadingToSuggestions = mutableStateOf(false)
        private set

    var transitRoutes = mutableStateOf<List<TransitRoute>>(emptyList())
        private set

    var isLoadingRoutes = mutableStateOf(false)
        private set

    private var autocompleteJob: Job? = null

    fun getFromAutocompleteSuggestions(input: String) {
        autocompleteJob?.cancel()

        if (input.length < 2) {
            fromSuggestions.value = emptyList()
            return
        }

        autocompleteJob = viewModelScope.launch {
            delay(300)
            isLoadingFromSuggestions.value = true

            repository.getPlaceAutocomplete(
                input = input,
                location = LatLng(-33.9249, 18.4241)
            )
                .onSuccess { suggestions ->
                    fromSuggestions.value = suggestions
                }
                .onFailure {
                    fromSuggestions.value = emptyList()
                }

            isLoadingFromSuggestions.value = false
        }
    }

    fun getToAutocompleteSuggestions(input: String) {
        autocompleteJob?.cancel()

        if (input.length < 2) {
            toSuggestions.value = emptyList()
            return
        }

        autocompleteJob = viewModelScope.launch {
            delay(300)
            isLoadingToSuggestions.value = true

            repository.getPlaceAutocomplete(
                input = input,
                location = LatLng(-33.9249, 18.4241)
            )
                .onSuccess { suggestions ->
                    toSuggestions.value = suggestions
                }
                .onFailure {
                    toSuggestions.value = emptyList()
                }

            isLoadingToSuggestions.value = false
        }
    }

    fun clearFromSuggestions() {
        fromSuggestions.value = emptyList()
    }

    fun clearToSuggestions() {
        toSuggestions.value = emptyList()
    }

    fun getTransitRoutes(fromLocation: String, toLocation: String) {
        viewModelScope.launch {
            isLoadingRoutes.value = true
            isLoading.value = true
            errorMessage.value = null
            transitRoutes.value = emptyList()

            repository.getTransitDirections(
                origin = fromLocation,
                destination = toLocation
            )
                .onSuccess { routes ->
                    transitRoutes.value = routes
                    if (routes.isEmpty()) {
                        errorMessage.value = "No transit routes found. Try different locations or check if transit is available."
                    }
                }
                .onFailure { error ->
                    errorMessage.value = "Failed to find routes: ${error.message}"
                    transitRoutes.value = emptyList()
                }

            isLoadingRoutes.value = false
            isLoading.value = false
        }
    }

    fun getTravelTimes(origins: List<LatLng>, destinations: List<LatLng>) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            repository.getTravelTimes(origins, destinations)
                .onSuccess { times ->
                    travelTimes.value = times
                }
                .onFailure { error ->
                    errorMessage.value = "Failed to get travel times: ${error.message}"
                }

            isLoading.value = false
        }
    }

    fun searchTransitPlaces(query: String, radiusMeters: Int = 5000) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            transitRepository.getTransitStops(
                centerLat = -33.9032,
                centerLng = 18.4168,
                radiusMeters = radiusMeters
            )
                .onSuccess { results ->
                    searchResults.value = results
                }
                .onFailure { error ->
                    errorMessage.value = "Failed to search places: ${error.message}"
                }

            isLoading.value = false
        }
    }

    suspend fun geocodeAddress(address: String): LatLng? {
        return repository.geocodeAddress(address)
            .onFailure { error ->
                errorMessage.value = "Failed to find location: ${error.message}"
            }
            .getOrNull()
    }

    fun planRouteWithAddresses(fromAddress: String, toAddress: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val fromCoords = geocodeAddress(fromAddress)
                val toCoords = geocodeAddress(toAddress)

                if (fromCoords != null && toCoords != null) {
                    getTravelTimes(
                        origins = listOf(fromCoords),
                        destinations = listOf(toCoords)
                    )
                } else {
                    errorMessage.value = "Could not find one or both locations"
                    isLoading.value = false
                }
            } catch (e: Exception) {
                errorMessage.value = "Error planning route: ${e.message}"
                isLoading.value = false
            }
        }
    }
}