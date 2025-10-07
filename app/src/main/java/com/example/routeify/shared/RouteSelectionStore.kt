package com.example.routeify.shared

import com.example.routeify.domain.model.TransitRoute

object RouteSelectionStore {
    @Volatile
    private var selectedRoute: TransitRoute? = null

    fun setSelectedRoute(route: TransitRoute) {
        selectedRoute = route
    }

    fun getSelectedRoute(): TransitRoute? = selectedRoute

    fun clear() {
        selectedRoute = null
    }
}


