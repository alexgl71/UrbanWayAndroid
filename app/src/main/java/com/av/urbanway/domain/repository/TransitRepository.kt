package com.av.urbanway.domain.repository

import com.av.urbanway.data.api.APIResult
import com.av.urbanway.data.models.*
import kotlinx.coroutines.flow.Flow

interface TransitRepository {
    
    fun getNearbyDepartures(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 500,
        lookAheadMinutes: Int = 60
    ): Flow<APIResult<List<NearbyDeparturesResponse>>>
    
    fun getBestJourneys(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Flow<APIResult<JourneyResponse>>
    
    fun getRoutesSummary(
        routeList: String,
        importance: String = "alta,media"
    ): Flow<APIResult<List<RoutesSummaryResponse>>>
    
    fun getTripDetails(tripId: String): Flow<APIResult<TripDetailsResponse>>

    suspend fun getStopsSync(): APIResult<List<StopChangePayload>>
    
    suspend fun refreshNearbyDepartures(
        latitude: Double,
        longitude: Double
    ): Result<List<NearbyDeparturesResponse>>

    fun getDestinationsFromRoutes(routeIds: List<String>): Flow<APIResult<RoutesSummaryResponse?>>

    suspend fun validateTrips(
        t0: Int = 28800,
        r1: String,
        o1: String,
        d1: String,
        r2: String? = null,
        o2: String? = null,
        d2: String? = null
    ): APIResult<ValidatedTripsResponse>

    suspend fun getConsecutiveTrips(
        t0: Int,
        transferBufferSeconds: Int,
        aRouteId: String,
        aHeadsign: String,
        aStartStopId: String,
        aEndStopId: String,
        bRouteId: String,
        bHeadsign: String,
        bStartStopId: String,
        bEndStopId: String
    ): APIResult<List<ConsecutiveTripsRow>>
}
