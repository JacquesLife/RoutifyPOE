/*
 * ============================================================================
 * ROUTE SELECTION STORE - Global Route State Management
 * ============================================================================
 * 
 * Singleton store for managing selected route data across app navigation.
 * Provides thread-safe access to current route selection state.
 * 
 * ============================================================================
 */

package com.example.routeify.shared

import com.example.routeify.domain.model.TransitRoute

// Singleton object to store and manage the selected route
object RouteSelectionStore {
    @Volatile
    private var selectedRoute: TransitRoute? = null

    // Set the selected route
    fun setSelectedRoute(route: TransitRoute) {
        selectedRoute = route
    }

    // Get the currently selected route
    fun getSelectedRoute(): TransitRoute? = selectedRoute

    // Clear the selected route
    fun clear() {
        selectedRoute = null
    }
}

// --------------------------------------------------End of File----------------------------------------------------------------