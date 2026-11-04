package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CanvasObjectModelTest {

    @Test
    fun mapsEveryCanvasSelectionToSemanticKind() {
        val page = Page.create("p")
        page.addShape("rect", 0f, 0f, 10f, 10f)
        page.addTextBox(20f, 0f, "text")
        page.addCard(CardPreset.NOTE, 40f, 0f)
        page.addShape("frame", 60f, 0f, 100f, 100f)
        val target = Page.create("target")
        page.addLinkedDoc(target, 0f, 100f)
        Mindmap.createRoot(page, 0f, 200f, "root", 0xffffd866.toInt())
        page.addCanvasImage("missing.img", 0f, 300f, 10f, 10f)
        page.addConnector(0f, 0f, 10f, 10f)
        page.addStyledStroke(listOf(Point(0f, 0f), Point(1f, 1f)), 0xff000000.toInt(), 2f)

        assertEquals(CanvasObjectKind.SHAPE, CanvasObjectSpecs.kindOf(page, CanvasSelection("shape", 0)))
        assertEquals(CanvasObjectKind.TEXT, CanvasObjectSpecs.kindOf(page, CanvasSelection("shape", 1)))
        assertEquals(CanvasObjectKind.NOTE, CanvasObjectSpecs.kindOf(page, CanvasSelection("shape", 2)))
        assertEquals(CanvasObjectKind.FRAME, CanvasObjectSpecs.kindOf(page, CanvasSelection("shape", 3)))
        assertEquals(CanvasObjectKind.LINK, CanvasObjectSpecs.kindOf(page, CanvasSelection("shape", 4)))
        assertEquals(CanvasObjectKind.MINDMAP, CanvasObjectSpecs.kindOf(page, CanvasSelection("shape", 5)))
        assertEquals(CanvasObjectKind.IMAGE, CanvasObjectSpecs.kindOf(page, CanvasSelection("image", 0)))
        assertEquals(CanvasObjectKind.CONNECTOR, CanvasObjectSpecs.kindOf(page, CanvasSelection("connector", 0)))
        assertEquals(CanvasObjectKind.STROKE, CanvasObjectSpecs.kindOf(page, CanvasSelection("stroke", 0)))
    }

    @Test
    fun invalidSelectionAndOutOfRangeReturnNull() {
        val page = Page.create("p")
        assertNull(CanvasObjectSpecs.kindOf(page, null))
        assertNull(CanvasObjectSpecs.kindOf(page, CanvasSelection("shape", 0)))
        assertNull(CanvasObjectSpecs.kindOf(page, CanvasSelection("unknown", 0)))
    }

    @Test
    fun capabilitiesSeparateTextNoteShapeAndImage() {
        assertTrue(CanvasObjectSpecs.supports(CanvasObjectKind.TEXT, CanvasCapability.FONT_FAMILY))
        assertTrue(CanvasObjectSpecs.supports(CanvasObjectKind.TEXT, CanvasCapability.TEXT_SELECTION))
        assertTrue(CanvasObjectSpecs.supports(CanvasObjectKind.NOTE, CanvasCapability.TEMPLATE))
        assertTrue(CanvasObjectSpecs.supports(CanvasObjectKind.SHAPE, CanvasCapability.ROTATE))
        assertTrue(CanvasObjectSpecs.supports(CanvasObjectKind.IMAGE, CanvasCapability.CROP))
        assertFalse(CanvasObjectSpecs.supports(CanvasObjectKind.IMAGE, CanvasCapability.FONT_SIZE))
        assertFalse(CanvasObjectSpecs.supports(CanvasObjectKind.CONNECTOR, CanvasCapability.CORNER_RADIUS))
        assertTrue(CanvasObjectSpecs.supports(CanvasObjectKind.LINK, CanvasCapability.BACKLINK))
    }

    @Test
    fun pageCardIsAnExplicitKindWithoutCanvasSelection() {
        val card = CanvasObjectKind.fromId("page-card")
        assertEquals(CanvasObjectKind.PAGE_CARD, card)
        assertTrue(CanvasObjectSpecs.supports(CanvasObjectKind.PAGE_CARD, CanvasCapability.EDIT_TEXT))
        assertNull(CanvasObjectKind.fromId("embed"))
    }
}
