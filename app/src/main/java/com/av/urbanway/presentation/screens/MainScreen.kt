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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.av.urbanway.RequestLocationPermission
import com.av.urbanway.data.local.LocationManager
import com.av.urbanway.data.models.UIState
import com.av.urbanway.presentation.components.DraggableBottomSheet
import com.av.urbanway.presentation.components.HomePage
import com.av.urbanway.presentation.components.ToastView
import com.av.urbanway.presentation.components.ContextualFABButtons
import com.av.urbanway.presentation.components.DefaultFABButtons
import com.av.urbanway.presentation.components.UnifiedFloatingToolbar
import com.av.urbanway.presentation.components.ToolbarButton
import com.av.urbanway.presentation.navigation.Screen
import com.av.urbanway.presentation.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel { MainViewModel.create(context) }
    val navController = rememberNavController()
    val locationManager = LocationManager.getInstance(context)
    
    val uiState by viewModel.uiState.collectAsState()
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val isBottomSheetExpanded by viewModel.isBottomSheetExpanded.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val alertMessage by viewModel.alertMessage.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD9731F)) // UrbanWay brand color
            .statusBarsPadding() // avoid drawing under the status bar
    ) {
        // Main Navigation Content
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Home.route) {
                HomePage(
                    viewModel = viewModel,
                    onNavigateToRealtime = {
                        navController.navigate(Screen.RealtimeArrivals.route)
                    },
                    onNavigateToRouteDetail = {
                        navController.navigate(Screen.RouteDetail.route)
                    },
                    onNavigateToJourneyPlanner = {
                        navController.navigate(Screen.JourneyPlanner.route)
                    },
                    onNavigateToJourneyResults = {
                        navController.navigate(Screen.JourneyResults.route)
                    }
                )
            }
            
            composable(Screen.RealtimeArrivals.route) {
                RealtimeArrivalsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onRouteSelect = { routeId, params ->
                        viewModel.handleRouteSelect(routeId, params)
                        navController.navigate(Screen.RouteDetail.route)
                    }
                )
            }
            
            composable(Screen.RouteDetail.route) {
                RouteDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.JourneyPlanner.route) {
                JourneyPlannerScreen(
                    viewModel = viewModel,
                    currentLocation = currentLocation,
                    onSearchJourney = { fromAddress, fromCoords, toAddress, toCoords ->
                        viewModel.startJourneySearch(fromAddress, fromCoords, toAddress, toCoords)
                        navController.navigate(Screen.JourneyResults.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.JourneyResults.route) {
                JourneyResultsScreen(
                    viewModel = viewModel,
                    onJourneySelect = { journey ->
                        viewModel.showFixedJourneyOverlay(journey)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.FullscreenMap.route) {
                FullscreenMapScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.FullscreenDestinations.route) {
                FullscreenDestinationsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Draggable Bottom Sheet (conditionally visible)
        if (showBottomSheet) {
            DraggableBottomSheet(
                viewModel = viewModel,
                onSearchOpen = {
                    viewModel.openSearch()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }


        // iOS-style FAB with animations (chevron ↔ X)
        if (showBottomSheet) {
            val fabSize = 56.dp
            val haptics = LocalHapticFeedback.current
            
            // Smooth icon transition animation
            val iconRotation by animateFloatAsState(
                targetValue = if (isBottomSheetExpanded) 180f else 0f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
                label = "iconRotation"
            )
            
            // Subtle pulsing animation when collapsed (to invite interaction)
            val pulseScale = remember { Animatable(1f) }
            val pulseAlpha = remember { Animatable(0.2f) }
            
            LaunchedEffect(isBottomSheetExpanded) {
                if (!isBottomSheetExpanded) {
                    while (true) {
                        pulseScale.animateTo(1.1f, animationSpec = tween(durationMillis = 1000))
                        pulseScale.snapTo(1f)
                    }
                }
            }
            
            LaunchedEffect(isBottomSheetExpanded) {
                if (!isBottomSheetExpanded) {
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
                // Subtle pulsing halo (only when collapsed)
                if (!isBottomSheetExpanded) {
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
                        viewModel.toggleBottomSheetExpanded()
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
                            imageVector = if (isBottomSheetExpanded) Icons.Filled.Close else Icons.Filled.ExpandLess,
                            contentDescription = if (isBottomSheetExpanded) "Close" else "Expand",
                            tint = Color(0xFF333333),
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer { rotationZ = iconRotation }
                        )
                    }
                }
            }
        }
        
        // Default toolbar (settings + search) - shown when bottom sheet is collapsed
        if (showBottomSheet && !isBottomSheetExpanded) {
            UnifiedFloatingToolbar(
                buttons = listOf(
                    ToolbarButton(
                        icon = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        onClick = { /* Handle settings */ }
                    ),
                    ToolbarButton(
                        icon = Icons.Filled.Search,
                        contentDescription = "Search", 
                        onClick = { viewModel.openSearch() }
                    )
                ),
                showButtons = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-100).dp)
                    .zIndex(4f)
            )
        }
        
        // Contextual toolbar (5 buttons) - shown when bottom sheet is expanded
        if (showBottomSheet && isBottomSheetExpanded) {
            UnifiedFloatingToolbar(
                buttons = listOf(
                    ToolbarButton(
                        icon = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        onClick = { /* Handle notifications */ }
                    ),
                    ToolbarButton(
                        icon = Icons.Filled.DirectionsWalk,
                        contentDescription = "Walking Directions",
                        onClick = { /* Handle walking directions */ }
                    ),
                    ToolbarButton(
                        icon = Icons.Filled.Close,
                        contentDescription = "Close",
                        onClick = { /* Handle close */ },
                        isHighlighted = true // Highlighted center button
                    ),
                    ToolbarButton(
                        icon = Icons.Filled.History,
                        contentDescription = "History",
                        onClick = { /* Handle history */ }
                    ),
                    ToolbarButton(
                        icon = Icons.Filled.Menu,
                        contentDescription = "Menu",
                        onClick = { /* Handle menu */ }
                    )
                ),
                showButtons = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-100).dp)
                    .zIndex(4f) // Behind the FAB
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
