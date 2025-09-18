package com.av.urbanway.presentation.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.data.models.JourneyOption
import com.av.urbanway.presentation.viewmodels.MainViewModel

@Composable
fun InlineJourneyResultsCard(
    viewModel: MainViewModel,
    journeys: List<JourneyOption>,
    isLoading: Boolean,
    fromAddress: String,
    toAddress: String,
    onClose: () -> Unit,
    isPreview: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                spotColor = Color.Black.copy(alpha = 0.08f),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Route,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Percorsi disponibili",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (!isPreview) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                // From / To section
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DA:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(34.dp)
                        )
                        Text(
                            text = fromAddress,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "A:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(34.dp)
                        )
                        Text(
                            text = toAddress,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }

            when {
                isLoading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Ricerca in corso...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                journeys.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Nessun percorso trovato",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Modifica partenza o destinazione e riprova",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                else -> {
                    if (isPreview) {
                        // Ultra-compact list: show only route ids and total minutes
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            journeys.forEach { journey ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val title = if (journey.isDirect == 1) {
                                        "Linea ${journey.route1Id}"
                                    } else {
                                        val r1 = journey.route1Id
                                        val r2 = journey.route2Id ?: "?"
                                        "Linee $r1 + $r2"
                                    }
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${journey.totalJourneyMinutes} min",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                    // Sort journeys: direct first, then transfers
                    val directJourneys = journeys.filter { it.isDirect == 1 }
                        .sortedBy { it.totalJourneyMinutes }
                    
                    val directRouteIds = directJourneys.map { it.route1Id }.toSet()
                    val transferJourneys = journeys.filter { it.isDirect == 0 }
                        .filter { journey ->
                            !directRouteIds.contains(journey.route1Id) && 
                            (journey.route2Id == null || !directRouteIds.contains(journey.route2Id))
                        }
                        .sortedBy { it.totalJourneyMinutes }

                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (directJourneys.isNotEmpty()) {
                            SectionLabel(text = "Diretto", color = Color.Green)
                            directJourneys.forEachIndexed { index, journey ->
                                InlineJourneyRow(
                                    journey = journey,
                                    modifier = Modifier
                                        .clickable { viewModel.showFixedJourneyOverlay(journey) }
                                        .padding(horizontal = 12.dp)
                                )
                                if (index < directJourneys.size - 1) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                                }
                            }
                        }
                        
                        if (transferJourneys.isNotEmpty()) {
                            SectionLabel(text = "Cambio", color = Color(0xFFFFA500)) // Orange
                            transferJourneys.forEachIndexed { index, journey ->
                                InlineJourneyRow(
                                    journey = journey,
                                    modifier = Modifier
                                        .clickable { viewModel.showFixedJourneyOverlay(journey) }
                                        .padding(horizontal = 12.dp)
                                )
                                if (index < transferJourneys.size - 1) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                                }
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
private fun SectionLabel(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 12.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier
                .background(
                    color = color.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 2.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun InlineJourneyRow(
    journey: JourneyOption,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Route badges
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RouteBadge(route = journey.route1Id)
            journey.route2Id?.let { route2 ->
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RouteBadge(route = route2)
            }
        }

        // Center: journey details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Headsigns
            if (!journey.route1Headsign.isNullOrEmpty() || !journey.route2Headsign.isNullOrEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Signpost,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    journey.route1Headsign?.takeIf { it.isNotEmpty() }?.let { headsign ->
                        Text(
                            text = headsign,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (journey.isDirect == 0) {
                        journey.route2Headsign?.takeIf { it.isNotEmpty() }?.let { headsign ->
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = headsign,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Times
            if (journey.depTime != null && journey.arrTime != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Stops
            Text(
                text = "${journey.totalStops} fermate",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Walking time
            if (journey.walkingTimeMinutes > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsWalk,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${journey.walkingTimeMinutes}' a piedi",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Total minutes
        val brandColor = Color(0xFFD9731F) // UrbanWay orange
        Text(
            text = "${journey.totalJourneyMinutes}'",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .background(
                    color = brandColor.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun RouteBadge(
    route: String,
    modifier: Modifier = Modifier
) {
    val color = getRouteColor(route)
    val displayText = getRouteDisplay(route)
    
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f))
            .background(
                color = Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color.Transparent,
                    shape = CircleShape
                )
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

private fun getRouteColor(route: String): Color {
    return if (route.uppercase().contains("METRO")) {
        Color(0xFFFFA500) // Orange
    } else {
        Color(0xFF0B3D91) // Blue
    }
}

private fun getRouteDisplay(route: String): String {
    return if (route.uppercase().endsWith("U")) {
        route.dropLast(1)
    } else {
        route
    }
}

private fun formatTime(seconds: Int): String {
    val s = maxOf(0, seconds) % (24 * 3600)
    val h = s / 3600
    val m = (s % 3600) / 60
    return String.format("%02d:%02d", h, m)
}
