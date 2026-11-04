package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TextFormattingExtensionsTest {
    @Test
    fun strikeAndCodeToggleIndependently() {
        var spans = TextFormatting.toggleRange(emptyList(), 1, 4, TextSpan.STRIKE, 10)
        assertEquals(listOf(TextSpan(1, 4, TextSpan.STRIKE)), spans)
        spans = TextFormatting.toggleRange(spans, 2, 5, TextSpan.CODE, 10)
        assertEquals(
            listOf(TextSpan(1, 2, TextSpan.STRIKE), TextSpan(2, 4, TextSpan.STRIKE or TextSpan.CODE), TextSpan(4, 5, TextSpan.CODE)),
            spans
        )
    }

    @Test
    fun foregroundAndBackgroundColorsUseSeparateFields() {
        val length = 8
        var spans = TextFormatting.setColorRange(emptyList(), 1, 5, TextSpan.FG_MASK, TextSpan.FG_SHIFT, 3, length)
        spans = TextFormatting.setColorRange(spans, 2, 6, TextSpan.BG_MASK, TextSpan.BG_SHIFT, 5, length)
        assertEquals(3, TextSpan.foreground(spans.first { 1 in it.start until it.end }.flags))
        assertEquals(5, TextSpan.background(TextFormatting.flagsAt(spans, 3)))
        assertEquals(3, TextSpan.foreground(TextFormatting.flagsAt(spans, 3)))
        assertEquals(0, TextSpan.background(TextFormatting.flagsAt(spans, 1)))
    }

    @Test
    fun defaultColorClearsTheField() {
        var spans = TextFormatting.setColorRange(emptyList(), 0, 4, TextSpan.FG_MASK, TextSpan.FG_SHIFT, 2, 4)
        assertEquals(listOf(TextSpan(0, 4, 2 shl TextSpan.FG_SHIFT)), spans)
        spans = TextFormatting.setColorRange(spans, 0, 4, TextSpan.FG_MASK, TextSpan.FG_SHIFT, 0, 4)
        assertEquals(emptyList<TextSpan>(), spans)
    }

    @Test
    fun linksRoundTripAndAdjustWithText() {
        val cardKind = "scale:100;valign:middle;link:1:4:${base64("https://example.com")}"
        val links = TextLinks.parse(cardKind)
        assertEquals(listOf(TextLink(1, 4, "https://example.com")), links)
        assertEquals("https://example.com", TextLinks.linkAt(cardKind, 2)?.url)
        assertNull(TextLinks.linkAt(cardKind, 5))

        val adjusted = TextLinks.adjust(cardKind, "abcdef", "abXXcdef")
        assertEquals(listOf(TextLink(1, 6, "https://example.com")), TextLinks.parse(adjusted))
        assertEquals("scale:100;valign:middle", TextLinks.withoutLinks(adjusted))
    }

    @Test
    fun pageLinkTargetsAreRecognized() {
        val link = TextLink(0, 2, "lingye://page/abc")
        assertEquals("abc", TextLinks.pageId(link))
        assertNull(TextLinks.pageId(TextLink(0, 2, "https://example.com")))
    }

    private fun base64(value: String): String =
        java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))
}
