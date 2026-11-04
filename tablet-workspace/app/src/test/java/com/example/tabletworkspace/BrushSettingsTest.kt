package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrushSettingsTest {
    @Test
    fun widthsMatchOfficialToolbar() {
        assertEquals(listOf(2f, 4f, 6f, 8f, 10f, 12f), BrushSettings.BRUSH_WIDTHS)
        assertEquals(listOf(10f, 14f, 18f, 22f, 26f, 30f), BrushSettings.HIGHLIGHTER_WIDTHS)
        assertEquals(BrushSettings.BRUSH_WIDTHS, BrushSettings.widthsFor(BrushSettings.KIND_BRUSH))
        assertEquals(BrushSettings.HIGHLIGHTER_WIDTHS, BrushSettings.widthsFor(BrushSettings.KIND_HIGHLIGHTER))
        assertEquals(22f, BrushSettings.defaultWidth(BrushSettings.KIND_HIGHLIGHTER))
    }

    @Test
    fun highlighterPaintIsThirtyPercentOpaque() {
        val color = BrushSettings.highlighterColor(0xffe8c64b.toInt())
        assertEquals(77, color ushr 24)
        assertEquals(0xe8c64b, color and 0xFFFFFF)
        assertTrue(BrushSettings.isHighlighter(color))
        assertFalse(BrushSettings.isHighlighter(0xffe8c64b.toInt()))
        assertEquals(color, BrushSettings.paintColor(BrushSettings.KIND_HIGHLIGHTER, 0xffe8c64b.toInt()))
        assertEquals(0xffe8c64b.toInt(), BrushSettings.paintColor(BrushSettings.KIND_BRUSH, 0x80e8c64b.toInt()))
    }

    @Test
    fun highlighterWidthsSurviveSaveAndReopen() {
        val page = Page.create("Ink")
        page.addStyledStroke(
            listOf(Point(0f, 0f), Point(40f, 10f)),
            BrushSettings.highlighterColor(0xffe8c64b.toInt()),
            BrushSettings.HIGHLIGHTER_WIDTHS.last()
        )

        val restored = Page.decode(page.encode())
        val style = restored.strokeStyles.single()
        assertEquals(30f, style.width, 0.001f)
        assertEquals(77, style.color ushr 24)
        assertTrue(BrushSettings.isHighlighter(style.color))
    }

    @Test
    fun paintWidthPicksTheActivePenType() {
        assertEquals(6f, BrushSettings.paintWidth(BrushSettings.KIND_BRUSH, 6f, 22f))
        assertEquals(22f, BrushSettings.paintWidth(BrushSettings.KIND_HIGHLIGHTER, 6f, 22f))
    }
}
