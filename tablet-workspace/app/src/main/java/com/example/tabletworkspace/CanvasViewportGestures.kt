package com.example.tabletworkspace

import androidx.compose.ui.geometry.Offset

internal data class CanvasViewportChange(val scale: Float, val pan: Offset)

/** Screen-pixel math shared by the two-finger canvas navigation handler. */
internal object CanvasViewportGestures {
    fun mayStart(touchCount: Int, stylusOrEraserPressed: Boolean): Boolean =
        touchCount >= 2 && !stylusOrEraserPressed

    fun transform(
        scale: Float,
        pan: Offset,
        previousFirst: Offset,
        previousSecond: Offset,
        currentFirst: Offset,
        currentSecond: Offset
    ): CanvasViewportChange {
        val previousCentroid = (previousFirst + previousSecond) / 2f
        val currentCentroid = (currentFirst + currentSecond) / 2f
        val previousSpan = (previousFirst - previousSecond).getDistance()
        val currentSpan = (currentFirst - currentSecond).getDistance()
        val zoom = if (previousSpan > 0.001f) currentSpan / previousSpan else 1f
        val nextScale = CanvasZoom.clamp(scale * zoom)
        val factor = nextScale / scale
        return CanvasViewportChange(nextScale, currentCentroid - (previousCentroid - pan) * factor)
    }
}
