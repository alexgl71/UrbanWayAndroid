package com.av.urbanway.presentation.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BotMessageContainer(
    isLastMessage: Boolean = false,
    onExpandClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Right-aligned bot message with left padding of 40px
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 40.dp)
    ) {
        Box(
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
        ) {
            // Content area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                content()
            }

            // Fullscreen button in top right corner - only for last message
            if (isLastMessage && onExpandClick != null) {
                IconButton(
                    onClick = onExpandClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.OpenInFull,
                        contentDescription = "Espandi",
                        tint = Color(0xFF0B3D91),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}