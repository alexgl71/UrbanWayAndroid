package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import com.av.urbanway.presentation.components.widgets.LivePill
import kotlinx.coroutines.delay

data class RouteStopInfo(
    val stopId: String,
    val stopName: String,
    val stopCode: String?,
    val arrivalTimeSeconds: Int?, // seconds for arrival time
    val isSelected: Boolean = false
)

@Composable
fun RouteDetailCard(
    routeId: String,
    destination: String,
    stops: List<RouteStopInfo>,
    isLoading: Boolean,
    selectedStopId: String? = null,
    onBack: () -> Unit,
    onShowAllStops: () -> Unit,
    useFixedHeight: Boolean = true,
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
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(if (useFixedHeight) Modifier else Modifier.fillMaxHeight())
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            RouteDetailHeader(
                routeId = routeId,
                destination = destination
            )
            
            // Stops Section
            RouteStopsSection(
                stops = stops,
                isLoading = isLoading,
                listState = listState,
                modifier = Modifier.weight(1f, fill = !useFixedHeight)
            )
        }
    }
}

@Composable
private fun RouteDetailHeader(
    routeId: String,
    destination: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = routeId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                LivePill()
            }
            Text(
                text = destination,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RouteStopsSection(
    stops: List<RouteStopInfo>,
    isLoading: Boolean,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Fermate del percorso",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
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
            .padding(horizontal = 0.dp, vertical = 10.dp),
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
                            .width(2.dp)
                            .height(10.dp)
                            .background(verticalLineColor)
                    )
                }
                
                // Circle/Dot
                Box(
                    modifier = Modifier
                        .size(if (isTerminal) 12.dp else 10.dp)
                        .clip(CircleShape)
                        .background(if (isTerminal) navyBlue else Color.White)
                        .then(
                            if (!isTerminal) {
                                Modifier.padding(1.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(1.dp)
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
                            .width(2.dp)
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

private fun cleanStopName(name: String): String {
    // Remove "Fermata XXX - " prefix like in iOS
    return name.replaceFirst(Regex("^Fermata \\d+ - "), "")
}

private fun formatArrivalTime(seconds: Int): String {
    val minutes = seconds / 60
    val hours = minutes / 60
    val mins = minutes % 60
    return String.format("%02d:%02d", hours, mins)
}

@Preview
@Composable
private fun RouteDetailCardPreview() {
    val sampleStops = listOf(
        RouteStopInfo("1", "Fermata 646 - ADRIANO", "646", 3600, false),
        RouteStopInfo("2", "Fermata 595 - VIA ROMA", "595", 3720, true),
        RouteStopInfo("3", "Fermata 201 - PIAZZA CASTELLO", "201", 3840, false),
        RouteStopInfo("4", "Fermata 102 - PORTA NUOVA", "102", 3960, false)
    )
    
    RouteDetailCard(
        routeId = "15U",
        destination = "Centro",
        stops = sampleStops,
        isLoading = false,
        selectedStopId = "2",
        onBack = {},
        onShowAllStops = {}
    )
}

@Preview
@Composable
private fun RouteDetailCardLoadingPreview() {
    RouteDetailCard(
        routeId = "9",
        destination = "SAN SALVARIO, CORSO D'AZEGLIO",
        stops = emptyList(),
        isLoading = true,
        onBack = {},
        onShowAllStops = {}
    )
}