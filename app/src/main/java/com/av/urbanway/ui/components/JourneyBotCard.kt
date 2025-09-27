package com.av.urbanway.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.data.model.TransitData

@Composable
fun JourneyBotCard(
    data: TransitData.JourneyData,
    isCompact: Boolean,
    onViewDetails: () -> Unit = {},
    onTapCompact: () -> Unit = {}
) {
    if (isCompact) {
        // Compact template: clickable text
        Text(
            text = "Percorso da ${data.origin.address ?: "Piazza Adriano"} a ${data.destination.address ?: "Piazza Castello"}",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            modifier = Modifier
                .clickable { onTapCompact() }
                .padding(start = 16.dp, end = 70.dp, top = 8.dp, bottom = 8.dp)
        )
    } else {
        // Expanded template: full card with button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 70.dp, top = 8.dp, bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Location header with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Journey",
                        tint = Color(0xFF4CAF50), // Green color
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Percorso da ${data.origin.address ?: "Piazza Adriano"} a ${data.destination.address ?: "Piazza Castello"}",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Journey summary
                if (data.routes.isNotEmpty()) {
                    val bestRoute = data.routes.first()

                    Text(
                        text = "Durata: ",
                        color = Color.Black,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )

                    Row {
                        Text(
                            text = "${bestRoute.totalDuration} minuti",
                            color = Color(0xFFE91E63), // Red/Pink color for numbers
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp
                        )
                        Text(
                            text = " (${bestRoute.totalWalking} min a piedi)",
                            color = Color.Black,
                            fontSize = 16.sp,
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Trovate ",
                        color = Color.Black,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )

                    Row {
                        Text(
                            text = "${data.routes.size}",
                            color = Color(0xFFE91E63), // Red/Pink color for numbers
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp
                        )
                        Text(
                            text = " opzioni di percorso.",
                            color = Color.Black,
                            fontSize = 16.sp,
                            lineHeight = 22.sp
                        )
                    }
                } else {
                    Text(
                        text = "Nessun percorso disponibile.",
                        color = Color.Black,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // "Vedi i dettagli" button
                Button(
                    onClick = onViewDetails,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C3E50) // Dark blue color
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Vedi i dettagli",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}