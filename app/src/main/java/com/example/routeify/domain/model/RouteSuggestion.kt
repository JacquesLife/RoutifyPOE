/*
 * ============================================================================
 * ROUTE SUGGESTION MODEL - Smart Route Recommendations
 * ============================================================================
 * 
 * Simple data class for representing intelligent route suggestions.
 * Used by the smart suggestions engine for route optimization.
 * 
 * ============================================================================
 */

package com.example.routeify.domain.model

// Data class representing a route suggestion with time estimate, distance, and recommendation status
data class RouteSuggestion(
    val routeId: String,
    val timeEst: Int,
    val distance: Double,
    val recommended: Boolean
)

// --------------------------------------------------End of File----------------------------------------------------------------