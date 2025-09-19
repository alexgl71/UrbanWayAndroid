package com.av.urbanway.presentation.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.data.models.StopInfo
import com.av.urbanway.data.models.WaitingTime
import com.av.urbanway.data.models.PinnedArrival
import com.av.urbanway.presentation.components.widgets.ArrivalRowContent

@Composable
fun PinnedCardView(
    pinnedArrivals: List<PinnedArrival>,
    waitingTimes: List<WaitingTime>,
    nearbyStops: List<StopInfo>,
    onPin: (routeId: String, destination: String, stopId: String, stopName: String) -> Unit,
    onUnpin: (routeId: String, destination: String, stopId: String) -> Unit,
    onRouteSelect: ((routeId: String, destination: String, stopId: String, stopName: String, arrivalTimes: List<WaitingTime>, distance: Int?) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Show ALL pinned arrivals, regardless of whether they have current arrival times
    // This ensures PinnedCardView shows exactly the same data as ChatView "Linee in evidenza"

    if (pinnedArrivals.isNotEmpty()) {
        // Clean content without container styling
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            // Custom header with dismiss button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FA))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.PushPin,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "NEW ARRIVI IN EVIDENZA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Chiudi",
                        tint = Color.Black
                    )
                }
            }

            Column(Modifier.padding(16.dp)) {
                pinnedArrivals.forEach { pinnedArrival ->
                    PinnedArrivalItem(
                        pinnedArrival = pinnedArrival,
                        nearbyStops = nearbyStops,
                        waitingTimes = waitingTimes,
                        onRouteSelect = onRouteSelect
                    )
                }
            }
        }
    }
}

@Composable
private fun PinnedArrivalItem(
    pinnedArrival: PinnedArrival,
    nearbyStops: List<StopInfo>,
    waitingTimes: List<WaitingTime>,
    onRouteSelect: ((routeId: String, destination: String, stopId: String, stopName: String, arrivalTimes: List<WaitingTime>, distance: Int?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onRouteSelect?.let { callback ->
                    // Find matching waiting times for this pinned arrival
                    val matchingTimes = waitingTimes.filter {
                        it.route == pinnedArrival.routeId &&
                        it.destination == pinnedArrival.destination &&
                        it.stopId == pinnedArrival.stopId
                    }
                    val distance = nearbyStops.firstOrNull { it.stopId == pinnedArrival.stopId }?.distanceToStop
                    callback(
                        pinnedArrival.routeId,
                        pinnedArrival.destination,
                        pinnedArrival.stopId,
                        pinnedArrival.stopName,
                        matchingTimes,
                        distance
                    )
                }
            }
            .background(Color(0xFFF8F9FA))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Small route badge (like in ChatView)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF0B3D91).copy(alpha = 0.76f)),
            contentAlignment = Alignment.Center
        ) {
            val cleanRouteId = if (pinnedArrival.routeId.endsWith("U")) pinnedArrival.routeId.dropLast(1) else pinnedArrival.routeId
            Text(
                text = cleanRouteId,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Destination text (like in ChatView)
        Text(
            text = pinnedArrival.destination,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}