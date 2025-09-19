package com.av.urbanway.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DotsLoadingIndicator(
    modifier: Modifier = Modifier,
    dotCount: Int = 3,
    size: Dp = 8.dp,
    spacing: Dp = 6.dp,
    color: Color = Color(0xFF0B3D91),
    durationMillis: Int = 700
) {
    val transition = rememberInfiniteTransition(label = "dotsLoading")
    Row(horizontalArrangement = Arrangement.spacedBy(spacing), modifier = modifier) {
        repeat(dotCount) { index ->
            val scale by transition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = durationMillis,
                        delayMillis = index * (durationMillis / dotCount),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dotScale$index"
            )
            Surface(
                modifier = Modifier
                    .size(size)
                    .scale(scale),
                shape = CircleShape,
                color = color,
                content = {}
            )
        }
    }
}

@Composable
fun LoadingWheelIndicator(
    modifier: Modifier = Modifier,
    indicatorSize: Dp = 42.dp,
    dotCount: Int = 12,
    dotSize: Dp = 5.dp,
    color: Color = Color(0xFF0B3D91),
    durationMillis: Int = 900
) {
    val transition = rememberInfiniteTransition(label = "wheel")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wheelRotation"
    )

    Canvas(modifier = modifier.size(indicatorSize)) {
        val canvasSize = this.size
        val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
        val radius = (canvasSize.minDimension / 2f) - (dotSize.toPx() / 2f)
        val step = 360f / dotCount

        // Rotate the whole wheel
        rotate(rotation) {
            for (i in 0 until dotCount) {
                val angleDeg = i * step
                val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
                val x = center.x + radius * kotlin.math.cos(angleRad)
                val y = center.y + radius * kotlin.math.sin(angleRad)
                // Trail alpha: leading dot brightest, then fades
                val trail = i.toFloat() / (dotCount - 1).toFloat()
                val alpha = 0.25f + (1f - trail) * 0.75f
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = dotSize.toPx() / 2f,
                    center = Offset(x, y)
                )
            }
        }
    }
}
