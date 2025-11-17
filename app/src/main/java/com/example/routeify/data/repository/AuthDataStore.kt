/*
 * ============================================================================
 * AUTH DATASTORE - Persistent Authentication State
 * ============================================================================
 * 
 * DataStore repository for managing user session persistence and login state.
 * Handles secure storage of authentication preferences across app restarts.
 * 
 * ============================================================================
 */

package com.example.routeify.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authPrefs by preferencesDataStore(name = "auth_prefs")

// DataStore for managing authentication state and user info
class AuthDataStore(private val context: Context) {
    private object Keys {
        val isAuthenticated = booleanPreferencesKey("is_authenticated")
        val email = stringPreferencesKey("email")
        val username = stringPreferencesKey("username")
        val biometricEnabled = booleanPreferencesKey("biometric_enabled")
        val darkModeEnabled = booleanPreferencesKey("dark_mode_enabled")
    }

// Flows to observe authentication state and user info
    val isAuthenticatedFlow: Flow<Boolean> = context.authPrefs.data.map { it[Keys.isAuthenticated] ?: false }
    val emailFlow: Flow<String?> = context.authPrefs.data.map { it[Keys.email] }
    val usernameFlow: Flow<String?> = context.authPrefs.data.map { it[Keys.username] }
    val biometricEnabledFlow: Flow<Boolean> = context.authPrefs.data.map { it[Keys.biometricEnabled] ?: false }
    val darkModeEnabledFlow: Flow<Boolean> = context.authPrefs.data.map { it[Keys.darkModeEnabled] ?: false }

    // Set authentication state and user info
    suspend fun setAuthenticated(email: String, username: String) {
        context.authPrefs.edit { prefs ->
            prefs[Keys.isAuthenticated] = true
            prefs[Keys.email] = email
            prefs[Keys.username] = username
        }
    }

    // Clear authentication state but keep user credentials for biometric login
    suspend fun clear() {
        context.authPrefs.edit { prefs ->
            prefs[Keys.isAuthenticated] = false
            // Keep email and username for biometric login to work
            // Don't clear biometric preference - keep it for next login
        }
    }
    
    // Set biometric authentication preference
    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.authPrefs.edit { prefs ->
            prefs[Keys.biometricEnabled] = enabled
        }
    }
    
    // Set dark mode preference
    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.authPrefs.edit { prefs ->
            prefs[Keys.darkModeEnabled] = enabled
        }
    }
}

// --------------------------------------------------End of File----------------------------------------------------------------