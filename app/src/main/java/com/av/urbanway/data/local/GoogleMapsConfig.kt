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
        
        // Turin bounding box for Places API filtering (matching iOS bounds exactly)
        const val TURIN_NORTH = 45.157389746249294
        const val TURIN_SOUTH = 44.95065585978324
        const val TURIN_EAST = 7.929152485258602
        const val TURIN_WEST = 7.519078379050264
        
        // Default map center (Piazza Castello, Turin - matching iOS)
        const val TURIN_LAT = 45.07102258187123
        const val TURIN_LNG = 7.685422860157677
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
            android.util.Log.d("TRANSITOAPP", "GoogleMapsConfig - placesClient requested")
            if (_placesClient == null) {
                android.util.Log.d("TRANSITOAPP", "GoogleMapsConfig - _placesClient is null, initializing")
                initializePlaces()
            }
            android.util.Log.d("TRANSITOAPP", "GoogleMapsConfig - returning placesClient")
            return _placesClient!!
        }
    
    private fun initializePlaces() {
        android.util.Log.d("TRANSITOAPP", "GoogleMapsConfig - initializePlaces called")
        android.util.Log.d("TRANSITOAPP", "GoogleMapsConfig - API key: ${apiKey.take(10)}...")
        if (!Places.isInitialized()) {
            android.util.Log.d("TRANSITOAPP", "GoogleMapsConfig - Places not initialized, initializing with API key")
            Places.initialize(context, apiKey)
        } else {
            android.util.Log.d("TRANSITOAPP", "GoogleMapsConfig - Places already initialized")
        }
        android.util.Log.d("TRANSITOAPP", "GoogleMapsConfig - creating Places client")
        _placesClient = Places.createClient(context)
    }
    
    fun isApiKeyConfigured(): Boolean {
        return apiKey.isNotBlank() && apiKey != "YOUR_API_KEY_HERE"
    }
    
    fun isInTurinArea(lat: Double, lng: Double): Boolean {
        return lat in TURIN_SOUTH..TURIN_NORTH && lng in TURIN_WEST..TURIN_EAST
    }
}