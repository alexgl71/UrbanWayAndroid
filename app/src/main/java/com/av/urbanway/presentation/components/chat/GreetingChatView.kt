package com.av.urbanway.presentation.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.av.urbanway.data.models.RoutesSummaryResponse
import com.av.urbanway.presentation.viewmodels.MainViewModel
import com.av.urbanway.presentation.components.ChatChoiceChip
import com.av.urbanway.presentation.components.DestinationSuggestionsCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GreetingChatView(
    onArriviClick: () -> Unit,
    onMappaClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPlaceSelected: (com.av.urbanway.data.models.SearchResult) -> Unit, // pass full result
    destinationsData: RoutesSummaryResponse? = null,
    viewModel: MainViewModel? = null,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    // Right-aligned bot message with left padding of 40px
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White.copy(alpha = 0.95f),
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 4.dp // Chat bubble style
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Greeting text - bot message style
            Text(
                text = "Dove vuoi andare oggi?",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.2).sp
                ),
                color = Color.Black.copy(alpha = 0.87f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Interactive search bar
            InteractiveSearchBar(
                searchText = searchText,
                onSearchTextChange = {
                    searchText = it
                    Log.d("TRANSITOAPP", "GreetingChatView: searchText changed to '$it'")
                    // Trigger search when user types
                    if (viewModel != null && it.length >= 2) {
                        viewModel.updateSearchQuery(it)
                        Log.d("TRANSITOAPP", "GreetingChatView: triggering search for '$it'")
                    } else if (viewModel != null && it.isEmpty()) {
                        viewModel.updateSearchQuery("")
                        Log.d("TRANSITOAPP", "GreetingChatView: clearing search")
                    }
                },
                isSearchFocused = isSearchFocused,
                onFocusChanged = {
                    isSearchFocused = it
                    Log.d("TRANSITOAPP", "GreetingChatView: focus changed to $it")
                    if (!it && viewModel != null) {
                        // Clear search when losing focus
                        viewModel.updateSearchQuery("")
                    }
                },
                focusRequester = focusRequester,
                onSearchClick = onSearchClick,
                modifier = Modifier.fillMaxWidth()
            )

            // Annulla button - only show when search is focused
            if (isSearchFocused) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            // Clear search and dismiss keyboard
                            searchText = ""
                            if (viewModel != null) {
                                viewModel.updateSearchQuery("")
                            }
                            keyboardController?.hide()
                            focusRequester.freeFocus()
                            isSearchFocused = false
                        }
                    ) {
                        Text(
                            text = "Annulla",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF0B3D91)
                        )
                    }
                }
            } else {
                // Normal spacing when not focused
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Show DestinationSuggestionsCard when search is focused
            Log.d("TRANSITOAPP", "GreetingChatView: isSearchFocused=$isSearchFocused, viewModel=${viewModel != null}, searchText='$searchText'")

            if (isSearchFocused && viewModel != null) {
                Log.d("TRANSITOAPP", "GreetingChatView: Showing DestinationSuggestionsCard")
                DestinationSuggestionsCard(
                    destinationsData = destinationsData,
                    viewModel = viewModel,
                    onPlaceSelected = { result ->
                        // Update search text with selected place
                        searchText = result.title
                        // Clear search state and follow chat flow
                        viewModel.updateSearchQuery("")
                        keyboardController?.hide()
                        focusRequester.freeFocus()
                        isSearchFocused = false
                        // Notify parent component
                        onPlaceSelected(result)
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Log.d("TRANSITOAPP", "GreetingChatView: NOT showing DestinationSuggestionsCard")
            }

            // Only show "oppure:" text and choice chips when search is not focused
            if (!isSearchFocused) {
                // "oppure:" text
                Text(
                    text = "oppure:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Choice chips - only Arrivi and Mappa now
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ChatChoiceChip(
                        text = "🚌 Arrivi",
                        onClick = onArriviClick
                    )

                    ChatChoiceChip(
                        text = "🗺️ Mappa",
                        onClick = onMappaClick
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveSearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    isSearchFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        placeholder = {
            Text(
                text = "Cerca una destinazione...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (searchText.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onSearchTextChange("")
                        focusRequester.requestFocus()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                onFocusChanged(focusState.isFocused)
            },
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF0B3D91),
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.Gray.copy(alpha = 0.05f)
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearchClick()
            }
        )
    )
}
