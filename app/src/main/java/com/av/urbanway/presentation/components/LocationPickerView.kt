package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.data.models.Coordinates
import com.av.urbanway.data.models.Location
import com.av.urbanway.data.models.PlaceResult
import com.av.urbanway.presentation.viewmodels.MainViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerView(
    title: String,
    selectedLocation: Location?,
    onLocationSelected: (Location) -> Unit,
    onDismiss: () -> Unit,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<PlaceResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    
    val currentLocation by viewModel.currentLocation.collectAsState()
    val context = LocalContext.current

    // Trigger search when text changes (2+ characters)
    LaunchedEffect(searchText) {
        if (searchText.length >= 2) {
            isSearching = true
            delay(300) // Debounce search
            // TODO: Call PlacesService.search(searchText)
            // For now, mock some results
            searchResults = listOf(
                PlaceResult(
                    id = "1",
                    placeId = "place1",
                    title = "Via Roma",
                    subtitle = "Via Roma, 10, Torino",
                    coordinates = Coordinates(45.0703, 7.6869)
                ),
                PlaceResult(
                    id = "2", 
                    placeId = "place2",
                    title = "Piazza Castello",
                    subtitle = "Piazza Castello, Torino",
                    coordinates = Coordinates(45.0703, 7.6869)
                )
            )
            isSearching = false
        } else {
            searchResults = emptyList()
            isSearching = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFD9731F))
            .statusBarsPadding()
    ) {
        // Navigation Header
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Annulla",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Search Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                BasicTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (searchText.isEmpty()) {
                            Text(
                                text = "Cerca indirizzi, luoghi...",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            )
                        }
                        innerTextField()
                    }
                )
                
                if (searchText.isNotEmpty()) {
                    IconButton(
                        onClick = { searchText = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Content Area
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Current Location Button (if available)
                currentLocation?.let { location ->
                    if (location.coordinates.lat != 0.0) {
                        LocationResultCard(
                            icon = Icons.Filled.MyLocation,
                            title = "Posizione attuale",
                            subtitle = location.address,
                            onClick = {
                                onLocationSelected(location)
                            },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.Gray.copy(alpha = 0.2f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Search Results
                when {
                    isSearching -> {
                        // Loading State
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF0B3D91)
                                )
                                Text(
                                    text = "Ricerca in corso...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    
                    searchText.isEmpty() -> {
                        // Empty Search State
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Inizia a digitare per cercare",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    searchResults.isEmpty() -> {
                        // No Results State
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "No results",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Nessun risultato",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    
                    else -> {
                        // Results List
                        LazyColumn {
                            items(searchResults) { result ->
                                LocationResultCard(
                                    icon = Icons.Filled.LocationOn,
                                    title = result.title,
                                    subtitle = result.subtitle ?: "",
                                    onClick = {
                                        val location = Location(
                                            address = result.subtitle ?: result.title,
                                            coordinates = result.coordinates ?: Coordinates(0.0, 0.0),
                                            isManual = true
                                        )
                                        onLocationSelected(location)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                if (result != searchResults.last()) {
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = Color.Gray.copy(alpha = 0.2f),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationResultCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Background
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFD9731F).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFD9731F),
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Text Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.Black
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        
        // Chevron
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Select",
            tint = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}