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
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
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
import androidx.compose.ui.geometry.Offset
import kotlin.math.*

@Composable
fun UrbanWayMapView(
    currentLocation: com.av.urbanway.data.models.Coordinates?,
    mapConfig: GoogleMapsConfig,
    modifier: Modifier = Modifier,
    stops: List<StopInfo> = emptyList(),
    refreshBoundsKey: Any? = null,
    routeTripDetails: com.av.urbanway.data.models.TripDetailsResponse? = null,
    selectedStopId: String? = null,
    uiState: com.av.urbanway.data.models.UIState = com.av.urbanway.data.models.UIState.NORMAL,
    isSheetAnimating: Boolean = false,
    selectedPlace: com.av.urbanway.presentation.viewmodels.SelectedPlaceData? = null,
    selectedJourney: com.av.urbanway.data.models.JourneyOption? = null,
    onMapReady: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Default to Turin city center if no location
    val defaultLocation = LatLng(
        GoogleMapsConfig.TURIN_LAT,
        GoogleMapsConfig.TURIN_LNG
    )
    
    // Use selected place location if available, otherwise current location
    val mapLocation = selectedPlace?.let {
        LatLng(it.coordinates.lat, it.coordinates.lng)
    } ?: currentLocation?.let { 
        LatLng(it.lat, it.lng) 
    } ?: defaultLocation
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapLocation, 16f)
    }
    
    // Move camera to selected place when it changes
    LaunchedEffect(selectedPlace) {
        selectedPlace?.let { place ->
            val placeLocation = LatLng(place.coordinates.lat, place.coordinates.lng)
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(placeLocation, 17f)
            )
        }
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
        val busIcon: BitmapDescriptor = remember(key1 = density) { com.av.urbanway.presentation.components.makeBusMarkerDescriptor(density, 12.dp) }
        val selectedBusIcon: BitmapDescriptor = remember(key1 = density) { com.av.urbanway.presentation.components.makeSelectedBusMarkerDescriptor(density, 14.dp) }
        val routeStopIcon: BitmapDescriptor = remember(key1 = density) { com.av.urbanway.presentation.components.makeRouteStopMarkerDescriptor(density, 10.dp) }
        val selectedRouteStopIcon: BitmapDescriptor = remember(key1 = density) { com.av.urbanway.presentation.components.makeSelectedRouteStopMarkerDescriptor(density, 11.dp) }
        val startMarkerIcon: BitmapDescriptor = remember(key1 = density) { com.av.urbanway.presentation.components.makeStartMarkerDescriptor(density, 34.dp) }
        val endMarkerIcon: BitmapDescriptor = remember(key1 = density) { com.av.urbanway.presentation.components.makeEndMarkerDescriptor(density, 34.dp) }
        val bounds = visibleBounds
        val centerNow = cameraPositionState.position.target
        val zoomNow = cameraPositionState.position.zoom
        val fallbackRadius = when {
            zoomNow >= 17f -> 1000f
            zoomNow >= 16f -> 800f
            else -> 600f
        }
        // When a place is selected, hide all stops (like iOS behavior)
        val filteredStops = if (selectedPlace != null) {
            // Selected place mode - show no transit stops, only the selected place pin
            emptyList()
        } else if (isSheetAnimating) {
            // During sheet animation - show no stops to prevent redrawing
            emptyList()
        } else {
            when (uiState) {
                com.av.urbanway.data.models.UIState.ROUTE_DETAIL -> {
                    if (routeTripDetails != null) {
                        // Show only route stops with special icon
                        val routeStopIds = routeTripDetails.stops.orEmpty().map { it.stopId }.toSet()
                        stops.filter { stop -> stop.stopId in routeStopIds }
                    } else {
                        // Loading state - show no stops
                        emptyList()
                    }
                }
                com.av.urbanway.data.models.UIState.NORMAL -> {
                    // Show closest 25 stops to user location for performance
                    currentLocation?.let { userCoords ->
                        val userLatLng = LatLng(userCoords.lat, userCoords.lng)
                        stops.sortedBy { stop ->
                            distanceMeters(userLatLng, LatLng(stop.stopLat, stop.stopLon))
                        }.take(25)
                    } ?: stops.take(25) // Fallback if no location
                }
                else -> emptyList() // Other states: no stops
            }
        }
        
        // Dynamic viewport filtering for drag-to-reveal functionality
        val stopsInView = if (bounds != null) {
            filteredStops.filter { stop -> bounds.contains(LatLng(stop.stopLat, stop.stopLon)) }
        } else {
            // Fallback before bounds are known: radial filter around current center
            filteredStops.filter { stop ->
                distanceMeters(centerNow, LatLng(stop.stopLat, stop.stopLon)) <= fallbackRadius
            }
        }

        // Only trigger MapEffect when absolutely necessary
        MapEffect(stopsInView, uiState, selectedStopId) { map ->
            val desiredIds = stopsInView.map { it.stopId }.toSet()
            // Remove markers that are no longer visible
            val toRemove = markerRefs.keys - desiredIds
            toRemove.forEach { id ->
                markerRefs[id]?.remove()
                markerRefs.remove(id)
            }
            // Add or update markers in view (batch operations for performance)
            stopsInView.forEach { stop ->
                val id = stop.stopId
                val pos = LatLng(stop.stopLat, stop.stopLon)
                val isSelected = selectedStopId == stop.stopId
                
                // Determine icon once and cache the decision
                val icon = when {
                    isSelected && uiState == com.av.urbanway.data.models.UIState.ROUTE_DETAIL -> selectedRouteStopIcon
                    isSelected -> selectedBusIcon
                    uiState == com.av.urbanway.data.models.UIState.ROUTE_DETAIL -> routeStopIcon
                    else -> busIcon
                }
                
                val existing = markerRefs[id]
                if (existing == null) {
                    // Create new marker only if doesn't exist
                    val options = MarkerOptions()
                        .position(pos)
                        .title(stop.stopName)
                        .snippet(stop.routes.joinToString(","))
                        .anchor(0.5f, 0.5f)
                        .icon(icon)
                    val marker = map.addMarker(options)
                    if (marker != null) markerRefs[id] = marker
                } else {
                    // Update existing marker efficiently (only if changed)
                    if (existing.position != pos) existing.position = pos
                    if (existing.title != stop.stopName) existing.title = stop.stopName
                    val newSnippet = stop.routes.joinToString(",")
                    if (existing.snippet != newSnippet) existing.snippet = newSnippet
                    // Only update icon if it's actually different (expensive operation)
                    existing.setIcon(icon)
                }
            }
        }

        // Selected place pin (like iOS - classic map pin)
        selectedPlace?.let { place ->
            val placeLocation = LatLng(place.coordinates.lat, place.coordinates.lng)
            
            // Add a classic red pin marker for the selected place
            com.google.maps.android.compose.Marker(
                state = com.google.maps.android.compose.MarkerState(position = placeLocation),
                title = place.name,
                snippet = place.description,
                anchor = Offset(0.5f, 1.0f), // Bottom center anchor for classic pin look
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
            
            // Center map on selected place
            LaunchedEffect(place.coordinates) {
                kotlinx.coroutines.delay(200) // Small delay to avoid conflicts
                try {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(
                            placeLocation,
                            16f // Good zoom level for viewing a selected place
                        ),
                        durationMs = 600
                    )
                } catch (e: Exception) {
                    android.util.Log.w("GoogleMapCompose", "Failed to center on selected place: ${e.message}")
                }
            }
        }

        // Route polyline rendering with auto-zoom
        routeTripDetails?.let { tripDetails ->
            val routePolylinePoints = remember(tripDetails) {
                tripDetails.stops.orEmpty().map { stop ->
                    LatLng(stop.stopLat, stop.stopLon)
                }
            }

            if (routePolylinePoints.size >= 2) {
                com.google.maps.android.compose.Polyline(
                    points = routePolylinePoints,
                    color = ComposeColor(0xFF0B3D91), // Navy blue
                    width = 12f
                )

                // Center on the closest stop with perfect zoom level 14
                LaunchedEffect(routePolylinePoints, selectedStopId) {
                    // Small delay to avoid conflicts with other camera movements
                    kotlinx.coroutines.delay(300)

                    if (selectedStopId != null) {
                        // Find the selected stop position
                        val selectedStop = routeTripDetails?.stops?.find { it.stopId == selectedStopId }
                        if (selectedStop != null) {
                            // Center on selected stop with zoom level 14
                            try {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(
                                        LatLng(selectedStop.stopLat, selectedStop.stopLon),
                                        14f // Perfect zoom level - shows great context!
                                    ),
                                    durationMs = 800
                                )
                            } catch (e: Exception) {
                                android.util.Log.w("GoogleMapCompose", "Failed to center on selected stop: ${e.message}")
                            }
                        }
                    }
                }
            }
        }

        // Journey polyline rendering with accurate shape-based trimming
        if (uiState == com.av.urbanway.data.models.UIState.JOURNEY_VIEW && selectedJourney != null) {
            android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🗺️ Rendering accurate journey polylines")
            android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🗺️ Journey shapes1 size: ${selectedJourney.shapes?.size}")
            android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🗺️ Journey shapes2 size: ${selectedJourney.shapes2?.size}")

            // Use ONLY shapes data - no fallbacks, no stop-to-stop lines
            val primaryPoints = remember(selectedJourney.shapes) {
                android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔍 DEBUG: selectedJourney.shapes = ${selectedJourney.shapes}")
                android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔍 DEBUG: shapes size = ${selectedJourney.shapes?.size}")
                android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔍 DEBUG: first shape = ${selectedJourney.shapes?.firstOrNull()}")

                if (selectedJourney.shapes != null && selectedJourney.shapes!!.isNotEmpty()) {
                    android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🗺️ Using primary shapes: ${selectedJourney.shapes!!.size} points")
                    val points = ShapeUtils.extractCoordinatesFromShapes(selectedJourney.shapes!!)
                    android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔍 DEBUG: extracted ${points.size} valid LatLng points")
                    android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔍 DEBUG: first point = ${points.firstOrNull()}")
                    points
                } else {
                    android.util.Log.e("TRANSITOAPP", "TRANSITOAPP: 🚨 NO primary shapes data - no polyline will be drawn")
                    emptyList()
                }
            }

            val secondaryPoints = remember(selectedJourney.shapes2) {
                if (selectedJourney.shapes2 != null && selectedJourney.shapes2!!.isNotEmpty()) {
                    android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🗺️ Using secondary shapes: ${selectedJourney.shapes2!!.size} points")
                    ShapeUtils.extractCoordinatesFromShapes(selectedJourney.shapes2!!)
                } else {
                    android.util.Log.w("TRANSITOAPP", "TRANSITOAPP: 🗺️ NO secondary shapes data - no polyline will be drawn")
                    emptyList()
                }
            }

            android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🗺️ Accurate primary polyline points: ${primaryPoints.size}")
            android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🗺️ Accurate secondary polyline points: ${secondaryPoints?.size ?: 0}")

            // Primary route polyline (blue) - trimmed to actual stops
            if (primaryPoints.size >= 2) {
                android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🎨 DRAWING PRIMARY POLYLINE with ${primaryPoints.size} points")
                com.google.maps.android.compose.Polyline(
                    points = primaryPoints,
                    color = ComposeColor(0xFF007AFF), // iOS blue
                    width = 12f, // More prominent
                    pattern = null // Solid line
                )
                android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: ✅ Primary polyline rendered successfully")
            } else {
                android.util.Log.e("TRANSITOAPP", "TRANSITOAPP: ❌ NOT DRAWING PRIMARY POLYLINE - only ${primaryPoints.size} points")
            }

            // Secondary route polyline (orange) for transfers - trimmed to actual stops
            secondaryPoints?.let { points ->
                if (points.size >= 2) {
                    android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🎨 DRAWING SECONDARY POLYLINE with ${points.size} points")
                    com.google.maps.android.compose.Polyline(
                        points = points,
                        color = ComposeColor(0xFFFF9500), // iOS orange
                        width = 12f, // More prominent
                        pattern = null // Solid line
                    )
                    android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: ✅ Secondary polyline rendered successfully")
                } else {
                    android.util.Log.e("TRANSITOAPP", "TRANSITOAPP: ❌ NOT DRAWING SECONDARY POLYLINE - only ${points.size} points")
                }
            }

            // NO walking polylines - only real shapes data

            // Journey stop pins with special markers for start/end
            selectedJourney.stops?.let { stops ->
                android.util.Log.d("TRANSITOAPP", "🗺️ Rendering primary route stops: ${stops.size}")
                stops.forEachIndexed { index, stopData ->
                    val stopLat = stopData["stopLat"] as? Double
                    val stopLon = stopData["stopLon"] as? Double
                    val stopId = stopData["stopId"] as? String
                    val stopName = stopData["stopName"] as? String

                    if (stopLat != null && stopLon != null) {
                        val isFirstStop = index == 0
                        val isLastStop = index == stops.size - 1

                        when {
                            isFirstStop -> {
                                // Start marker for first stop
                                com.google.maps.android.compose.Marker(
                                    state = com.google.maps.android.compose.MarkerState(
                                        position = LatLng(stopLat, stopLon)
                                    ),
                                    title = stopName ?: "Start",
                                    anchor = Offset(0.5f, 0.5f),
                                    icon = startMarkerIcon
                                )
                            }
                            isLastStop -> {
                                // End marker for last stop (only if journey has no secondary route)
                                if (selectedJourney.stops2.isNullOrEmpty()) {
                                    com.google.maps.android.compose.Marker(
                                        state = com.google.maps.android.compose.MarkerState(
                                            position = LatLng(stopLat, stopLon)
                                        ),
                                        title = stopName ?: "End",
                                        anchor = Offset(0.5f, 0.5f),
                                        icon = endMarkerIcon
                                    )
                                } else {
                                    // Regular circle for intermediate stops in multi-segment journeys
                                    com.google.maps.android.compose.Circle(
                                        center = LatLng(stopLat, stopLon),
                                        radius = 10.0,
                                        fillColor = ComposeColor(0xFFFFFFFF),
                                        strokeColor = ComposeColor(0xFF007AFF),
                                        strokeWidth = 2f,
                                        clickable = false
                                    )
                                }
                            }
                            else -> {
                                // Regular circle for intermediate stops
                                com.google.maps.android.compose.Circle(
                                    center = LatLng(stopLat, stopLon),
                                    radius = 10.0,
                                    fillColor = ComposeColor(0xFFFFFFFF),
                                    strokeColor = ComposeColor(0xFF007AFF),
                                    strokeWidth = 2f,
                                    clickable = false
                                )
                            }
                        }
                    }
                }
            }

            // Secondary route stop pins for transfers
            selectedJourney.stops2?.let { stops2 ->
                android.util.Log.d("TRANSITOAPP", "🗺️ Rendering secondary route stops: ${stops2.size}")
                stops2.forEachIndexed { index, stopData ->
                    val stopLat = stopData["stopLat"] as? Double
                    val stopLon = stopData["stopLon"] as? Double
                    val stopId = stopData["stopId"] as? String
                    val stopName = stopData["stopName"] as? String

                    if (stopLat != null && stopLon != null) {
                        val isLastStop = index == stops2.size - 1

                        if (isLastStop) {
                            // End marker for last stop of secondary route
                            com.google.maps.android.compose.Marker(
                                state = com.google.maps.android.compose.MarkerState(
                                    position = LatLng(stopLat, stopLon)
                                ),
                                title = stopName ?: "End",
                                anchor = Offset(0.5f, 0.5f),
                                icon = endMarkerIcon
                            )
                        } else {
                            // Regular circle for intermediate stops
                            com.google.maps.android.compose.Circle(
                                center = LatLng(stopLat, stopLon),
                                radius = 10.0,
                                fillColor = ComposeColor(0xFFFFFFFF),
                                strokeColor = ComposeColor(0xFFFF9500),
                                strokeWidth = 2f,
                                clickable = false
                            )
                        }
                    }
                }
            }

            // Auto-fit camera to accurate journey bounds using trimmed polylines
            LaunchedEffect(selectedJourney) {
                kotlinx.coroutines.delay(500) // Wait for polylines to render

                val allPoints = mutableListOf<LatLng>()

                // Add trimmed primary route points
                allPoints.addAll(primaryPoints)

                // Add trimmed secondary route points
                secondaryPoints?.let { allPoints.addAll(it) }

                // Add current location if available
                currentLocation?.let {
                    allPoints.add(LatLng(it.lat, it.lng))
                }

                android.util.Log.d("TRANSITOAPP", "🗺️ Auto-fitting camera to ${allPoints.size} accurate points")

                if (allPoints.size >= 2) {
                    val boundsBuilder = LatLngBounds.Builder()
                    allPoints.forEach { boundsBuilder.include(it) }
                    val bounds = boundsBuilder.build()

                    try {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(bounds, 100)
                        )
                        android.util.Log.d("TRANSITOAPP", "🗺️ Camera fitted to accurate journey bounds")
                    } catch (e: Exception) {
                        android.util.Log.e("TRANSITOAPP", "🗺️ Failed to fit accurate journey bounds: ${e.message}")
                    }
                }
            }
        }

        // Reset camera to current location when route is cleared (debounced and conditional)
        LaunchedEffect(uiState, currentLocation) {
            // Only animate to user location if NO place is selected (selectedPlace takes priority)
            if (uiState == com.av.urbanway.data.models.UIState.NORMAL && currentLocation != null && selectedPlace == null) {
                // Small delay to avoid conflicts and ensure state is stable
                kotlinx.coroutines.delay(500)
                
                try {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(
                            LatLng(currentLocation.lat, currentLocation.lng), 
                            17f // Fixed zoom level for NORMAL state
                        ),
                        durationMs = 600 // Faster return animation
                    )
                } catch (e: Exception) {
                    android.util.Log.w("GoogleMapCompose", "Failed to return to user location: ${e.message}")
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
        // Only animate to user location if NO place is selected (selectedPlace takes priority)
        if (selectedPlace == null) {
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

    
