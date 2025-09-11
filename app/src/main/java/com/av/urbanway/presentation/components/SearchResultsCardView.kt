package com.av.urbanway.presentation.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.data.models.SearchResult
import com.av.urbanway.presentation.viewmodels.MainViewModel

@Composable
fun SearchResultsCardView(
    searchResults: List<SearchResult>,
    isSearching: Boolean,
    searchText: String,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.98f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        when {
            isSearching -> {
                // Loading State
                SearchLoadingState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
            
            searchText.isEmpty() -> {
                // Empty Search State
                SearchEmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
            
            searchResults.isEmpty() -> {
                // No Results State
                SearchNoResultsState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
            
            else -> {
                // Results List
                SearchResultsList(
                    results = searchResults,
                    onResultClick = { result ->
                        // TODO: Add selectSearchResult method to MainViewModel
                        // viewModel.selectSearchResult(result)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SearchLoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = Color(0xFF0B3D91),
            strokeWidth = 3.dp
        )
        Text(
            text = "Ricerca in corso...",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun SearchEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Inizia a digitare per cercare",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = Color.Gray
        )
    }
}

@Composable
private fun SearchNoResultsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "No results",
            modifier = Modifier.size(48.dp),
            tint = Color.Gray.copy(alpha = 0.6f)
        )
        Text(
            text = "Nessun risultato trovato",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = Color.Gray
        )
    }
}

@Composable
private fun SearchResultsList(
    results: List<SearchResult>,
    onResultClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(results) { result ->
            SearchResultItem(
                result = result,
                onClick = { 
                    // Animate selection with easeInOut duration 0.3
                    onResultClick(result)
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Divider between items (aligned under text, not icon)
            if (result != results.last()) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                    thickness = 1.dp,
                    color = Color.Gray.copy(alpha = 0.12f)
                )
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "itemAlpha"
    )

    Row(
        modifier = modifier
            .clickable { 
                isPressed = true
                onClick()
            }
            .alpha(alpha)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Location Icon Background (Orange circle)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(red = 0.85f, green = 0.45f, blue = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        
        // Text Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = Color.Black
            )
            
            result.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = Color.Gray
                )
            }
        }
        
        // Chevron Right Arrow
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Select",
            tint = Color.Gray.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(300)
            isPressed = false
        }
    }
}