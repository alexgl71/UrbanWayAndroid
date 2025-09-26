package com.av.urbanway.data.constants

/**
 * Raw JSON responses as they would be received from the actual API
 * These simulate real API payloads for testing purposes
 */
object ApiJsonConstants {

    /**
     * Route detail JSON response for route 56 - TABACCHI
     * This simulates the API response structure exactly as provided
     */
    const val ROUTE_56_TABACCHI_JSON = """{
    "stops": [
        {
            "stop_id": "644",
            "stop_code": "1576",
            "stop_name": "Fermata 1576 - TABACCHI CAP",
            "arrival_date": 64800,
            "stop_lat": 45.066740,
            "stop_lon": 7.720680
        },
        {
            "stop_id": "642",
            "stop_code": "1573",
            "stop_name": "Fermata 1573 - GASSINO",
            "arrival_date": 64889,
            "stop_lat": 45.066300,
            "stop_lon": 7.717310
        },
        {
            "stop_id": "640",
            "stop_code": "1571",
            "stop_name": "Fermata 1571 - TOSELLI",
            "arrival_date": 64960,
            "stop_lat": 45.065550,
            "stop_lon": 7.714830
        },
        {
            "stop_id": "637",
            "stop_code": "1569",
            "stop_name": "Fermata 1569 - FIGLIE DEI MILITARI",
            "arrival_date": 65073,
            "stop_lat": 45.064160,
            "stop_lon": 7.710990
        },
        {
            "stop_id": "635",
            "stop_code": "1567",
            "stop_name": "Fermata 1567 - BRICCA",
            "arrival_date": 65187,
            "stop_lat": 45.062430,
            "stop_lon": 7.707360
        },
        {
            "stop_id": "475",
            "stop_code": "1422",
            "stop_name": "Fermata 1422 - VILLA DELLA REGINA",
            "arrival_date": 65347,
            "stop_lat": 45.060210,
            "stop_lon": 7.704150
        },
        {
            "stop_id": "473",
            "stop_code": "1420",
            "stop_name": "Fermata 1420 - BIAMONTI",
            "arrival_date": 65415,
            "stop_lat": 45.061300,
            "stop_lon": 7.702060
        },
        {
            "stop_id": "470",
            "stop_code": "1418",
            "stop_name": "Fermata 1418 - GRAN MADRE",
            "arrival_date": 65469,
            "stop_lat": 45.062100,
            "stop_lon": 7.700370
        },
        {
            "stop_id": "3026",
            "stop_code": "479",
            "stop_name": "Fermata 479 - VITTORIO VENETO",
            "arrival_date": 65640,
            "stop_lat": 45.064920,
            "stop_lon": 7.695270
        },
        {
            "stop_id": "3024",
            "stop_code": "477",
            "stop_name": "Fermata 477 - SANT'OTTAVIO",
            "arrival_date": 65728,
            "stop_lat": 45.066500,
            "stop_lon": 7.692820
        },
        {
            "stop_id": "3022",
            "stop_code": "475",
            "stop_name": "Fermata 475 - ROSSINI",
            "arrival_date": 65815,
            "stop_lat": 45.068060,
            "stop_lon": 7.690420
        },
        {
            "stop_id": "3019",
            "stop_code": "472",
            "stop_name": "Fermata 472 - CASTELLO",
            "arrival_date": 66000,
            "stop_lat": 45.070640,
            "stop_lon": 7.684880
        },
        {
            "stop_id": "3017",
            "stop_code": "470",
            "stop_name": "Fermata 470 - BERTOLA",
            "arrival_date": 66104,
            "stop_lat": 45.070500,
            "stop_lon": 7.680440
        },
        {
            "stop_id": "3014",
            "stop_code": "468",
            "stop_name": "Fermata 468 - SICCARDI",
            "arrival_date": 66234,
            "stop_lat": 45.071190,
            "stop_lon": 7.675210
        },
        {
            "stop_id": "1975",
            "stop_code": "28",
            "stop_name": "Fermata 28 - XVIII DICEMBRE",
            "arrival_date": 66442,
            "stop_lat": 45.074370,
            "stop_lon": 7.668470
        },
        {
            "stop_id": "3012",
            "stop_code": "466",
            "stop_name": "Fermata 466 - STATUTO",
            "arrival_date": 66540,
            "stop_lat": 45.076910,
            "stop_lon": 7.669430
        },
        {
            "stop_id": "628",
            "stop_code": "1560",
            "stop_name": "Fermata 1560 - EANDI",
            "arrival_date": 66681,
            "stop_lat": 45.074020,
            "stop_lon": 7.666370
        },
        {
            "stop_id": "2567",
            "stop_code": "3350",
            "stop_name": "Fermata 3350 - DROVETTI",
            "arrival_date": 66760,
            "stop_lat": 45.073230,
            "stop_lon": 7.664470
        },
        {
            "stop_id": "2568",
            "stop_code": "3351",
            "stop_name": "Fermata 3351 - PALMIERI",
            "arrival_date": 66867,
            "stop_lat": 45.074590,
            "stop_lon": 7.660610
        },
        {
            "stop_id": "597",
            "stop_code": "1531",
            "stop_name": "Fermata 1531 - DUCHESSA JOLANDA",
            "arrival_date": 66930,
            "stop_lat": 45.074970,
            "stop_lon": 7.658360
        },
        {
            "stop_id": "598",
            "stop_code": "1532",
            "stop_name": "Fermata 1532 - CAVALLI",
            "arrival_date": 67004,
            "stop_lat": 45.073070,
            "stop_lon": 7.656980
        },
        {
            "stop_id": "3430",
            "stop_code": "646",
            "stop_name": "Fermata 646 - ADRIANO",
            "arrival_date": 67065,
            "stop_lat": 45.072460,
            "stop_lon": 7.655660
        },
        {
            "stop_id": "2414",
            "stop_code": "3199",
            "stop_name": "Fermata 3199 - ADRIANO OVEST",
            "arrival_date": 67144,
            "stop_lat": 45.070210,
            "stop_lon": 7.655230
        },
        {
            "stop_id": "2368",
            "stop_code": "3151",
            "stop_name": "Fermata 3151 - MORETTA",
            "arrival_date": 67234,
            "stop_lat": 45.067620,
            "stop_lon": 7.654790
        },
        {
            "stop_id": "2412",
            "stop_code": "3197",
            "stop_name": "Fermata 3197 - MONGINEVRO EST",
            "arrival_date": 67300,
            "stop_lat": 45.065760,
            "stop_lon": 7.655140
        },
        {
            "stop_id": "3161",
            "stop_code": "551",
            "stop_name": "Fermata 551 - GERMANASCA",
            "arrival_date": 67380,
            "stop_lat": 45.065260,
            "stop_lon": 7.652470
        },
        {
            "stop_id": "3160",
            "stop_code": "550",
            "stop_name": "Fermata 550 - SABOTINO EST",
            "arrival_date": 67440,
            "stop_lat": 45.065040,
            "stop_lon": 7.650070
        },
        {
            "stop_id": "1540",
            "stop_code": "2391",
            "stop_name": "Fermata 2391 - SABOTINO OVEST",
            "arrival_date": 67505,
            "stop_lat": 45.065370,
            "stop_lon": 7.647970
        },
        {
            "stop_id": "579",
            "stop_code": "1515",
            "stop_name": "Fermata 1515 - MONGINEVRO",
            "arrival_date": 67661,
            "stop_lat": 45.064800,
            "stop_lon": 7.644860
        },
        {
            "stop_id": "577",
            "stop_code": "1513",
            "stop_name": "Fermata 1513 - ROBILANT NORD",
            "arrival_date": 67783,
            "stop_lat": 45.061610,
            "stop_lon": 7.644990
        },
        {
            "stop_id": "589",
            "stop_code": "1524",
            "stop_name": "Fermata 1524 - ROBILANT SUD",
            "arrival_date": 67853,
            "stop_lat": 45.060110,
            "stop_lon": 7.644790
        },
        {
            "stop_id": "617",
            "stop_code": "1549",
            "stop_name": "Fermata 1549 - RENIER",
            "arrival_date": 67933,
            "stop_lat": 45.057990,
            "stop_lon": 7.644800
        },
        {
            "stop_id": "258",
            "stop_code": "1226",
            "stop_name": "Fermata 1226 - TOLMINO",
            "arrival_date": 68037,
            "stop_lat": 45.055960,
            "stop_lon": 7.644160
        },
        {
            "stop_id": "256",
            "stop_code": "1224",
            "stop_name": "Fermata 1224 - ISSIGLIO",
            "arrival_date": 68187,
            "stop_lat": 45.057210,
            "stop_lon": 7.638820
        },
        {
            "stop_id": "302",
            "stop_code": "126",
            "stop_name": "Fermata 126 - PARCO RUFFINI",
            "arrival_date": 68340,
            "stop_lat": 45.056890,
            "stop_lon": 7.634810
        },
        {
            "stop_id": "324",
            "stop_code": "128",
            "stop_name": "Fermata 128 - MONFALCONE",
            "arrival_date": 68430,
            "stop_lat": 45.052920,
            "stop_lon": 7.634580
        },
        {
            "stop_id": "3497",
            "stop_code": "710",
            "stop_name": "Fermata 710 - GUIDO RENI",
            "arrival_date": 68504,
            "stop_lat": 45.053860,
            "stop_lon": 7.630830
        },
        {
            "stop_id": "3494",
            "stop_code": "708",
            "stop_name": "Fermata 708 - VEGLIA EST",
            "arrival_date": 68550,
            "stop_lat": 45.053660,
            "stop_lon": 7.628070
        },
        {
            "stop_id": "3492",
            "stop_code": "706",
            "stop_name": "Fermata 706 - LESNA",
            "arrival_date": 68631,
            "stop_lat": 45.052480,
            "stop_lon": 7.623280
        },
        {
            "stop_id": "3488",
            "stop_code": "702",
            "stop_name": "Fermata 702 - ALFIERI",
            "arrival_date": 68695,
            "stop_lat": 45.054640,
            "stop_lon": 7.624040
        },
        {
            "stop_id": "609",
            "stop_code": "1542",
            "stop_name": "Fermata 1542 - RODI",
            "arrival_date": 68731,
            "stop_lat": 45.055590,
            "stop_lon": 7.622960
        },
        {
            "stop_id": "2387",
            "stop_code": "3172",
            "stop_name": "Fermata 3172 - ANTICA DI GRUGLIASCO EST",
            "arrival_date": 68843,
            "stop_lat": 45.058330,
            "stop_lon": 7.620400
        },
        {
            "stop_id": "2385",
            "stop_code": "3170",
            "stop_name": "Fermata 3170 - CREA",
            "arrival_date": 68960,
            "stop_lat": 45.059710,
            "stop_lon": 7.613940
        },
        {
            "stop_id": "2712",
            "stop_code": "3490",
            "stop_name": "Fermata 3490 - GIACOSA",
            "arrival_date": 69109,
            "stop_lat": 45.062140,
            "stop_lon": 7.606940
        },
        {
            "stop_id": "2721",
            "stop_code": "3499",
            "stop_name": "Fermata 3499 - TIRRENO CAP",
            "arrival_date": 69180,
            "stop_lat": 45.064110,
            "stop_lon": 7.608050
        }
    ],
    "shapes": [
        {
            "shape_pt_lat": 4.506673000000000e+001,
            "shape_pt_lon": 7.720680000000000e+000
        },
        {
            "shape_pt_lat": 4.506654000000000e+001,
            "shape_pt_lon": 7.719330000000000e+000
        },
        {
            "shape_pt_lat": 4.506656000000000e+001,
            "shape_pt_lon": 7.719160000000000e+000
        },
        {
            "shape_pt_lat": 4.506629000000000e+001,
            "shape_pt_lon": 7.717640000000000e+000
        },
        {
            "shape_pt_lat": 4.506541000000000e+001,
            "shape_pt_lon": 7.714690000000000e+000
        },
        {
            "shape_pt_lat": 4.506531000000000e+001,
            "shape_pt_lon": 7.714600000000000e+000
        },
        {
            "shape_pt_lat": 4.506427000000000e+001,
            "shape_pt_lon": 7.711460000000000e+000
        },
        {
            "shape_pt_lat": 4.506160000000000e+001,
            "shape_pt_lon": 7.705780000000000e+000
        },
        {
            "shape_pt_lat": 4.506147000000000e+001,
            "shape_pt_lon": 7.705640000000000e+000
        },
        {
            "shape_pt_lat": 4.506132000000000e+001,
            "shape_pt_lon": 7.705590000000000e+000
        },
        {
            "shape_pt_lat": 4.505975000000000e+001,
            "shape_pt_lon": 7.705390000000000e+000
        },
        {
            "shape_pt_lat": 4.505974000000000e+001,
            "shape_pt_lon": 7.705050000000000e+000
        },
        {
            "shape_pt_lat": 4.505978000000000e+001,
            "shape_pt_lon": 7.704890000000000e+000
        },
        {
            "shape_pt_lat": 4.506215000000000e+001,
            "shape_pt_lon": 7.700130000000000e+000
        },
        {
            "shape_pt_lat": 4.506274000000000e+001,
            "shape_pt_lon": 7.699130000000000e+000
        },
        {
            "shape_pt_lat": 4.506294000000000e+001,
            "shape_pt_lon": 7.698250000000000e+000
        },
        {
            "shape_pt_lat": 4.507027000000000e+001,
            "shape_pt_lon": 7.686960000000000e+000
        },
        {
            "shape_pt_lat": 4.507032000000000e+001,
            "shape_pt_lon": 7.686750000000000e+000
        },
        {
            "shape_pt_lat": 4.507021000000000e+001,
            "shape_pt_lon": 7.686070000000000e+000
        },
        {
            "shape_pt_lat": 4.507072000000000e+001,
            "shape_pt_lon": 7.684520000000000e+000
        },
        {
            "shape_pt_lat": 4.507078000000000e+001,
            "shape_pt_lon": 7.684090000000000e+000
        },
        {
            "shape_pt_lat": 4.507026000000000e+001,
            "shape_pt_lon": 7.677740000000000e+000
        },
        {
            "shape_pt_lat": 4.507283000000000e+001,
            "shape_pt_lon": 7.670500000000000e+000
        },
        {
            "shape_pt_lat": 4.507370000000000e+001,
            "shape_pt_lon": 7.668170000000000e+000
        },
        {
            "shape_pt_lat": 4.507378000000000e+001,
            "shape_pt_lon": 7.668070000000000e+000
        },
        {
            "shape_pt_lat": 4.507390000000000e+001,
            "shape_pt_lon": 7.668110000000000e+000
        },
        {
            "shape_pt_lat": 4.507660000000000e+001,
            "shape_pt_lon": 7.669940000000000e+000
        },
        {
            "shape_pt_lat": 4.507676000000000e+001,
            "shape_pt_lon": 7.669840000000000e+000
        },
        {
            "shape_pt_lat": 4.507691000000000e+001,
            "shape_pt_lon": 7.669420000000000e+000
        },
        {
            "shape_pt_lat": 4.507704000000000e+001,
            "shape_pt_lon": 7.668540000000000e+000
        },
        {
            "shape_pt_lat": 4.507664000000000e+001,
            "shape_pt_lon": 7.668270000000000e+000
        },
        {
            "shape_pt_lat": 4.507633000000000e+001,
            "shape_pt_lon": 7.667980000000000e+000
        },
        {
            "shape_pt_lat": 4.507277000000000e+001,
            "shape_pt_lon": 7.665520000000000e+000
        },
        {
            "shape_pt_lat": 4.507519000000000e+001,
            "shape_pt_lon": 7.658670000000000e+000
        },
        {
            "shape_pt_lat": 4.507260000000000e+001,
            "shape_pt_lon": 7.656810000000000e+000
        },
        {
            "shape_pt_lat": 4.507293000000000e+001,
            "shape_pt_lon": 7.655820000000000e+000
        },
        {
            "shape_pt_lat": 4.506736000000000e+001,
            "shape_pt_lon": 7.654900000000000e+000
        },
        {
            "shape_pt_lat": 4.506551000000000e+001,
            "shape_pt_lon": 7.655300000000000e+000
        },
        {
            "shape_pt_lat": 4.506496000000000e+001,
            "shape_pt_lon": 7.649610000000000e+000
        },
        {
            "shape_pt_lat": 4.506523000000000e+001,
            "shape_pt_lon": 7.648740000000000e+000
        },
        {
            "shape_pt_lat": 4.506518000000000e+001,
            "shape_pt_lon": 7.648560000000000e+000
        },
        {
            "shape_pt_lat": 4.506508000000000e+001,
            "shape_pt_lon": 7.648460000000000e+000
        },
        {
            "shape_pt_lat": 4.506647000000000e+001,
            "shape_pt_lon": 7.644830000000000e+000
        },
        {
            "shape_pt_lat": 4.506103000000000e+001,
            "shape_pt_lon": 7.645010000000000e+000
        },
        {
            "shape_pt_lat": 4.506096000000000e+001,
            "shape_pt_lon": 7.644720000000000e+000
        },
        {
            "shape_pt_lat": 4.506087000000000e+001,
            "shape_pt_lon": 7.644600000000000e+000
        },
        {
            "shape_pt_lat": 4.506063000000000e+001,
            "shape_pt_lon": 7.644520000000000e+000
        },
        {
            "shape_pt_lat": 4.506048000000000e+001,
            "shape_pt_lon": 7.644620000000000e+000
        },
        {
            "shape_pt_lat": 4.506039000000000e+001,
            "shape_pt_lon": 7.644780000000000e+000
        },
        {
            "shape_pt_lat": 4.506036000000000e+001,
            "shape_pt_lon": 7.644950000000000e+000
        },
        {
            "shape_pt_lat": 4.505582000000000e+001,
            "shape_pt_lon": 7.644900000000000e+000
        },
        {
            "shape_pt_lat": 4.505582000000000e+001,
            "shape_pt_lon": 7.644700000000000e+000
        },
        {
            "shape_pt_lat": 4.505591000000000e+001,
            "shape_pt_lon": 7.644300000000000e+000
        },
        {
            "shape_pt_lat": 4.505711000000000e+001,
            "shape_pt_lon": 7.639260000000000e+000
        },
        {
            "shape_pt_lat": 4.505807000000000e+001,
            "shape_pt_lon": 7.634980000000000e+000
        },
        {
            "shape_pt_lat": 4.505263000000000e+001,
            "shape_pt_lon": 7.634670000000000e+000
        },
        {
            "shape_pt_lat": 4.505337000000000e+001,
            "shape_pt_lon": 7.632340000000000e+000
        },
        {
            "shape_pt_lat": 4.505402000000000e+001,
            "shape_pt_lon": 7.629970000000000e+000
        },
        {
            "shape_pt_lat": 4.505299000000000e+001,
            "shape_pt_lon": 7.625180000000000e+000
        },
        {
            "shape_pt_lat": 4.505230000000000e+001,
            "shape_pt_lon": 7.623010000000000e+000
        },
        {
            "shape_pt_lat": 4.505250000000000e+001,
            "shape_pt_lon": 7.622870000000000e+000
        },
        {
            "shape_pt_lat": 4.505259000000000e+001,
            "shape_pt_lon": 7.623100000000000e+000
        },
        {
            "shape_pt_lat": 4.505523000000000e+001,
            "shape_pt_lon": 7.624220000000000e+000
        },
        {
            "shape_pt_lat": 4.505638000000000e+001,
            "shape_pt_lon": 7.619190000000000e+000
        },
        {
            "shape_pt_lat": 4.505647000000000e+001,
            "shape_pt_lon": 7.619380000000000e+000
        },
        {
            "shape_pt_lat": 4.505877000000000e+001,
            "shape_pt_lon": 7.620400000000000e+000
        },
        {
            "shape_pt_lat": 4.505900000000000e+001,
            "shape_pt_lon": 7.619430000000000e+000
        },
        {
            "shape_pt_lat": 4.505945000000000e+001,
            "shape_pt_lon": 7.615680000000000e+000
        },
        {
            "shape_pt_lat": 4.505944000000000e+001,
            "shape_pt_lon": 7.615250000000000e+000
        },
        {
            "shape_pt_lat": 4.505951000000000e+001,
            "shape_pt_lon": 7.615050000000000e+000
        },
        {
            "shape_pt_lat": 4.505947000000000e+001,
            "shape_pt_lon": 7.614820000000000e+000
        },
        {
            "shape_pt_lat": 4.505958000000000e+001,
            "shape_pt_lon": 7.614230000000000e+000
        },
        {
            "shape_pt_lat": 4.506006000000000e+001,
            "shape_pt_lon": 7.610560000000000e+000
        },
        {
            "shape_pt_lat": 4.506072000000000e+001,
            "shape_pt_lon": 7.606760000000000e+000
        },
        {
            "shape_pt_lat": 4.506450000000000e+001,
            "shape_pt_lon": 7.607140000000000e+000
        },
        {
            "shape_pt_lat": 4.506447000000000e+001,
            "shape_pt_lon": 7.607330000000000e+000
        },
        {
            "shape_pt_lat": 4.506415000000000e+001,
            "shape_pt_lon": 7.608090000000000e+000
        }
    ]
}"""

    /**
     * Nearby arrivals JSON response for Piazza Adriano area
     * This is the exact raw JSON as provided from the actual API
     */
    const val NEARBY_ARRIVALS_JSON = """[
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
                        "trip_id": "27386894U",
                        "actual_departure_time": "2025-09-26T19:36:24",
                        "wait_minutes": 6,
                        "has_realtime_update": false
                    },
                    {
                        "trip_id": "27386961U",
                        "actual_departure_time": "2025-09-26T19:49:24",
                        "wait_minutes": 19,
                        "has_realtime_update": false
                    },
                    {
                        "trip_id": "27386993U",
                        "actual_departure_time": "2025-09-26T20:02:58",
                        "wait_minutes": 32,
                        "has_realtime_update": false
                    }
                ]
            }
        ]
    },
    {
        "route_id": "16CSU",
        "headsigns": [
            {
                "trip_headsign": "CIRCOLARE SINISTRA, PIAZZA SABOTINO",
                "stop_id": "3430",
                "stop_name": "Fermata 646 - ADRIANO",
                "distance_to_stop": 119,
                "stop_lat": 45.072460,
                "stop_lon": 7.655660,
                "departures": [
                    {
                        "trip_id": "27428801U",
                        "actual_departure_time": "2025-09-26T19:52:45",
                        "wait_minutes": 22,
                        "has_realtime_update": false
                    },
                    {
                        "trip_id": "27428734U",
                        "actual_departure_time": "2025-09-26T19:54:34",
                        "wait_minutes": 24,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27428767U",
                        "actual_departure_time": "2025-09-26T20:04:42",
                        "wait_minutes": 34,
                        "has_realtime_update": false
                    }
                ]
            }
        ]
    },
    {
        "route_id": "55U",
        "headsigns": [
            {
                "trip_headsign": "GERBIDO, VIA MONCALIERI",
                "stop_id": "2501",
                "stop_name": "Fermata 3286 - FERRUCCI",
                "distance_to_stop": 177,
                "stop_lat": 45.071020,
                "stop_lon": 7.656040,
                "departures": [
                    {
                        "trip_id": "27363851U",
                        "actual_departure_time": "2025-09-26T19:34:51",
                        "wait_minutes": 4,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27363698U",
                        "actual_departure_time": "2025-09-26T19:47:44",
                        "wait_minutes": 17,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27363794U",
                        "actual_departure_time": "2025-09-26T19:58:44",
                        "wait_minutes": 28,
                        "has_realtime_update": true
                    }
                ]
            },
            {
                "trip_headsign": "LIMITATO, PIAZZA SANTA RITA",
                "stop_id": "2501",
                "stop_name": "Fermata 3286 - FERRUCCI",
                "distance_to_stop": 177,
                "stop_lat": 45.071020,
                "stop_lon": 7.656040,
                "departures": [
                    {
                        "trip_id": "27363726U",
                        "actual_departure_time": "2025-09-26T20:27:30",
                        "wait_minutes": 57,
                        "has_realtime_update": false
                    }
                ]
            },
            {
                "trip_headsign": "VANCHIGLIA, CORSO FARINI",
                "stop_id": "2502",
                "stop_name": "Fermata 3287 - FERRUCCI",
                "distance_to_stop": 194,
                "stop_lat": 45.070720,
                "stop_lon": 7.656530,
                "departures": [
                    {
                        "trip_id": "27363718U",
                        "actual_departure_time": "2025-09-26T19:32:49",
                        "wait_minutes": 2,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27363732U",
                        "actual_departure_time": "2025-09-26T19:55:34",
                        "wait_minutes": 25,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27363781U",
                        "actual_departure_time": "2025-09-26T19:59:15",
                        "wait_minutes": 29,
                        "has_realtime_update": true
                    }
                ]
            }
        ]
    },
    {
        "route_id": "56U",
        "headsigns": [
            {
                "trip_headsign": "GRUGLIASCO, CORSO TIRRENO",
                "stop_id": "598",
                "stop_name": "Fermata 1532 - CAVALLI",
                "distance_to_stop": 75,
                "stop_lat": 45.073070,
                "stop_lon": 7.656980,
                "departures": [
                    {
                        "trip_id": "27401176U",
                        "actual_departure_time": "2025-09-26T19:33:21",
                        "wait_minutes": 3,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27401192U",
                        "actual_departure_time": "2025-09-26T19:55:23",
                        "wait_minutes": 25,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27401145U",
                        "actual_departure_time": "2025-09-26T20:05:42",
                        "wait_minutes": 35,
                        "has_realtime_update": false
                    }
                ]
            },
            {
                "trip_headsign": "MADONNA DEL PILONE, LARGO TABACCHI",
                "stop_id": "3207",
                "stop_name": "Fermata 595 - ADRIANO",
                "distance_to_stop": 109,
                "stop_lat": 45.072730,
                "stop_lon": 7.655860,
                "departures": [
                    {
                        "trip_id": "27401078U",
                        "actual_departure_time": "2025-09-26T19:31:33",
                        "wait_minutes": 1,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27401091U",
                        "actual_departure_time": "2025-09-26T19:40:56",
                        "wait_minutes": 10,
                        "has_realtime_update": false
                    },
                    {
                        "trip_id": "27401107U",
                        "actual_departure_time": "2025-09-26T19:52:42",
                        "wait_minutes": 22,
                        "has_realtime_update": false
                    }
                ]
            },
            {
                "trip_headsign": "GRUGLIASCO, CORSO TIRRENO",
                "stop_id": "3430",
                "stop_name": "Fermata 646 - ADRIANO",
                "distance_to_stop": 119,
                "stop_lat": 45.072460,
                "stop_lon": 7.655660,
                "departures": [
                    {
                        "trip_id": "27401030U",
                        "actual_departure_time": "2025-09-26T19:30:45",
                        "wait_minutes": 0,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27401176U",
                        "actual_departure_time": "2025-09-26T19:34:22",
                        "wait_minutes": 4,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27401192U",
                        "actual_departure_time": "2025-09-26T19:56:24",
                        "wait_minutes": 26,
                        "has_realtime_update": true
                    }
                ]
            },
            {
                "trip_headsign": "MADONNA DEL PILONE, LARGO TABACCHI",
                "stop_id": "2413",
                "stop_name": "Fermata 3198 - ADRIANO OVEST",
                "distance_to_stop": 263,
                "stop_lat": 45.070360,
                "stop_lon": 7.655480,
                "departures": [
                    {
                        "trip_id": "27401078U",
                        "actual_departure_time": "2025-09-26T19:30:01",
                        "wait_minutes": 0,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27401091U",
                        "actual_departure_time": "2025-09-26T19:39:24",
                        "wait_minutes": 9,
                        "has_realtime_update": false
                    },
                    {
                        "trip_id": "27401107U",
                        "actual_departure_time": "2025-09-26T19:51:27",
                        "wait_minutes": 21,
                        "has_realtime_update": false
                    }
                ]
            },
            {
                "trip_headsign": "GRUGLIASCO, CORSO TIRRENO",
                "stop_id": "2414",
                "stop_name": "Fermata 3199 - ADRIANO OVEST",
                "distance_to_stop": 288,
                "stop_lat": 45.070210,
                "stop_lon": 7.655230,
                "departures": [
                    {
                        "trip_id": "27401030U",
                        "actual_departure_time": "2025-09-26T19:32:04",
                        "wait_minutes": 2,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27401176U",
                        "actual_departure_time": "2025-09-26T19:35:41",
                        "wait_minutes": 5,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27401192U",
                        "actual_departure_time": "2025-09-26T19:57:43",
                        "wait_minutes": 27,
                        "has_realtime_update": true
                    }
                ]
            },
            {
                "trip_headsign": "GRUGLIASCO, CORSO TIRRENO",
                "stop_id": "597",
                "stop_name": "Fermata 1531 - DUCHESSA JOLANDA",
                "distance_to_stop": 299,
                "stop_lat": 45.074970,
                "stop_lon": 7.658360,
                "departures": [
                    {
                        "trip_id": "27401176U",
                        "actual_departure_time": "2025-09-26T19:32:07",
                        "wait_minutes": 2,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27401192U",
                        "actual_departure_time": "2025-09-26T19:54:09",
                        "wait_minutes": 24,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27401145U",
                        "actual_departure_time": "2025-09-26T20:04:38",
                        "wait_minutes": 34,
                        "has_realtime_update": false
                    }
                ]
            }
        ]
    },
    {
        "route_id": "68U",
        "headsigns": [
            {
                "trip_headsign": "CENISIA, VIA FREJUS",
                "stop_id": "2501",
                "stop_name": "Fermata 3286 - FERRUCCI",
                "distance_to_stop": 177,
                "stop_lat": 45.071020,
                "stop_lon": 7.656040,
                "departures": [
                    {
                        "trip_id": "27401304U",
                        "actual_departure_time": "2025-09-26T19:46:37",
                        "wait_minutes": 16,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27401323U",
                        "actual_departure_time": "2025-09-26T19:50:42",
                        "wait_minutes": 20,
                        "has_realtime_update": false
                    },
                    {
                        "trip_id": "27401344U",
                        "actual_departure_time": "2025-09-26T20:02:34",
                        "wait_minutes": 32,
                        "has_realtime_update": true
                    }
                ]
            },
            {
                "trip_headsign": "BORGATA ROSA, VIA CAFASSO",
                "stop_id": "2502",
                "stop_name": "Fermata 3287 - FERRUCCI",
                "distance_to_stop": 194,
                "stop_lat": 45.070720,
                "stop_lon": 7.656530,
                "departures": [
                    {
                        "trip_id": "27401248U",
                        "actual_departure_time": "2025-09-26T19:34:21",
                        "wait_minutes": 4,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27401278U",
                        "actual_departure_time": "2025-09-26T19:49:31",
                        "wait_minutes": 19,
                        "has_realtime_update": false
                    },
                    {
                        "trip_id": "27401316U",
                        "actual_departure_time": "2025-09-26T20:02:16",
                        "wait_minutes": 32,
                        "has_realtime_update": false
                    }
                ]
            }
        ]
    },
    {
        "route_id": "9U",
        "headsigns": [
            {
                "trip_headsign": "BARRIERA LANZO, PIAZZA STAMPALIA",
                "stop_id": "3207",
                "stop_name": "Fermata 595 - ADRIANO",
                "distance_to_stop": 109,
                "stop_lat": 45.072730,
                "stop_lon": 7.655860,
                "departures": [
                    {
                        "trip_id": "27334298U",
                        "actual_departure_time": "2025-09-26T19:32:00",
                        "wait_minutes": 2,
                        "has_realtime_update": false
                    },
                    {
                        "trip_id": "27334171U",
                        "actual_departure_time": "2025-09-26T19:46:00",
                        "wait_minutes": 16,
                        "has_realtime_update": false
                    },
                    {
                        "trip_id": "27334152U",
                        "actual_departure_time": "2025-09-26T19:57:47",
                        "wait_minutes": 27,
                        "has_realtime_update": true
                    }
                ]
            },
            {
                "trip_headsign": "SAN SALVARIO, CORSO D'AZEGLIO (TO EXPO)",
                "stop_id": "3430",
                "stop_name": "Fermata 646 - ADRIANO",
                "distance_to_stop": 119,
                "stop_lat": 45.072460,
                "stop_lon": 7.655660,
                "departures": [
                    {
                        "trip_id": "27334251U",
                        "actual_departure_time": "2025-09-26T19:32:48",
                        "wait_minutes": 2,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27334230U",
                        "actual_departure_time": "2025-09-26T19:39:38",
                        "wait_minutes": 9,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27334273U",
                        "actual_departure_time": "2025-09-26T19:51:00",
                        "wait_minutes": 21,
                        "has_realtime_update": false
                    }
                ]
            },
            {
                "trip_headsign": "BARRIERA LANZO, PIAZZA STAMPALIA",
                "stop_id": "2501",
                "stop_name": "Fermata 3286 - FERRUCCI",
                "distance_to_stop": 177,
                "stop_lat": 45.071020,
                "stop_lon": 7.656040,
                "departures": [
                    {
                        "trip_id": "27334298U",
                        "actual_departure_time": "2025-09-26T19:30:53",
                        "wait_minutes": 0,
                        "has_realtime_update": false
                    },
                    {
                        "trip_id": "27334171U",
                        "actual_departure_time": "2025-09-26T19:44:53",
                        "wait_minutes": 14,
                        "has_realtime_update": false
                    },
                    {
                        "trip_id": "27334152U",
                        "actual_departure_time": "2025-09-26T19:56:40",
                        "wait_minutes": 26,
                        "has_realtime_update": true
                    }
                ]
            },
            {
                "trip_headsign": "SAN SALVARIO, CORSO D'AZEGLIO (TO EXPO)",
                "stop_id": "2502",
                "stop_name": "Fermata 3287 - FERRUCCI",
                "distance_to_stop": 194,
                "stop_lat": 45.070720,
                "stop_lon": 7.656530,
                "departures": [
                    {
                        "trip_id": "27334251U",
                        "actual_departure_time": "2025-09-26T19:34:06",
                        "wait_minutes": 4,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27334230U",
                        "actual_departure_time": "2025-09-26T19:40:56",
                        "wait_minutes": 10,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27334273U",
                        "actual_departure_time": "2025-09-26T19:52:18",
                        "wait_minutes": 22,
                        "has_realtime_update": false
                    }
                ]
            }
        ]
    },
    {
        "route_id": "ST1U",
        "headsigns": [
            {
                "trip_headsign": "VANCHIGLIA, CORSO FARINI",
                "stop_id": "2413",
                "stop_name": "Fermata 3198 - ADRIANO OVEST",
                "distance_to_stop": 263,
                "stop_lat": 45.070360,
                "stop_lon": 7.655480,
                "departures": [
                    {
                        "trip_id": "27324157U",
                        "actual_departure_time": "2025-09-26T19:39:01",
                        "wait_minutes": 9,
                        "has_realtime_update": true
                    },
                    {
                        "trip_id": "27324167U",
                        "actual_departure_time": "2025-09-26T19:59:01",
                        "wait_minutes": 29,
                        "has_realtime_update": false
                    },
                    {
                        "trip_id": "27324138U",
                        "actual_departure_time": "2025-09-26T20:19:01",
                        "wait_minutes": 49,
                        "has_realtime_update": false
                    }
                ]
            }
        ]
    }
]"""
}