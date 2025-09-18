package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
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
import android.util.Log
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
    onPlaceSelected: (com.av.urbanway.data.models.SearchResult) -> Unit, // pass full result
    modifier: Modifier = Modifier
) {
    Log.d("TRANSITOAPP", "DestinationSuggestionsCard: Composing suggestions card")
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Get search results from viewModel
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

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
            Log.d("TRANSITOAPP", "DestinationSuggestionsCard: searchResults.size=${searchResults.size}, searchQuery='$searchQuery'")

            if (searchResults.isNotEmpty()) {
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
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = Color(0xFF0B3D91),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Risultati ricerca",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = Color.Gray
                    )
                }

                // Search Results List
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    searchResults.take(5).forEach { result ->
                        SearchResultItem(
                            searchResult = result,
                            onClick = {
                                // Handle result selection
                                Log.d("TRANSITOAPP", "DestinationSuggestionsCard: Selected result: ${result.title}")
                                onPlaceSelected(result)
                            }
                        )
                    }
                }
            } else {
                // Show message when no results
                Text(
                    text = "Inizia a digitare per cercare una destinazione...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    searchResult: com.av.urbanway.data.models.SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Location Icon
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = Color(0xFF0B3D91),
            modifier = Modifier.size(20.dp)
        )

        // Place Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = searchResult.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                color = Color.Black
            )

            searchResult.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp
                    ),
                    color = Color.Gray
                )
            }
        }
    }
}
