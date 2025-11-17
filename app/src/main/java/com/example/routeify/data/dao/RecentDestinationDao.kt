/*
 * ============================================================================
 * RECENT DESTINATION DAO - Database Access Object
 * ============================================================================
 * 
 * Room DAO for offline storage of recent destinations and search history.
 * Provides CRUD operations with sync status tracking.
 * 
 * ============================================================================
 */

package com.example.routeify.data.dao

import androidx.room.*
import com.example.routeify.shared.RecentDestination
import com.example.routeify.shared.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentDestinationDao {
    
    /**
     * Get all recent destinations ordered by last visited (most recent first)
     */
    @Query("SELECT * FROM recent_destinations ORDER BY lastVisited DESC")
    fun getAllDestinations(): Flow<List<RecentDestination>>
    
    /**
     * Get all destinations with a specific sync status
     */
    @Query("SELECT * FROM recent_destinations WHERE syncStatus = :status ORDER BY lastVisited DESC")
    suspend fun getDestinationsByStatus(status: SyncStatus): List<RecentDestination>
    
    /**
     * Get all pending destinations that need to be synced
     */
    @Query("SELECT * FROM recent_destinations WHERE syncStatus = 'PENDING' OR syncStatus = 'LOCAL_ONLY' ORDER BY lastVisited DESC")
    suspend fun getPendingDestinations(): List<RecentDestination>
    
    /**
     * Insert a new destination
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestination(destination: RecentDestination)
    
    /**
     * Insert multiple destinations
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestinations(destinations: List<RecentDestination>)
    
    /**
     * Update an existing destination
     */
    @Update
    suspend fun updateDestination(destination: RecentDestination)
    
    /**
     * Delete a specific destination
     */
    @Delete
    suspend fun deleteDestination(destination: RecentDestination)
    
    /**
     * Delete destination by ID
     */
    @Query("DELETE FROM recent_destinations WHERE id = :destinationId")
    suspend fun deleteById(destinationId: String)
    
    /**
     * Clear all destinations
     */
    @Query("DELETE FROM recent_destinations")
    suspend fun clearAll()
    
    /**
     * Update sync status for a specific destination
     */
    @Query("UPDATE recent_destinations SET syncStatus = :status WHERE id = :destinationId")
    suspend fun updateSyncStatus(destinationId: String, status: SyncStatus)
    
    /**
     * Get count of destinations by sync status
     */
    @Query("SELECT COUNT(*) FROM recent_destinations WHERE syncStatus = :status")
    suspend fun getCountByStatus(status: SyncStatus): Int
    
    /**
     * Get total count of destinations
     */
    @Query("SELECT COUNT(*) FROM recent_destinations")
    suspend fun getTotalCount(): Int
}

// --------------------------------------------------End of File----------------------------------------------------------------
