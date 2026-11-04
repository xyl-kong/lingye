package com.example.tabletworkspace

import kotlin.math.roundToInt

/**
 * Brush parameters matching BlockSuite's brush tool:
 * two pen types (brush / highlighter), six widths each, highlighter painted at 30% alpha.
 */
object BrushSettings {
    const val KIND_BRUSH = "brush"
    const val KIND_HIGHLIGHTER = "highlighter"

    val BRUSH_WIDTHS = listOf(2f, 4f, 6f, 8f, 10f, 12f)
    val HIGHLIGHTER_WIDTHS = listOf(10f, 14f, 18f, 22f, 26f, 30f)

    const val DEFAULT_BRUSH_WIDTH = 4f
    const val DEFAULT_HIGHLIGHTER_WIDTH = 22f
    const val HIGHLIGHTER_ALPHA = 0.3f

    /** Ink colors from the official short stroke palette (transparent omitted). */
    val COLORS = listOf(
        0xff111111.toInt(), 0xffef7076.toInt(), 0xfff08c43.toInt(), 0xffe8c64b.toInt(),
        0xff57b85e.toInt(), 0xff79b8ef.toInt(), 0xff6656d9.toInt(), 0xffcf69b4.toInt(),
        0xff9a9a9a.toInt(), 0xffffffff.toInt()
    )

    fun widthsFor(kind: String): List<Float> = if (kind == KIND_HIGHLIGHTER) HIGHLIGHTER_WIDTHS else BRUSH_WIDTHS

    fun defaultWidth(kind: String): Float = if (kind == KIND_HIGHLIGHTER) DEFAULT_HIGHLIGHTER_WIDTH else DEFAULT_BRUSH_WIDTH

    fun withAlpha(color: Int, alpha: Float): Int {
        val a = (alpha.coerceIn(0f, 1f) * 255).roundToInt()
        return (color and 0x00FFFFFF) or (a shl 24)
    }

    fun highlighterColor(color: Int): Int = withAlpha(color, HIGHLIGHTER_ALPHA)

    /** Strokes painted by the highlighter carry partial alpha; plain ink is opaque. */
    fun isHighlighter(color: Int): Boolean = (color ushr 24) in 1..254

    fun paintColor(kind: String, color: Int): Int = if (kind == KIND_HIGHLIGHTER) highlighterColor(color) else (color or 0xFF000000.toInt())

    fun paintWidth(kind: String, brushWidth: Float, highlighterWidth: Float): Float =
        if (kind == KIND_HIGHLIGHTER) highlighterWidth else brushWidth
}
