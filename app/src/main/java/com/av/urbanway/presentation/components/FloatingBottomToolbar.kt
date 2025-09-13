package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ContextualFABButtons(
    showButtons: Boolean,
    onNotificationsClick: () -> Unit,
    onWalkingDirectionsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    
    // Animate buttons appearance with staggered delay
    val buttonScale by animateFloatAsState(
        targetValue = if (showButtons) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )
    
    val buttonAlpha by animateFloatAsState(
        targetValue = if (showButtons) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (showButtons) 300 else 200,
            delayMillis = if (showButtons) 100 else 0
        ),
        label = "buttonAlpha"
    )

    if (buttonScale > 0f || showButtons) {
        // Horizontal row toolbar like iOS - translucent background
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .scale(buttonScale)
                .graphicsLayer { alpha = buttonAlpha },
            color = Color.White.copy(alpha = 1f), // Fully opaque for maximum visibility
            shadowElevation = 20.dp, // Higher elevation for more separation
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = Color(0xFF0B3D91).copy(alpha = 0.15f) // More prominent navy border
            ),
            shape = RoundedCornerShape(32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left buttons
                ContextualButton(
                    icon = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNotificationsClick()
                    },
                    delay = 0
                )
                
                ContextualButton(
                    icon = Icons.Filled.DirectionsWalk,
                    contentDescription = "Walking Directions", 
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onWalkingDirectionsClick()
                    },
                    delay = 50
                )
                
                // Center close button (replacing FAB functionality)
                ContextualButton(
                    icon = Icons.Filled.Close,
                    contentDescription = "Close",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMenuClick() // Close action
                    },
                    delay = 100,
                    isCenter = true
                )
                
                // Right buttons
                ContextualButton(
                    icon = Icons.Filled.History,
                    contentDescription = "History",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onHistoryClick()
                    },
                    delay = 150
                )
                
                ContextualButton(
                    icon = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMenuClick()
                    },
                    delay = 200
                )
            }
        }
    }
}

@Composable
private fun ContextualButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    delay: Int,
    modifier: Modifier = Modifier,
    isCenter: Boolean = false
) {
    // Individual button animation with staggered delay
    val buttonScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring<Float>(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp) // Larger button size
            .scale(buttonScale)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isCenter) Color(0xFFD9731F) else Color(0xFF333333),
            modifier = Modifier.size(if (isCenter) 32.dp else 28.dp) // Larger icons
        )
    }
}

@Composable
fun DefaultFABButtons(
    showButtons: Boolean,
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    
    // Animate buttons appearance with staggered delay
    val buttonScale by animateFloatAsState(
        targetValue = if (showButtons) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "defaultButtonScale"
    )
    
    val buttonAlpha by animateFloatAsState(
        targetValue = if (showButtons) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (showButtons) 300 else 200,
            delayMillis = if (showButtons) 100 else 0
        ),
        label = "defaultButtonAlpha"
    )

    if (buttonScale > 0f || showButtons) {
        // Full width toolbar with buttons close to FAB center
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .scale(buttonScale)
                .graphicsLayer { alpha = buttonAlpha },
            color = Color.White.copy(alpha = 0.98f), // More opaque for better visibility
            shadowElevation = 16.dp, // More elevation for separation from map
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFF0B3D91).copy(alpha = 0.1f) // Subtle navy border
            ),
            shape = RoundedCornerShape(32.dp) // Consistent rounded borders
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Left: Settings button - positioned close to center
                DefaultContextualButton(
                    icon = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSettingsClick()
                    },
                    delay = 0,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = (-80).dp) // 80dp left from center
                )
                
                // Right: Search button - positioned close to center  
                DefaultContextualButton(
                    icon = Icons.Filled.Search,
                    contentDescription = "Search",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSearchClick()
                    },
                    delay = 50,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 80.dp) // 80dp right from center
                )
            }
        }
    }
}

@Composable
private fun DefaultContextualButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    delay: Int,
    modifier: Modifier = Modifier
) {
    // Individual button animation with staggered delay
    val buttonScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring<Float>(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "defaultButtonScale"
    )
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp) // Larger button size
            .scale(buttonScale)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xFF333333),
            modifier = Modifier.size(28.dp) // Larger icons
        )
    }
}

// iOS-matching FloatingToolbar with 3 buttons
@Composable
fun IOSFloatingToolbar(
    onMapTap: () -> Unit,
    onFavoritesTap: () -> Unit,
    onJourneyTap: () -> Unit,
    showButtons: Boolean,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    
    // Animate buttons appearance
    val buttonScale by animateFloatAsState(
        targetValue = if (showButtons) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "toolbarScale"
    )
    
    val buttonAlpha by animateFloatAsState(
        targetValue = if (showButtons) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (showButtons) 300 else 200,
            delayMillis = if (showButtons) 100 else 0
        ),
        label = "toolbarAlpha"
    )

    if (buttonScale > 0f || showButtons) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(25.dp))
                .scale(buttonScale)
                .graphicsLayer { alpha = buttonAlpha },
            color = Color.White.copy(alpha = 0.9f),
            shadowElevation = 10.dp,
            shape = RoundedCornerShape(25.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IOSToolbarButton(
                    icon = Icons.Filled.Map,
                    title = "Mappa",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMapTap()
                    }
                )
                
                IOSToolbarButton(
                    icon = Icons.Filled.Star,
                    title = "Preferiti",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFavoritesTap()
                    }
                )
                
                IOSToolbarButton(
                    icon = Icons.Filled.SwapHoriz,
                    title = "Percorso",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onJourneyTap()
                    }
                )
            }
        }
    }
}

@Composable
private fun IOSToolbarButton(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// iOS-style toolbar with back button
@Composable
fun IOSFloatingToolbarWithBack(
    onBackTap: () -> Unit,
    showButtons: Boolean,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    
    // Animate buttons appearance
    val buttonScale by animateFloatAsState(
        targetValue = if (showButtons) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "toolbarScale"
    )
    
    val buttonAlpha by animateFloatAsState(
        targetValue = if (showButtons) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (showButtons) 300 else 200,
            delayMillis = if (showButtons) 100 else 0
        ),
        label = "toolbarAlpha"
    )

    if (buttonScale > 0f || showButtons) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(25.dp))
                .scale(buttonScale)
                .graphicsLayer { alpha = buttonAlpha },
            color = Color.White.copy(alpha = 0.9f),
            shadowElevation = 10.dp,
            shape = RoundedCornerShape(25.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IOSToolbarButton(
                    icon = Icons.Filled.ArrowBack,
                    title = "Indietro",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBackTap()
                    }
                )
                
                IOSToolbarButton(
                    icon = Icons.Filled.Star,
                    title = "Preferiti",
                    onClick = { /* Disabled for now */ }
                )
                
                IOSToolbarButton(
                    icon = Icons.Filled.SwapHoriz,
                    title = "Percorso",
                    onClick = { /* Disabled for now */ }
                )
            }
        }
    }
}

// Legacy unified toolbar for backward compatibility
@Composable
fun UnifiedFloatingToolbar(
    buttons: List<ToolbarButton>,
    showButtons: Boolean,
    modifier: Modifier = Modifier
) {
    // For now, map to iOS-style if we have 2 buttons (for backward compatibility)
    if (buttons.size == 2) {
        IOSFloatingToolbar(
            onMapTap = buttons.getOrNull(0)?.onClick ?: {},
            onFavoritesTap = {},
            onJourneyTap = buttons.getOrNull(1)?.onClick ?: {},
            showButtons = showButtons,
            modifier = modifier
        )
    }
}

// Data class for toolbar buttons
data class ToolbarButton(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val isHighlighted: Boolean = false
)

@Composable
fun PlaceSelectionToolbar(
    onAcceptPlace: () -> Unit,
    onCloseSelection: () -> Unit,
    onShowDirections: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(25.dp)),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(25.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accept Place Button (Left)
            PlaceSelectionButton(
                icon = Icons.Filled.Check,
                title = "Seleziona",
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAcceptPlace()
                },
                isPrimary = true
            )
            
            // Close Button (Center)
            PlaceSelectionButton(
                icon = Icons.Filled.Close,
                title = "Chiudi",
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCloseSelection()
                }
            )
            
            // Directions Button (Right)
            PlaceSelectionButton(
                icon = Icons.Filled.Directions,
                title = "Mappa",
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onShowDirections()
                }
            )
        }
    }
}

@Composable
private fun PlaceSelectionButton(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) Color(0xFFD9731F) else Color.Transparent,
            contentColor = if (isPrimary) Color.White else MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(20.dp),
                tint = if (isPrimary) Color.White else Color(0xFF0B3D91)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isPrimary) Color.White else Color(0xFF0B3D91)
            )
        }
    }
}

@Composable
fun FloatingActionBarWithCenterGap(
    leftButtons: List<ToolbarButton>,
    rightButtons: List<ToolbarButton>,
    centerGap: Dp = 74.dp,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val barHeight = 50.dp
    val slotWidth = 54.dp
    
    val totalWidth = ((leftButtons.size + rightButtons.size) * slotWidth.value + centerGap.value + 16).dp
    
    Surface(
        modifier = modifier.width(totalWidth),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 14.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left buttons
            Row(horizontalArrangement = Arrangement.Start) {
                leftButtons.forEach { button ->
                    ActionBarButton(
                        button = button,
                        slotWidth = slotWidth,
                        barHeight = barHeight
                    )
                }
            }
            
            // Center gap for FAB
            Spacer(modifier = Modifier.width(centerGap))
            
            // Right buttons
            Row(horizontalArrangement = Arrangement.End) {
                rightButtons.forEach { button ->
                    ActionBarButton(
                        button = button,
                        slotWidth = slotWidth,
                        barHeight = barHeight
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionBarButton(
    button: ToolbarButton,
    slotWidth: Dp,
    barHeight: Dp,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    
    Button(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            button.onClick()
        },
        modifier = modifier
            .width(slotWidth)
            .height(barHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color(0xFF0B3D91)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = button.icon,
            contentDescription = button.contentDescription,
            modifier = Modifier.size(24.dp),
            tint = Color(0xFF0B3D91)
        )
    }
}

@Composable
private fun ToolbarButtonItem(
    button: ToolbarButton,
    delay: Int,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier
) {
    // Individual button animation with staggered delay
    val buttonScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring<Float>(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonItemScale"
    )
    
    IconButton(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            button.onClick()
        },
        modifier = modifier
            .size(56.dp) // Larger button size
            .scale(buttonScale)
    ) {
        Icon(
            imageVector = button.icon,
            contentDescription = button.contentDescription,
            tint = if (button.isHighlighted) Color(0xFFD9731F) else Color(0xFF333333),
            modifier = Modifier.size(if (button.isHighlighted) 32.dp else 28.dp) // Larger icons
        )
    }
}