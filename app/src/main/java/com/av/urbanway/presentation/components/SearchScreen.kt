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
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SearchCategory.INDIRIZZO) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFD9731F)) // UrbanWay orange background
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Search bar (without cancel button)
        SearchBar(
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            onClear = { searchText = "" },
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

        // Segmented buttons
        SegmentedButtons(
            selectedCategory = selectedCategory,
            onCategorySelect = { selectedCategory = it },
            modifier = Modifier.fillMaxWidth()
        )

        // Search results placeholder
        Spacer(modifier = Modifier.height(20.dp))
        
        // TODO: Add search results based on selected category and search text
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cerca ${selectedCategory.name.lowercase()} con: \"$searchText\"",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
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