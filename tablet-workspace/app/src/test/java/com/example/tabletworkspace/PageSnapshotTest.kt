package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageSnapshotTest {
    @Test
    fun snapshotIsIsolatedFromLaterEdits() {
        val page = Page.create("before")
        page.addBlock("paragraph", "first")
        page.addShape("rect", 1f, 2f, 30f, 40f)
        page.addConnector(0f, 0f, 20f, 20f)
        page.addCanvasImage("a.img", 5f, 6f, 20f, 10f)
        page.folders.addAll(listOf("one", "two"))
        page.tags.add("important")
        page.favorite = true

        val snapshot = page.snapshot()
        page.title = "after"
        page.blocks[0].content = "changed"
        page.shapes[0] = page.shapes[0].copy(x = 99f)
        page.connectors[0] = page.connectors[0].copy(label = "changed")
        page.images[0] = page.images[0].copy(rotation = 2)
        page.folders.clear()
        page.tags.clear()
        page.favorite = false

        assertEquals("before", snapshot.title)
        assertEquals("first", snapshot.blocks.single().content)
        assertEquals(1f, snapshot.shapes.single().x)
        assertEquals("", snapshot.connectors.single().label)
        assertEquals(0, snapshot.images.single().rotation)
        assertEquals(setOf("one", "two"), snapshot.folders)
        assertEquals(setOf("important"), snapshot.tags)
        assertTrue(snapshot.favorite)
        assertFalse(page.title == snapshot.title)
    }

    @Test
    fun snapshotRoundTripsAllPageData() {
        val page = Page.create("snapshot")
        page.addStyledStroke(listOf(Point(1f, 2f), Point(3f, 4f)), 0xff123456.toInt(), 7f)
        page.addCard(CardPreset.NOTE, 0f, 0f)

        assertEquals(page.encode().toList(), page.snapshot().encode().toList())
    }
}
