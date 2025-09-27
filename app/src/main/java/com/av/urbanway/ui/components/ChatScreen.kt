package com.av.urbanway.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.av.urbanway.data.model.*
import com.av.urbanway.data.parser.QueryParser
import com.av.urbanway.data.repository.ChatRepository
import com.av.urbanway.data.service.ChatService
import com.av.urbanway.data.service.HardcodedDataService
import kotlinx.coroutines.launch

@Composable
fun ChatScreen() {
    // Remember instances to prevent recreation on recomposition
    val chatRepository = remember { ChatRepository() }
    val queryParser = remember { QueryParser() }
    val dataService = remember { HardcodedDataService() }
    val chatService = remember { ChatService(chatRepository, queryParser, dataService) }

    val messages by chatRepository.messages.collectAsState()
    var userInput by remember { mutableStateOf("") }
    var showModal by remember { mutableStateOf<ChatMessage?>(null) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Start with empty chat - let user type first

    // LazyList state for scrolling control
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)) // Dark background like screenshot
            .statusBarsPadding() // Handle status bar
            .imePadding() // Handle keyboard
    ) {
            // Header - exactly like screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App icon
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🚌",
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Assistente UrbanWay",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Online • 1 conversazioni",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // Chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatMessageItem(
                        message = message,
                        onViewDetails = {
                            showModal = message
                        }
                    )
                }
            }

            // Bottom input field - exactly like screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = {
                        Text(
                            "Chiedimi qualcosa sui trasporti...",
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(25.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (userInput.isNotBlank()) {
                                    // Dismiss keyboard first
                                    focusManager.clearFocus()
                                    scope.launch {
                                        chatService.handleUserInput(userInput)
                                        userInput = ""
                                        // Scroll to bottom after sending
                                        if (messages.isNotEmpty()) {
                                            listState.animateScrollToItem(messages.size)
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.Gray
                            )
                        }
                    }
                )
            }
        }

        // Show modal if needed
        showModal?.let { message ->
            when (message.queryType) {
                QueryType.NEARBY -> {
                    if (message.data is TransitData.NearbyData) {
                        NearbyModal(
                            data = message.data,
                            onDismiss = { showModal = null },
                            onRouteClick = { routeSelection ->
                                // Close modal first
                                showModal = null
                                // Handle route selection via ChatService with full route info
                                chatService.handleModalSelection(routeSelection, QueryType.NEARBY)
                            }
                        )
                    }
                }
                QueryType.ROUTEDETAIL -> {
                    if (message.data is TransitData.RouteDetailData) {
                        RouteDetailModal(
                            data = message.data,
                            onDismiss = { showModal = null },
                            onStopClick = { stop ->
                                // Close modal first
                                showModal = null
                                // Handle stop selection
                                chatService.handleStopSelection(stop)
                            }
                        )
                    }
                }
                QueryType.STOPDETAIL -> {
                    if (message.data is TransitData.StopDetailData) {
                        StopDetailModal(
                            data = message.data,
                            onDismiss = { showModal = null },
                            onRouteClick = { routeSelection ->
                                // Close modal first
                                showModal = null
                                // Handle route selection via ChatService
                                chatService.handleModalSelection(routeSelection, QueryType.STOPDETAIL)
                            }
                        )
                    }
                }
                QueryType.JOURNEY -> {
                    if (message.data is TransitData.JourneyData) {
                        JourneyModal(
                            data = message.data,
                            onDismiss = { showModal = null }
                        )
                    }
                }
                else -> {
                    // Handle other query types when implemented
                }
            }
        }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    onViewDetails: () -> Unit
) {
    when (message.type) {
        MessageType.USER -> {
            // User message bubble (right side)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 70.dp, end = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = message.content,
                        color = Color.Black,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }

        MessageType.AUTOGENERATED -> {
            // User interaction tag (right side)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 70.dp, end = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    color = Color(0xFF2C3E50),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = message.content,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }

        MessageType.BOT -> {
            // Bot response card
            when (message.queryType) {
                QueryType.NEARBY -> {
                    if (message.data is TransitData.NearbyData) {
                        NearbyBotCard(
                            data = message.data,
                            isCompact = message.isCompact,
                            onViewDetails = onViewDetails,
                            onTapCompact = onViewDetails  // Same action as "Vedi i dettagli"
                        )
                    }
                }
                QueryType.ROUTEDETAIL -> {
                    if (message.data is TransitData.RouteDetailData) {
                        RouteDetailBotCard(
                            data = message.data,
                            isCompact = message.isCompact,
                            onViewDetails = onViewDetails,
                            onTapCompact = onViewDetails  // Same action as "Vedi i dettagli"
                        )
                    }
                }
                QueryType.STOPDETAIL -> {
                    if (message.data is TransitData.StopDetailData) {
                        StopDetailBotCard(
                            data = message.data,
                            isCompact = message.isCompact,
                            onViewDetails = onViewDetails,
                            onTapCompact = onViewDetails  // Same action as "Vedi i dettagli"
                        )
                    }
                }
                QueryType.JOURNEY -> {
                    if (message.data is TransitData.JourneyData) {
                        JourneyBotCard(
                            data = message.data,
                            isCompact = message.isCompact,
                            onViewDetails = onViewDetails,
                            onTapCompact = onViewDetails  // Same action as "Vedi i dettagli"
                        )
                    }
                }
                else -> {
                    // Fallback for other query types
                    Text(
                        text = message.content,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}