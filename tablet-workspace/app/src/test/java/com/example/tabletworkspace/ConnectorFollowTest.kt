package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectorFollowTest {
    private fun pageWithShapeAndConnector(
        endX: Float = 100f,
        endY: Float = 40f,
        shapeLocked: Boolean = false
    ): Page {
        val page = Page.create("follow")
        page.addShape("rect", 0f, 0f, 100f, 100f)
        if (shapeLocked) page.shapes[0] = page.shapes[0].copy(locked = true)
        page.addConnector(300f, 40f, endX, endY)
        return page
    }

    @Test
    fun freeEndpointOnEdgeFollowsShapeTranslation() {
        val page = pageWithShapeAndConnector()
        page.moveSelections(listOf(CanvasSelection("shape", 0)), 50f, 30f)
        val line = page.connectors[0]
        assertEquals(300f, line.x1, 0.001f)
        assertEquals(40f, line.y1, 0.001f)
        assertEquals(150f, line.x2, 0.001f)
        assertEquals(70f, line.y2, 0.001f)
    }

    @Test
    fun freeEndpointAwayFromEdgeDoesNotFollow() {
        val page = pageWithShapeAndConnector(endX = 250f, endY = 250f)
        page.moveSelections(listOf(CanvasSelection("shape", 0)), 50f, 30f)
        val line = page.connectors[0]
        assertEquals(250f, line.x2, 0.001f)
        assertEquals(250f, line.y2, 0.001f)
    }

    @Test
    fun bothFreeEndpointsOnEdgesFollow() {
        val page = Page.create("both")
        page.addShape("rect", 0f, 0f, 100f, 100f)
        page.addConnector(100f, 20f, 100f, 80f)
        page.moveSelections(listOf(CanvasSelection("shape", 0)), 10f, 5f)
        val line = page.connectors[0]
        assertEquals(110f, line.x1, 0.001f)
        assertEquals(25f, line.y1, 0.001f)
        assertEquals(110f, line.x2, 0.001f)
        assertEquals(85f, line.y2, 0.001f)
    }

    @Test
    fun selectedConnectorMovesOnceNotTwice() {
        val page = pageWithShapeAndConnector()
        page.moveSelections(
            listOf(CanvasSelection("shape", 0), CanvasSelection("connector", 0)),
            20f, 20f
        )
        val line = page.connectors[0]
        assertEquals(320f, line.x1, 0.001f)
        assertEquals(60f, line.y1, 0.001f)
        assertEquals(120f, line.x2, 0.001f)
        assertEquals(60f, line.y2, 0.001f)
    }

    @Test
    fun lockedShapeDoesNotMoveOrDragEndpoint() {
        val page = pageWithShapeAndConnector(shapeLocked = true)
        page.moveSelections(listOf(CanvasSelection("shape", 0)), 20f, 20f)
        assertEquals(0f, page.shapes[0].x, 0.001f)
        assertEquals(100f, page.connectors[0].x2, 0.001f)
    }

    @Test
    fun boundaryToleranceRecognizesExactAndNearPoints() {
        assertTrue(ConnectorSnapping.isPointOnRectEdge(Point(100f, 40f), 0f, 0f, 100f, 100f, 2f))
        assertTrue(ConnectorSnapping.isPointOnRectEdge(Point(101f, 40f), 0f, 0f, 100f, 100f, 2f))
        assertFalse(ConnectorSnapping.isPointOnRectEdge(Point(103f, 40f), 0f, 0f, 100f, 100f, 2f))
        assertFalse(ConnectorSnapping.isPointOnRectEdge(Point(50f, 50f), 0f, 0f, 100f, 100f, 2f))
    }
}
