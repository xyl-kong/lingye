package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextualToolbarPositionTest {

    private fun resolve(
        anchorX: Float = 100f,
        top: Float = 200f,
        bottom: Float = 260f,
        width: Float = 200f,
        height: Float = 44f,
        viewportWidth: Float = 1000f,
        viewportHeight: Float = 800f,
        imeBottom: Float = 0f,
        alignCenter: Boolean = false
    ) = resolveToolbarPlacement(
        anchorX = anchorX,
        anchorTop = top,
        anchorBottom = bottom,
        toolbarWidth = width,
        toolbarHeight = height,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
        imeBottom = imeBottom,
        margin = 12f,
        gap = 8f,
        bottomReserve = 92f,
        alignCenter = alignCenter
    )

    @Test
    fun placesAboveObjectWithMargin() {
        val p = resolve(anchorX = 100f, top = 300f, bottom = 350f)
        assertEquals(100f, p.x, 0.01f)
        assertEquals(248f, p.y, 0.01f)
        assertFalse(p.below)
    }

    @Test
    fun clampsToLeftAndRightSafeMargins() {
        val left = resolve(anchorX = -50f, top = 300f, bottom = 350f)
        assertEquals(12f, left.x, 0.01f)
        val right = resolve(anchorX = 950f, top = 300f, bottom = 350f)
        assertEquals(788f, right.x, 0.01f)
    }

    @Test
    fun centersConnectorToolbarOnMidpoint() {
        val p = resolve(anchorX = 500f, top = 300f, bottom = 300f, width = 180f, height = 44f, alignCenter = true)
        assertEquals(410f, p.x, 0.01f)
    }

    @Test
    fun flipsBelowWhenNoRoomAbove() {
        val p = resolve(anchorX = 100f, top = 20f, bottom = 80f)
        assertEquals(88f, p.y, 0.01f)
        assertTrue(p.below)
    }

    @Test
    fun neverCoversBottomToolbar() {
        val p = resolve(anchorX = 100f, top = 760f, bottom = 790f)
        assertEquals(800f - 92f - 44f, p.y, 0.01f)
    }

    @Test
    fun imePushesToolbarAboveKeyboard() {
        val p = resolve(anchorX = 100f, top = 700f, bottom = 740f, imeBottom = 300f)
        assertEquals(800f - 300f - 12f - 44f, p.y, 0.01f)
    }

    @Test
    fun handlesToolbarWiderThanViewportWithoutNegativeCoordinates() {
        val p = resolve(anchorX = 50f, top = 200f, bottom = 240f, width = 1200f, viewportWidth = 800f)
        assertTrue(p.x >= 12f)
        assertTrue(p.y >= 12f)
    }
}
