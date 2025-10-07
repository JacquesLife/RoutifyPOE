/*
 * ============================================================================
 * USER DAO - Data Access Object for User Management
 * ============================================================================
 * 
 * This file defines the Data Access Object (DAO) interface for user operations
 * in the Room database.
 * 
 * PRIMARY PURPOSE:
 * - Provides type-safe database operations for User entities
 * - Handles user authentication queries and data persistence
 * - Manages user profile creation, retrieval, and updates
 * 
 * KEY OPERATIONS:
 * - Insert new users with conflict resolution strategy
 * - Query users by email for authentication
 * - Retrieve all users for admin/debugging purposes
 * - Thread-safe database transactions
 * 
 * AUTHENTICATION FLOW:
 * - insertUser(): Creates new user accounts during registration
 * - getUserByEmail(): Validates user credentials during login
 * - getAllUsers(): Administrative access to user database
 * 
 * USAGE:
 * This DAO is accessed through the AppDatabase instance and provides
 * the primary interface for all user-related database operations
 * throughout the application.
 * 
 * ============================================================================
 */

package com.example.routeify.data.api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.routeify.data.model.User

// Data Access Object (DAO) for User entity
@Dao
interface UserDao {
    // Insert a new user into the database
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    // Find a user by their email address
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    // Find a user by their username
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?
}


// --------------------------------------------------End of File----------------------------------------------------------------