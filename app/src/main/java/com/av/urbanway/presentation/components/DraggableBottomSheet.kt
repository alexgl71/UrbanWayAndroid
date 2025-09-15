package com.av.urbanway.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.av.urbanway.data.local.GoogleMapsConfig
import com.av.urbanway.presentation.viewmodels.MainViewModel
import com.av.urbanway.data.models.Coordinates
import kotlinx.coroutines.launch

@Composable
fun DraggableBottomSheet(
    viewModel: MainViewModel,
    onSearchOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val allStops by viewModel.allStops.collectAsState()
    val nearbyStops by viewModel.nearbyStops.collectAsState()
    val scope = rememberCoroutineScope()
    val expanded by viewModel.isBottomSheetExpanded.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val routeDetailData by viewModel.routeDetailData.collectAsState()
    val routeTripDetails by viewModel.routeTripDetails.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isSheetAnimating by viewModel.isSheetAnimating.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val selectedJourney by viewModel.selectedJourney.collectAsState()
    val startLocation by viewModel.startLocation.collectAsState()
    val endLocation by viewModel.endLocation.collectAsState()

    if (!showBottomSheet) return

    val navy = Color(0xFF0B3D91)

    // Track sheet animation completion (optimized timing)
    LaunchedEffect(expanded) {
        if (expanded) {
            // Wait for expansion animation to complete + small buffer
            kotlinx.coroutines.delay(900) // Reduced from 1000ms
            viewModel.onSheetFullyOpened()
        } else {
            // Wait for collapse animation to complete + small buffer  
            kotlinx.coroutines.delay(600) // Reduced from 800ms
            viewModel.onSheetFullyCollapsed()
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val rawTargetHeight = if (expanded) this.maxHeight - 30.dp else 140.dp
        val targetHeight by animateDpAsState(
            targetValue = rawTargetHeight,
            animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow),
            label = "sheetHeight"
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(targetHeight),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Shared map as background (streets-only style)
                Box(modifier = Modifier.fillMaxSize()) {
                    val stopsForMap = if (allStops.isNotEmpty()) allStops else nearbyStops
                    UrbanWayMapView(
                        currentLocation = currentLocation?.coordinates,
                        mapConfig = GoogleMapsConfig.getInstance(context),
                        modifier = Modifier.fillMaxSize(),
                        stops = stopsForMap,
                        refreshBoundsKey = expanded,
                        routeTripDetails = routeTripDetails,
                        selectedStopId = routeDetailData?.get("stopId") as? String,
                        uiState = uiState,
                        isSheetAnimating = isSheetAnimating,
                        selectedPlace = selectedPlace,
                        selectedJourney = selectedJourney
                    )
                    // Center fixed red circle overlay (independent of the map)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(12.dp)
                            .border(2.dp, Color.White, CircleShape)
                            .then(Modifier)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(8.dp)
                            .border(0.dp, Color.Transparent, CircleShape)
                            .background(Color(0xFFE53935), CircleShape)
                    )
                }

                // Foreground content with transition
                if (expanded) {
                    if (selectedRoute != null && routeDetailData != null) {
                        // Show loading state while waiting for trip details, then show card
                        androidx.compose.animation.AnimatedVisibility(
                            visible = routeTripDetails != null,
                            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) + 
                                   androidx.compose.animation.slideInVertically(
                                       animationSpec = androidx.compose.animation.core.tween(300),
                                       initialOffsetY = { it / 4 }
                                   ),
                            exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                        ) {
                            RouteDetailInfoCard(
                                route = selectedRoute!!,
                                destination = routeDetailData?.get("destination") as? String ?: "",
                                stopName = routeDetailData?.get("stopName") as? String ?: "",
                                distance = routeDetailData?.get("distance") as? Int,
                                arrivalTimes = routeDetailData?.get("arrivalTimes") as? List<com.av.urbanway.data.models.WaitingTime> ?: emptyList(),
                                onClose = {
                                    viewModel.clearRouteDetail()
                                }
                            )
                        }
                        
                        // Show loading indicator while fetching trip details
                        androidx.compose.animation.AnimatedVisibility(
                            visible = routeTripDetails == null,
                            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(200)),
                            exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Caricamento percorso...",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    } else if (selectedJourney != null && uiState == com.av.urbanway.data.models.UIState.JOURNEY_VIEW) {
                        // Detailed Journey Information Overlay
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
                                   androidx.compose.animation.slideInVertically(
                                       animationSpec = androidx.compose.animation.core.tween(300),
                                       initialOffsetY = { it / 4 }
                                   ),
                            exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Spacer(modifier = Modifier.height(8.dp)) // Space for handle

                                // Detailed Journey Information Card
                                JourneyInformationCard(
                                    journey = selectedJourney!!,
                                    allStops = allStops,
                                    onBack = {
                                        viewModel.clearSelectedJourney()
                                    }
                                )

                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        // Default expanded content
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Spacer(Modifier.height(42.dp))

                        }
                    }
                }

                // FAB removed from inside the Card to avoid clipping by Card shape
            }
        }

        // FAB is rendered in MainScreen at a fixed position to avoid moving with the sheet
    }
}


