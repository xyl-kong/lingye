package com.example.tabletworkspace

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CanvasViewportGesturesTest {
    @Test fun requiresTwoFingersWithoutStylus() {
        assertFalse(CanvasViewportGestures.mayStart(1, false))
        assertTrue(CanvasViewportGestures.mayStart(2, false))
        assertFalse(CanvasViewportGestures.mayStart(2, true))
    }

    @Test fun twoFingerPanPreservesScale() {
        val result = CanvasViewportGestures.transform(
            1f, Offset.Zero,
            Offset(100f, 100f), Offset(200f, 100f),
            Offset(120f, 130f), Offset(220f, 130f)
        )
        assertEquals(1f, result.scale, 0.0001f)
        assertEquals(20f, result.pan.x, 0.0001f)
        assertEquals(30f, result.pan.y, 0.0001f)
    }

    @Test fun zoomKeepsWorldPointUnderCentroid() {
        val result = CanvasViewportGestures.transform(
            1f, Offset.Zero,
            Offset(100f, 100f), Offset(200f, 100f),
            Offset(90f, 110f), Offset(210f, 110f)
        )
        assertEquals(1.2f, result.scale, 0.0001f)
        assertEquals(-30f, result.pan.x, 0.0001f)
        assertEquals(-10f, result.pan.y, 0.0001f)
    }

    @Test fun clampDoesNotMovePastMaximumZoom() {
        val result = CanvasViewportGestures.transform(
            4f, Offset(15f, 20f),
            Offset(100f, 100f), Offset(200f, 100f),
            Offset(50f, 100f), Offset(250f, 100f)
        )
        assertEquals(4f, result.scale, 0.0001f)
        assertEquals(15f, result.pan.x, 0.0001f)
        assertEquals(20f, result.pan.y, 0.0001f)
    }
}
