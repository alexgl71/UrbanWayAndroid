package com.av.urbanway.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.av.urbanway.data.models.StopInfo
import com.av.urbanway.data.models.WaitingTime
import com.av.urbanway.data.models.PinnedArrival
import com.av.urbanway.presentation.components.widgets.SingleArrivalsCard
import com.av.urbanway.presentation.components.widgets.ArrivalsEmptyCard
import android.util.Log

@Composable
fun ArrivalsCards(
    waitingTimes: List<WaitingTime>,
    nearbyStops: List<StopInfo>,
    pinnedArrivals: List<PinnedArrival>,
    onPin: (routeId: String, destination: String, stopId: String, stopName: String) -> Unit,
    onUnpin: (routeId: String, destination: String, stopId: String) -> Unit,
    onRouteSelect: ((routeId: String, destination: String, stopId: String, stopName: String, arrivalTimes: List<WaitingTime>, distance: Int?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Split times first (route+destination+stop specificity), then group each set separately.
    val pinnedTimes = waitingTimes.filter { wt ->
        pinnedArrivals.any { it.routeId == wt.route && it.destination == wt.destination && it.stopId == wt.stopId }
    }
    val otherTimes = waitingTimes.filter { wt ->
        pinnedArrivals.none { it.routeId == wt.route && it.destination == wt.destination && it.stopId == wt.stopId }
    }

    val pinnedGrouped = groupByRouteDestination(pinnedTimes)
    val pinnedReps = pinnedGrouped.mapNotNull { (key, list) ->
        val rep = repsForGroup(list, nearbyStops) ?: return@mapNotNull null
        key to rep
    }.sortedBy { it.second.minutes }

    val otherGrouped = groupByRouteDestination(otherTimes)
    val otherReps = otherGrouped.mapNotNull { (key, list) ->
        val rep = repsForGroup(list, nearbyStops) ?: return@mapNotNull null
        key to rep
    }.sortedBy { it.second.minutes }

    Log.d("TRANSITOAPP", "ArrivalsCards: pinnedTimes=${pinnedTimes.size}, otherTimes=${otherTimes.size}, pinnedReps=${pinnedReps.size}, otherReps=${otherReps.size}")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (pinnedReps.isNotEmpty()) {
            SingleArrivalsCard(
                title = "ARRIVI IN EVIDENZA",
                leadingIcon = Icons.Filled.PushPin,
                arrivalItems = pinnedReps.map { Triple(it.first, it.second, pinnedGrouped[it.first] ?: emptyList()) },
                nearbyStops = nearbyStops,
                isPinnedCard = true,
                onPin = onPin,
                onUnpin = onUnpin,
                onRouteSelect = onRouteSelect
            )
        }
        if (otherReps.isNotEmpty()) {
            SingleArrivalsCard(
                title = "LINEE IN ARRIVO",
                leadingIcon = Icons.Filled.AccessTime,
                arrivalItems = otherReps.map { Triple(it.first, it.second, otherGrouped[it.first] ?: emptyList()) },
                nearbyStops = nearbyStops,
                isPinnedCard = false,
                onPin = onPin,
                onUnpin = onUnpin,
                onRouteSelect = onRouteSelect
            )
        } else if (pinnedReps.isEmpty()) {
            ArrivalsEmptyCard()
        }
    }
}


// Group reps
private fun groupByRouteDestination(times: List<WaitingTime>): Map<String, List<WaitingTime>> =
    times.groupBy { it.route + "|" + it.destination }

private fun repsForGroup(group: List<WaitingTime>, stops: List<StopInfo>): WaitingTime? {
    return group.minWithOrNull(compareBy<WaitingTime> { distanceForStop(it.stopId, stops) }.thenBy { it.minutes })
}

private fun timesForSameStopFromGroup(rep: WaitingTime, group: List<WaitingTime>): List<WaitingTime> =
    group.filter { it.stopId == rep.stopId }
        .sortedBy { it.minutes }
        .ifEmpty { listOf(rep) }

private fun distanceForStop(stopId: String, stops: List<StopInfo>): Int {
    return stops.firstOrNull { it.stopId == stopId }?.distanceToStop ?: Int.MAX_VALUE
}
