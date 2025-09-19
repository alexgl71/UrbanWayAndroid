package com.av.urbanway.presentation.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
    modifier: Modifier = Modifier,
    minHeight: Dp = 80.dp,
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
                        .height(56.dp)
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
                            .padding(top = 8.dp)
                            .height(36.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Gray.copy(alpha = 0.5f))
                        )
                    }
                }
                // Subtle separator under handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black.copy(alpha = 0.06f))
                )

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

                    if (!shouldShowContent) {
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
