package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PageDuplicateTest {
    @Test
    fun duplicateCopiesContentWithNewId() {
        val page = Page.create("Original").also {
            it.updateText("Lead")
            it.addBlock("paragraph", "Body")
            it.addShape("rectangle", 10f, 20f, 100f, 50f)
            it.tags.add("tag")
            it.folders.add("Folder")
            it.icon = "star"
            it.favorite = true
        }
        val copy = page.duplicate("Original copy")
        assertNotEquals(page.id, copy.id)
        assertEquals("Original copy", copy.title)
        assertEquals("Lead", copy.text)
        assertEquals(listOf("Body"), copy.blocks.map { it.content })
        assertEquals(1, copy.shapes.size)
        assertEquals(page.tags, copy.tags)
        assertEquals(page.folders, copy.folders)
        assertEquals("star", copy.icon)
        assertEquals(true, copy.favorite)
        copy.blocks[0].content = "Changed"
        assertEquals("Body", page.blocks[0].content)
    }
}
