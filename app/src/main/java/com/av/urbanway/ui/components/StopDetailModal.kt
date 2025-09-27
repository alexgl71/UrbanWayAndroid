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
import com.av.urbanway.data.model.Route
import com.av.urbanway.data.model.RouteSelection
import com.av.urbanway.data.model.Stop
import com.av.urbanway.data.model.TransitData

@Composable
fun StopDetailModal(
    data: TransitData.StopDetailData,
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
                            text = data.stop.name,
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )

                        val routeCount = data.routes.size
                        val arrivalCount = data.arrivals.size

                        Text(
                            text = "$routeCount linee • $arrivalCount arrivi",
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

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Take remaining space above toolbar
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Stop location info section
                    item {
                        StopInfoCard(data.stop)
                    }

                    // Routes section
                    item {
                        Text(
                            text = "Linee che passano da questa fermata",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(data.routes) { route ->
                        StopDetailRouteCard(
                            route = route,
                            arrivals = data.arrivals.filter { it.routeId == route.id },
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
private fun StopInfoCard(stop: Stop) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Informazioni fermata",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ID: ${stop.id}",
                color = Color.Gray,
                fontSize = 12.sp
            )

            if (stop.location.address != null) {
                Text(
                    text = "Indirizzo: ${stop.location.address}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "Coordinate: ${String.format("%.6f", stop.location.latitude)}, ${String.format("%.6f", stop.location.longitude)}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun StopDetailRouteCard(
    route: Route,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Create RouteSelection from route info
                        val routeSelection = RouteSelection(
                            routeId = route.id,
                            routeName = route.name,
                            headsign = route.direction,
                            tripId = arrivals.firstOrNull()?.tripId ?: "",
                            displayText = "${route.name} - ${route.direction}"
                        )
                        onRouteClick(routeSelection)
                    }
            ) {
                // Route number circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(getRouteColor(route.name)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = extractRouteNumber(route.name),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Route info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = route.direction,
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Show next arrivals if available
                    if (arrivals.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            arrivals
                                .sortedBy { it.realTimeMinutes }
                                .take(3)
                                .forEach { arrival ->
                                    ArrivalTimeBadge(arrival.realTimeMinutes, arrival.isRealTime)
                                }
                        }
                    } else {
                        Text(
                            text = "Nessun arrivo disponibile",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
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

private fun extractRouteNumber(routeName: String): String {
    return routeName.filter { it.isDigit() }.ifEmpty { routeName.take(2) }
}

private fun getRouteColor(routeName: String): Color {
    val routeNumber = extractRouteNumber(routeName)
    return when (routeNumber) {
        "11" -> Color(0xFF9C27B0) // Purple
        "12" -> Color(0xFF9C27B0) // Purple
        "4" -> Color(0xFF9C27B0) // Purple
        "52" -> Color(0xFF9C27B0) // Purple
        "64" -> Color(0xFF9C27B0) // Purple
        "15" -> Color(0xFF4CAF50) // Green
        "16" -> Color(0xFFFF9800) // Orange
        "55" -> Color(0xFFF44336) // Red
        "56" -> Color(0xFF3F51B5) // Indigo
        "68" -> Color(0xFF795548) // Brown
        "9" -> Color(0xFFE91E63) // Pink
        else -> Color(0xFF2196F3) // Blue default
    }
}