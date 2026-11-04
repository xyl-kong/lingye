package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Test

class CanvasZoomTest {
    @Test
    fun lowerBoundIsTenPercent() {
        assertEquals(0.1f, CanvasZoom.MIN, 0.0001f)
        assertEquals(0.1f, CanvasZoom.clamp(0.02f), 0.0001f)
        assertEquals(0.1f, CanvasZoom.clamp(-3f), 0.0001f)
    }

    @Test
    fun keepsValuesInsideTheRange() {
        assertEquals(0.1f, CanvasZoom.clamp(0.1f), 0.0001f)
        assertEquals(0.35f, CanvasZoom.clamp(0.35f), 0.0001f)
        assertEquals(1f, CanvasZoom.DEFAULT, 0.0001f)
    }

    @Test
    fun upperBoundIsFourHundredPercent() {
        assertEquals(4f, CanvasZoom.clamp(9f), 0.0001f)
        assertEquals(4f, CanvasZoom.MAX, 0.0001f)
    }
}
