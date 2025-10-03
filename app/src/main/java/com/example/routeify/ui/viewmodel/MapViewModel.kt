package com.example.routeify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routeify.data.model.RealBusStop
import com.example.routeify.data.model.RailwayLine
import com.example.routeify.data.repository.BusStopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

data class MapUiState(
    val busStops: List<RealBusStop> = emptyList(),
    val railwayLines: List<RailwayLine> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentZoom: Float = 11f
)

class MapViewModel : ViewModel() {
    
    private val repository = BusStopRepository()
    
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    // Performance: Debounce rapid zoom changes
    private var loadingJob: Job? = null
    
    /**
     * MEMORY-OPTIMIZED: Load bus stops with debouncing and memory management
     */
    fun loadBusStopsForZoom(zoomLevel: Float) {
        // Cancel previous loading job to prevent overlapping requests
        loadingJob?.cancel()
        
        loadingJob = viewModelScope.launch {
            // Debounce rapid zoom changes
            delay(300) // Wait 300ms for zoom to settle
            
            // Clear previous data to free memory before loading new data
            _uiState.value = _uiState.value.copy(
                busStops = emptyList(),
                railwayLines = emptyList(),
                isLoading = true, 
                error = null,
                currentZoom = zoomLevel
            )
            
            // Force garbage collection suggestion
            System.gc()
            
            // Load both bus stops and railway lines
            val busStopsResult = repository.getBusStopsForZoom(zoomLevel)
            val railwayLinesResult = repository.getRailwayLines()
            
            busStopsResult
                .onSuccess { busStops ->
                    railwayLinesResult
                        .onSuccess { railwayLines ->
                            _uiState.value = _uiState.value.copy(
                                busStops = busStops,
                                railwayLines = railwayLines,
                                isLoading = false,
                                error = null
                            )
                        }
                        .onFailure { railwayError ->
                            // If railway lines fail, still show bus stops
                            _uiState.value = _uiState.value.copy(
                                busStops = busStops,
                                railwayLines = emptyList(),
                                isLoading = false,
                                error = "Railway lines failed to load: ${railwayError.message}"
                            )
                        }
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        busStops = emptyList(),
                        railwayLines = emptyList(),
                        isLoading = false,
                        error = "Failed to load transport data: ${exception.message}"
                    )
                }
        }
    }    /**
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