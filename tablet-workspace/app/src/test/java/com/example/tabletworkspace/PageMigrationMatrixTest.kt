package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression matrix for every page format version still present in the historical backups. */
class PageMigrationMatrixTest {
    private val observedVersions = listOf(4, 5, 6, 8, 11, 12, 13, 14, 15, 16, 22, 23, 24)

    @Test
    fun everyObservedHistoricalFormatUpgradesTo25() {
        observedVersions.forEach { version ->
            val bytes = LegacyPageFixtures.bytes(version)
            assertEquals("fixture header for v$version", version, pageFormatVersion(bytes))
            val page = try {
                Page.decode(bytes)
            } catch (error: Exception) {
                throw AssertionError("v$version generated fixture failed to decode", error)
            }
            page.shapes.forEach { shape ->
                assertEquals("sans", shape.fontFamily)
                assertEquals("normal", shape.fontStyle)
                assertEquals(400, shape.fontWeight)
                assertTrue(shape.textSpans.isEmpty())
            }
            val upgraded = page.encode()
            assertEquals("upgraded header for v$version", 25, pageFormatVersion(upgraded))
            val restored = Page.decode(upgraded)
            assertEquals(page.id, restored.id)
            assertEquals(page.title, restored.title)
            assertEquals(page.blocks.size, restored.blocks.size)
            assertEquals(page.shapes.size, restored.shapes.size)
            assertEquals(page.connectors.size, restored.connectors.size)
            assertEquals(page.images.size, restored.images.size)
            assertEquals(page.strokes.size, restored.strokes.size)
            assertEquals(page.tags, restored.tags)
            assertEquals(page.folders, restored.folders)
        }
    }

    @Test
    fun futureFormatIsRejected() {
        val bytes = Page.create("future").encode()
        bytes[3] = 26
        assertThrows(IllegalArgumentException::class.java) { Page.decode(bytes) }
    }

    @Test
    fun truncatedPageIsRejected() {
        val bytes = Page.create("truncated").encode().copyOf(8)
        assertThrows(Exception::class.java) { Page.decode(bytes) }
    }

    @Test
    fun corruptTrailingDataIsRejected() {
        val bytes = resource("/format25-styled.page").copyOf()
        bytes[bytes.lastIndex - 3] = (bytes[bytes.lastIndex - 3].toInt() or 0x80).toByte()
        assertThrows(Exception::class.java) { Page.decode(bytes) }
    }

    private fun resource(name: String): ByteArray =
        javaClass.getResourceAsStream(name)?.readBytes() ?: error("missing fixture $name")
}
