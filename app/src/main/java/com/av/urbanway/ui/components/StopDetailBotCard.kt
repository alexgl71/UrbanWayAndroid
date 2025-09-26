package com.av.urbanway.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.av.urbanway.data.model.TransitData

@Composable
fun StopDetailBotCard(
    data: TransitData.StopDetailData,
    isCompact: Boolean,
    onViewDetails: () -> Unit,
    onTapCompact: () -> Unit = {}
) {
    if (isCompact) {
        // Compact version - clickable stop name
        Text(
            text = data.stop.name,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            modifier = Modifier
                .clickable { onTapCompact() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    } else {
        // Expanded version with details button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                // Stop name header
                Text(
                    text = data.stop.name,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

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