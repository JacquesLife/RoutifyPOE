package com.example.routeify.domain.smartsuggestions

import com.example.routeify.domain.model.RouteSuggestion
import com.example.routeify.domain.model.TransitRoute

class SmartSuggesstionsUseCases {

    private val engine = SmartSuggestionEngine()

    // Generates and analyzes smart route suggestions
    fun generateSmartSuggestions(routes: List<TransitRoute>): List<RouteSuggestion> {
        val initialSuggestions = engine.createSmartSuggestions(routes)
        return engine.analyzeRouteSuggestions(initialSuggestions)
    }
}