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
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
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

    // Unified BottomSheet state
    var showBottomSheet by remember { mutableStateOf(false) }
    var bottomSheetContent by remember { mutableStateOf("") } // Track what content to show
    var lastUserChoice by remember { mutableStateOf("") } // Track which user choice opened the sheet

    // Golden rule: Reset function to clear all subsequent states
    fun resetToLevel(level: Int) {
        when (level) {
            0 -> { // Reset to greeting level
                userSelectedArrivi = false
                userSelectedMappa = false
                userSelectedCerca = false
                showArriviView = false
                userSelectedAltreLinee = false
                userSelectedOrariFromArrivals = false
                userSelectedMappaFromArrivals = false
                showDynamicArrivalsCard = false
                showBottomSheet = false
                bottomSheetContent = ""
                lastUserChoice = ""
            }
            1 -> { // Reset from first level choices (after greeting)
                showArriviView = false
                userSelectedAltreLinee = false
                userSelectedOrariFromArrivals = false
                userSelectedMappaFromArrivals = false
                showDynamicArrivalsCard = false
                showBottomSheet = false
                bottomSheetContent = ""
                lastUserChoice = ""
            }
            2 -> { // Reset from second level choices (after arrivals)
                userSelectedOrariFromArrivals = false
                userSelectedMappaFromArrivals = false
                showDynamicArrivalsCard = false
                showBottomSheet = false
                bottomSheetContent = ""
                lastUserChoice = ""
            }
        }
    }

    // Generic function to handle BottomSheet dismissal
    fun dismissBottomSheet() {
        showBottomSheet = false
        bottomSheetContent = ""

        // Remove the last user choice that triggered the sheet
        when (lastUserChoice) {
            "altreLinee" -> userSelectedAltreLinee = false
            "orari" -> userSelectedOrariFromArrivals = false
            "mappa" -> userSelectedMappaFromArrivals = false
            // Add more cases as needed for future sheet content
        }

        lastUserChoice = ""
    }

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
                        resetToLevel(0)
                        userSelectedArrivi = true
                        showArriviView = true
                        // Trigger API call when user selects "Arrivi"
                        scope.launch {
                            viewModel.loadNearbyData()
                        }
                    },
                    onMappaClick = {
                        resetToLevel(0)
                        userSelectedMappa = true
                        viewModel.showToast("Mappa - Coming soon!")
                    },
                    onCercaClick = {
                        resetToLevel(0)
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
                    onClick = {
                        resetToLevel(1)
                        userSelectedArrivi = true
                        showArriviView = true
                        scope.launch {
                            viewModel.loadNearbyData()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (userSelectedMappa) {
            item {
                UserMessageView(
                    message = "🗺️ Mappa",
                    onClick = {
                        resetToLevel(1)
                        userSelectedMappa = true
                        viewModel.showToast("Mappa - Coming soon!")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (userSelectedCerca) {
            item {
                UserMessageView(
                    message = "🔍 Cerca",
                    onClick = {
                        resetToLevel(1)
                        userSelectedCerca = true
                        viewModel.showToast("Cerca - Coming soon!")
                    },
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
                        showBottomSheet = true
                        bottomSheetContent = "arrivals_detail"
                        lastUserChoice = "altreLinee"
                    },
                    onOrariClick = {
                        userSelectedOrariFromArrivals = true
                        showDynamicArrivalsCard = true
                        viewModel.showToast("Orari delle tue linee!")
                    },
                    onMappaClick = {
                        userSelectedMappaFromArrivals = true
                        showBottomSheet = true
                        bottomSheetContent = "map_view"
                        lastUserChoice = "mappa"
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
                    onClick = {
                        resetToLevel(2)
                        userSelectedAltreLinee = true
                        showBottomSheet = true
                        bottomSheetContent = "arrivals_detail"
                        lastUserChoice = "altreLinee"
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (userSelectedOrariFromArrivals) {
            item {
                UserMessageView(
                    message = "📅 Orari",
                    onClick = {
                        resetToLevel(2)
                        userSelectedOrariFromArrivals = true
                        showDynamicArrivalsCard = true
                        viewModel.showToast("Orari delle tue linee!")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (userSelectedMappaFromArrivals) {
            item {
                UserMessageView(
                    message = "🗺️ Mappa",
                    onClick = {
                        resetToLevel(2)
                        userSelectedMappaFromArrivals = true
                        showBottomSheet = true
                        bottomSheetContent = "map_view"
                        lastUserChoice = "mappa"
                    },
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
                        // Remove the user choice that triggered this card
                        userSelectedOrariFromArrivals = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Legacy ArrivalsCards removed for chatbot experiment
        // (API data flow and business logic preserved)
    }

    // Unified Modal Dialog for all content
    if (showBottomSheet) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { dismissBottomSheet() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .clickable(enabled = false) { }, // Prevent clicks from propagating to background
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header with close button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (bottomSheetContent) {
                                "arrivals_detail" -> "Altre linee"
                                "map_view" -> "Mappa"
                                else -> "Dettagli"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { dismissBottomSheet() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Chiudi",
                                tint = Color.Black
                            )
                        }
                    }

                    // Content area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (bottomSheetContent) {
                            "arrivals_detail" -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
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
                                                dismissBottomSheet()
                                                viewModel.showToast("Orari delle tue linee!")
                                            },
                                            onMappaClick = {
                                                viewModel.showToast("Mappa degli arrivi - Coming soon!")
                                            },
                                            isPreview = false, // Show detail mode in modal
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            "map_view" -> {
                                UrbanWayMapView(
                                    currentLocation = currentLocation?.coordinates,
                                    mapConfig = com.av.urbanway.data.local.GoogleMapsConfig.getInstance(context),
                                    stops = nearbyStops,
                                    uiState = UIState.NORMAL,
                                    modifier = Modifier.fillMaxSize() // Full size for better map interaction
                                )
                            }
                            // Add more content types here as needed
                            else -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Content not found",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
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

