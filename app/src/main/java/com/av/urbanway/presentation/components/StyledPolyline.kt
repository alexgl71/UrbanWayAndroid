package com.av.urbanway.presentation.components

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap

/**
 * Android equivalent of iOS StyledPolyline
 * Represents a styled polyline with color and dash pattern properties
 */
data class StyledPolyline(
    val points: List<LatLng>,
    val strokeColor: Color,
    val isPast: Boolean = false,
    val lineWidth: Float = if (isPast) 6f else 12f,
    val id: String = "${points.hashCode()}_${strokeColor.value}_$isPast"
) {
    /**
     * Get dash pattern for walking/past segments (matching iOS dashed style)
     */
    val dashPattern: List<PatternItem>?
        get() = if (isPast) {
            listOf(Dash(4f), Gap(6f)) // Same as iOS [4, 6] pattern
        } else {
            null // Solid line for active routes
        }

    companion object {
        // Brand colors matching iOS implementation
        val brandBlue = Color(0xFF007AFF)      // iOS systemBlue
        val brandOrange = Color(0xFFFF9500)    // iOS systemOrange
        val brandGray = Color(0xFF8E8E93)      // iOS systemGray

        /**
         * Create styled polyline for primary transport segment (blue)
         */
        fun createPrimaryRoute(points: List<LatLng>): StyledPolyline {
            return StyledPolyline(
                points = points,
                strokeColor = brandBlue,
                isPast = false
            )
        }

        /**
         * Create styled polyline for secondary transport segment (orange)
         */
        fun createSecondaryRoute(points: List<LatLng>): StyledPolyline {
            return StyledPolyline(
                points = points,
                strokeColor = brandOrange,
                isPast = false
            )
        }

        /**
         * Create styled polyline for walking connector (gray, dashed)
         */
        fun createWalkingConnector(points: List<LatLng>): StyledPolyline {
            return StyledPolyline(
                points = points,
                strokeColor = brandGray,
                isPast = true // Uses dashed styling
            )
        }
    }
}