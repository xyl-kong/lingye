package com.example.tabletworkspace
import com.example.tabletworkspace.ui.model.DocumentRowModel
import com.example.tabletworkspace.ui.model.documentRowModel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentRowModelTest {

    @Test
    fun mapsDisplayFieldsAndModifiedTime() {
        val page = Page.create("Alpha")
        page.text = "line one\nline two"
        page.icon = "📚"
        page.favorite = true
        page.folders.add("Work")

        val model = documentRowModel(page, 1234L)
        assertEquals(page.id, model.id)
        assertEquals("Alpha", model.title)
        assertEquals("line one line two", model.preview)
        assertEquals("📚", model.icon)
        assertTrue(model.favorite)
        assertEquals(listOf("Work"), model.folders)
        assertEquals(1234L, model.modifiedTime)
        assertFalse(model.canvasIcon)
    }

    @Test
    fun previewFallsBackToFirstNonBlankBlockAndIsCapped() {
        val page = Page.create("Blocks")
        page.addBlock("paragraph", "block text")
        assertEquals("block text", documentRowModel(page, null).preview)

        page.text = "x".repeat(400)
        assertEquals(220, documentRowModel(page, null).preview.length)
    }

    @Test
    fun canvasOnlyPageUsesCanvasGlyph() {
        val board = Page.create("Board")
        board.addCanvasImage("img", 0f, 0f, 10f, 10f)
        assertTrue(documentRowModel(board, null).canvasIcon)

        val doc = Page.create("Doc")
        doc.addBlock("paragraph", "text")
        assertFalse(documentRowModel(doc, null).canvasIcon)
    }

    @Test
    fun renameProducesNewModelValue() {
        val page = Page.create("Old")
        val before = documentRowModel(page, 1L)
        page.title = "New"
        val after = documentRowModel(page, 1L)
        assertNotEquals(before, after)
        assertEquals("New", after.title)
    }
}
