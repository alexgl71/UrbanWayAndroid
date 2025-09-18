package com.av.urbanway.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GreetingChatView(
    onArriviClick: () -> Unit,
    onMappaClick: () -> Unit,
    onCercaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = "Ciao, cosa vuoi fare oggi?",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.2).sp
                ),
                color = Color.Black.copy(alpha = 0.87f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Choice chips - horizontal and wrapping for compact layout
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

                ChatChoiceChip(
                    text = "🔍 Cerca",
                    onClick = onCercaClick
                )
            }
        }
    }
}

@Composable
private fun ChatChoiceChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF0B3D91).copy(alpha = 0.08f),
            contentColor = Color(0xFF0B3D91)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 1.dp
        ),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        )
    }
}