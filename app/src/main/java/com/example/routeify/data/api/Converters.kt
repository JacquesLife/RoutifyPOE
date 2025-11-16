/*
 * ============================================================================
 * TYPE CONVERTERS - Room Database Type Converters
 * ============================================================================
 * 
 * Custom type converters for Room database to handle complex types.
 * Required for enums and other non-primitive types.
 * 
 * ============================================================================
 */

package com.example.routeify.data.api

import androidx.room.TypeConverter
import com.example.routeify.shared.DestinationIconType
import com.example.routeify.shared.SyncStatus

class Converters {
    
    /**
     * Convert SyncStatus enum to String for database storage
     */
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String {
        return value.name
    }
    
    /**
     * Convert String from database to SyncStatus enum
     */
    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return try {
            SyncStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            SyncStatus.LOCAL_ONLY // Default fallback
        }
    }
    
    /**
     * Convert DestinationIconType enum to String for database storage
     */
    @TypeConverter
    fun fromDestinationIconType(value: DestinationIconType): String {
        return value.name
    }
    
    /**
     * Convert String from database to DestinationIconType enum
     */
    @TypeConverter
    fun toDestinationIconType(value: String): DestinationIconType {
        return try {
            DestinationIconType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            DestinationIconType.OTHER // Default fallback
        }
    }
}

// --------------------------------------------------End of File----------------------------------------------------------------
