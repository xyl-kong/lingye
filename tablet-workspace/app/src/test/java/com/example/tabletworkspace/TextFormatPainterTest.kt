package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TextFormatPainterTest {
    @Test fun replacesOnlyVisualFlagsWithinTargetAndPreservesMetadata() {
        val paint = TextFormatPaint("source", 0, 3, TextSpan.BOLD or TextSpan.ITALIC or (3 shl TextSpan.FG_SHIFT))
        val original = listOf(
            TextSpan(0, 2, TextSpan.UNDERLINE),
            TextSpan(2, 6, TextSpan.CODE or TextSpan.LINK or TextSpan.COMMENT),
            TextSpan(6, 8, TextSpan.STRIKE)
        )
        val result = TextFormatPainter.apply(original, 3, 5, 8, paint)
        assertEquals(TextSpan.UNDERLINE, TextFormatting.flagsAt(result, 1))
        assertEquals(TextSpan.CODE or TextSpan.LINK or TextSpan.COMMENT, TextFormatting.flagsAt(result, 2))
        assertEquals(
            TextSpan.BOLD or TextSpan.ITALIC or TextSpan.LINK or TextSpan.COMMENT or (3 shl TextSpan.FG_SHIFT),
            TextFormatting.flagsAt(result, 3)
        )
        assertEquals(TextSpan.CODE or TextSpan.LINK or TextSpan.COMMENT, TextFormatting.flagsAt(result, 5))
        assertEquals(TextSpan.STRIKE, TextFormatting.flagsAt(result, 7))
    }

    @Test fun defaultSourceClearsVisualFormattingButKeepsEquation() {
        val paint = TextFormatPaint("source", 0, 1, 0)
        val result = TextFormatPainter.apply(listOf(TextSpan(0, 4, TextSpan.BOLD or TextSpan.MATH)), 1, 3, 4, paint)
        assertEquals(TextSpan.BOLD or TextSpan.MATH, TextFormatting.flagsAt(result, 0))
        assertEquals(TextSpan.MATH, TextFormatting.flagsAt(result, 1))
        assertEquals(TextSpan.BOLD or TextSpan.MATH, TextFormatting.flagsAt(result, 3))
        assertEquals(result, TextFormatPainter.apply(result, 2, 2, 4, paint))
    }

    @Test fun rejectsEmptySourceSelection() {
        val shape = Page.create("test").also { it.addTextBox(0f, 0f, "test") }.shapes.last()
        assertNull(TextFormatPainter.capture(shape, 2, 2))
    }

    @Test fun appliedFormatSurvivesPageRoundTrip() {
        val page = Page.create("paint")
        page.addTextBox(0f, 0f, "source")
        page.addTextBox(20f, 20f, "target")
        val source = page.shapes[0].copy(textSpans = listOf(TextSpan(0, 6, TextSpan.BOLD or TextSpan.UNDERLINE)))
        page.shapes[0] = source
        val paint = TextFormatPainter.capture(source, 0, 6)!!
        val target = page.shapes[1]
        page.shapes[1] = target.copy(textSpans = TextFormatPainter.apply(target.textSpans, 1, 4, target.body.length, paint))

        val restored = Page.decode(page.encode())
        assertEquals(0, TextFormatting.flagsAt(restored.shapes[1].textSpans, 0))
        assertEquals(TextSpan.BOLD or TextSpan.UNDERLINE, TextFormatting.flagsAt(restored.shapes[1].textSpans, 2))
        assertEquals(0, TextFormatting.flagsAt(restored.shapes[1].textSpans, 4))
    }
}
