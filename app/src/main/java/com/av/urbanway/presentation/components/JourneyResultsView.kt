package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.data.models.Coordinates
import com.av.urbanway.data.models.JourneyOption

data class JourneyResultsData(
    val fromAddress: String,
    val toAddress: String,
    val fromCoordinates: Coordinates,
    val toCoordinates: Coordinates,
    val journeys: List<JourneyOption>
)

@Composable
fun JourneyResultsView(
    journeyData: JourneyResultsData,
    isLoading: Boolean,
    onJourneySelect: (JourneyOption) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Debug logging matching iOS screenshot
    LaunchedEffect(Unit) {
        android.util.Log.d("TRANSIT", "📊 JourneyResults UI State:")
        android.util.Log.d("TRANSIT", "📊 IsLoading: $isLoading")
        android.util.Log.d("TRANSIT", "📊 Journeys count: ${journeyData.journeys.size}")
        if (journeyData.journeys.isNotEmpty()) {
            val first = journeyData.journeys.first()
            android.util.Log.d("TRANSIT", "📊 First journey: ${first.route1Id} -> ${first.route2Id ?: "nil"}")
        }
    }
    
    // Main card matching iOS screenshot
    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with blue diamond icon and close button (matching iOS)
            IOSHeaderSection(
                journeyData = journeyData,
                onBack = onBack
            )
            
            // Content
            when {
                isLoading -> LoadingView()
                journeyData.journeys.isEmpty() -> NoResultsView()
                else -> IOSJourneyResultsList(
                    journeys = journeyData.journeys,
                    onJourneySelect = onJourneySelect
                )
            }
        }
    }
}

// Header matching iOS screenshot exactly
@Composable
private fun IOSHeaderSection(
    journeyData: JourneyResultsData,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Top row with diamond icon, title and close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blue diamond icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Color(0xFF007AFF),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Navigation,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "Percorsi disponibili",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            
            // Close button
            IconButton(onClick = onBack) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // From and To addresses
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                Text(
                    text = "DA:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.width(40.dp)
                )
                Text(
                    text = journeyData.fromAddress,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            
            Row {
                Text(
                    text = "A:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.width(40.dp)
                )
                Text(
                    text = journeyData.toAddress,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}

// iOS-style journey results list
@Composable
private fun IOSJourneyResultsList(
    journeys: List<JourneyOption>,
    onJourneySelect: (JourneyOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedJourneys = groupAndOptimizeJourneys(journeys)
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Direct journeys section
        if (groupedJourneys.directJourneys.isNotEmpty()) {
            item {
                IOSSectionHeader(
                    title = "Diretto",
                    color = Color(0xFF4CAF50) // Green
                )
            }
            
            items(groupedJourneys.directJourneys) { journey ->
                IOSDirectJourneyItem(
                    journey = journey,
                    onClick = { onJourneySelect(journey) }
                )
                
                // Add divider except for last item
                if (journey != groupedJourneys.directJourneys.last()) {
                    HorizontalDivider(
                        color = Color.Gray.copy(alpha = 0.2f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 64.dp) // Start after the route circle
                    )
                }
            }
            
            if (groupedJourneys.transferJourneys.isNotEmpty()) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
        
        // Transfer journeys section  
        if (groupedJourneys.transferJourneys.isNotEmpty()) {
            item {
                IOSSectionHeader(
                    title = "Cambio",
                    color = Color(0xFFFF9500) // Orange
                )
            }
            
            items(groupedJourneys.transferJourneys) { journey ->
                IOSTransferJourneyItem(
                    journey = journey,
                    onClick = { onJourneySelect(journey) }
                )
                
                // Add divider except for last item
                if (journey != groupedJourneys.transferJourneys.last()) {
                    HorizontalDivider(
                        color = Color.Gray.copy(alpha = 0.2f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 112.dp) // Start after the two route circles
                    )
                }
            }
        }
    }
}

// Section header matching iOS
@Composable
private fun IOSSectionHeader(
    title: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

// Direct journey item matching iOS screenshot
@Composable
private fun IOSDirectJourneyItem(
    journey: JourneyOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Route circle
        IOSRouteCircle(routeId = journey.route1Id)
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Journey details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${journey.totalStops} fermate",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsWalk,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.Gray
                )
                Text(
                    text = "${journey.walkingTimeMinutes}' a piedi",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        
        // Time with beige background
        Box(
            modifier = Modifier
                .background(
                    Color(0xFFF5F5DC), // Beige color
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${journey.totalJourneyMinutes}'",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}

// Transfer journey item matching iOS screenshot
@Composable
private fun IOSTransferJourneyItem(
    journey: JourneyOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Route circles with arrow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IOSRouteCircle(routeId = journey.route1Id)
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color.Gray
            )
            
            journey.route2Id?.let { route2Id ->
                IOSRouteCircle(routeId = route2Id)
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Journey details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${journey.totalStops} fermate",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsWalk,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.Gray
                )
                Text(
                    text = "${journey.walkingTimeMinutes}' a piedi",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        
        // Time with beige background
        Box(
            modifier = Modifier
                .background(
                    Color(0xFFF5F5DC), // Beige color
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${journey.totalJourneyMinutes}'",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}

// Route circle matching iOS style
@Composable
private fun IOSRouteCircle(
    routeId: String,
    modifier: Modifier = Modifier
) {
    // Remove 'U' suffix if present
    val displayRouteId = if (routeId.endsWith("U")) {
        routeId.dropLast(1)
    } else {
        routeId
    }
    
    Box(
        modifier = modifier
            .size(48.dp)
            .border(1.dp, Color(0xFF007AFF), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayRouteId,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF007AFF),
            textAlign = TextAlign.Center
        )
    }
}

// Loading view
@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp,
            color = Color(0xFF007AFF)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Ricerca percorsi in corso...",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

// No results view
@Composable
private fun NoResultsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.DirectionsBus,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Nessun percorso trovato",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Prova a modificare i punti di partenza o arrivo",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

// Journey grouping logic
data class GroupedJourneyResults(
    val directJourneys: List<JourneyOption>,
    val transferJourneys: List<JourneyOption>
)

private fun groupAndOptimizeJourneys(journeys: List<JourneyOption>): GroupedJourneyResults {
    android.util.Log.d("TRANSIT", "🔍 Smart grouping ${journeys.size} journeys into direct vs transfer")
    
    val directJourneys = journeys
        .filter { it.isDirect == 1 }
        .groupBy { it.route1Id }
        .mapNotNull { (routeId, groupedJourneys) ->
            val best = groupedJourneys.minByOrNull { it.totalJourneyMinutes }
            if (best != null) {
                android.util.Log.d("TRANSIT", "🔍 Direct route $routeId: ${best.totalJourneyMinutes}min")
            }
            best
        }
        .sortedBy { it.totalJourneyMinutes }
    
    val transferJourneys = journeys
        .filter { it.isDirect == 0 }
        .groupBy { journey ->
            val routes = listOfNotNull(journey.route1Id, journey.route2Id).sorted()
            routes.joinToString(" + ")
        }
        .mapNotNull { (combination, groupedJourneys) ->
            val best = groupedJourneys.minByOrNull { it.totalJourneyMinutes }
            if (best != null) {
                android.util.Log.d("TRANSIT", "🔍 Transfer combination $combination: ${best.totalJourneyMinutes}min")
            }
            best
        }
        .sortedBy { it.totalJourneyMinutes }
    
    android.util.Log.d("TRANSIT", "🎯 Final results: ${directJourneys.size} direct + ${transferJourneys.size} transfer")
    
    return GroupedJourneyResults(
        directJourneys = directJourneys,
        transferJourneys = transferJourneys
    )
}