package com.example.routeify.domain.model

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

class TravelTimeTest {

    @Test
    fun `TravelTime constructor creates valid object`() {
        // Arrange
        val origin = LatLng(-33.9249, 18.4241)
        val destination = LatLng(-33.9321, 18.8602)
        val originAddress = "Cape Town CBD"
        val destinationAddress = "Stellenbosch"
        val distance = "35.2 km"
        val distanceValue = 35200
        val duration = "45 mins"
        val durationValue = 2700
        val mode = "transit"

        // Act
        val travelTime = TravelTime(
            origin = origin,
            destination = destination,
            originAddress = originAddress,
            destinationAddress = destinationAddress,
            distance = distance,
            distanceValue = distanceValue,
            duration = duration,
            durationValue = durationValue,
            mode = mode
        )

        // Assert
        assertEquals(origin, travelTime.origin)
        assertEquals(destination, travelTime.destination)
        assertEquals(originAddress, travelTime.originAddress)
        assertEquals(destinationAddress, travelTime.destinationAddress)
        assertEquals(distance, travelTime.distance)
        assertEquals(distanceValue, travelTime.distanceValue)
        assertEquals(duration, travelTime.duration)
        assertEquals(durationValue, travelTime.durationValue)
        assertEquals(mode, travelTime.mode)
    }

    @Test
    fun `TravelTime with minimum values`() {
        // Arrange
        val origin = LatLng(0.0, 0.0)
        val destination = LatLng(0.0, 0.0)

        // Act
        val travelTime = TravelTime(
            origin = origin,
            destination = destination,
            originAddress = "",
            destinationAddress = "",
            distance = "",
            distanceValue = 0,
            duration = "",
            durationValue = 0,
            mode = ""
        )

        // Assert
        assertNotNull(travelTime)
        assertEquals(0, travelTime.distanceValue)
        assertEquals(0, travelTime.durationValue)
        assertEquals("", travelTime.originAddress)
        assertEquals("", travelTime.destinationAddress)
    }

    @Test
    fun `TravelTime equality based on all properties`() {
        // Arrange
        val origin = LatLng(-33.9249, 18.4241)
        val destination = LatLng(-33.9321, 18.8602)
        
        val travelTime1 = TravelTime(
            origin = origin,
            destination = destination,
            originAddress = "Cape Town",
            destinationAddress = "Stellenbosch",
            distance = "35 km",
            distanceValue = 35000,
            duration = "45 min",
            durationValue = 2700,
            mode = "transit"
        )
        
        val travelTime2 = TravelTime(
            origin = origin,
            destination = destination,
            originAddress = "Cape Town",
            destinationAddress = "Stellenbosch",
            distance = "35 km",
            distanceValue = 35000,
            duration = "45 min",
            durationValue = 2700,
            mode = "transit"
        )

        // Act & Assert
        assertEquals(travelTime1, travelTime2)
        assertEquals(travelTime1.hashCode(), travelTime2.hashCode())
    }

    @Test
    fun `TravelTime with different modes`() {
        // Arrange
        val origin = LatLng(-33.9249, 18.4241)
        val destination = LatLng(-33.9321, 18.8602)
        
        val transitTime = TravelTime(
            origin = origin,
            destination = destination,
            originAddress = "Cape Town",
            destinationAddress = "Stellenbosch",
            distance = "35 km",
            distanceValue = 35000,
            duration = "60 min",
            durationValue = 3600,
            mode = "transit"
        )
        
        val drivingTime = TravelTime(
            origin = origin,
            destination = destination,
            originAddress = "Cape Town",
            destinationAddress = "Stellenbosch",
            distance = "35 km",
            distanceValue = 35000,
            duration = "45 min",
            durationValue = 2700,
            mode = "driving"
        )

        // Act & Assert
        assertNotEquals(transitTime, drivingTime)
        assertEquals("transit", transitTime.mode)
        assertEquals("driving", drivingTime.mode)
    }

    @Test
    fun `TravelTime toString contains meaningful information`() {
        // Arrange
        val travelTime = TravelTime(
            origin = LatLng(-33.9249, 18.4241),
            destination = LatLng(-33.9321, 18.8602),
            originAddress = "Cape Town CBD",
            destinationAddress = "Stellenbosch",
            distance = "35.2 km",
            distanceValue = 35200,
            duration = "45 mins",
            durationValue = 2700,
            mode = "transit"
        )

        // Act
        val travelTimeString = travelTime.toString()

        // Assert
        assertTrue("toString should contain class name", 
            travelTimeString.contains("TravelTime"))
    }

    @Test
    fun `TravelTime with extreme coordinates`() {
        // Arrange - Test with extreme valid coordinates
        val origin = LatLng(-90.0, -180.0) // South Pole, International Date Line
        val destination = LatLng(90.0, 180.0) // North Pole, International Date Line

        // Act
        val travelTime = TravelTime(
            origin = origin,
            destination = destination,
            originAddress = "South Pole",
            destinationAddress = "North Pole",
            distance = "20015 km", // Roughly half Earth's circumference
            distanceValue = 20015000,
            duration = "N/A",
            durationValue = 0,
            mode = "impossible"
        )

        // Assert
        assertNotNull(travelTime)
        assertEquals("South Pole", travelTime.originAddress)
        assertEquals("North Pole", travelTime.destinationAddress)
        assertEquals("impossible", travelTime.mode)
        assertEquals(20015000, travelTime.distanceValue)
        assertEquals(0, travelTime.durationValue)
    }
}