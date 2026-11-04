package com.example.tabletworkspace

import org.junit.Assert.*
import org.junit.Test

class EditorFeaturesTest {
    @Test fun blocksAndCanvasObjectsRoundTrip() {
        val page = Page.create("页面")
        page.addBlock("heading", "标题")
        page.addBlock("image", "assets/photo.png")
        page.addShape("rect", 30f, 40f, 100f, 80f)
        page.addConnector(10f, 20f, 110f, 120f)
        val restored = Page.decode(page.encode())
        assertEquals(listOf("heading", "image"), restored.blocks.map { it.type })
        assertEquals("assets/photo.png", restored.blocks[1].content)
        assertEquals("rect", restored.shapes[0].type)
        assertEquals(120f, restored.connectors[0].y2, 0.001f)
    }
    @Test fun viewportConvertsScreenToCanvasAndBack() {
        val viewport = Viewport(2f, 40f, 60f)
        val world = viewport.toWorld(Point(140f, 260f))
        assertEquals(Point(50f, 100f), world)
        assertEquals(Point(140f, 260f), viewport.toScreen(world))
    }
    @Test fun changingBlockDoesNotRemoveOtherBlocks() {
        val page = Page.create("页面")
        page.addBlock("paragraph", "one")
        page.addBlock("todo", "two")
        page.updateBlock(0, "changed")
        assertEquals(listOf("changed", "two"), Page.decode(page.encode()).blocks.map { it.content })
    }
    @Test fun legacyPageRemainsReadable() {
        val data = java.io.ByteArrayOutputStream()
        java.io.DataOutputStream(data).use { out ->
            out.writeInt(1); out.writeUTF("old-id"); out.writeUTF("旧页面")
            val text = "原有正文".toByteArray(Charsets.UTF_8); out.writeInt(text.size); out.write(text)
            out.writeFloat(12f); out.writeFloat(24f); out.writeInt(0)
        }
        val page = Page.decode(data.toByteArray())
        assertEquals("原有正文", page.text)
        assertEquals(12f, page.cardX, 0.001f)
        assertEquals("原有正文", Page.decode(page.encode()).text)
    }    @Test fun shapesAndLinesCanBeSelectedMovedAndRemoved() {
        val page = Page.create("test")
        page.addShape("rect", 10f, 20f, 100f, 80f)
        page.addConnector(200f, 200f, 300f, 200f)
        assertEquals(CanvasSelection("shape", 0), page.hitTest(Point(30f, 40f), 10f))
        page.moveSelection(CanvasSelection("shape", 0), 5f, 7f)
        assertEquals(15f, page.shapes[0].x, 0.001f)
        assertEquals(CanvasSelection("connector", 0), page.hitTest(Point(250f, 204f), 10f))
        page.moveSelection(CanvasSelection("connector", 0), 10f, 20f)
        assertEquals(210f, page.connectors[0].x1, 0.001f)
        page.deleteSelection(CanvasSelection("connector", 0))
        assertTrue(Page.decode(page.encode()).connectors.isEmpty())
    }}


