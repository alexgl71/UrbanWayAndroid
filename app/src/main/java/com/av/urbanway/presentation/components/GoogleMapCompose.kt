package com.av.urbanway.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import com.av.urbanway.data.local.GoogleMapsConfig
import com.av.urbanway.data.models.StopInfo
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.av.urbanway.R
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.RectF
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color as ComposeColor
import kotlin.math.*

@Composable
fun UrbanWayMapView(
    currentLocation: com.av.urbanway.data.models.Coordinates?,
    mapConfig: GoogleMapsConfig,
    modifier: Modifier = Modifier,
    stops: List<StopInfo> = emptyList(),
    refreshBoundsKey: Any? = null,
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
        position = CameraPosition.fromLatLngZoom(mapLocation, 16f)
    }
    var visibleBounds by remember { mutableStateOf<LatLngBounds?>(null) }
    val markerRefs = remember { mutableStateMapOf<String, Marker>() }
    
    if (!mapConfig.isApiKeyConfigured()) {
        GoogleMapsErrorScreen(
            onRetry = onMapReady,
            modifier = modifier
        )
        return
    }
    
    val style: MapStyleOptions? = remember { MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_minimal) }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.NORMAL,
                mapStyleOptions = style,
                maxZoomPreference = 17f
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                zoomControlsEnabled = false,
                compassEnabled = true
            )
        ) {
        // Imperative marker diffing and reuse to prevent flicker
        val density = LocalDensity.current
        val busIcon: BitmapDescriptor = remember(key1 = density) { com.av.urbanway.presentation.components.makeBusMarkerDescriptor(density, 28.dp) }
        val bounds = visibleBounds
        val centerNow = cameraPositionState.position.target
        val zoomNow = cameraPositionState.position.zoom
        val fallbackRadius = when {
            zoomNow >= 17f -> 1000f
            zoomNow >= 16f -> 800f
            else -> 600f
        }
        val stopsInView = if (bounds != null) {
            stops.filter { stop -> bounds.contains(LatLng(stop.stopLat, stop.stopLon)) }
        } else {
            // Fallback before bounds are known: radial filter around current center
            stops.filter { stop ->
                distanceMeters(centerNow, LatLng(stop.stopLat, stop.stopLon)) <= fallbackRadius
            }
        }

        MapEffect(stopsInView, busIcon) { map ->
            val desiredIds = stopsInView.map { it.stopId }.toSet()
            // Remove markers that are no longer visible
            val toRemove = markerRefs.keys - desiredIds
            toRemove.forEach { id ->
                markerRefs[id]?.remove()
                markerRefs.remove(id)
            }
            // Add or update markers in view
            stopsInView.forEach { stop ->
                val id = stop.stopId
                val pos = LatLng(stop.stopLat, stop.stopLon)
                val existing = markerRefs[id]
                if (existing == null) {
                    val options = MarkerOptions()
                        .position(pos)
                        .title(stop.stopName)
                        .snippet(stop.routes.joinToString(","))
                        .anchor(0.5f, 0.5f)
                        .icon(busIcon)
                    val marker = map.addMarker(options)
                    if (marker != null) markerRefs[id] = marker
                } else {
                    if (existing.position != pos) existing.position = pos
                    if (existing.title != stop.stopName) existing.title = stop.stopName
                    val newSnippet = stop.routes.joinToString(",")
                    if (existing.snippet != newSnippet) existing.snippet = newSnippet
                }
            }
        }

        // Attach camera idle listener within Map scope to update bounds only
        MapEffect(Unit) { map ->
            map.setOnCameraIdleListener {
                // Update visible bounds to filter markers to viewport only
                visibleBounds = map.projection.visibleRegion.latLngBounds
            }
            map.setOnMapLoadedCallback {
                // Capture initial bounds as soon as the map finishes rendering
                visibleBounds = map.projection.visibleRegion.latLngBounds
            }
        }

        // Force bounds refresh when external key changes (e.g., sheet expansion finished)
        MapEffect(refreshBoundsKey) { map ->
            // Immediate read (may still be old if layout not applied yet)
            visibleBounds = map.projection.visibleRegion.latLngBounds
            // Post a delayed read to capture bounds after map re-layout
            Handler(Looper.getMainLooper()).postDelayed({
                visibleBounds = map.projection.visibleRegion.latLngBounds
            }, 120)
        }
    }

    // Initial loading indicator until we know visible bounds (overlay above the map)
    if (visibleBounds == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
    }

    
    // Update camera when location changes
    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            val newPosition = LatLng(location.lat, location.lng)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    newPosition,
                    16f
                ),
                durationMs = 1000
            )
        }
    }
    LaunchedEffect(Unit) {
        onMapReady()
    }
}

private fun distanceMeters(a: LatLng, b: LatLng): Float {
    val R = 6371000.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLng = Math.toRadians(b.longitude - a.longitude)
    val sinLat = kotlin.math.sin(dLat / 2)
    val sinLng = kotlin.math.sin(dLng / 2)
    val aa = sinLat * sinLat + kotlin.math.cos(Math.toRadians(a.latitude)) * kotlin.math.cos(Math.toRadians(b.latitude)) * sinLng * sinLng
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(aa), kotlin.math.sqrt(1 - aa))
    return (R * c).toFloat()
}

    
