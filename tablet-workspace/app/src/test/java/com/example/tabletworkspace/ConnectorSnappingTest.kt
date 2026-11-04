package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectorSnappingTest {
    private fun rect(
        left: Float = 0f,
        top: Float = 0f,
        right: Float = 100f,
        bottom: Float = 100f,
        withAnchors: Boolean = true,
        shapeIndex: Int = 0
    ) = ConnectorSnapRect(
        key = shapeIndex,
        left = left,
        top = top,
        right = right,
        bottom = bottom,
        anchors = if (withAnchors) ConnectorSnapping.anchorsOf(left, top, right, bottom) else emptyList(),
        shapeIndex = shapeIndex
    )

    private fun snap(point: Point, rects: List<ConnectorSnapRect>, anchor: Float = 24f, edge: Float = 30f) =
        ConnectorSnapping.snap(point, rects, anchorThreshold = anchor, edgeThreshold = edge)

    @Test
    fun onlyFourEdgeMidpointsAreAnchors() {
        val anchors = ConnectorSnapping.anchorsOf(0f, 0f, 100f, 60f)
        assertEquals(listOf("left", "right", "top", "bottom"), anchors.map { it.port })
        assertEquals(Point(0f, 30f), anchors[0].point)
        assertEquals(Point(100f, 30f), anchors[1].point)
        assertEquals(Point(50f, 0f), anchors[2].point)
        assertEquals(Point(50f, 60f), anchors[3].point)
    }

    @Test
    fun pointNearRightAnchorSnapsToAnchor() {
        val result = snap(Point(120f, 50f), listOf(rect()))
        assertEquals(ConnectorSnapKind.ANCHOR, result.kind)
        assertEquals("right", result.port)
        assertEquals(Point(100f, 50f), result.point)
        assertEquals(20f, result.distance, 0.001f)
    }

    @Test
    fun pointNearTopAnchorSnapsToAnchor() {
        val result = snap(Point(50f, 20f), listOf(rect()))
        assertEquals(ConnectorSnapKind.ANCHOR, result.kind)
        assertEquals("top", result.port)
        assertEquals(Point(50f, 0f), result.point)
    }

    @Test
    fun anyPointAlongEdgeBecomesEdgeDropPoint() {
        val result = snap(Point(10f, 3f), listOf(rect()))
        assertEquals(ConnectorSnapKind.EDGE, result.kind)
        assertEquals(Point(10f, 0f), result.point)
        assertEquals("top", result.edge)
    }

    @Test
    fun pointOnLeftEdgeAwayFromAnchorBecomesEdgeDropPoint() {
        val result = snap(Point(2f, 80f), listOf(rect()))
        assertEquals(ConnectorSnapKind.EDGE, result.kind)
        assertEquals(Point(0f, 80f), result.point)
        assertEquals("left", result.edge)
    }

    @Test
    fun pointInsideCardFarFromEdgeIsNotSnapped() {
        val pointer = Point(50f, 50f)
        val result = snap(pointer, listOf(rect()))
        assertEquals(ConnectorSnapKind.NONE, result.kind)
        assertEquals(pointer, result.point)
    }

    @Test
    fun pointOutsideEdgeToleranceIsNotSnapped() {
        val result = snap(Point(135f, 80f), listOf(rect()))
        assertEquals(ConnectorSnapKind.NONE, result.kind)
        assertNull(result.shapeIndex)
    }

    @Test
    fun edgeSnapIsContinuousAlongTheWholeEdge() {
        listOf(5f, 25f, 75f, 95f).forEach { y ->
            val result = snap(Point(2f, y), listOf(rect()))
            assertEquals(ConnectorSnapKind.EDGE, result.kind)
            assertEquals(Point(0f, y), result.point)
            assertEquals("left", result.edge)
        }
    }

    @Test
    fun edgeThresholdIsInclusive() {
        val result = snap(Point(130f, 20f), listOf(rect()))
        assertEquals(ConnectorSnapKind.EDGE, result.kind)
        assertEquals(Point(100f, 20f), result.point)
        assertEquals(30f, result.distance, 0.001f)
    }

    @Test
    fun imageSupportsEdgeDropPointsButNoAnchors() {
        val image = rect(withAnchors = false)
        val onEdge = snap(Point(42f, 3f), listOf(image))
        assertEquals(ConnectorSnapKind.EDGE, onEdge.kind)
        assertEquals(Point(42f, 0f), onEdge.point)
        val inside = snap(Point(50f, 50f), listOf(image))
        assertEquals(ConnectorSnapKind.NONE, inside.kind)
    }

    @Test
    fun picksNearestAnchor() {
        val near = rect(200f, 0f, 300f, 100f, shapeIndex = 1)
        val far = rect(0f, 0f, 100f, 100f, shapeIndex = 0)
        val result = snap(Point(205f, 50f), listOf(far, near))
        assertEquals(ConnectorSnapKind.ANCHOR, result.kind)
        assertEquals(1, result.shapeIndex)
        assertEquals("left", result.port)
        assertEquals(Point(200f, 50f), result.point)
    }

    @Test
    fun anchorThresholdIsInclusive() {
        val result = snap(Point(124f, 50f), listOf(rect()))
        assertEquals(ConnectorSnapKind.ANCHOR, result.kind)
        assertEquals(24f, result.distance, 0.001f)
    }

    @Test
    fun pageSnapRectsExcludeSourceAndIncludeImages() {
        val page = Page.create("snap")
        page.addShape("rect", 0f, 0f, 100f, 100f)
        page.addShape("text", 200f, 0f, 100f, 100f)
        page.addCanvasImage("photo.png", 400f, 0f, 100f, 100f)
        val rects = page.connectorSnapRects(excludeShape = 0)
        assertEquals(2, rects.size)
        assertTrue(rects.none { it.shapeIndex == 0 })
        assertEquals(4, rects.first { it.shapeIndex == 1 }.anchors.size)
        assertTrue(rects.any { it.shapeIndex == null })
    }

    @Test
    fun closestPointOnRectEdgeHandlesInsideAndOutside() {
        val (outside, outsideEdge) = ConnectorSnapping.closestPointOnRectEdge(Point(110f, 110f), 0f, 0f, 100f, 100f)
        assertEquals(Point(100f, 100f), outside)
        assertEquals("right", outsideEdge)
        val (inside, insideEdge) = ConnectorSnapping.closestPointOnRectEdge(Point(42f, 18f), 0f, 0f, 100f, 100f)
        assertEquals(Point(42f, 0f), inside)
        assertEquals("top", insideEdge)
    }
}
