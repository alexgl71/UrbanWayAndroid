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
import com.av.urbanway.presentation.components.chat.GreetingChatView
import com.av.urbanway.presentation.components.chat.ArrivalsChatView
import com.av.urbanway.presentation.components.chat.PinnedCardView
import com.av.urbanway.presentation.components.chat.BotMessageContainer
import com.av.urbanway.presentation.components.chat.UserMessageView
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    // Inline journey results state (must be collected in @Composable scope, not LazyListScope)
    val showInlineResults by viewModel.showInlineJourneyResults.collectAsState()
    val journeys by viewModel.journeys.collectAsState()
    val isLoadingJourneys by viewModel.isLoadingJourneys.collectAsState()
    val startLoc by viewModel.startLocation.collectAsState()
    val endLoc by viewModel.endLocation.collectAsState()

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
    var userSelectedPlace by remember { mutableStateOf(false) }
    var selectedPlaceName by remember { mutableStateOf("") }
    var showJourneyView by remember { mutableStateOf(false) }
    var isJourneyExpanded by remember { mutableStateOf(false) }
    var isJourneyResultsExpanded by remember { mutableStateOf(false) }
    var userSelectedPianifica by remember { mutableStateOf(false) }

    // ArrivalsChatView interaction tracking
    var userSelectedAltreLinee by remember { mutableStateOf(false) }
    var userSelectedOrariFromArrivals by remember { mutableStateOf(false) }
    var userSelectedMappaFromArrivals by remember { mutableStateOf(false) }

    // Unified BottomSheet state
    var showBottomSheet by remember { mutableStateOf(false) }
    var bottomSheetContent by remember { mutableStateOf("") } // Track what content to show
    var lastUserChoice by remember { mutableStateOf("") } // Track which user choice opened the sheet

    // Track the last expandable message for fullscreen button
    var lastExpandableMessage by remember { mutableStateOf("") } // "arrivals", "map", etc.

    // Chat detail sheet state (always visible). Height requested as fraction between min..max
    var sheetHeightFraction by remember { mutableStateOf<Float?>(null) }
    var sheetContentType by remember { mutableStateOf("") }
    var sheetContentRequestId by remember { mutableStateOf(0) }

    // Golden rule: Reset function to clear all subsequent states
    fun resetToLevel(level: Int) {
        when (level) {
            0 -> { // Reset to greeting level
                userSelectedArrivi = false
                userSelectedMappa = false
                userSelectedCerca = false
                userSelectedPlace = false
                selectedPlaceName = ""
                showJourneyView = false
                isJourneyExpanded = false
                isJourneyResultsExpanded = false
                userSelectedPianifica = false
                showArriviView = false
                userSelectedAltreLinee = false
                userSelectedOrariFromArrivals = false
                userSelectedMappaFromArrivals = false
                showDynamicArrivalsCard = false
                showBottomSheet = false
                bottomSheetContent = ""
                lastUserChoice = ""
                lastExpandableMessage = ""
                // Keep the sheet compact and visible
        sheetHeightFraction = null
                viewModel.closeInlineJourneyResults()
            }
            1 -> { // Reset from first level choices (after greeting)
                showArriviView = false
                isJourneyExpanded = false
                isJourneyResultsExpanded = false
                userSelectedPianifica = false
                userSelectedAltreLinee = false
                userSelectedOrariFromArrivals = false
                userSelectedMappaFromArrivals = false
                showDynamicArrivalsCard = false
                showBottomSheet = false
                bottomSheetContent = ""
                lastUserChoice = ""
                lastExpandableMessage = ""
                // Keep the sheet compact and visible
        sheetHeightFraction = null
                viewModel.closeInlineJourneyResults()
            }
            2 -> { // Reset from second level choices (after arrivals)
                userSelectedOrariFromArrivals = false
                userSelectedMappaFromArrivals = false
                showDynamicArrivalsCard = false
                isJourneyExpanded = false
                isJourneyResultsExpanded = false
                userSelectedPianifica = false
                showBottomSheet = false
                bottomSheetContent = ""
                lastUserChoice = ""
                lastExpandableMessage = ""
                // Keep the sheet compact and visible
        sheetHeightFraction = null
                viewModel.closeInlineJourneyResults()
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

    fun collapseAndClearDraggableSheet() {
        // Immediately collapse to min height and clear content
        sheetHeightFraction = 0f
        sheetContentType = ""
    }

    // Function to show content in draggable sheet
    fun showInDraggableSheet(contentType: String, heightFraction: Float = 0.5f) {
        android.util.Log.d("URBANWAY_DEBUG", "showInDraggableSheet called with contentType: $contentType, heightFraction: $heightFraction")
        sheetContentType = contentType
        sheetContentRequestId += 1
        // Always visible; just set the content and optional target height fraction
        sheetHeightFraction = heightFraction

        when (contentType) {
            "arrivals" -> {
                lastUserChoice = "altreLinee"
            }
            "map" -> {
                lastUserChoice = "mappa"
            }
            "pinned" -> {
                lastUserChoice = "orari"
            }
        }
    }

    // Function to expand content to fullscreen (legacy for journey)
    fun expandToFullscreen(contentType: String) {
        when (contentType) {
            "arrivals" -> showInDraggableSheet("arrivals", 0.9f)
            "map" -> showInDraggableSheet("map", 0.9f)
            "journey" -> {
                // Expand inline instead of opening sheet
                isJourneyExpanded = true
            }
        }
    }

    // Revealable actions state (shared across chat list)
    var revealedActionsFor by remember { mutableStateOf<String?>(null) }
    var hideActionsJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    fun toggleReveal(id: String) {
        if (revealedActionsFor == id) {
            revealedActionsFor = null
            hideActionsJob?.cancel()
            hideActionsJob = null
        } else {
            revealedActionsFor = id
            hideActionsJob?.cancel()
            hideActionsJob = scope.launch {
                kotlinx.coroutines.delay(5000)
                revealedActionsFor = null
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
        // Always show greeting first
        if (showGreeting) {
            item {
                com.av.urbanway.presentation.components.chat.BotMessageCard(
                    messageId = "greeting_bot",
                    isLastMessage = true,
                    onExpandClick = null,
                    actions = listOf(
                        com.av.urbanway.presentation.components.chat.BotAction(
                            id = "arrivi",
                            label = "🚌 Arrivi",
                            onInvoke = {
                                resetToLevel(0)
                                userSelectedArrivi = true
                                showArriviView = true
                                scope.launch { viewModel.loadNearbyData() }
                            }
                        ),
                        com.av.urbanway.presentation.components.chat.BotAction(
                            id = "mappa",
                            label = "🗺️ Mappa",
                            onInvoke = {
                                // Mirror Arrivals -> Mappa behavior from Greeting: clear from root and show map pair
                                collapseAndClearDraggableSheet()
                                resetToLevel(0)
                                userSelectedMappaFromArrivals = true
                            }
                        )
                    ),
                    lastUpdatedEpochMillis = System.currentTimeMillis(),
                    isLoading = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GreetingChatView(
                        onArriviClick = {
                            resetToLevel(0)
                            userSelectedArrivi = true
                            showArriviView = true
                            scope.launch { viewModel.loadNearbyData() }
                        },
                        onMappaClick = {
                            resetToLevel(0)
                            userSelectedMappa = true
                            viewModel.showToast("Mappa - Coming soon!")
                        },
                        onSearchClick = {
                            viewModel.openSearch()
                        },
                        onPlaceSelected = { result ->
                            resetToLevel(0)
                            userSelectedPlace = true
                            selectedPlaceName = result.title
                            showJourneyView = false
                            viewModel.prepareJourneyForChatWithResult(result)
                            scope.launch {
                                repeat(30) {
                                    val s = viewModel.startLocation.value
                                    val e = viewModel.endLocation.value
                                    if (s != null && e != null) {
                                        viewModel.startJourneySearchInline(
                                            s.address, s.coordinates, e.address, e.coordinates
                                        )
                                        return@launch
                                    }
                                    kotlinx.coroutines.delay(100)
                                }
                            }
                        },
                        destinationsData = null,
                        viewModel = viewModel,
                        showActions = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
                        // Keep subtle feedback if needed
                        viewModel.showToast("Mappa")
                    },
                    
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Show a preview bubble for map with expand
            item {
                lastExpandableMessage = "map"
                BotMessageContainer(
                    isLastMessage = true,
                    onExpandClick = { showInDraggableSheet("map", 0.9f) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    com.av.urbanway.presentation.components.chat.MapChatView(
                        viewModel = viewModel,
                        isPreview = true
                    )
                }
            }
        }

        if (userSelectedCerca) {
            item {
                UserMessageView(
                    message = "🔍 Cerca",
                    onClick = {
                        // Open the full search experience (SearchScreen)
                        viewModel.openSearch()
                    },
                    
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (userSelectedPlace) {
            item {
                UserMessageView(
                    message = "📍 $selectedPlaceName",
                    onClick = {
                        resetToLevel(1)
                        userSelectedPlace = true
                        viewModel.showToast("Place: $selectedPlaceName")
                    },
                    
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Show bot responses based on user selections
        if (showArriviView) {
            // Set this as the last expandable message
            lastExpandableMessage = "arrivals"

            item {
                com.av.urbanway.presentation.components.chat.BotMessageCard(
                    messageId = "arrivals_bot",
                    isLastMessage = lastExpandableMessage == "arrivals",
                    onExpandClick = { expandToFullscreen("arrivals") },
                    actions = listOf(
                        com.av.urbanway.presentation.components.chat.BotAction(
                            id = "altre_linee",
                            label = "🚌 Altre linee",
                            onInvoke = {
                                collapseAndClearDraggableSheet()
                                resetToLevel(2)
                                userSelectedAltreLinee = true
                                showInDraggableSheet("arrivals", 0.9f)
                            }
                        ),
                        com.av.urbanway.presentation.components.chat.BotAction(
                            id = "mappa",
                            label = "🗺️ Mappa",
                            onInvoke = {
                                collapseAndClearDraggableSheet()
                                resetToLevel(2)
                                userSelectedMappaFromArrivals = true
                            }
                        )
                    ),
                    lastUpdatedEpochMillis = System.currentTimeMillis(),
                    isLoading = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                        onAltreLineeClick = { /* handled via reveal actions */ },
                        onOrariClick = { /* removed: use expand button */ },
                        onMappaClick = { /* handled via reveal actions */ },
                        isPreview = true, // Show preview mode in chat
                        userCoordinates = currentLocation?.coordinates
                    )
                }
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
                    },
                    
                    modifier = Modifier.fillMaxWidth()
                )
            }
            val coords = currentLocation?.coordinates ?: com.av.urbanway.data.models.Coordinates(
                com.av.urbanway.data.local.GoogleMapsConfig.TURIN_LAT,
                com.av.urbanway.data.local.GoogleMapsConfig.TURIN_LNG
            )
            run {
                lastExpandableMessage = "map_inline"
                item {
                    com.av.urbanway.presentation.components.chat.BotMessageCard(
                        messageId = "map_inline_bot",
                        isLastMessage = lastExpandableMessage == "map_inline",
                        onExpandClick = { showInDraggableSheet("map", 0.92f) },
                        actions = listOf(
                            com.av.urbanway.presentation.components.chat.BotAction(
                                id = "open_map",
                                label = "Apri mappa",
                                onInvoke = {
                                    // Keep prior context; just open map in sheet
                                    collapseAndClearDraggableSheet()
                                    showInDraggableSheet("map", 0.92f)
                                }
                            )
                        ),
                        lastUpdatedEpochMillis = System.currentTimeMillis(),
                        isLoading = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        com.av.urbanway.presentation.components.StaticMapThumbnail(
                            coords = coords,
                            width = 640,
                            heightPx = 360,
                            zoom = 19,
                            imageHeight = 260.dp
                        )
                    }
                }
            }
        }

        // PinnedCardView as chat item - shown when user clicks "Orari"
        if (showDynamicArrivalsCard && pinnedArrivals.isNotEmpty()) {
            // Set this as the last expandable message
            lastExpandableMessage = "pinned"

            item {
                com.av.urbanway.presentation.components.chat.BotMessageCard(
                    messageId = "pinned_bot",
                    isLastMessage = lastExpandableMessage == "pinned",
                    onExpandClick = { expandToFullscreen("map") },
                    actions = listOf(
                        com.av.urbanway.presentation.components.chat.BotAction(
                            id = "open_map",
                            label = "Apri mappa",
                            onInvoke = { expandToFullscreen("map") }
                        )
                    ),
                    lastUpdatedEpochMillis = System.currentTimeMillis(),
                    isLoading = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                        }
                    )
                }
            }
        }

        // Show journey planning when user selects a place
        if (showJourneyView) {
            val location = currentLocation
            if (location != null) {
                // Set this as the last expandable message
                lastExpandableMessage = "journey"

                item {
                    com.av.urbanway.presentation.components.chat.BotMessageCard(
                        messageId = "journey_planner_bot",
                        isLastMessage = lastExpandableMessage == "journey",
                        onExpandClick = { isJourneyExpanded = true },
                        actions = emptyList(),
                        lastUpdatedEpochMillis = System.currentTimeMillis(),
                        isLoading = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Inline Journey Planner (chat) - expands inline, not in sheet
                        com.av.urbanway.presentation.screens.JourneyPlannerScreen(
                            viewModel = viewModel,
                            currentLocation = location,
                            onSearchJourney = { fromAddress, fromCoords, toAddress, toCoords ->
                                // Treat as chip: show user bubble, then trigger inline results
                                userSelectedPianifica = true
                                viewModel.startJourneySearchInline(fromAddress, fromCoords, toAddress, toCoords)
                            },
                            onBack = { /* handled by state */ },
                            isPreview = !isJourneyExpanded
                        )
                    }
                }
            }
        }

        // Inline Journey Results as bot message (avoid sheet)
        // Show user action bubble for Pianifica (chip-style)
        if (userSelectedPianifica) {
            item {
                UserMessageView(
                    message = "🧭 Pianifica",
                    onClick = {
                        // Golden rule: remove subsequent content and re-trigger
                        viewModel.closeInlineJourneyResults()
                        userSelectedPianifica = true
                        val s = viewModel.startLocation.value
                        val e = viewModel.endLocation.value
                        if (s != null && e != null) {
                            viewModel.startJourneySearchInline(
                                s.address, s.coordinates, e.address, e.coordinates
                            )
                        }
                    },
                    
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (showInlineResults && startLoc != null && endLoc != null) {
            // Force show expand button for this bubble per UX request
            lastExpandableMessage = "journey_results"

            item {
                com.av.urbanway.presentation.components.chat.BotMessageCard(
                    messageId = "journey_results_bot",
                    isLastMessage = true,
                    onExpandClick = {
                        showInDraggableSheet("journey_results", 0.9f)
                        lastUserChoice = "percorsi"
                    },
                    actions = listOf(
                        com.av.urbanway.presentation.components.chat.BotAction(
                            id = "open_results",
                            label = "Mostra dettagli",
                            onInvoke = {
                                collapseAndClearDraggableSheet()
                                viewModel.closeInlineJourneyResults()
                                showInDraggableSheet("journey_results", 0.9f)
                                lastUserChoice = "percorsi"
                            }
                        )
                    ),
                    lastUpdatedEpochMillis = System.currentTimeMillis(),
                    isLoading = isLoadingJourneys,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InlineJourneyResultsCard(
                        viewModel = viewModel,
                        journeys = journeys,
                        isLoading = isLoadingJourneys,
                        fromAddress = startLoc!!.address,
                        toAddress = endLoc!!.address,
                        onClose = { viewModel.closeInlineJourneyResults() },
                        isPreview = true
                    )
                }
            }
        }

            // Legacy ArrivalsCards removed for chatbot experiment
            // (API data flow and business logic preserved)
        }

        // Always-visible chat detail sheet at bottom
        ChatDetailSheet(
            viewModel = viewModel,
            contentType = sheetContentType,
            requestedHeightFraction = sheetHeightFraction,
            onHeightFractionApplied = { sheetHeightFraction = null },
            onSearchPlaceSelected = { result ->
                // Clear branch first, then collapse sheet so request is honored
                resetToLevel(0)
                collapseAndClearDraggableSheet()
                // Show user bubble for selected place
                userSelectedPlace = true
                selectedPlaceName = result.title
                showJourneyView = false
                viewModel.prepareJourneyForChatWithResult(result)
                scope.launch {
                    repeat(30) {
                        val s = viewModel.startLocation.value
                        val e = viewModel.endLocation.value
                        if (s != null && e != null) {
                            viewModel.startJourneySearchInline(
                                s.address, s.coordinates, e.address, e.coordinates
                            )
                            return@launch
                        }
                        kotlinx.coroutines.delay(100)
                    }
                }
            },
            contentRequestId = sheetContentRequestId,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )

        // OLD: Unified Modal Dialog for all content (keeping for journey results)
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
                                "journey_results" -> "Percorsi disponibili"
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
                                            onOrariClick = { },
                                            onMappaClick = { },
                                            isPreview = false, // Show detail mode in modal
                                            userCoordinates = currentLocation?.coordinates
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
                            "journey_results" -> {
                                // Fullscreen Journey Results
                                val data = com.av.urbanway.presentation.components.JourneyResultsData(
                                    fromAddress = startLoc?.address ?: "",
                                    toAddress = endLoc?.address ?: "",
                                    fromCoordinates = startLoc?.coordinates ?: com.av.urbanway.data.models.Coordinates(0.0, 0.0),
                                    toCoordinates = endLoc?.coordinates ?: com.av.urbanway.data.models.Coordinates(0.0, 0.0),
                                    journeys = journeys
                                )
                                com.av.urbanway.presentation.components.JourneyResultsView(
                                    journeyData = data,
                                    isLoading = isLoadingJourneys,
                                    onJourneySelect = { journey ->
                                        viewModel.showFixedJourneyOverlay(journey)
                                    },
                                    onBack = { dismissBottomSheet() },
                                    modifier = Modifier.fillMaxWidth()
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
fun AddressSearchBar(
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
}

// No custom chip here; user selections are represented with UserMessageView for consistency
