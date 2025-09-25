package com.av.urbanway.ui.components

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
import com.av.urbanway.data.service.HardcodedDataService

@Composable
fun NearbyBotCard(
    data: TransitData.NearbyData,
    isCompact: Boolean,
    onViewDetails: () -> Unit = {}
) {
    val dataService = HardcodedDataService()
    val userLocation = dataService.getUserLocation()

    if (isCompact) {
        // Compact template: just the address
        Text(
            text = userLocation.address ?: "Piazza Adriano",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    } else {
        // Expanded template: full card with button
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
                // Location header with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFF4CAF50), // Green color
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = userLocation.address ?: "Piazza Adriano",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary text
                val uniqueRoutes = data.arrivals.map { it.routeName }.toSet().size
                val totalStops = data.stops.size

                Text(
                    text = "Da qui passano ",
                    color = Color.Black,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )

                Row {
                    Text(
                        text = "$uniqueRoutes linee",
                        color = Color(0xFFE91E63), // Red/Pink color for numbers
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    )
                    Text(
                        text = " e hai ",
                        color = Color.Black,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                    Text(
                        text = "$totalStops",
                        color = Color(0xFFE91E63), // Red/Pink color for numbers
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    )
                }

                Text(
                    text = "fermate disponibili.",
                    color = Color.Black,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )

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