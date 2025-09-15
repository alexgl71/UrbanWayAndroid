package com.av.urbanway.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.av.urbanway.data.local.GoogleMapsConfig
import com.av.urbanway.data.models.JourneyOption
import com.av.urbanway.data.models.Coordinates
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlin.math.*

/**
 * Android equivalent of iOS RouteMapView.swift
 * Displays journey routes with multi-segment polylines and color coding
 */
@Composable
fun RouteMapView(
    selectedJourney: JourneyOption?,
    startLocation: Coordinates?,
    endLocation: Coordinates?,
    modifier: Modifier = Modifier,
    mapConfig: GoogleMapsConfig? = null,
    onMapReady: () -> Unit = {}
) {
    val context = LocalContext.current

    // Default to Turin city center if no location
    val defaultLocation = LatLng(
        GoogleMapsConfig.TURIN_LAT,
        GoogleMapsConfig.TURIN_LNG
    )

    // Calculate initial camera position
    val initialLocation = startLocation?.let {
        LatLng(it.lat, it.lng)
    } ?: defaultLocation

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 14f)
    }

    val actualMapConfig = mapConfig ?: GoogleMapsConfig.getInstance(context)

    if (!actualMapConfig.isApiKeyConfigured()) {
        GoogleMapsErrorScreen(
            onRetry = onMapReady,
            modifier = modifier
        )
        return
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            zoomControlsEnabled = false,
            compassEnabled = true
        )
    ) {
        // Draw journey polylines
        selectedJourney?.let { journey ->
            RenderJourneyPolylines(
                journey = journey,
                startLocation = startLocation,
                endLocation = endLocation
            )

            // Auto-fit camera to journey bounds
            LaunchedEffect(journey) {
                val bounds = calculateJourneyBounds(journey, startLocation, endLocation)
                bounds?.let { boundsRect ->
                    try {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngBounds(boundsRect, 100)
                        )
                    } catch (e: Exception) {
                        android.util.Log.w("RouteMapView", "Failed to fit journey bounds: ${e.message}")
                    }
                }
            }
        }

        // Start and end location markers
        startLocation?.let { start ->
            Marker(
                state = MarkerState(position = LatLng(start.lat, start.lng)),
                title = "Partenza"
            )
        }

        endLocation?.let { end ->
            Marker(
                state = MarkerState(position = LatLng(end.lat, end.lng)),
                title = "Arrivo"
            )
        }
    }

    LaunchedEffect(Unit) {
        onMapReady()
    }
}

/**
 * Renders journey polylines with iOS-style color coding and segments
 */
@Composable
private fun RenderJourneyPolylines(
    journey: JourneyOption,
    startLocation: Coordinates?,
    endLocation: Coordinates?
) {
    // Extract coordinates from journey shapes (matching iOS logic)
    val primaryShapes = journey.shapes?.let { shapes ->
        extractCoordinatesFromShapes(shapes)
    } ?: emptyList()

    val secondaryShapes = journey.shapes2?.let { shapes ->
        extractCoordinatesFromShapes(shapes)
    } ?: emptyList()

    // Primary route polyline (blue)
    if (primaryShapes.isNotEmpty()) {
        val styledPolyline = StyledPolyline.createPrimaryRoute(primaryShapes)

        Polyline(
            points = styledPolyline.points,
            color = styledPolyline.strokeColor,
            width = styledPolyline.lineWidth,
            pattern = styledPolyline.dashPattern
        )
    }

    // Secondary route polyline (orange) for transfers
    if (secondaryShapes.isNotEmpty()) {
        val styledPolyline = StyledPolyline.createSecondaryRoute(secondaryShapes)

        Polyline(
            points = styledPolyline.points,
            color = styledPolyline.strokeColor,
            width = styledPolyline.lineWidth,
            pattern = styledPolyline.dashPattern
        )
    }

    // Walking connector polylines (gray, dashed)
    val walkingConnectors = createWalkingConnectors(
        journey = journey,
        primaryShapes = primaryShapes,
        secondaryShapes = secondaryShapes,
        startLocation = startLocation,
        endLocation = endLocation
    )

    walkingConnectors.forEach { connector ->
        Polyline(
            points = connector.points,
            color = connector.strokeColor,
            width = connector.lineWidth,
            pattern = connector.dashPattern
        )
    }
}

/**
 * Extract coordinates from shape data (matching iOS coordinate handling)
 */
private fun extractCoordinatesFromShapes(shapes: List<Map<String, Double>>): List<LatLng> {
    return shapes.mapNotNull { shapePoint ->
        val lat = shapePoint["lat"] ?: shapePoint["shapePtLat"]
        val lon = shapePoint["lon"] ?: shapePoint["shapePtLon"]

        if (lat != null && lon != null) {
            // Validate coordinate ranges (matching iOS validation)
            if (lat >= -90.0 && lat <= 90.0 && lon >= -180.0 && lon <= 180.0) {
                LatLng(lat, lon)
            } else {
                android.util.Log.w("RouteMapView", "Invalid coordinates: lat=$lat, lon=$lon")
                null
            }
        } else {
            null
        }
    }
}

/**
 * Create walking connector polylines between segments
 */
private fun createWalkingConnectors(
    journey: JourneyOption,
    primaryShapes: List<LatLng>,
    secondaryShapes: List<LatLng>,
    startLocation: Coordinates?,
    endLocation: Coordinates?
): List<StyledPolyline> {
    val connectors = mutableListOf<StyledPolyline>()

    // Start walking segment (to first route)
    if (startLocation != null && primaryShapes.isNotEmpty()) {
        val startPoint = LatLng(startLocation.lat, startLocation.lng)
        val firstRoutePoint = primaryShapes.first()

        if (startPoint != firstRoutePoint) {
            connectors.add(
                StyledPolyline.createWalkingConnector(listOf(startPoint, firstRoutePoint))
            )
        }
    }

    // Transfer walking segment (between routes)
    if (journey.isDirect == 0 && primaryShapes.isNotEmpty() && secondaryShapes.isNotEmpty()) {
        val primaryEnd = primaryShapes.last()
        val secondaryStart = secondaryShapes.first()

        if (primaryEnd != secondaryStart) {
            connectors.add(
                StyledPolyline.createWalkingConnector(listOf(primaryEnd, secondaryStart))
            )
        }
    }

    // End walking segment (from last route)
    if (endLocation != null) {
        val endPoint = LatLng(endLocation.lat, endLocation.lng)
        val lastRoutePoint = when {
            secondaryShapes.isNotEmpty() -> secondaryShapes.last()
            primaryShapes.isNotEmpty() -> primaryShapes.last()
            else -> null
        }

        if (lastRoutePoint != null && lastRoutePoint != endPoint) {
            connectors.add(
                StyledPolyline.createWalkingConnector(listOf(lastRoutePoint, endPoint))
            )
        }
    }

    return connectors
}

/**
 * Calculate bounds for journey to auto-fit camera
 */
private fun calculateJourneyBounds(
    journey: JourneyOption,
    startLocation: Coordinates?,
    endLocation: Coordinates?
): LatLngBounds? {
    val allPoints = mutableListOf<LatLng>()

    // Add start and end points
    startLocation?.let { allPoints.add(LatLng(it.lat, it.lng)) }
    endLocation?.let { allPoints.add(LatLng(it.lat, it.lng)) }

    // Add shape points
    journey.shapes?.let { shapes ->
        allPoints.addAll(extractCoordinatesFromShapes(shapes))
    }

    journey.shapes2?.let { shapes ->
        allPoints.addAll(extractCoordinatesFromShapes(shapes))
    }

    return if (allPoints.isNotEmpty()) {
        val boundsBuilder = LatLngBounds.Builder()
        allPoints.forEach { point ->
            boundsBuilder.include(point)
        }
        boundsBuilder.build()
    } else {
        null
    }
}