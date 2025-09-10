package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.data.local.FavoritesManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.RadioButtonChecked
import com.av.urbanway.data.models.ArrivalDisplay
import kotlinx.coroutines.launch

private data class HeadSignGroup(
    val destination: String,
    val stopId: String,
    val stopName: String,
    val distanceMeters: Int,
    val times: List<Triple<Int, Boolean, String>> // (mins, isRealTime, tripId)
)

@Composable
fun RealTimeArrivalsCard(
    arrivalsData: List<ArrivalDisplay>,
    pinnedArrivalsIds: Set<String>,
    highlightStopId: String?,
    onRouteSelect: (routeId: String, params: Map<String, Any>) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = remember { LazyListState() }
    val scope = rememberCoroutineScope()

    // Prepare grouped data similar to iOS logic
    val displayArrivals = remember(arrivalsData) {
        arrivalsData
            .filter { it.waitMinutes >= 0 }
            .sortedBy { it.waitMinutes }
            .take(20)
    }

    val routeGroups: Map<String, List<HeadSignGroup>> = remember(displayArrivals) {
        // Group by routeId|destination first
        val byRouteDest = displayArrivals.groupBy { "${it.routeId}|${it.destination}" }
        val resultGroups = mutableMapOf<String, List<ArrivalDisplay>>()
        byRouteDest.forEach { (key, list) ->
            val closest = list.minByOrNull { it.distanceMeters }
            if (closest != null) {
                val sameStop = list.filter { it.stopId == closest.stopId }.sortedBy { it.waitMinutes }
                val newKey = "$key|${closest.stopId}|${closest.stopName}"
                resultGroups[newKey] = sameStop
            }
        }
        // Map to route -> HeadSignGroup[]
        val final = mutableMapOf<String, MutableList<HeadSignGroup>>()
        resultGroups.forEach { (key, list) ->
            val parts = key.split('|')
            val routeId = parts.getOrNull(0) ?: ""
            val destination = parts.getOrNull(1) ?: ""
            val stopId = parts.getOrNull(2) ?: ""
            val stopName = parts.getOrNull(3) ?: ""
            val distance = list.firstOrNull()?.distanceMeters ?: 0
            val times = list.map { Triple(it.waitMinutes, it.isRealTime, it.tripId) }
            final.getOrPut(routeId) { mutableListOf() }
                .add(HeadSignGroup(destination, stopId, stopName, distance, times))
        }
        // Sort groups in each route by distance
        final.forEach { (_, groups) -> groups.sortBy { it.distanceMeters } }
        final
    }

    // Create ordered items list: first pinned routes (merged), then others
    val pinnedByRoute = remember(routeGroups, pinnedArrivalsIds) {
        routeGroups.mapValues { (route, groups) ->
            groups.filter { g -> pinnedArrivalsIds.contains("${route}_${g.destination}_${g.stopId}") }
        }.filterValues { it.isNotEmpty() }
    }
    val otherRoutes = remember(routeGroups, pinnedByRoute) {
        routeGroups.mapValues { (route, groups) ->
            val pinned = pinnedByRoute[route]?.toSet() ?: emptySet()
            groups.filter { it !in pinned }
        }.filterValues { it.isNotEmpty() }
    }

    // Auto-scroll to highlighted stop's route on first appearance or when highlight changes
    var didAutoScroll by remember { mutableStateOf(false) }
    LaunchedEffect(highlightStopId, routeGroups) {
        if (!didAutoScroll && highlightStopId != null) {
            // Find route containing a group with the stopId
            val targetIndex = routeGroups.keys.indexOfFirst { route ->
                routeGroups[route]?.any { it.stopId.equals(highlightStopId, ignoreCase = true) } == true
            }
            if (targetIndex >= 0) {
                didAutoScroll = true
                scope.launch { listState.animateScrollToItem(targetIndex) }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (displayArrivals.isEmpty()) {
            NoArrivalsView()
            return@Column
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (pinnedByRoute.isNotEmpty()) {
                item {
                    SectionCard(headerIcon = "pin", title = "In evidenza") {
                        Column(Modifier.padding(8.dp)) {
                            pinnedByRoute.keys.sortedBy { routeId ->
                                pinnedByRoute[routeId]?.minOfOrNull { it.distanceMeters } ?: Int.MAX_VALUE
                            }.forEach { routeId ->
                                RouteGroupCard(
                                    route = routeId,
                                    groups = pinnedByRoute[routeId] ?: emptyList(),
                                    embedded = true,
                                    showPinnedGlyph = false,
                                ) { dest, stopId, tripId ->
                                    onRouteSelect(
                                        routeId,
                                        mapOf("destination" to dest, "tripId" to tripId, "stopId" to stopId)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (otherRoutes.isNotEmpty()) {
                item {
                    SectionCard(headerIcon = "list", title = "Altre linee") {
                        Column(Modifier.padding(8.dp)) {
                            otherRoutes.keys.sortedBy { routeId ->
                                otherRoutes[routeId]?.minOfOrNull { it.distanceMeters } ?: Int.MAX_VALUE
                            }.forEach { routeId ->
                                RouteGroupCard(
                                    route = routeId,
                                    groups = otherRoutes[routeId] ?: emptyList(),
                                    embedded = true,
                                    showPinnedGlyph = false,
                                ) { dest, stopId, tripId ->
                                    onRouteSelect(
                                        routeId,
                                        mapOf("destination" to dest, "tripId" to tripId, "stopId" to stopId)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (pinnedByRoute.isNotEmpty()) {
                item { Spacer(Modifier.height(1.dp)) }
            }
        }
    }
}

@Composable
private fun NoArrivalsView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Nessun arrivo previsto", color = Color.Gray)
    }
}

@Composable
private fun SectionCard(
    headerIcon: String,
    title: String,
    content: @Composable () -> Unit
) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F3))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simple header; iconName not mapped to SF Symbols here
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            LivePill()
        }
        content()
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
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF34C759))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun RouteGroupCard(
    route: String,
    groups: List<HeadSignGroup>,
    embedded: Boolean,
    showPinnedGlyph: Boolean = false,
    onSelect: (destination: String, stopId: String, tripId: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (embedded) 0.dp else 8.dp)
    ) {
        RouteHeader(route)
        Spacer(Modifier.height(4.dp))
        Column(Modifier.padding(horizontal = 8.dp)) {
            groups.forEachIndexed { index, g ->
                HeadSignRow(route, g, onSelect)
                if (index < groups.lastIndex) {
                    Divider(color = Color.Black.copy(alpha = 0.08f))
                }
            }
        }
    }
}

@Composable
private fun RouteHeader(route: String) {
    val isMetro = route.uppercase().contains("METRO")
    val routeColor = if (isMetro) Color(0xFFFF9800) else Color(0xFF1E88E5)
    val routeDisplay = if (route.uppercase().endsWith("U")) route.dropLast(1) else route
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(routeColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                routeDisplay,
                color = routeColor,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    letterSpacing = (-0.8).sp
                ),
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(40.dp)
                .background(routeColor.copy(alpha = 0.12f))
        )
    }
}

@Composable
private fun HeadSignRow(
    route: String,
    group: HeadSignGroup,
    onSelect: (destination: String, stopId: String, tripId: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(group.destination, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val shown = group.times.take(4)
            shown.forEachIndexed { index, (mins, isReal, _) ->
                if (index > 0) Spacer(Modifier.width(10.dp))
                val green = Color(0xFF34C759)
                val base = if (isReal) green else Color.Gray
                val fill = if (isReal) base.copy(alpha = 0.9f) else base.copy(alpha = 0.18f)
                val textColor = if (isReal) Color.White else Color.Black
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(fill)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isReal) Icons.Filled.RadioButtonChecked else Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${mins}'",
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (group.times.size > 4) {
                Spacer(Modifier.width(10.dp))
                Text("+${group.times.size - 4}", color = Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            // Whole row clickable
            TextButton(onClick = { onSelect(group.destination, group.stopId, group.times.firstOrNull()?.third ?: "") }) {
                Text("Dettagli", color = Color(0xFFD9731F))
            }
        }
    }
}
