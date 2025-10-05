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
import kotlinx.coroutines.launch

data class AuthState(
    val isAuthenticated: Boolean = false,
    val email: String? = null,
    val username: String? = null,
    val errorMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val userRepository: UserRepository by lazy {
        val app = getApplication<RoutifyApplication>()
        UserRepository(app.database.userDao())
    }

    private val authStore: AuthDataStore by lazy {
        AuthDataStore(getApplication())
    }

    init {
        // Observe persisted auth state and reflect it in-memory
        viewModelScope.launch {
            authStore.isAuthenticatedFlow
                .combine(authStore.emailFlow) { isAuth, email -> isAuth to email }
                .combine(authStore.usernameFlow) { (isAuth, email), username -> Triple(isAuth, email, username) }
                .collect { (isAuth, email, username) ->
                    if (isAuth && email != null && username != null) {
                        _authState.value = AuthState(isAuthenticated = true, email = email, username = username)
                    }
                }
        }
    }

    fun login(emailOrUsername: String, password: String) {
        if (emailOrUsername.isBlank() || password.length < 4) {
            _authState.value = AuthState(isAuthenticated = false, email = null, errorMessage = "Invalid credentials")
            return
        }
        viewModelScope.launch {
            val result = if ("@" in emailOrUsername) {
                userRepository.loginWithEmail(emailOrUsername, password)
            } else {
                userRepository.loginWithUsername(emailOrUsername, password)
            }
            result
                .onSuccess {
                    _authState.value = AuthState(isAuthenticated = true, email = it.email, username = it.username)
                    authStore.setAuthenticated(it.email, it.username)
                }
                .onFailure {
                    _authState.value = AuthState(isAuthenticated = false, email = null, errorMessage = it.message)
                }
        }
    }

    fun register(email: String, username: String, password: String, confirm: String) {
        if (email.isBlank() || username.isBlank() || password.length < 4 || password != confirm) {
            _authState.value = AuthState(isAuthenticated = false, email = null, errorMessage = "Registration failed")
            return
        }
        viewModelScope.launch {
            val result = userRepository.register(email, username, password)
            result
                .onSuccess {
                    _authState.value = AuthState(isAuthenticated = true, email = it.email, username = it.username)
                    authStore.setAuthenticated(it.email, it.username)
                }
                .onFailure {
                    _authState.value = AuthState(isAuthenticated = false, email = null, errorMessage = it.message)
                }
        }
    }

    fun logout() {
        _authState.value = AuthState()
        viewModelScope.launch { authStore.clear() }
    }

    fun ssoSignIn(email: String, displayName: String?) {
        viewModelScope.launch {
            val generatedUsername = displayName?.replace("\s+".toRegex(), "")?.lowercase()?.takeIf { it.isNotBlank() }
                ?: email.substringBefore("@")
            val result = userRepository.upsertSsoUser(email, generatedUsername)
            result
                .onSuccess {
                    _authState.value = AuthState(isAuthenticated = true, email = it.email, username = it.username)
                    authStore.setAuthenticated(it.email, it.username)
                }
                .onFailure {
                    _authState.value = AuthState(isAuthenticated = false, email = null, errorMessage = it.message)
                }
        }
    }
}


