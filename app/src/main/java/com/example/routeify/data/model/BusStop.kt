package com.example.routeify.data.model

data class BusStop(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val routes: List<String> = emptyList(),
    val stopType: StopType = StopType.REGULAR,
    val area: String? = null
)

enum class StopType {
    MAJOR_HUB,     // Main stations/interchanges  
    REGULAR,       // Normal bus stops
    RAILWAY        // Railway stations
}

// Smart data management for 770 stops
object BusStopManager {
    
    // Sample of major hubs - these show at low zoom levels
    val majorHubs = listOf(
        BusStop(
            id = "hub_1",
            name = "Cape Town Station",
            latitude = -33.9187,
            longitude = 18.4287,
            routes = listOf("Central Line", "MyCiti T01", "Golden Arrow"),
            stopType = StopType.MAJOR_HUB,
            area = "City Center"
        ),
        BusStop(
            id = "hub_2",
            name = "V&A Waterfront",
            latitude = -33.9043,
            longitude = 18.4234,
            routes = listOf("MyCiti 104", "MyCiti 108", "Sightseeing Bus"),
            stopType = StopType.MAJOR_HUB,
            area = "Waterfront"
        ),
        BusStop(
            id = "hub_3",
            name = "Wynberg Station",
            latitude = -34.0186,
            longitude = 18.4624,
            routes = listOf("Southern Line", "MyCiti", "Multiple Routes"),
            stopType = StopType.MAJOR_HUB,
            area = "Southern Suburbs"
        ),
        BusStop(
            id = "hub_4",
            name = "Bellville Station",
            latitude = -33.8988,
            longitude = 18.6300,
            routes = listOf("Northern Line", "MyCiti", "Taxi Routes"),
            stopType = StopType.MAJOR_HUB,
            area = "Northern Suburbs"
        ),
        BusStop(
            id = "hub_5",
            name = "Khayelitsha Station", 
            latitude = -34.0370,
            longitude = 18.6797,
            routes = listOf("Khayelitsha Line", "B97", "C3"),
            stopType = StopType.MAJOR_HUB,
            area = "Khayelitsha"
        )
    )
    
    // Sample area stops - these show at medium zoom levels
    val areaStops = listOf(
        BusStop(
            id = "area_1",
            name = "Company Gardens",
            latitude = -33.9307,
            longitude = 18.4197,
            routes = listOf("MyCiti 106", "A01"),
            area = "City Center"
        ),
        BusStop(
            id = "area_2",
            name = "Sea Point Promenade",
            latitude = -33.9194,
            longitude = 18.3836,
            routes = listOf("MyCiti 107", "MyCiti 108"),
            area = "Atlantic Seaboard"
        ),
        BusStop(
            id = "area_3",
            name = "UCT Upper Campus",
            latitude = -33.9577,
            longitude = 18.4613,
            routes = listOf("UCT Shuttle", "Jammie Shuttle"),
            area = "Southern Suburbs"
        ),
        BusStop(
            id = "area_4",
            name = "Claremont Station",
            latitude = -33.9848,
            longitude = 18.4644,
            routes = listOf("Southern Line", "MyCiti 106"),
            area = "Southern Suburbs"
        ),
        BusStop(
            id = "area_5",
            name = "Observatory Station",
            latitude = -33.9360,
            longitude = 18.4715,
            routes = listOf("Southern Line", "Local Routes"),
            area = "Southern Suburbs"
        )
    )
}