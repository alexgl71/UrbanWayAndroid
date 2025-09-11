package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RouteDetailTabbedCard(
    routeId: String,
    tripId: String,
    destination: String,
    stops: List<RouteStopInfo>,
    isLoading: Boolean,
    selectedStopId: String? = null,
    tripDetails: com.av.urbanway.data.models.TripDetailsResponse? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxSize()
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = routeId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.weight(1f))
            }
            
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                TabButton(
                    title = "Fermate",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    title = "Mappa",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Stops tab - reuse RouteDetailCard content
                        RouteDetailCard(
                            routeId = routeId,
                            destination = destination,
                            stops = stops,
                            isLoading = isLoading,
                            selectedStopId = selectedStopId,
                            onBack = onBack,
                            onShowAllStops = {},
                            useFixedHeight = false
                        )
                    }
                    1 -> {
                        // Map tab - show map with trip details
                        MapRouteView(
                            routeId = routeId,
                            stops = stops,
                            selectedStopId = selectedStopId,
                            tripDetails = tripDetails
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (isSelected) Color.Blue else Color.Gray
        ),
        elevation = null,
        shape = RoundedCornerShape(0.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            
            // Bottom indicator line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        if (isSelected) Color.Blue else Color.Transparent
                    )
            )
        }
    }
}

@Composable
private fun MapRouteView(
    routeId: String,
    stops: List<RouteStopInfo>,
    selectedStopId: String?,
    tripDetails: com.av.urbanway.data.models.TripDetailsResponse? = null,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    if (tripDetails?.stops != null) {
        val mapStops = tripDetails.stops.map { tripStop ->
            com.av.urbanway.data.models.StopInfo(
                stopId = tripStop.stopId,
                stopName = tripStop.stopName,
                stopLat = tripStop.stopLat,
                stopLon = tripStop.stopLon,
                distanceToStop = 0,
                routes = emptyList()
            )
        }
        
        Box(modifier = modifier.fillMaxSize()) {
            UrbanWayMapView(
                currentLocation = null, // Route detail view doesn't need user location for centering
                mapConfig = com.av.urbanway.data.local.GoogleMapsConfig.getInstance(context),
                modifier = Modifier.fillMaxSize(),
                stops = mapStops,
                routeTripDetails = tripDetails,
                selectedStopId = selectedStopId,
                selectedPlace = null // Route detail doesn't show selected places
            )
        }
    } else {
        // Show error state instead of fallback data
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE) // Light red background
                ),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚠️ Errore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = "Impossibile caricare i dettagli del percorso",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF424242),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "Linea: $routeId",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun RouteDetailTabbedCardPreview() {
    val sampleStops = listOf(
        RouteStopInfo("1", "Fermata 646 - ADRIANO", "646", 3600, false),
        RouteStopInfo("2", "Fermata 595 - VIA ROMA", "595", 3720, true),
        RouteStopInfo("3", "Fermata 201 - PIAZZA CASTELLO", "201", 3840, false),
        RouteStopInfo("4", "Fermata 102 - PORTA NUOVA", "102", 3960, false)
    )
    
    RouteDetailTabbedCard(
        routeId = "15U",
        tripId = "trip123",
        destination = "Centro",
        stops = sampleStops,
        isLoading = false,
        selectedStopId = "2",
        tripDetails = null,
        onBack = {}
    )
}