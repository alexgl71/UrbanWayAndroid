package com.av.urbanway.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.av.urbanway.data.api.APIResult
import com.av.urbanway.data.local.FavoritesManager
import com.av.urbanway.data.local.LocationManager
import com.av.urbanway.data.local.PlacesService
import android.content.Context
import com.av.urbanway.data.models.*
import com.av.urbanway.data.models.UIState
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
    
    // Legacy navigation properties removed - now using UIState-based navigation
    
    private val _isTransitioning = MutableStateFlow(false)
    val isTransitioning: StateFlow<Boolean> = _isTransitioning.asStateFlow()
    
    // Current location and data - start with null, set to real location or fallback based on permission
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    // API Data - matching iOS
    private val _nearbyStops = MutableStateFlow<List<StopInfo>>(emptyList())
    val nearbyStops: StateFlow<List<StopInfo>> = _nearbyStops.asStateFlow()
    // Persisted full stops dataset (used by map)
    private val _allStops = MutableStateFlow<List<StopInfo>>(emptyList())
    val allStops: StateFlow<List<StopInfo>> = _allStops.asStateFlow()

    // Bootstrap full stops catalog if present on disk or by fetching once
    suspend fun bootstrapStopsIfNeeded(context: Context) {
        if (_allStops.value.isNotEmpty()) return
        val store = com.av.urbanway.data.local.StopsCatalogStore.getInstance(context)
        // Try loading from disk first
        val local = store.load()
        if (local.isNotEmpty()) {
            _allStops.value = local
            return
        }
        // Download delta list and build catalog like iOS
        when (val res = transitRepository.getStopsSync()) {
            is APIResult.Success -> {
                val byId = mutableMapOf<String, StopInfo>()
                for (chg in res.data) {
                    val inner = try {
                        com.google.gson.Gson().fromJson(chg.stopDataJson, com.av.urbanway.data.models.InnerStopData::class.java)
                    } catch (e: Exception) { null }
                    if (inner != null) {
                        byId[inner.stopId] = StopInfo(
                            stopId = inner.stopId,
                            stopName = inner.stopName,
                            stopLat = inner.stopLat,
                            stopLon = inner.stopLon,
                            distanceToStop = 0,
                            routes = emptyList()
                        )
                    }
                }
                val items = byId.values.sortedBy { it.stopName }
                _allStops.value = items
                // Persist to disk for future boots
                store.save(items)
            }
            is APIResult.Error -> {
                // Keep empty; could show a toast/log
                android.util.Log.e(TAG, "Stops sync failed: ${res.exception}")
            }
        }
    }
    
    private val _nearbyDepartures = MutableStateFlow<List<NearbyDeparturesResponse>>(emptyList())
    val nearbyDepartures: StateFlow<List<NearbyDeparturesResponse>> = _nearbyDepartures.asStateFlow()
    
    private val _activeWaitingTimes = MutableStateFlow<List<WaitingTime>>(emptyList())
    val activeWaitingTimes: StateFlow<List<WaitingTime>> = _activeWaitingTimes.asStateFlow()
    
    private val _destinationsData = MutableStateFlow<RoutesSummaryResponse?>(null)
    val destinationsData: StateFlow<RoutesSummaryResponse?> = _destinationsData.asStateFlow()
    
    private val _journeys = MutableStateFlow<List<JourneyOption>>(emptyList())
    val journeys: StateFlow<List<JourneyOption>> = _journeys.asStateFlow()

    private val _selectedJourney = MutableStateFlow<JourneyOption?>(null)
    val selectedJourney: StateFlow<JourneyOption?> = _selectedJourney.asStateFlow()

    private val _selectedStop = MutableStateFlow<StopInfo?>(null)
    val selectedStop: StateFlow<StopInfo?> = _selectedStop.asStateFlow()
    
    private val _selectedRoute = MutableStateFlow<String?>(null)
    val selectedRoute: StateFlow<String?> = _selectedRoute.asStateFlow()
    
    private val _routeDetailData = MutableStateFlow<Map<String, Any>?>(null)
    val routeDetailData: StateFlow<Map<String, Any>?> = _routeDetailData.asStateFlow()
    
    private val _routeTripDetails = MutableStateFlow<com.av.urbanway.data.models.TripDetailsResponse?>(null)
    val routeTripDetails: StateFlow<com.av.urbanway.data.models.TripDetailsResponse?> = _routeTripDetails.asStateFlow()
    
    // Flag to prevent map redrawing during sheet animations
    private val _isSheetAnimating = MutableStateFlow(false)
    val isSheetAnimating: StateFlow<Boolean> = _isSheetAnimating.asStateFlow()

    // Map interaction state - tracks when user is manually dragging the map
    private val _isMapBeingDragged = MutableStateFlow(false)
    val isMapBeingDragged: StateFlow<Boolean> = _isMapBeingDragged.asStateFlow()

    // Route detail stops list visibility
    private val _showRouteStopsList = MutableStateFlow(false)
    val showRouteStopsList: StateFlow<Boolean> = _showRouteStopsList.asStateFlow()

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

    // Flag to prevent auto-navigation to journey planner when user cancels
    private val _allowJourneyAutoNavigation = MutableStateFlow(true)
    val allowJourneyAutoNavigation: StateFlow<Boolean> = _allowJourneyAutoNavigation.asStateFlow()

    
    // Legacy support - computed properties for backward compatibility
    val isSearchActive: Boolean
        get() = when (_uiState.value) {
            UIState.NORMAL, UIState.ACCEPTMAPPLACE, UIState.ROUTE_DETAIL, UIState.JOURNEY_VIEW, UIState.JOURNEY_RESULTS -> false
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
    
    // Legacy navigation action removed - now using direct UIState changes
    
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
    fun toggleBottomSheetExpanded() {
        val wasExpanded = _isBottomSheetExpanded.value
        _isBottomSheetExpanded.value = !wasExpanded

        android.util.Log.d("TRANSITOAPP", "📱 Bottom sheet toggled: expanded=$wasExpanded -> ${!wasExpanded}")

        // Clear journey when collapsing sheet
        if (wasExpanded) {
            android.util.Log.d("TRANSITOAPP", "📱 Collapsing sheet - clearing journey")
            clearSelectedJourney()
        }

        // Set animation flag to prevent map redrawing during transition
        _isSheetAnimating.value = true
        
        // When collapsing sheet in ROUTE_DETAIL state, return to NORMAL
        if (wasExpanded && _uiState.value == UIState.ROUTE_DETAIL) {
            // State changes to NORMAL immediately, map clears immediately
            _uiState.value = UIState.NORMAL
            clearRouteDetail() // This also resets the stops list toggle state
            // Nearby stops will be drawn when sheet is fully collapsed (see onSheetFullyCollapsed)
        }
    }
    
    fun onSheetFullyOpened() {
        // Called when draggable sheet animation completes (fully opened)
        _isSheetAnimating.value = false // Animation complete, safe to draw
        
        if (_uiState.value == UIState.ROUTE_DETAIL && _selectedRoute.value != null) {
            // Now fetch trip details and draw polyline + route stops
            val routeId = _selectedRoute.value!!
            val params = _routeDetailData.value ?: emptyMap()
            fetchRouteTrip(routeId, params)
        }
    }
    
    fun onSheetFullyCollapsed() {
        // Called when draggable sheet animation completes (fully collapsed)
        _isSheetAnimating.value = false // Animation complete, safe to draw
        
        if (_uiState.value == UIState.NORMAL) {
            // Now safe to draw nearby stops (sheet animation finished)
            // The map will automatically show nearby stops since state is NORMAL
        }
    }
    fun toggleBottomSheet() { _showBottomSheet.value = !_showBottomSheet.value }

    // Map drag state control
    fun onMapDragStarted() {
        _isMapBeingDragged.value = true
        android.util.Log.d("TRANSITOAPP", "📍 Map drag started - showing center circle")
    }

    fun onMapDragEnded() {
        _isMapBeingDragged.value = false
        android.util.Log.d("TRANSITOAPP", "📍 Map drag ended - hiding center circle")
    }

    // Route detail stops list control
    fun toggleRouteStopsList() {
        _showRouteStopsList.value = !_showRouteStopsList.value
        android.util.Log.d("TRANSITOAPP", "🚌 Route stops list toggled: ${_showRouteStopsList.value}")
    }

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
                            stopId = headsign.stopId,
                            tripId = departureTime.tripId
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

        // Add logging for UI state changes
        viewModelScope.launch {
            _uiState.collect { newState ->
                android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔄 UI STATE CHANGED: $newState")
                android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔄 Stack trace: ${Thread.currentThread().stackTrace.take(6).joinToString("\n") { "  $it" }}")
            }
        }
    }

    private fun updateToolbarForCurrentContext() { /* no-op for now */ }
    private fun updateBottomSheetVisibility() { /* no-op for now */ }
    private fun handleAPIError(e: Exception) { _errorMessage.value = e.message }

    // MARK: - Journey helpers (current location)
    fun setStartToCurrentLocation() {
        android.util.Log.d("TRANSITOAPP", "🔄 setStartToCurrentLocation() called - Stack trace:")
        android.util.Log.d("TRANSITOAPP", Thread.currentThread().stackTrace.joinToString("\n") { "  at $it" })
        viewModelScope.launch {
            val resolvedLocation = locationManager.getCurrentLocation()
            if (resolvedLocation != null) {
                android.util.Log.d("TRANSITOAPP", "🔄 Setting startLocation to current location: ${resolvedLocation.address}")
                _startLocation.value = resolvedLocation
                _allowJourneyAutoNavigation.value = true // Re-enable auto-navigation
            } else {
                android.util.Log.d("TRANSITOAPP", "🔄 Current location is null, not setting startLocation")
            }
        }
    }
    
    fun setEndToCurrentLocation() {
        viewModelScope.launch {
            val resolvedLocation = locationManager.getCurrentLocation()
            if (resolvedLocation != null) {
                _endLocation.value = resolvedLocation
                _allowJourneyAutoNavigation.value = true // Re-enable auto-navigation
                if (_startLocation.value == null) {
                    _requestSearchFocus.value = true
                }
            }
        }
    }
    
    fun setEndLocationFromPlace(place: SelectedPlaceData) {
        // Convert selected place to Location and set as destination
        _endLocation.value = Location(
            address = place.name, // Use place name as address
            coordinates = place.coordinates
        )
        _allowJourneyAutoNavigation.value = true // Re-enable auto-navigation
        // Set start location to current location if not already set
        if (_startLocation.value == null) {
            setStartToCurrentLocation()
        }
        // Clear the selected place since we've used it
        _selectedPlace.value = null
    }
    
    // MARK: - Manual journey location setters
    fun setStartLocation(location: Location) {
        android.util.Log.d("TRANSITOAPP", "🔄 setStartLocation() called with: ${location.address}")
        _startLocation.value = location
        _allowJourneyAutoNavigation.value = true // Re-enable auto-navigation
    }
    
    fun setEndLocation(location: Location) {
        _endLocation.value = location
        _allowJourneyAutoNavigation.value = true // Re-enable auto-navigation
    }
    
    fun startJourneyToSelectedPlace() {
        val currentSelectedPlace = _selectedPlace.value ?: return
        
        // Clear the selected place and start journey planning
        _selectedPlace.value = null
        
        // Set the selected place as the destination for journey planning
        val destination = Location(
            address = currentSelectedPlace.name,
            coordinates = currentSelectedPlace.coordinates,
            isManual = false
        )
        
        _endLocation.value = destination
        
        // Set user's current location as the default starting point
        val currentLoc = _currentLocation.value
        android.util.Log.d("TRANSITOAPP", "🔄 startJourneyToSelectedPlace() setting start location")
        if (currentLoc != null) {
            android.util.Log.d("TRANSITOAPP", "🔄 Setting startLocation to current location in startJourneyToSelectedPlace: ${currentLoc.address}")
            _startLocation.value = currentLoc
        } else {
            android.util.Log.d("TRANSITOAPP", "🔄 Setting startLocation to null in startJourneyToSelectedPlace")
            _startLocation.value = null
        }
        
        // Hide bottom sheet
        _showBottomSheet.value = false

        // Re-enable auto-navigation for journey planning
        _allowJourneyAutoNavigation.value = true

        // Show journey planning state - transition to journey planner
        _uiState.value = UIState.JOURNEY_PLANNING
        _requestSearchFocus.value = false // Don't auto-focus search
        
        // Show confirmation toast
        _toastMessage.value = "Destinazione impostata: ${destination.address}"
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
        android.util.Log.d("TRANSITOAPP", "MainViewModel - setupSearchBindings called")
        viewModelScope.launch {
            android.util.Log.d("TRANSITOAPP", "MainViewModel - starting to collect placesService.searchResults")
            placesService.searchResults.collect { results ->
                android.util.Log.d("TRANSITOAPP", "MainViewModel - received ${results.size} results from placesService")
                _searchResults.value = results.map { pr ->
                    android.util.Log.d("TRANSITOAPP", "MainViewModel - mapping PlaceResult: ${pr.title}")
                    SearchResult(
                        title = pr.title,
                        subtitle = pr.subtitle,
                        type = SearchResultType.ADDRESS,
                        coordinates = pr.coordinates,
                        placeId = pr.placeId
                    )
                }
                android.util.Log.d("TRANSITOAPP", "MainViewModel - updated _searchResults with ${_searchResults.value.size} SearchResults")
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
    
    fun setLocationPermissionDeniedFallback() {
        android.util.Log.d("TRANSITOAPP", "MainViewModel - Location permission denied, using Piazza Castello fallback")
        _currentLocation.value = Location(
            address = "Piazza Castello, Torino",
            coordinates = Coordinates(lat = 45.07102258187123, lng = 7.685422860157677),
            isManual = false
        )
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
                coordinates = _currentLocation.value?.coordinates ?: Coordinates(lat = 45.07102258187123, lng = 7.685422860157677), // Use current coordinates or fallback
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

        // Set default start location to current location if not already set
        if (_startLocation.value == null) {
            android.util.Log.d("TRANSITOAPP", "🔄 openJourneyPlanner() setting default start location")
            setStartToCurrentLocation()
        }
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
        // Default radius tuned for initial zoom ~16
        loadNearbyDeparturesAndDestinations(coordinates, radiusMeters = 800)
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
        loadNearbyDeparturesAndDestinations(coordinates, radiusMeters = 800)
        realtimeArrivalsLastLoaded = Date()
    }
    
    suspend fun loadNearbyDeparturesAndDestinations(coordinates: Coordinates, radiusMeters: Int = 800) {
        _isLoadingDepartures.value = true
        _isLoadingDestinations.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            // Load departures
            android.util.Log.d(TAG, "Requesting nearby departures API…")
            transitRepository.getNearbyDepartures(
                latitude = coordinates.lat,
                longitude = coordinates.lng,
                radiusMeters = radiusMeters,
                lookAheadMinutes = 60
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
        android.util.Log.d("TRANSITOAPP", "🔄 openSearch() called - changing state from ${_uiState.value} to SEARCHING")
        android.util.Log.d("TRANSITOAPP", "🔄 Stack trace: ${Thread.currentThread().stackTrace.take(5).joinToString("\n") { "  $it" }}")
        _uiState.value = UIState.SEARCHING
    }
    
    fun closeSearch() {
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔍 closeSearch() called - Stack trace:")
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: ${Thread.currentThread().stackTrace.take(8).joinToString("\n") { "  at $it" }}")
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔍 Current UI state in closeSearch: ${_uiState.value}")

        // Don't reset UI state if we're editing journey fields - let the selection handler manage the state
        if (_uiState.value != UIState.EDITING_JOURNEY_FROM && _uiState.value != UIState.EDITING_JOURNEY_TO) {
            android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔍 closeSearch() setting state to NORMAL (was ${_uiState.value})")
            _uiState.value = UIState.NORMAL
        } else {
            android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔍 closeSearch() preserving journey editing state: ${_uiState.value}")
        }
        _searchQuery.value = ""
        placesService.stopAutocomplete()
        _searchResults.value = emptyList()
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔍 closeSearch() finished, UI state: ${_uiState.value}")
    }
    
    fun clearSelectedPlace() {
        android.util.Log.d("TRANSITOAPP", "MainViewModel - clearing selected place")
        _selectedPlace.value = null
    }

    // Missing methods for search components integration
    fun setCategoryForHome(categoryType: String) {
        // TODO: Handle category selection for popular destinations
        // Could trigger a search or filter nearby stops by category
    }
    
    fun updateSearchQuery(query: String) {
        android.util.Log.d("TRANSITO", "MainViewModel - updateSearchQuery called with: '$query'")
        _searchQuery.value = query
        android.util.Log.d("TRANSITO", "MainViewModel - _searchQuery.value updated to: '${_searchQuery.value}'")
        val userLocation = _currentLocation.value?.coordinates
        android.util.Log.d("TRANSITO", "MainViewModel - calling placesService.startAutocomplete with location: $userLocation")
        placesService.startAutocomplete(query, userLocation)
    }

    fun startJourneySearch(
        fromAddress: String,
        fromCoordinates: Coordinates,
        toAddress: String,
        toCoordinates: Coordinates
    ) {
        _startLocation.value = Location(fromAddress, fromCoordinates)
        _endLocation.value = Location(toAddress, toCoordinates)
        _allowJourneyAutoNavigation.value = true // Re-enable auto-navigation
        _uiState.value = UIState.JOURNEY_RESULTS // Show journey results card
        
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
        // Step 1: Set animation flag to prevent drawing during transition
        _isSheetAnimating.value = true
        
        // Step 2: Change state to ROUTE_DETAIL (this clears map immediately)
        _uiState.value = UIState.ROUTE_DETAIL
        _selectedRoute.value = routeId
        _routeDetailData.value = params
        _targetHighlightStopId.value = params["stopId"] as? String
        
        // Step 3: Expand bottom sheet 
        _isBottomSheetExpanded.value = true
        
        // Step 4: API call will happen when sheet is fully opened (see onSheetFullyOpened)
    }
    
    private fun fetchRouteTrip(routeId: String, params: Map<String, Any>) {
        val tripId = params["tripId"] as? String
        
        if (tripId == null) {
            android.util.Log.e("MainViewModel", "No tripId provided for route $routeId")
            _routeTripDetails.value = null
            return
        }
        
        viewModelScope.launch {
            try {
                transitRepository.getTripDetails(tripId).collect { result ->
                    when (result) {
                        is com.av.urbanway.data.api.APIResult.Success -> {
                            _routeTripDetails.value = result.data
                        }
                        is com.av.urbanway.data.api.APIResult.Error -> {
                            android.util.Log.e("MainViewModel", "Failed to fetch trip details for $tripId: ${result.exception.message}")
                            _routeTripDetails.value = null
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Failed to fetch trip details for $tripId: ${e.message}")
                _routeTripDetails.value = null
            }
        }
    }
    
    fun clearRouteDetail() {
        _selectedRoute.value = null
        _routeDetailData.value = null
        _targetHighlightStopId.value = null
        _routeTripDetails.value = null
        _showRouteStopsList.value = false // Reset stops list toggle state
    }

    fun returnToNormalState() {
        android.util.Log.d("TRANSITOAPP", "📱 returnToNormalState() called from ${_uiState.value}")
        _uiState.value = UIState.NORMAL
        _isBottomSheetExpanded.value = false // Collapse sheet to show route arrivals
        _showBottomSheet.value = true // Ensure bottom sheet is visible
    }

    fun showFixedJourneyOverlay(journey: JourneyOption) {
        android.util.Log.d("TRANSITOAPP", "📍 Journey selected: ${journey.route1Id} -> ${journey.route2Id ?: "direct"}")
        android.util.Log.d("TRANSITOAPP", "📍 Trip IDs - trip1: ${journey.trip1Id}, trip2: ${journey.trip2Id}")
        android.util.Log.d("TRANSITOAPP", "📍 Journey isDirect: ${journey.isDirect}")

        // Set UI state to journey view
        _uiState.value = UIState.JOURNEY_VIEW

        // Set the selected journey
        _selectedJourney.value = journey

        // Show and expand bottom sheet to display journey map
        _showBottomSheet.value = true
        expandBottomSheet()

        // Fetch trip details to get polyline shapes
        viewModelScope.launch {
            try {
                _isLoadingJourneys.value = true

                // Fetch first trip details
                val trip1Id = journey.trip1Id
                if (trip1Id != null) {
                    android.util.Log.d("TRANSITOAPP", "📍 Fetching trip details for trip1: $trip1Id")

                    transitRepository.getTripDetails(trip1Id).collect { result ->
                        when (result) {
                            is APIResult.Success -> {
                                android.util.Log.d("TRANSITOAPP", "📍 Trip1 details fetched successfully")
                                android.util.Log.d("TRANSITOAPP", "📍 Trip1 raw stops count: ${result.data.stops?.size}")

                                // Filter stops based on start/end stop IDs (iOS logic)
                                val route1StartStopId = journey.route1StartStopId?.toString()
                                val route1EndStopId = journey.route1EndStopId?.toString()

                                android.util.Log.d("TRANSITOAPP", "📍 Trip1 filtering: start=$route1StartStopId, end=$route1EndStopId")

                                val filteredStops1 = if (route1StartStopId != null && route1EndStopId != null && result.data.stops != null) {
                                    sliceStops(result.data.stops, route1StartStopId, route1EndStopId)
                                } else {
                                    result.data.stops ?: emptyList()
                                }

                                android.util.Log.d("TRANSITOAPP", "📍 Trip1 filtered stops count: ${filteredStops1.size}")

                                // Create polyline from stop coordinates (iOS approach)
                                val shapes1 = filteredStops1.map { stop ->
                                    mapOf(
                                        "lat" to stop.stopLat,
                                        "lon" to stop.stopLon
                                    )
                                }

                                // Store stop data for pins
                                val stops1 = filteredStops1.map { stop ->
                                    mapOf(
                                        "stopId" to stop.stopId,
                                        "stopLat" to stop.stopLat,
                                        "stopLon" to stop.stopLon,
                                        "stopName" to stop.stopName
                                    )
                                }

                                android.util.Log.d("TRANSITOAPP", "📍 Trip1 polyline points: ${shapes1.size}")

                                val updatedJourney = journey.copy(
                                    shapes = shapes1,
                                    stops = stops1
                                )

                                // Fetch second trip if this is a transfer journey
                                val trip2Id = journey.trip2Id
                                if (trip2Id != null && journey.isDirect == 0) {
                                    android.util.Log.d("TRANSITOAPP", "📍 Fetching trip details for trip2: $trip2Id")

                                    transitRepository.getTripDetails(trip2Id).collect { result2 ->
                                        when (result2) {
                                            is APIResult.Success -> {
                                                android.util.Log.d("TRANSITOAPP", "📍 Trip2 details fetched successfully")
                                                android.util.Log.d("TRANSITOAPP", "📍 Trip2 raw stops count: ${result2.data.stops?.size}")

                                                // Filter stops based on start/end stop IDs (iOS logic)
                                                val route2StartStopId = journey.route2StartStopId?.toString()
                                                val route2EndStopId = journey.route2EndStopId?.toString()

                                                android.util.Log.d("TRANSITOAPP", "📍 Trip2 filtering: start=$route2StartStopId, end=$route2EndStopId")

                                                val filteredStops2 = if (route2StartStopId != null && route2EndStopId != null && result2.data.stops != null) {
                                                    sliceStops(result2.data.stops, route2StartStopId, route2EndStopId)
                                                } else {
                                                    result2.data.stops ?: emptyList()
                                                }

                                                android.util.Log.d("TRANSITOAPP", "📍 Trip2 filtered stops count: ${filteredStops2.size}")

                                                // Create polyline from stop coordinates (iOS approach)
                                                val shapes2 = filteredStops2.map { stop ->
                                                    mapOf(
                                                        "lat" to stop.stopLat,
                                                        "lon" to stop.stopLon
                                                    )
                                                }

                                                // Store stop data for pins
                                                val stops2 = filteredStops2.map { stop ->
                                                    mapOf(
                                                        "stopId" to stop.stopId,
                                                        "stopLat" to stop.stopLat,
                                                        "stopLon" to stop.stopLon,
                                                        "stopName" to stop.stopName
                                                    )
                                                }

                                                android.util.Log.d("TRANSITOAPP", "📍 Trip2 polyline points: ${shapes2.size}")

                                                val finalJourney = updatedJourney.copy(
                                                    shapes2 = shapes2,
                                                    stops2 = stops2
                                                )

                                                _selectedJourney.value = finalJourney
                                                _isLoadingJourneys.value = false

                                                android.util.Log.d("TRANSITOAPP", "📍 Final journey updated with both shapes")
                                                android.util.Log.d("TRANSITOAPP", "📍 Final journey shapes1: ${finalJourney.shapes?.size}")
                                                android.util.Log.d("TRANSITOAPP", "📍 Final journey shapes2: ${finalJourney.shapes2?.size}")

                                                _toastMessage.value = "Percorso caricato: ${journey.route1Id} + ${journey.route2Id}"
                                            }
                                            is APIResult.Error -> {
                                                android.util.Log.e("TRANSITOAPP", "📍 Error fetching trip2 details: ${result2.exception.message}")
                                                _selectedJourney.value = updatedJourney // Use first trip only
                                                _isLoadingJourneys.value = false
                                                _toastMessage.value = "Percorso caricato: ${journey.route1Id}"
                                            }
                                        }
                                    }
                                } else {
                                    // Direct journey - only one trip
                                    _selectedJourney.value = updatedJourney
                                    _isLoadingJourneys.value = false

                                    android.util.Log.d("TRANSITOAPP", "📍 Direct journey updated with shapes")
                                    android.util.Log.d("TRANSITOAPP", "📍 Direct journey shapes: ${updatedJourney.shapes?.size}")

                                    _toastMessage.value = "Percorso caricato: ${journey.route1Id}"
                                }
                            }
                            is APIResult.Error -> {
                                android.util.Log.e("TRANSITOAPP", "📍 Error fetching trip1 details: ${result.exception.message}")
                                _selectedJourney.value = journey // Keep original without shapes
                                _isLoadingJourneys.value = false
                                _toastMessage.value = "Errore nel caricamento percorso"
                            }
                        }
                    }
                } else {
                    android.util.Log.w("TRANSITOAPP", "📍 No trip1Id available for journey")
                    _selectedJourney.value = journey
                    _isLoadingJourneys.value = false
                    _toastMessage.value = "Percorso selezionato senza dettagli"
                }
            } catch (e: Exception) {
                android.util.Log.e("TRANSITOAPP", "📍 Exception in showFixedJourneyOverlay: ${e.message}")
                _selectedJourney.value = journey
                _isLoadingJourneys.value = false
                _toastMessage.value = "Errore nel caricamento percorso"
            }
        }
    }

    fun clearSelectedJourney(updateState: Boolean = true) {
        android.util.Log.d("TRANSITOAPP", "📍 Clearing selected journey")
        _selectedJourney.value = null

        if (updateState) {
            // If we're coming from journey view, return to journey results
            // Otherwise, return to normal state
            val targetState = if (_uiState.value == UIState.JOURNEY_VIEW) {
                UIState.JOURNEY_RESULTS
            } else {
                UIState.NORMAL
            }

            _uiState.value = targetState
            android.util.Log.d("TRANSITOAPP", "📍 UI state reset to $targetState")
        } else {
            android.util.Log.d("TRANSITOAPP", "📍 Journey cleared without state change")
        }
        // Note: Don't collapse bottom sheet here - let user do it with FAB
    }

    // iOS equivalent sliceStops function
    private fun sliceStops(
        rawStops: List<com.av.urbanway.data.models.TripStop>,
        startStopId: String,
        endStopId: String
    ): List<com.av.urbanway.data.models.TripStop> {
        android.util.Log.d("TRANSITOAPP", "📍 Slicing stops from $startStopId to $endStopId")

        val startIndex = rawStops.indexOfFirst { it.stopId == startStopId }
        val endIndex = rawStops.indexOfFirst { it.stopId == endStopId }

        android.util.Log.d("TRANSITOAPP", "📍 Found indices: start=$startIndex, end=$endIndex")

        if (startIndex == -1 || endIndex == -1) {
            android.util.Log.w("TRANSITOAPP", "📍 Stop IDs not found in trip, returning all stops")
            return rawStops
        }

        val low = minOf(startIndex, endIndex)
        val high = maxOf(startIndex, endIndex)
        val slicedStops = rawStops.subList(low, high + 1)

        // Reverse if we're going backwards
        return if (startIndex > endIndex) {
            slicedStops.reversed()
        } else {
            slicedStops
        }
    }

    fun selectSearchResult(result: SearchResult) {
        android.util.Log.d("TRANSITOAPP", "MainViewModel - selectSearchResult called with: ${result.title}")
        android.util.Log.d("TRANSITOAPP", "MainViewModel - current UI state: ${_uiState.value}")
        when (result.type) {
            SearchResultType.ADDRESS, SearchResultType.PLACE -> {
                if (result.coordinates != null) {
                    // Direct coordinates available
                    android.util.Log.d("TRANSITOAPP", "MainViewModel - using direct coordinates")

                    // Check if we're editing a journey field
                    when (_uiState.value) {
                        UIState.EDITING_JOURNEY_FROM -> {
                            android.util.Log.d("TRANSITOAPP", "🔄 MainViewModel - setting FROM location to: ${result.title}")
                            android.util.Log.d("TRANSITOAPP", "🔄 Before setting - startLocation: ${_startLocation.value?.address}")
                            _startLocation.value = Location(
                                address = result.title,
                                coordinates = result.coordinates
                            )
                            _allowJourneyAutoNavigation.value = true // Re-enable auto-navigation
                            android.util.Log.d("TRANSITOAPP", "🔄 After setting - startLocation: ${_startLocation.value?.address}")

                            // Add a small delay to check if the location gets overridden
                            viewModelScope.launch {
                                kotlinx.coroutines.delay(100)
                                android.util.Log.d("TRANSITOAPP", "🔄 After 100ms delay - startLocation: ${_startLocation.value?.address}")
                                kotlinx.coroutines.delay(500)
                                android.util.Log.d("TRANSITOAPP", "🔄 After 600ms delay - startLocation: ${_startLocation.value?.address}")
                            }

                            android.util.Log.d("TRANSITOAPP", "🔄 Transitioning to JOURNEY_PLANNING state")
                            closeSearch() // Call closeSearch BEFORE changing state
                            _uiState.value = UIState.JOURNEY_PLANNING
                            _toastMessage.value = "Partenza selezionata: ${result.title}"
                            android.util.Log.d("TRANSITOAPP", "🔄 Final check - startLocation: ${_startLocation.value?.address}")
                        }
                        UIState.EDITING_JOURNEY_TO -> {
                            android.util.Log.d("TRANSITOAPP", "MainViewModel - setting TO location")
                            _endLocation.value = Location(
                                address = result.title,
                                coordinates = result.coordinates
                            )
                            _allowJourneyAutoNavigation.value = true // Re-enable auto-navigation
                            closeSearch() // Call closeSearch BEFORE changing state
                            _uiState.value = UIState.JOURNEY_PLANNING
                            _toastMessage.value = "Destinazione selezionata: ${result.title}"
                        }
                        else -> {
                            // Default behavior - this should only happen in normal search mode
                            // Journey field editing is handled in the cases above
                            android.util.Log.d("TRANSITOAPP", "MainViewModel - normal search mode, showing selected place on map")
                            android.util.Log.d("TRANSITOAPP", "MainViewModel - allowJourneyAutoNavigation: ${_allowJourneyAutoNavigation.value}")

                            // Set selected place data for map pin
                            _selectedPlace.value = SelectedPlaceData(
                                name = result.title,
                                description = result.subtitle ?: "",
                                coordinates = result.coordinates!!
                            )

                            // Check if we should auto-navigate to journey planning (only in normal search mode)
                            if (_allowJourneyAutoNavigation.value) {
                                android.util.Log.d("TRANSITOAPP", "MainViewModel - auto-navigating to journey planning")
                                // Auto-navigate to journey planning (like first time search)
                                startJourneyToSelectedPlace()
                            } else {
                                android.util.Log.d("TRANSITOAPP", "MainViewModel - showing route/walk toolbar")
                                // Set state to accept map place (shows route/walk toolbar)
                                _uiState.value = UIState.ACCEPTMAPPLACE

                                // Close search and show bottom sheet
                                closeSearch()
                                _showBottomSheet.value = true
                                _isBottomSheetExpanded.value = true

                                // Show confirmation
                                _toastMessage.value = "Luogo selezionato: ${result.title}"
                            }
                        }
                    }
                } else if (result.placeId != null) {
                    // Need to fetch place details
                    android.util.Log.d("TRANSITOAPP", "MainViewModel - fetching place details for placeId: ${result.placeId}")
                    android.util.Log.d("TRANSITOAPP", "🔄 Current UI state before place details fetch: ${_uiState.value}")
                    viewModelScope.launch {
                        val placeDetailsResult = placesService.getPlaceDetails(result.placeId)
                        placeDetailsResult.fold(
                            onSuccess = { placeDetails ->
                                android.util.Log.d("TRANSITOAPP", "MainViewModel - place details fetched successfully: ${placeDetails.name}")
                                android.util.Log.d("TRANSITOAPP", "🔄 Current UI state when handling place details: ${_uiState.value}")

                                // Check if we're editing a journey field
                                when (_uiState.value) {
                                    UIState.EDITING_JOURNEY_FROM -> {
                                        android.util.Log.d("TRANSITOAPP", "🔄 MainViewModel - setting FROM location from place details: ${placeDetails.name}")
                                        android.util.Log.d("TRANSITOAPP", "🔄 Before setting - startLocation: ${_startLocation.value?.address}")
                                        _startLocation.value = Location(
                                            address = placeDetails.name,
                                            coordinates = placeDetails.coordinates
                                        )
                                        android.util.Log.d("TRANSITOAPP", "🔄 After setting - startLocation: ${_startLocation.value?.address}")
                                        android.util.Log.d("TRANSITOAPP", "🔄 Transitioning to JOURNEY_PLANNING state")
                                        closeSearch() // Call closeSearch BEFORE changing state
                                        _uiState.value = UIState.JOURNEY_PLANNING
                                        _toastMessage.value = "Partenza selezionata: ${placeDetails.name}"
                                        android.util.Log.d("TRANSITOAPP", "🔄 Final check - startLocation: ${_startLocation.value?.address}")
                                    }
                                    UIState.EDITING_JOURNEY_TO -> {
                                        android.util.Log.d("TRANSITOAPP", "MainViewModel - setting TO location from place details")
                                        _endLocation.value = Location(
                                            address = placeDetails.name,
                                            coordinates = placeDetails.coordinates
                                        )
                                        _allowJourneyAutoNavigation.value = true // Re-enable auto-navigation
                                        closeSearch() // Call closeSearch BEFORE changing state
                                        _uiState.value = UIState.JOURNEY_PLANNING
                                        _toastMessage.value = "Destinazione selezionata: ${placeDetails.name}"
                                    }
                                    else -> {
                                        // Default behavior - this should only happen in normal search mode
                                        // Journey field editing is handled in the cases above
                                        android.util.Log.d("TRANSITOAPP", "MainViewModel - normal search mode from place details, allowJourneyAutoNavigation: ${_allowJourneyAutoNavigation.value}")

                                        // Set selected place data for map pin
                                        _selectedPlace.value = SelectedPlaceData(
                                            name = placeDetails.name,
                                            description = placeDetails.address ?: "",
                                            coordinates = placeDetails.coordinates
                                        )

                                        // Check if we should auto-navigate to journey planning (only in normal search mode)
                                        if (_allowJourneyAutoNavigation.value) {
                                            android.util.Log.d("TRANSITOAPP", "MainViewModel - auto-navigating to journey planning from place details")
                                            // Auto-navigate to journey planning (like first time search)
                                            startJourneyToSelectedPlace()
                                        } else {
                                            android.util.Log.d("TRANSITOAPP", "MainViewModel - showing route/walk toolbar from place details")
                                            // Set state to accept map place (shows route/walk toolbar)
                                            _uiState.value = UIState.ACCEPTMAPPLACE

                                            // Close search first
                                            closeSearch()
                                            android.util.Log.d("TRANSITOAPP", "MainViewModel - showing bottom sheet for selected place")
                                            // Ensure sheet is visible and expanded for selected place
                                            _showBottomSheet.value = true
                                            _isBottomSheetExpanded.value = true

                                            // Show confirmation
                                            _toastMessage.value = "Destinazione selezionata: ${placeDetails.name}"
                                        }
                                    }
                                }
                            },
                            onFailure = { error ->
                                android.util.Log.e("TRANSITOAPP", "MainViewModel - failed to fetch place details: ${error.message}")
                                // Could show an error message to user here
                            }
                        )
                    }
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

    // MARK: - Journey Field Editing Functions
    fun startEditingJourneyFrom() {
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔄 startEditingJourneyFrom() called - Stack trace:")
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: ${Thread.currentThread().stackTrace.take(8).joinToString("\n") { "  at $it" }}")
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔄 Current UI state before change: ${_uiState.value}")
        _uiState.value = UIState.EDITING_JOURNEY_FROM
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔄 UI state set to: ${_uiState.value}")
        _searchQuery.value = ""
        _searchCategory.value = "address"
        _searchResults.value = emptyList()
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 🔄 Search initialized for journey FROM editing")
    }

    fun startEditingJourneyTo() {
        android.util.Log.d("TRANSITOAPP", "MainViewModel - Starting editing journey TO field")
        _uiState.value = UIState.EDITING_JOURNEY_TO
        _searchQuery.value = ""
        _searchCategory.value = "address"
        _searchResults.value = emptyList()
        android.util.Log.d("TRANSITOAPP", "🔄 Search initialized for journey TO editing")
    }

    fun cancelJourneyPlanning() {
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: ❌ cancelJourneyPlanning() called - Stack trace:")
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: ${Thread.currentThread().stackTrace.take(8).joinToString("\n") { "  at $it" }}")
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: ❌ Current UI state before cancel: ${_uiState.value}")
        // Disable auto-navigation first to prevent race condition
        _allowJourneyAutoNavigation.value = false
        _uiState.value = UIState.NORMAL
        closeSearch()
        _showBottomSheet.value = true // Show bottom sheet to return to initial state
        _isBottomSheetExpanded.value = false // Collapse sheet to show route arrivals
        clearSelectedJourney(updateState = false) // Don't let it change state
        // Clear journey locations AND selected place
        _startLocation.value = null
        _endLocation.value = null
        _selectedPlace.value = null // Clear selected place to hide route/walk toolbar
        android.util.Log.d("TRANSITOAPP", "MainViewModel - Disabled auto-navigation, collapsed sheet, cleared all data")
    }

    fun backToJourneyPlanner() {
        android.util.Log.d("TRANSITOAPP", "MainViewModel - Going back to journey planner")
        _uiState.value = UIState.JOURNEY_PLANNING
    }

    // MARK: - Stops persistence hooks
    // Call this once (e.g., on first app launch) after downloading the full stops dataset
    fun setAllStops(stops: List<StopInfo>) {
        _allStops.value = stops
    }

}
