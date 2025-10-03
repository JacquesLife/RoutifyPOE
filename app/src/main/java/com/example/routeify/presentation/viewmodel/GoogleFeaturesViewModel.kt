package com.example.routeify.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routeify.data.repository.GooglePlacesEnhancedRepository
import com.example.routeify.data.model.TransitStop
import com.example.routeify.domain.model.TravelTime
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

/**
 * ViewModel for enhanced Google Places features
 * Uses the same API infrastructure that's already working
 */
class GoogleFeaturesViewModel : ViewModel() {

    private val repository = GooglePlacesEnhancedRepository()

    // UI State
    var travelTimes = mutableStateOf<List<TravelTime>>(emptyList())
        private set
    
    var searchResults = mutableStateOf<List<TransitStop>>(emptyList())
        private set
    
    var isLoading = mutableStateOf(false)
        private set
    
    var errorMessage = mutableStateOf<String?>(null)
        private set

    /**
     * Get travel times between locations
     */
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

    /**
     * Search for transit places
     */
    fun searchTransitPlaces(query: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            
            repository.searchTransitPlaces(query)
                .onSuccess { results ->
                    searchResults.value = results
                }
                .onFailure { error ->
                    errorMessage.value = "Failed to search places: ${error.message}"
                }
            
            isLoading.value = false
        }
    }

    /**
     * Convert address to coordinates using geocoding
     */
    suspend fun geocodeAddress(address: String): LatLng? {
        return repository.geocodeAddress(address)
            .onFailure { error ->
                errorMessage.value = "Failed to find location: ${error.message}"
            }
            .getOrNull()
    }

    /**
     * Plan route between two addresses
     */
    fun planRouteWithAddresses(fromAddress: String, toAddress: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            
            try {
                // Geocode both addresses
                val fromCoords = geocodeAddress(fromAddress)
                val toCoords = geocodeAddress(toAddress)
                
                if (fromCoords != null && toCoords != null) {
                    // Get travel times
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