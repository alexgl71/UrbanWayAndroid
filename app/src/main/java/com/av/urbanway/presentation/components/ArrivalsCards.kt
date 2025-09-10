package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.av.urbanway.data.models.StopInfo
import com.av.urbanway.data.models.WaitingTime
import com.av.urbanway.data.models.PinnedArrival
import android.util.Log

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
                .background(Color(0xFFF0F0F3))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading()
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            LivePill()
        }

        Column(Modifier.padding(12.dp)) {
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
                if (index < items.lastIndex) {
                    Divider(color = Color.Black.copy(alpha = 0.08f))
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
                .background(Color(0xFFF0F0F3))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.AccessTime, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("LINEE IN ARRIVO", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
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
    timesAtStop: List<Int>,
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
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        // Route circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E88E5)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayRoute(rep.route),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = (-0.5).sp
                )
            )
        }
        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DirectionsBus, contentDescription = null, tint = Color(0xFF1E88E5))
                Spacer(Modifier.width(6.dp))
                Text(rep.destination, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.W500, maxLines = 1)
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val shown = timesAtStop.take(3)
                val remaining = (timesAtStop.size - shown.size).coerceAtLeast(0)
                shown.forEachIndexed { i, m ->
                    if (i > 0) Spacer(Modifier.width(12.dp))
                    Icon(Icons.Filled.AccessTime, contentDescription = null, tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(if (m == 0) "Ora" else "${m}'", color = Color.Gray)
                }
                if (remaining > 0) {
                    Spacer(Modifier.width(12.dp))
                    Text("+${remaining}", color = Color.Gray)
                }
            }
        }
        }
    }
}

private fun displayRoute(route: String): String {
    val upper = route.uppercase()
    // Metro lines should display as M1
    if (upper.startsWith("METRO")) return "M1"
    // Drop trailing letter suffix (e.g., 56U -> 56, ST1U -> ST1)
    return if (route.isNotEmpty() && route.last().isLetter()) route.dropLast(1) else route
}

// Group reps
private fun groupByRouteDestination(times: List<WaitingTime>): Map<String, List<WaitingTime>> =
    times.groupBy { it.route + "|" + it.destination }

private fun repsForGroup(group: List<WaitingTime>, stops: List<StopInfo>): WaitingTime? {
    return group.minWithOrNull(compareBy<WaitingTime> { distanceForStop(it.stopId, stops) }.thenBy { it.minutes })
}

private fun timesForSameStopFromGroup(rep: WaitingTime, group: List<WaitingTime>): List<Int> =
    group.filter { it.stopId == rep.stopId }
        .map { it.minutes }
        .sorted()
        .ifEmpty { listOf(rep.minutes) }

private fun distanceForStop(stopId: String, stops: List<StopInfo>): Int {
    return stops.firstOrNull { it.stopId == stopId }?.distanceToStop ?: Int.MAX_VALUE
}
