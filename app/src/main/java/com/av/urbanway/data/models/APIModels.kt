package com.av.urbanway.data.models

import com.google.gson.annotations.SerializedName

// API Request Models

data class NearbyDeparturesRequest(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("radius_meters")
    val radiusMeters: Int = 300,
    @SerializedName("look_ahead_minutes")
    val lookAheadMinutes: Int = 60
)

data class JourneyRequest(
    @SerializedName("StartLat")
    val startLat: Double,
    @SerializedName("StartLon")
    val startLon: Double,
    @SerializedName("EndLat")
    val endLat: Double,
    @SerializedName("EndLon")
    val endLon: Double
)

data class RoutesSummaryRequest(
    @SerializedName("route_list")
    val routeList: String,
    val importance: Int = 3
)

data class TripDetailsRequest(
    @SerializedName("trip_id")
    val tripId: String
)


// API Response Models

data class RoutesSummaryResponse(
    @SerializedName("zoneList")
    val zoneList: List<String>,
    @SerializedName("routeList")
    val routeList: List<String>,
    @SerializedName("allDestinations")
    val allDestinations: List<String>,
    @SerializedName("destinationsByType")
    val destinationsByType: Map<String, List<String>>,
    val museums: List<String>,
    val hospitals: List<String>,
    val universities: List<String>,
    val shopping: List<String>
)

data class NearbyDeparturesResponse(
    @SerializedName("route_id")
    val routeId: String,
    val headsigns: List<Headsign>
)

data class Headsign(
    @SerializedName("trip_headsign")
    val tripHeadsign: String,
    @SerializedName("stop_id")
    val stopId: String,
    @SerializedName("stop_name")
    val stopName: String,
    @SerializedName("distance_to_stop")
    val distanceToStop: Int,
    @SerializedName("stop_lat")
    val stopLat: Double,
    @SerializedName("stop_lon")
    val stopLon: Double,
    val departures: List<Departure>
)

data class JourneyResponse(
    val journeys: List<JourneyOption>,
    val status: String?,
    val message: String?
)

data class TripDetailsResponse(
    // Some backends may omit fields; keep nullable to avoid runtime NPEs
    val stops: List<TripStop>?,
    val shapes: List<TripShape>?
)

data class TripStop(
    @SerializedName("stop_id") val stopId: String,
    @SerializedName("stop_code") val stopCode: String,
    @SerializedName("stop_name") val stopName: String,
    @SerializedName("arrival_date") val arrivalDate: Int,
    @SerializedName("stop_lat") val stopLat: Double,
    @SerializedName("stop_lon") val stopLon: Double
)

data class TripShape(
    @SerializedName("shape_pt_lat") val shapePtLat: Double,
    @SerializedName("shape_pt_lon") val shapePtLon: Double
)

// Extra API models to match iOS repository helpers
data class ValidatedTripsResponse(
    @SerializedName("seg1_TripID") val seg1TripID: String?,
    @SerializedName("seg1_OriginTime") val seg1OriginTime: Int?,
    @SerializedName("seg1_DestTime") val seg1DestTime: Int?,
    @SerializedName("seg2_TripID") val seg2TripID: String?,
    @SerializedName("seg2_OriginTime") val seg2OriginTime: Int?,
    @SerializedName("seg2_DestTime") val seg2DestTime: Int?
)

data class ConsecutiveTripsRow(
    @SerializedName("a_trip_id") val aTripId: String,
    @SerializedName("a_start_time") val aStartTime: Int,
    @SerializedName("a_end_time") val aEndTime: Int,
    @SerializedName("b_trip_id") val bTripId: String,
    @SerializedName("b_start_time") val bStartTime: Int,
    @SerializedName("b_end_time") val bEndTime: Int
)
