# UrbanWay Android - App States Documentation

## 📱 Primary UIState Enum

Located in: `data/models/NavigationModels.kt`

```kotlin
enum class UIState {
    NORMAL,                    // Default state, home page
    SEARCHING,                 // User is searching for places/addresses  
    JOURNEY_PLANNING,          // Planning a trip from A to B
    EDITING_JOURNEY_FROM,      // Editing departure location
    EDITING_JOURNEY_TO,        // Editing destination location
    ROUTE_DETAIL               // 🆕 Route detail mode with polyline
}
```

## 🗺️ Map Display States

### State Combination Logic:
The map behavior depends on **combination** of states:

| UIState | selectedRoute | routeTripDetails | Map Shows |
|---------|---------------|------------------|-----------|
| `NORMAL` | `null` | `null` | Nearby stops (limited to 25 closest) ⭐ |
| `ROUTE_DETAIL` | `"55"` | `null` | Empty map (loading) + spinner |
| `ROUTE_DETAIL` | `"55"` | `{data}` | **Route stops only + polyline** 🎯 |
| `SEARCHING` | - | - | Search-related pins |
| `JOURNEY_PLANNING` | - | - | Journey route + stops |

## 🔄 State Variables in MainViewModel

### Route Selection States:
```kotlin
// Route detail mode
private val _selectedRoute = MutableStateFlow<String?>(null)           // e.g. "55"
private val _routeDetailData = MutableStateFlow<Map<String, Any>?>(null) // Route params
private val _routeTripDetails = MutableStateFlow<TripDetailsResponse?>(null) // API data

// UI behavior
private val _uiState = MutableStateFlow(UIState.NORMAL)
private val _showBottomSheet = MutableStateFlow(true)
private val _isBottomSheetExpanded = MutableStateFlow(false)
```

## ⚠️ Current Problem

**Issue**: When `selectedRoute` is cleared → `UIState.NORMAL` → Shows ALL stops in viewport (hundreds!)

**Root Cause**: Map filtering logic doesn't limit nearby stops in NORMAL state

```kotlin
// PROBLEMATIC CODE:
val filteredStops = if (routeTripDetails != null) {
    // Route mode: show only route stops
    val routeStopIds = routeTripDetails.stops.orEmpty().map { it.stopId }.toSet()
    stops.filter { stop -> stop.stopId in routeStopIds }
} else {
    stops  // ❌ ALL stops in viewport (can be 100s when zoomed out)
}
```

**Solution**: Limit nearby stops in NORMAL state:
```kotlin
} else {
    // Normal mode: show closest 25 stops to user location
    stops.sortedBy { distanceToUser(it) }.take(25)
}
```

## 🎯 State Transitions

### Route Selection Flow:
1. **Tap Route** → `selectedRoute = "55"`, `uiState = NORMAL`
2. **API Call** → `getTripDetails()` 
3. **Data Loaded** → `routeTripDetails = {data}`
4. **Map Updates** → Shows route stops + polyline
5. **Close/FAB** → `clearRouteDetail()` → Back to nearby stops

### State Management Functions:
```kotlin
// Route selection
fun handleRouteSelect(routeId: String, params: Map<String, Any>)
fun clearRouteDetail()

// Bottom sheet
fun toggleBottomSheetExpanded() // Also clears route when collapsing
fun expandBottomSheet()
fun collapseBottomSheet()

// Search
fun startSearch()  // → UIState.SEARCHING
fun clearSearch()  // → UIState.NORMAL
```

## 🗂️ Map Behavior Summary

### Normal State (`UIState.NORMAL`, `selectedRoute = null`):
- Show 20-30 closest nearby stops
- Camera on user location (16x zoom)
- No polylines

### Route Detail State (`selectedRoute != null`, `routeTripDetails != null`):
- Hide other nearby stops
- Show only route stops as markers
- Show navy blue route polyline
- Auto-zoom to fit route bounds

### Search State (`UIState.SEARCHING`):
- Show search results as pins
- Hide nearby stops

### Journey Planning State (`UIState.JOURNEY_PLANNING`):
- Show planned route
- Show start/end points
- Journey-specific behavior

---

*Last updated: 2025-01-11*
*Location: `/Volumes/SanDisk/GITHUB/UrbanWayAndroid/APP_STATES.md`*