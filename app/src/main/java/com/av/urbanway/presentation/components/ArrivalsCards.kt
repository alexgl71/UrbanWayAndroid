package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Tram
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.av.urbanway.data.models.StopInfo
import com.av.urbanway.data.models.WaitingTime
import com.av.urbanway.data.models.PinnedArrival
import android.util.Log
import androidx.compose.ui.Alignment

@Composable
fun ArrivalsCards(
    waitingTimes: List<WaitingTime>,
    nearbyStops: List<StopInfo>,
    pinnedArrivals: List<PinnedArrival>,
    onPin: (routeId: String, destination: String, stopId: String, stopName: String) -> Unit,
    onUnpin: (routeId: String, destination: String, stopId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Split times first (route+destination+stop specificity), then group each set separately.
    val pinnedTimes = waitingTimes.filter { wt ->
        pinnedArrivals.any { it.routeId == wt.route && it.destination == wt.destination && it.stopId == wt.stopId }
    }
    val otherTimes = waitingTimes.filter { wt ->
        pinnedArrivals.none { it.routeId == wt.route && it.destination == wt.destination && it.stopId == wt.stopId }
    }

    val pinnedGrouped = groupByRouteDestination(pinnedTimes)
    val pinnedReps = pinnedGrouped.mapNotNull { (key, list) ->
        val rep = repsForGroup(list, nearbyStops) ?: return@mapNotNull null
        key to rep
    }.sortedBy { it.second.minutes }

    val otherGrouped = groupByRouteDestination(otherTimes)
    val otherReps = otherGrouped.mapNotNull { (key, list) ->
        val rep = repsForGroup(list, nearbyStops) ?: return@mapNotNull null
        key to rep
    }.sortedBy { it.second.minutes }

    Log.d("TRANSITOAPP", "ArrivalsCards: pinnedTimes=${pinnedTimes.size}, otherTimes=${otherTimes.size}, pinnedReps=${pinnedReps.size}, otherReps=${otherReps.size}")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (pinnedReps.isNotEmpty()) {
            ArrivalsCard(
                title = "ARRIVI IN EVIDENZA",
                leading = { Icon(Icons.Filled.PushPin, contentDescription = null) },
                items = pinnedReps.map { Triple(it.first, it.second, pinnedGrouped[it.first] ?: emptyList()) },
                nearbyStops = nearbyStops,
                isPinnedCard = true,
                onPin = onPin,
                onUnpin = onUnpin
            )
        }
        if (otherReps.isNotEmpty()) {
            ArrivalsCard(
                title = "LINEE IN ARRIVO",
                leading = { Icon(Icons.Filled.AccessTime, contentDescription = null) },
                items = otherReps.map { Triple(it.first, it.second, otherGrouped[it.first] ?: emptyList()) },
                nearbyStops = nearbyStops,
                isPinnedCard = false,
                onPin = onPin,
                onUnpin = onUnpin
            )
        } else if (pinnedReps.isEmpty()) {
            ArrivalsEmptyCard()
        }
    }
}

@Composable
private fun ArrivalsCard(
    title: String,
    leading: @Composable () -> Unit,
    items: List<Triple<String, WaitingTime, List<WaitingTime>>>,
    nearbyStops: List<StopInfo>,
    isPinnedCard: Boolean,
    onPin: (routeId: String, destination: String, stopId: String, stopName: String) -> Unit,
    onUnpin: (routeId: String, destination: String, stopId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // Header band
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F9FA))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading()
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            LivePill()
        }

        Column(Modifier.padding(16.dp)) {
            items.forEachIndexed { index, triple ->
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
                        onUnpin = onUnpin
                    )
                }
            }
        }
    }
}

@Composable
private fun LivePill() {
    Text(
        text = "LIVE",
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF34C759))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun ArrivalsEmptyCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F9FA))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.AccessTime, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("LINEE IN ARRIVO", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            LivePill()
        }
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Nessuna fermata nelle vicinanze", color = Color.Gray)
        }
    }
}

@Composable
private fun ArrivalRow(
    rep: WaitingTime,
    timesAtStop: List<WaitingTime>,
    nearbyStops: List<StopInfo>,
    isPinned: Boolean,
    onPin: (routeId: String, destination: String, stopId: String, stopName: String) -> Unit,
    onUnpin: (routeId: String, destination: String, stopId: String) -> Unit
) {
    // Swipe-to-pin/unpin using Material3 SwipeToDismissBox
    val haptic = LocalHapticFeedback.current
    val swipeState = androidx.compose.material3.rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            // Trigger action when swiped to either side
            if (value != androidx.compose.material3.SwipeToDismissBoxValue.Settled) {
                val stopName = nearbyStops.firstOrNull { it.stopId == rep.stopId }?.stopName ?: ""
                if (isPinned) {
                    onUnpin(rep.route, rep.destination, rep.stopId)
                } else {
                    onPin(rep.route, rep.destination, rep.stopId, stopName)
                }
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                // Do not actually dismiss the row; keep it in place
                false
            } else {
                false
            }
        }
    )

    androidx.compose.material3.SwipeToDismissBox(
        state = swipeState,
        enableDismissFromStartToEnd = false, // only allow trailing swipe to match iOS
        enableDismissFromEndToStart = true,
        backgroundContent = {
            // Show a trailing-only icon while dragging, hidden when idle
            val isActive = swipeState.currentValue == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart ||
                    swipeState.targetValue == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
            if (isActive) {
                val actionColor = if (isPinned) Color(0xFFE53935) else Color(0xFF43A047)
                val icon = if (isPinned) Icons.Filled.RemoveCircle else Icons.Filled.PushPin
                val alpha by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isActive) 1f else 0f, label = "swipeAlpha"
                )
                val scale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isActive) 1f else 0.9f, label = "swipeScale"
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(actionColor.copy(alpha = 0.10f))
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(46.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(actionColor.copy(alpha = 0.22f))
                            .scale(scale),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = actionColor.copy(alpha = alpha))
                    }
                }
            } else {
                // Empty background when not dragging or dragging from start
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Column 1: Route badge with integrated transport icon
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B3D91).copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(Modifier.height(2.dp))
                    // Transport icon at the top - larger
                    Icon(
                        imageVector = getTransportIcon(rep.route),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    // Route number at the bottom - larger and more readable
                    Text(
                        text = displayRoute(rep.route),
                        color = Color.White,
                        fontWeight = FontWeight.W300,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 20.sp,
                            letterSpacing = (-0.1).sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                }
            }
            
            Spacer(Modifier.width(16.dp))
            // Column 2: Split into two rows
            Column(
                modifier = Modifier.weight(1f)
            ) {

                // Row 1: Headsign
                Text(
                    text = rep.destination,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF555555),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                // Row 2: Fermata stopname and right aligned the distance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val stopName = nearbyStops.firstOrNull { it.stopId == rep.stopId }?.stopName.toString()
                    Text(stopName, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp))
                    Spacer(Modifier.weight(1f))

                    // Distance on the right
                    val stopDistance = nearbyStops.firstOrNull { it.stopId == rep.stopId }?.distanceToStop
                    if (stopDistance != null) {
                        Text(
                            text = "${stopDistance}mt",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))
                // Row 3: Arrival times (left) and distance (right)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Arrival times on the left
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val shown = timesAtStop.take(3)
                        val remaining = (timesAtStop.size - shown.size).coerceAtLeast(0)
                        shown.forEachIndexed { i, waitingTime ->
                            if (i > 0) Spacer(Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (waitingTime.minutes <= 0) "Ora" else "${waitingTime.minutes}'",
                                    color = if (waitingTime.isRealTime) Color(0xFF34C759) else Color.Gray,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                                    fontWeight = if (waitingTime.isRealTime) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(Modifier.width(2.dp))
                                // Icon after the text - sync for real-time, clock for scheduled
                                if (waitingTime.isRealTime) {
                                    Icon(
                                        Icons.Filled.Sync,
                                        contentDescription = null,
                                        tint = Color(0xFF34C759),
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Filled.AccessTime,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        if (remaining > 0) {
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = "+${remaining}",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }


                }
                Spacer(Modifier.height(2.dp))


            }
        }
    }
}

private fun displayRoute(route: String): String {
    return cleanRoute(route)
}

private fun getTransportIcon(route: String): androidx.compose.ui.graphics.vector.ImageVector {
    val cleanedRoute = cleanRoute(route)
    
    // Metro/Subway routes
    if (cleanedRoute == "M1S" || cleanedRoute == "M1" || cleanedRoute.startsWith("METRO")) {
        return Icons.Default.Subway
    }
    
    // Tram routes
    if (cleanedRoute in listOf("3", "4", "9", "10", "13", "15", "16CS", "16CD")) {
        return Icons.Default.Tram
    }
    
    // Default to bus for all other routes
    return Icons.Filled.DirectionsBus
}

private fun cleanRoute(route: String): String {
    val upper = route.uppercase()
    // Metro lines should display as M1
    if (upper.startsWith("METRO")) return "M1"
    // Drop trailing letter suffix (e.g., 56U -> 56, ST1U -> ST1, 9U -> 9)
    return if (route.isNotEmpty() && route.last().isLetter()) route.dropLast(1).uppercase() else upper
}

// Group reps
private fun groupByRouteDestination(times: List<WaitingTime>): Map<String, List<WaitingTime>> =
    times.groupBy { it.route + "|" + it.destination }

private fun repsForGroup(group: List<WaitingTime>, stops: List<StopInfo>): WaitingTime? {
    return group.minWithOrNull(compareBy<WaitingTime> { distanceForStop(it.stopId, stops) }.thenBy { it.minutes })
}

private fun timesForSameStopFromGroup(rep: WaitingTime, group: List<WaitingTime>): List<WaitingTime> =
    group.filter { it.stopId == rep.stopId }
        .sortedBy { it.minutes }
        .ifEmpty { listOf(rep) }

private fun distanceForStop(stopId: String, stops: List<StopInfo>): Int {
    return stops.firstOrNull { it.stopId == stopId }?.distanceToStop ?: Int.MAX_VALUE
}
