package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.data.models.WaitingTime
import com.av.urbanway.data.models.StopInfo
import com.av.urbanway.presentation.components.widgets.RouteBadge
import com.av.urbanway.presentation.components.widgets.ArrivalTimesRow
import kotlinx.coroutines.delay

@Composable
fun RouteDetailInfoCard(
    route: String,
    destination: String,
    stopName: String,
    distance: Int?,
    arrivalTimes: List<WaitingTime>,
    showStopsList: Boolean = false,
    routeStops: List<RouteStopInfo> = emptyList(),
    selectedStopId: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header without close button
            Text(
                text = destination.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Route info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Route badge
                RouteBadge(route = route)
                
                // Route details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Stop name with distance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cleanStopName(stopName),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        distance?.let {
                            Text(
                                text = "${it}mt",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Arrival times
                    if (arrivalTimes.isNotEmpty()) {
                        ArrivalTimesRow(waitingTimes = arrivalTimes)
                    }
                }
            }

            // Route stops list (expandable)
            if (showStopsList && routeStops.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                // Stops section header
                Text(
                    text = "Fermate del percorso",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable stops list
                RouteStopsSection(
                    stops = routeStops,
                    selectedStopId = selectedStopId,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Fixed height for scrolling
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}

private fun cleanStopName(name: String): String {
    // Remove "Fermata XXX - " prefix
    return name.replaceFirst(Regex("^Fermata \\d+ - "), "")
}

// Route stops section with scrollable list (copied from RouteDetailCard.kt)
@Composable
private fun RouteStopsSection(
    stops: List<RouteStopInfo>,
    selectedStopId: String?,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to selected stop
    LaunchedEffect(selectedStopId, stops) {
        selectedStopId?.let { targetStopId ->
            val index = stops.indexOfFirst { it.stopId == targetStopId }
            if (index >= 0) {
                delay(100) // Small delay to ensure layout is complete
                listState.animateScrollToItem(index)
            }
        }
    }

    Box(modifier = modifier) {
        when {
            stops.isEmpty() -> {
                Text(
                    text = "Nessun dettaglio disponibile",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(stops) { index, stop ->
                        StopListItem(
                            stop = stop,
                            isFirst = index == 0,
                            isLast = index == stops.size - 1,
                            showConnector = index < stops.size - 1
                        )
                    }
                }
            }
        }
    }
}

// Stop list item with timeline design (copied from RouteDetailCard.kt)
@Composable
private fun StopListItem(
    stop: RouteStopInfo,
    isFirst: Boolean,
    isLast: Boolean,
    showConnector: Boolean,
    modifier: Modifier = Modifier
) {
    val navyBlue = Color(0xFF0B3D91)
    val verticalLineColor = Color.Gray.copy(alpha = 0.25f)
    val isTerminal = isFirst || isLast
    val backgroundColor = if (stop.isSelected) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 0.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Timeline column
        Box(
            modifier = Modifier.width(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Top connector line
                if (!isFirst) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(10.dp)
                            .background(verticalLineColor)
                    )
                }

                // Circle/Dot (double size)
                Box(
                    modifier = Modifier
                        .size(if (isTerminal) 24.dp else 20.dp)
                        .clip(CircleShape)
                        .background(if (isTerminal) navyBlue else Color.White)
                        .then(
                            if (!isTerminal) {
                                Modifier.padding(2.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(navyBlue, CircleShape)
                            } else {
                                Modifier
                            }
                        )
                )

                // Bottom connector line
                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(10.dp)
                            .background(verticalLineColor)
                    )
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = cleanStopName(stop.stopName),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                stop.arrivalTimeSeconds?.let { seconds ->
                    Text(
                        text = formatArrivalTime(seconds),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color.Blue
                    )
                }

                stop.stopCode?.let { code ->
                    Text(
                        text = "Fermata $code",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Helper function for formatting arrival time (copied from RouteDetailCard.kt)
private fun formatArrivalTime(seconds: Int): String {
    val minutes = seconds / 60
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) {
        String.format("%02d:%02d", hours, mins)
    } else {
        "${mins}m"
    }
}

@Preview
@Composable
private fun RouteDetailInfoCardPreview() {
    val sampleTimes = listOf(
        WaitingTime("55", 2, "VANCHIGLIA, CORSO FARINI", com.av.urbanway.data.models.TransportType.BUS, true, "3287", "trip_55_1"),
        WaitingTime("55", 13, "VANCHIGLIA, CORSO FARINI", com.av.urbanway.data.models.TransportType.BUS, true, "3287", "trip_55_2"),
        WaitingTime("55", 26, "VANCHIGLIA, CORSO FARINI", com.av.urbanway.data.models.TransportType.BUS, false, "3287", "trip_55_3")
    )
    
    RouteDetailInfoCard(
        route = "55",
        destination = "VANCHIGLIA, CORSO FARINI",
        stopName = "Fermata 3287 - FERRUCCI",
        distance = 221,
        arrivalTimes = sampleTimes
    )
}