package com.av.urbanway.data.local

import android.content.Context
import com.av.urbanway.BuildConfig
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

class GoogleMapsConfig private constructor(
    private val context: Context
) {
    companion object {
        @Volatile
        private var INSTANCE: GoogleMapsConfig? = null
        
        // Turin bounding box for Places API filtering
        const val TURIN_NORTH = 45.1307
        const val TURIN_SOUTH = 45.0158
        const val TURIN_EAST = 7.7717
        const val TURIN_WEST = 7.5883
        
        // Default map center (Turin city center)
        const val TURIN_LAT = 45.0703
        const val TURIN_LNG = 7.6869
        const val DEFAULT_ZOOM = 12f
        
        fun getInstance(context: Context): GoogleMapsConfig {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GoogleMapsConfig(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private var _placesClient: PlacesClient? = null
    
    val apiKey: String
        get() = BuildConfig.GOOGLE_MAPS_API_KEY
    
    val placesClient: PlacesClient
        get() {
            if (_placesClient == null) {
                initializePlaces()
            }
            return _placesClient!!
        }
    
    private fun initializePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(context, apiKey)
        }
        _placesClient = Places.createClient(context)
    }
    
    fun isApiKeyConfigured(): Boolean {
        return apiKey.isNotBlank() && apiKey != "YOUR_API_KEY_HERE"
    }
    
    fun isInTurinArea(lat: Double, lng: Double): Boolean {
        return lat in TURIN_SOUTH..TURIN_NORTH && lng in TURIN_WEST..TURIN_EAST
    }
}