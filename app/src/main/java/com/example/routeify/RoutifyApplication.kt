package com.example.routeify

import android.app.Application
import androidx.room.Room
import com.example.routeify.data.api.AppDatabase

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
    }
}