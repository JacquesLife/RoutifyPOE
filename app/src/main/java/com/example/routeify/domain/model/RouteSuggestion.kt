package com.example.routeify.domain.model

data class RouteSuggestion(
    val routeId: String,
    val timeEst: Int,
    val distance: Double,
    val recommended: Boolean
)