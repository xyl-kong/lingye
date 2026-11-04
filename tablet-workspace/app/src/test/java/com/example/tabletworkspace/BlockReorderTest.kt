package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Test

class BlockReorderTest {
    private val centers = mapOf(0 to 100f, 1 to 300f, 2 to 500f)

    @Test
    fun draggedPointerSelectsNearestBlockCenter() {
        assertEquals(2, BlockReorderCalculator.targetIndex(0, 480f, centers))
        assertEquals(1, BlockReorderCalculator.targetIndex(2, 235f, centers))
    }

    @Test
    fun draggingBeyondDocumentClampsToFirstOrLastBlock() {
        assertEquals(0, BlockReorderCalculator.targetIndex(2, -400f, centers))
        assertEquals(2, BlockReorderCalculator.targetIndex(0, 900f, centers))
    }

    @Test
    fun smallMovementKeepsCurrentBlock() {
        assertEquals(1, BlockReorderCalculator.targetIndex(1, 310f, centers))
    }
}
