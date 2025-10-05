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
                .collect { (isAuth, email) ->
                    if (isAuth && email != null) {
                        _authState.value = AuthState(isAuthenticated = true, email = email)
                    }
                }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.length < 4) {
            _authState.value = AuthState(isAuthenticated = false, email = null, errorMessage = "Invalid credentials")
            return
        }
        viewModelScope.launch {
            val result = userRepository.login(email, password)
            result
                .onSuccess {
                    _authState.value = AuthState(isAuthenticated = true, email = it.email)
                    authStore.setAuthenticated(it.email)
                }
                .onFailure {
                    _authState.value = AuthState(isAuthenticated = false, email = null, errorMessage = it.message)
                }
        }
    }

    fun register(email: String, password: String, confirm: String) {
        if (email.isBlank() || password.length < 4 || password != confirm) {
            _authState.value = AuthState(isAuthenticated = false, email = null, errorMessage = "Registration failed")
            return
        }
        viewModelScope.launch {
            val result = userRepository.register(email, password)
            result
                .onSuccess {
                    _authState.value = AuthState(isAuthenticated = true, email = it.email)
                    authStore.setAuthenticated(it.email)
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
}


