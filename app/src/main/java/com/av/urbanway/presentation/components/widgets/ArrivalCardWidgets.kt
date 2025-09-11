package com.av.urbanway.presentation.components.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.av.urbanway.data.models.StopInfo
import com.av.urbanway.data.models.WaitingTime
import com.av.urbanway.data.models.TransportType

// 1. Complete Arrival Row Widget (with swipe functionality)
@Composable
fun ArrivalRow(
    rep: WaitingTime,
    timesAtStop: List<WaitingTime>,
    nearbyStops: List<StopInfo>,
    isPinned: Boolean,
    onPin: (routeId: String, destination: String, stopId: String, stopName: String) -> Unit,
    onUnpin: (routeId: String, destination: String, stopId: String) -> Unit,
    onRouteSelect: ((routeId: String, destination: String, stopId: String, stopName: String, arrivalTimes: List<WaitingTime>, distance: Int?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                val stopName = nearbyStops.firstOrNull { it.stopId == rep.stopId }?.stopName ?: ""
                if (isPinned) {
                    onUnpin(rep.route, rep.destination, rep.stopId)
                } else {
                    onPin(rep.route, rep.destination, rep.stopId, stopName)
                }
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = swipeState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isActive = swipeState.currentValue == SwipeToDismissBoxValue.EndToStart ||
                    swipeState.targetValue == SwipeToDismissBoxValue.EndToStart
            SwipeActionBackground(
                isActive = isActive,
                isPinned = isPinned
            )
        },
        modifier = modifier
    ) {
        ArrivalRowContent(
            rep = rep,
            timesAtStop = timesAtStop,
            nearbyStops = nearbyStops,
            onRouteSelect = onRouteSelect
        )
    }
}

// 2. Arrival Row Content Widget (without swipe)
@Composable
fun ArrivalRowContent(
    rep: WaitingTime,
    timesAtStop: List<WaitingTime>,
    nearbyStops: List<StopInfo>,
    onRouteSelect: ((routeId: String, destination: String, stopId: String, stopName: String, arrivalTimes: List<WaitingTime>, distance: Int?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val stopInfo = nearbyStops.firstOrNull { it.stopId == rep.stopId }
    val stopName = stopInfo?.stopName ?: ""
    val distance = stopInfo?.distanceToStop

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .then(
                if (onRouteSelect != null) {
                    Modifier.clickable {
                        onRouteSelect(
                            rep.route,
                            rep.destination,
                            rep.stopId,
                            stopName,
                            timesAtStop,
                            distance
                        )
                    }
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.Top
    ) {
        // Route badge
        RouteBadge(route = rep.route)
        
        Spacer(Modifier.width(16.dp))
        
        // Route info column
        RouteInfoColumn(
            destination = rep.destination,
            stopName = stopName,
            distance = distance,
            waitingTimes = timesAtStop,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview
@Composable
private fun ArrivalRowContentPreview() {
    val sampleRep = WaitingTime("9", 0, "SAN SALVARIO, CORSO D'AZEGLIO", TransportType.TRAM, true, "1", "trip_9_1")
    val sampleTimes = listOf(
        WaitingTime("9", 0, "SAN SALVARIO, CORSO D'AZEGLIO", TransportType.TRAM, true, "1", "trip_9_1"),
        WaitingTime("9", 11, "SAN SALVARIO, CORSO D'AZEGLIO", TransportType.TRAM, true, "1", "trip_9_2"),
        WaitingTime("9", 30, "SAN SALVARIO, CORSO D'AZEGLIO", TransportType.TRAM, false, "1", "trip_9_3")
    )
    val sampleStops = listOf(
        StopInfo("1", "Fermata 646 - ADRIANO", 0.0, 0.0, 111, listOf("9"))
    )

    ArrivalRowContent(
        rep = sampleRep,
        timesAtStop = sampleTimes,
        nearbyStops = sampleStops
    )
}

// 3. Arrivals Card Container Widget
@Composable
fun ArrivalsCardContainer(
    title: String,
    leadingIcon: ImageVector,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
    ) {
        CardHeader(
            title = title,
            leadingIcon = leadingIcon
        )
        
        Column(Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Preview
@Composable
private fun ArrivalsCardContainerPreview() {
    val sampleRep = WaitingTime("9", 0, "SAN SALVARIO, CORSO D'AZEGLIO", TransportType.TRAM, true, "1", "trip_9_1")
    val sampleTimes = listOf(
        WaitingTime("9", 0, "SAN SALVARIO, CORSO D'AZEGLIO", TransportType.TRAM, true, "1", "trip_9_1"),
        WaitingTime("9", 11, "SAN SALVARIO, CORSO D'AZEGLIO", TransportType.TRAM, true, "1", "trip_9_2")
    )
    val sampleStops = listOf(
        StopInfo("1", "Fermata 646 - ADRIANO", 0.0, 0.0, 111, listOf("9"))
    )

    ArrivalsCardContainer(
        title = "ARRIVI IN EVIDENZA",
        leadingIcon = Icons.Filled.PushPin,
        content = {
            ArrivalRowContent(
                rep = sampleRep,
                timesAtStop = sampleTimes,
                nearbyStops = sampleStops
            )
        }
    )
}

// 4. Empty Arrivals Card Widget
@Composable
fun ArrivalsEmptyCard(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        CardHeader(
            title = "LINEE IN ARRIVO",
            leadingIcon = Icons.Filled.AccessTime
        )
        
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nessuna fermata nelle vicinanze",
                color = Color.Gray
            )
        }
    }
}

@Preview
@Composable
private fun ArrivalsEmptyCardPreview() {
    ArrivalsEmptyCard()
}

// 5. Single Arrival Card Widget (combines container with rows)
@Composable
fun SingleArrivalsCard(
    title: String,
    leadingIcon: ImageVector,
    arrivalItems: List<Triple<String, WaitingTime, List<WaitingTime>>>,
    nearbyStops: List<StopInfo>,
    isPinnedCard: Boolean,
    onPin: (routeId: String, destination: String, stopId: String, stopName: String) -> Unit,
    onUnpin: (routeId: String, destination: String, stopId: String) -> Unit,
    onRouteSelect: ((routeId: String, destination: String, stopId: String, stopName: String, arrivalTimes: List<WaitingTime>, distance: Int?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (arrivalItems.isEmpty()) return
    
    ArrivalsCardContainer(
        title = title,
        leadingIcon = leadingIcon,
        modifier = modifier,
        content = {
            arrivalItems.forEachIndexed { index, triple ->
                val rep = triple.second
                val groupList = triple.third
                val rowKey = triple.first + "|" + rep.stopId
                key(rowKey) {
                    ArrivalRow(
                        rep = rep,
                        timesAtStop = timesForSameStopFromGroup(rep, groupList),
                        nearbyStops = nearbyStops,
                        isPinned = isPinnedCard,
                        onPin = onPin,
                        onUnpin = onUnpin,
                        onRouteSelect = onRouteSelect
                    )
                }
            }
        }
    )
}

@Preview
@Composable
private fun SingleArrivalsCardPreview() {
    val sampleRep = WaitingTime("9", 0, "SAN SALVARIO, CORSO D'AZEGLIO", TransportType.TRAM, true, "1", "trip_9_1")
    val sampleTimes = listOf(
        WaitingTime("9", 0, "SAN SALVARIO, CORSO D'AZEGLIO", TransportType.TRAM, true, "1", "trip_9_1"),
        WaitingTime("9", 11, "SAN SALVARIO, CORSO D'AZEGLIO", TransportType.TRAM, true, "1", "trip_9_2")
    )
    val sampleStops = listOf(
        StopInfo("1", "Fermata 646 - ADRIANO", 0.0, 0.0, 111, listOf("9"))
    )
    val sampleItems = listOf(
        Triple("9|SAN SALVARIO, CORSO D'AZEGLIO", sampleRep, sampleTimes)
    )

    SingleArrivalsCard(
        title = "ARRIVI IN EVIDENZA",
        leadingIcon = Icons.Filled.PushPin,
        arrivalItems = sampleItems,
        nearbyStops = sampleStops,
        isPinnedCard = true,
        onPin = { _, _, _, _ -> },
        onUnpin = { _, _, _ -> }
    )
}

// Helper function (from original file)
private fun timesForSameStopFromGroup(rep: WaitingTime, group: List<WaitingTime>): List<WaitingTime> =
    group.filter { it.stopId == rep.stopId }
        .sortedBy { it.minutes }
        .ifEmpty { listOf(rep) }