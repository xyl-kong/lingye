package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartGuidesTest {
    private fun box(l: Float, t: Float, r: Float, b: Float) = CanvasBounds(l, t, r, b)

    @Test
    fun snapsLeftEdgeAndBuildsVerticalGuide() {
        val result = SmartGuides.snap(box(100f, 100f, 150f, 140f), listOf(box(98f, 50f, 120f, 80f)))

        assertEquals(-2f, result.dx, 0.001f)
        assertEquals(0f, result.dy, 0.001f)
        assertEquals(98f, result.bounds.left, 0.001f)
        assertEquals(1, result.guides.size)
        val guide = result.guides.single()
        assertTrue(guide.vertical)
        assertEquals(98f, guide.position, 0.001f)
        assertEquals(50f, guide.start, 0.001f)
        assertEquals(140f, guide.end, 0.001f)
    }

    @Test
    fun thresholdIsInclusive() {
        val result = SmartGuides.snap(box(100f, 100f, 150f, 140f), listOf(box(95f, 0f, 120f, 40f)))
        assertEquals(-5f, result.dx, 0.001f)
    }

    @Test
    fun doesNotSnapBeyondThreshold() {
        val result = SmartGuides.snap(box(100f, 100f, 150f, 140f), listOf(box(60f, 0f, 84f, 20f)))
        assertEquals(0f, result.dx, 0.001f)
        assertEquals(0f, result.dy, 0.001f)
        assertTrue(result.guides.isEmpty())
    }

    @Test
    fun picksTheNearestCandidate() {
        val result = SmartGuides.snap(
            box(100f, 100f, 150f, 140f),
            listOf(box(96f, 0f, 120f, 40f), box(103f, 0f, 130f, 40f))
        )
        assertEquals(3f, result.dx, 0.001f)
        assertEquals(103f, result.guides.first { it.vertical }.position, 0.001f)
    }

    @Test
    fun snapsCentres() {
        val result = SmartGuides.snap(box(100f, 100f, 200f, 200f), listOf(box(140f, 300f, 164f, 340f)))
        assertEquals(2f, result.dx, 0.001f)
        val guide = result.guides.single()
        assertTrue(guide.vertical)
        assertEquals(152f, guide.position, 0.001f)
    }

    @Test
    fun snapsHorizontalEdgeAndBuildsHorizontalGuide() {
        val result = SmartGuides.snap(box(100f, 100f, 200f, 200f), listOf(box(160f, 204f, 260f, 260f)))
        assertEquals(4f, result.dy, 0.001f)
        assertEquals(0f, result.dx, 0.001f)
        val guide = result.guides.single()
        assertTrue(!guide.vertical)
        assertEquals(204f, guide.position, 0.001f)
        assertEquals(100f, guide.start, 0.001f)
        assertEquals(260f, guide.end, 0.001f)
    }

    @Test
    fun snapsBothAxesAtOnce() {
        val result = SmartGuides.snap(box(100f, 100f, 150f, 150f), listOf(box(102f, 98f, 140f, 130f)))
        assertEquals(2f, result.dx, 0.001f)
        assertEquals(-2f, result.dy, 0.001f)
        assertEquals(2, result.guides.size)
        assertTrue(result.guides.any { it.vertical && it.position == 102f })
        assertTrue(result.guides.any { !it.vertical && it.position == 98f })
    }

    @Test
    fun oneGuideSpansEveryAlignedElement() {
        val result = SmartGuides.snap(
            box(100f, 100f, 150f, 150f),
            listOf(box(98f, 0f, 120f, 20f), box(98f, 200f, 120f, 260f))
        )
        val guide = result.guides.single()
        assertEquals(0f, guide.start, 0.001f)
        assertEquals(260f, guide.end, 0.001f)
    }

    @Test
    fun emptyOthersLeavesBoxUnchanged() {
        val moving = box(10f, 10f, 20f, 20f)
        val result = SmartGuides.snap(moving, emptyList())
        assertEquals(moving, result.bounds)
        assertTrue(result.guides.isEmpty())
    }

    @Test
    fun zeroThresholdNeverSnaps() {
        val result = SmartGuides.snap(box(100f, 100f, 150f, 150f), listOf(box(100f, 100f, 150f, 150f)), threshold = 0f)
        assertEquals(0f, result.dx, 0.001f)
        assertTrue(result.guides.isEmpty())
    }
}
