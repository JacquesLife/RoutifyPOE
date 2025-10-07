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
    }

// Flows to observe authentication state and user info
    val isAuthenticatedFlow: Flow<Boolean> = context.authPrefs.data.map { it[Keys.isAuthenticated] ?: false }
    val emailFlow: Flow<String?> = context.authPrefs.data.map { it[Keys.email] }
    val usernameFlow: Flow<String?> = context.authPrefs.data.map { it[Keys.username] }

    // Set authentication state and user info
    suspend fun setAuthenticated(email: String, username: String) {
        context.authPrefs.edit { prefs ->
            prefs[Keys.isAuthenticated] = true
            prefs[Keys.email] = email
            prefs[Keys.username] = username
        }
    }

    // Clear authentication state and user info
    suspend fun clear() {
        context.authPrefs.edit { prefs ->
            prefs[Keys.isAuthenticated] = false
            prefs.remove(Keys.email)
            prefs.remove(Keys.username)
        }
    }
}


