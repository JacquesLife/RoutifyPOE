/*
 * ============================================================================
 * ROUTEIFY APPLICATION - App Initialization & Global Configuration
 * ============================================================================
 * 
 * Main application class handling global app initialization.
 * Configures Room database and provides app-wide dependencies.
 * 
 * ============================================================================
 */

package com.example.routeify

import android.app.Application
import androidx.room.Room
import com.example.routeify.data.api.AppDatabase
import com.example.routeify.shared.RecentDestinationsStore

// Main application class initializing the Room database
class RoutifyApplication : Application() {
    lateinit var database: AppDatabase
        private set

// Initialize the Room database when the application is created
    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "routeify.db"
        ).fallbackToDestructiveMigration().build()
        
        // Initialize offline mode store
        RecentDestinationsStore.initialize(this)
    }
}

// --------------------------------------------------End of File----------------------------------------------------------------