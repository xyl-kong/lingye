package com.example.tabletworkspace
import com.example.tabletworkspace.ui.model.PageRowModel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PageRowModelTest {

    @Test
    fun snapshotCopiesDisplayFields() {
        val page = Page.create("Project notes")
        page.tags.add("work")
        page.folders.add("Clients")
        page.favorite = true
        page.icon = "📌"

        val model = PageRowModel.of(page)
        assertEquals(page.id, model.id)
        assertEquals("Project notes", model.title)
        assertEquals(listOf("work"), model.tags)
        assertEquals(listOf("Clients"), model.folders)
        assertTrue(model.favorite)
        assertFalse(model.trashed)
        assertEquals("📌", model.icon)
        assertFalse(model.canvasIcon)
    }

    @Test
    fun canvasOnlyPagesShowTheCanvasGlyph() {
        val page = Page.create("Board")
        page.addCanvasImage("img", 0f, 0f, 10f, 10f)
        assertTrue(PageRowModel.of(page).canvasIcon)

        val document = Page.create("Doc")
        document.addBlock("paragraph", "text")
        assertFalse(PageRowModel.of(document).canvasIcon)
    }

    @Test
    fun renameChangesTheModelValue() {
        val page = Page.create("Old")
        val before = PageRowModel.of(page)
        page.title = "New"
        val after = PageRowModel.of(page)
        assertNotEquals(before, after)
        assertEquals("New", after.title)
    }

    @Test
    fun unchangedPageProducesEqualModels() {
        val page = Page.create("Stable")
        assertEquals(PageRowModel.of(page), PageRowModel.of(page))
    }
}
