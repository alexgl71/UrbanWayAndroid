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

fun makeSelectedBusMarkerDescriptor(density: Density, size: Dp = 32.dp): BitmapDescriptor {
    val requestedPx = with(density) { size.roundToPx() }
    val sizePx = if (requestedPx <= 0) 64 else requestedPx
    val navy = ComposeColor(0xFF0B3D91).toArgb()
    val white = ComposeColor.White.toArgb()
    val orange = ComposeColor(0xFFFF9500).toArgb()
    return runCatching {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val c = AndroidCanvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = sizePx / 2f

        // Orange filled circle for selected state
        paint.style = Paint.Style.FILL
        paint.color = orange
        c.drawCircle(cx, cy, radius, paint)

        // Navy border (thicker for selected state)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(3f, sizePx * 0.10f)
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
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
    }
}

fun makeRouteStopMarkerDescriptor(density: Density, size: Dp = 20.dp): BitmapDescriptor {
    val requestedPx = with(density) { size.roundToPx() }
    val sizePx = if (requestedPx <= 0) 40 else requestedPx
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

        // Navy blue border (thicker for visibility)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(3f, sizePx * 0.15f)
        paint.color = navy
        c.drawCircle(cx, cy, radius - paint.strokeWidth / 2f, paint)

        BitmapDescriptorFactory.fromBitmap(bmp)
    }.getOrElse {
        // Fallback to default marker
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
    }
}

fun makeSelectedRouteStopMarkerDescriptor(density: Density, size: Dp = 22.dp): BitmapDescriptor {
    val requestedPx = with(density) { size.roundToPx() }
    val sizePx = if (requestedPx <= 0) 44 else requestedPx
    val red = ComposeColor(0xFFE53935).toArgb() // Red for selected stop
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

        // Red border (thicker for selected state)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(4f, sizePx * 0.18f)
        paint.color = red
        c.drawCircle(cx, cy, radius - paint.strokeWidth / 2f, paint)

        BitmapDescriptorFactory.fromBitmap(bmp)
    }.getOrElse {
        // Fallback to default marker
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
    }
}

fun makeStartMarkerDescriptor(density: Density, size: Dp = 28.dp): BitmapDescriptor {
    val requestedPx = with(density) { size.roundToPx() }
    val sizePx = if (requestedPx <= 0) 56 else requestedPx
    val green = ComposeColor(0xFF34C759).toArgb() // iOS systemGreen
    val white = ComposeColor.White.toArgb()
    val darkGreen = ComposeColor(0xFF28A745).toArgb()

    return runCatching {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val c = AndroidCanvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = sizePx / 2f

        // White outer glow/shadow
        paint.style = Paint.Style.FILL
        paint.color = ComposeColor.White.copy(alpha = 0.3f).toArgb()
        c.drawCircle(cx, cy, radius, paint)

        // Green filled circle (slightly smaller)
        paint.color = green
        c.drawCircle(cx, cy, radius * 0.85f, paint)

        // Darker green border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(2f, sizePx * 0.06f)
        paint.color = darkGreen
        c.drawCircle(cx, cy, radius * 0.85f - paint.strokeWidth / 2f, paint)

        // Play icon (triangle pointing right) instead of "S"
        paint.style = Paint.Style.FILL
        paint.color = white
        val iconSize = sizePx * 0.3f
        val triangleOffset = iconSize * 0.1f // Slight offset to center visually

        // Triangle points
        val path = android.graphics.Path()
        path.moveTo(cx - iconSize/2 + triangleOffset, cy - iconSize/2) // Top left
        path.lineTo(cx + iconSize/2 + triangleOffset, cy) // Right center
        path.lineTo(cx - iconSize/2 + triangleOffset, cy + iconSize/2) // Bottom left
        path.close()

        c.drawPath(path, paint)

        BitmapDescriptorFactory.fromBitmap(bmp)
    }.getOrElse {
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
    }
}

fun makeEndMarkerDescriptor(density: Density, size: Dp = 28.dp): BitmapDescriptor {
    val requestedPx = with(density) { size.roundToPx() }
    val sizePx = if (requestedPx <= 0) 56 else requestedPx
    val red = ComposeColor(0xFFFF3B30).toArgb() // iOS systemRed
    val white = ComposeColor.White.toArgb()
    val darkRed = ComposeColor(0xFFDC3545).toArgb()

    return runCatching {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val c = AndroidCanvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = sizePx / 2f

        // White outer glow/shadow
        paint.style = Paint.Style.FILL
        paint.color = ComposeColor.White.copy(alpha = 0.3f).toArgb()
        c.drawCircle(cx, cy, radius, paint)

        // Red filled circle (slightly smaller)
        paint.color = red
        c.drawCircle(cx, cy, radius * 0.85f, paint)

        // Darker red border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(2f, sizePx * 0.06f)
        paint.color = darkRed
        c.drawCircle(cx, cy, radius * 0.85f - paint.strokeWidth / 2f, paint)

        // Square/stop icon instead of "E"
        paint.style = Paint.Style.FILL
        paint.color = white
        val iconSize = sizePx * 0.25f

        // Draw a square (stop symbol)
        val rect = RectF(
            cx - iconSize/2,
            cy - iconSize/2,
            cx + iconSize/2,
            cy + iconSize/2
        )
        c.drawRoundRect(rect, iconSize * 0.1f, iconSize * 0.1f, paint)

        BitmapDescriptorFactory.fromBitmap(bmp)
    }.getOrElse {
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
    }
}

fun makeUserLocationMarkerDescriptor(density: Density, size: Dp = 20.dp): BitmapDescriptor {
    val requestedPx = with(density) { size.roundToPx() }
    val sizePx = if (requestedPx <= 0) 40 else requestedPx
    val blue = ComposeColor(0xFF007AFF).toArgb() // iOS blue
    val white = ComposeColor.White.toArgb()
    val lightGray = ComposeColor(0xFFE0E0E0).toArgb()

    return runCatching {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val c = AndroidCanvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val outerRadius = sizePx / 2f
        val innerRadius = outerRadius * 0.7f

        // White outer circle with light gray border
        paint.style = Paint.Style.FILL
        paint.color = white
        c.drawCircle(cx, cy, outerRadius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(1f, sizePx * 0.05f)
        paint.color = lightGray
        c.drawCircle(cx, cy, outerRadius - paint.strokeWidth / 2f, paint)

        // Blue inner circle (GPS dot)
        paint.style = Paint.Style.FILL
        paint.color = blue
        c.drawCircle(cx, cy, innerRadius, paint)

        BitmapDescriptorFactory.fromBitmap(bmp)
    }.getOrElse {
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
    }
}

fun makeStaleLocationMarkerDescriptor(density: Density, size: Dp = 20.dp): BitmapDescriptor {
    val requestedPx = with(density) { size.roundToPx() }
    val sizePx = if (requestedPx <= 0) 40 else requestedPx
    val gray = ComposeColor(0xFF808080).toArgb() // Gray for stale
    val white = ComposeColor.White.toArgb()
    val lightGray = ComposeColor(0xFFE0E0E0).toArgb()

    return runCatching {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val c = AndroidCanvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val outerRadius = sizePx / 2f
        val innerRadius = outerRadius * 0.7f

        // White outer circle with light gray border
        paint.style = Paint.Style.FILL
        paint.color = white
        c.drawCircle(cx, cy, outerRadius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(1f, sizePx * 0.05f)
        paint.color = lightGray
        c.drawCircle(cx, cy, outerRadius - paint.strokeWidth / 2f, paint)

        // Gray inner circle (stale GPS dot)
        paint.style = Paint.Style.FILL
        paint.color = gray
        c.drawCircle(cx, cy, innerRadius, paint)

        BitmapDescriptorFactory.fromBitmap(bmp)
    }.getOrElse {
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
    }
}

