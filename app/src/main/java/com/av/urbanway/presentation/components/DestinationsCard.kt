package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.data.models.RoutesSummaryResponse
import com.av.urbanway.presentation.viewmodels.MainViewModel

// DestinationsCard aligned 1:1 with iOS DestinationsCard.swift
@Composable
fun DestinationsCard(
    viewModel: MainViewModel,
    destinationsData: RoutesSummaryResponse?,
    onTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                spotColor = Color.Black.copy(alpha = 0.08f),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            )
            .clickable { onTap() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header band flush to card edges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.GridView,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "DESTINAZIONI PRINCIPALI",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            // Content with inner padding
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Categories grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.padding(start = -8.dp, end = -16.dp, top = 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(extractDestinationTypes(destinationsData).take(9)) { item ->
                        DestinationTypeChip(
                            item = item,
                            onTap = {
                                // Show category on map using existing setCategoryForHome method
                                viewModel.setCategoryForHome(item.type)
                            }
                        )
                    }
                }
            }
        }
    }
}

// MARK: - Data mapping (iOS parity)
private fun extractDestinationTypes(destinationsData: RoutesSummaryResponse?): List<MiniDestinationType> {
    if (destinationsData == null) return defaultTypes()
    
    val types = mutableListOf<MiniDestinationType>()
    
    // Prefer destinationsByType if available
    destinationsData.destinationsByType?.forEach { (key, list) ->
        if (list.isNotEmpty() && key.lowercase() != "capolinea") {
            types.add(
                MiniDestinationType(
                    type = key,
                    destinations = list,
                    icon = getIconForType(key),
                    displayName = getDisplayNameForType(key)
                )
            )
        }
    }
    
    if (types.isEmpty()) {
        // Fallback to allDestinations parsing in Android style: "culturale: A, B | ospedale: C"
        destinationsData.allDestinations?.forEach { entry ->
            val groups = entry.split(" | ")
            groups.forEach { g ->
                val colonIndex = g.indexOf(": ")
                if (colonIndex != -1) {
                    val type = g.substring(0, colonIndex).trim()
                    val list = g.substring(colonIndex + 2).split(",").map { it.trim() }
                    if (list.isNotEmpty()) {
                        types.add(
                            MiniDestinationType(
                                type = type,
                                destinations = list,
                                icon = getIconForType(type),
                                displayName = getDisplayNameForType(type)
                            )
                        )
                    }
                }
            }
        }
    }
    
    // Deduplicate, filter, sort by importance like iOS
    val unique = types.groupBy { it.type.lowercase() }.values.mapNotNull { it.firstOrNull() }
    return unique.filter { it.type.lowercase() != "capolinea" }.sortedBy { getImportance(it.type) }
}

private fun getIconForType(type: String): ImageVector {
    return when (type.lowercase()) {
        "culturale", "monumento" -> Icons.Filled.AccountBalance
        "ospedale" -> Icons.Filled.MedicalServices
        "università", "universita" -> Icons.Filled.School
        "commerciale", "mercato" -> Icons.Filled.ShoppingCart
        "parco" -> Icons.Filled.Park
        "stazione ferroviaria", "stazione" -> Icons.Filled.Train
        "giudiziario", "tribunale" -> Icons.Filled.Gavel
        "museo", "musei" -> Icons.Filled.AccountBalance
        "chiesa", "religioso" -> Icons.Filled.Church
        "sport", "stadio" -> Icons.Filled.SportsFootball
        "teatro", "cinema" -> Icons.Filled.TheaterComedy
        "hotel", "albergo" -> Icons.Filled.Hotel
        "aeroporto" -> Icons.Filled.Flight
        else -> Icons.Filled.LocationOn
    }
}

private fun getDisplayNameForType(type: String): String {
    return when (type.lowercase()) {
        "culturale" -> "Cultura"
        "ospedale" -> "Ospedali"
        "università", "universita" -> "Università"
        "commerciale" -> "Shopping"
        "mercato" -> "Mercati"
        "parco" -> "Parchi"
        "stazione ferroviaria", "stazione" -> "Stazioni"
        "giudiziario", "tribunale" -> "Tribunali"
        "monumento" -> "Monumenti"
        "museo", "musei" -> "Musei"
        "chiesa", "religioso" -> "Chiese"
        "sport", "stadio" -> "Sport"
        "teatro", "cinema" -> "Teatro"
        "hotel", "albergo" -> "Hotel"
        "aeroporto" -> "Aeroporti"
        else -> type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

private fun getImportance(type: String): Int {
    return when (type.lowercase()) {
        "stazione ferroviaria", "stazione" -> 1
        "ospedale" -> 2
        "università", "universita" -> 3
        "culturale", "monumento" -> 4
        "commerciale", "mercato" -> 5
        "parco" -> 6
        "giudiziario", "tribunale" -> 7
        else -> 99
    }
}

private fun defaultTypes(): List<MiniDestinationType> {
    return listOf(
        MiniDestinationType("stazione", listOf("1"), Icons.Filled.Train, "Stazioni"),
        MiniDestinationType("università", listOf("1"), Icons.Filled.School, "Università"),
        MiniDestinationType("culturale", listOf("3"), Icons.Filled.AccountBalance, "Cultura"),
        MiniDestinationType("commerciale", listOf("1"), Icons.Filled.ShoppingCart, "Shopping"),
        MiniDestinationType("ristoranti", listOf("2"), Icons.Filled.Restaurant, "Rist.")
    )
}

private data class MiniDestinationType(
    val type: String,
    val destinations: List<String>,
    val icon: ImageVector,
    val displayName: String
)

@Composable
private fun DestinationTypeChip(
    item: MiniDestinationType,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onTap,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 58.dp, height = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = item.displayName,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(72.dp),
                maxLines = 1
            )
        }
    }
}