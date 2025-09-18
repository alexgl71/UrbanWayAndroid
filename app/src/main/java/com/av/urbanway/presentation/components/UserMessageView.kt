package com.av.urbanway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UserMessageView(
    message: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Left-aligned user message with right padding of 40px
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 40.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .background(
                    Color(0xFF0B3D91),
                    RoundedCornerShape(
                        topStart = 4.dp, // Chat bubble style
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp
                    )
                )
                .then(
                    if (onClick != null) {
                        Modifier.clickable { onClick() }
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                color = Color.White
            )
        }
    }
}