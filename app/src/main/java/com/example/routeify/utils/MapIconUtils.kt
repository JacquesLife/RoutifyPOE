package com.example.routeify.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.example.routeify.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object MapIconUtils {
    
    /**
     * Get custom marker icon based on transit stop type using your beautiful vector drawables!
     */
    fun getTransportIcon(context: Context, stopType: com.example.routeify.data.model.TransitStopType): BitmapDescriptor {
        val drawableRes = when (stopType) {
            com.example.routeify.data.model.TransitStopType.BUS_STATION -> {
                R.drawable.bus_stop
            }
            com.example.routeify.data.model.TransitStopType.TRAIN_STATION -> {
                R.drawable.train_station
            }
            com.example.routeify.data.model.TransitStopType.SUBWAY_STATION -> {
                R.drawable.train_station
            }
            com.example.routeify.data.model.TransitStopType.LIGHT_RAIL_STATION -> {
                R.drawable.train_station
            }
            com.example.routeify.data.model.TransitStopType.TRANSIT_STATION -> {
                R.drawable.bus_station
            }
        }
        
        return vectorToBitmap(context, drawableRes)
    }
    
    /**
     * Convert vector drawable to bitmap descriptor for Google Maps
     */
    private fun vectorToBitmap(context: Context, drawableRes: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, drawableRes)
        
        // Set size for the marker (adjust as needed)
        val width = 96  // 96dp for good visibility
        val height = 96
        
        vectorDrawable?.setBounds(0, 0, width, height)
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable?.draw(canvas)
        
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}