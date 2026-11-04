package com.example.tabletworkspace

import org.junit.Assert.*
import org.junit.Test

class WorkspaceTest {
    @Test fun sharedTextSurvivesDocumentAndCanvasChanges() {
        val page = Page.create("笔记")
        page.updateText("hello")
        page.moveCard(80f, 120f)
        val restored = Page.decode(page.encode())
        assertEquals("hello", restored.text)
        assertEquals(80f, restored.cardX, 0.001f)
    }
    @Test fun strokesPersistIndependentlyFromText() {
        val page = Page.create("笔记")
        page.addStroke(listOf(Point(1f, 2f), Point(3f, 4f)))
        page.updateText("updated")
        val restored = Page.decode(page.encode())
        assertEquals(1, restored.strokes.size)
        assertEquals(Point(3f, 4f), restored.strokes[0][1])
    }
}


