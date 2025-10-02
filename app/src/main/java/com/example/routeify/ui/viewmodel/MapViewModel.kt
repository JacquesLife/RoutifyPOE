package com.example.routeify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routeify.data.model.RealBusStop
import com.example.routeify.data.repository.BusStopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapUiState(
    val busStops: List<RealBusStop> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentZoom: Float = 11f
)

class MapViewModel : ViewModel() {
    
    private val repository = BusStopRepository()
    
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    /**
     * Load bus stops based on current zoom level
     */
    fun loadBusStopsForZoom(zoomLevel: Float) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                error = null,
                currentZoom = zoomLevel
            )
            
            repository.getBusStopsForZoom(zoomLevel)
                .onSuccess { busStops ->
                    _uiState.value = _uiState.value.copy(
                        busStops = busStops,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        busStops = emptyList(),
                        isLoading = false,
                        error = "Failed to load bus stops: ${exception.message}"
                    )
                }
        }
    }
    
    /**
     * Load bus stops within map viewport for better performance
     */
    fun loadBusStopsInViewport(
        northLat: Double,
        southLat: Double,
        eastLng: Double,
        westLng: Double
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getBusStopsInViewport(northLat, southLat, eastLng, westLng)
                .onSuccess { busStops ->
                    _uiState.value = _uiState.value.copy(
                        busStops = busStops,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        busStops = emptyList(),
                        isLoading = false,
                        error = "Failed to load bus stops: ${exception.message}"
                    )
                }
        }
    }
    
    /**
     * Search for bus stops
     */
    fun searchBusStops(query: String) {
        if (query.isBlank()) {
            loadBusStopsForZoom(_uiState.value.currentZoom)
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.searchBusStops(query)
                .onSuccess { busStops ->
                    _uiState.value = _uiState.value.copy(
                        busStops = busStops,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        busStops = emptyList(),
                        isLoading = false,
                        error = "Search failed: ${exception.message}"
                    )
                }
        }
    }
    
    /**
     * Clear any errors
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}