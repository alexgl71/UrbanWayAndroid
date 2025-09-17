package com.av.urbanway.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.data.models.NearbyDeparturesResponse
import com.av.urbanway.data.models.UIState
import com.av.urbanway.presentation.viewmodels.MainViewModel

@Composable
fun HomePage(
    viewModel: MainViewModel,
    onNavigateToRealtime: () -> Unit,
    onNavigateToRouteDetail: () -> Unit
) {
    val pinnedArrivals by viewModel.pinnedArrivals.collectAsState()
    // Collect to trigger recomposition when API data updates
    val nearbyDepartures by viewModel.nearbyDepartures.collectAsState()
    val nearbyStops by viewModel.nearbyStops.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Search bar with address display - shows selected place when available, otherwise current location
        AddressSearchBar(
            address = selectedPlace?.name ?: currentLocation?.address ?: "Ricerca posizione...",
            onClick = { 
                if (selectedPlace != null) {
                    // Clear selected place and return to search
                    viewModel.clearSelectedPlace()
                }
                viewModel.openSearch() 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // ChatView - shown only when there are route arrivals
        ChatView(
            waitingTimes = viewModel.locationCardWaitingTimes,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        ArrivalsCards(
            waitingTimes = viewModel.locationCardWaitingTimes,
            nearbyStops = nearbyStops,
            pinnedArrivals = pinnedArrivals,
            onPin = { routeId, destination, stopId, stopName ->
                viewModel.addPinnedArrival(routeId, destination, stopId, stopName)
            },
            onUnpin = { routeId, destination, stopId ->
                viewModel.removePinnedArrival(routeId, destination, stopId)
            },
            onRouteSelect = { routeId, destination, stopId, stopName, arrivalTimes, distance ->
                // Extract tripId from first arrival time if available
                val tripId = arrivalTimes.firstOrNull()?.tripId
                
                val params = mutableMapOf<String, Any>(
                    "destination" to destination,
                    "stopId" to stopId,
                    "stopName" to stopName,
                    "distance" to (distance ?: 0),
                    "arrivalTimes" to arrivalTimes
                )
                
                // Only add tripId if it's actually available
                if (tripId != null) {
                    params["tripId"] = tripId
                }
                
                viewModel.handleRouteSelect(routeId, params)
            },
            modifier = Modifier
        )
    }
}

@Composable
private fun AddressSearchBar(
    address: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Prefer showing street + street number when the number appears after a comma
    // e.g. "Via Roma, 5, Torino" -> "Via Roma 5"
    val streetLine = remember(address) {
        val parts = address.split(',')
        val first = parts.getOrNull(0)?.trim().orEmpty()
        val second = parts.getOrNull(1)?.trim().orEmpty()
        when {
            // If the second segment begins with a number (e.g., "5" or "5/A") include it
            second.firstOrNull()?.isDigit() == true ->
                listOf(first, second).filter { it.isNotEmpty() }.joinToString(" ")
            else -> first
        }.ifEmpty { address }
    }
    
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.98f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "GPS Location",
                tint = Color(0xFF0B3D91),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = streetLine,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp
                ),
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = Color(0xFF0B3D91),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun NearbyDepartureCard(
    departure: NearbyDeparturesResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Linea ${departure.routeId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD9731F)
                )
                Text(
                    text = "${departure.headsigns.firstOrNull()?.distanceToStop ?: 0}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            departure.headsigns.take(2).forEach { headsign ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = headsign.tripHeadsign,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Stop: ${headsign.stopName ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    
                    // Show next departures if available
                    headsign.departures.take(3).forEach { dep ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    dep.waitMinutes <= 2 -> Color(0xFFE53E3E)
                                    dep.waitMinutes <= 10 -> Color(0xFFFFA500)
                                    else -> Color(0xFF4CAF50)
                                }.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = if (dep.waitMinutes == 0) "Now" else "${dep.waitMinutes}min",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
