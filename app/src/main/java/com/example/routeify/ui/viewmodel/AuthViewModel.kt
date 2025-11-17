/*
 * ============================================================================
 * AUTH VIEWMODEL - User Authentication State Management
 * ============================================================================
 * 
 * ViewModel handling user login, registration, and session management.
 * Manages authentication state and persistent login across app sessions.
 * 
 * ============================================================================
 */

package com.example.routeify.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.routeify.RoutifyApplication
import com.example.routeify.data.repository.UserRepository
import com.example.routeify.data.repository.AuthDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


// Data class to represent authentication state
data class AuthState(
    val isAuthenticated: Boolean = false,
    val email: String? = null,
    val username: String? = null,
    val errorMessage: String? = null,
    val biometricEnabled: Boolean = false
)

// ViewModel to manage authentication state and actions (login, register, logout, SSO
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Lazy initialization of UserRepository and AuthDataStore
    private val userRepository: UserRepository by lazy {
        val app = getApplication<RoutifyApplication>()
        UserRepository(app.database.userDao())
    }

    // DataStore for persisting auth state
    private val authStore: AuthDataStore by lazy {
        AuthDataStore(getApplication())
    }

    init {
        // Observe persisted auth state and reflect it in-memory
        viewModelScope.launch {
            authStore.isAuthenticatedFlow
                .combine(authStore.emailFlow) { isAuth, email -> isAuth to email }
                .combine(authStore.usernameFlow) { (isAuth, email), username -> Triple(isAuth, email, username) }
                .combine(authStore.biometricEnabledFlow) { (isAuth, email, username), biometric -> 
                    AuthState(
                        isAuthenticated = isAuth && email != null && username != null,
                        email = email,
                        username = username,
                        biometricEnabled = biometric
                    )
                }
                .collect { state ->
                    if (state.isAuthenticated) {
                        _authState.value = state
                    }
                }
        }
    }

    // Login function supporting email or username
    fun login(emailOrUsername: String, password: String) {
        if (emailOrUsername.isBlank() || password.length < 4) {
            _authState.value = AuthState(isAuthenticated = false, email = null, errorMessage = "Invalid credentials")
            return
        }
        // Perform login asynchronously
        viewModelScope.launch {
            val result = if ("@" in emailOrUsername) {
                // Login with email
                userRepository.loginWithEmail(emailOrUsername, password)
            } else {
                // Login with username
                userRepository.loginWithUsername(emailOrUsername, password)
            }
            // Handle login
            result
                .onSuccess {
                    // Update state and persist auth info
                    _authState.value = AuthState(isAuthenticated = true, email = it.email, username = it.username)
                    authStore.setAuthenticated(it.email, it.username)
                }
                .onFailure {
                    _authState.value = AuthState(isAuthenticated = false, email = null, errorMessage = it.message)
                }
        }
    }

    // Registration function
    fun register(email: String, username: String, password: String, confirm: String) {
        if (email.isBlank() || username.isBlank() || password.length < 4 || password != confirm) {
            _authState.value = AuthState(isAuthenticated = false, email = null, errorMessage = "Registration failed")
            return
        }
        // Perform registration asynchronously
        viewModelScope.launch {
            val result = userRepository.register(email, username, password)
            result
                .onSuccess {
                    _authState.value = AuthState(isAuthenticated = true, email = it.email, username = it.username)
                    authStore.setAuthenticated(it.email, it.username)
                }
                // Handle registration failure
                .onFailure {
                    _authState.value = AuthState(isAuthenticated = false, email = null, errorMessage = it.message)
                }
        }
    }

    // Logout function
    fun logout() {
        _authState.value = AuthState()
        viewModelScope.launch { authStore.clear() }
    }

    // SSO Sign-In function (Google)
    fun ssoSignIn(email: String, displayName: String?) {
        viewModelScope.launch {
            // Generate a username from display name or email
            val generatedUsername = displayName?.replace("\\s+".toRegex(), "")?.lowercase()?.takeIf { it.isNotBlank() }
                ?: email.substringBefore("@")
            // Upsert user in the database
            val result = userRepository.upsertSsoUser(email, generatedUsername)
            result
                .onSuccess {
                    // Update state and persist auth info
                    _authState.value = AuthState(isAuthenticated = true, email = it.email, username = it.username)
                    authStore.setAuthenticated(it.email, it.username)
                }
                // Handle SSO failure
                .onFailure {
                    _authState.value = AuthState(isAuthenticated = false, email = null, errorMessage = it.message)
                }
        }
    }
    
    // Enable/disable biometric authentication
    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            authStore.setBiometricEnabled(enabled)
            _authState.value = _authState.value.copy(biometricEnabled = enabled)
        }
    }
    
    // Quick login with biometric (skips password check)
    fun loginWithBiometric() {
        viewModelScope.launch {
            // Biometric authentication already verified by BiometricPrompt
            // Retrieve stored user data and restore the session
            val email = authStore.emailFlow.first()
            val username = authStore.usernameFlow.first()
            
            if (email != null && username != null) {
                // Re-authenticate with stored credentials
                authStore.setAuthenticated(email, username)
                
                // Update the auth state
                _authState.value = AuthState(
                    isAuthenticated = true,
                    email = email,
                    username = username,
                    biometricEnabled = true
                )
            } else {
                // No stored credentials - biometric can't work
                _authState.value = _authState.value.copy(
                    errorMessage = "No stored credentials found. Please login with email and password first."
                )
            }
        }
    }
}

// --------------------------------------------------End of File----------------------------------------------------------------