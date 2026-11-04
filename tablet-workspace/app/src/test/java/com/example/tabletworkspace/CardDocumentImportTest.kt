package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CardDocumentImportTest {
    @Test
    fun convertsTitleAndPlainLinesToDocumentBlocks() {
        val blocks = CardDocumentImport.blocks("Plan", "first\nsecond")
        assertEquals(listOf("heading", "paragraph", "paragraph"), blocks.map { it.type })
        assertEquals("Plan", blocks[0].content)
        assertEquals("first", blocks[1].content)
        assertEquals("second", blocks[2].content)
    }

    @Test
    fun keepsTodosAndTheirCheckedState() {
        val blocks = CardDocumentImport.blocks("", "☐ open\n☑ done\n[ ] markdown open\n[x] markdown done")
        assertEquals(listOf("todo", "todo", "todo", "todo"), blocks.map { it.type })
        assertFalse(blocks[0].checked)
        assertTrue(blocks[1].checked)
        assertFalse(blocks[2].checked)
        assertTrue(blocks[3].checked)
        assertEquals("open", blocks[0].content)
        assertEquals("done", blocks[1].content)
        assertEquals("markdown open", blocks[2].content)
        assertEquals("markdown done", blocks[3].content)
    }

    @Test
    fun keepsListQuoteHeadingAndDividerTypes() {
        val blocks = CardDocumentImport.blocks(
            "",
            "• bullet\n- dash\n1. number\n2) second\n> quote\n## Heading\n---"
        )
        assertEquals(
            listOf("bulleted", "bulleted", "numbered", "numbered", "quote", "heading2", "divider"),
            blocks.map { it.type }
        )
        assertEquals("bullet", blocks[0].content)
        assertEquals("dash", blocks[1].content)
        assertEquals("number", blocks[2].content)
        assertEquals("second", blocks[3].content)
        assertEquals("quote", blocks[4].content)
        assertEquals("Heading", blocks[5].content)
    }

    @Test
    fun keepsFencedCodeAsOneBlockAndSkipsBlankLines() {
        val blocks = CardDocumentImport.blocks("", "before\n\n```\nval x = 1\nval y = 2\n```\nafter")
        assertEquals(listOf("paragraph", "code", "paragraph"), blocks.map { it.type })
        assertEquals("val x = 1\nval y = 2", blocks[1].content)
    }

    @Test
    fun emptyCardProducesNoBlocks() {
        assertTrue(CardDocumentImport.blocks("", "").isEmpty())
    }
}
