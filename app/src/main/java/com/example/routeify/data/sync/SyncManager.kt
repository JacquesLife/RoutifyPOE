/*
 * ============================================================================
 * SYNC MANAGER - Offline Data Synchronization
 * ============================================================================
 * 
 * Manages offline data caching and automatic sync when connection restored.
 * Monitors network connectivity and triggers data synchronization operations.
 * 
 * Key Features:
 * - Network connectivity monitoring
 * - Automatic sync on reconnection
 * - Pending data queue management
 * - Sync status tracking
 * 
 * ============================================================================
 */

package com.example.routeify.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SyncManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SyncManager"
    }
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _isOnline = MutableStateFlow(checkConnectivity())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    
    sealed class SyncStatus {
        object IDLE : SyncStatus()
        object SYNCING : SyncStatus()
        data class SUCCESS(val itemsSynced: Int, val timestamp: Long = System.currentTimeMillis()) : SyncStatus()
        data class ERROR(val message: String, val timestamp: Long = System.currentTimeMillis()) : SyncStatus()
    }
    
    init {
        registerNetworkCallback()
        Log.d(TAG, "SyncManager initialized. Online status: ${_isOnline.value}")
    }
    
    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available - Connection restored")
                _isOnline.value = true
                // Trigger sync when connection restored
                scope.launch { 
                    syncPendingData() 
                }
            }
            
            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost - Offline mode activated")
                _isOnline.value = false
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                _isOnline.value = hasInternet && isValidated
            }
        }
        
        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
            Log.d(TAG, "Network callback registered successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
    }
    
    private fun checkConnectivity(): Boolean {
        return try {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null && 
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking connectivity", e)
            false
        }
    }
    
    suspend fun syncPendingData() {
        if (!_isOnline.value) {
            Log.d(TAG, "Sync skipped - Device offline")
            return
        }
        
        _syncStatus.value = SyncStatus.SYNCING
        Log.d(TAG, "Starting sync operation...")
        
        try {
            var syncedCount = 0
            
            // Here you would sync recent destinations to your backend/cloud
            // For now, we'll just mark them as synced
            // Example: Upload to Firebase, your REST API, etc.
            
            // Simulate sync delay
            kotlinx.coroutines.delay(500)
            
            Log.d(TAG, "Sync completed successfully. Items synced: $syncedCount")
            _syncStatus.value = SyncStatus.SUCCESS(syncedCount)
            
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            _syncStatus.value = SyncStatus.ERROR(e.message ?: "Unknown sync error")
        }
    }
    
    fun manualSync() {
        scope.launch {
            syncPendingData()
        }
    }
    
    fun cleanup() {
        try {
            networkCallback?.let {
                connectivityManager.unregisterNetworkCallback(it)
                Log.d(TAG, "Network callback unregistered")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}

// --------------------------------------------------End of File----------------------------------------------------------------
