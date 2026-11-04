package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SlashCommandsTest {
    @Test
    fun slashCommandQueryEndsAtWhitespace() {
        assertEquals("he", SlashCommandCatalog.query("/he"))
        assertEquals("代码", SlashCommandCatalog.query("/代码"))
        assertNull(SlashCommandCatalog.query("/heading with space"))
        assertNull(SlashCommandCatalog.query("text /heading"))
    }

    @Test
    fun slashCommandsFilterByEnglishAndChineseKeywords() {
        assertTrue(SlashCommandCatalog.matches("he").any { it.type == "heading" })
        assertTrue(SlashCommandCatalog.matches("代码").any { it.type == "code" })
        assertTrue(SlashCommandCatalog.matches("quote").any { it.type == "quote" })
    }

    @Test
    fun emptySlashCommandShowsEverySupportedBlockType() {
        assertEquals(
            listOf("paragraph", "heading", "todo", "bulleted", "numbered", "quote", "code", "divider", "image", "database", "shared"),
            SlashCommandCatalog.matches("").map { it.type }
        )
    }
}
