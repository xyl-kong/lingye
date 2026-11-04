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
    fun newShapesDefaultToCardShadow() {
        assertEquals("box", Shape("card", 0f, 0f, 10f, 10f).shadowStyle)
    }

    @Test
    fun invalidShadowStyleIsRejectedOnSave() {
        val page = Page.create("Invalid")
        page.shapes.add(Shape("card", 0f, 0f, 10f, 10f, shadowStyle = "glow"))

        assertThrows(IllegalArgumentException::class.java) { page.encode() }
    }
}
