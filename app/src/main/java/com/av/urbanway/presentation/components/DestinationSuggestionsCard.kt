package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.data.models.RoutesSummaryResponse
import com.av.urbanway.presentation.viewmodels.MainViewModel

data class DestinationType(
    val type: String,
    val destinations: List<String>,
    val icon: ImageVector,
    val displayName: String
)

@Composable
fun DestinationSuggestionsCard(
    destinationsData: RoutesSummaryResponse?,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    
    // Mock destination categories (replace with actual data from destinationsData)
    val categories = remember {
        listOf(
            DestinationType(
                type = "hospital",
                destinations = listOf("Ospedale Molinette", "Ospedale San Giovanni"),
                icon = Icons.Filled.LocalHospital,
                displayName = "Ospedali"
            ),
            DestinationType(
                type = "university",
                destinations = listOf("Università di Torino", "Politecnico"),
                icon = Icons.Filled.School,
                displayName = "Università"
            ),
            DestinationType(
                type = "museum",
                destinations = listOf("Museo Egizio", "Palazzo Reale"),
                icon = Icons.Filled.Museum,
                displayName = "Musei"
            ),
            DestinationType(
                type = "shopping",
                destinations = listOf("Via Roma", "Porta Nuova"),
                icon = Icons.Filled.ShoppingCart,
                displayName = "Shopping"
            ),
            DestinationType(
                type = "restaurant",
                destinations = listOf("Quadrilatero Romano", "San Salvario"),
                icon = Icons.Filled.Restaurant,
                displayName = "Ristoranti"
            ),
            DestinationType(
                type = "business",
                destinations = listOf("Centro Direzionale", "Lingotto"),
                icon = Icons.Filled.Business,
                displayName = "Business"
            )
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp
        )
    ) {
        Column {
            // Header Band
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.05f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFD9731F),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Categorie popolari",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = Color.Gray
                )
            }

            // Horizontal Scrollable Categories
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.forEach { category ->
                    CategoryChip(
                        category = category,
                        onClick = {
                            // Dismiss keyboard and clear search
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            
                            // TODO: Call PlacesService.stopAutocomplete()
                            
                            // Set category for home
                            viewModel.setCategoryForHome(category.type)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: DestinationType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.Gray.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category Icon
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = Color(0xFF0B3D91),
                modifier = Modifier.size(18.dp)
            )
            
            // Category Label
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = Color.Black
            )
        }
    }
}