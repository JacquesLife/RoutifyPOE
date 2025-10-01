package com.example.routeify.domain.smartsuggestions

import com.example.routeify.domain.smartsuggestions.model.RouteSuggestion

class SmartSuggestionEngine{

    fun analyzeRouteSuggestions(suggestions: List<RouteSuggestion>): List<RouteSuggestion> {
        // sort by the shortest time estimate then shortest distance
        return suggestions.minWith(compareBy({ it.timeEst }, { it.distance })).let { bestSuggestion ->
            suggestions.map {
                if (it == bestSuggestion) {
                    it.copy(recommended = true)
                } else {
                    it.copy(recommended = false)
                }
            }
        }
    }
}
