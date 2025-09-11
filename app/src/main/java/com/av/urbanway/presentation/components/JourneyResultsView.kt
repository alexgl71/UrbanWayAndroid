package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.av.urbanway.data.models.Coordinates
import com.av.urbanway.data.models.JourneyOption

data class JourneyResultsData(
    val fromAddress: String,
    val toAddress: String,
    val fromCoordinates: Coordinates,
    val toCoordinates: Coordinates,
    val journeys: List<JourneyOption>
)

@Composable
fun JourneyResultsView(
    journeyData: JourneyResultsData,
    isLoading: Boolean,
    onJourneySelect: (JourneyOption) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Debug logging
    LaunchedEffect(Unit) {
        android.util.Log.d("TRANSIT", "📊 JourneyResults UI State:")
        android.util.Log.d("TRANSIT", "📊 IsLoading: $isLoading")
        android.util.Log.d("TRANSIT", "📊 Journeys count: ${journeyData.journeys.size}")
        if (journeyData.journeys.isNotEmpty()) {
            val first = journeyData.journeys.first()
            android.util.Log.d("TRANSIT", "📊 First journey: ${first.route1Id} -> ${first.route2Id}")
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Section
            HeaderSection(
                journeyData = journeyData,
                onBack = onBack
            )
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Content
            when {
                isLoading -> LoadingView()
                journeyData.journeys.isEmpty() -> NoResultsView()
                else -> JourneyResultsList(
                    journeys = journeyData.journeys,
                    onJourneySelect = onJourneySelect
                )
            }
        }
    }
}

@Composable
private fun HeaderSection(
    journeyData: JourneyResultsData,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Percorsi Disponibili",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = journeyData.fromAddress,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.FlagCircle,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = journeyData.toAddress,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
        
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Ricerca percorsi in corso...",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NoResultsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.DirectionsBus,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Nessun percorso trovato",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Prova a modificare i punti di partenza o arrivo",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun JourneyResultsList(
    journeys: List<JourneyOption>,
    onJourneySelect: (JourneyOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedJourneys = groupAndOptimizeJourneys(journeys)
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Direct Journeys Section
        if (groupedJourneys.directJourneys.isNotEmpty()) {
            item {
                JourneySectionHeader(
                    title = "Percorsi Diretti",
                    subtitle = "${groupedJourneys.directJourneys.size} linea/e senza cambi",
                    icon = Icons.Filled.DirectionsBus
                )
            }
            
            itemsIndexed(groupedJourneys.directJourneys) { index, journey ->
                JourneyResultItem(
                    journey = journey,
                    isRecommended = index == 0 && groupedJourneys.transferJourneys.isEmpty(),
                    onClick = { onJourneySelect(journey) }
                )
            }
            
            if (groupedJourneys.transferJourneys.isNotEmpty()) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
        
        // Transfer Journeys Section
        if (groupedJourneys.transferJourneys.isNotEmpty()) {
            item {
                JourneySectionHeader(
                    title = "Percorsi con Cambio",
                    subtitle = "${groupedJourneys.transferJourneys.size} combinazione/i di linee",
                    icon = Icons.Filled.SwapHoriz
                )
            }
            
            itemsIndexed(groupedJourneys.transferJourneys) { index, journey ->
                JourneyResultItem(
                    journey = journey,
                    isRecommended = index == 0 && groupedJourneys.directJourneys.isEmpty(),
                    onClick = { onJourneySelect(journey) }
                )
            }
        }
    }
}

@Composable
private fun JourneySectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun JourneyResultItem(
    journey: JourneyOption,
    isRecommended: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isRecommended) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isRecommended) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                    Text(
                        text = "CONSIGLIATO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
                .let { content ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        content
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (journey.isDirect == 1) {
                        RouteChip(routeId = journey.route1Id)
                        Text(
                            text = "DIRETTO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        RouteChip(routeId = journey.route1Id)
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        journey.route2Id?.let { route2Id ->
                            RouteChip(routeId = route2Id)
                        }
                    }
                }
                
                Text(
                    text = "${journey.totalJourneyMinutes} min",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Headsigns line
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
                                modifier = Modifier.size(12.dp),
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
            
            // Time line
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
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsWalk,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${journey.totalWalkingDistance}m",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "(${journey.walkingTimeMinutes} min)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${journey.totalStops} fermate",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteChip(
    routeId: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        routeId.uppercase().contains("METRO") -> Color(0xFFF44336) // Red
        routeId.endsWith("U") -> Color(0xFF2196F3) // Blue
        else -> Color(0xFF2196F3) // Blue
    }
    
    Text(
        text = routeId,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

// Journey Grouping Logic
data class GroupedJourneyResults(
    val directJourneys: List<JourneyOption>,
    val transferJourneys: List<JourneyOption>
)

private fun groupAndOptimizeJourneys(journeys: List<JourneyOption>): GroupedJourneyResults {
    android.util.Log.d("TRANSIT", "🔍 Smart grouping ${journeys.size} journeys into direct vs transfer")
    
    val directJourneysMap = journeys
        .filter { it.isDirect == 1 }
        .groupBy { it.route1Id }
    
    val directJourneys = directJourneysMap.mapNotNull { (routeId, groupedJourneys) ->
        val best = groupedJourneys.minByOrNull { it.totalJourneyMinutes }
        if (best != null) {
            android.util.Log.d("TRANSIT", "🔍 Direct route $routeId: ${best.totalJourneyMinutes}min (from ${groupedJourneys.size} options)")
        }
        best
    }.sortedBy { it.totalJourneyMinutes }
    
    val transferJourneys = journeys.filter { it.isDirect == 0 }
    
    val transferJourneysMap = transferJourneys.groupBy { journey ->
        val routes = listOfNotNull(journey.route1Id, journey.route2Id).sorted()
        routes.joinToString(" + ")
    }
    
    val optimizedTransferJourneys = transferJourneysMap.mapNotNull { (combination, groupedJourneys) ->
        val best = groupedJourneys.minByOrNull { it.totalJourneyMinutes }
        if (best != null) {
            android.util.Log.d("TRANSIT", "🔍 Transfer combination $combination: ${best.totalJourneyMinutes}min (from ${groupedJourneys.size} options)")
        }
        best
    }.sortedBy { it.totalJourneyMinutes }
    
    android.util.Log.d("TRANSIT", "🎯 Final results: ${directJourneys.size} direct + ${optimizedTransferJourneys.size} transfer = ${directJourneys.size + optimizedTransferJourneys.size} total")
    
    return GroupedJourneyResults(
        directJourneys = directJourneys,
        transferJourneys = optimizedTransferJourneys
    )
}

private fun formatTime(seconds: Int): String {
    val s = maxOf(0, seconds) % (24 * 3600)
    val h = s / 3600
    val m = (s % 3600) / 60
    return String.format("%02d:%02d", h, m)
}