/*
 * ============================================================================
 * RECENT DESTINATIONS STORE - User History Management
 * ============================================================================
 * 
 * Shared state store for managing recently visited places and destinations.
 * Provides quick access to frequently used locations across the app.
 * 
 * ============================================================================
 */

package com.example.routeify.shared

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.routeify.RoutifyApplication
import com.example.routeify.data.api.AppDatabase
import com.example.routeify.data.dao.RecentDestinationDao
import com.example.routeify.data.sync.SyncManager
import com.example.routeify.domain.model.PlaceSuggestion
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Sync status for offline mode
enum class SyncStatus {
    SYNCED,      // Data is synced to cloud/server
    PENDING,     // Needs sync when online
    LOCAL_ONLY   // Never needs sync (local-only data)
}

// Data class representing a recent destination
@Entity(tableName = "recent_destinations")
data class RecentDestination(
    @PrimaryKey
    val id: String,
    val name: String,
    val address: String,
    val placeId: String,
    val latitude: Double,
    val longitude: Double,
    val iconType: DestinationIconType,
    val lastVisited: Long = System.currentTimeMillis(),
    val visitCount: Int = 1,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val isFavorite: Boolean = false
) {
    // Convert to PlaceSuggestion
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

// Enum for different types of destination icons
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

// Singleton object to manage recent destinations
object RecentDestinationsStore {
    private lateinit var database: AppDatabase
    private lateinit var dao: RecentDestinationDao
    private lateinit var syncManager: SyncManager
    private var isInitialized = false
    
    private val _recentDestinations = MutableStateFlow<List<RecentDestination>>(emptyList())
    val recentDestinations: StateFlow<List<RecentDestination>> = _recentDestinations.asStateFlow()
    
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private const val MAX_RECENT_DESTINATIONS = 10
    private val scope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Initialize the store with database and sync manager
     * Must be called before using the store
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        val app = context.applicationContext as RoutifyApplication
        database = app.database
        dao = database.recentDestinationDao()
        syncManager = SyncManager(context)
        
        // Load from database
        scope.launch {
            dao.getAllDestinations().collect { destinations ->
                _recentDestinations.value = destinations
            }
        }
        
        // Monitor connectivity
        scope.launch {
            syncManager.isOnline.collect { isOnline ->
                _isOfflineMode.value = !isOnline
                if (isOnline) {
                    // Sync pending destinations when back online
                    syncPendingDestinations()
                }
            }
        }
        
        isInitialized = true
    }
    
    /**
     * Sync destinations with PENDING status
     */
    private suspend fun syncPendingDestinations() {
        try {
            val pending = dao.getPendingDestinations()
            if (pending.isNotEmpty()) {
                syncManager.syncPendingData()
                
                // Mark as synced after successful sync
                pending.forEach { destination ->
                    dao.updateSyncStatus(destination.id, SyncStatus.SYNCED)
                }
            }
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }
    }
    
    // Update offline mode status
    fun setOfflineMode(isOffline: Boolean) {
        _isOfflineMode.value = isOffline
    }

    // Initialize with some dummy data for demonstration
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
            // Second recent destination
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
            // Additional dummy destinations
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
            // Fourth recent destination
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
            // Fifth recent destination
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

    // Add or update a recent destination
    fun addDestination(destination: RecentDestination) {
        scope.launch {
            val currentList = _recentDestinations.value
            val existingIndex = currentList.indexOfFirst { it.placeId == destination.placeId }
            
            if (existingIndex >= 0) {
                // Update existing destination
                val existing = currentList[existingIndex]
                val updated = existing.copy(
                    lastVisited = System.currentTimeMillis(),
                    visitCount = existing.visitCount + 1,
                    syncStatus = if (_isOfflineMode.value) SyncStatus.PENDING else SyncStatus.SYNCED
                )
                dao.updateDestination(updated)
            } else {
                // Add new destination
                val newDestination = destination.copy(
                    syncStatus = if (_isOfflineMode.value) SyncStatus.PENDING else SyncStatus.SYNCED
                )
                dao.insertDestination(newDestination)
                
                // Maintain max limit
                val totalCount = dao.getTotalCount()
                if (totalCount > MAX_RECENT_DESTINATIONS) {
                    // Remove oldest destinations
                    val allDestinations = dao.getDestinationsByStatus(SyncStatus.SYNCED)
                    val toRemove = allDestinations.drop(MAX_RECENT_DESTINATIONS)
                    toRemove.forEach { dao.deleteDestination(it) }
                }
            }
        }
    }

    // Add destination from PlaceSuggestion
    fun addDestinationFromPlaceSuggestion(
        placeSuggestion: PlaceSuggestion,
        latitude: Double,
        longitude: Double,
        iconType: DestinationIconType = DestinationIconType.OTHER
    ) {
        // Create RecentDestination from PlaceSuggestion
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

    // Get icon for a destination type
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
        scope.launch {
            dao.clearAll()
        }
    }

    /**
     * Remove a specific destination
     */
    fun removeDestination(placeId: String) {
        scope.launch {
            dao.deleteById(placeId)
        }
    }
    
    /**
     * Get destinations that need syncing
     */
    fun getPendingSyncDestinations(): List<RecentDestination> {
        return _recentDestinations.value.filter { it.syncStatus == SyncStatus.PENDING }
    }
    
    /**
     * Mark destinations as synced
     */
    fun markAsSynced(placeIds: List<String>) {
        scope.launch {
            placeIds.forEach { placeId ->
                dao.updateSyncStatus(placeId, SyncStatus.SYNCED)
            }
        }
    }
    
    /**
     * Toggle favorite status for a destination
     */
    fun toggleFavorite(placeId: String) {
        scope.launch {
            val destinations = _recentDestinations.value
            val destination = destinations.find { it.placeId == placeId }
            destination?.let {
                val updated = it.copy(
                    isFavorite = !it.isFavorite,
                    syncStatus = if (it.syncStatus == SyncStatus.SYNCED) SyncStatus.PENDING else it.syncStatus
                )
                dao.updateDestination(updated)
            }
        }
    }
    
    /**
     * Get sync manager for UI integration
     */
    fun getSyncManager(): SyncManager {
        check(isInitialized) { "RecentDestinationsStore must be initialized before use" }
        return syncManager
    }
}

// --------------------------------------------------End of File----------------------------------------------------------------