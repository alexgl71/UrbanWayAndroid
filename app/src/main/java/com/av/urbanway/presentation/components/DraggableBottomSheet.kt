package com.av.urbanway.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.av.urbanway.data.local.GoogleMapsConfig
import com.av.urbanway.presentation.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraggableBottomSheet(
    viewModel: MainViewModel,
    onSearchOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val expanded by viewModel.isBottomSheetExpanded.collectAsState()

    if (!showBottomSheet) return

    val Navy = Color(0xFF0B3D91)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val targetHeight = if (expanded) maxHeight - 30.dp else 120.dp
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(targetHeight),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
            // Shared map as background (streets-only style)
            UrbanWayMapView(
                currentLocation = currentLocation.coordinates,
                mapConfig = GoogleMapsConfig.getInstance(context),
                modifier = Modifier.matchParentSize()
            )

            // Foreground content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Address pill under the top edge
                AddressPill(
                    address = currentLocation.address,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
                // Bottom-center FAB to expand/collapse sheet (iOS-style positioning)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    FloatingActionButton(
                        onClick = { viewModel.toggleBottomSheetExpanded() },
                        containerColor = Color.White,
                        contentColor = Navy,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 12.dp,
                            pressedElevation = 16.dp
                        ),
                        modifier = Modifier
                            .offset(y = (-32).dp)
                            .size(72.dp)
                            .border(width = 3.dp, color = Navy.copy(alpha = 0.4f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = Navy,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (expanded) {
                    // Quick destination categories (chips)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Ospedali", "Università", "Musei", "Shopping").forEach { label ->
                            AssistChip(
                                onClick = { onSearchOpen() },
                                label = { Text(label, fontWeight = FontWeight.Medium) }
                            )
                        }
                    }
                }

            }
            }
        }
    }
}

@Composable
fun AddressPill(address: String, modifier: Modifier = Modifier) {
    // Prefer showing street + street number when the number appears after a comma
    // e.g. "Via Roma, 5, Torino" -> "Via Roma 5"
    val streetLine = remember(address) {
        val parts = address.split(',')
        val first = parts.getOrNull(0)?.trim().orEmpty()
        val second = parts.getOrNull(1)?.trim().orEmpty()
        when {
            // If the second segment begins with a number (e.g., "5" or "5/A") include it
            second.firstOrNull()?.isDigit() == true ->
                listOf(first, second).filter { it.isNotEmpty() }.joinToString(" ")
            else -> first
        }.ifEmpty { address }
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .height(44.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Place, contentDescription = null, tint = Color(0xFF0B3D91))
            Spacer(Modifier.width(8.dp))
            Text(
                text = streetLine,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
    }
}
    
