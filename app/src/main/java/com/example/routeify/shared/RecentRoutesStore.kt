/*
 * ============================================================================
 * RECENT ROUTES STORE - User Route History Management
 * ============================================================================
 * 
 * Shared state store for managing recently used routes.
 * Provides quick access to frequently used routes across the app.
 * 
 * ============================================================================
 */

package com.example.routeify.shared

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Data class representing a recent route
@Entity(tableName = "recent_routes")
data class RecentRoute(
    @PrimaryKey
    val id: String,
    val fromName: String,
    val fromAddress: String,
    val fromLat: Double,
    val fromLng: Double,
    val toName: String,
    val toAddress: String,
    val toLat: Double,
    val toLng: Double,
    val polyline: String? = null,
    val duration: String? = null,
    val distance: String? = null,
    val lastUsed: Long = System.currentTimeMillis(),
    val useCount: Int = 1,
    val isFavorite: Boolean = false
) {
    fun getFromLatLng(): LatLng = LatLng(fromLat, fromLng)
    fun getToLatLng(): LatLng = LatLng(toLat, toLng)
    
    fun getRouteDescription(): String = "$fromName → $toName"
}

// Singleton object to manage recent routes
object RecentRoutesStore {
    private val _recentRoutes = MutableStateFlow<List<RecentRoute>>(emptyList())
    val recentRoutes: StateFlow<List<RecentRoute>> = _recentRoutes.asStateFlow()
    
    private const val MAX_RECENT_ROUTES = 10
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // Add a new route to recent history
    fun addRoute(
        fromName: String,
        fromAddress: String,
        fromLat: Double,
        fromLng: Double,
        toName: String,
        toAddress: String,
        toLat: Double,
        toLng: Double,
        polyline: String? = null,
        duration: String? = null,
        distance: String? = null
    ) {
        scope.launch {
            val routeId = "${fromLat},${fromLng}-${toLat},${toLng}"
            val currentList = _recentRoutes.value.toMutableList()
            
            // Check if route already exists
            val existingIndex = currentList.indexOfFirst { it.id == routeId }
            
            if (existingIndex >= 0) {
                // Update existing route
                val existing = currentList[existingIndex]
                currentList[existingIndex] = existing.copy(
                    lastUsed = System.currentTimeMillis(),
                    useCount = existing.useCount + 1,
                    polyline = polyline ?: existing.polyline,
                    duration = duration ?: existing.duration,
                    distance = distance ?: existing.distance
                )
            } else {
                // Add new route
                val newRoute = RecentRoute(
                    id = routeId,
                    fromName = fromName,
                    fromAddress = fromAddress,
                    fromLat = fromLat,
                    fromLng = fromLng,
                    toName = toName,
                    toAddress = toAddress,
                    toLat = toLat,
                    toLng = toLng,
                    polyline = polyline,
                    duration = duration,
                    distance = distance
                )
                currentList.add(0, newRoute)
                
                // Maintain max limit
                if (currentList.size > MAX_RECENT_ROUTES) {
                    currentList.removeAt(currentList.lastIndex)
                }
            }
            
            // Sort by last used
            currentList.sortByDescending { it.lastUsed }
            _recentRoutes.value = currentList
        }
    }
    
    // Clear all recent routes
    fun clearAll() {
        _recentRoutes.value = emptyList()
    }
    
    // Remove a specific route
    fun removeRoute(routeId: String) {
        scope.launch {
            val currentList = _recentRoutes.value.toMutableList()
            currentList.removeAll { it.id == routeId }
            _recentRoutes.value = currentList
        }
    }
    
    // Toggle favorite status
    fun toggleFavorite(routeId: String) {
        scope.launch {
            val currentList = _recentRoutes.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == routeId }
            if (index >= 0) {
                currentList[index] = currentList[index].copy(isFavorite = !currentList[index].isFavorite)
                _recentRoutes.value = currentList
            }
        }
    }
}
