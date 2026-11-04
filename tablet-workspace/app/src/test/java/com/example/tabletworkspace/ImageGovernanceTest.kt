package com.example.tabletworkspace
import com.example.tabletworkspace.data.images.ImageReferences
import com.example.tabletworkspace.data.images.containsBytes

import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageGovernanceTest {

    private fun templateWithImage(name: String): CardTemplate {
        val canvas = Page.create("template")
        canvas.addCanvasImage(name, 0f, 0f, 20f, 20f)
        return CardTemplate(
            name = "t",
            title = "t",
            body = "",
            color = 0xffffffff.toInt(),
            canvasData = Base64.getEncoder().encodeToString(canvas.encode())
        )
    }

    @Test
    fun collectsReferencesFromPagesTemplatesAndSharedBlocks() {
        val page = Page.create("p")
        page.addCanvasImage("canvas.img", 0f, 0f, 10f, 10f)
        page.addBlock("image", "block.img")

        val shared = SharedBlock(id = "s", text = "shared.img", kind = "image")

        val refs = ImageReferences.referenced(
            pages = listOf(page),
            templates = listOf(templateWithImage("template.img")),
            shared = listOf(shared)
        )
        assertEquals(
            setOf("canvas.img", "block.img", "shared.img", "template.img"),
            refs
        )
    }

    @Test
    fun sharedUndoHistoryImagesStayReferenced() {
        val shared = SharedBlock(
            id = "s",
            text = "current.img",
            kind = "image",
            undoHistory = listOf(SharedSnapshot(text = "old.img", kind = "image", bold = false, italic = false, underline = false, checked = false, spans = emptyList()))
        )
        val refs = ImageReferences.referenced(emptyList(), emptyList(), listOf(shared))
        assertEquals(setOf("current.img", "old.img"), refs)
    }

    @Test
    fun emptyNamesAreIgnored() {
        val page = Page.create("p")
        page.addBlock("image", "")
        page.addCanvasImage("", 0f, 0f, 5f, 5f)
        assertTrue(ImageReferences.referenced(listOf(page), emptyList(), emptyList()).isEmpty())
    }

    @Test
    fun containsBytesFindsNamesInsideHistoryLikePayloads() {
        val payload = byteArrayOf(1, 2, 3, 0x61, 0x62, 0x63, 9, 9)
        assertTrue(payload.containsBytes("abc".toByteArray()))
        assertFalse(payload.containsBytes("abd".toByteArray()))
        assertFalse(byteArrayOf().containsBytes("abc".toByteArray()))
    }
}
