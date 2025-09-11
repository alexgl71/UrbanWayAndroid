package com.av.urbanway.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.data.models.JourneyOption
import com.av.urbanway.data.models.Location

@Composable
fun JourneyCardView(
    from: String,
    to: String,
    journey: JourneyOption,
    currentLocation: Location,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                spotColor = Color.Black.copy(alpha = 0.1f),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            HeaderSection(from = from, to = to, journey = journey)
            
            // Main Journey Info
            MainInfoSection(journey = journey)
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            // Journey Steps
            JourneyStepsSection(from = from, to = to, journey = journey)
            
            // Expand/Collapse Button
            ExpandCollapseButton(
                isExpanded = isExpanded,
                onToggle = { isExpanded = !isExpanded }
            )
            
            // Expanded Details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                ExpandedDetailsSection(journey = journey)
            }
        }
    }
}

@Composable
private fun HeaderSection(
    from: String,
    to: String,
    journey: JourneyOption,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Route,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Percorso selezionato",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "$from → $to",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (journey.depTime != null && journey.arrTime != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatTime(journey.depTime)} → ${formatTime(journey.arrTime)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Journey type badge
        Text(
            text = if (journey.isDirect == 1) "DIRETTO" else "CAMBIO",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .background(
                    color = if (journey.isDirect == 1) Color(0xFF4CAF50) else Color(0xFFFFA500),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun MainInfoSection(
    journey: JourneyOption,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        JourneyInfoItem(
            icon = Icons.Filled.Schedule,
            value = "${journey.totalJourneyMinutes}",
            label = "minuti",
            color = MaterialTheme.colorScheme.primary
        )
        
        JourneyInfoItem(
            icon = Icons.Filled.DirectionsWalk,
            value = "${journey.totalWalkingDistance}",
            label = "metri",
            color = Color(0xFF4CAF50)
        )
        
        JourneyInfoItem(
            icon = Icons.Filled.DirectionsBus,
            value = "${journey.totalStops}",
            label = "fermate",
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun JourneyInfoItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = color
            )
        }
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun JourneyStepsSection(
    from: String,
    to: String,
    journey: JourneyOption,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Departure
        JourneyStep(
            type = JourneyStepType.START,
            icon = Icons.Filled.LocationOn,
            title = "Partenza",
            subtitle = from,
            info = "${journey.startWalkingDist}m a piedi",
            color = Color(0xFF4CAF50)
        )
        
        // First route
        JourneyStep(
            type = JourneyStepType.ROUTE,
            icon = Icons.Filled.DirectionsBus,
            title = "Prendi ${journey.route1Id}",
            subtitle = buildString {
                val stopsText = "per ${if (journey.isDirect == 1) journey.totalStops else journey.totalStops / 2} fermate"
                if (!journey.route1Headsign.isNullOrEmpty()) {
                    append("verso ${journey.route1Headsign} • $stopsText")
                } else {
                    append(stopsText)
                }
            },
            info = null,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Transfer (if needed)
        if (journey.isDirect == 0 && journey.route2Id != null) {
            JourneyStep(
                type = JourneyStepType.CHANGE,
                icon = Icons.Filled.SwapHoriz,
                title = "Cambia",
                subtitle = "${journey.changeWalkingDist}m a piedi",
                info = null,
                color = Color(0xFFFFA500)
            )
            
            JourneyStep(
                type = JourneyStepType.ROUTE,
                icon = Icons.Filled.DirectionsBus,
                title = "Prendi ${journey.route2Id}",
                subtitle = buildString {
                    val stopsText = "per ${journey.totalStops / 2} fermate"
                    if (!journey.route2Headsign.isNullOrEmpty()) {
                        append("verso ${journey.route2Headsign} • $stopsText")
                    } else {
                        append(stopsText)
                    }
                },
                info = null,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Arrival
        JourneyStep(
            type = JourneyStepType.END,
            icon = Icons.Filled.FlagCircle,
            title = "Arrivo",
            subtitle = to,
            info = "${journey.endWalkingDist}m a piedi",
            color = Color(0xFFF44336)
        )
    }
}

enum class JourneyStepType {
    START, ROUTE, CHANGE, END
}

@Composable
private fun JourneyStep(
    type: JourneyStepType,
    icon: ImageVector,
    title: String,
    subtitle: String,
    info: String?,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeline
        Column(
            modifier = Modifier.width(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = color.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = color
                )
            }
            
            if (type != JourneyStepType.END) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(color.copy(alpha = 0.3f))
                )
            }
        }
        
        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            info?.let {
                Text(
                    text = it,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun ExpandCollapseButton(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isExpanded) "Mostra meno" else "Mostra dettagli",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
        )
        
        Icon(
            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(12.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ExpandedDetailsSection(
    journey: JourneyOption,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DetailRow(label = "Tempo di viaggio", value = "${journey.estimatedTravelMinutes} min")
        DetailRow(label = "Tempo a piedi", value = "${journey.walkingTimeMinutes} min")
        DetailRow(label = "Distanza totale a piedi", value = "${journey.totalWalkingDistance}m")
        
        if (journey.isDirect == 0) {
            DetailRow(label = "Cambio a piedi", value = "${journey.changeWalkingDist}m")
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatTime(seconds: Int): String {
    val s = maxOf(0, seconds) % (24 * 3600)
    val h = s / 3600
    val m = (s % 3600) / 60
    return String.format("%02d:%02d", h, m)
}