package com.av.urbanway.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object RealtimeArrivals : Screen("realtime_arrivals")
    object RouteDetail : Screen("route_detail")
    object FullscreenMap : Screen("fullscreen_map")
    object FullscreenDestinations : Screen("fullscreen_destinations")
    object JourneyPlanner : Screen("journey_planner")
    object JourneyResults : Screen("journey_results")
}

// Navigation arguments
object NavArgs {
    const val ROUTE_ID = "routeId"
    const val STOP_ID = "stopId"
    const val TRIP_ID = "tripId"
}