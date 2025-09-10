package com.av.urbanway.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.av.urbanway.data.api.APIResult
import com.av.urbanway.data.local.FavoritesManager
import com.av.urbanway.data.local.LocationManager
import com.av.urbanway.data.local.PlacesService
import android.content.Context
import com.av.urbanway.data.models.*
import com.av.urbanway.domain.repository.TransitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.*
import com.google.gson.Gson

// Lightweight model for quick route detail shown in the draggable sheet
data class QuickArrivalDetail(
    val id: String = UUID.randomUUID().toString(),
    val routeId: String,
    val destination: String,
    val stopId: String,
    val stopName: String,
    val distanceMeters: Int,
    val times: List<WaitingTime>
)

// Lightweight model for showing a selected place on the map
data class SelectedPlaceData(
    val name: String,
    val description: String,
    val coordinates: Coordinates
)

data class MapOverlayData(
    val routeId: String,
    val shapes: List<Map<String, Double>>,
    val stops: List<Map<String, Any>>,
    val selectedStopId: String,
    // Optional second segment for multi-leg overlays
    val shapes2: List<Map<String, Double>>? = null,
    val stops2: List<Map<String, Any>>? = null
)

class MainViewModel(
    private val transitRepository: TransitRepository,
    private val locationManager: LocationManager,
    private val favoritesManager: FavoritesManager,
    private val placesService: PlacesService
) : ViewModel() {
    private val TAG = "TRANSITOAPP"
    
    companion object {
        fun create(context: Context): MainViewModel {
            val apiService = com.av.urbanway.data.api.ApiServiceFactory.urbanWayApiService
            val transitRepository = com.av.urbanway.data.repository.TransitRepositoryImpl.getInstance(apiService)
            val locationManager = LocationManager.getInstance(context)
            val favoritesManager = FavoritesManager.getInstance(context)
            val placesService = PlacesService.getInstance(context)
            
            return MainViewModel(transitRepository, locationManager, favoritesManager, placesService)
        }
    }

    // MARK: - Published Properties (Android-like architecture)
    
    // Main navigation state - matching iOS MainViewModel
    private val _cards = MutableStateFlow<List<CardItem>>(listOf(
        CardItem(id = "location-default", type = CardType.LOCATION),
        CardItem(id = "map-default", type = CardType.MAP),
        CardItem(id = "destinations-default", type = CardType.DESTINATIONS)
    ))
    val cards: StateFlow<List<CardItem>> = _cards.asStateFlow()
    
    private val _navigationHistory = MutableStateFlow<List<NavigationHistoryItem>>(emptyList())
    val navigationHistory: StateFlow<List<NavigationHistoryItem>> = _navigationHistory.asStateFlow()
    
    private val _isTransitioning = MutableStateFlow(false)
    val isTransitioning: StateFlow<Boolean> = _isTransitioning.asStateFlow()
    
    // Current location and data
    private val _currentLocation = MutableStateFlow(
        Location(
            address = "Piazza Castello, Torino",
            coordinates = Coordinates(lat = 45.07102258187123, lng = 7.685422860157677),
            isManual = false
        )
    )
    val currentLocation: StateFlow<Location> = _currentLocation.asStateFlow()

    // API Data - matching iOS
    private val _nearbyStops = MutableStateFlow<List<StopInfo>>(emptyList())
    val nearbyStops: StateFlow<List<StopInfo>> = _nearbyStops.asStateFlow()
    
    private val _nearbyDepartures = MutableStateFlow<List<NearbyDeparturesResponse>>(emptyList())
    val nearbyDepartures: StateFlow<List<NearbyDeparturesResponse>> = _nearbyDepartures.asStateFlow()
    
    private val _activeWaitingTimes = MutableStateFlow<List<WaitingTime>>(emptyList())
    val activeWaitingTimes: StateFlow<List<WaitingTime>> = _activeWaitingTimes.asStateFlow()
    
    private val _destinationsData = MutableStateFlow<RoutesSummaryResponse?>(null)
    val destinationsData: StateFlow<RoutesSummaryResponse?> = _destinationsData.asStateFlow()
    
    private val _journeys = MutableStateFlow<List<JourneyOption>>(emptyList())
    val journeys: StateFlow<List<JourneyOption>> = _journeys.asStateFlow()
    
    private val _selectedStop = MutableStateFlow<StopInfo?>(null)
    val selectedStop: StateFlow<StopInfo?> = _selectedStop.asStateFlow()
    
    private val _selectedRoute = MutableStateFlow<String?>(null)
    val selectedRoute: StateFlow<String?> = _selectedRoute.asStateFlow()
    
    private val _routeDetailData = MutableStateFlow<Map<String, Any>?>(null)
    val routeDetailData: StateFlow<Map<String, Any>?> = _routeDetailData.asStateFlow()

    // Loading states
    private val _isLoadingNearbyStops = MutableStateFlow(false)
    val isLoadingNearbyStops: StateFlow<Boolean> = _isLoadingNearbyStops.asStateFlow()
    
    private val _isLoadingDepartures = MutableStateFlow(false)
    val isLoadingDepartures: StateFlow<Boolean> = _isLoadingDepartures.asStateFlow()
    
    private val _isLoadingDestinations = MutableStateFlow(false)
    val isLoadingDestinations: StateFlow<Boolean> = _isLoadingDestinations.asStateFlow()
    
    private val _isLoadingJourneys = MutableStateFlow(false)
    val isLoadingJourneys: StateFlow<Boolean> = _isLoadingJourneys.asStateFlow()
    
    private val _showInlineJourneyResults = MutableStateFlow(false)
    val showInlineJourneyResults: StateFlow<Boolean> = _showInlineJourneyResults.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _showingLocationPermissionAlert = MutableStateFlow(false)
    val showingLocationPermissionAlert: StateFlow<Boolean> = _showingLocationPermissionAlert.asStateFlow()

    // Location permissions
    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted.asStateFlow()
    
    // UI State - clean enum-based approach
    private val _uiState = MutableStateFlow(UIState.NORMAL)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()
    
    private val _closeSearchToggle = MutableStateFlow(false)
    val closeSearchToggle: StateFlow<Boolean> = _closeSearchToggle.asStateFlow()
    
    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading.asStateFlow()
    
    private val _searchCategory = MutableStateFlow("address")
    val searchCategory: StateFlow<String> = _searchCategory.asStateFlow()
    
    // Request keyboard focus on the search field from other views
    private val _requestSearchFocus = MutableStateFlow(false)
    val requestSearchFocus: StateFlow<Boolean> = _requestSearchFocus.asStateFlow()
    
    // Journey planner
    private val _startLocation = MutableStateFlow<Location?>(null)
    val startLocation: StateFlow<Location?> = _startLocation.asStateFlow()
    
    private val _endLocation = MutableStateFlow<Location?>(null)
    val endLocation: StateFlow<Location?> = _endLocation.asStateFlow()
    
    private val _showingJourneyPlanner = MutableStateFlow(false)
    val showingJourneyPlanner: StateFlow<Boolean> = _showingJourneyPlanner.asStateFlow()

    // UIState enum - matching iOS
    enum class UIState {
        NORMAL,                    // Homepage with location cards
        SEARCHING,                 // General address/place search with categories
        JOURNEY_PLANNING,          // Journey composer is visible
        EDITING_JOURNEY_FROM,      // Editing FROM field - search for starting point
        EDITING_JOURNEY_TO         // Editing TO field - search for destination
    }
    
    // Legacy support - computed properties for backward compatibility
    val isSearchActive: Boolean
        get() = when (_uiState.value) {
            UIState.NORMAL -> false
            UIState.SEARCHING, UIState.JOURNEY_PLANNING, UIState.EDITING_JOURNEY_FROM, UIState.EDITING_JOURNEY_TO -> true
        }
    
    val showJourneyComposerInSearch: Boolean
        get() = _uiState.value == UIState.JOURNEY_PLANNING
    
    // Centralized visibility for search results card based on UI state
    val shouldShowSearchResultsCard: Boolean
        get() = when (_uiState.value) {
            UIState.SEARCHING -> _searchCategory.value == "address" && _searchQuery.value.isNotEmpty()
            UIState.EDITING_JOURNEY_FROM, UIState.EDITING_JOURNEY_TO -> _searchQuery.value.isNotEmpty()
            else -> false
        }
    
    val editingJourneyField: JourneyFieldType
        get() = when (_uiState.value) {
            UIState.EDITING_JOURNEY_FROM -> JourneyFieldType.FROM
            UIState.EDITING_JOURNEY_TO -> JourneyFieldType.TO
            else -> JourneyFieldType.NONE
        }
    
    enum class JourneyFieldType {
        FROM, TO, NONE
    }

    // UI State
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    
    // Cache management - timestamps for data freshness
    private var nearbyDataLastLoaded: Date? = null
    private var realtimeArrivalsLastLoaded: Date? = null
    private val cacheExpiryInterval: Long = 60 * 1000 // 1 minute for destinations
    private val realtimeCacheExpiryInterval: Long = 30 * 1000 // 30 seconds for real-time data
    
    private val _showingFavorites = MutableStateFlow(false)
    val showingFavorites: StateFlow<Boolean> = _showingFavorites.asStateFlow()
    
    private val _refreshTrigger = MutableStateFlow(UUID.randomUUID())
    val refreshTrigger: StateFlow<UUID> = _refreshTrigger.asStateFlow()
    
    // Target stop to highlight when opening real-time arrivals
    private val _targetHighlightStopId = MutableStateFlow<String?>(null)
    val targetHighlightStopId: StateFlow<String?> = _targetHighlightStopId.asStateFlow()
    
    // Quick detail content for the bottom sheet
    private val _quickDetail = MutableStateFlow<QuickArrivalDetail?>(null)
    val quickDetail: StateFlow<QuickArrivalDetail?> = _quickDetail.asStateFlow()
    
    // Overlay data for the shared map in the bottom sheet
    private val _sheetRouteOverlay = MutableStateFlow<MapOverlayData?>(null)
    val sheetRouteOverlay: StateFlow<MapOverlayData?> = _sheetRouteOverlay.asStateFlow()
    
    // Category results to display on the shared map in bottom sheet (mocked coordinates for now)
    private val _sheetCategory = MutableStateFlow<String?>(null)
    val sheetCategory: StateFlow<String?> = _sheetCategory.asStateFlow()
    
    private val _sheetCategoryPlaces = MutableStateFlow<List<String>>(emptyList())
    val sheetCategoryPlaces: StateFlow<List<String>> = _sheetCategoryPlaces.asStateFlow()
    
    private val _sheetAnnotations = MutableStateFlow<List<MapAnnotation>>(emptyList())
    val sheetAnnotations: StateFlow<List<MapAnnotation>> = _sheetAnnotations.asStateFlow()
    
    // One-shot request to expand bottom sheet (used when opening category results)
    private val _requestExpandBottomSheet = MutableStateFlow(false)
    val requestExpandBottomSheet: StateFlow<Boolean> = _requestExpandBottomSheet.asStateFlow()
    
    // Selected place to show on map
    private val _selectedPlace = MutableStateFlow<SelectedPlaceData?>(null)
    val selectedPlace: StateFlow<SelectedPlaceData?> = _selectedPlace.asStateFlow()
    
    // Token to prevent race conditions when loading overlays
    private var overlayRequestToken: UUID = UUID.randomUUID()
    
    // Toast state
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()
    
    // Alert popup state
    private val _alertMessage = MutableStateFlow<String?>(null)
    val alertMessage: StateFlow<String?> = _alertMessage.asStateFlow()
    
    // Toolbar state - matching iOS
    private val _toolbarVisible = MutableStateFlow(false)
    val toolbarVisible: StateFlow<Boolean> = _toolbarVisible.asStateFlow()
    
    private val _toolbarActions = MutableStateFlow<List<String>>(emptyList())
    val toolbarActions: StateFlow<List<String>> = _toolbarActions.asStateFlow()
    
    // Navigation state for new architecture
    private val _navigationAction = MutableStateFlow<NavigationAction?>(null)
    val navigationAction: StateFlow<NavigationAction?> = _navigationAction.asStateFlow()
    
    // Target destination category to scroll to in FullScreenDestinations
    private val _scrollToDestinationType = MutableStateFlow<String?>(null)
    val scrollToDestinationType: StateFlow<String?> = _scrollToDestinationType.asStateFlow()
    
    // Bottom sheet visibility control
    private val _showBottomSheet = MutableStateFlow(true)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()
    
    // Requests for sheet expansion/collapse (observed by DraggableBottomSheet)
    private val _requestCollapseBottomSheet = MutableStateFlow(false)
    val requestCollapseBottomSheet: StateFlow<Boolean> = _requestCollapseBottomSheet.asStateFlow()
    
    // Expanded/collapsed state for bottom sheet (controlled via FAB)
    private val _isBottomSheetExpanded = MutableStateFlow(false)
    val isBottomSheetExpanded: StateFlow<Boolean> = _isBottomSheetExpanded.asStateFlow()

    fun expandBottomSheet() { _isBottomSheetExpanded.value = true }
    fun collapseBottomSheet() { _isBottomSheetExpanded.value = false }
    fun toggleBottomSheetExpanded() { _isBottomSheetExpanded.value = !_isBottomSheetExpanded.value }
    
    // Favorites
    val pinnedArrivals: StateFlow<List<PinnedArrival>> = favoritesManager.getPinnedArrivals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // MARK: - Computed Properties
    
    val locationCardWaitingTimes: List<WaitingTime>
        get() {
            val waitingTimes = mutableListOf<WaitingTime>()
            
            for (departure in _nearbyDepartures.value) {
                for (headsign in departure.headsigns) {
                    for (departureTime in headsign.departures) {
                        val waitingTime = WaitingTime(
                            route = departure.routeId,
                            minutes = departureTime.waitMinutes,
                            destination = headsign.tripHeadsign,
                            type = TransportType.BUS,
                            isRealTime = departureTime.hasRealtimeUpdate,
                            stopId = headsign.stopId
                        )
                        waitingTimes.add(waitingTime)
                    }
                }
            }
            
            // Sort by minutes (closest first)
            return waitingTimes.sortedBy { it.minutes }
        }
    
    val realTimeArrivalsData: List<ArrivalDisplay>
        get() {
            val arrivalDisplays = mutableListOf<ArrivalDisplay>()
            
            for (routeData in _nearbyDepartures.value) {
                for (headsign in routeData.headsigns) {
                    for (departure in headsign.departures) {
                        val arrivalDisplay = ArrivalDisplay(
                            routeId = routeData.routeId,
                            destination = headsign.tripHeadsign,
                            waitMinutes = departure.waitMinutes,
                            stopName = headsign.stopName,
                            stopId = headsign.stopId,
                            distanceMeters = headsign.distanceToStop,
                            isRealTime = departure.hasRealtimeUpdate,
                            tripId = departure.tripId
                        )
                        arrivalDisplays.add(arrivalDisplay)
                    }
                }
            }
            
            // Filter and sort
            return arrivalDisplays
                .filter { it.waitMinutes >= 0 }
                .sortedBy { it.waitMinutes }
        }
    
    init {
        startLocationObserver()
        setupErrorHandling()
        setupSearchBindings()
        updateToolbarForCurrentContext()
        updateBottomSheetVisibility()
    }

    private fun updateToolbarForCurrentContext() { /* no-op for now */ }
    private fun updateBottomSheetVisibility() { /* no-op for now */ }
    private fun handleAPIError(e: Exception) { _errorMessage.value = e.message }

    // MARK: - Journey helpers (current location)
    fun setStartToCurrentLocation() {
        viewModelScope.launch {
            val resolvedLocation = locationManager.getCurrentLocation()
            if (resolvedLocation != null) {
                _startLocation.value = resolvedLocation
                _uiState.value = UIState.JOURNEY_PLANNING
            }
        }
    }
    
    fun setEndToCurrentLocation() {
        viewModelScope.launch {
            val resolvedLocation = locationManager.getCurrentLocation()
            if (resolvedLocation != null) {
                _endLocation.value = resolvedLocation
                _uiState.value = UIState.JOURNEY_PLANNING
                if (_startLocation.value == null) {
                    _requestSearchFocus.value = true
                }
            }
        }
    }
    
    // MARK: - Setup Methods
    
    private var locationUpdatesJob: kotlinx.coroutines.Job? = null
    private fun startLocationObserver() {
        if (!locationManager.hasLocationPermission()) {
            android.util.Log.w(TAG, "Location permission not granted; not starting updates")
            return
        }
        locationUpdatesJob?.cancel()
        locationUpdatesJob = viewModelScope.launch {
            android.util.Log.d(TAG, "Starting to collect location updates…")
            locationManager.getLocationUpdates().collect { location ->
                _currentLocation.value = location
                android.util.Log.d(TAG, "Got location ${'$'}{location.coordinates}; loading nearby data")
                loadNearbyData()
            }
        }
    }

    fun onLocationPermissionGranted() {
        android.util.Log.d(TAG, "onLocationPermissionGranted: restarting observer and fetching once")
        startLocationObserver()
        viewModelScope.launch {
            val current = locationManager.getCurrentLocation()
            if (current != null) {
                _currentLocation.value = current
                loadNearbyData()
            } else {
                android.util.Log.w(TAG, "getCurrentLocation returned null")
            }
        }
    }
    
    private fun setupErrorHandling() {
        // Placeholder for non-location error handling
    }
    
    private fun setupSearchBindings() {
        viewModelScope.launch {
            placesService.searchResults.collect { results ->
                _searchResults.value = results.map { pr ->
                    SearchResult(
                        title = pr.title,
                        subtitle = pr.subtitle,
                        type = SearchResultType.ADDRESS,
                        coordinates = pr.coordinates,
                        placeId = pr.placeId
                    )
                }
            }
        }
        
        viewModelScope.launch {
            placesService.isSearching.collect { loading ->
                _isSearchLoading.value = loading
            }
        }
    }
    
    // MARK: - Location Methods
    
    fun requestLocationPermission() { /* handled by Activity */ }
    
    fun refreshLocation() {
        viewModelScope.launch {
            val loc = locationManager.getCurrentLocation()
            if (loc != null) _currentLocation.value = loc
        }
    }
    
    fun returnToGPSMode() {
        // Reset to GPS mode and clear manual location
        viewModelScope.launch {
            val gpsLocation = locationManager.getCurrentLocation()
            if (gpsLocation != null) {
                _currentLocation.value = gpsLocation
            } else {
            // Reset to default with isManual = false and request location
            _currentLocation.value = Location(
                address = "Ricerca posizione...",
                coordinates = _currentLocation.value.coordinates, // Keep current coordinates temporarily
                isManual = false
            )
            }
        // Clear any selected place
        _selectedPlace.value = null
        }
    }
    
    fun openJourneyPlanner() {
        _showingJourneyPlanner.value = true
        // Clear existing search state
        _uiState.value = UIState.JOURNEY_PLANNING
        _searchQuery.value = ""
        placesService.stopAutocomplete()
    }

    // MARK: - Cache Management helpers
    
    private val shouldRefreshNearbyData: Boolean
        get() {
            val lastLoaded = nearbyDataLastLoaded ?: return true
            return Date().time - lastLoaded.time > cacheExpiryInterval
        }
    
    private val shouldRefreshRealtimeArrivals: Boolean
        get() {
            val lastLoaded = realtimeArrivalsLastLoaded ?: return true
            return Date().time - lastLoaded.time > realtimeCacheExpiryInterval
        }
    
    // MARK: - API Methods
    
    suspend fun loadNearbyData() {
        if (!shouldRefreshNearbyData) {
            android.util.Log.d(TAG, "Nearby cache fresh; skip")
            return
        }
        
        val coordinates = locationManager.getCurrentLocation()?.coordinates
        if (coordinates == null) {
            _errorMessage.value = "Posizione non disponibile"
            android.util.Log.w(TAG, "No coordinates available; cannot load nearby data")
            return
        }
        
        android.util.Log.d(TAG, "Loading fresh nearby data for ${'$'}coordinates")
        loadNearbyDeparturesAndDestinations(coordinates)
        nearbyDataLastLoaded = Date()
    }
    
    suspend fun forceRefreshNearbyData() {
        nearbyDataLastLoaded = null
        loadNearbyData()
    }
    
    suspend fun loadRealtimeArrivals() {
        if (!shouldRefreshRealtimeArrivals) {
            android.util.Log.d(TAG, "Realtime cache fresh; skip")
            return
        }
        
        val coordinates = locationManager.getCurrentLocation()?.coordinates
        if (coordinates == null) {
            _errorMessage.value = "Posizione non disponibile"
            android.util.Log.w(TAG, "No coordinates for realtime arrivals")
            return
        }
        
        android.util.Log.d(TAG, "Loading realtime arrivals for ${'$'}coordinates")
        loadNearbyDeparturesAndDestinations(coordinates)
        realtimeArrivalsLastLoaded = Date()
    }
    
    suspend fun loadNearbyDeparturesAndDestinations(coordinates: Coordinates) {
        _isLoadingDepartures.value = true
        _isLoadingDestinations.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            // Load departures
            android.util.Log.d(TAG, "Requesting nearby departures API…")
            transitRepository.getNearbyDepartures(
                latitude = coordinates.lat,
                longitude = coordinates.lng
            ).collect { result ->
                when (result) {
                    is APIResult.Success -> {
                        android.util.Log.d(TAG, "Nearby departures loaded: " + result.data.size + " routes")
                        logJson("nearby_departures_payload", result.data)
                        _nearbyDepartures.value = result.data
                        
                        // Build stops map
                        val stopsMap = mutableMapOf<String, StopInfo>()
                        for (departureResponse in result.data) {
                            for (headsign in departureResponse.headsigns) {
                                val existingStop = stopsMap[headsign.stopId]
                                if (existingStop != null) {
                                    val routes = existingStop.routes.toMutableList()
                                    if (!routes.contains(departureResponse.routeId)) {
                                        routes.add(departureResponse.routeId)
                                    }
                                    stopsMap[headsign.stopId] = existingStop.copy(routes = routes)
                                } else {
                                    stopsMap[headsign.stopId] = StopInfo(
                                        stopId = headsign.stopId,
                                        stopName = headsign.stopName,
                                        stopLat = headsign.stopLat,
                                        stopLon = headsign.stopLon,
                                        distanceToStop = headsign.distanceToStop,
                                        routes = listOf(departureResponse.routeId)
                                    )
                                }
                            }
                        }
                        _nearbyStops.value = stopsMap.values.sortedBy { it.distanceToStop }
                        // Log derived counts for debugging
                        val wtCount = locationCardWaitingTimes.size
                        android.util.Log.d(TAG, "waitingTimes computed: " + wtCount + ", nearbyStops: " + _nearbyStops.value.size)
                        
                    }
                    is APIResult.Error -> {
                        android.util.Log.e(TAG, "Nearby departures failed: ${'$'}{result.exception}")
                        handleAPIError(result.exception)
                    }
                }
                _isLoadingDepartures.value = false
            }
        }
        
        // TODO: Load destinations - implement destinations API
        _isLoadingDestinations.value = false
    }

    private fun logJson(label: String, obj: Any) {
        try {
            val json = Gson().toJson(obj)
            if (json.length <= 3500) {
                android.util.Log.d(TAG, "$label: $json")
            } else {
                var i = 0
                while (i < json.length) {
                    val end = kotlin.math.min(i + 3500, json.length)
                    android.util.Log.d(TAG, "$label[$i..$end]: ${json.substring(i, end)}")
                    i = end
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to serialize $label: $e")
        }
    }

    fun refreshNearbyDepartures() {
        viewModelScope.launch {
            val coords = locationManager.getUserLocation()?.coordinates
            if (coords != null) {
                _isLoadingDepartures.value = true
                val result = transitRepository.refreshNearbyDepartures(coords.lat, coords.lng)
                result.onSuccess { list -> _nearbyDepartures.value = list }
                    .onFailure { e -> _errorMessage.value = e.message }
                _isLoadingDepartures.value = false
            }
        }
    }

    // MARK: - Search Methods (Android-like)
    fun openSearch() { 
        _uiState.value = UIState.SEARCHING
    }
    
    fun closeSearch() {
        _uiState.value = UIState.NORMAL
        _searchQuery.value = ""
        placesService.stopAutocomplete()
        _searchResults.value = emptyList()
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        placesService.startAutocomplete(query)
    }

    fun startJourneySearch(
        fromAddress: String,
        fromCoordinates: Coordinates,
        toAddress: String,
        toCoordinates: Coordinates
    ) {
        _startLocation.value = Location(fromAddress, fromCoordinates)
        _endLocation.value = Location(toAddress, toCoordinates)
        
        viewModelScope.launch {
            _isLoadingJourneys.value = true
            
            transitRepository.getBestJourneys(
                startLat = fromCoordinates.lat,
                startLon = fromCoordinates.lng,
                endLat = toCoordinates.lat,
                endLon = toCoordinates.lng
            ).collect { result ->
                when (result) {
                    is APIResult.Success -> {
                        _journeys.value = result.data.journeys
                    }
                    is APIResult.Error -> {
                        _toastMessage.value = "Errore nella ricerca del viaggio"
                    }
                }
                _isLoadingJourneys.value = false
            }
        }
    }

    fun handleRouteSelect(routeId: String, params: Map<String, Any> = emptyMap()) {
        _selectedRoute.value = routeId
        _routeDetailData.value = params
        _targetHighlightStopId.value = params["stopId"] as? String
    }

    fun showFixedJourneyOverlay(journey: JourneyOption) {
        // This would show the journey on the map overlay in the bottom sheet
        _toastMessage.value = "Viaggio selezionato: ${journey.route1Id}"
    }

    fun selectSearchResult(result: SearchResult) {
        when (result.type) {
            SearchResultType.ADDRESS, SearchResultType.PLACE -> {
                result.coordinates?.let { coordinates ->
                    _endLocation.value = Location(result.title, coordinates)
                    _uiState.value = UIState.JOURNEY_PLANNING
                }
            }
            SearchResultType.ROUTE -> {
                result.routeId?.let { routeId ->
                    handleRouteSelect(routeId)
                }
            }
            SearchResultType.STOP -> {
                result.stopId?.let { stopId ->
                    _targetHighlightStopId.value = stopId
                }
            }
            SearchResultType.CATEGORY -> {
                // Handle category selection
            }
        }
    }

    // Favorites Management
    fun addPinnedArrival(routeId: String, destination: String, stopId: String, stopName: String) {
        viewModelScope.launch {
            val pinnedArrival = PinnedArrival(
                routeId = routeId,
                destination = destination,
                stopId = stopId,
                stopName = stopName
            )
            favoritesManager.addPinnedArrival(pinnedArrival)
            showToast("Fermata aggiunta ai preferiti")
        }
    }

    fun removePinnedArrival(routeId: String, destination: String, stopId: String) {
        viewModelScope.launch {
            favoritesManager.removePinnedArrival(routeId, destination, stopId)
            showToast("Fermata rimossa dai preferiti")
        }
    }

    suspend fun isPinnedArrival(routeId: String, destination: String, stopId: String): Boolean {
        return favoritesManager.isPinnedArrival(routeId, destination, stopId)
    }

    fun showToast(message: String) {
        _toastMessage.value = message
        viewModelScope.launch {
            delay(2000)
            _toastMessage.value = null
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

}
