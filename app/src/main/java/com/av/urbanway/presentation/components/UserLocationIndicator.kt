package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
fun UserLocationIndicator(
    userLocation: LatLng,
    accuracyMeters: Float = 20f, // Default accuracy radius
    showAccuracy: Boolean = true
) {
    val density = LocalDensity.current

    // Accuracy circle (light blue, semi-transparent)
    if (showAccuracy && accuracyMeters > 0) {
        Circle(
            center = userLocation,
            radius = accuracyMeters.toDouble(),
            fillColor = Color(0x1A007AFF), // Light blue with transparency
            strokeColor = Color(0x4D007AFF), // Slightly more opaque blue border
            strokeWidth = 1f,
            clickable = false
        )
    }

    // User location dot (blue dot with white border - classic GPS style)
    val userLocationIcon: BitmapDescriptor = remember(density) {
        makeUserLocationMarkerDescriptor(density, 20.dp)
    }

    Marker(
        state = MarkerState(position = userLocation),
        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f), // Center the marker
        icon = userLocationIcon
    )
}

@Composable
fun UserLocationStaleIndicator(
    userLocation: LatLng,
    accuracyMeters: Float = 50f
) {
    val density = LocalDensity.current

    // Stale location indicator (gray instead of blue)
    if (accuracyMeters > 0) {
        Circle(
            center = userLocation,
            radius = accuracyMeters.toDouble(),
            fillColor = Color(0x1A808080), // Light gray with transparency
            strokeColor = Color(0x4D808080), // Gray border
            strokeWidth = 1f,
            clickable = false
        )
    }

    // Stale user location dot (gray dot with white border)
    val staleLocationIcon: BitmapDescriptor = remember(density) {
        makeStaleLocationMarkerDescriptor(density, 20.dp)
    }

    Marker(
        state = MarkerState(position = userLocation),
        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
        icon = staleLocationIcon
    )
}