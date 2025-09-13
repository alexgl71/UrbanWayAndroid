package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.presentation.viewmodels.MainViewModel
import android.util.Log

enum class SearchCategory {
    INDIRIZZO,
    LUOGHI,
    LINEE,
    FERMATE
}

@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    // Use ViewModel's search state instead of local state
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearchLoading.collectAsState()
    var selectedCategory by remember { mutableStateOf(SearchCategory.INDIRIZZO) }
    
    // Debug logging
    LaunchedEffect(searchQuery) {
        Log.d("TRANSITOAPP", "SearchScreen - searchQuery changed: '$searchQuery'")
    }
    LaunchedEffect(searchResults) {
        Log.d("TRANSITOAPP", "SearchScreen - searchResults changed: ${searchResults.size} results")
        searchResults.forEach { result ->
            Log.d("TRANSITOAPP", "SearchScreen - result: ${result.title} - ${result.subtitle}")
        }
    }
    LaunchedEffect(isSearching) {
        Log.d("TRANSITOAPP", "SearchScreen - isSearching changed: $isSearching")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFD9731F)) // UrbanWay orange background
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Search bar (without cancel button)
        SearchBar(
            searchText = searchQuery,
            onSearchTextChange = { query -> 
                Log.d("TRANSITOAPP", "SearchBar - text changed to: '$query'")
                viewModel.updateSearchQuery(query)
            },
            onClear = { 
                Log.d("TRANSITOAPP", "SearchBar - clear button pressed")
                viewModel.updateSearchQuery("")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Cancel button below search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { viewModel.closeSearch() },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) {
                Text(
                    text = "Annulla",
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Show different content based on search state - matching iOS behavior
        // Debug logging
        LaunchedEffect(searchQuery) {
            android.util.Log.d("SEARCH_DEBUG", "SearchQuery changed: '$searchQuery' (isEmpty: ${searchQuery.isEmpty()})")
        }
        
        if (searchQuery.isEmpty()) {
            // Empty search state - show segmented buttons and categories
            android.util.Log.d("SEARCH_DEBUG", "Showing empty state UI")
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Segmented buttons
                SegmentedButtons(
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it },
                    modifier = Modifier.fillMaxWidth()
                )

                // Destination Suggestions (Popular Categories)
                DestinationSuggestionsCard(
                    destinationsData = null, // TODO: Pass actual data
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
        } else {
            // User is typing - show only search results (matching iOS)
            android.util.Log.d("SEARCH_DEBUG", "Showing search results UI for query: '$searchQuery'")
            
            Column {
                // Add a big red text to test if this branch is working
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Red)
                ) {
                    Text(
                        text = "TYPING MODE - Query: '$searchQuery'",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                SearchResultsCardView(
                    searchResults = searchResults,
                    isSearching = isSearching,
                    searchText = searchQuery,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Search input field
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.98f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = Color(0xFF0B3D91),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            
            BasicTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (searchText.isEmpty()) {
                        Text(
                            text = "Cerca luoghi, fermate, linee...",
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
                    onClick = onClear,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear",
                        tint = Color(0xFF0B3D91),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentedButtons(
    selectedCategory: SearchCategory,
    onCategorySelect: (SearchCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        SearchCategory.INDIRIZZO to "Indirizzo",
        SearchCategory.LUOGHI to "Luoghi", 
        SearchCategory.LINEE to "Linee",
        SearchCategory.FERMATE to "Fermate"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        categories.forEachIndexed { index, (category, label) ->
            val isSelected = selectedCategory == category
            val isFirst = index == 0
            val isLast = index == categories.size - 1
            
            val shape = when {
                isFirst -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                isLast -> RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                else -> RoundedCornerShape(0.dp)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .background(
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
                        shape = shape
                    )
                    .border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = Color.White.copy(alpha = 0.5f),
                        shape = shape
                    )
                    .clickable { onCategorySelect(category) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color(0xFF0B3D91) else Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}