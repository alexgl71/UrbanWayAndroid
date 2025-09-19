package com.av.urbanway.presentation.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.av.urbanway.data.models.WaitingTime
import com.av.urbanway.presentation.components.widgets.RouteBadge
import com.av.urbanway.presentation.components.widgets.RouteInfoColumn
import com.av.urbanway.presentation.components.ChatChoiceChip
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Lightweight, precomputed item for pinned rows in detail mode
private data class PinnedDetailData(
    val routeId: String,
    val destination: String,
    val stopId: String,
    val stopName: String,
    val distance: Int,
    val timesForStop: List<WaitingTime>
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ArrivalsChatView(
    waitingTimes: List<WaitingTime>,
    nearbyStops: List<com.av.urbanway.data.models.StopInfo> = emptyList(),
    pinnedArrivals: List<com.av.urbanway.data.models.PinnedArrival> = emptyList(),
    onPin: (routeId: String, destination: String, stopId: String, stopName: String) -> Unit,
    onUnpin: (routeId: String, destination: String, stopId: String) -> Unit,
    onAltreLineeClick: () -> Unit = {},
    onOrariClick: () -> Unit = {},
    onMappaClick: () -> Unit = {},
    isPreview: Boolean = false,
    modifier: Modifier = Modifier
) {
    // State for popup
    var selectedRouteId by remember { mutableStateOf<String?>(null) }
    var showHeadsignPopup by remember { mutableStateOf(false) }

    // State to control route circles visibility
    var showRouteCircles by remember { mutableStateOf(pinnedArrivals.isEmpty()) }

    // Extract unique route IDs and filter for display. Use remember to avoid heavy recompute on sheet drag.
    val routeIds by remember(waitingTimes, pinnedArrivals) {
        mutableStateOf(
            waitingTimes.map { it.route }
                .distinct()
                .map { routeId -> if (routeId.endsWith("U")) routeId.dropLast(1) else routeId }
                .distinct()
                .sortedBy { routeId -> routeId.toIntOrNull() ?: Int.MAX_VALUE }
                .filter { routeId ->
                    val availableHeadsigns = waitingTimes
                        .filter { (if (it.route.endsWith("U")) it.route.dropLast(1) else it.route) == routeId }
                        .map { it.destination }
                        .distinct()

                    val pinnedHeadsigns = pinnedArrivals
                        .filter { it.routeId == routeId }
                        .map { it.destination }
                        .distinct()

                    availableHeadsigns.size > pinnedHeadsigns.size
                }
        )
    }

    // Only show if there are routes or pinned arrivals
    if (routeIds.isNotEmpty() || pinnedArrivals.isNotEmpty()) {
        if (isPreview) {
            // PREVIEW MODE: Clean content without container styling
            Column(
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                    // Show compact summary
                    if (pinnedArrivals.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hai ${pinnedArrivals.size} linee preferite: ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black.copy(alpha = 0.8f)
                            )
                            Text(
                                text = pinnedArrivals.joinToString(", ") { it.routeId },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = Color(0xFF0B3D91)
                            )
                        }
                    }

                    if (routeIds.isNotEmpty()) {
                        Text(
                            text = "Ci sono altre ${routeIds.size} linee vicino a te",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Choice chips for preview
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ChatChoiceChip(text = "📋 Dettagli", onClick = onAltreLineeClick)
                        ChatChoiceChip(text = "📅 Orari", onClick = onOrariClick)
                        ChatChoiceChip(text = "🗺️ Mappa", onClick = onMappaClick)
                    }
                }
        } else {
            // DETAIL MODE: Use LazyColumn to keep layout stable under sheet height changes
            // Precompute lightweight maps to avoid heavy filtering during drags

            fun String.cleanedRoute(): String = if (this.endsWith("U")) this.dropLast(1) else this

            val stopsById = remember(nearbyStops) {
                nearbyStops.associateBy { it.stopId }
            }

            val wtByStop = remember(waitingTimes) {
                waitingTimes.groupBy { it.stopId }
            }

            val pinnedDetailItems = remember(waitingTimes, pinnedArrivals, nearbyStops) {
                pinnedArrivals.map { p ->
                    val stopInfo = stopsById[p.stopId]
                    val timesAtStop = wtByStop[p.stopId].orEmpty().filter {
                        it.route.cleanedRoute() == p.routeId && it.destination == p.destination
                    }
                    PinnedDetailData(
                        routeId = p.routeId,
                        destination = p.destination,
                        stopId = p.stopId,
                        stopName = stopInfo?.stopName ?: "",
                        distance = stopInfo?.distanceToStop ?: Int.MAX_VALUE,
                        timesForStop = timesAtStop
                    )
                }
            }

            // Pre-chunk route IDs into rows for lazy list rendering
            val routeRows = remember(routeIds) { routeIds.chunked(6) }

            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pinnedArrivals.isNotEmpty()) {
                    item(key = "pinned_header") {
                        Text(
                            text = "Linee preferite da te",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.2.sp
                            ),
                            color = Color.Black,
                        )
                    }

                    items(
                        items = pinnedDetailItems,
                        key = { it.routeId + "|" + it.destination + "|" + it.stopId }
                    ) { item ->
                        PinnedDetailRow(
                            data = item,
                            onUnpin = { onUnpin(item.routeId, item.destination, item.stopId) },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    item(key = "pinned_spacer") { Spacer(modifier = Modifier.height(4.dp)) }
                }

                if (showRouteCircles && routeIds.isNotEmpty()) {
                    item(key = "routes_header") {
                        Text(
                            text = "Linee disponibili",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.2.sp
                            ),
                            color = Color.Black,
                        )
                    }
                    itemsIndexed(
                        items = routeRows,
                        key = { index, _ -> "route_row_$index" }
                    ) { _, rowRoutes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowRoutes.forEach { routeId ->
                                RouteCircle(
                                    routeId = routeId,
                                    onClick = {
                                        selectedRouteId = routeId
                                        showHeadsignPopup = true
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(6 - rowRoutes.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                item(key = "chips") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (routeIds.isNotEmpty() && !showRouteCircles) {
                            ChatChoiceChip(
                                text = "🚌 Altre linee",
                                onClick = { showRouteCircles = true; onAltreLineeClick() }
                            )
                        }
                        ChatChoiceChip(text = "📅 Orari", onClick = onOrariClick)
                        ChatChoiceChip(text = "🗺️ Mappa", onClick = onMappaClick)
                    }
                }
            }
        }
    }

    // Headsign selection popup
    if (showHeadsignPopup && selectedRouteId != null) {
        HeadsignSelectionPopup(
            routeId = selectedRouteId!!,
            waitingTimes = waitingTimes,
            nearbyStops = nearbyStops,
            pinnedArrivals = pinnedArrivals,
            onHeadsignSelected = { headsign, stopId, stopName ->
                onPin(selectedRouteId!!, headsign, stopId, stopName)
                showHeadsignPopup = false
                selectedRouteId = null
            },
            onDismiss = {
                showHeadsignPopup = false
                selectedRouteId = null
            }
        )
    }
}

// All the helper composables...
// Reuse shared ChatChoiceChip from ChatChips.kt

// RouteCirclesGrid removed; routed rows lazily via parent LazyColumn

@Composable
private fun PinnedDetailRow(
    data: PinnedDetailData,
    onUnpin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: route badge
        RouteBadge(route = data.routeId)

        Spacer(modifier = Modifier.width(12.dp))

        // Center: destination + times
        if (data.timesForStop.isNotEmpty()) {
            RouteInfoColumn(
                destination = data.destination,
                stopName = data.stopName,
                distance = data.distance,
                waitingTimes = data.timesForStop,
                modifier = Modifier.weight(1f)
            )
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.destination,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        letterSpacing = (-0.3).sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF555555),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${data.stopName} - Nessun orario disponibile",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color.Gray
                )
            }
        }

        // Right: unpin action
        IconButton(onClick = onUnpin) {
            Icon(
                imageVector = Icons.Filled.PushPin,
                contentDescription = "Rimuovi",
                tint = Color(0xFFD9731F)
            )
        }
    }
}

@Composable
private fun RouteCircle(
    routeId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(Color(0xFF0B3D91))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = routeId,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PinnedRouteItem(
    routeId: String,
    destination: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF0B3D91)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = routeId,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = destination,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DetailPinnedRouteItem(
    pinnedArrival: com.av.urbanway.data.models.PinnedArrival,
    waitingTimes: List<WaitingTime>,
    nearbyStops: List<com.av.urbanway.data.models.StopInfo>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Debug logging removed to reduce jank during sheet drags

    // Handle the route suffix mismatch: pinned arrivals have clean IDs, API data has U suffix
    val matchingTimes = waitingTimes.filter { wt ->
        val cleanWtRoute = if (wt.route.endsWith("U")) wt.route.dropLast(1) else wt.route
        pinnedArrival.routeId == cleanWtRoute &&
        pinnedArrival.destination == wt.destination &&
        pinnedArrival.stopId == wt.stopId
    }

    Log.d("TRANSITOAPP", "DetailPinnedRouteItem - MatchingTimes found: ${matchingTimes.size}")
    matchingTimes.forEach { wt ->
        Log.d("TRANSITOAPP", "DetailPinnedRouteItem - Matching: route=${wt.route}, destination=${wt.destination}, stopId=${wt.stopId}, minutes=${wt.minutes}")
    }

    // Skip grouping step - we already have the right data from matchingTimes
    // Just use the matching times directly since they already match route+destination+stop
    val timesForStop = matchingTimes.sortedBy { it.minutes }

    Log.d("TRANSITOAPP", "DetailPinnedRouteItem - Final timesForStop: ${timesForStop.size}")

    val stopInfo = nearbyStops.firstOrNull { it.stopId == pinnedArrival.stopId }
    val stopName = stopInfo?.stopName ?: pinnedArrival.stopName
    val distance = stopInfo?.distanceToStop

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Route badge (similar to ArrivalRowContent)
        RouteBadge(route = pinnedArrival.routeId)

        Spacer(modifier = Modifier.width(16.dp))

        // Route info column with arrival times
        if (timesForStop.isNotEmpty()) {
            RouteInfoColumn(
                destination = pinnedArrival.destination,
                stopName = stopName,
                distance = distance,
                waitingTimes = timesForStop,
                modifier = Modifier.weight(1f)
            )
        } else {
            // Fallback to simple text if no arrival times available
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pinnedArrival.destination,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        letterSpacing = (-0.3).sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF555555),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$stopName - Nessun orario disponibile",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color.Gray
                )
            }
        }
    }
}

// Helper function matching ArrivalsCards pattern
private fun distanceForStop(stopId: String, stops: List<com.av.urbanway.data.models.StopInfo>): Int {
    return stops.firstOrNull { it.stopId == stopId }?.distanceToStop ?: Int.MAX_VALUE
}

@Composable
private fun HeadsignSelectionPopup(
    routeId: String,
    waitingTimes: List<WaitingTime>,
    nearbyStops: List<com.av.urbanway.data.models.StopInfo>,
    pinnedArrivals: List<com.av.urbanway.data.models.PinnedArrival>,
    onHeadsignSelected: (headsign: String, stopId: String, stopName: String) -> Unit,
    onDismiss: () -> Unit
) {
    val availableHeadsigns = waitingTimes
        .filter { (if (it.route.endsWith("U")) it.route.dropLast(1) else it.route) == routeId }
        .map { it.destination }
        .distinct()

    val pinnedHeadsigns = pinnedArrivals
        .filter { it.routeId == routeId }
        .map { it.destination }
        .toSet()

    val unpinnedHeadsigns = availableHeadsigns.filter { it !in pinnedHeadsigns }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Scegli destinazione per la linea $routeId",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                unpinnedHeadsigns.forEach { headsign ->
                    val closestWaitingTime = waitingTimes
                        .filter { (if (it.route.endsWith("U")) it.route.dropLast(1) else it.route) == routeId }
                        .filter { it.destination == headsign }
                        .minByOrNull { stop ->
                            nearbyStops.find { it.stopId == stop.stopId }?.distanceToStop ?: Int.MAX_VALUE
                        }

                    if (closestWaitingTime != null) {
                        val stopInfo = nearbyStops.find { it.stopId == closestWaitingTime.stopId }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onHeadsignSelected(
                                        headsign,
                                        closestWaitingTime.stopId,
                                        stopInfo?.stopName ?: "Fermata sconosciuta"
                                    )
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = headsign,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                                if (stopInfo != null) {
                                    Text(
                                        text = "${stopInfo.stopName} - ${stopInfo.distanceToStop}m",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                if (unpinnedHeadsigns.isEmpty()) {
                    Text(
                        text = "Tutte le destinazioni sono già state aggiunte ai preferiti",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
