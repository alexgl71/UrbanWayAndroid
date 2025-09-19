package com.av.urbanway.presentation.components.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.av.urbanway.data.local.GoogleMapsConfig
import com.av.urbanway.presentation.components.UrbanWayMapView
import com.av.urbanway.presentation.viewmodels.MainViewModel

@Composable
fun MapChatView(
    viewModel: MainViewModel,
    isPreview: Boolean,
    modifier: Modifier = Modifier,
    isSheetAnimating: Boolean = false
) {
    val context = LocalContext.current
    val mapConfig = GoogleMapsConfig.getInstance(context)

    // State from VM
    val currentLocation by viewModel.currentLocation.collectAsState()
    val nearbyStops by viewModel.nearbyStops.collectAsState()

    if (isPreview) {
        // Lightweight placeholder for chat bubble preview
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Anteprima mappa",
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.Gray
            )
        }
    } else {
        // Full map view in sheet: centered on user with nearby stops (UIState.NORMAL)
        UrbanWayMapView(
            currentLocation = currentLocation?.coordinates,
            mapConfig = mapConfig,
            stops = nearbyStops,
            uiState = com.av.urbanway.data.models.UIState.NORMAL,
            isSheetAnimating = isSheetAnimating,
            selectedPlace = null,
            selectedJourney = null,
            routeTripDetails = null,
            selectedStopId = null,
            viewModel = viewModel,
            modifier = modifier.fillMaxSize()
        )
    }
}

