package com.example.routeify.domain.model

// Data class representing a route suggestion with time estimate, distance, and recommendation status
data class RouteSuggestion(
    val routeId: String,
    val timeEst: Int,
    val distance: Double,
    val recommended: Boolean
)