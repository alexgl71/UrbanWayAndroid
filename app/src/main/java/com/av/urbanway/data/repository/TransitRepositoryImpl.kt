package com.av.urbanway.data.repository

import com.av.urbanway.data.api.APIResult
import com.av.urbanway.data.api.APIException
import com.av.urbanway.data.api.UrbanWayAPIService
import com.av.urbanway.data.models.*
import com.av.urbanway.domain.repository.TransitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TransitRepositoryImpl private constructor(
    private val apiService: UrbanWayAPIService
) : TransitRepository {
    companion object {
        @Volatile
        private var INSTANCE: TransitRepositoryImpl? = null
        
        fun getInstance(apiService: UrbanWayAPIService): TransitRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TransitRepositoryImpl(apiService).also { INSTANCE = it }
            }
        }
    }

    override fun getNearbyDepartures(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        lookAheadMinutes: Int
    ): Flow<APIResult<List<NearbyDeparturesResponse>>> = flow {
        try {
            val result = apiService.getNearbyDepartures(
                latitude = latitude,
                longitude = longitude,
                radiusMeters = radiusMeters,
                lookAheadMinutes = lookAheadMinutes
            )
            emit(APIResult.Success(result))
        } catch (e: Exception) {
            emit(APIResult.Error(e))
        }
    }

    override fun getBestJourneys(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Flow<APIResult<JourneyResponse>> = flow {
        try {
            val result = apiService.getBestJourneys(
                startLat = startLat,
                startLon = startLon,
                endLat = endLat,
                endLon = endLon
            )
            emit(APIResult.Success(result))
        } catch (e: Exception) {
            emit(APIResult.Error(e))
        }
    }

    override fun getRoutesSummary(
        routeList: String,
        importance: String
    ): Flow<APIResult<List<RoutesSummaryResponse>>> = flow {
        try {
            val result = apiService.getRoutesSummary(
                routeList = routeList,
                importance = importance
            )
            emit(APIResult.Success(result))
        } catch (e: Exception) {
            emit(APIResult.Error(e))
        }
    }

    override fun getTripDetails(tripId: String): Flow<APIResult<TripDetailsResponse>> = flow {
        try {
            val result = apiService.getTripDetails(tripId)
            emit(APIResult.Success(result))
        } catch (e: Exception) {
            emit(APIResult.Error(e))
        }
    }

    override suspend fun refreshNearbyDepartures(
        latitude: Double,
        longitude: Double
    ): Result<List<NearbyDeparturesResponse>> {
        return try {
            val result = apiService.getNearbyDepartures(latitude, longitude)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDestinationsFromRoutes(routeIds: List<String>): Flow<APIResult<RoutesSummaryResponse?>> = flow {
        try {
            val routeList = routeIds.joinToString(",")
            val result = apiService.getRoutesSummary(routeList = routeList)
            val first = result.firstOrNull()
            emit(APIResult.Success(first))
        } catch (e: Exception) {
            emit(APIResult.Error(e))
        }
    }

    override suspend fun validateTrips(
        t0: Int,
        r1: String,
        o1: String,
        d1: String,
        r2: String?,
        o2: String?,
        d2: String?
    ): APIResult<ValidatedTripsResponse> {
        return try {
            val res = apiService.validateTrips(t0 = t0, r1 = r1, o1 = o1, d1 = d1, r2 = r2, o2 = o2, d2 = d2)
            APIResult.Success(res)
        } catch (e: Exception) {
            APIResult.Error(e)
        }
    }

    override suspend fun getConsecutiveTrips(
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
    ): APIResult<List<ConsecutiveTripsRow>> {
        return try {
            val res = apiService.getConsecutiveTrips(
                t0 = t0,
                transferBufferSeconds = transferBufferSeconds,
                aRouteId = aRouteId,
                aHeadsign = aHeadsign,
                aStartStopId = aStartStopId,
                aEndStopId = aEndStopId,
                bRouteId = bRouteId,
                bHeadsign = bHeadsign,
                bStartStopId = bStartStopId,
                bEndStopId = bEndStopId
            )
            APIResult.Success(res)
        } catch (e: Exception) {
            APIResult.Error(e)
        }
    }
}
