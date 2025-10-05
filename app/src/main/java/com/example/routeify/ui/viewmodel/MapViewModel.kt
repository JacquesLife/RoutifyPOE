package com.example.routeify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routeify.data.model.TransitStop
import com.example.routeify.data.repository.GoogleTransitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapUiState(
    val transitStops: List<TransitStop> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentZoom: Float = 11f
)


class MapViewModel : ViewModel() {
    
    private val repository = GoogleTransitRepository()
    
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    init {
        loadTransitData()
    }
    
    /**
     * Update zoom level - kept for camera state tracking
     */
    fun updateZoom(zoomLevel: Float) {
        _uiState.value = _uiState.value.copy(currentZoom = zoomLevel)
    }
    
    private fun loadTransitData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getTransitStops()
                .onSuccess { stops ->
                    _uiState.value = _uiState.value.copy(
                        transitStops = stops,
                        isLoading = false,
                        error = null
                    )
                }
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

