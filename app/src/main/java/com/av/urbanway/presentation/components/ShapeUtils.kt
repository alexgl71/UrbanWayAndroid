package com.av.urbanway.presentation.components

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

/**
 * Utilities for working with journey shapes and creating accurate polylines
 * based on GPS coordinates instead of straight lines between stops
 */
object ShapeUtils {

    /**
     * Calculate distance between two coordinates in meters using Haversine formula
     */
    private fun distanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // Earth's radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    /**
     * Find the index of the shape point closest to a given stop coordinate
     */
    fun findClosestShapeIndex(
        shapes: List<Map<String, Double>>,
        stopLat: Double,
        stopLon: Double
    ): Int? {
        if (shapes.isEmpty()) return null

        var closestIndex = 0
        var minDistance = Double.MAX_VALUE

        shapes.forEachIndexed { index, shape ->
            val shapeLat = shape["lat"] ?: shape["shapePtLat"]
            val shapeLon = shape["lon"] ?: shape["shapePtLon"]

            if (shapeLat != null && shapeLon != null) {
                val distance = distanceInMeters(stopLat, stopLon, shapeLat, shapeLon)
                if (distance < minDistance) {
                    minDistance = distance
                    closestIndex = index
                }
            }
        }

        return closestIndex
    }

    /**
     * Extract coordinates from shape data with proper validation
     */
    fun extractCoordinatesFromShapes(shapes: List<Map<String, Double>>): List<LatLng> {
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: ShapeUtils extracting ${shapes.size} shapes")
        val result = shapes.mapNotNull { shapePoint ->
            val lat = shapePoint["lat"] ?: shapePoint["shapePtLat"]
            val lon = shapePoint["lon"] ?: shapePoint["shapePtLon"]

            if (lat != null && lon != null &&
                lat >= -90.0 && lat <= 90.0 &&
                lon >= -180.0 && lon <= 180.0) {
                LatLng(lat, lon)
            } else {
                android.util.Log.w("TRANSITOAPP", "TRANSITOAPP: Invalid coordinates: lat=$lat, lon=$lon")
                null
            }
        }
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: ShapeUtils extracted ${result.size} valid coordinates")
        return result
    }

    /**
     * Create trimmed polyline for a journey leg based on start and end stops
     */
    fun createTrimmedPolyline(
        shapes: List<Map<String, Double>>,
        startStopLat: Double,
        startStopLon: Double,
        endStopLat: Double,
        endStopLon: Double
    ): List<LatLng> {
        if (shapes.isEmpty()) return emptyList()

        // Find closest shape points to start and end stops
        val startIndex = findClosestShapeIndex(shapes, startStopLat, startStopLon) ?: 0
        val endIndex = findClosestShapeIndex(shapes, endStopLat, endStopLon) ?: (shapes.size - 1)

        // Ensure we have a valid range
        val fromIndex = minOf(startIndex, endIndex)
        val toIndex = maxOf(startIndex, endIndex)

        android.util.Log.d("ShapeUtils", "Trimming shapes from index $fromIndex to $toIndex (total: ${shapes.size})")

        // Extract the relevant subset of shapes
        val trimmedShapes = shapes.subList(fromIndex, minOf(toIndex + 1, shapes.size))

        return extractCoordinatesFromShapes(trimmedShapes)
    }

    /**
     * Create accurate journey polylines by trimming shapes to actual start/end stops
     */
    fun createAccurateJourneyPolylines(
        journey: com.av.urbanway.data.models.JourneyOption
    ): Pair<List<LatLng>, List<LatLng>?> {

        // Primary leg polyline
        val primaryPolyline = if (journey.shapes != null && journey.stops != null && journey.stops!!.isNotEmpty()) {
            val firstStop = journey.stops!!.first()
            val lastStop = journey.stops!!.last()

            val startLat = firstStop["stopLat"] as? Double
            val startLon = firstStop["stopLon"] as? Double
            val endLat = lastStop["stopLat"] as? Double
            val endLon = lastStop["stopLon"] as? Double

            if (startLat != null && startLon != null && endLat != null && endLon != null) {
                android.util.Log.d("ShapeUtils", "Primary leg: start=($startLat,$startLon), end=($endLat,$endLon)")
                createTrimmedPolyline(journey.shapes!!, startLat, startLon, endLat, endLon)
            } else {
                android.util.Log.w("ShapeUtils", "Invalid stop coordinates for primary leg: start=($startLat,$startLon), end=($endLat,$endLon)")
                extractCoordinatesFromShapes(journey.shapes!!)
            }
        } else {
            android.util.Log.w("ShapeUtils", "No shapes or stops for primary leg: shapes=${journey.shapes?.size}, stops=${journey.stops?.size}")
            emptyList()
        }

        // Secondary leg polyline (for transfers)
        val secondaryPolyline = if (journey.shapes2 != null && journey.stops2 != null && journey.stops2!!.isNotEmpty()) {
            val firstStop = journey.stops2!!.first()
            val lastStop = journey.stops2!!.last()

            val startLat = firstStop["stopLat"] as? Double
            val startLon = firstStop["stopLon"] as? Double
            val endLat = lastStop["stopLat"] as? Double
            val endLon = lastStop["stopLon"] as? Double

            if (startLat != null && startLon != null && endLat != null && endLon != null) {
                createTrimmedPolyline(journey.shapes2!!, startLat, startLon, endLat, endLon)
            } else {
                android.util.Log.w("ShapeUtils", "Invalid stop coordinates for secondary leg")
                extractCoordinatesFromShapes(journey.shapes2!!)
            }
        } else {
            null
        }

        android.util.Log.d("ShapeUtils", "Created accurate polylines: primary=${primaryPolyline.size}, secondary=${secondaryPolyline?.size ?: 0}")

        if (primaryPolyline.isEmpty()) {
            android.util.Log.e("ShapeUtils", "Primary polyline is empty - NO FALLBACK, showing nothing")
        }

        return Pair(primaryPolyline, secondaryPolyline)
    }
}