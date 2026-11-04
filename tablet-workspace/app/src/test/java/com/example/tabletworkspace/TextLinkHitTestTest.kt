package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TextLinkHitTestTest {
    private val body = "双击编Toolbar辑文本"
    private val kind = TextLinks.withLink("page:1", 0, body.length, "https://example.com")

    private fun shape(
        textAlign: String = "left",
        verticalAlign: String = "top",
        width: Float = 260f,
        height: Float = 120f,
        fontSize: Float = 26f
    ) = Shape(
        type = "text",
        x = 0f,
        y = 0f,
        width = width,
        height = height,
        body = body,
        cardKind = if (verticalAlign == "top") kind else "$kind;valign:$verticalAlign",
        fontSize = fontSize,
        textAlign = textAlign
    )

    @Test
    fun findsLinkInsidePaddedTextArea() {
        val link = TextLinkHitTest.linkAt(shape(), 40f, 40f)
        assertEquals("https://example.com", link?.url)
    }

    @Test
    fun ignoresTapsInInnerPadding() {
        assertNull(TextLinkHitTest.linkAt(shape(), 40f, 10f))
        assertNull(TextLinkHitTest.linkAt(shape(), 10f, 40f))
    }

    @Test
    fun ignoresTapsPastTheLineEnd() {
        assertNull(TextLinkHitTest.linkAt(shape(), 255f, 40f))
    }

    @Test
    fun respectsHorizontalAlignment() {
        val centred = shape(textAlign = "center", width = 400f)
        assertEquals("https://example.com", TextLinkHitTest.linkAt(centred, 200f, 40f)?.url)
        assertNull(TextLinkHitTest.linkAt(centred, 40f, 40f))
    }

    @Test
    fun respectsVerticalAlignment() {
        val bottom = shape(verticalAlign = "bottom")
        assertEquals("https://example.com", TextLinkHitTest.linkAt(bottom, 40f, 80f)?.url)
        assertNull(TextLinkHitTest.linkAt(bottom, 40f, 30f))
    }
}
