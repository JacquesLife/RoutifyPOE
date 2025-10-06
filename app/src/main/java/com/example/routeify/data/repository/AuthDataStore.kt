package com.example.routeify.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authPrefs by preferencesDataStore(name = "auth_prefs")

class AuthDataStore(private val context: Context) {
    private object Keys {
        val isAuthenticated = booleanPreferencesKey("is_authenticated")
        val email = stringPreferencesKey("email")
        val username = stringPreferencesKey("username")
    }

    val isAuthenticatedFlow: Flow<Boolean> = context.authPrefs.data.map { it[Keys.isAuthenticated] ?: false }
    val emailFlow: Flow<String?> = context.authPrefs.data.map { it[Keys.email] }
    val usernameFlow: Flow<String?> = context.authPrefs.data.map { it[Keys.username] }

    suspend fun setAuthenticated(email: String, username: String) {
        context.authPrefs.edit { prefs ->
            prefs[Keys.isAuthenticated] = true
            prefs[Keys.email] = email
            prefs[Keys.username] = username
        }
    }

    suspend fun clear() {
        context.authPrefs.edit { prefs ->
            prefs[Keys.isAuthenticated] = false
            prefs.remove(Keys.email)
            prefs.remove(Keys.username)
        }
    }
}


