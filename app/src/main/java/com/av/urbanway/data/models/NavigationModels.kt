package com.av.urbanway.data.models

// UI State Models
enum class UIState {
    NORMAL,
    SEARCHING,
    ACCEPTMAPPLACE,
    JOURNEY_PLANNING,
    JOURNEY_RESULTS,
    EDITING_JOURNEY_FROM,
    EDITING_JOURNEY_TO,
    ROUTE_DETAIL,
    JOURNEY_VIEW
}

// Navigation Models
sealed class NavigationDestination {
    object RealtimeArrivals : NavigationDestination()
    object RouteDetail : NavigationDestination()
    object FullscreenMap : NavigationDestination()
    object FullscreenDestinations : NavigationDestination()
    object JourneyPlanner : NavigationDestination()
    object JourneyResults : NavigationDestination()
}

// Search Models
data class SearchResult(
    val title: String,
    val subtitle: String?,
    val type: SearchResultType,
    val coordinates: Coordinates?,
    val placeId: String? = null,
    val routeId: String? = null,
    val stopId: String? = null
)

enum class SearchResultType {
    ADDRESS,
    PLACE,
    ROUTE,
    STOP,
    CATEGORY
}

// Journey Planning Models
data class JourneyPlannerData(
    val toAddress: String,
    val toCoordinates: Coordinates
)

data class JourneyResultsData(
    val fromAddress: String,
    val toAddress: String,
    val fromCoordinates: Coordinates,
    val toCoordinates: Coordinates,
    val journeys: List<JourneyOption>,
    val isLoading: Boolean
)

// Favorites Models
data class FavoriteRoute(
    val routeId: String,
    val destination: String,
    val stopId: String,
    val stopName: String,
    val pinned: Boolean = false
)

data class PinnedArrival(
    val routeId: String,
    val destination: String,
    val stopId: String,
    val stopName: String,
    val addedDate: Long = System.currentTimeMillis()
)

// Navigation Models (matching iOS)
enum class NavigationAction {
    PUSH_LOCATION_CARD,
    PUSH_MAP,
    PUSH_DESTINATIONS,
    PUSH_JOURNEY_PLANNER,
    PUSH_JOURNEY_RESULTS,
    BACK
}

enum class CardType(val rawValue: String) {
    LOCATION("location"),
    MAP("map"),
    DESTINATIONS("destinations"),
    SEARCH("search"),
    SEARCH_RESULTS("searchResults"),
    JOURNEY_PLANNER("journeyPlanner"),
    JOURNEY_RESULTS("journeyResults"),
    JOURNEY("journey"),
    REALTIME_ARRIVALS("realtimeArrivals"),
    ROUTE_DETAIL("routeDetail"),
    ROUTE_DETAIL_TABBED("routeDetailTabbed"),
    DETAILED_MAP("detailedMap"),
    FULLSCREEN_MAP("fullscreenMap"),
    FULLSCREEN_DESTINATIONS("fullscreenDestinations")
}

data class CardItem(
    val id: String,
    val type: CardType,
    val timestamp: Long = System.currentTimeMillis(),
    val data: String? = null
)

data class NavigationHistoryItem(
    val cards: List<CardItem>,
    val title: String,
    val primaryType: CardType
)

// Map Models
enum class MapAnnotationType {
    BUS_STOP,
    TRAIN_STATION,
    PLACE,
    USER_LOCATION
}

data class MapAnnotation(
    val coordinate: Coordinates,
    val title: String,
    val subtitle: String? = null,
    val type: MapAnnotationType,
    val data: Any? = null
)

data class ArrivalDisplay(
    val routeId: String,
    val destination: String,
    val waitMinutes: Int,
    val stopName: String,
    val stopId: String,
    val distanceMeters: Int,
    val isRealTime: Boolean,
    val tripId: String
)

// Error Models
sealed class UrbanWayError : Exception() {
    object NetworkError : UrbanWayError()
    object LocationPermissionDenied : UrbanWayError()
    object LocationNotAvailable : UrbanWayError()
    object NoRoutesFound : UrbanWayError()
    object InvalidAddress : UrbanWayError()
    data class ApiError(val code: Int, override val message: String) : UrbanWayError()
    data class UnknownError(override val message: String) : UrbanWayError()
}
