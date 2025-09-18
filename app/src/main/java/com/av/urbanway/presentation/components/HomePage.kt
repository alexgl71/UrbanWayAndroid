package com.av.urbanway.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
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
import com.av.urbanway.data.models.StopInfo
import com.av.urbanway.data.models.WaitingTime
import com.av.urbanway.data.models.PinnedArrival
import com.av.urbanway.presentation.viewmodels.MainViewModel
import com.av.urbanway.presentation.components.widgets.SingleArrivalsCard
import com.av.urbanway.presentation.components.widgets.ArrivalRowContent
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.heightIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    viewModel: MainViewModel,
    onNavigateToRealtime: () -> Unit,
    onNavigateToRouteDetail: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pinnedArrivals by viewModel.pinnedArrivals.collectAsState()
    // Collect to trigger recomposition when API data updates
    val nearbyDepartures by viewModel.nearbyDepartures.collectAsState()
    val nearbyStops by viewModel.nearbyStops.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()

    // State for dynamic arrivals card
    var showDynamicArrivalsCard by remember { mutableStateOf(false) }
    // Track routes selected ONLY through ChatView (separate from global pinned)
    var chatViewSelectedRoutes by remember { mutableStateOf<List<PinnedArrival>>(emptyList()) }

    // Chat state - controls which chat view is shown
    var currentChatView by remember { mutableStateOf("greeting") } // "greeting", "arrivi", "mappa", "cerca"

    // Chat conversation flow tracking
    var showGreeting by remember { mutableStateOf(true) }
    var showArriviView by remember { mutableStateOf(false) }
    var userSelectedArrivi by remember { mutableStateOf(false) }
    var userSelectedMappa by remember { mutableStateOf(false) }
    var userSelectedCerca by remember { mutableStateOf(false) }

    // ArrivalsChatView interaction tracking
    var userSelectedAltreLinee by remember { mutableStateOf(false) }
    var userSelectedOrariFromArrivals by remember { mutableStateOf(false) }
    var userSelectedMappaFromArrivals by remember { mutableStateOf(false) }

    // BottomSheet state for detailed views
    var showArrivalsDetailSheet by remember { mutableStateOf(false) }
    var isRouteCirclesExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Always show greeting first
        if (showGreeting) {
            item {
                GreetingChatView(
                    onArriviClick = {
                        userSelectedArrivi = true
                        showArriviView = true
                        // Trigger API call when user selects "Arrivi"
                        scope.launch {
                            viewModel.loadNearbyData()
                        }
                    },
                    onMappaClick = {
                        userSelectedMappa = true
                        viewModel.showToast("Mappa - Coming soon!")
                    },
                    onCercaClick = {
                        userSelectedCerca = true
                        viewModel.showToast("Cerca - Coming soon!")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Show user selection messages
        if (userSelectedArrivi) {
            item {
                UserMessageView(
                    message = "🚌 Arrivi",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (userSelectedMappa) {
            item {
                UserMessageView(
                    message = "🗺️ Mappa",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (userSelectedCerca) {
            item {
                UserMessageView(
                    message = "🔍 Cerca",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Show bot responses based on user selections
        if (showArriviView) {
            item {
                ArrivalsChatView(
                    waitingTimes = viewModel.locationCardWaitingTimes,
                    nearbyStops = nearbyStops,
                    pinnedArrivals = pinnedArrivals,
                    onPin = { routeId, destination, stopId, stopName ->
                        viewModel.addPinnedArrival(routeId, destination, stopId, stopName)
                    },
                    onUnpin = { routeId, destination, stopId ->
                        viewModel.removePinnedArrival(routeId, destination, stopId)
                    },
                    onAltreLineeClick = {
                        userSelectedAltreLinee = true
                        showArrivalsDetailSheet = true
                    },
                    onOrariClick = {
                        userSelectedOrariFromArrivals = true
                        showDynamicArrivalsCard = true
                        viewModel.showToast("Orari delle tue linee!")
                    },
                    onMappaClick = {
                        userSelectedMappaFromArrivals = true
                        viewModel.showToast("Mappa degli arrivi - Coming soon!")
                    },
                    isPreview = true, // Show preview mode in chat
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Show user selections from ArrivalsChatView
        if (userSelectedAltreLinee) {
            item {
                UserMessageView(
                    message = "🚌 Altre linee",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (userSelectedOrariFromArrivals) {
            item {
                UserMessageView(
                    message = "📅 Orari",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (userSelectedMappaFromArrivals) {
            item {
                UserMessageView(
                    message = "🗺️ Mappa",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // PinnedCardView as chat item - shown when user clicks "Orari"
        if (showDynamicArrivalsCard && pinnedArrivals.isNotEmpty()) {
            item {
                PinnedCardView(
                    pinnedArrivals = pinnedArrivals,
                    waitingTimes = viewModel.locationCardWaitingTimes,
                    nearbyStops = nearbyStops,
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
                    onDismiss = {
                        showDynamicArrivalsCard = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Legacy ArrivalsCards removed for chatbot experiment
        // (API data flow and business logic preserved)
    }

    // BottomSheet for detailed views
    if (showArrivalsDetailSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showArrivalsDetailSheet = false
                isRouteCirclesExpanded = false
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 700.dp), // Set reasonable min/max height
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ArrivalsChatView(
                        waitingTimes = viewModel.locationCardWaitingTimes,
                        nearbyStops = nearbyStops,
                        pinnedArrivals = pinnedArrivals,
                        onPin = { routeId, destination, stopId, stopName ->
                            viewModel.addPinnedArrival(routeId, destination, stopId, stopName)
                        },
                        onUnpin = { routeId, destination, stopId ->
                            viewModel.removePinnedArrival(routeId, destination, stopId)
                        },
                        onAltreLineeClick = {
                            // In detail mode, this shows route circles
                            viewModel.showToast("Route circles shown!")
                        },
                        onOrariClick = {
                            showDynamicArrivalsCard = true
                            showArrivalsDetailSheet = false
                            viewModel.showToast("Orari delle tue linee!")
                        },
                        onMappaClick = {
                            viewModel.showToast("Mappa degli arrivi - Coming soon!")
                        },
                        isPreview = false, // Show detail mode in sheet
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Add some bottom spacing to prevent cut-off
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
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

