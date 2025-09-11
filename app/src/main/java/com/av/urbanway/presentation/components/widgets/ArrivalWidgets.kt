package com.av.urbanway.presentation.components.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Tram
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.av.urbanway.data.models.StopInfo
import com.av.urbanway.data.models.WaitingTime
import com.av.urbanway.data.models.TransportType

// 1. Live Pill Widget
@Composable
fun LivePill(
    modifier: Modifier = Modifier
) {
    Text(
        text = "LIVE",
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF34C759))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Preview
@Composable
private fun LivePillPreview() {
    LivePill()
}

// 2. Card Header Widget
@Composable
fun CardHeader(
    title: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = leadingIcon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        LivePill()
    }
}

@Preview
@Composable
private fun CardHeaderPreview() {
    CardHeader(
        title = "ARRIVI IN EVIDENZA",
        leadingIcon = Icons.Filled.PushPin
    )
}

// 3. Route Badge Widget
@Composable
fun RouteBadge(
    route: String,
    modifier: Modifier = Modifier
) {
    val transportIcon = getTransportIcon(route)
    val displayedRoute = cleanRoute(route)
    
    Box(
        modifier = modifier
            .width(60.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0B3D91).copy(alpha = 0.76f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(2.dp))
            Icon(
                imageVector = transportIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = displayedRoute,
                color = Color.White,
                fontWeight = FontWeight.W300,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 20.sp,
                    letterSpacing = (-0.1).sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
        }
    }
}

@Preview
@Composable
private fun RouteBadgePreview() {
    Row {
        RouteBadge(route = "9")
        Spacer(Modifier.width(8.dp))
        RouteBadge(route = "M1")
        Spacer(Modifier.width(8.dp))
        RouteBadge(route = "68")
    }
}

// 4. Arrival Time Chip Widget
@Composable
fun ArrivalTimeChip(
    minutes: Int,
    isRealTime: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = if (minutes <= 0) "Ora" else "${minutes}'",
            color = if (isRealTime) Color(0xFF34C759) else Color.Gray,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            fontWeight = if (isRealTime) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(Modifier.width(2.dp))
        Icon(
            imageVector = if (isRealTime) Icons.Filled.Sync else Icons.Filled.AccessTime,
            contentDescription = null,
            tint = if (isRealTime) Color(0xFF34C759) else Color.Gray,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview
@Composable
private fun ArrivalTimeChipPreview() {
    Row {
        ArrivalTimeChip(minutes = 0, isRealTime = true)
        Spacer(Modifier.width(16.dp))
        ArrivalTimeChip(minutes = 11, isRealTime = true)
        Spacer(Modifier.width(16.dp))
        ArrivalTimeChip(minutes = 30, isRealTime = false)
    }
}

// 5. Arrival Times Row Widget
@Composable
fun ArrivalTimesRow(
    waitingTimes: List<WaitingTime>,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        val shown = waitingTimes.take(3)
        val remaining = (waitingTimes.size - shown.size).coerceAtLeast(0)
        
        shown.forEachIndexed { i, waitingTime ->
            if (i > 0) Spacer(Modifier.width(16.dp))
            ArrivalTimeChip(
                minutes = waitingTime.minutes,
                isRealTime = waitingTime.isRealTime
            )
        }
        
        if (remaining > 0) {
            Spacer(Modifier.width(16.dp))
            Text(
                text = "+${remaining}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview
@Composable
private fun ArrivalTimesRowPreview() {
    val sampleTimes = listOf(
        WaitingTime("9", 0, "Corso Francia", TransportType.TRAM, true, "1", "trip_9_1"),
        WaitingTime("9", 11, "Corso Francia", TransportType.TRAM, true, "1", "trip_9_2"),
        WaitingTime("9", 30, "Corso Francia", TransportType.TRAM, false, "1", "trip_9_3"),
        WaitingTime("9", 45, "Corso Francia", TransportType.TRAM, false, "1", "trip_9_4")
    )
    ArrivalTimesRow(waitingTimes = sampleTimes)
}

// 6. Stop Info Row Widget
@Composable
fun StopInfoRow(
    stopName: String,
    distance: Int?,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stopName,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
        )
        Spacer(Modifier.weight(1f))
        
        distance?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = "${it}mt",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview
@Composable
private fun StopInfoRowPreview() {
    StopInfoRow(
        stopName = "Fermata 646 - ADRIANO",
        distance = 111
    )
}

// 7. Route Info Column Widget
@Composable
fun RouteInfoColumn(
    destination: String,
    stopName: String,
    distance: Int?,
    waitingTimes: List<WaitingTime>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Destination
        Text(
            text = destination,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                letterSpacing = (-0.3).sp
            ),
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF555555),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Stop info
        StopInfoRow(stopName = stopName, distance = distance)
        
        Spacer(Modifier.height(2.dp))
        
        // Arrival times
        ArrivalTimesRow(waitingTimes = waitingTimes)
        
        Spacer(Modifier.height(2.dp))
    }
}

@Preview
@Composable
private fun RouteInfoColumnPreview() {
    val sampleTimes = listOf(
        WaitingTime("9", 0, "Corso Francia", TransportType.TRAM, true, "1", "trip_9_1"),
        WaitingTime("9", 11, "Corso Francia", TransportType.TRAM, true, "1", "trip_9_2"),
        WaitingTime("9", 30, "Corso Francia", TransportType.TRAM, false, "1", "trip_9_3")
    )
    RouteInfoColumn(
        destination = "SAN SALVARIO, CORSO D'AZEGLIO (TO EXP.)",
        stopName = "Fermata 646 - ADRIANO",
        distance = 111,
        waitingTimes = sampleTimes
    )
}

// 8. Swipe Action Background Widget
@Composable
fun SwipeActionBackground(
    isActive: Boolean,
    isPinned: Boolean,
    modifier: Modifier = Modifier
) {
    if (isActive) {
        val actionColor = if (isPinned) Color(0xFFE53935) else Color(0xFF43A047)
        val icon = if (isPinned) Icons.Filled.RemoveCircle else Icons.Filled.PushPin
        val alpha by animateFloatAsState(
            targetValue = if (isActive) 1f else 0f, label = "swipeAlpha"
        )
        val scale by animateFloatAsState(
            targetValue = if (isActive) 1f else 0.9f, label = "swipeScale"
        )
        
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(actionColor.copy(alpha = 0.10f))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(actionColor.copy(alpha = 0.22f))
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = actionColor.copy(alpha = alpha))
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize())
    }
}

@Preview
@Composable
private fun SwipeActionBackgroundPreview() {
    Row {
        Box(modifier = Modifier.size(100.dp, 60.dp)) {
            SwipeActionBackground(isActive = true, isPinned = false)
        }
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.size(100.dp, 60.dp)) {
            SwipeActionBackground(isActive = true, isPinned = true)
        }
    }
}

// Helper functions (from original file)
private fun getTransportIcon(route: String): ImageVector {
    val cleanedRoute = cleanRoute(route)
    
    if (cleanedRoute == "M1S" || cleanedRoute == "M1" || cleanedRoute.startsWith("METRO")) {
        return Icons.Default.Subway
    }
    
    if (cleanedRoute in listOf("3", "4", "9", "10", "13", "15", "16CS", "16CD")) {
        return Icons.Default.Tram
    }
    
    return Icons.Filled.DirectionsBus
}

private fun cleanRoute(route: String): String {
    val upper = route.uppercase()
    if (upper.startsWith("METRO")) return "M1"
    return if (route.isNotEmpty() && route.last().isLetter()) route.dropLast(1).uppercase() else upper
}