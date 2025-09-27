package com.av.urbanway.ui.components

import androidx.compose.foundation.background
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
fun RouteDetailBotCard(
    data: TransitData.RouteDetailData,
    isCompact: Boolean,
    onViewDetails: () -> Unit,
    onTapCompact: () -> Unit = {}
) {
    if (isCompact) {
        // Compact version - clickable route info
        Text(
            text = "Linea ${data.route.name} ${data.route.direction}",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            modifier = Modifier
                .clickable { onTapCompact() }
                .padding(start = 16.dp, end = 70.dp, top = 8.dp, bottom = 8.dp)
        )
    } else {
        // Full version with details button
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
                // Route header
                Text(
                    text = "Linea ${data.route.name}",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = data.route.direction,
                    color = Color.Black,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Route info
                Text(
                    text = "${data.stops.size} fermate totali",
                    color = Color.Black,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // View details button
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