package com.av.urbanway.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.av.urbanway.data.models.FavoriteRoute
import com.av.urbanway.data.models.PinnedArrival
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension property for DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")

class FavoritesManager private constructor(
    private val context: Context,
    private val gson: Gson = Gson()
) {
    companion object {
        @Volatile
        private var INSTANCE: FavoritesManager? = null
        
        fun getInstance(context: Context): FavoritesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FavoritesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        private val PINNED_ARRIVALS_KEY = stringPreferencesKey("pinned_arrivals")
        private val FAVORITE_ROUTES_KEY = stringPreferencesKey("favorite_routes")
    }
    
    private val dataStore = context.dataStore
    
    // Pinned Arrivals
    fun getPinnedArrivals(): Flow<List<PinnedArrival>> {
        return dataStore.data.map { preferences ->
            val json = preferences[PINNED_ARRIVALS_KEY] ?: ""
            if (json.isBlank()) {
                emptyList()
            } else {
                try {
                    val type = object : TypeToken<List<PinnedArrival>>() {}.type
                    gson.fromJson(json, type) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }
    
    suspend fun addPinnedArrival(arrival: PinnedArrival) {
        val currentArrivals = getPinnedArrivals().first().toMutableList()
        
        // Check if already exists
        val existingIndex = currentArrivals.indexOfFirst { 
            it.routeId == arrival.routeId && 
            it.destination == arrival.destination && 
            it.stopId == arrival.stopId 
        }
        
        if (existingIndex == -1) {
            currentArrivals.add(arrival)
            savePinnedArrivals(currentArrivals)
        }
    }
    
    suspend fun removePinnedArrival(routeId: String, destination: String, stopId: String) {
        val currentArrivals = getPinnedArrivals().first().toMutableList()
        currentArrivals.removeAll { 
            it.routeId == routeId && 
            it.destination == destination && 
            it.stopId == stopId 
        }
        savePinnedArrivals(currentArrivals)
    }
    
    suspend fun isPinnedArrival(routeId: String, destination: String, stopId: String): Boolean {
        return getPinnedArrivals().first().any { 
            it.routeId == routeId && 
            it.destination == destination && 
            it.stopId == stopId 
        }
    }
    
    private suspend fun savePinnedArrivals(arrivals: List<PinnedArrival>) {
        dataStore.edit { preferences ->
            val json = gson.toJson(arrivals)
            preferences[PINNED_ARRIVALS_KEY] = json
        }
    }
    
    // Favorite Routes
    fun getFavoriteRoutes(): Flow<List<FavoriteRoute>> {
        return dataStore.data.map { preferences ->
            val json = preferences[FAVORITE_ROUTES_KEY] ?: ""
            if (json.isBlank()) {
                emptyList()
            } else {
                try {
                    val type = object : TypeToken<List<FavoriteRoute>>() {}.type
                    gson.fromJson(json, type) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }
    
    suspend fun addFavoriteRoute(route: FavoriteRoute) {
        val currentRoutes = getFavoriteRoutes().first().toMutableList()
        
        // Check if already exists
        val existingIndex = currentRoutes.indexOfFirst { 
            it.routeId == route.routeId && 
            it.destination == route.destination 
        }
        
        if (existingIndex == -1) {
            currentRoutes.add(route)
            saveFavoriteRoutes(currentRoutes)
        }
    }
    
    suspend fun removeFavoriteRoute(routeId: String, destination: String) {
        val currentRoutes = getFavoriteRoutes().first().toMutableList()
        currentRoutes.removeAll { 
            it.routeId == routeId && 
            it.destination == destination 
        }
        saveFavoriteRoutes(currentRoutes)
    }
    
    suspend fun isFavoriteRoute(routeId: String, destination: String): Boolean {
        return getFavoriteRoutes().first().any { 
            it.routeId == routeId && 
            it.destination == destination 
        }
    }
    
    private suspend fun saveFavoriteRoutes(routes: List<FavoriteRoute>) {
        dataStore.edit { preferences ->
            val json = gson.toJson(routes)
            preferences[FAVORITE_ROUTES_KEY] = json
        }
    }
    
    suspend fun clearAllFavorites() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}