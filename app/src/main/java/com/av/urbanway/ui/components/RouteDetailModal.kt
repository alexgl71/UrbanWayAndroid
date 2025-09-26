package com.av.urbanway.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
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
import com.av.urbanway.data.model.RouteStop
import com.av.urbanway.data.model.TransitData

@Composable
fun RouteDetailModal(
    data: TransitData.RouteDetailData,
    onDismiss: () -> Unit
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
                // Orange Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFFF6B35),
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Route number circle
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = data.route.name,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Linea ${data.route.name}",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${data.stops.size} fermate • Percorso completo",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Stops Timeline
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(data.stops) { stop ->
                        StopTimelineItem(
                            stop = stop,
                            isFirst = data.stops.first() == stop,
                            isLast = data.stops.last() == stop
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
private fun StopTimelineItem(
    stop: RouteStop,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timeline column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            // Top line (only if not first)
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                        .background(Color(0xFFFF6B35))
                )
            }

            // Circle dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF6B35))
            )

            // Bottom line (only if not last)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                        .background(Color(0xFFFF6B35))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Stop name
        Text(
            text = stop.stopName,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // Time
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Time",
                tint = Color(0xFFFF6B35),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatArrivalTime(stop.arrivalDate),
                color = Color(0xFFFF6B35),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatArrivalTime(arrivalDate: Int): String {
    // Convert seconds since start of day to HH:MM format
    val hours = arrivalDate / 3600
    val minutes = (arrivalDate % 3600) / 60
    return String.format("%02d:%02d", hours, minutes)
}