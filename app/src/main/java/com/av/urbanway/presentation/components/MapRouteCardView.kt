package com.av.urbanway.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.av.urbanway.data.local.GoogleMapsConfig
import com.av.urbanway.data.models.JourneyOption
import com.av.urbanway.data.models.Coordinates

/**
 * Android equivalent of iOS MapRouteCardView.swift
 * Wrapper component for displaying journey route on map within a card
 */
@Composable
fun MapRouteCardView(
    selectedJourney: JourneyOption?,
    startLocation: Coordinates?,
    endLocation: Coordinates?,
    modifier: Modifier = Modifier,
    mapHeight: androidx.compose.ui.unit.Dp = 300.dp,
    mapConfig: GoogleMapsConfig? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            // Map area
            Box(modifier = Modifier.height(mapHeight)) {
                RouteMapView(
                    selectedJourney = selectedJourney,
                    startLocation = startLocation,
                    endLocation = endLocation,
                    modifier = Modifier.fillMaxSize(),
                    mapConfig = mapConfig
                )
            }
        }
    }
}