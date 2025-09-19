package com.av.urbanway.presentation.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clipToBounds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.av.urbanway.presentation.viewmodels.MainViewModel
import androidx.compose.animation.core.FastOutSlowInEasing
import com.av.urbanway.presentation.components.chat.ArrivalsChatView

@Composable
fun ChatDetailSheet(
    viewModel: MainViewModel,
    contentType: String,
    requestedHeightFraction: Float? = null,
    onHeightFractionApplied: (() -> Unit)? = null,
    onSearchPlaceSelected: ((com.av.urbanway.data.models.SearchResult) -> Unit)? = null,
    modifier: Modifier = Modifier,
    minHeight: Dp = 124.dp,
    topInset: Dp = 30.dp,
    collapseThreshold: Dp = 96.dp,
    contentRequestId: Int = 0
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val maxHeightDp = this.maxHeight - topInset
        val minHeightDp = minHeight

        // Essential: single height state for drag
        var currentHeight by remember { mutableStateOf(minHeightDp) }
        // Optional: tiny animation only for programmatic expansion
        var animateProgrammatic by remember { mutableStateOf(false) }
        val animatedHeight by androidx.compose.animation.core.animateDpAsState(
            targetValue = currentHeight,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 360,
                easing = FastOutSlowInEasing
            ),
            label = "programmaticExpand"
        )
        // Use animation only when we trigger it programmatically; never on drag
        val visibleHeight = if (animateProgrammatic) animatedHeight else currentHeight

        // Defer heavy content until first reveal at TOP
        var deferContent by remember { mutableStateOf(false) }
        var contentInserted by remember { mutableStateOf(false) }
        var minSpinnerElapsed by remember { mutableStateOf(false) }
        var programmaticTargetHeight by remember { mutableStateOf<Dp?>(null) }
        var searchActive by remember { mutableStateOf(false) }
        var searchText by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

        // Apply external requested height (as fraction between min and max)
        LaunchedEffect(requestedHeightFraction) {
            val f = requestedHeightFraction
            if (f != null) {
                val frac = f.coerceIn(0f, 1f)
                if (frac <= 0.001f) {
                    // Immediate collapse to minimum height without spinner
                    animateProgrammatic = false
                    currentHeight = minHeightDp
                    deferContent = false
                    contentInserted = false
                    minSpinnerElapsed = false
                    programmaticTargetHeight = null
                } else {
                    val h = (minHeightDp + (maxHeightDp - minHeightDp) * frac).coerceIn(minHeightDp, maxHeightDp)
                    // Animate only for this programmatic change
                    animateProgrammatic = true
                    currentHeight = h
                    // Start deferred reveal for scenic effect; reveal at TOP
                    deferContent = true
                    contentInserted = false
                    minSpinnerElapsed = false
                    programmaticTargetHeight = h
                    // Ensure spinner is visible long enough for a scenic feel
                    launch {
                        delay(320)
                        minSpinnerElapsed = true
                    }
                }
                onHeightFractionApplied?.invoke()
            }
        }
        LaunchedEffect(searchActive) {
            if (searchActive) {
                animateProgrammatic = true
                currentHeight = maxHeightDp
                // give compose a frame to layout the textfield
                kotlinx.coroutines.delay(10)
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }

        // On content change or explicit new request: defer and show spinner until fully expanded to target/top
        LaunchedEffect(contentType, contentRequestId) {
            if (contentType.isNotEmpty()) {
                deferContent = true
                contentInserted = false
                minSpinnerElapsed = false
                launch {
                    delay(320)
                    minSpinnerElapsed = true
                }
            } else {
                deferContent = false
                contentInserted = false
                minSpinnerElapsed = false
                programmaticTargetHeight = null
            }
        }

        // Scenic scrim and elevation during programmatic expand (progress toward top)
        val progressToTop: Float = if (animateProgrammatic) {
            val min = minHeightDp.value
            val max = maxHeightDp.value
            val cur = visibleHeight.value
            ((cur - min) / (max - min)).coerceIn(0f, 1f)
        } else 0f
        val scrimAlpha = 0.10f * progressToTop
        val cardElevation = 2.dp + (6.dp * progressToTop)

        Box(Modifier.fillMaxSize()) {
        if (scrimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(visibleHeight)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Drag handle header (captures vertical drags)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(vertical = 8.dp)
                        .pointerInput(maxHeightDp, minHeightDp) {
                            detectDragGestures(
                                onDrag = { change, drag ->
                                    val delta = with(density) { drag.y.toDp() }
                                    val next = (currentHeight - delta).coerceIn(minHeightDp, maxHeightDp)
                                    // During drag, disable animation and update directly
                                    animateProgrammatic = false
                                    currentHeight = next
                                    change.consume()
                                },
                                onDragEnd = {
                                    // If released near bottom, collapse to min height
                                    if (currentHeight <= (minHeightDp + collapseThreshold)) {
                                        animateProgrammatic = false
                                        currentHeight = minHeightDp
                                    }
                                }
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .height(24.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF9AA0A6).copy(alpha = 0.35f))
                        )
                    }
                }
                // Separator removed to avoid resemblance to system bar

                // Header search bar (visual only; no wiring yet)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!searchActive) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F7)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchActive = true
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Cerca una destinazione...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = {
                                searchText = it
                                // Wire suggestions: update query when 2+ chars, else clear
                                if (it.length >= 2) {
                                    viewModel.updateSearchQuery(it)
                                } else {
                                    viewModel.updateSearchQuery("")
                                }
                            },
                            placeholder = { Text("Cerca una destinazione...", color = Color.Gray) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .focusRequester(focusRequester)
                        )
                    }
                }

                // Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Reveal only when reached programmatic target (if any), else at TOP
                    val threshold = 2.dp
                    val targetForReveal = programmaticTargetHeight ?: maxHeightDp
                    val reachedTarget = visibleHeight >= (targetForReveal - threshold)
                    val shouldShowContent = if (deferContent && !contentInserted) {
                        minSpinnerElapsed && reachedTarget
                    } else true

                    if (searchActive) {
                        DestinationSuggestionsCard(
                            destinationsData = null,
                            viewModel = viewModel,
                            onPlaceSelected = { result ->
                                viewModel.updateSearchQuery("")
                                keyboardController?.hide()
                                searchActive = false
                                onSearchPlaceSelected?.invoke(result)
                            }
                        )
                    } else if (!shouldShowContent) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            com.av.urbanway.presentation.components.LoadingWheelIndicator(
                                indicatorSize = 42.dp,
                                dotCount = 12,
                                dotSize = 5.dp,
                                color = Color(0xFF0B3D91)
                            )
                        }
                    } else {
                        if (!contentInserted) {
                            // Reveal content: stop treating sheet as animating for map overlays
                            contentInserted = true
                            deferContent = false
                            animateProgrammatic = false
                        }
                        when (contentType) {
                            "arrivals" -> {
                                val nearbyStops by viewModel.nearbyStops.collectAsState()
                                val pinnedArrivals by viewModel.pinnedArrivals.collectAsState()
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
                                onAltreLineeClick = {},
                                onOrariClick = {},
                                onMappaClick = {},
                                isPreview = false,
                                userCoordinates = viewModel.currentLocation.collectAsState().value?.coordinates
                            )
                                    }
                                    "journey_results" -> {
                                        val journeys by viewModel.journeys.collectAsState()
                                        val isLoading by viewModel.isLoadingJourneys.collectAsState()
                                        val startLoc by viewModel.startLocation.collectAsState()
                                        val endLoc by viewModel.endLocation.collectAsState()
                                        if (startLoc != null && endLoc != null) {
                                            InlineJourneyResultsCard(
                                                viewModel = viewModel,
                                                journeys = journeys,
                                                isLoading = isLoading,
                                                fromAddress = startLoc!!.address,
                                                toAddress = endLoc!!.address,
                                                onClose = { /* no-op in sheet */ },
                                                isPreview = false
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Dettagli percorso non disponibili",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                            "map" -> {
                                com.av.urbanway.presentation.components.chat.MapChatView(
                                    viewModel = viewModel,
                                    isPreview = false,
                                    isSheetAnimating = animateProgrammatic,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                // Empty or unknown content
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (contentType.isEmpty()) "Trascina per espandere" else contentType,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray.copy(alpha = 0.7f)
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
}
