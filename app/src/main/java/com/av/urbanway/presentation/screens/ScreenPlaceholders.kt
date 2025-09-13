package com.av.urbanway.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.av.urbanway.data.models.*
import com.av.urbanway.presentation.components.RealTimeArrivalsCard
import com.av.urbanway.presentation.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealtimeArrivalsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onRouteSelect: (String, Map<String, Any>) -> Unit
) {
    val realTimeArrivalsData = viewModel.realTimeArrivalsData
    val targetHighlightStopId by viewModel.targetHighlightStopId.collectAsState()
    val pinned by viewModel.pinnedArrivals.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Arrivi in Tempo Reale") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD9731F),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
                .background(Color(0xFFD9731F))
        ) {
            RealTimeArrivalsCard(
                arrivalsData = realTimeArrivalsData,
                pinnedArrivalsIds = pinned.map { "${it.routeId}_${it.destination}_${it.stopId}" }.toSet(),
                highlightStopId = targetHighlightStopId,
                onRouteSelect = onRouteSelect,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val routeDetailData by viewModel.routeDetailData.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dettaglio Linea") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD9731F),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Dettaglio Linea",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedRoute?.let { route ->
                        Text(
                            text = "Linea: $route",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFD9731F)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = "Questa schermata mostrerà i dettagli della linea selezionata con fermate e orari",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyPlannerScreen(
    viewModel: MainViewModel,
    currentLocation: Location?,
    onSearchJourney: (String, Coordinates, String, Coordinates) -> Unit,
    onBack: () -> Unit
) {
    val startLocation by viewModel.startLocation.collectAsState()
    val endLocation by viewModel.endLocation.collectAsState()
    
    // Orange background (no Scaffold - copying iOS exactly)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD9731F))
            .padding(16.dp)
    ) {
        // Header with "Pianifica viaggio" and "Annulla" button (copying iOS)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pianifica viaggio",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            
            TextButton(onClick = onBack) {
                Text(
                    text = "Annulla",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Single white card with both fields (copying iOS JourneyComposerView exactly)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                // FROM row (copying iOS line 477-524)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Da:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier.width(32.dp)
                    )
                    
                    Button(
                        onClick = { 
                            viewModel.setStartToCurrentLocation() 
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = startLocation?.address ?: "VIA GIOVANNI CARLO CAVALLI, 38",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                modifier = Modifier.weight(1f),
                                maxLines = 2
                            )
                            
                            Text(
                                text = "Cambia",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Blue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                // Divider (copying iOS line 544)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = Color.LightGray
                )
                
                // TO row (copying iOS line 547-595)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "A:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier.width(32.dp)
                    )
                    
                    Button(
                        onClick = { 
                            // Handle "Cambia" button for destination
                            viewModel.showToast("Cambia destinazione - Feature in development")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = endLocation?.address ?: "Piazza Castello",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                modifier = Modifier.weight(1f),
                                maxLines = 2
                            )
                            
                            Text(
                                text = "Cambia",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Blue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // "Pianifica" button (copying iOS line 605-618)
        if (startLocation != null && endLocation != null) {
            Button(
                onClick = {
                    onSearchJourney(
                        startLocation!!.address,
                        startLocation!!.coordinates,
                        endLocation!!.address,
                        endLocation!!.coordinates
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD9731F)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Route,
                        contentDescription = "Pianifica",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pianifica",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyResultsScreen(
    viewModel: MainViewModel,
    onJourneySelect: (JourneyOption) -> Unit,
    onBack: () -> Unit
) {
    val journeys by viewModel.journeys.collectAsState()
    val isLoading by viewModel.isLoadingJourneys.collectAsState()
    val startLocation by viewModel.startLocation.collectAsState()
    val endLocation by viewModel.endLocation.collectAsState()
    
    // Create journey data for iOS-ported component
    val journeyData = if (startLocation != null && endLocation != null) {
        com.av.urbanway.presentation.components.JourneyResultsData(
            fromAddress = startLocation!!.address,
            toAddress = endLocation!!.address,
            fromCoordinates = startLocation!!.coordinates,
            toCoordinates = endLocation!!.coordinates,
            journeys = journeys
        )
    } else {
        com.av.urbanway.presentation.components.JourneyResultsData(
            fromAddress = "",
            toAddress = "",
            fromCoordinates = Coordinates(0.0, 0.0),
            toCoordinates = Coordinates(0.0, 0.0),
            journeys = emptyList()
        )
    }
    
    // Use the iOS-ported JourneyResultsView component
    com.av.urbanway.presentation.components.JourneyResultsView(
        journeyData = journeyData,
        isLoading = isLoading,
        onJourneySelect = onJourneySelect,
        onBack = onBack,
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenMapScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mappa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD9731F),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        // Use the iOS-ported FullscreenMapCardView component
        com.av.urbanway.presentation.components.FullscreenMapCardView(
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenDestinationsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Destinazioni") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD9731F),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Lista destinazioni",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
    }
}
