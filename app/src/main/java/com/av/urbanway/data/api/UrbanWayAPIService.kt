package com.av.urbanway.data.api

import com.av.urbanway.data.models.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UrbanWayAPIService {
    // Full stops sync (delta list) used to build on-device catalog (parity with iOS)
    @GET("api/stops/sync")
    suspend fun getStopsSync(): List<StopChangePayload>
    
    @GET("api/departures/nearby")
    suspend fun getNearbyDepartures(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius_meters") radiusMeters: Int = 500,
        @Query("look_ahead_minutes") lookAheadMinutes: Int = 60
    ): List<NearbyDeparturesResponse>
    
    @GET("api/journeys/best")
    suspend fun getBestJourneys(
        @Query("StartLat") startLat: Double,
        @Query("StartLon") startLon: Double,
        @Query("EndLat") endLat: Double,
        @Query("EndLon") endLon: Double
    ): JourneyResponse
    
    @GET("api/routes/summary")
    suspend fun getRoutesSummary(
        @Query("route_list") routeList: String,
        @Query("importance") importance: String = "alta,media"
    ): List<RoutesSummaryResponse>
    
    @GET("api/getTripDetails")
    suspend fun getTripDetails(
        @Query("trip_id") tripId: String
    ): TripDetailsResponse
    @GET("api/journey/validate-trips")
    suspend fun validateTrips(
        @Query("t0") t0: Int = 28800,
        @Query("r1") r1: String,
        @Query("o1") o1: String,
        @Query("d1") d1: String,
        @Query("r2") r2: String? = null,
        @Query("o2") o2: String? = null,
        @Query("d2") d2: String? = null
    ): ValidatedTripsResponse

    @GET("api/trips/consecutive")
    suspend fun getConsecutiveTrips(
        @Query("t0") t0: Int,
        @Query("transfer_buffer_seconds") transferBufferSeconds: Int,
        @Query("A_route_id") aRouteId: String,
        @Query("A_headsign") aHeadsign: String,
        @Query("A_start_stop_id") aStartStopId: String,
        @Query("A_end_stop_id") aEndStopId: String,
        @Query("B_route_id") bRouteId: String,
        @Query("B_headsign") bHeadsign: String,
        @Query("B_start_stop_id") bStartStopId: String,
        @Query("B_end_stop_id") bEndStopId: String
    ): List<ConsecutiveTripsRow>
}

sealed class APIResult<out T> {
    data class Success<T>(val data: T) : APIResult<T>()
    data class Error(val exception: Exception) : APIResult<Nothing>()
}

class APIException(
    val code: Int,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
