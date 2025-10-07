/*
 * ============================================================================
 * APP DATABASE - Room Database Configuration
 * ============================================================================
 * 
 * This file defines the main Room database for the Routeify application.
 * 
 * PRIMARY PURPOSE:
 * - Configures the SQLite database using Room persistence library
 * - Manages user authentication and profile data storage
 * - Provides centralized database access point for the application
 * 
 * KEY FEATURES:
 * - User entity management with DAO pattern
 * - Database versioning and migration support
 * - Schema export for database evolution tracking
 * - Thread-safe database operations
 * 
 * ENTITIES MANAGED:
 * - User: Stores user authentication and profile information
 * 
 * USAGE:
 * This database is initialized once per app lifecycle and provides
 * access to user data throughout the application via the UserDao.
 * 
 * ============================================================================
 */

package com.example.routeify.data.api

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.routeify.data.model.User

// The AppDatabase class for Room database
@Database(
    entities = [User::class],
    version = 2,
    exportSchema = true
)

// Abstract class extending RoomDatabase
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}


// --------------------------------------------------End of File----------------------------------------------------------------