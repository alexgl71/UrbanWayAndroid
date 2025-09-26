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

    fun getRouteDetailData(routeId: String): TransitData.RouteDetailData {
        return when (routeId) {
            "56", "56U" -> getRoute56TabacchiData()
            else -> getDefaultRouteDetailData(routeId)
        }
    }

    private fun getRoute56TabacchiData(): TransitData.RouteDetailData {
        return TransitData.RouteDetailData(
            route = Route(
                id = "56U",
                name = "56",
                type = TransportType.BUS,
                color = "#FF6B35",
                direction = "TABACCHI"
            ),
            stops = listOf(
                RouteStop("644", "1576", "Fermata 1576 - TABACCHI CAP", 64800, 45.066740, 7.720680),
                RouteStop("642", "1573", "Fermata 1573 - GASSINO", 64889, 45.066300, 7.717310),
                RouteStop("640", "1571", "Fermata 1571 - TOSELLI", 64960, 45.065550, 7.714830),
                RouteStop("637", "1569", "Fermata 1569 - FIGLIE DEI MILITARI", 65073, 45.064160, 7.710990),
                RouteStop("635", "1567", "Fermata 1567 - BRICCA", 65187, 45.062430, 7.707360),
                RouteStop("475", "1422", "Fermata 1422 - VILLA DELLA REGINA", 65347, 45.060210, 7.704150),
                RouteStop("473", "1420", "Fermata 1420 - BIAMONTI", 65415, 45.061300, 7.702060),
                RouteStop("470", "1418", "Fermata 1418 - GRAN MADRE", 65469, 45.062100, 7.700370),
                RouteStop("3026", "479", "Fermata 479 - VITTORIO VENETO", 65640, 45.064920, 7.695270),
                RouteStop("3024", "477", "Fermata 477 - SANT'OTTAVIO", 65728, 45.066500, 7.692820),
                RouteStop("3022", "475", "Fermata 475 - ROSSINI", 65815, 45.068060, 7.690420),
                RouteStop("3019", "472", "Fermata 472 - CASTELLO", 66000, 45.070640, 7.684880),
                RouteStop("3017", "470", "Fermata 470 - BERTOLA", 66104, 45.070500, 7.680440),
                RouteStop("3014", "468", "Fermata 468 - SICCARDI", 66234, 45.071190, 7.675210),
                RouteStop("1975", "28", "Fermata 28 - XVIII DICEMBRE", 66442, 45.074370, 7.668470),
                RouteStop("3012", "466", "Fermata 466 - STATUTO", 66540, 45.076910, 7.669430),
                RouteStop("628", "1560", "Fermata 1560 - EANDI", 66681, 45.074020, 7.666370),
                RouteStop("2567", "3350", "Fermata 3350 - DROVETTI", 66760, 45.073230, 7.664470),
                RouteStop("2568", "3351", "Fermata 3351 - PALMIERI", 66867, 45.074590, 7.660610),
                RouteStop("597", "1531", "Fermata 1531 - DUCHESSA JOLANDA", 66930, 45.074970, 7.658360),
                RouteStop("598", "1532", "Fermata 1532 - CAVALLI", 67004, 45.073070, 7.656980),
                RouteStop("3430", "646", "Fermata 646 - ADRIANO", 67065, 45.072460, 7.655660),
                RouteStop("2414", "3199", "Fermata 3199 - ADRIANO OVEST", 67144, 45.070210, 7.655230),
                RouteStop("2368", "3151", "Fermata 3151 - MORETTA", 67234, 45.067620, 7.654790),
                RouteStop("2412", "3197", "Fermata 3197 - MONGINEVRO EST", 67300, 45.065760, 7.655140),
                RouteStop("3161", "551", "Fermata 551 - GERMANASCA", 67380, 45.065260, 7.652470),
                RouteStop("3160", "550", "Fermata 550 - SABOTINO EST", 67440, 45.065040, 7.650070),
                RouteStop("1540", "2391", "Fermata 2391 - SABOTINO OVEST", 67505, 45.065370, 7.647970),
                RouteStop("579", "1515", "Fermata 1515 - MONGINEVRO", 67661, 45.064800, 7.644860),
                RouteStop("577", "1513", "Fermata 1513 - ROBILANT NORD", 67783, 45.061610, 7.644990),
                RouteStop("589", "1524", "Fermata 1524 - ROBILANT SUD", 67853, 45.060110, 7.644790),
                RouteStop("617", "1549", "Fermata 1549 - RENIER", 67933, 45.057990, 7.644800),
                RouteStop("258", "1226", "Fermata 1226 - TOLMINO", 68037, 45.055960, 7.644160),
                RouteStop("256", "1224", "Fermata 1224 - ISSIGLIO", 68187, 45.057210, 7.638820),
                RouteStop("302", "126", "Fermata 126 - PARCO RUFFINI", 68340, 45.056890, 7.634810),
                RouteStop("324", "128", "Fermata 128 - MONFALCONE", 68430, 45.052920, 7.634580),
                RouteStop("3497", "710", "Fermata 710 - GUIDO RENI", 68504, 45.053860, 7.630830),
                RouteStop("3494", "708", "Fermata 708 - VEGLIA EST", 68550, 45.053660, 7.628070),
                RouteStop("3492", "706", "Fermata 706 - LESNA", 68631, 45.052480, 7.623280),
                RouteStop("3488", "702", "Fermata 702 - ALFIERI", 68695, 45.054640, 7.624040),
                RouteStop("609", "1542", "Fermata 1542 - RODI", 68731, 45.055590, 7.622960),
                RouteStop("2387", "3172", "Fermata 3172 - ANTICA DI GRUGLIASCO EST", 68843, 45.058330, 7.620400),
                RouteStop("2385", "3170", "Fermata 3170 - CREA", 68960, 45.059710, 7.613940),
                RouteStop("2712", "3490", "Fermata 3490 - GIACOSA", 69109, 45.062140, 7.606940),
                RouteStop("2721", "3499", "Fermata 3499 - TIRRENO CAP", 69180, 45.064110, 7.608050)
            ),
            shapes = listOf(
                ShapePoint(45.066730, 7.720680),
                ShapePoint(45.066540, 7.719330),
                ShapePoint(45.066560, 7.719160),
                ShapePoint(45.066290, 7.717640),
                ShapePoint(45.065410, 7.714690),
                ShapePoint(45.065310, 7.714600),
                ShapePoint(45.064270, 7.711460),
                ShapePoint(45.061600, 7.705780),
                ShapePoint(45.061470, 7.705640),
                ShapePoint(45.061320, 7.705590),
                ShapePoint(45.059750, 7.705390),
                ShapePoint(45.059740, 7.705050),
                ShapePoint(45.059780, 7.704890),
                ShapePoint(45.062150, 7.700130),
                ShapePoint(45.062740, 7.699130),
                ShapePoint(45.062940, 7.698250),
                ShapePoint(45.070270, 7.686960),
                ShapePoint(45.070320, 7.686750),
                ShapePoint(45.070210, 7.686070),
                ShapePoint(45.070720, 7.684520),
                ShapePoint(45.070780, 7.684090),
                ShapePoint(45.070260, 7.677740),
                ShapePoint(45.072830, 7.670500),
                ShapePoint(45.073700, 7.668170),
                ShapePoint(45.073780, 7.668070),
                ShapePoint(45.073900, 7.668110),
                ShapePoint(45.076600, 7.669940),
                ShapePoint(45.076760, 7.669840),
                ShapePoint(45.076910, 7.669420),
                ShapePoint(45.077040, 7.668540),
                ShapePoint(45.076640, 7.668270),
                ShapePoint(45.076330, 7.667980),
                ShapePoint(45.072770, 7.665520),
                ShapePoint(45.075190, 7.658670),
                ShapePoint(45.072600, 7.656810),
                ShapePoint(45.072930, 7.655820),
                ShapePoint(45.067360, 7.654900),
                ShapePoint(45.065510, 7.655300),
                ShapePoint(45.064960, 7.649610),
                ShapePoint(45.065230, 7.648740),
                ShapePoint(45.065180, 7.648560),
                ShapePoint(45.065080, 7.648460),
                ShapePoint(45.066470, 7.644830),
                ShapePoint(45.061030, 7.645010),
                ShapePoint(45.060960, 7.644720),
                ShapePoint(45.060870, 7.644600),
                ShapePoint(45.060630, 7.644520),
                ShapePoint(45.060480, 7.644620),
                ShapePoint(45.060390, 7.644780),
                ShapePoint(45.060360, 7.644950),
                ShapePoint(45.055820, 7.644900),
                ShapePoint(45.055820, 7.644700),
                ShapePoint(45.055910, 7.644300),
                ShapePoint(45.057110, 7.639260),
                ShapePoint(45.058070, 7.634980),
                ShapePoint(45.052630, 7.634670),
                ShapePoint(45.053370, 7.632340),
                ShapePoint(45.054020, 7.629970),
                ShapePoint(45.052990, 7.625180),
                ShapePoint(45.052300, 7.623010),
                ShapePoint(45.052500, 7.622870),
                ShapePoint(45.052590, 7.623100),
                ShapePoint(45.055230, 7.624220),
                ShapePoint(45.056380, 7.619190),
                ShapePoint(45.056470, 7.619380),
                ShapePoint(45.058770, 7.620400),
                ShapePoint(45.059000, 7.619430),
                ShapePoint(45.059450, 7.615680),
                ShapePoint(45.059440, 7.615250),
                ShapePoint(45.059510, 7.615050),
                ShapePoint(45.059470, 7.614820),
                ShapePoint(45.059580, 7.614230),
                ShapePoint(45.060060, 7.610560),
                ShapePoint(45.060720, 7.606760),
                ShapePoint(45.064500, 7.607140),
                ShapePoint(45.064470, 7.607330),
                ShapePoint(45.064150, 7.608090)
            )
        )
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