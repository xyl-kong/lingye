package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextFormattingTest {

    @Test
    fun normalizeClampsSortsAndMerges() {
        val spans = listOf(
            TextSpan(4, 8, TextSpan.ITALIC),
            TextSpan(0, 5, TextSpan.BOLD),
            TextSpan(8, 12, TextSpan.BOLD),
            TextSpan(8, 10, TextSpan.BOLD)
        )
        val normalized = TextFormatting.normalize(spans, 10)
        // Overlapping spans are combined into a union, then adjacent same flags merge.
        assertEquals(
            listOf(
                TextSpan(0, 8, TextSpan.BOLD or TextSpan.ITALIC),
                TextSpan(8, 10, TextSpan.BOLD)
            ),
            normalized
        )
    }

    @Test
    fun toggleRangeAddsAndRemovesFlag() {
        val added = TextFormatting.toggleRange(emptyList(), 2, 5, TextSpan.BOLD, 10)
        assertEquals(listOf(TextSpan(2, 5, TextSpan.BOLD)), added)
        val removed = TextFormatting.toggleRange(added, 2, 5, TextSpan.BOLD, 10)
        assertEquals(emptyList<TextSpan>(), removed)
    }

    @Test
    fun rangeFlagsOnlyReportsFullyCoveredFlag() {
        val spans = listOf(TextSpan(0, 6, TextSpan.ITALIC))
        assertEquals(TextSpan.ITALIC, TextFormatting.rangeFlags(spans, 1, 5))
        assertEquals(0, TextFormatting.rangeFlags(spans, 3, 8))
        assertEquals(0, TextFormatting.rangeFlags(spans, 6, 8))
    }

    @Test
    fun adjustForEditExtendsAndTrimsSpans() {
        val spans = listOf(TextSpan(2, 5, TextSpan.BOLD))
        val inserted = TextFormatting.adjustForEdit(spans, "abcdefgh", "abcXdefgh")
        assertEquals(listOf(TextSpan(2, 6, TextSpan.BOLD)), inserted)
        val deleted = TextFormatting.adjustForEdit(spans, "abcdefgh", "abdefgh")
        assertEquals(listOf(TextSpan(2, 4, TextSpan.BOLD)), deleted)
        val after = TextFormatting.adjustForEdit(spans, "abcdefgh", "abcdefghX")
        assertEquals(listOf(TextSpan(2, 5, TextSpan.BOLD)), after)
    }

    @Test
    fun runsSplitAtSpanBoundaries() {
        val runs = TextFormatting.runs(
            listOf(TextSpan(2, 5, TextSpan.BOLD), TextSpan(5, 7, TextSpan.ITALIC)),
            0,
            10
        )
        assertEquals(
            listOf(
                TextRun(0, 2, 0),
                TextRun(2, 5, TextSpan.BOLD),
                TextRun(5, 7, TextSpan.ITALIC),
                TextRun(7, 10, 0)
            ),
            runs
        )
    }

    @Test
    fun wrapSplitsAtWidthAndPrefersSpaces() {
        val fixed: (Int) -> Float = { 10f }
        assertEquals(
            listOf(TextLine(0, 2), TextLine(2, 4), TextLine(4, 5)),
            TextFormatting.wrap("abcde", 25f, fixed)
        )
        assertEquals(
            listOf(TextLine(0, 2), TextLine(3, 5)),
            TextFormatting.wrap("ab cd", 25f, fixed)
        )
    }

    @Test
    fun wrapKeepsExplicitEmptyLines() {
        assertEquals(
            listOf(TextLine(0, 1), TextLine(2, 2), TextLine(3, 4)),
            TextFormatting.wrap("a\n\nb", 50f) { 10f }
        )
    }

    @Test
    fun fontValidation() {
        assertTrue(TextFormatting.validWeight(400))
        assertTrue(TextFormatting.validWeight(900))
        assertFalse(TextFormatting.validWeight(450))
        assertFalse(TextFormatting.validWeight(50))
        assertTrue(TextFormatting.FONT_FAMILIES.contains("sans"))
        assertTrue(TextFormatting.FONT_STYLES.contains("italic"))
    }
}
