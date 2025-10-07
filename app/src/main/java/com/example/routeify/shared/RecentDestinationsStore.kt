package com.example.routeify.shared

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.routeify.domain.model.PlaceSuggestion
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Data class representing a recent destination
 */
data class RecentDestination(
    val id: String,
    val name: String,
    val address: String,
    val placeId: String,
    val latitude: Double,
    val longitude: Double,
    val iconType: DestinationIconType,
    val lastVisited: Long = System.currentTimeMillis(),
    val visitCount: Int = 1
) {
    fun toPlaceSuggestion(): PlaceSuggestion {
        return PlaceSuggestion(
            placeId = placeId,
            description = "$name, $address",
            mainText = name,
            secondaryText = address
        )
    }
    
    fun toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }
}

/**
 * Icon types for different destination categories
 */
enum class DestinationIconType {
    TRAIN_STATION,
    BUS_STATION,
    AIRPORT,
    SHOPPING,
    UNIVERSITY,
    HOSPITAL,
    RESTAURANT,
    TOURIST_ATTRACTION,
    WORK,
    HOME,
    OTHER
}

/**
 * Singleton store for managing recent destinations
 */
object RecentDestinationsStore {
    private val _recentDestinations = MutableStateFlow<List<RecentDestination>>(emptyList())
    val recentDestinations: StateFlow<List<RecentDestination>> = _recentDestinations.asStateFlow()

    private const val MAX_RECENT_DESTINATIONS = 10

    init {
        // Initialize with some default destinations for demo purposes
        _recentDestinations.value = listOf(
            RecentDestination(
                id = "1",
                name = "Claremont Station",
                address = "Claremont, Cape Town",
                placeId = "ChIJClaremontStation",
                latitude = -33.9806,
                longitude = 18.4653,
                iconType = DestinationIconType.TRAIN_STATION,
                lastVisited = System.currentTimeMillis() - 86400000, // 1 day ago
                visitCount = 5
            ),
            RecentDestination(
                id = "2",
                name = "V&A Waterfront",
                address = "Victoria & Alfred Waterfront, Cape Town",
                placeId = "ChIJV&AWaterfront",
                latitude = -33.9046,
                longitude = 18.4181,
                iconType = DestinationIconType.SHOPPING,
                lastVisited = System.currentTimeMillis() - 172800000, // 2 days ago
                visitCount = 3
            ),
            RecentDestination(
                id = "3",
                name = "University of Cape Town",
                address = "Rondebosch, Cape Town",
                placeId = "ChIJUCT",
                latitude = -33.9577,
                longitude = 18.4612,
                iconType = DestinationIconType.UNIVERSITY,
                lastVisited = System.currentTimeMillis() - 259200000, // 3 days ago
                visitCount = 8
            ),
            RecentDestination(
                id = "4",
                name = "Cape Town International Airport",
                address = "Cape Town, South Africa",
                placeId = "ChIJCapeTownAirport",
                latitude = -33.9648,
                longitude = 18.6017,
                iconType = DestinationIconType.AIRPORT,
                lastVisited = System.currentTimeMillis() - 345600000, // 4 days ago
                visitCount = 2
            ),
            RecentDestination(
                id = "5",
                name = "Table Mountain",
                address = "Table Mountain National Park, Cape Town",
                placeId = "ChIJTableMountain",
                latitude = -33.9628,
                longitude = 18.4096,
                iconType = DestinationIconType.TOURIST_ATTRACTION,
                lastVisited = System.currentTimeMillis() - 432000000, // 5 days ago
                visitCount = 1
            )
        )
    }

    /**
     * Add a new destination to recent destinations
     */
    fun addDestination(destination: RecentDestination) {
        val currentList = _recentDestinations.value
        val existingIndex = currentList.indexOfFirst { it.placeId == destination.placeId }
        
        if (existingIndex >= 0) {
            // Update existing destination
            val existing = currentList[existingIndex]
            val updated = existing.copy(
                lastVisited = System.currentTimeMillis(),
                visitCount = existing.visitCount + 1
            )
            val newList = currentList.toMutableList().apply {
                removeAt(existingIndex)
                add(0, updated) // Move to top
            }
            _recentDestinations.value = newList
        } else {
            // Add new destination
            val newList = mutableListOf<RecentDestination>()
            newList.add(destination)
            newList.addAll(currentList)
            
            // Keep only the most recent destinations
            _recentDestinations.value = newList.take(MAX_RECENT_DESTINATIONS)
        }
    }

    /**
     * Add destination from PlaceSuggestion
     */
    fun addDestinationFromPlaceSuggestion(
        placeSuggestion: PlaceSuggestion,
        latitude: Double,
        longitude: Double,
        iconType: DestinationIconType = DestinationIconType.OTHER
    ) {
        val destination = RecentDestination(
            id = placeSuggestion.placeId,
            name = placeSuggestion.mainText,
            address = placeSuggestion.secondaryText,
            placeId = placeSuggestion.placeId,
            latitude = latitude,
            longitude = longitude,
            iconType = iconType
        )
        addDestination(destination)
    }

    /**
     * Get icon for destination type
     */
    fun getIconForType(iconType: DestinationIconType): androidx.compose.ui.graphics.vector.ImageVector {
        return when (iconType) {
            DestinationIconType.TRAIN_STATION -> Icons.Default.Train
            DestinationIconType.BUS_STATION -> Icons.Default.DirectionsBus
            DestinationIconType.AIRPORT -> Icons.Default.Flight
            DestinationIconType.SHOPPING -> Icons.Default.ShoppingBag
            DestinationIconType.UNIVERSITY -> Icons.Default.School
            DestinationIconType.HOSPITAL -> Icons.Default.LocalHospital
            DestinationIconType.RESTAURANT -> Icons.Default.Restaurant
            DestinationIconType.TOURIST_ATTRACTION -> Icons.Default.Landscape
            DestinationIconType.WORK -> Icons.Default.Work
            DestinationIconType.HOME -> Icons.Default.Home
            DestinationIconType.OTHER -> Icons.Default.Place
        }
    }

    /**
     * Clear all recent destinations
     */
    fun clearAll() {
        _recentDestinations.value = emptyList()
    }

    /**
     * Remove a specific destination
     */
    fun removeDestination(placeId: String) {
        _recentDestinations.value = _recentDestinations.value.filter { it.placeId != placeId }
    }
}
