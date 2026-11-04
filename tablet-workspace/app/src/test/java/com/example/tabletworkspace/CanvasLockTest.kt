package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CanvasLockTest {

    private fun pageWithLocked(): Page = Page.create("p").apply {
        addShape("rect", 0f, 0f, 100f, 100f)
        shapes[0] = shapes[0].copy(locked = true)
        addShape("rect", 200f, 0f, 100f, 100f)
    }

    @Test
    fun lockedShapeIsRecognisedOnlyForShapeBackedObjects() {
        val page = pageWithLocked()
        val locked = CanvasSelection("shape", 0)
        assertTrue(CanvasObjectSpecs.isLocked(page, locked))
        assertTrue(CanvasObjectSpecs.supportsLock(page, locked))
        assertFalse(CanvasObjectSpecs.isLocked(page, CanvasSelection("shape", 1)))
        assertFalse(CanvasObjectSpecs.supportsLock(page, CanvasSelection("image", 0)))
        assertTrue(CanvasObjectSpecs.hasLocked(page, listOf(locked, CanvasSelection("shape", 1))))
        assertEquals(listOf(CanvasSelection("shape", 1)), CanvasObjectSpecs.unlocked(page, listOf(locked, CanvasSelection("shape", 1))))
    }

    @Test
    fun moveAndDeleteSkipLockedShapes() {
        val page = pageWithLocked()
        page.moveSelections(listOf(CanvasSelection("shape", 0), CanvasSelection("shape", 1)), 10f, 10f)
        assertEquals(0f, page.shapes[0].x, 0.01f)
        assertEquals(210f, page.shapes[1].x, 0.01f)
        page.deleteSelection(CanvasSelection("shape", 0))
        assertEquals(2, page.shapes.size)
        page.deleteSelection(CanvasSelection("shape", 1))
        assertEquals(1, page.shapes.size)
    }

    @Test
    fun resizeAndAlignSkipLockedShapes() {
        val page = pageWithLocked()
        page.resizeSelectionsAnchored(listOf(CanvasSelection("shape", 0), CanvasSelection("shape", 1)), 2f, Point(0f, 0f))
        assertEquals(100f, page.shapes[0].width, 0.01f)
        assertEquals(200f, page.shapes[1].width, 0.01f)
        page.alignSelections(listOf(CanvasSelection("shape", 0), CanvasSelection("shape", 1)), "left")
        assertEquals(0f, page.shapes[0].x, 0.01f)
        assertEquals(400f, page.shapes[1].x, 0.01f)
    }

    @Test
    fun groupingExcludesLockedShapesWithoutThrowing() {
        val page = pageWithLocked()
        // one locked + one unlocked cannot form a group; must be a no-op rather than require() failure
        page.groupSelections(listOf(CanvasSelection("shape", 0), CanvasSelection("shape", 1)))
        assertEquals("", page.shapes[0].group)
        assertEquals("", page.shapes[1].group)
    }

    @Test
    fun ungroupLeavesGroupsContainingLockedShapesIntact() {
        val page = Page.create("p").apply {
            addShape("rect", 0f, 0f, 100f, 100f)
            addShape("rect", 200f, 0f, 100f, 100f)
        }
        page.groupSelections(listOf(CanvasSelection("shape", 0), CanvasSelection("shape", 1)))
        val group = page.shapes[0].group
        assertTrue(group.isNotEmpty())
        page.shapes[0] = page.shapes[0].copy(locked = true)
        page.ungroupSelections(listOf(CanvasSelection("shape", 0), CanvasSelection("shape", 1)))
        assertEquals(group, page.shapes[0].group)
        assertEquals(group, page.shapes[1].group)
    }

    @Test
    fun duplicateOfLockedShapeIsUnlocked() {
        val page = pageWithLocked()
        val copied = page.duplicateSelections(listOf(CanvasSelection("shape", 0)))
        assertEquals(1, copied.size)
        val copy = page.shapes[copied.first().index]
        assertFalse(copy.locked)
    }
}
