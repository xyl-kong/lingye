package com.example.tabletworkspace

/** Canvas viewport zoom limits shared by pinch, the zoom buttons and session restore. */
object CanvasZoom {
    const val MIN = 0.10f
    const val MAX = 4f
    const val DEFAULT = 1f

    fun clamp(scale: Float): Float = scale.coerceIn(MIN, MAX)
}
