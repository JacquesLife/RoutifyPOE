package com.example.routeify.utils

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.graphics.createBitmap

object MapIconUtils {
    
    // Cache for bitmap descriptors to improve performance
    private val iconCache = mutableMapOf<Int, BitmapDescriptor?>()
    
    /**
     * Convert a vector drawable resource to a BitmapDescriptor for use in Google Maps markers
     */
    fun getBitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        // Check cache first
        if (iconCache.containsKey(vectorResId)) {
            return iconCache[vectorResId]
        }
        
        return try {
            val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
            val result = vectorDrawable?.let { drawable ->
                drawable.setBounds(0, 0, 80, 80) // Increased size for better visibility
                val bitmap = createBitmap(80, 80)
                val canvas = Canvas(bitmap)
                drawable.draw(canvas)
                BitmapDescriptorFactory.fromBitmap(bitmap)
            }
            // Cache the result (even if null)
            iconCache[vectorResId] = result
            result
        } catch (e: Exception) {
            android.util.Log.e("MapIconUtils", "Failed to load custom icon $vectorResId: ${e.message}")
            iconCache[vectorResId] = null
            null // Return null if conversion fails, will fallback to default marker
        }
    }
    
    /**
     * Get marker icon based on stop type with custom vector icons
     */
    fun getTransportIcon(context: Context, stopType: com.example.routeify.data.model.StopType): BitmapDescriptor {
        return when (stopType) {
            com.example.routeify.data.model.StopType.MAJOR_HUB -> {
                val customIcon = getBitmapDescriptorFromVector(context, com.example.routeify.R.drawable.ic_transport_hub)
                if (customIcon != null) {
                    android.util.Log.d("MapIconUtils", "Using custom hub icon")
                    customIcon
                } else {
                    android.util.Log.w("MapIconUtils", "Fallback to default red marker for hub")
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                }
            }
            com.example.routeify.data.model.StopType.RAILWAY -> {
                val customIcon = getBitmapDescriptorFromVector(context, com.example.routeify.R.drawable.ic_train_station)
                if (customIcon != null) {
                    android.util.Log.d("MapIconUtils", "Using custom train icon")
                    customIcon
                } else {
                    android.util.Log.w("MapIconUtils", "Fallback to default green marker for railway")
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                }
            }
            com.example.routeify.data.model.StopType.REGULAR -> {
                val customIcon = getBitmapDescriptorFromVector(context, com.example.routeify.R.drawable.ic_bus_stop)
                if (customIcon != null) {
                    android.util.Log.d("MapIconUtils", "Using custom bus stop icon")
                    customIcon
                } else {
                    android.util.Log.w("MapIconUtils", "Fallback to default azure marker for bus stop")
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                }
            }
        }
    }
}