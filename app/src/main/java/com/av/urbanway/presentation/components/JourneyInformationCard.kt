package com.av.urbanway.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.data.models.JourneyOption
import com.av.urbanway.data.models.StopInfo

@Composable
fun JourneyInformationCard(
    journey: JourneyOption,
    allStops: List<StopInfo>,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.98f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with back button and expand/collapse button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Back button (only show if callback is provided)
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Indietro",
                                tint = Color(0xFF007AFF)
                            )
                        }
                    }

                    Text(
                        "Dettagli Percorso",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1D1F)
                    )
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "Chiudi dettagli" else "Mostra dettagli",
                        tint = Color(0xFF007AFF)
                    )
                }
            }

            // Journey summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // First route chip
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            journey.route1Id,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    },
                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF007AFF),
                        labelColor = Color.White
                    )
                )

                // Transfer indicator and second route if exists
                if (journey.route2Id != null) {
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = "Cambio",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )

                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                journey.route2Id!!,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        },
                        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFFF9500),
                            labelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    "${journey.totalJourneyMinutes}' totali",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
            }

            if (isExpanded) {
                Divider(color = Color(0xFFE5E5E7))

                // Detailed journey steps
                JourneySteps(journey = journey, allStops = allStops)
            }
        }
    }
}

@Composable
private fun JourneySteps(
    journey: JourneyOption,
    allStops: List<StopInfo>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Walking to first stop
        val startWalkingMinutes = calculateWalkingMinutes(journey.startWalkingDist)
        JourneyStep(
            stepType = StepType.WALKING,
            description = "Cammina fino alla fermata per $startWalkingMinutes min",
            stopName = getStopNameById(journey.route1StartStopId?.toString(), allStops),
            routeId = null,
            isFirst = true
        )

        // First route segment - board
        val route1Stops = calculateStopsCount(journey.leg1BoardStopSequence, journey.leg1AlightStopSequence)
        JourneyStep(
            stepType = StepType.TRANSIT,
            description = "Prendi la linea ${journey.route1Id}" + if (route1Stops > 0) " per $route1Stops fermate" else "",
            stopName = getStopNameById(journey.route1StartStopId?.toString(), allStops),
            routeId = journey.route1Id,
            routeColor = Color(0xFF007AFF)
        )

        // First route segment - alight
        JourneyStep(
            stepType = StepType.ALIGHTING,
            description = "Scendi alla fermata",
            stopName = getStopNameById(journey.route1EndStopId?.toString(), allStops),
            routeId = journey.route1Id,
            routeColor = Color(0xFF007AFF)
        )

        // Transfer if needed
        if (journey.route2Id != null) {
            val transferWalkingMinutes = calculateWalkingMinutes(journey.changeWalkingDist)
            val transferDescription = if (transferWalkingMinutes > 0) {
                "Cammina per $transferWalkingMinutes min e vai alla fermata"
            } else {
                "Vai alla fermata"
            }
            JourneyStep(
                stepType = StepType.WALKING,
                description = transferDescription,
                stopName = getStopNameById(journey.route2StartStopId?.toString(), allStops),
                routeId = null
            )

            // Second route segment - board
            val route2Stops = calculateStopsCount(journey.leg2BoardStopSequence, journey.leg2AlightStopSequence)
            JourneyStep(
                stepType = StepType.TRANSIT,
                description = "Prendi la linea ${journey.route2Id}" + if (route2Stops > 0) " per $route2Stops fermate" else "",
                stopName = getStopNameById(journey.route2StartStopId?.toString(), allStops),
                routeId = journey.route2Id!!,
                routeColor = Color(0xFFFF9500)
            )

            // Second route segment - alight
            JourneyStep(
                stepType = StepType.ALIGHTING,
                description = "Scendi alla fermata",
                stopName = getStopNameById(journey.route2EndStopId?.toString(), allStops),
                routeId = journey.route2Id!!,
                routeColor = Color(0xFFFF9500)
            )
        }

        // Walking to destination
        val endWalkingMinutes = calculateWalkingMinutes(journey.endWalkingDist)
        JourneyStep(
            stepType = StepType.WALKING,
            description = "Cammina fino alla destinazione per $endWalkingMinutes min",
            stopName = getStopNameById((journey.route2EndStopId ?: journey.route1EndStopId)?.toString(), allStops),
            routeId = null,
            isLast = true
        )
    }
}

@Composable
private fun JourneyStep(
    stepType: StepType,
    description: String,
    stopName: String,
    routeId: String?,
    routeColor: Color = Color.Gray,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step indicator
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = when (stepType) {
                        StepType.WALKING -> Color(0xFFF2F2F7)
                        StepType.TRANSIT -> routeColor.copy(alpha = 0.1f)
                        StepType.ALIGHTING -> routeColor.copy(alpha = 0.1f)
                    },
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = when (stepType) {
                        StepType.WALKING -> Color(0xFF8E8E93)
                        StepType.TRANSIT -> routeColor
                        StepType.ALIGHTING -> routeColor
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when (stepType) {
                StepType.WALKING -> {
                    Icon(
                        imageVector = Icons.Filled.DirectionsWalk,
                        contentDescription = "Cammina",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(16.dp)
                    )
                }
                StepType.TRANSIT -> {
                    Text(
                        text = routeId?.take(2) ?: "",
                        color = routeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                StepType.ALIGHTING -> {
                    Text(
                        text = "↓",
                        color = routeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Step details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1D1D1F)
                )

                if (routeId != null) {
                    Text(
                        text = routeId,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = routeColor
                    )
                }
            }

            if (stopName.isNotEmpty()) {
                Text(
                    text = stopName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

private fun getStopNameById(stopId: String?, allStops: List<StopInfo>): String {
    if (stopId == null) return ""
    return allStops.find { it.stopId == stopId }?.stopName ?: stopId
}

private fun calculateWalkingMinutes(distanceMeters: Int): Int {
    // Average walking speed is about 5 km/h = 83.33 meters/minute
    // Round up to ensure we don't underestimate walking time
    val walkingSpeedMetersPerMinute = 83.33
    return kotlin.math.ceil(distanceMeters / walkingSpeedMetersPerMinute).toInt().coerceAtLeast(1)
}

private fun calculateStopsCount(boardSequence: Int?, alightSequence: Int?): Int {
    return if (boardSequence != null && alightSequence != null && alightSequence > boardSequence) {
        alightSequence - boardSequence
    } else {
        0
    }
}

private enum class StepType {
    WALKING,
    TRANSIT,
    ALIGHTING
}