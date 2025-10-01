package com.example.routeify.domain.smartsuggestions

import com.example.routeify.domain.smartsuggestions.model.RouteSuggestion

class SmartSuggesstionsUseCases {

    private val engine = SmartSuggestionEngine()

    fun getBestRouteSuggestion(suggestions: List<RouteSuggestion>): RouteSuggestion? {
        if (suggestions.isEmpty()) return null
        val analyzedSuggestions = engine.analyzeRouteSuggestions(suggestions)
        return analyzedSuggestions.find { it.recommended }
    }
}