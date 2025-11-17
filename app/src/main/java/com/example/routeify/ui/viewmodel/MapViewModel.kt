/*
 * ============================================================================
 * MAP VIEWMODEL - Interactive Map State Management
 * ============================================================================
 * 
 * ViewModel managing map display state, transit stops, and zoom controls.
 * Handles Google Maps integration and location-based data loading.
 * 
 * ============================================================================
 */

package com.example.routeify.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routeify.data.model.TransitStop
import com.example.routeify.data.repository.GoogleTransitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// UI state for the Map screen
data class MapUiState(
    val transitStops: List<TransitStop> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentZoom: Float = 11f
)

// ViewModel to manage map and transit data
class MapViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // Repository for fetching transit data
    private val repository = GoogleTransitRepository()
    
    // UI state exposed as StateFlow
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    init {
        // Try to read route args if provided
        val fromLat = savedStateHandle.get<String>("fromLat")?.toDoubleOrNull()
        val fromLng = savedStateHandle.get<String>("fromLng")?.toDoubleOrNull()
        val toLat = savedStateHandle.get<String>("toLat")?.toDoubleOrNull()
        val toLng = savedStateHandle.get<String>("toLng")?.toDoubleOrNull()
        val poly = savedStateHandle.get<String>("poly")
        
        // Try to read single location args (for recent destinations)
        val lat = savedStateHandle.get<String>("lat")?.toDoubleOrNull()
        val lng = savedStateHandle.get<String>("lng")?.toDoubleOrNull()

        // If valid coordinates are present, clear any error state
        if ((fromLat != null && fromLng != null && toLat != null && toLng != null) ||
            (lat != null && lng != null)) {
            _uiState.value = _uiState.value.copy(error = null)
        }

        // Initial load of transit data
        loadTransitData()
    }

    // Retrieve selected route arguments from saved state
    fun getSelectedRouteArgs(): SelectedRouteArgs? {
        val fromLat = savedStateHandle.get<String>("fromLat")?.toDoubleOrNull()
        val fromLng = savedStateHandle.get<String>("fromLng")?.toDoubleOrNull()
        val toLat = savedStateHandle.get<String>("toLat")?.toDoubleOrNull()
        val toLng = savedStateHandle.get<String>("toLng")?.toDoubleOrNull()
        val poly = savedStateHandle.get<String>("poly")?.let {
            try {
                java.net.URLDecoder.decode(it, "UTF-8")
            } catch (e: Exception) {
                it
            }
        }
        val fromName = savedStateHandle.get<String>("fromName")?.let {
            try {
                java.net.URLDecoder.decode(it, "UTF-8")
            } catch (e: Exception) {
                it
            }
        }
        val toName = savedStateHandle.get<String>("toName")?.let {
            try {
                java.net.URLDecoder.decode(it, "UTF-8")
            } catch (e: Exception) {
                it
            }
        }
        
        // Debug logging
        android.util.Log.d("MapViewModel", "Route Args - fromLat: $fromLat, fromLng: $fromLng")
        android.util.Log.d("MapViewModel", "Route Args - toLat: $toLat, toLng: $toLng")
        android.util.Log.d("MapViewModel", "Route Args - poly exists: ${poly != null}, length: ${poly?.length}")
        android.util.Log.d("MapViewModel", "Route Args - fromName: $fromName, toName: $toName")

        // Return SelectedRouteArgs if all coordinates are valid
        return if (fromLat != null && fromLng != null && toLat != null && toLng != null) {
            val result = SelectedRouteArgs(
                origin = com.google.android.gms.maps.model.LatLng(fromLat, fromLng),
                destination = com.google.android.gms.maps.model.LatLng(toLat, toLng),
                encodedPolyline = poly,
                originName = fromName,
                destinationName = toName
            )
            android.util.Log.d("MapViewModel", "Returning route args: origin=$fromLat,$fromLng dest=$toLat,$toLng")
            result
        } else {
            android.util.Log.d("MapViewModel", "Route args incomplete - returning null")
            null
        }
    }
    
    // Retrieve single location arguments from saved state
    fun getSingleLocationArgs(): SingleLocationArgs? {
        val lat = savedStateHandle.get<String>("lat")?.toDoubleOrNull()
        val lng = savedStateHandle.get<String>("lng")?.toDoubleOrNull()
        val name = savedStateHandle.get<String>("name")?.let { 
            try {
                java.net.URLDecoder.decode(it, "UTF-8")
            } catch (e: Exception) {
                it
            }
        }
        val address = savedStateHandle.get<String>("address")?.let {
            try {
                java.net.URLDecoder.decode(it, "UTF-8")
            } catch (e: Exception) {
                it
            }
        }
        
        return if (lat != null && lng != null) {
            SingleLocationArgs(
                location = com.google.android.gms.maps.model.LatLng(lat, lng),
                name = name,
                address = address
            )
        } else null
    }
    
    // Update zoom level in UI state
    fun updateZoom(zoomLevel: Float) {
        _uiState.value = _uiState.value.copy(currentZoom = zoomLevel)
    }
    
    // Load transit stops from the repository
    private fun loadTransitData() {
        // Indicate loading state
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Fetch transit stops
            repository.getTransitStops()
                .onSuccess { stops ->
                    _uiState.value = _uiState.value.copy(
                        transitStops = stops,
                        isLoading = false,
                        error = null
                    )
                }
                // Handle failure case
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        transitStops = emptyList(),
                        isLoading = false,
                        error = "Failed to load transit stops: ${exception.message}"
                    )
                }
        }
    }
}

// Data class to hold selected route arguments
data class SelectedRouteArgs(
    val origin: com.google.android.gms.maps.model.LatLng,
    val destination: com.google.android.gms.maps.model.LatLng,
    val encodedPolyline: String?,
    val originName: String? = null,
    val destinationName: String? = null
)

// Data class to hold single location arguments (for recent destinations)
data class SingleLocationArgs(
    val location: com.google.android.gms.maps.model.LatLng,
    val name: String?,
    val address: String?
)

// --------------------------------------------------End of File----------------------------------------------------------------