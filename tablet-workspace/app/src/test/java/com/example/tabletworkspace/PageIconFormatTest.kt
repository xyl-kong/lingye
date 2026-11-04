package com.example.tabletworkspace

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageIconFormatTest {
    @Test
    fun iconSurvivesRoundTripSnapshotAndDuplicate() {
        val page = Page.create("Icon page")
        page.icon = "🚀"
        val restored = Page.decode(page.encode())
        assertEquals("🚀", restored.icon)

        page.text = "changed"
        assertEquals("🚀", page.snapshot().icon)
        assertEquals("🚀", page.duplicate(UUID.randomUUID().toString(), emptyMap()).icon)
    }

    @Test
    fun legacyFormat22DecodesWithEmptyIcon() {
        val bytes = ByteArrayOutputStream().also { out ->
            DataOutputStream(out).use { data ->
                data.writeInt(22)
                data.writeUTF(UUID.randomUUID().toString())
                data.writeUTF("Legacy page")
                data.writeStringForTest("")
                data.writeFloat(80f); data.writeFloat(100f)
                data.writeInt(0) // strokes
                data.writeInt(0); data.writeInt(0); data.writeInt(0) // blocks, shapes, connectors
                data.writeInt(0) // images
                data.writeInt(0) // folder string
                data.writeBoolean(false) // trashed
                data.writeInt(0) // templates
                data.writeInt(0) // layer ids
                data.writeInt(0); data.writeInt(0); data.writeBoolean(false) // folders, tags, favorite
            }
        }.toByteArray()

        val page = Page.decode(bytes)
        assertEquals("Legacy page", page.title)
        assertEquals("", page.icon)
        assertTrue(page.blocks.isEmpty())
    }

    @Test
    fun pageIconValidationRejectsLongOrControlValues() {
        assertTrue(PageIcons.isValid(""))
        assertTrue(PageIcons.isValid("📄"))
        assertFalse(PageIcons.isValid("a".repeat(PageIcons.MAX_LENGTH + 1)))
        assertFalse(PageIcons.isValid("a\u0000b"))
    }

    private fun DataOutputStream.writeStringForTest(value: String) {
        val data = value.toByteArray(Charsets.UTF_8)
        writeInt(data.size)
        write(data)
    }
}
