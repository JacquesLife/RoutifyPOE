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

//Android Cookies, 2023. Clean Architecture: Business Logic, Domain Layer & Use Cases. [online] YouTube. Available at: <https://www.youtube.com/watch?v=HI0U8gHuFmk> [Accessed 7 October 2025].