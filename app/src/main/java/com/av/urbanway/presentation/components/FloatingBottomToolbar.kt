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
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .scale(buttonScale)
                .graphicsLayer { alpha = buttonAlpha },
            color = Color.White.copy(alpha = 0.92f),
            shadowElevation = 12.dp
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
            .size(48.dp)
            .scale(buttonScale)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isCenter) Color(0xFFD9731F) else Color(0xFF333333),
            modifier = Modifier.size(if (isCenter) 28.dp else 24.dp)
        )
    }
}