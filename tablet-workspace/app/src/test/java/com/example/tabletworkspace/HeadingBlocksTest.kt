package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HeadingBlocksTest {
    private val levels = (1..6).map { "heading$it" }

    @Test
    fun levelsOfHeadingTypesAreRecognised() {
        assertEquals(1, HeadingBlocks.level("heading"))
        levels.forEachIndexed { i, type -> assertEquals(i + 1, HeadingBlocks.level(type)) }
        listOf("heading0", "heading7", "heading10", "headings", "paragraph", "").forEach { assertNull(it, HeadingBlocks.level(it)) }
    }

    @Test
    fun headingsCreatedFromCanvasMenuSurviveSaveAndReopen() {
        val page = Page.create("Headings")
        levels.forEachIndexed { i, type -> page.addBlock(type, "Level ${i + 1} 标题") }
        page.addBlock("heading3", "")

        val restored = Page.decode(page.encode())

        assertEquals(levels + "heading3", restored.blocks.map { it.type })
        assertEquals((1..6).map { "Level $it 标题" } + "", restored.blocks.map { it.content })
    }

    @Test
    fun headingEditsStaySeparateFromNeighboursAfterReopen() {
        val page = Page.create("Headings")
        page.addBlock("heading2", "")
        page.addBlock("paragraph", "body")
        page.updateBlock(0, "typed after creation")
        page.blocks[0].italic = true

        val restored = Page.decode(page.encode())

        assertEquals("typed after creation", restored.blocks[0].content)
        assertTrue(restored.blocks[0].italic)
        assertEquals("body", restored.blocks[1].content)
    }

    @Test
    fun everyHeadingLevelCanBeConvertedAndFormatted() {
        levels.forEach { type ->
            val page = Page.create("Headings").apply { addBlock(type, "Title") }
            assertTrue(type, page.canChangeBlockType(0))
            assertTrue(type, page.canFormatBlockText(0))
            page.changeBlockType(0, "paragraph")
            assertEquals("paragraph", page.blocks[0].type)
            assertEquals("Title", page.blocks[0].content)
        }
    }

    @Test
    fun unknownTypesStillCannotBeConverted() {
        val page = Page.create("Headings").apply { addBlock("heading7", "x"); addBlock("image", "a.png") }
        assertFalse(page.canChangeBlockType(0))
        assertFalse(page.canChangeBlockType(1))
    }

    @Test
    fun markdownKeepsHeadingStructureAndLegacyHeadingFormat() {
        val page = Page.create("Doc").apply {
            addBlock("heading", "Legacy")
            levels.forEachIndexed { i, type -> addBlock(type, "H${i + 1}") }
        }

        val lines = MarkdownCodec.encode(page).lines()

        assertTrue(lines.contains("## Legacy"))
        assertTrue(lines.contains("## H1"))
        assertTrue(lines.contains("### H2"))
        assertTrue(lines.contains("#### H3"))
        assertTrue(lines.contains("##### H4"))
        assertTrue(lines.contains("###### H5"))
        assertTrue("Markdown has only six levels, so H6 shares the deepest one", lines.contains("###### H6"))
    }

    @Test
    fun markdownImportRestoresHeadingLevels() {
        val decoded = MarkdownCodec.decode("# Doc\n\n## Two\n\n### Three\n\n#### Four\n\n##### Five\n\n###### Six\n")

        assertEquals(listOf("heading", "heading2", "heading3", "heading4", "heading5"), decoded.blocks.map { it.type })
        assertEquals(listOf("Two", "Three", "Four", "Five", "Six"), decoded.blocks.map { it.content })
    }

    @Test
    fun paragraphTextThatLooksLikeAHeadingStaysAParagraphAfterRoundTrip() {
        val page = Page.create("Doc").apply { updateText("Lead"); addBlock("paragraph", "### not a heading") }

        val decoded = MarkdownCodec.decode(MarkdownCodec.encode(page))

        assertEquals(listOf("paragraph"), decoded.blocks.map { it.type })
        assertEquals("### not a heading", decoded.blocks[0].content)
    }
}
