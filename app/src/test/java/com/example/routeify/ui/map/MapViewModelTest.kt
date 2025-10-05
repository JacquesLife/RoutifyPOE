package com.example.routeify.ui.map

import com.example.routeify.ui.viewmodel.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.After

/**
 * Unit tests for MapViewModel
 * Tests basic state management functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MapViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Note: Don't instantiate viewModel here as it triggers network calls
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateZoom should update zoom level correctly`() {
        // Arrange
        viewModel = MapViewModel()
        val newZoom = 15f

        // Act
        viewModel.updateZoom(newZoom)

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals("Zoom should be updated", newZoom, uiState.currentZoom, 0.1f)
    }

    @Test
    fun `updateZoom should accept various zoom levels`() {
        // Arrange
        viewModel = MapViewModel()
        val zoomLevels = listOf(5f, 10f, 15f, 20f)

        zoomLevels.forEach { zoomLevel ->
            // Act
            viewModel.updateZoom(zoomLevel)

            // Assert
            val uiState = viewModel.uiState.value
            assertEquals("Zoom should be updated to $zoomLevel", 
                zoomLevel, uiState.currentZoom, 0.1f)
        }
    }

    @Test
    fun `MapUiState should have correct default values`() {
        // This tests the data class directly without network calls
        
        // Arrange & Act
        val uiState = com.example.routeify.ui.viewmodel.MapUiState()

        // Assert
        assertTrue("Should start with empty transit stops", uiState.transitStops.isEmpty())
        assertFalse("Should not be loading initially", uiState.isLoading)
        assertNull("Should have no error initially", uiState.error)
        assertEquals("Should have default zoom", 11f, uiState.currentZoom, 0.1f)
    }

    @Test
    fun `MapUiState copy should work correctly`() {
        // Test the data class copy functionality
        
        // Arrange
        val originalState = com.example.routeify.ui.viewmodel.MapUiState(
            transitStops = emptyList(),
            isLoading = false,
            error = null,
            currentZoom = 12f
        )

        // Act
        val updatedState = originalState.copy(currentZoom = 15f)

        // Assert
        assertEquals("Original zoom should be unchanged", 12f, originalState.currentZoom, 0.1f)
        assertEquals("Updated zoom should be changed", 15f, updatedState.currentZoom, 0.1f)
        assertEquals("Other fields should be preserved", 
            originalState.transitStops, updatedState.transitStops)
        assertEquals("Loading state should be preserved", 
            originalState.isLoading, updatedState.isLoading)
        assertEquals("Error state should be preserved", 
            originalState.error, updatedState.error)
    }
}