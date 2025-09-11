package com.av.urbanway.data.local

import android.content.Context
import com.av.urbanway.data.models.Coordinates
import com.av.urbanway.data.models.PlaceDetails
import com.av.urbanway.data.models.PlaceResult
import com.av.urbanway.data.models.SearchResult
import com.av.urbanway.data.models.SearchResultType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume

class PlacesService private constructor(
    private val googleMapsConfig: GoogleMapsConfig
) {
    companion object {
        @Volatile
        private var INSTANCE: PlacesService? = null
        
        fun getInstance(context: Context): PlacesService {
            return INSTANCE ?: synchronized(this) {
                val config = GoogleMapsConfig.getInstance(context)
                INSTANCE ?: PlacesService(config).also { INSTANCE = it }
            }
        }
    }

    // Turin bounding box for search filtering
    private val turinBounds = LatLngBounds(
        LatLng(GoogleMapsConfig.TURIN_SOUTH, GoogleMapsConfig.TURIN_WEST), // SW
        LatLng(GoogleMapsConfig.TURIN_NORTH, GoogleMapsConfig.TURIN_EAST)  // NE
    )

    // State
    private val _searchResults = MutableStateFlow<List<PlaceResult>>(emptyList())
    val searchResults: StateFlow<List<PlaceResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun startAutocomplete(query: String, userLocation: Coordinates? = null) {
        android.util.Log.d("TRANSITOAPP", "PlacesService - startAutocomplete called with query: '$query'")
        // Basic debounce/trim could be added here if needed
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            android.util.Log.d("TRANSITOAPP", "PlacesService - query empty, clearing results")
            _searchResults.value = emptyList()
            return
        }
        android.util.Log.d("TRANSITOAPP", "PlacesService - setting isSearching = true, starting search for: '$trimmed'")
        _isSearching.value = true
        // Fire and forget; the underlying API is callback-based
        // Use existing searchPlaces() to fetch suggestions in the Turin bounding box
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            android.util.Log.d("TRANSITOAPP", "PlacesService - calling searchPlaces for: '$trimmed'")
            val results = searchPlaces(trimmed, userLocation)
            val list = results.getOrElse { error ->
                android.util.Log.e("TRANSITOAPP", "PlacesService - searchPlaces failed: ${error.message}")
                emptyList()
            }
            android.util.Log.d("TRANSITOAPP", "PlacesService - searchPlaces returned ${list.size} results")
            _searchResults.value = list.map {
                android.util.Log.d("TRANSITOAPP", "PlacesService - mapping result: ${it.title}")
                PlaceResult(
                    placeId = it.placeId,
                    title = it.title,
                    subtitle = it.subtitle,
                    coordinates = it.coordinates
                )
            }
            android.util.Log.d("TRANSITOAPP", "PlacesService - setting isSearching = false")
            _isSearching.value = false
        }
    }

    fun stopAutocomplete() {
        _searchResults.value = emptyList()
        _isSearching.value = false
    }
    
    suspend fun searchPlaces(
        query: String,
        location: Coordinates? = null
    ): Result<List<SearchResult>> = suspendCancellableCoroutine { continuation ->
        
        if (!googleMapsConfig.isApiKeyConfigured()) {
            continuation.resume(Result.failure(Exception("Google Maps API key not configured")))
            return@suspendCancellableCoroutine
        }
        
        try {
            val placesClient = googleMapsConfig.placesClient
            
            // Create autocomplete request
            android.util.Log.d("TRANSITOAPP", "PlacesService - creating autocomplete request for query: '$query'")
            android.util.Log.d("TRANSITOAPP", "PlacesService - Turin bounds: N=${turinBounds.northeast.latitude}, S=${turinBounds.southwest.latitude}, E=${turinBounds.northeast.longitude}, W=${turinBounds.southwest.longitude}")
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setLocationBias(RectangularBounds.newInstance(turinBounds))
                .setCountries("IT") // Restrict to Italy
                // Removed type filter to avoid mixing ADDRESS with other types
                .build()
            android.util.Log.d("TRANSITOAPP", "PlacesService - autocomplete request created successfully")
            
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                    val results = response.autocompletePredictions.map { prediction ->
                        SearchResult(
                            title = prediction.getPrimaryText(null).toString(),
                            subtitle = prediction.getSecondaryText(null)?.toString(),
                            type = SearchResultType.PLACE, // Simplified for now
                            coordinates = null, // Will be fetched when selected
                            placeId = prediction.placeId
                        )
                    }
                    continuation.resume(Result.success(results))
                }
                .addOnFailureListener { exception: Exception ->
                    continuation.resume(Result.failure(exception))
                }
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }
    
    suspend fun getPlaceDetails(placeId: String): Result<PlaceDetails> = suspendCancellableCoroutine { continuation ->
        
        if (!googleMapsConfig.isApiKeyConfigured()) {
            continuation.resume(Result.failure(Exception("Google Maps API key not configured")))
            return@suspendCancellableCoroutine
        }
        
        try {
            val placesClient = googleMapsConfig.placesClient
            
            // Define the fields to retrieve
            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.TYPES
            )
            
            val request = FetchPlaceRequest.newInstance(placeId, placeFields)
            
            placesClient.fetchPlace(request)
                .addOnSuccessListener { response: FetchPlaceResponse ->
                    val place = response.place
                    val latLng = place.latLng
                    
                    val coords = latLng?.let { Coordinates(it.latitude, it.longitude) }
                    if (coords == null) {
                        continuation.resume(Result.failure(Exception("Missing coordinates for placeId=$placeId")))
                        return@addOnSuccessListener
                    }
                    val result = PlaceDetails(
                        placeId = placeId,
                        name = place.name ?: "Unknown Place",
                        address = place.address,
                        coordinates = coords
                    )
                    continuation.resume(Result.success(result))
                }
                .addOnFailureListener { exception: Exception ->
                    continuation.resume(Result.failure(exception))
                }
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }
    
    fun getDestinationCategories(): List<SearchResult> {
        return listOf(
            SearchResult(
                title = "Ospedali",
                subtitle = "Strutture sanitarie",
                type = SearchResultType.CATEGORY,
                coordinates = null
            ),
            SearchResult(
                title = "Università",
                subtitle = "Campus e istituti di istruzione",
                type = SearchResultType.CATEGORY,
                coordinates = null
            ),
            SearchResult(
                title = "Musei",
                subtitle = "Luoghi culturali",
                type = SearchResultType.CATEGORY,
                coordinates = null
            ),
            SearchResult(
                title = "Centri Commerciali",
                subtitle = "Shopping e servizi",
                type = SearchResultType.CATEGORY,
                coordinates = null
            ),
            SearchResult(
                title = "Aeroporto",
                subtitle = "Torino Caselle",
                type = SearchResultType.PLACE,
                coordinates = Coordinates(45.2008, 7.6497)
            ),
            SearchResult(
                title = "Porta Nuova",
                subtitle = "Stazione centrale",
                type = SearchResultType.STOP,
                coordinates = Coordinates(45.0617, 7.6781)
            ),
            SearchResult(
                title = "Porta Susa",
                subtitle = "Stazione ferroviaria",
                type = SearchResultType.STOP,
                coordinates = Coordinates(45.0708, 7.6664)
            )
        )
    }
    
    suspend fun reverseGeocode(coordinates: Coordinates): Result<String> {
        // This would use Geocoding API to convert coordinates to address
        // For now, return a simple format
        return Result.success("${coordinates.lat}, ${coordinates.lng}")
    }
}
