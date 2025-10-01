package com.example.routeify.data

data class RouteStep(
    val description: String,
    val duration: String,
    val type: StepType
)

enum class StepType {
    WALK, TRAIN, BUS, TAXI
}

data class Route(
    val id: String,
    val steps: List<RouteStep>,
    val departureTime: String,
    val arrivalTime: String,
    val totalDuration: String,
    val price: String,
    val transportModes: List<StepType>
)

// Mock route data
object MockRouteData {
    val sampleRoute = Route(
        id = "1",
        steps = listOf(
            RouteStep("Walk to Rondebosch Station", "8 min", StepType.WALK),
            RouteStep("Southern Line to Claremont", "15 min", StepType.TRAIN),
            RouteStep("Walk to destination", "5 min", StepType.WALK)
        ),
        departureTime = "08:45",
        arrivalTime = "09:32",
        totalDuration = "47 min",
        price = "R 12.50",
        transportModes = listOf(StepType.WALK, StepType.TRAIN, StepType.WALK)
    )
    
    val mockRoutes = listOf(
        Route(
            id = "1",
            departureTime = "08:45",
            arrivalTime = "09:32",
            totalDuration = "47 min",
            transportModes = listOf(StepType.WALK, StepType.TRAIN, StepType.WALK),
            price = "R 12.50",
            steps = listOf(
                RouteStep("Walk to Rondebosch Station", "8 min", StepType.WALK),
                RouteStep("Southern Line to Claremont", "15 min", StepType.TRAIN),
                RouteStep("Walk to destination", "5 min", StepType.WALK)
            )
        ),
        Route(
            id = "2",
            departureTime = "09:15",
            arrivalTime = "10:10",
            totalDuration = "55 min",
            transportModes = listOf(StepType.WALK, StepType.BUS, StepType.WALK),
            price = "R 8.00",
            steps = listOf(
                RouteStep("Walk to Main Road", "3 min", StepType.WALK),
                RouteStep("Golden Arrow 104 to Claremont", "45 min", StepType.BUS),
                RouteStep("Walk to destination", "7 min", StepType.WALK)
            )
        ),
        Route(
            id = "3",
            departureTime = "09:30",
            arrivalTime = "10:05",
            totalDuration = "35 min",
            transportModes = listOf(StepType.WALK, StepType.TRAIN, StepType.WALK),
            price = "R 15.00",
            steps = listOf(
                RouteStep("Walk to Rondebosch Station", "8 min", StepType.WALK),
                RouteStep("Direct train to Claremont", "22 min", StepType.TRAIN),
                RouteStep("Walk to destination", "5 min", StepType.WALK)
            )
        )
    )
}
