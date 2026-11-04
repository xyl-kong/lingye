package com.example.tabletworkspace

/** Keeps floating toolbars inside the canvas viewport on narrow or split windows. */
object CanvasLayout {
    const val MARGIN = 12f

    fun toolbarX(atX: Float, viewportWidthDp: Float, toolbarWidthDp: Float, margin: Float = MARGIN): Float {
        require(viewportWidthDp.isFinite() && viewportWidthDp >= 0f) { "视口宽度无效" }
        require(toolbarWidthDp.isFinite() && toolbarWidthDp > 0f) { "工具条宽度无效" }
        require(margin.isFinite() && margin >= 0f) { "边距无效" }
        val rightLimit = (viewportWidthDp - toolbarWidthDp - margin).coerceAtLeast(margin)
        return atX.coerceIn(margin, rightLimit)
    }

    /** Widest a bottom parameter bar may be, leaving [margin] on both sides for the scroll row. */
    fun parameterBarMaxWidth(viewportWidthDp: Float, margin: Float = 8f): Float {
        require(viewportWidthDp.isFinite() && viewportWidthDp >= 0f) { "视口宽度无效" }
        require(margin.isFinite() && margin >= 0f) { "边距无效" }
        return (viewportWidthDp - margin * 2f).coerceAtLeast(MIN_BAR_WIDTH)
    }

    const val MIN_BAR_WIDTH = 240f
}
