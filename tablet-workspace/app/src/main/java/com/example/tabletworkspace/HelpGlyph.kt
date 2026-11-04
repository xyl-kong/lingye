package com.example.tabletworkspace

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A small circular "?" badge. The glyph is drawn on the canvas with its font metrics
 * centred on the circle instead of relying on Text layout, which shifted the question
 * mark up/left inside the circle.
 */
@Composable
internal fun HelpGlyph(
    diameter: Dp = 18.dp,
    tint: Color = LingyeColors.Muted,
    outlined: Boolean = false,
    modifier: Modifier = Modifier
) {
    Canvas(modifier.size(diameter)) {
        val radius = size.minDimension / 2f
        if (outlined) {
            drawCircle(tint, radius = radius - 0.5.dp.toPx(), style = Stroke(1.dp.toPx()))
        } else {
            drawCircle(LingyeColors.Chip, radius = radius)
        }
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = tint.toArgb()
            textSize = 11.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        val metrics = paint.fontMetrics
        val baseline = size.height / 2f - (metrics.ascent + metrics.descent) / 2f
        drawContext.canvas.nativeCanvas.drawText("?", size.width / 2f, baseline, paint)
    }
}
