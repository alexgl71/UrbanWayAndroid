package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.av.urbanway.data.models.WaitingTime

@Composable
fun ChatView(
    waitingTimes: List<WaitingTime>,
    nearbyStops: List<com.av.urbanway.data.models.StopInfo> = emptyList(),
    pinnedArrivals: List<com.av.urbanway.data.models.PinnedArrival> = emptyList(),
    onPin: (routeId: String, destination: String, stopId: String, stopName: String) -> Unit,
    onUnpin: (routeId: String, destination: String, stopId: String) -> Unit,
    onVisualizzaClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // State for popup
    var selectedRouteId by remember { mutableStateOf<String?>(null) }
    var showHeadsignPopup by remember { mutableStateOf(false) }

    // Extract unique route IDs from waiting times and clean them
    val allRouteIds = waitingTimes.map { it.route }
        .distinct()
        .map { routeId ->
            // Remove last character if it's 'U'
            if (routeId.endsWith("U")) routeId.dropLast(1) else routeId
        }
        .sorted()

    // Filter out routes that have all their headsigns already pinned
    val routeIds = allRouteIds.filter { cleanRouteId ->
        // Get all available headsigns for this route
        val availableHeadsigns = waitingTimes
            .filter { wt ->
                val cleanedWtRoute = if (wt.route.endsWith("U")) wt.route.dropLast(1) else wt.route
                cleanedWtRoute == cleanRouteId
            }
            .map { it.destination }
            .distinct()

        // Get already pinned headsigns for this route
        val pinnedHeadsigns = pinnedArrivals
            .filter { it.routeId == cleanRouteId }
            .map { it.destination }
            .distinct()

        // Show route circle only if there are unpinned headsigns available
        availableHeadsigns.size > pinnedHeadsigns.size
    }

    // Only show if there are routes
    if (routeIds.isNotEmpty()) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.98f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Linee disponibili",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    ),
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Wrapping layout instead of horizontal scroll
                RouteCirclesGrid(
                    routeIds = routeIds,
                    onRouteClick = { routeId ->
                        selectedRouteId = routeId
                        showHeadsignPopup = true
                    }
                )

                // Pinned routes section
                if (pinnedArrivals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Linee in evidenza",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.2.sp
                        ),
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // List of pinned routes/headsigns
                    pinnedArrivals.forEach { pinnedArrival ->
                        PinnedRouteItem(
                            routeId = pinnedArrival.routeId,
                            destination = pinnedArrival.destination,
                            onClick = {
                                onUnpin(
                                    pinnedArrival.routeId,
                                    pinnedArrival.destination,
                                    pinnedArrival.stopId
                                )
                            },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Visualizza button
                    Button(
                        onClick = onVisualizzaClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0B3D91)
                        )
                    ) {
                        Text(
                            text = "Visualizza",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
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
}

@Composable
private fun RouteCirclesGrid(
    routeIds: List<String>,
    onRouteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Create a wrapping layout by chunking routes into rows
    val chunkedRoutes = routeIds.chunked(6) // Adjust number per row as needed

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chunkedRoutes.forEach { rowRoutes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowRoutes.forEach { routeId ->
                    RouteCircle(
                        routeId = routeId,
                        onClick = { onRouteClick(routeId) }
                    )
                }
            }
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
            .size(48.dp) // Increased by 20% from 40dp
            .clip(CircleShape)
            .background(Color(0xFF0B3D91).copy(alpha = 0.76f)) // Same color as route badges
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = routeId,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
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
    // Filter waiting times for this route and get unique headsigns
    val routeWaitingTimes = waitingTimes.filter {
        val cleanRoute = if (it.route.endsWith("U")) it.route.dropLast(1) else it.route
        cleanRoute == routeId
    }

    val headsignOptions = routeWaitingTimes
        .map { it.destination } // Get all destinations
        .distinct() // Get unique headsigns only
        .filter { destination ->
            // Filter out headsigns that are already pinned for this route (no stopId involved)
            // Use the cleaned routeId to match properly
            val cleanedRouteId = if (routeWaitingTimes.first().route.endsWith("U"))
                routeWaitingTimes.first().route.dropLast(1)
            else
                routeWaitingTimes.first().route

            pinnedArrivals.none { pinned ->
                pinned.routeId == cleanedRouteId &&
                pinned.destination == destination
            }
        }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Seleziona direzione per linea $routeId",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                headsignOptions.forEach { destination ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                // Find the best stop for this route+destination combination
                                val bestWaitingTime = routeWaitingTimes
                                    .filter { it.destination == destination }
                                    .minByOrNull { waitingTime ->
                                        nearbyStops.firstOrNull { it.stopId == waitingTime.stopId }?.distanceToStop ?: Int.MAX_VALUE
                                    }

                                if (bestWaitingTime != null) {
                                    val stopName = nearbyStops.firstOrNull { it.stopId == bestWaitingTime.stopId }?.stopName ?: ""
                                    onHeadsignSelected(
                                        destination,
                                        bestWaitingTime.stopId,
                                        stopName
                                    )
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                    ) {
                        Text(
                            text = destination,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Annulla")
                }
            }
        }
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
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(Color(0xFFF8F9FA))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Small route badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF0B3D91).copy(alpha = 0.76f)),
            contentAlignment = Alignment.Center
        ) {
            val cleanRouteId = if (routeId.endsWith("U")) routeId.dropLast(1) else routeId
            Text(
                text = cleanRouteId,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Destination text
        Text(
            text = destination,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}