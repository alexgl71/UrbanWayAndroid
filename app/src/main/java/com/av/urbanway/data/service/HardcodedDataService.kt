package com.av.urbanway.data.service

import com.av.urbanway.data.constants.ApiJsonConstants
import com.av.urbanway.data.model.*
import org.json.JSONObject
import org.json.JSONArray
import java.time.LocalDateTime

class HardcodedDataService {

    // Default user location - Piazza Adriano
    fun getUserLocation(): Location {
        return Location(45.071542060347255, 7.6548553701378355, "Piazza Adriano")
    }

    fun getNearbyArrivals(): TransitData.NearbyData {
        return parseNearbyFromJson(ApiJsonConstants.NEARBY_ARRIVALS_JSON)
    }


    fun getRouteDetailData(routeId: String): TransitData.RouteDetailData {
        return when (routeId) {
            "56", "56U" -> parseRouteDetailFromJson(routeId, ApiJsonConstants.ROUTE_56_TABACCHI_JSON)
            else -> getDefaultRouteDetailData(routeId)
        }
    }

    private fun parseRouteDetailFromJson(routeId: String, jsonString: String): TransitData.RouteDetailData {
        try {
            // Parse JSON like a real API response
            val apiResponse = parseRouteDetailApiResponse(jsonString)

            // Map API models to internal models
            val routeStops = apiResponse.stops.map { apiStop ->
                RouteStop(
                    stopId = apiStop.stop_id,
                    stopCode = apiStop.stop_code,
                    stopName = apiStop.stop_name,
                    arrivalDate = apiStop.arrival_date,
                    stopLat = apiStop.stop_lat,
                    stopLon = apiStop.stop_lon
                )
            }

            val shapePoints = apiResponse.shapes.map { apiShape ->
                ShapePoint(
                    shapePtLat = apiShape.shape_pt_lat,
                    shapePtLon = apiShape.shape_pt_lon
                )
            }

            // Create route info based on routeId
            val route = createRouteFromId(routeId)

            return TransitData.RouteDetailData(
                route = route,
                stops = routeStops,
                shapes = shapePoints
            )
        } catch (e: Exception) {
            // Fallback in case of parsing error
            return getDefaultRouteDetailData(routeId)
        }
    }

    private fun createRouteFromId(routeId: String): Route {
        return when (routeId) {
            "56", "56U" -> Route(
                id = "56U",
                name = "56",
                type = TransportType.BUS,
                color = "#FF6B35",
                direction = "TABACCHI"
            )
            else -> Route(
                id = routeId,
                name = routeId,
                type = TransportType.BUS,
                color = "#FF6B35",
                direction = "DESTINAZIONE GENERICA"
            )
        }
    }

    private fun parseRouteDetailApiResponse(jsonString: String): RouteDetailApiResponse {
        val jsonObject = JSONObject(jsonString)

        // Parse stops array
        val stopsArray = jsonObject.getJSONArray("stops")
        val stops = mutableListOf<ApiStop>()

        for (i in 0 until stopsArray.length()) {
            val stopObj = stopsArray.getJSONObject(i)
            stops.add(
                ApiStop(
                    stop_id = stopObj.getString("stop_id"),
                    stop_code = stopObj.getString("stop_code"),
                    stop_name = stopObj.getString("stop_name"),
                    arrival_date = stopObj.getInt("arrival_date"),
                    stop_lat = stopObj.getDouble("stop_lat"),
                    stop_lon = stopObj.getDouble("stop_lon")
                )
            )
        }

        // Parse shapes array
        val shapesArray = jsonObject.getJSONArray("shapes")
        val shapes = mutableListOf<ApiShape>()

        for (i in 0 until shapesArray.length()) {
            val shapeObj = shapesArray.getJSONObject(i)
            shapes.add(
                ApiShape(
                    shape_pt_lat = shapeObj.getDouble("shape_pt_lat"),
                    shape_pt_lon = shapeObj.getDouble("shape_pt_lon")
                )
            )
        }

        return RouteDetailApiResponse(stops = stops, shapes = shapes)
    }

    private fun parseNearbyFromJson(jsonString: String): TransitData.NearbyData {
        try {
            // Parse JSON like a real API response
            val jsonArray = JSONArray(jsonString)
            val allStops = mutableSetOf<Stop>()
            val allArrivals = mutableListOf<Arrival>()

            for (i in 0 until jsonArray.length()) {
                val routeObj = jsonArray.getJSONObject(i)
                val routeId = routeObj.getString("route_id")
                val headsignsArray = routeObj.getJSONArray("headsigns")

                for (j in 0 until headsignsArray.length()) {
                    val headsignObj = headsignsArray.getJSONObject(j)
                    val tripHeadsign = headsignObj.getString("trip_headsign")
                    val stopId = headsignObj.getString("stop_id")
                    val stopName = headsignObj.getString("stop_name")
                    val stopLat = headsignObj.getDouble("stop_lat")
                    val stopLon = headsignObj.getDouble("stop_lon")
                    val departuresArray = headsignObj.getJSONArray("departures")

                    // Add stop to our collection (using Set to avoid duplicates)
                    allStops.add(
                        Stop(
                            id = stopId,
                            name = stopName,
                            location = Location(stopLat, stopLon, "Piazza Adriano"),
                            routes = listOf(routeId) // Simplified - could collect all routes per stop
                        )
                    )

                    // Parse departures and convert to arrivals
                    for (k in 0 until departuresArray.length()) {
                        val departureObj = departuresArray.getJSONObject(k)
                        val tripId = departureObj.getString("trip_id")
                        val actualDepartureTime = departureObj.getString("actual_departure_time")
                        val waitMinutes = departureObj.getInt("wait_minutes")
                        val hasRealtimeUpdate = departureObj.getBoolean("has_realtime_update")

                        // Convert route_id to readable route name
                        val routeName = when (routeId) {
                            "16CDU" -> "16 Circolare Destra"
                            "16CSU" -> "16 Circolare Sinistra"
                            "55U" -> "55"
                            "56U" -> "56"
                            "68U" -> "68"
                            "9U" -> "9"
                            "ST1U" -> "ST1"
                            else -> routeId
                        }

                        allArrivals.add(
                            Arrival(
                                routeId = routeId,
                                routeName = routeName,
                                direction = tripHeadsign,
                                scheduledTime = LocalDateTime.parse(actualDepartureTime),
                                realTimeMinutes = waitMinutes,
                                isRealTime = hasRealtimeUpdate,
                                tripId = tripId
                            )
                        )
                    }
                }
            }

            return TransitData.NearbyData(
                stops = allStops.toList(),
                arrivals = allArrivals.sortedBy { it.realTimeMinutes }
            )
        } catch (e: Exception) {
            // Fallback in case of parsing error
            return TransitData.NearbyData(stops = emptyList(), arrivals = emptyList())
        }
    }

    private fun getDefaultRouteDetailData(routeId: String): TransitData.RouteDetailData {
        return TransitData.RouteDetailData(
            route = Route(
                id = routeId,
                name = routeId,
                type = TransportType.BUS,
                color = "#FF6B35",
                direction = "DESTINAZIONE GENERICA"
            ),
            stops = emptyList(),
            shapes = emptyList()
        )
    }




}