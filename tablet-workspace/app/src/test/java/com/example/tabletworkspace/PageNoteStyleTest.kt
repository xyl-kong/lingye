package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PageNoteStyleTest {
    @Test
    fun noteShadowAndCornerSurvivePageRoundTrip() {
        val page = Page.create("Styled note")
        page.addCard(
            CardPreset.NOTE,
            x = 10f,
            y = 20f,
            language = AppLanguage.ZH,
            cornerRadius = 24f,
            shadowStyle = "sticker"
        )

        val restored = Page.decode(page.encode())
        val card = restored.shapes.single()

        assertEquals("sticker", card.shadowStyle)
        assertEquals(24f, card.cornerRadius)
    }

    @Test
    fun newShapesDefaultToPaperShadow() {
        assertEquals("paper", Shape("card", 0f, 0f, 10f, 10f).shadowStyle)
    }

    @Test
    fun allTextViewShadowStylesRoundTrip() {
        val page = Page.create("All shadows")
        SHADOW_STYLES.forEachIndexed { index, style ->
            page.shapes.add(
                Shape("text", index * 10f, 0f, 120f, 60f, shadowStyle = style, body = style)
            )
        }

        val restored = Page.decode(page.encode())
        assertEquals(SHADOW_STYLES, restored.shapes.map { it.shadowStyle }.toSet())
    }

    @Test
    fun invalidShadowStyleIsRejectedOnSave() {
        val page = Page.create("Invalid")
        page.shapes.add(Shape("card", 0f, 0f, 10f, 10f, shadowStyle = "glow"))

        assertThrows(IllegalArgumentException::class.java) { page.encode() }
    }
}
