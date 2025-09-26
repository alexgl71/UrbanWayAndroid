package com.av.urbanway.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.av.urbanway.data.model.Arrival
import com.av.urbanway.data.model.RouteSelection
import com.av.urbanway.data.model.TransitData

@Composable
fun NearbyModal(
    data: TransitData.NearbyData,
    onDismiss: () -> Unit,
    onRouteClick: (RouteSelection) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Arrivi nelle vicinanze",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )

                        val uniqueRoutes = data.arrivals.map { it.routeName }.toSet().size
                        val totalStops = data.stops.size

                        Text(
                            text = "$uniqueRoutes linee • $totalStops fermate",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }
                }

                // Group arrivals by routeId first, then by headsign/direction
                val groupedByRoute = data.arrivals.groupBy {
                    it.routeName.filter { char -> char.isDigit() } // Extract route number
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Take remaining space above toolbar
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(groupedByRoute.entries.toList()) { (routeNumber, arrivals) ->
                        RouteCard(
                            routeNumber = routeNumber,
                            arrivals = arrivals,
                            onRouteClick = onRouteClick
                        )
                    }

                    // Add bottom padding for toolbar space
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            // Bottom toolbar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color.White)
            ) {
                ModalBottomToolbar(
                    onMapClick = {
                        // TODO: Implement map functionality
                    },
                    onCloseClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun RouteCard(
    routeNumber: String,
    arrivals: List<Arrival>,
    onRouteClick: (RouteSelection) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Route number circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(getRouteColor(routeNumber)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = routeNumber,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Route destinations grouped by headsign
                Column(modifier = Modifier.weight(1f)) {
                    // Group by direction/headsign and show each one
                    val groupedByDirection = arrivals.groupBy { it.direction }

                    groupedByDirection.forEach { (direction, arrivalsForDirection) ->
                        // Make each headsign/direction clickable
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Get the first arrival for this direction to extract trip info
                                    val firstArrival = arrivalsForDirection.firstOrNull()
                                    if (firstArrival != null) {
                                        // Extract destination from headsign
                                        val destination = direction.split(",").firstOrNull()?.trim()
                                        val displayText = if (destination != null) {
                                            "$routeNumber - $destination"
                                        } else {
                                            "$routeNumber - $direction"
                                        }

                                        // Create RouteSelection with all required info
                                        val routeSelection = RouteSelection(
                                            routeId = firstArrival.routeId,
                                            routeName = firstArrival.routeName,
                                            headsign = direction,
                                            tripId = firstArrival.tripId,
                                            displayText = displayText
                                        )
                                        onRouteClick(routeSelection)
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            // Headsign title
                            Text(
                                text = direction,
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            // Arrival time badges for this headsign
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                            ) {
                                arrivalsForDirection
                                    .sortedBy { it.realTimeMinutes }
                                    .take(3)
                                    .forEach { arrival ->
                                        ArrivalTimeBadge(arrival.realTimeMinutes, arrival.isRealTime)
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
private fun ArrivalTimeBadge(
    minutes: Int,
    isRealTime: Boolean
) {
    val (text, backgroundColor) = when {
        minutes == 0 -> "Ora" to Color(0xFF4CAF50) // Green for "now"
        minutes == 1 -> "1 min" to Color(0xFFFFC107) // Yellow for 1 minute
        else -> "$minutes min" to Color(0xFFE0E0E0) // Gray for other times
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            color = if (minutes <= 1) Color.White else Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun getRouteColor(routeNumber: String): Color {
    return when (routeNumber) {
        "11" -> Color(0xFF9C27B0) // Purple
        "12" -> Color(0xFF9C27B0) // Purple
        "4" -> Color(0xFF9C27B0) // Purple
        "52" -> Color(0xFF9C27B0) // Purple
        "64" -> Color(0xFF9C27B0) // Purple
        else -> Color(0xFF2196F3) // Blue default
    }
}