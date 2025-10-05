package com.example.routeify.data.model

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for TransitStop model
 * Tests essential data validation and structure
 */
class TransitStopTest {

    @Test
    fun `TransitStop should create with valid data`() {
        // Arrange
        val stop = TransitStop(
            id = "test_id_123",
            name = "Cape Town Station",
            latitude = -33.9249,
            longitude = 18.4241,
            stopType = TransitStopType.TRAIN_STATION,
            vicinity = "Cape Town CBD",
            rating = 4.2
        )

        // Assert
        assertEquals("test_id_123", stop.id)
        assertEquals("Cape Town Station", stop.name)
        assertEquals(-33.9249, stop.latitude, 0.0001)
        assertEquals(18.4241, stop.longitude, 0.0001)
        assertEquals(TransitStopType.TRAIN_STATION, stop.stopType)
        assertEquals("Cape Town CBD", stop.vicinity)
        assertEquals(4.2, stop.rating!!, 0.1)
    }

    @Test
    fun `TransitStop should handle optional fields`() {
        // Arrange
        val stop = TransitStop(
            id = "minimal_stop",
            name = "Basic Stop",
            latitude = -33.9249,
            longitude = 18.4241,
            stopType = TransitStopType.BUS_STATION
            // vicinity and rating are null
        )

        // Assert
        assertNull(stop.vicinity)
        assertNull(stop.rating)
    }

    @Test
    fun `TransitStop coordinates should be valid for Cape Town area`() {
        // Arrange
        val stop = TransitStop(
            id = "cape_town_stop",
            name = "Cape Town Stop",
            latitude = -33.9249,
            longitude = 18.4241,
            stopType = TransitStopType.BUS_STATION
        )

        // Assert - Cape Town latitude range: approximately -34.4 to -33.7
        assertTrue("Latitude should be in Cape Town range", 
            stop.latitude >= -34.5 && stop.latitude <= -33.0)
        
        // Assert - Cape Town longitude range: approximately 18.3 to 18.9
        assertTrue("Longitude should be in Cape Town range", 
            stop.longitude >= 18.0 && stop.longitude <= 19.0)
    }

    @Test
    fun `TransitStopType enum should have all expected values`() {
        // Assert all transit stop types exist
        val types = TransitStopType.values()
        
        assertTrue("Should contain BUS_STATION", 
            types.contains(TransitStopType.BUS_STATION))
        assertTrue("Should contain TRAIN_STATION", 
            types.contains(TransitStopType.TRAIN_STATION))
        assertTrue("Should contain SUBWAY_STATION", 
            types.contains(TransitStopType.SUBWAY_STATION))
        assertTrue("Should contain TRANSIT_STATION", 
            types.contains(TransitStopType.TRANSIT_STATION))
        assertTrue("Should contain LIGHT_RAIL_STATION", 
            types.contains(TransitStopType.LIGHT_RAIL_STATION))
    }
}