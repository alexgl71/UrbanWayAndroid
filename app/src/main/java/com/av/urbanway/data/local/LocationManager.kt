package com.av.urbanway.data.local

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location as AndroidLocation
import android.os.Looper
import androidx.core.content.ContextCompat
import com.av.urbanway.data.models.Coordinates
import com.av.urbanway.data.models.Location
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class LocationManager private constructor(
    private val context: Context
) {
    companion object {
        @Volatile
        private var INSTANCE: LocationManager? = null
        
        // Turin bounding box for filtering
        private const val TURIN_NORTH = 45.1307
        private const val TURIN_SOUTH = 45.0158
        private const val TURIN_EAST = 7.7717
        private const val TURIN_WEST = 7.5883
        private const val LOCATION_UPDATE_THRESHOLD = 60f // 60 meters like iOS
        
        fun getInstance(context: Context): LocationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    private val geocoder = Geocoder(context, Locale.getDefault())
    
    private var currentLocation: AndroidLocation? = null
    private var locationAddress: String = ""
    
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) return null
        
        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: AndroidLocation? ->
                        if (location != null) {
                            currentLocation = location
                            val coordinates = Coordinates(location.latitude, location.longitude)
                            
                            // Get address
                            try {
                                val addresses = geocoder.getFromLocation(
                                    location.latitude,
                                    location.longitude,
                                    1
                                )
                                locationAddress = addresses?.firstOrNull()?.getAddressLine(0) 
                                    ?: "Posizione corrente"
                            } catch (e: Exception) {
                                locationAddress = "Posizione corrente"
                            }
                            
                            continuation.resume(
                                Location(
                                    address = locationAddress,
                                    coordinates = coordinates
                                )
                            )
                        } else {
                            continuation.resume(null)
                        }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(null)
                    }
            } catch (e: SecurityException) {
                continuation.resume(null)
            }
        }
    }
    
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }
        
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdateDelayMillis(15000)
            .build()
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    // Check if location changed significantly (60m threshold)
                    if (currentLocation == null || 
                        currentLocation!!.distanceTo(location) > LOCATION_UPDATE_THRESHOLD) {
                        
                        currentLocation = location
                        val coordinates = Coordinates(location.latitude, location.longitude)
                        
                        // Get address in background
                        try {
                            val addresses = geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                            )
                            locationAddress = addresses?.firstOrNull()?.getAddressLine(0) 
                                ?: "Posizione corrente"
                        } catch (e: Exception) {
                            locationAddress = "Posizione corrente"
                        }
                        
                        val userLocation = Location(
                            address = locationAddress,
                            coordinates = coordinates
                        )
                        
                        trySend(userLocation)
                    }
                }
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
        }
        
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    
    suspend fun getAddressFromCoordinates(coordinates: Coordinates): String? {
        return try {
            val addresses = geocoder.getFromLocation(
                coordinates.lat,
                coordinates.lng,
                1
            )
            addresses?.firstOrNull()?.getAddressLine(0)
        } catch (e: Exception) {
            null
        }
    }
    
    fun isInTurinArea(coordinates: Coordinates): Boolean {
        return coordinates.lat in TURIN_SOUTH..TURIN_NORTH &&
               coordinates.lng in TURIN_WEST..TURIN_EAST
    }
    
    fun getUserLocation(): Location? {
        return currentLocation?.let { location ->
            Location(
                address = locationAddress,
                coordinates = Coordinates(location.latitude, location.longitude)
            )
        }
    }
}