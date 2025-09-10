package com.av.urbanway.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.av.urbanway.data.local.GoogleMapsConfig
import com.av.urbanway.data.models.Coordinates
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.av.urbanway.R
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun UrbanWayMapView(
    currentLocation: Coordinates?,
    mapConfig: GoogleMapsConfig,
    modifier: Modifier = Modifier,
    onMapReady: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Default to Turin city center if no location
    val defaultLocation = LatLng(
        GoogleMapsConfig.TURIN_LAT,
        GoogleMapsConfig.TURIN_LNG
    )
    
    val mapLocation = currentLocation?.let { 
        LatLng(it.lat, it.lng) 
    } ?: defaultLocation
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapLocation, GoogleMapsConfig.DEFAULT_ZOOM)
    }
    
    if (!mapConfig.isApiKeyConfigured()) {
        GoogleMapsErrorScreen(
            onRetry = onMapReady,
            modifier = modifier
        )
        return
    }
    
    val style: MapStyleOptions? = remember { MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_minimal) }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false,
            mapType = MapType.NORMAL,
            mapStyleOptions = style
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            zoomControlsEnabled = false,
            compassEnabled = true
        )
    ) {
        // Intentionally empty: streets-only style
    }
    
    // Update camera when location changes
    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            val newPosition = LatLng(location.lat, location.lng)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    newPosition,
                    GoogleMapsConfig.DEFAULT_ZOOM
                ),
                durationMs = 1000
            )
        }
    }
    
    LaunchedEffect(Unit) {
        onMapReady()
    }
}
