package com.av.urbanway.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.lifecycle.viewmodel.compose.viewModel
import com.av.urbanway.RequestLocationPermission
import com.av.urbanway.data.local.LocationManager
import com.av.urbanway.data.models.UIState
import com.av.urbanway.presentation.components.DraggableBottomSheet
import com.av.urbanway.presentation.components.HomePage
import com.av.urbanway.presentation.components.ToastView
import com.av.urbanway.presentation.components.ContextualFABButtons
import com.av.urbanway.presentation.components.DefaultFABButtons
import com.av.urbanway.presentation.components.UnifiedFloatingToolbar
import com.av.urbanway.presentation.components.IOSFloatingToolbar
import com.av.urbanway.presentation.components.FloatingActionBarWithCenterGap
import com.av.urbanway.presentation.components.ToolbarButton
import com.av.urbanway.presentation.components.SearchScreen
import com.av.urbanway.presentation.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel { MainViewModel.create(context) }
    val locationManager = LocationManager.getInstance(context)
    
    val uiState by viewModel.uiState.collectAsState()

    // Log UI state changes in MainScreen
    LaunchedEffect(uiState) {
        android.util.Log.d("TRANSITOAPP", "TRANSITOAPP: 📱 MainScreen detected UI state change: $uiState")
    }
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val isBottomSheetExpanded by viewModel.isBottomSheetExpanded.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val alertMessage by viewModel.alertMessage.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val startLocation by viewModel.startLocation.collectAsState()
    val endLocation by viewModel.endLocation.collectAsState()
    val allowJourneyAutoNavigation by viewModel.allowJourneyAutoNavigation.collectAsState()

    var hasLocationPermission by remember { mutableStateOf(locationManager.hasLocationPermission()) }
    var showLocationPermissionRequest by remember { mutableStateOf(!hasLocationPermission) }
    var showLocationAlert by remember { mutableStateOf(false) }

    // Handle location permission
    if (showLocationPermissionRequest) {
        RequestLocationPermission(
            onPermissionGranted = {
                hasLocationPermission = true
                showLocationPermissionRequest = false
                // Refresh location immediately after permission granted
                viewModel.onLocationPermissionGranted()
            },
            onPermissionDenied = {
                showLocationAlert = true
                showLocationPermissionRequest = false
            }
        )
    }

    // If permission is already granted on start, ensure observer is running
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            viewModel.onLocationPermissionGranted()
        }
    }

    // Bootstrap full stops catalog (download once or load from disk)
    LaunchedEffect(Unit) {
        viewModel.bootstrapStopsIfNeeded(context)
    }
    
    // Auto-navigation removed - using card-based UI like iOS

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD9731F)) // UrbanWay brand color
            .statusBarsPadding() // avoid drawing under the status bar
    ) {
        // Conditional content based on UI state
        when (uiState) {
            UIState.NORMAL -> {
                // Only show arrival cards in NORMAL state
                HomePage(
                    viewModel = viewModel,
                    onNavigateToRealtime = {
                        // TODO: Convert to UIState or remove if legacy
                    },
                    onNavigateToRouteDetail = {
                        // TODO: Convert to UIState or remove if legacy
                    }
                )
            }
            UIState.SEARCHING, UIState.EDITING_JOURNEY_FROM, UIState.EDITING_JOURNEY_TO -> {
                SearchScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
            UIState.ACCEPTMAPPLACE, UIState.ROUTE_DETAIL, UIState.JOURNEY_VIEW, UIState.JOURNEY_PLANNING, UIState.JOURNEY_RESULTS -> {
                // These states show their own specific cards or no card at all
            }
        }

        // Draggable Bottom Sheet (conditionally visible - not in search states)
        if (showBottomSheet && uiState != UIState.SEARCHING && uiState != UIState.EDITING_JOURNEY_FROM && uiState != UIState.EDITING_JOURNEY_TO) {
            DraggableBottomSheet(
                viewModel = viewModel,
                onSearchOpen = {
                    viewModel.openSearch()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }


        // iOS-style FAB with animations (chevron ↔ X) - ALWAYS visible
        if (true) { // FAB is never hidden
            val fabSize = 56.dp
            val haptics = LocalHapticFeedback.current
            
            // Determine FAB state based on UI state and bottom sheet
            val shouldShowX = isBottomSheetExpanded || uiState == UIState.JOURNEY_RESULTS || uiState == UIState.JOURNEY_PLANNING || uiState == UIState.ROUTE_DETAIL
            val shouldShowPulse = !isBottomSheetExpanded && uiState != UIState.JOURNEY_RESULTS && uiState != UIState.JOURNEY_PLANNING

            // Smooth icon transition animation
            val iconRotation by animateFloatAsState(
                targetValue = if (shouldShowX) 180f else 0f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
                label = "iconRotation"
            )
            
            // Subtle pulsing animation when collapsed (to invite interaction)
            val pulseScale = remember { Animatable(1f) }
            val pulseAlpha = remember { Animatable(0.2f) }
            
            LaunchedEffect(shouldShowPulse) {
                if (shouldShowPulse) {
                    while (true) {
                        pulseScale.animateTo(1.1f, animationSpec = tween(durationMillis = 1000))
                        pulseScale.snapTo(1f)
                    }
                }
            }

            LaunchedEffect(shouldShowPulse) {
                if (shouldShowPulse) {
                    while (true) {
                        pulseAlpha.animateTo(0f, animationSpec = tween(durationMillis = 1000))
                        pulseAlpha.snapTo(0.2f)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-106).dp)
                    .zIndex(5f)
            ) {
                // Subtle pulsing halo (only when should pulse)
                if (shouldShowPulse) {
                    Box(
                        modifier = Modifier
                            .size(fabSize)
                            .scale(pulseScale.value)
                            .background(
                                Color(0xFFD9731F).copy(alpha = pulseAlpha.value),
                                CircleShape
                            )
                    )
                }
                
                // Main FAB - iOS style
                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        when (uiState) {
                            UIState.JOURNEY_PLANNING -> {
                                // Same action as "Annulla" button in Journey Planner
                                viewModel.cancelJourneyPlanning()
                            }
                            UIState.JOURNEY_RESULTS -> {
                                // Same action as top-right X button in Journey Results
                                viewModel.backToJourneyPlanner()
                            }
                            UIState.ROUTE_DETAIL -> {
                                // Close route detail and return to normal state
                                viewModel.clearRouteDetail()
                                viewModel.returnToNormalState()
                            }
                            else -> {
                                // Normal bottom sheet toggle
                                viewModel.toggleBottomSheetExpanded()
                            }
                        }
                    },
                    modifier = Modifier.size(fabSize),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = Color(0xFF0B3D91) // Navy blue border
                    )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (shouldShowX) Icons.Filled.Close else Icons.Filled.ExpandLess,
                            contentDescription = if (shouldShowX) "Close" else "Expand",
                            tint = Color(0xFF333333),
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer { rotationZ = iconRotation }
                        )
                    }
                }
            }
        }
        
        // iOS-style place selection toolbar - shown when place is selected (matches iOS DraggableBottomSheet)
        val showPlaceSelectionToolbar = selectedPlace != null && uiState != UIState.SEARCHING && uiState != UIState.EDITING_JOURNEY_FROM && uiState != UIState.EDITING_JOURNEY_TO
        if (showPlaceSelectionToolbar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-106).dp)
                    .zIndex(3f)
            ) {
                FloatingActionBarWithCenterGap(
                    leftButtons = listOf(
                        ToolbarButton(
                            icon = Icons.Filled.Route,
                            contentDescription = "Percorso",
                            onClick = {
                                android.util.Log.d("TRANSITOAPP", "Showing journey planner card")
                                viewModel.startJourneyToSelectedPlace()
                                android.util.Log.d("TRANSITOAPP", "Journey planner card shown")
                            }
                        )
                    ),
                    rightButtons = listOf(
                        ToolbarButton(
                            icon = Icons.Filled.DirectionsWalk,
                            contentDescription = "A piedi",
                            onClick = {
                                android.util.Log.d("TRANSITOAPP", "Show walking directions")
                                // TODO: Implement walking directions
                                viewModel.showToast("Indicazioni a piedi - Feature in development")
                            }
                        )
                    )
                )
            }
        }

        // Route detail toolbar - shown when viewing route details
        val showRouteDetailToolbar = uiState == UIState.ROUTE_DETAIL && !showPlaceSelectionToolbar
        if (showRouteDetailToolbar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-106).dp)
                    .zIndex(3f)
            ) {
                FloatingActionBarWithCenterGap(
                    leftButtons = listOf(
                        ToolbarButton(
                            icon = Icons.Filled.Menu, // Menu icon for stops list
                            contentDescription = "Lista fermate",
                            onClick = {
                                android.util.Log.d("TRANSITOAPP", "Toggle route stops list")
                                viewModel.toggleRouteStopsList()
                            }
                        )
                    ),
                    rightButtons = listOf(
                        ToolbarButton(
                            icon = Icons.Filled.DirectionsWalk,
                            contentDescription = "A piedi",
                            onClick = {
                                android.util.Log.d("TRANSITOAPP", "Show walking directions from route")
                                viewModel.showToast("Indicazioni a piedi - Feature in development")
                            }
                        )
                    )
                )
            }
        }

        // Journey Planner Card - slides up when UIState.JOURNEY_PLANNING
        if (uiState == UIState.JOURNEY_PLANNING) {
            JourneyPlannerScreen(
                viewModel = viewModel,
                currentLocation = currentLocation,
                onSearchJourney = { fromAddress, fromCoords, toAddress, toCoords ->
                    viewModel.startJourneySearch(fromAddress, fromCoords, toAddress, toCoords)
                },
                onBack = { /* No navigation - handled by viewModel.cancelJourneyPlanning() */ }
            )
        }

        // Journey Results Card - slides up when UIState.JOURNEY_RESULTS
        if (uiState == UIState.JOURNEY_RESULTS) {
            JourneyResultsScreen(
                viewModel = viewModel,
                onJourneySelect = { journey ->
                    viewModel.showFixedJourneyOverlay(journey)
                },
                onBack = {
                    // Go back to journey planner
                    viewModel.backToJourneyPlanner()
                }
            )
        }

        // Global Toast Overlay
        toastMessage?.let { message ->
            ToastView(
                text = message,
                onDismiss = { viewModel.clearToast() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp)
            )
        }
    }

    // Location Permission Alert
    if (showLocationAlert) {
        AlertDialog(
            onDismissRequest = { showLocationAlert = false },
            title = { Text("Permesso Localizzazione") },
            text = { Text("Per utilizzare UrbanWay è necessario concedere il permesso di localizzazione.") },
            confirmButton = {
                TextButton(onClick = { 
                    showLocationAlert = false
                    showLocationPermissionRequest = true
                }) {
                    Text("Riprova")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationAlert = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    // Alert Message Dialog
    alertMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { /* Handle dismiss */ },
            title = { Text("Avviso") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { 
                    // Clear alert message in viewModel
                }) {
                    Text("OK")
                }
            }
        )
    }
}
