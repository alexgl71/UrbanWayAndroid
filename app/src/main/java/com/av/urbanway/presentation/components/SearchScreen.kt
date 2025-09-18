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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    
    // Focus management
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }
    
    // Debug logging
    LaunchedEffect(searchQuery) {
        Log.d("TRANSITO", "SearchScreen - searchQuery changed: '$searchQuery'")
    }
    LaunchedEffect(searchResults) {
        Log.d("TRANSITO", "SearchScreen - searchResults changed: ${searchResults.size} results")
        searchResults.forEach { result ->
            Log.d("TRANSITO", "SearchScreen - result: ${result.title} - ${result.subtitle}")
        }
    }
    LaunchedEffect(isSearching) {
        Log.d("TRANSITO", "SearchScreen - isSearching changed: $isSearching")
    }
    
    // Auto-focus search bar when screen appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFD9731F)) // UrbanWay orange background
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Unified search bar with segmented buttons
        SearchBarWithSegments(
            searchText = searchQuery,
            selectedCategory = selectedCategory,
            showSegments = isSearchFocused || searchQuery.isNotEmpty(),
            onSearchTextChange = { query -> 
                Log.d("TRANSITO", "SearchBar - text changed to: '$query'")
                Log.d("TRANSITO", "SearchBar - about to call viewModel.updateSearchQuery")
                viewModel.updateSearchQuery(query)
                Log.d("TRANSITO", "SearchBar - viewModel.updateSearchQuery called successfully")
            },
            onClear = { 
                Log.d("TRANSITO", "SearchBar - clear button pressed")
                viewModel.updateSearchQuery("")
            },
            onFocusChange = { focused ->
                Log.d("TRANSITO", "SearchBar - focus changed to: $focused")
                isSearchFocused = focused
            },
            onCategorySelect = { selectedCategory = it },
            focusRequester = focusRequester,
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
            android.util.Log.d("TRANSITO", "SearchQuery changed: '$searchQuery' (isEmpty: ${searchQuery.isEmpty()})")
        }
        
        // Also log on every recomposition to see current state
        android.util.Log.d("TRANSITO", "Current searchQuery: '$searchQuery' (${searchQuery.length} chars)")
        android.util.Log.d("TRANSITO", "Current isSearchFocused: $isSearchFocused")
        
        if (searchQuery.isEmpty() && !isSearchFocused) {
            // Empty search state - show segmented buttons and categories
            android.util.Log.d("TRANSITO", "Showing empty state UI")
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Journey Planner button - direct access to journey planning
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openJourneyPlanner() },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Journey Planner",
                            tint = Color(0xFFD9731F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Pianifica viaggio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Text(
                                text = "Trova il percorso migliore",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }

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
                    onPlaceSelected = { placeName ->
                        // Handle place selection in search screen
                        viewModel.showToast("Selected: $placeName")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
        } else {
            // User is focused/typing - show search results (segmented buttons now in unified search bar)
            android.util.Log.d("TRANSITO", "Showing search results UI for query: '$searchQuery', focused: $isSearchFocused")
            
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

@Composable
private fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onClear: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    focusRequester: FocusRequester,
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
                onValueChange = { newText ->
                    android.util.Log.d("TRANSITO", "BasicTextField onValueChange: '$newText'")
                    android.util.Log.d("TRANSITO", "BasicTextField - about to call onSearchTextChange")
                    onSearchTextChange(newText)
                    android.util.Log.d("TRANSITO", "BasicTextField - onSearchTextChange called")
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        android.util.Log.d("TRANSITO", "BasicTextField focus changed - isFocused: ${focusState.isFocused}")
                        onFocusChange(focusState.isFocused)
                    },
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
private fun SearchBarWithSegments(
    searchText: String,
    selectedCategory: SearchCategory,
    showSegments: Boolean,
    onSearchTextChange: (String) -> Unit,
    onClear: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onCategorySelect: (SearchCategory) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.98f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Search input field (same as before)
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
                    onValueChange = { newText ->
                        android.util.Log.d("TRANSITO", "BasicTextField onValueChange: '$newText'")
                        android.util.Log.d("TRANSITO", "BasicTextField - about to call onSearchTextChange")
                        onSearchTextChange(newText)
                        android.util.Log.d("TRANSITO", "BasicTextField - onSearchTextChange called")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            android.util.Log.d("TRANSITO", "BasicTextField focus changed - isFocused: ${focusState.isFocused}")
                            onFocusChange(focusState.isFocused)
                        },
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
            
            // Segmented buttons (shown when focused/typing)
            if (showSegments) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = Color.Gray.copy(alpha = 0.2f)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    val categories = listOf(
                        SearchCategory.INDIRIZZO to "Indirizzo",
                        SearchCategory.LUOGHI to "Luoghi", 
                        SearchCategory.LINEE to "Linee",
                        SearchCategory.FERMATE to "Fermate"
                    )

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
                                    color = if (isSelected) Color(0xFF0B3D91) else Color.Gray.copy(alpha = 0.1f),
                                    shape = shape
                                )
                                .border(
                                    width = if (isSelected) 0.dp else 1.dp,
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    shape = shape
                                )
                                .clickable { onCategorySelect(category) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFF0B3D91),
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
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