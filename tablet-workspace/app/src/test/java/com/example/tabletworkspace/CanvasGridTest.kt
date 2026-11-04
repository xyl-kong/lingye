package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Test

class CanvasGridTest {
    @Test fun gridPhaseStaysAlignedForPositiveAndNegativePanning() {
        assertEquals(3f, gridPhase(3f, 10f), 0.001f)
        assertEquals(7f, gridPhase(-3f, 10f), 0.001f)
        assertEquals(0f, gridPhase(20f, 10f), 0.001f)
    }
}
