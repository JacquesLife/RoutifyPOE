package com.example.routeify.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object MapIconUtils {
    
    /**
     * Convert a vector drawable resource to a BitmapDescriptor for use in Google Maps markers
     */
    fun getBitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return try {
            val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
            vectorDrawable?.let { drawable ->
                drawable.setBounds(0, 0, 64, 64) // Size of the marker icon
                val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.draw(canvas)
                BitmapDescriptorFactory.fromBitmap(bitmap)
            }
        } catch (e: Exception) {
            null // Return null if conversion fails, will fallback to default marker
        }
    }
    
    /**
     * Get marker icon based on stop type with custom vector icons
     */
    fun getTransportIcon(context: Context, stopType: com.example.routeify.data.model.StopType): BitmapDescriptor {
        return when (stopType) {
            com.example.routeify.data.model.StopType.MAJOR_HUB -> {
                getBitmapDescriptorFromVector(context, com.example.routeify.R.drawable.ic_transport_hub)
                    ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            }
            com.example.routeify.data.model.StopType.RAILWAY -> {
                getBitmapDescriptorFromVector(context, com.example.routeify.R.drawable.ic_train_station)
                    ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            }
            com.example.routeify.data.model.StopType.REGULAR -> {
                getBitmapDescriptorFromVector(context, com.example.routeify.R.drawable.ic_bus_stop)
                    ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            }
        }
    }
}