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

// Unified parametric toolbar component
@Composable
fun UnifiedFloatingToolbar(
    buttons: List<ToolbarButton>,
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
        // Full-width toolbar with center space reserved for FAB
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
            when (buttons.size) {
                2 -> {
                    // For 2 buttons: spread them around center FAB space
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left side
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            ToolbarButtonItem(
                                button = buttons[0],
                                delay = 0,
                                haptics = haptics
                            )
                        }
                        
                        // Center space for FAB
                        Spacer(modifier = Modifier.width(88.dp))
                        
                        // Right side
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            ToolbarButtonItem(
                                button = buttons[1],
                                delay = 50,
                                haptics = haptics
                            )
                        }
                    }
                }
                else -> {
                    // For 5 buttons: give FAB more importance with extra spacing around it
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left buttons (0, 1)
                        buttons.take(2).forEachIndexed { index, button ->
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                ToolbarButtonItem(
                                    button = button,
                                    delay = index * 50,
                                    haptics = haptics
                                )
                            }
                        }
                        
                        // Extra space before center button (FAB area)
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        // Center button (2) - highlighted
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            ToolbarButtonItem(
                                button = buttons[2],
                                delay = 2 * 50,
                                haptics = haptics
                            )
                        }
                        
                        // Extra space after center button (FAB area)
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        // Right buttons (3, 4)
                        buttons.drop(3).forEachIndexed { index, button ->
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                ToolbarButtonItem(
                                    button = button,
                                    delay = (3 + index) * 50,
                                    haptics = haptics
                                )
                            }
                        }
                    }
                }
            }
        }
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