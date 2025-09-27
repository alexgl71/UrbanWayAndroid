package com.av.urbanway.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
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
import com.av.urbanway.data.model.*

@Composable
fun JourneyModal(
    data: TransitData.JourneyData,
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
                            text = "Opzioni di percorso",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Da ${data.origin.address ?: "Piazza Adriano"} a ${data.destination.address ?: "Piazza Castello"}",
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

                // Journey routes list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Take remaining space above toolbar
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(data.routes.take(5)) { journeyRoute -> // Show max 5 routes
                        JourneyRouteCard(journeyRoute = journeyRoute)
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
private fun JourneyRouteCard(
    journeyRoute: JourneyRoute
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
            // Header with time info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${journeyRoute.totalDuration}",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " min",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }

                if (journeyRoute.totalWalking > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "Walking",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${journeyRoute.totalWalking} min",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Journey steps
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                journeyRoute.steps.forEachIndexed { index, step ->
                    when (step.type) {
                        StepType.WALK -> {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = "Walk",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        StepType.BUS, StepType.TRAM, StepType.METRO, StepType.TRAIN -> {
                            step.route?.let { route ->
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(getRouteColor(route.name)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = route.name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Add arrow between steps (except last)
                    if (index < journeyRoute.steps.size - 1) {
                        Text(
                            text = "→",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Route details text
            Spacer(modifier = Modifier.height(8.dp))

            val routeDescriptions = journeyRoute.steps
                .filter { it.route != null }
                .mapNotNull { step ->
                    step.route?.let { route ->
                        "${route.name} ${route.direction}"
                    }
                }

            if (routeDescriptions.isNotEmpty()) {
                Text(
                    text = routeDescriptions.joinToString(" → "),
                    color = Color(0xFF666666),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

private fun getRouteColor(routeNumber: String): Color {
    return when (routeNumber) {
        "55" -> Color(0xFF4CAF50) // Green
        "56" -> Color(0xFF2196F3) // Blue
        "16" -> Color(0xFF9C27B0) // Purple
        "68" -> Color(0xFFFF5722) // Deep Orange
        "9" -> Color(0xFFE91E63) // Pink
        "13" -> Color(0xFF795548) // Brown
        "61" -> Color(0xFF607D8B) // Blue Grey
        else -> Color(0xFF757575) // Default grey
    }
}