package com.av.urbanway.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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

        // Fixed-position FAB to toggle sheet expansion (does not move with the sheet)
        if (showBottomSheet) {
            val fabSize = 64.dp
            val haptics = LocalHapticFeedback.current
            val navy = Color(0xFF0B3D91)
            val brand = Color(0xFFD9731F)
            val chevronFlipScaleX by animateFloatAsState(
                targetValue = if (isBottomSheetExpanded) -1f else 1f,
                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow),
                label = "chevronFlip"
            )
            // Attention-drawing halo animation (manual loop for broad Compose compatibility)
            val pulseScale = remember { Animatable(1f) }
            val pulseAlpha = remember { Animatable(0.35f) }
            LaunchedEffect(Unit) {
                while (true) {
                    pulseScale.animateTo(1.35f, animationSpec = tween(durationMillis = 1200))
                    pulseScale.snapTo(1f)
                }
            }
            LaunchedEffect(Unit) {
                while (true) {
                    pulseAlpha.animateTo(0f, animationSpec = tween(durationMillis = 1200))
                    pulseAlpha.snapTo(0.35f)
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-106).dp)
                    .zIndex(5f)
            ) {
                // Pulsing halo behind the FAB
                Box(
                    modifier = Modifier
                        .size(fabSize)
                        .graphicsLayer {
                            this.scaleX = pulseScale.value
                            this.scaleY = pulseScale.value
                            this.alpha = pulseAlpha.value
                        }
                        .background(brand.copy(alpha = 0.55f), CircleShape)
                )
                // Actual FAB with navy border
                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleBottomSheetExpanded()
                    },
                    color = brand,
                    shape = CircleShape,
                    tonalElevation = 10.dp,
                    shadowElevation = 18.dp,
                    modifier = Modifier
                        .size(fabSize)
                        .border(width = 3.dp, color = navy.copy(alpha = 0.45f), shape = CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBottomSheetExpanded) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
                            contentDescription = if (isBottomSheetExpanded) "Collapse" else "Expand",
                            tint = Color.White,
                            modifier = Modifier
                                .size(30.dp)
                                .graphicsLayer { this.scaleX = chevronFlipScaleX }
                        )
                    }
                }
            }
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
