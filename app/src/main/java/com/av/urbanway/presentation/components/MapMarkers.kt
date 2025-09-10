package com.av.urbanway.presentation.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.max

fun makeBusMarkerDescriptor(density: Density, size: Dp = 28.dp): BitmapDescriptor {
    val requestedPx = with(density) { size.roundToPx() }
    val sizePx = if (requestedPx <= 0) 56 else requestedPx
    val navy = ComposeColor(0xFF0B3D91).toArgb()
    val white = ComposeColor.White.toArgb()
    return runCatching {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val c = AndroidCanvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = sizePx / 2f

        // White filled circle
        paint.style = Paint.Style.FILL
        paint.color = white
        c.drawCircle(cx, cy, radius, paint)

        // Navy border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(2f, sizePx * 0.08f)
        paint.color = navy
        c.drawCircle(cx, cy, radius - paint.strokeWidth / 2f, paint)

        // Simple bus glyph: navy rounded rectangle + white windows + white wheels
        paint.style = Paint.Style.FILL
        paint.color = navy
        val busWidth = sizePx * 0.60f
        val busHeight = sizePx * 0.42f
        val left = cx - busWidth / 2f
        val top = cy - busHeight / 2f
        val rect = RectF(left, top, left + busWidth, top + busHeight)
        c.drawRoundRect(rect, sizePx * 0.10f, sizePx * 0.10f, paint)

        // Windows
        paint.color = white
        val winPadding = sizePx * 0.08f
        val winTop = top + winPadding
        val winBottom = top + busHeight * 0.55f
        val winLeft = left + winPadding
        val winRight = left + busWidth - winPadding
        val windowRect = RectF(winLeft, winTop, winRight, winBottom)
        c.drawRoundRect(windowRect, sizePx * 0.06f, sizePx * 0.06f, paint)

        // Wheels
        val wheelRadius = max(1f, sizePx * 0.06f)
        val wheelY = top + busHeight - wheelRadius
        c.drawCircle(left + busWidth * 0.25f, wheelY, wheelRadius, paint)
        c.drawCircle(left + busWidth * 0.75f, wheelY, wheelRadius, paint)

        BitmapDescriptorFactory.fromBitmap(bmp)
    }.getOrElse {
        // Fallback to default marker in case of any drawing error
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
    }
}

