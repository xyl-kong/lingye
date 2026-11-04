package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AreaEraserTest {
    private fun page(): Page = Page.create("Erase").apply {
        addShape("rect", 100f, 100f, 100f, 80f)                 // shape 0: crossed by x=150
        addShape("rect", 500f, 100f, 100f, 80f)                 // shape 1: far away
        shapes[1] = shapes[1].copy(locked = true)
        addCanvasImage("img.png", 300f, 100f, 80f, 60f)          // image 0: far
        addStyledStroke(listOf(Point(0f, 300f), Point(400f, 300f)), 0xff000000.toInt(), 3f) // stroke 0: crossed
        addStyledStroke(listOf(Point(0f, 600f), Point(100f, 600f)), 0xff000000.toInt(), 3f) // stroke 1: far
        addConnector(200f, 450f, 300f, 450f)                     // connector 0: far
        addConnector(100f, 250f, 200f, 250f)                     // connector 1: crossed
    }

    @Test
    fun areaEraserHitsTouchedUnlockedObjects() {
        val page = page()
        val hits = AreaEraser.hits(page, Point(150f, 50f), Point(150f, 350f), 10f)

        assertTrue(CanvasSelection("shape", 0) in hits)
        assertFalse("locked shape stays", CanvasSelection("shape", 1) in hits)
        assertFalse("distant image stays", CanvasSelection("image", 0) in hits)
        assertTrue(CanvasSelection("stroke", 0) in hits)
        assertFalse(CanvasSelection("stroke", 1) in hits)
        assertTrue(CanvasSelection("connector", 1) in hits)
        assertFalse(CanvasSelection("connector", 0) in hits)
    }

    @Test
    fun areaEraseRemovesWholeObjectsAndKeepsCollectionsConsistent() {
        val page = page()
        val hits = AreaEraser.hits(page, Point(150f, 50f), Point(150f, 350f), 10f)

        assertTrue(AreaEraser.erase(page, hits))

        assertEquals(listOf("rect"), page.shapes.map { it.type })       // locked rect remains
        assertEquals(1, page.images.size)
        assertEquals(1, page.strokes.size)
        assertEquals(1, page.strokeStyles.size)
        assertEquals(1, page.strokeIds.size)
        assertEquals(1, page.connectors.size)
        assertTrue(Page.decode(page.encode()).shapes.isNotEmpty())
    }

    @Test
    fun longCrossingStrokeIsHitEvenWhenEndpointsAreFar() {
        val page = Page.create("Cross").apply {
            addStyledStroke(listOf(Point(0f, 200f), Point(400f, 200f)), 0xff000000.toInt(), 3f)
        }
        val hits = AreaEraser.hits(page, Point(200f, 100f), Point(200f, 300f), 4f)
        assertTrue(CanvasSelection("stroke", 0) in hits)
    }

    @Test
    fun erasingAShapeLeavesItsConnectorUnboundButPresent() {
        val page = Page.create("Bound").apply {
            addShape("rect", 0f, 0f, 100f, 100f)
            addShape("rect", 200f, 0f, 100f, 100f)
            addBoundConnector(0, 1)
        }
        val hits = AreaEraser.hits(page, Point(10f, 5f), Point(10f, 15f), 5f)
        assertEquals(listOf(CanvasSelection("shape", 0)), hits)

        AreaEraser.erase(page, hits)

        assertEquals(1, page.shapes.size)
        assertEquals(1, page.connectors.size)
        assertTrue(page.connectors[0].startShape.isEmpty())
        assertEquals(page.shapes[0].id, page.connectors[0].endShape)
    }

    @Test
    fun rectHitUsesInflatedBounds() {
        assertTrue(AreaEraser.segmentHitsRect(Point(0f, 0f), Point(0f, 100f), 20f, 0f, 100f, 100f, 25f))
        assertFalse(AreaEraser.segmentHitsRect(Point(0f, 0f), Point(0f, 100f), 60f, 0f, 100f, 100f, 25f))
        assertTrue(AreaEraser.segmentHitsRect(Point(50f, 50f), Point(150f, 50f), 20f, 20f, 100f, 100f, 1f))
    }

    @Test
    fun erasingTwoShapesInOnePassRemovesBoth() {
        val page = Page.create("Two").apply {
            addShape("rect", 0f, 0f, 50f, 50f)
            addShape("rect", 100f, 0f, 50f, 50f)
        }
        val hits = AreaEraser.hits(page, Point(0f, 25f), Point(160f, 25f), 4f)
        assertEquals(2, hits.size)

        AreaEraser.erase(page, hits)

        assertTrue(page.shapes.isEmpty())
    }
}
