package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FrameSemanticsTest {
    private fun pageWithFrame(): Page = Page.create("Frame page").apply {
        addShape("frame", 100f, 100f, 400f, 300f)
        shapes[0] = shapes[0].copy(title = "Section A")
    }

    @Test
    fun frameIsHitOnBorderAndTitleButNotInBody() {
        val frame = pageWithFrame().shapes[0]
        assertTrue("title strip", FrameSemantics.hit(frame, Point(300f, 90f), 8f))
        assertTrue("left border", FrameSemantics.hit(frame, Point(102f, 250f), 8f))
        assertTrue("bottom border", FrameSemantics.hit(frame, Point(300f, 398f), 8f))
        assertFalse("body passes through", FrameSemantics.hit(frame, Point(300f, 250f), 8f))
        assertFalse("far outside", FrameSemantics.hit(frame, Point(600f, 250f), 8f))
        assertFalse("plain shapes keep area hits", FrameSemantics.hit(Shape("rect", 0f, 0f, 10f, 10f), Point(5f, 5f), 1f))
    }

    @Test
    fun frameMembersAreFullyContainedObjectsOnly() {
        val page = pageWithFrame()
        page.addShape("rect", 150f, 150f, 100f, 80f)          // fully inside
        page.addShape("rect", 450f, 200f, 100f, 80f)          // crosses the right border
        page.addShape("card", 500f, 500f, 100f, 80f)          // outside
        page.addCanvasImage("inside.png", 120f, 320f, 60f, 40f)
        page.addCanvasImage("outside.png", 0f, 0f, 60f, 40f)
        page.addStyledStroke(listOf(Point(200f, 200f), Point(260f, 260f)), 0xff000000.toInt(), 3f)
        page.addStyledStroke(listOf(Point(0f, 0f), Point(50f, 50f)), 0xff000000.toInt(), 3f)
        page.addConnector(180f, 180f, 240f, 220f)             // free connector inside
        page.addShape("frame", 120f, 120f, 80f, 80f)          // nested frame stays separate

        val members = page.frameMembers(0)

        assertTrue(CanvasSelection("shape", 1) in members)
        assertFalse(CanvasSelection("shape", 2) in members)
        assertFalse(CanvasSelection("shape", 0) in members)
        assertFalse(CanvasSelection("shape", 3) in members)
        assertTrue(CanvasSelection("image", 0) in members)
        assertFalse(CanvasSelection("image", 1) in members)
        assertTrue(CanvasSelection("stroke", 0) in members)
        assertFalse(CanvasSelection("stroke", 1) in members)
        assertTrue(CanvasSelection("connector", 0) in members)
        assertTrue("nested frame is a member", CanvasSelection("shape", page.shapes.lastIndex) in members)
    }

    @Test
    fun nestedFrameBelongsToOuterAndOwnsItsContents() {
        val page = pageWithFrame()
        page.addShape("frame", 120f, 120f, 120f, 100f)
        page.addShape("rect", 140f, 140f, 40f, 30f)

        val outer = page.frameMembers(0)
        val inner = page.frameMembers(1)

        assertTrue(CanvasSelection("shape", 1) in outer)
        assertTrue(CanvasSelection("shape", 2) in outer)
        assertEquals(listOf(CanvasSelection("shape", 2)), inner)
    }

    @Test
    fun movingOuterFrameMovesNestedFrameAndContentsOnce() {
        val page = pageWithFrame()
        page.addShape("frame", 120f, 120f, 120f, 100f)
        page.addShape("rect", 140f, 140f, 40f, 30f)

        page.moveSelections(listOf(CanvasSelection("shape", 0)), 40f, 30f)

        assertEquals(160f, page.shapes[1].x, 0.001f)
        assertEquals(150f, page.shapes[1].y, 0.001f)
        assertEquals(180f, page.shapes[2].x, 0.001f)
        assertEquals(170f, page.shapes[2].y, 0.001f)
    }

    @Test
    fun movingFrameCarriesMembersAndLeavesOutsiders() {
        val page = pageWithFrame()
        page.addShape("rect", 150f, 150f, 100f, 80f)
        page.addShape("rect", 600f, 600f, 100f, 80f)

        page.moveSelections(listOf(CanvasSelection("shape", 0)), 40f, 30f)

        assertEquals(140f, page.shapes[0].x, 0.001f)
        assertEquals(130f, page.shapes[0].y, 0.001f)
        assertEquals(190f, page.shapes[1].x, 0.001f)
        assertEquals(180f, page.shapes[1].y, 0.001f)
        assertEquals(600f, page.shapes[2].x, 0.001f)
        assertEquals(0f, page.shapes[2].y - 600f, 0.001f)
    }

    @Test
    fun resizingFrameKeepsMemberPositions() {
        val page = pageWithFrame()
        page.addShape("rect", 150f, 150f, 100f, 80f)

        page.resizeSelections(listOf(CanvasSelection("shape", 0)), 1.4f)

        assertEquals(150f, page.shapes[1].x, 0.001f)
        assertEquals(150f, page.shapes[1].y, 0.001f)
        assertTrue(page.shapes[0].width > 400f)
    }

    @Test
    fun duplicatingFrameCopiesItsMembers() {
        val page = pageWithFrame()
        page.addShape("rect", 150f, 150f, 100f, 80f)

        val created = page.duplicateSelections(listOf(CanvasSelection("shape", 0)))

        assertEquals(2, created.size)
        val frameCopy = created.first()
        val memberCopy = created.last()
        assertEquals("shape", frameCopy.type)
        assertEquals(124f, page.shapes[frameCopy.index].x, 0.001f)
        assertEquals(174f, page.shapes[memberCopy.index].x, 0.001f)
        assertTrue(page.frameMembers(frameCopy.index).any { it == memberCopy })
    }

    @Test
    fun fitCentersFrameAndClampsScale() {
        val frame = pageWithFrame().shapes[0]

        val fit = FrameSemantics.fit(frame, 800f, 600f)!!

        assertEquals(460f / 300f, fit.scale, 0.001f)
        assertEquals(400f - (frame.x + frame.width / 2f) * fit.scale, fit.offsetX, 0.001f)
        assertEquals(300f - (frame.y + FrameSemantics.TITLE_ZONE / 2f + frame.height / 2f) * fit.scale, fit.offsetY, 0.001f)
        assertEquals(FrameSemantics.MAX_SCALE, FrameSemantics.fit(frame, 10000f, 10000f)!!.scale, 0.001f)
        assertEquals(FrameSemantics.MIN_SCALE, FrameSemantics.fit(frame, 80f, 80f)!!.scale, 0.001f)
    }

    @Test
    fun fitReturnsNullForDegenerateInput() {
        val frame = pageWithFrame().shapes[0]
        assertNull(FrameSemantics.fit(frame, 0f, 600f))
        assertNull(FrameSemantics.fit(frame.copy(width = 0f), 800f, 600f))
        assertNull(FrameSemantics.fit(Shape("rect", 0f, 0f, 10f, 10f), 800f, 600f))
    }

    @Test
    fun fillCoversCanvasAndCentersFrame() {
        val frame = pageWithFrame().shapes[0]

        val fill = FrameSemantics.fill(frame, 800f, 600f)!!

        assertEquals(2f, fill.scale, 0.001f)
        assertEquals(-200f, fill.offsetX, 0.001f)
        assertEquals(-200f, fill.offsetY, 0.001f)
        assertEquals(FrameSemantics.MAX_SCALE, FrameSemantics.fill(frame.copy(width = 10f, height = 10f), 800f, 600f)!!.scale, 0.001f)
        assertEquals(FrameSemantics.PRESENTATION_MAX_SCALE, FrameSemantics.fill(frame.copy(width = 10f, height = 10f), 800f, 600f, FrameSemantics.PRESENTATION_MIN_SCALE, FrameSemantics.PRESENTATION_MAX_SCALE)!!.scale, 0.001f)
        assertEquals(FrameSemantics.MIN_SCALE, FrameSemantics.fill(frame.copy(width = 10000f, height = 10000f), 800f, 600f)!!.scale, 0.001f)
        assertNull(FrameSemantics.fill(Shape("rect", 0f, 0f, 10f, 10f), 800f, 600f))
    }

    @Test
    fun deletingFrameLeavesItsMembersOnThePage() {
        val page = pageWithFrame()
        page.addShape("rect", 150f, 150f, 100f, 80f)

        page.deleteSelection(CanvasSelection("shape", 0))

        assertEquals(1, page.shapes.size)
        assertEquals("rect", page.shapes[0].type)
    }
}
