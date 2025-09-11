package com.av.urbanway.data.models

import com.google.gson.annotations.SerializedName
import com.google.gson.*
import java.lang.reflect.Type

data class WaitingTime(
    val route: String,
    val minutes: Int,
    val destination: String,
    val type: TransportType,
    @SerializedName("isRealTime")
    val isRealTime: Boolean,
    @SerializedName("stopId")
    val stopId: String,
    @SerializedName("tripId")
    val tripId: String? = null,
    val id: String = "${route}_${destination}_${stopId}"
)

data class Coordinates(
    val lat: Double,
    val lng: Double
)

data class Location(
    val address: String,
    val coordinates: Coordinates,
    val isManual: Boolean = false
)

enum class TransportType {
    BUS,
    METRO,
    TRAM;
    
    companion object {
        fun fromString(type: String): TransportType {
            return when (type.uppercase()) {
                "BUS" -> BUS
                "METRO" -> METRO
                "TRAM" -> TRAM
                else -> BUS
            }
        }
    }
}

data class JourneyOption(
    @SerializedName("is_direct") val isDirect: Int,
    @SerializedName("start_stop_id") val startStopId: Int,
    @SerializedName("end_stop_id") val endStopId: Int,
    @SerializedName("route1_id") val route1Id: String,
    @SerializedName("route2_id") val route2Id: String?,
    // extras (optional)
    @SerializedName("t1") val trip1Id: String? = null,
    @SerializedName("t2") val trip2Id: String? = null,
    @SerializedName("dep") val depTime: Int? = null,
    @SerializedName("arr") val arrTime: Int? = null,
    // leg sequences (optional)
    @SerializedName("leg1_board_stop_sequence") val leg1BoardStopSequence: Int? = null,
    @SerializedName("leg1_alight_stop_sequence") val leg1AlightStopSequence: Int? = null,
    @SerializedName("leg2_board_stop_sequence") val leg2BoardStopSequence: Int? = null,
    @SerializedName("leg2_alight_stop_sequence") val leg2AlightStopSequence: Int? = null,
    // totals
    @SerializedName("total_stops") val totalStops: Int,
    @SerializedName("total_journey_minutes") val totalJourneyMinutes: Int,
    @SerializedName("route_ranking") val routeRanking: Int? = null,
    @SerializedName("start_walking_dist") val startWalkingDist: Int,
    @SerializedName("end_walking_dist") val endWalkingDist: Int,
    @SerializedName("change_walking_dist") val changeWalkingDist: Int = 0,
    @SerializedName("total_walking_distance") val totalWalkingDistance: Int,
    @SerializedName("estimated_travel_minutes") val estimatedTravelMinutes: Int,
    @SerializedName("walking_time_minutes") val walkingTimeMinutes: Int,
    // segment-specific (optional)
    @SerializedName("route1_start_stop_id") val route1StartStopId: Int? = null,
    @SerializedName("route1_end_stop_id") val route1EndStopId: Int? = null,
    @SerializedName("route2_start_stop_id") val route2StartStopId: Int? = null,
    @SerializedName("route2_end_stop_id") val route2EndStopId: Int? = null,
    @SerializedName("route1_headsign") val route1Headsign: String? = null,
    @SerializedName("route2_headsign") val route2Headsign: String? = null
)

data class Departure(
    @SerializedName("trip_id") val tripId: String,
    @SerializedName("actual_departure_time") val actualDepartureTime: String,
    @SerializedName("wait_minutes") val waitMinutes: Int,
    @SerializedName("has_realtime_update") val hasRealtimeUpdate: Boolean
)

data class Stop(
    @SerializedName("stop_id")
    val stopId: String,
    @SerializedName("stop_name")
    val stopName: String,
    @SerializedName("stop_lat")
    val stopLat: Double,
    @SerializedName("stop_lon")
    val stopLon: Double
)

// Additional models matching iOS
enum class VehicleType {
    BUS,
    METRO,
    TRAM
}

data class StopInfo(
    val stopId: String,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double,
    val distanceToStop: Int,
    val routes: List<String>
)

// Custom deserializer to support both legacy and new journey response shapes
class JourneyOptionDeserializer : JsonDeserializer<JourneyOption> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): JourneyOption {
        val obj = json.asJsonObject

        fun asInt(key: String): Int? = obj.get(key)?.takeIf { !it.isJsonNull }?.asInt
        fun asString(key: String): String? = obj.get(key)?.takeIf { !it.isJsonNull }?.asString

        // Detect legacy by presence of camelCase keys like route1Id
        if (obj.has("route1Id") || obj.has("totalStops")) {
            val isDirect = obj.get("isDirect")?.asInt ?: 1
            val route1Id = obj.get("route1Id")?.asString ?: ""
            val route2Id = asString("route2Id")
            val totalStops = obj.get("totalStops")?.asInt ?: 0
            val totalJourneyMinutes = obj.get("totalJourneyMinutes")?.asInt ?: 0
            val walkingTimeMinutes = obj.get("walkingTimeMinutes")?.asInt ?: 0
            val route1StartStopId = asInt("route1StopId")
            val route2StartStopId = asInt("route2StopId")
            return JourneyOption(
                isDirect = isDirect,
                startStopId = route1StartStopId ?: 0,
                endStopId = route2StartStopId ?: route1StartStopId ?: 0,
                route1Id = route1Id,
                route2Id = route2Id,
                totalStops = totalStops,
                totalJourneyMinutes = totalJourneyMinutes,
                startWalkingDist = 0,
                endWalkingDist = 0,
                totalWalkingDistance = 0,
                estimatedTravelMinutes = totalJourneyMinutes - walkingTimeMinutes,
                walkingTimeMinutes = walkingTimeMinutes
            )
        }

        // New shape keys mapping
        val type = asString("type")?.lowercase() ?: "direct"
        val isDirect = if (type == "direct") 1 else 0
        val route1Id = asString("r1") ?: ""
        val route2Id = asString("r2")
        val startStop = asInt("start_stop") ?: 0
        val endStop = asInt("end_stop") ?: 0
        val totalStops = asInt("stops") ?: 0
        val totalMin = asInt("total_min") ?: 0
        val transitMin = asInt("transit_min") ?: (totalMin - (asInt("walk_min") ?: 0))
        val walkMin = asInt("walk_min") ?: 0

        fun meters(key: String): Int {
            val el = obj.get(key) ?: return 0
            return when {
                el.isJsonNull -> 0
                el.isJsonPrimitive && el.asJsonPrimitive.isNumber -> {
                    val num = el.asJsonPrimitive.asNumber
                    // can be double or int
                    kotlin.math.round(num.toDouble()).toInt()
                }
                else -> 0
            }
        }
        val startWalk = meters("start_walk")
        val endWalk = meters("end_walk")

        val t1 = asString("t1")
        val t2 = asString("t2")
        val dep = asInt("dep")
        val arr = asInt("arr")
        val h1 = asString("h1")
        val h2 = asString("h2")

        val l1Board = asInt("leg1_board_stop_id")
        val l1BoardSeq = asInt("leg1_board_stop_sequence")
        val l1Alight = asInt("leg1_alight_stop_id")
        val l1AlightSeq = asInt("leg1_alight_stop_sequence")
        val l2Board = asInt("leg2_board_stop_id")
        val l2BoardSeq = asInt("leg2_board_stop_sequence")
        val l2Alight = asInt("leg2_alight_stop_id")
        val l2AlightSeq = asInt("leg2_alight_stop_sequence")
        val transferStop = asInt("transfer_stop")

        val route1Start = l1Board ?: startStop
        val route1End = l1Alight ?: (if (isDirect == 1) endStop else transferStop)
        val route2Start = if (isDirect == 1) null else (l2Board ?: transferStop)
        val route2End = if (isDirect == 1) null else (l2Alight ?: endStop)

        return JourneyOption(
            isDirect = isDirect,
            startStopId = startStop,
            endStopId = endStop,
            route1Id = route1Id,
            route2Id = route2Id,
            trip1Id = t1,
            trip2Id = t2,
            depTime = dep,
            arrTime = arr,
            leg1BoardStopSequence = l1BoardSeq,
            leg1AlightStopSequence = l1AlightSeq,
            leg2BoardStopSequence = l2BoardSeq,
            leg2AlightStopSequence = l2AlightSeq,
            totalStops = totalStops,
            totalJourneyMinutes = totalMin,
            startWalkingDist = startWalk,
            endWalkingDist = endWalk,
            changeWalkingDist = 0,
            totalWalkingDistance = startWalk + endWalk,
            estimatedTravelMinutes = transitMin,
            walkingTimeMinutes = walkMin,
            route1StartStopId = route1Start,
            route1EndStopId = route1End,
            route2StartStopId = route2Start,
            route2EndStopId = route2End,
            route1Headsign = h1,
            route2Headsign = h2
        )
    }
}
