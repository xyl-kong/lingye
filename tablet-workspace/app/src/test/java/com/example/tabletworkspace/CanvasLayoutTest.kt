package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Test

class CanvasLayoutTest {
    @Test
    fun clampsToRightEdgeOnNarrowViewport() {
        assertEquals(208f, CanvasLayout.toolbarX(900f, viewportWidthDp = 640f, toolbarWidthDp = 420f), 0.001f)
    }

    @Test
    fun keepsSmallPositionsAtTheMargin() {
        assertEquals(12f, CanvasLayout.toolbarX(2f, viewportWidthDp = 800f, toolbarWidthDp = 300f), 0.001f)
    }

    @Test
    fun wideViewportLeavesPositionUntouched() {
        assertEquals(500f, CanvasLayout.toolbarX(500f, viewportWidthDp = 1280f, toolbarWidthDp = 420f), 0.001f)
    }

    @Test
    fun toolbarWiderThanViewportFallsBackToMargin() {
        assertEquals(12f, CanvasLayout.toolbarX(300f, viewportWidthDp = 360f, toolbarWidthDp = 420f), 0.001f)
    }

    @Test
    fun parameterBarLeavesRoomOnBothSides() {
        assertEquals(624f, CanvasLayout.parameterBarMaxWidth(640f), 0.001f)
    }

    @Test
    fun parameterBarKeepsMinimumWidthWhenViewportCollapses() {
        assertEquals(240f, CanvasLayout.parameterBarMaxWidth(200f), 0.001f)
    }
}
