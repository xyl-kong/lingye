package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownCodecTest {
    @Test
    fun commonDocumentBlocksRoundTripThroughMarkdown() {
        val page = Page.create("Plan")
        page.updateText("Intro")
        page.addBlock("heading", "Steps")
        page.addBlock("paragraph", "First step")
        page.addBlock("todo", "Ship it").also {
            page.blocks.last().checked = true
        }

        val decoded = MarkdownCodec.decode(MarkdownCodec.encode(page))

        assertEquals("Plan", decoded.title)
        assertEquals("Intro", decoded.lead)
        assertEquals(listOf("heading", "paragraph", "todo"), decoded.blocks.map { it.type })
        assertEquals(listOf("Steps", "First step", "Ship it"), decoded.blocks.map { it.content })
        assertTrue(decoded.blocks.last().checked)
    }

    @Test
    fun plainMarkdownWithoutTitleUsesFallback() {
        val decoded = MarkdownCodec.decode(
            """
            A plain paragraph.

            - [ ] First task
            """.trimIndent(),
            fallbackTitle = "Imported"
        )

        assertEquals("Imported", decoded.title)
        assertEquals(listOf("paragraph", "todo"), decoded.blocks.map { it.type })
    }

    @Test
    fun headingAndTodoPrefixesAreNotDuplicated() {
        val markdown = MarkdownCodec.encode(
            Page.create("Title").apply {
                addBlock("heading", "Section")
                addBlock("todo", "Task")
            }
        )

        assertEquals(1, markdown.lines().count { it == "## Section" })
        assertEquals(1, markdown.lines().count { it == "- [ ] Task" })
    }

    @Test
    fun sharedTextIsExportedAsReadableMarkdown() {
        val shared = SharedBlock("shared-1", "Shared content")
        val page = Page.create("Shared")
        page.addBlock("shared", shared.id)

        val markdown = MarkdownCodec.encode(page, listOf(shared))

        assertTrue(markdown.contains("Shared content"))
        assertEquals("Shared content", MarkdownCodec.decode(markdown).lead)
    }

    @Test
    fun languageFencedCodeImportsAsCodeWithoutFenceMarkers() {
        val decoded = MarkdownCodec.decode(
            """
            # Code

            ```kotlin
            val answer = 42
            println(answer)
            ```
            """.trimIndent()
        )

        val code = decoded.blocks.single()
        assertEquals("code", code.type)
        assertEquals("val answer = 42\nprintln(answer)", code.content)
    }
}
