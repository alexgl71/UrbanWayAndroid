package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun RouteDetailInfoCard(
    route: String,
    destination: String,
    stopName: String,
    distance: Int?,
    arrivalTimes: List<WaitingTime>,
    onClose: () -> Unit,
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
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = destination.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
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
        }
    }
}

private fun cleanStopName(name: String): String {
    // Remove "Fermata XXX - " prefix
    return name.replaceFirst(Regex("^Fermata \\d+ - "), "")
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
        arrivalTimes = sampleTimes,
        onClose = {}
    )
}