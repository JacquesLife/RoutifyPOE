package com.example.routeify.domain.smartsuggestions

import com.example.routeify.domain.model.RouteSuggestion
import com.example.routeify.domain.model.TransitRoute

class SmartSuggestionEngine{

    // Analyzing and ranking RouteSuggestions
    fun analyzeRouteSuggestions(suggestions: List<RouteSuggestion>): List<RouteSuggestion> {
        if (suggestions.isEmpty()) return suggestions
        //Sorting by time first, then by distance
        val sorted = suggestions.sortedWith(compareBy({it.timeEst}, {it.distance}))
        //Taking the top 2 suggestions
        val best3 = sorted.take(2)
        //Marking the fastest suggestion as recommended
        return best3.mapIndexed { index, suggestion ->
            suggestion.copy(
                recommended = index == 0
            )
        }

    }

    // Generating RouteSuggestions from TransitRoutes
    fun createSmartSuggestions(routes: List<TransitRoute>):List<RouteSuggestion>{
        return routes.mapIndexed{ index, route ->
            RouteSuggestion(
                routeId = "route_$index",
                timeEst = route.getDurationInMinutes(),
                distance = route.totalDistance.filter
                { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0,
                recommended = false,
                isQuickRoute = route.isQuickTrip,
                hasTransfer = route.numberOfTransfers > 0,
                transportType = route.primaryTransitMode,
                reliabilityScore = when {
                    route.getDurationInMinutes() < 20 -> "Excellent"
                    route.getDurationInMinutes() < 30 -> "Good"
                    else -> "Fair"
                }
            )
        }
    }
}

// Developer, A., 2025. Domain layer. [online] Android Developers. Available at: <https://developer.android.com/topic/architecture/domain-layer>. [Accessed 6 October 2025].
// Android Cookies, 2023. Clean Architecture: Business Logic, Domain Layer & Use Cases. [online] YouTube. Available at: <https://www.youtube.com/watch?v=HI0U8gHuFmk> [Accessed 7 October 2025].