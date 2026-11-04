package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CanvasPropertyEditorTest {

    @Test
    fun shapeUpdateReadsCurrentValueAndNotifiesOnce() {
        val page = Page.create("p").apply { addShape("rect", 0f, 0f, 100f, 100f) }
        var changes = 0
        val ok = CanvasPropertyEditor.updateShape(page, 0, { changes++ }) { it.copy(color = 0xff112233.toInt()) }
        assertTrue(ok)
        assertEquals(0xff112233.toInt(), page.shapes[0].color)
        assertEquals(1, changes)
    }

    @Test
    fun lockedShapePropertyUpdateIsRejectedWithoutHistoryEntry() {
        val page = Page.create("p").apply {
            addShape("rect", 0f, 0f, 100f, 100f)
            shapes[0] = shapes[0].copy(locked = true)
        }
        var changes = 0
        val ok = CanvasPropertyEditor.updateShape(page, 0, { changes++ }) { it.copy(color = 0xff112233.toInt()) }
        assertFalse(ok)
        assertEquals(0, changes)
        assertEquals(0xffffd866.toInt(), page.shapes[0].color)
    }

    @Test
    fun imageConnectorAndStrokeUpdatesAreValidated() {
        val page = Page.create("p")
        page.addCanvasImage("image.img", 0f, 0f, 100f, 100f)
        page.addConnector(0f, 0f, 50f, 50f)
        page.addStyledStroke(listOf(Point(0f, 0f), Point(10f, 10f)), 0xff000000.toInt(), 2f)
        var changes = 0
        assertTrue(CanvasPropertyEditor.updateImage(page, 0, { changes++ }) { it.copy(width = 120f) })
        assertTrue(CanvasPropertyEditor.updateConnector(page, 0, { changes++ }) { it.copy(width = 4f) })
        assertTrue(CanvasPropertyEditor.updateStrokeStyle(page, 0, { changes++ }) { it.copy(width = 6f) })
        assertEquals(120f, page.images[0].width, 0.01f)
        assertEquals(4f, page.connectors[0].width, 0.01f)
        assertEquals(6f, page.strokeStyles[0].width, 0.01f)
        assertEquals(3, changes)
        assertFalse(CanvasPropertyEditor.updateShape(page, 99, { changes++ }) { it })
        assertEquals(3, changes)
    }
}
