package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CanvasHitTestIndexTest {

    @Test
    fun topmostLayerWinsAtTheSamePoint() {
        val page = Page.create("hit")
        page.addShape("rect", 0f, 0f, 100f, 100f)
        page.addTextBox(10f, 10f, "on top")
        assertEquals(CanvasSelection("shape", 1), page.hitTest(Point(20f, 20f), 2f))
    }

    @Test
    fun emptyAreaReturnsNull() {
        val page = Page.create("hit")
        page.addShape("rect", 0f, 0f, 100f, 100f)
        assertNull(page.hitTest(Point(400f, 400f), 2f))
    }

    @Test
    fun frameIsHitOnItsBorderButNotItsInterior() {
        val page = Page.create("hit")
        page.addShape("frame", 0f, 0f, 200f, 150f)
        assertEquals(CanvasSelection("shape", 0), page.hitTest(Point(0f, 75f), 4f))
        assertNull(page.hitTest(Point(100f, 75f), 4f))
    }

    @Test
    fun connectorIsHitAlongItsPath() {
        val page = Page.create("hit")
        page.addConnector(0f, 0f, 100f, 0f, pathStyle = "straight")
        assertEquals(CanvasSelection("connector", 0), page.hitTest(Point(50f, 1f), 4f))
        assertNull(page.hitTest(Point(50f, 40f), 4f))
    }

    @Test
    fun selectionSurvivesWhenShapeIdsAreRenumberedAfterDelete() {
        val page = Page.create("hit")
        page.addShape("rect", 0f, 0f, 100f, 100f)
        page.addShape("ellipse", 200f, 0f, 100f, 100f)
        page.deleteSelection(CanvasSelection("shape", 0))
        assertEquals(CanvasSelection("shape", 0), page.hitTest(Point(250f, 50f), 2f))
        assertEquals("ellipse", page.shapes[0].type)
    }
}
