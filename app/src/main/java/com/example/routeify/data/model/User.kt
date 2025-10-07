/*
 * ============================================================================
 * USER MODEL - Authentication & Profile Data
 * ============================================================================
 * 
 * Room entity for storing user authentication and profile information.
 * Handles user registration, login, and profile management.
 * 
 * ============================================================================
 */

package com.example.routeify.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// User entity representing a user in the database
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true)
    ]
)
// Indices to enforce uniqueness on email and username
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val username: String,
    val passwordHash: String
)

// --------------------------------------------------End of File----------------------------------------------------------------