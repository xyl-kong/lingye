package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageHistory

import org.junit.Assert.*
import org.junit.Test

class InkTest {
    @Test fun canceledEraseRestoresInkIdsAndStyles() {
        val p=Page.create("ink"); p.addStyledStroke(listOf(Point(10f,10f),Point(20f,20f)),0xff000000.toInt(),4f)
        val id=p.strokeIds.single(); val gesture=PageGesture(p)
        assertTrue(p.eraseAt(Point(10f,10f),5f)); assertTrue(p.strokeIds.isEmpty()); gesture.cancel()
        assertEquals(id,p.strokeIds.single()); assertEquals(4f,p.strokeStyles.single().width,0.001f)
    }
    @Test fun styledStrokesSurviveSaveAndUndo() {
        val page = Page.create("ink"); val history = PageHistory(page)
        page.addStyledStroke(listOf(Point(2f,3f), Point(90f,3f)), 0xffd2313b.toInt(), 8f)
        history.record(page)
        val restored = Page.decode(page.encode())
        assertEquals(0xffd2313b.toInt(), restored.strokeStyles[0].color)
        assertEquals(8f, restored.strokeStyles[0].width, 0.001f)
        assertTrue(history.undo()!!.strokes.isEmpty())
        assertEquals(8f, history.redo()!!.strokeStyles[0].width, 0.001f)
    }
    @Test fun eraserHitsSegmentBetweenSparsePointsAndPreservesOtherStrokes() {
        val page = Page.create("erase")
        page.addStyledStroke(listOf(Point(0f,0f),Point(100f,0f)), 123, 4f)
        page.addStyledStroke(listOf(Point(0f,50f),Point(100f,50f)), 456, 7f)
        assertTrue(page.eraseAt(Point(50f, 3f), 4f))
        assertEquals(1, page.strokes.size)
        assertEquals(456, Page.decode(page.encode()).strokeStyles.single().color)
        assertFalse(page.eraseAt(Point(500f,500f), 4f))
    }
    @Test fun penOnlyRejectsFingerButAcceptsStylusAndEraser() {
        assertFalse(inkAcceptsPointer(true, "touch"))
        assertTrue(inkAcceptsPointer(true, "stylus"))
        assertTrue(inkAcceptsPointer(true, "eraser"))
        assertTrue(inkAcceptsPointer(false, "touch"))
    }
    @Test fun versionThreeStrokeGetsOriginalDefaultStyle() {
        val bytes = java.io.ByteArrayOutputStream()
        java.io.DataOutputStream(bytes).use { out ->
            out.writeInt(3); out.writeUTF("legacy"); out.writeUTF("old"); out.writeInt(0)
            out.writeFloat(80f); out.writeFloat(100f); out.writeInt(1)
            out.writeInt(2); out.writeFloat(1f); out.writeFloat(2f); out.writeFloat(3f); out.writeFloat(4f)
            out.writeInt(0); out.writeInt(0); out.writeInt(0); out.writeInt(0)
        }
        val page = Page.decode(bytes.toByteArray())
        assertEquals(InkStyle(), page.strokeStyles.single())
        assertEquals(InkStyle(), Page.decode(page.encode()).strokeStyles.single())
    }
    @Test fun eraserSweepBetweenSamplesCrossesStroke() {
        val page = Page.create("erase sweep")
        page.addStroke(listOf(Point(0f,50f), Point(100f,50f)))
        assertTrue(page.eraseBetween(Point(50f,0f), Point(50f,100f), 4f))
        assertTrue(page.strokes.isEmpty())
    }}

