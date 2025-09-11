package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.av.urbanway.data.local.GoogleMapsConfig
import com.av.urbanway.data.models.Coordinates
import com.av.urbanway.presentation.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun FullscreenMapCardView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val nearbyStops by viewModel.nearbyStops.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val scope = rememberCoroutineScope()
    
    var lastCenter by remember { mutableStateOf<LatLng?>(null) }
    
    // Default to current location or Turin center
    val initialLocation = currentLocation?.coordinates?.let { 
        LatLng(it.lat, it.lng) 
    } ?: LatLng(
        GoogleMapsConfig.TURIN_LAT,
        GoogleMapsConfig.TURIN_LNG
    )
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 16f)
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.NORMAL,
                maxZoomPreference = 17f
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                zoomControlsEnabled = false,
                compassEnabled = true
            ),
            onMapLoaded = {
                // Set initial last center
                lastCenter = cameraPositionState.position.target
            }
        ) {
            // Show nearby stops as markers
            nearbyStops.forEach { stop ->
                Marker(
                    state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
                    title = stop.stopName,
                    snippet = stop.routes.joinToString(", ")
                )
            }
        }
        
        // Red dot overlay centered
        Box(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = Color.Red.copy(alpha = 0.85f),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        color = Color.Transparent,
                        shape = CircleShape
                    )
                    .run {
                        // Add white border
                        this.then(
                            Modifier.background(
                                color = Color.White,
                                shape = CircleShape
                            )
                        ).padding(2.dp).background(
                            color = Color.Red.copy(alpha = 0.85f),
                            shape = CircleShape
                        )
                    }
            )
        }
    }
    
    // Monitor camera position changes
    LaunchedEffect(cameraPositionState.position.target) {
        val currentCenter = cameraPositionState.position.target
        lastCenter?.let { last ->
            val distance = calculateDistance(last, currentCenter)
            if (distance > 60) { // 60 meters threshold like iOS
                // Refresh nearby data at new center
                scope.launch {
                    refreshNearbyAtCenter(viewModel, currentCenter)
                    lastCenter = currentCenter
                }
            }
        }
    }
    
    // Handle drag gestures
    LaunchedEffect(Unit) {
        // Set initial location if available
        currentLocation?.coordinates?.let { coords ->
            val newPosition = LatLng(coords.lat, coords.lng)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(newPosition, 16f),
                durationMs = 1000
            )
            lastCenter = newPosition
        }
    }
}

private fun calculateDistance(from: LatLng, to: LatLng): Double {
    val R = 6371000.0 // Earth's radius in meters
    val dLat = Math.toRadians(to.latitude - from.latitude)
    val dLng = Math.toRadians(to.longitude - from.longitude)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) *
            sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

private suspend fun refreshNearbyAtCenter(viewModel: MainViewModel, center: LatLng) {
    // Update the location in the view model and reload nearby data
    // This would need to be implemented in the view model
    // For now, we can trigger a reload of nearby data
    viewModel.loadNearbyData()
}