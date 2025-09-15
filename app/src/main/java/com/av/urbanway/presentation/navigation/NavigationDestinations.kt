package com.av.urbanway.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object RealtimeArrivals : Screen("realtime_arrivals")
    object RouteDetail : Screen("route_detail")
    object FullscreenMap : Screen("fullscreen_map")
    object FullscreenDestinations : Screen("fullscreen_destinations")
    // JourneyPlanner and JourneyResults removed - now handled by UIState card-based navigation
}

// Navigation arguments
object NavArgs {
    const val ROUTE_ID = "routeId"
    const val STOP_ID = "stopId"
    const val TRIP_ID = "tripId"
}