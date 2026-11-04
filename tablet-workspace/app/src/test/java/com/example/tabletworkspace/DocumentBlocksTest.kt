package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentBlocksTest {
    @Test
    fun updatingRemovedBlockIndexIsANoOp() {
        val page = Page.create("p")
        page.addBlock("paragraph", "item")
        page.blocks.removeAt(0)

        page.updateBlock(0, "stale")

        assertTrue(page.blocks.isEmpty())
    }

    @Test
    fun changingBlockTypePreservesContentAndStyles() {
        val page = Page.create("p")
        page.addBlock("paragraph", "item")
        page.blocks[0].checked = true
        page.blocks[0].bold = true

        page.changeBlockType(0, "bulleted")

        val block = page.blocks.single()
        assertEquals("bulleted", block.type)
        assertEquals("item", block.content)
        assertTrue(block.checked)
        assertTrue(block.bold)
    }

    @Test
    fun unsupportedBlockTypeIsRejected() {
        val page = Page.create("p")
        page.addBlock("paragraph", "item")

        val rejected = runCatching { page.changeBlockType(0, "unknown") }.isFailure

        assertTrue(rejected)
        assertEquals("paragraph", page.blocks.single().type)
    }

    @Test
    fun structuredDocumentBlocksCannotBeConvertedToText() {
        val page = Page.create("p").apply {
            addBlock("image", "image-key")
            addBlock("database", "database-data")
            addBlock("shared", "shared-id")
        }

        page.blocks.indices.forEach { index ->
            val rejected = runCatching { page.changeBlockType(index, "paragraph") }.isFailure
            assertTrue("Block ${page.blocks[index].type} must remain structured", rejected)
        }
        assertEquals(listOf("image", "database", "shared"), page.blocks.map { it.type })
    }

    @Test
    fun conversionControlsOnlyOpenForConvertibleDocumentBlocks() {
        val page = Page.create("p").apply {
            addBlock("paragraph", "text")
            addBlock("image", "image-key")
            addBlock("database", "database-data")
            addBlock("shared", "shared-id")
        }

        assertTrue(page.canChangeBlockType(0))
        assertFalse(page.canChangeBlockType(1))
        assertFalse(page.canChangeBlockType(2))
        assertFalse(page.canChangeBlockType(3))
    }

    @Test
    fun dividerConversionPreservesTextForReversibleEdits() {
        val page = Page.create("p")
        page.addBlock("paragraph", "keep me")
        page.blocks[0].bold = true

        page.changeBlockType(0, "divider")
        page.changeBlockType(0, "paragraph")

        val block = page.blocks.single()
        assertEquals("paragraph", block.type)
        assertEquals("keep me", block.content)
        assertTrue(block.bold)
    }

    @Test
    fun formattingControlsOnlyOpenForTextBlocksThatRenderFormatting() {
        val page = Page.create("p").apply {
            addBlock("paragraph", "text")
            addBlock("heading", "heading")
            addBlock("todo", "todo")
            addBlock("bulleted", "bullet")
            addBlock("numbered", "numbered")
            addBlock("quote", "quote")
            addBlock("code", "code")
            addBlock("divider", "")
            addBlock("image", "image-key")
            addBlock("database", "database-data")
            addBlock("shared", "shared-id")
        }

        (0..5).forEach { index -> assertTrue(page.canFormatBlockText(index)) }
        (6..10).forEach { index -> assertFalse(page.canFormatBlockText(index)) }
    }

    @Test
    fun newDocumentBlocksRoundTripThroughMarkdown() {
        val page = Page.create("Blocks").apply {
            addBlock("bulleted", "Bullet")
            addBlock("numbered", "Numbered")
            addBlock("quote", "Quoted")
            addBlock("code", "val x = 1")
            addBlock("divider", "")
        }

        val decoded = MarkdownCodec.decode(MarkdownCodec.encode(page))

        assertEquals(
            listOf("bulleted", "numbered", "quote", "code", "divider"),
            decoded.blocks.map { it.type }
        )
        assertEquals("val x = 1", decoded.blocks[3].content)
        assertFalse(decoded.blocks.any { it.type == "paragraph" })
    }
}
