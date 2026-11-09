package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Test

class TextBlockTransformsTest {
    @Test
    fun bulletListTogglesWithoutDuplicatingPrefixes() {
        val once = TextBlockTransforms.applyList("One\nTwo", "bulleted")
        assertEquals("• One\n• Two", once)
        assertEquals("One\nTwo", TextBlockTransforms.applyList(once, "bulleted"))
    }

    @Test
    fun numberedListUsesLineNumbersAndTogglesOff() {
        val once = TextBlockTransforms.applyList("One\n\nTwo", "numbered")
        assertEquals("1. One\n\n2. Two", once)
        assertEquals("One\n\nTwo", TextBlockTransforms.applyList(once, "numbered"))
    }

    @Test
    fun todoListPreservesCheckedMarkersWhenTogglingOff() {
        val once = TextBlockTransforms.applyList("One\nTwo", "todo")
        assertEquals("☐ One\n☐ Two", once)
        assertEquals("One\nTwo", TextBlockTransforms.applyList(once, "todo"))
    }

    @Test
    fun quoteTogglesPrefixes() {
        val once = TextBlockTransforms.applyQuote("One\nTwo")
        assertEquals("> One\n> Two", once)
        assertEquals("One\nTwo", TextBlockTransforms.applyQuote(once))
    }
}
