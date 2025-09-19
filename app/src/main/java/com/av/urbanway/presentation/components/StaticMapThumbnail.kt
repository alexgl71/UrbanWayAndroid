package com.av.urbanway.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.av.urbanway.BuildConfig
import com.av.urbanway.data.models.Coordinates

@Composable
fun StaticMapThumbnail(
    coords: Coordinates,
    width: Int = 640,
    heightPx: Int = 300,
    zoom: Int = 19,
    scale: Int = 2,
    corner: Dp = 12.dp,
    imageHeight: Dp = 240.dp
) {
    val styleParams = listOf(
        "feature:poi|visibility:off",
        "feature:transit|visibility:off",
        "feature:administrative|visibility:off",
        "feature:road|element:labels.icon|visibility:off",
        "feature:poi.park|visibility:off",
        "feature:landscape|visibility:off",
        "feature:water|element:labels|visibility:off"
    ).joinToString(separator = "") { "&style=$it" }

    val url = "https://maps.googleapis.com/maps/api/staticmap" +
            "?center=${coords.lat},${coords.lng}" +
            "&zoom=$zoom" +
            "&size=${width}x${heightPx}" +
            "&scale=$scale" +
            "&maptype=roadmap" +
            styleParams +
            "&markers=color:blue%7C${coords.lat},${coords.lng}" +
            "&key=${BuildConfig.GOOGLE_MAPS_API_KEY}"

    Card(
        shape = RoundedCornerShape(corner),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        AsyncImage(
            model = url,
            contentDescription = "Anteprima mappa",
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight)
        )
    }
}

