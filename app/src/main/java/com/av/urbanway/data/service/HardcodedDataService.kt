package com.av.urbanway.data.service

import com.av.urbanway.data.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HardcodedDataService {

    // Default user location - Piazza Adriano
    fun getUserLocation(): Location {
        return Location(45.071542060347255, 7.6548553701378355, "Piazza Adriano")
    }

    fun getNearbyArrivals(): TransitData.NearbyData {
        return TransitData.NearbyData(
            stops = getNearbyStops(),
            arrivals = getNearbyArrivals_Internal()
        )
    }

    private fun getNearbyStops(): List<Stop> {
        return listOf(
            Stop(
                id = "3207",
                name = "Fermata 595 - ADRIANO",
                location = Location(45.071542060347255, 7.6548553701378355, "Piazza Adriano"),
                routes = listOf("16CDU", "56U", "9U")
            ),
            Stop(
                id = "3430",
                name = "Fermata 646 - ADRIANO",
                location = Location(45.072460, 7.655660, "Piazza Adriano"),
                routes = listOf("16CSU", "56U", "9U")
            ),
            Stop(
                id = "2501",
                name = "Fermata 3286 - FERRUCCI",
                location = Location(45.071020, 7.656040, "Piazza Adriano"),
                routes = listOf("55U", "68U", "9U")
            ),
            Stop(
                id = "2502",
                name = "Fermata 3287 - FERRUCCI",
                location = Location(45.070720, 7.656530, "Piazza Adriano"),
                routes = listOf("55U", "68U", "9U")
            ),
            Stop(
                id = "598",
                name = "Fermata 1532 - CAVALLI",
                location = Location(45.073070, 7.656980, "Piazza Adriano"),
                routes = listOf("56U")
            )
        )
    }

    private fun getNearbyArrivals_Internal(): List<Arrival> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        return listOf(
            // Route 16CDU
            Arrival(
                routeId = "16CDU",
                routeName = "16 Circolare Destra",
                direction = "CIRCOLARE DESTRA, PORTA PALAZZO",
                scheduledTime = LocalDateTime.parse("2025-09-25T18:13:08", formatter),
                realTimeMinutes = 4,
                isRealTime = true
            ),
            Arrival(
                routeId = "16CSU",
                routeName = "16 Circolare Sinistra",
                direction = "CIRCOLARE SINISTRA, PIAZZA SABOTINO",
                scheduledTime = LocalDateTime.parse("2025-09-25T18:12:23", formatter),
                realTimeMinutes = 3,
                isRealTime = true
            ),
            Arrival(
                routeId = "55U",
                routeName = "55",
                direction = "GERBIDO, VIA MONCALIERI",
                scheduledTime = LocalDateTime.parse("2025-09-25T18:13:41", formatter),
                realTimeMinutes = 4,
                isRealTime = false
            ),
            Arrival(
                routeId = "55U",
                routeName = "55",
                direction = "VANCHIGLIA, CORSO FARINI",
                scheduledTime = LocalDateTime.parse("2025-09-25T18:19:02", formatter),
                realTimeMinutes = 10,
                isRealTime = true
            ),
            Arrival(
                routeId = "56U",
                routeName = "56",
                direction = "GRUGLIASCO, CORSO TIRRENO",
                scheduledTime = LocalDateTime.parse("2025-09-25T18:22:44", formatter),
                realTimeMinutes = 13,
                isRealTime = false
            ),
            Arrival(
                routeId = "68U",
                routeName = "68",
                direction = "CENISIA, VIA FREJUS",
                scheduledTime = LocalDateTime.parse("2025-09-25T18:18:18", formatter),
                realTimeMinutes = 9,
                isRealTime = true
            ),
            Arrival(
                routeId = "9U",
                routeName = "9",
                direction = "BARRIERA LANZO, PIAZZA STAMPALIA",
                scheduledTime = LocalDateTime.parse("2025-09-25T18:23:55", formatter),
                realTimeMinutes = 14,
                isRealTime = true
            ),
            Arrival(
                routeId = "9U",
                routeName = "9",
                direction = "SAN SALVARIO, CORSO D'AZEGLIO (TO EXPO)",
                scheduledTime = LocalDateTime.parse("2025-09-25T18:11:22", formatter),
                realTimeMinutes = 2,
                isRealTime = true
            )
        ).sortedBy { it.realTimeMinutes }
    }

    // Store the original JSON for reference
    private val nearbyJsonData = """
    [
        {
            "route_id": "16CDU",
            "headsigns": [
                {
                    "trip_headsign": "CIRCOLARE DESTRA, PORTA PALAZZO",
                    "stop_id": "3207",
                    "stop_name": "Fermata 595 - ADRIANO",
                    "distance_to_stop": 109,
                    "stop_lat": 45.072730,
                    "stop_lon": 7.655860,
                    "departures": [
                        {
                            "trip_id": "27386884U",
                            "actual_departure_time": "2025-09-25T18:13:08",
                            "wait_minutes": 4,
                            "has_realtime_update": true
                        }
                    ]
                }
            ]
        }
    ]
    """.trimIndent()
}