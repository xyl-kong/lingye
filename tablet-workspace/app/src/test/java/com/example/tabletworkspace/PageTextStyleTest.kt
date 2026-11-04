package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageHistory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PageTextStyleTest {

    @Test
    fun textFontAndSpansSurvivePageRoundTrip() {
        val page = Page.create("Text style")
        page.addTextBox(0f, 0f, "Hello world")
        val index = page.shapes.lastIndex
        page.shapes[index] = page.shapes[index].copy(
            fontFamily = "serif",
            fontStyle = "italic",
            fontWeight = 600,
            textSpans = listOf(
                TextSpan(0, 5, TextSpan.BOLD),
                TextSpan(6, 11, TextSpan.ITALIC or TextSpan.UNDERLINE)
            )
        )

        val bytes = page.encode()
        assertEquals(listOf(0, 0, 0, 25), bytes.take(4).map { it.toInt() and 0xff })
        val restored = Page.decode(bytes).shapes.single()
        assertEquals("serif", restored.fontFamily)
        assertEquals("italic", restored.fontStyle)
        assertEquals(600, restored.fontWeight)
        assertEquals(listOf(TextSpan(0, 5, TextSpan.BOLD), TextSpan(6, 11, TextSpan.ITALIC or TextSpan.UNDERLINE)), restored.textSpans)
    }

    @Test
    fun newShapesDefaultToSansNormal400AndNoSpans() {
        val shape = Shape("text", 0f, 0f, 100f, 50f)
        assertEquals("sans", shape.fontFamily)
        assertEquals("normal", shape.fontStyle)
        assertEquals(400, shape.fontWeight)
        assertTrue(shape.textSpans.isEmpty())
    }

    @Test
    fun invalidFontValuesAreRejectedOnSave() {
        val page = Page.create("Invalid")
        page.addTextBox(0f, 0f, "abc")
        page.shapes[0] = page.shapes[0].copy(fontFamily = "comic")
        assertThrows(IllegalArgumentException::class.java) { page.encode() }

        page.shapes[0] = page.shapes[0].copy(fontFamily = "sans", fontWeight = 450)
        assertThrows(IllegalArgumentException::class.java) { page.encode() }

        page.shapes[0] = page.shapes[0].copy(fontWeight = 400, textSpans = listOf(TextSpan(1, 4, TextSpan.BOLD)))
        assertThrows(IllegalArgumentException::class.java) { page.encode() }

        page.shapes[0] = page.shapes[0].copy(type = "rect", textSpans = listOf(TextSpan(0, 1, TextSpan.BOLD)))
        assertThrows(IllegalArgumentException::class.java) { page.encode() }
    }

    @Test
    fun formatVersionHelperReadsPageHeader() {
        val bytes = Page.create("Version").encode()
        assertEquals(25, pageFormatVersion(bytes))
        assertEquals(null, pageFormatVersion(byteArrayOf(0, 0, 0)))
    }

    @Test
    fun format24HistorySnapshotsStayUsableAfterPageUpgrade() {
        val fixture = LegacyPageFixtures.bytes(24)
        val page = Page.decode(fixture)
        val historyFile = java.io.ByteArrayOutputStream()
        java.io.DataOutputStream(historyFile).use { out ->
            out.writeInt(1)
            out.writeInt(fixture.size); out.write(fixture)
            out.writeInt(1)
            out.writeInt(fixture.size); out.write(fixture)
            out.writeInt(0)
        }
        val history = PageHistory.fromEncoded(page, historyFile.toByteArray())
        assertTrue(history.undo() != null)
    }

    @Test
    fun savedFormat25TextObjectKeepsFontAndSpans() {
        val bytes = javaClass.getResourceAsStream("/format25-styled.page")?.readBytes()
            ?: error("missing format25 fixture")
        assertEquals(25, pageFormatVersion(bytes))
        val text = Page.decode(bytes).shapes.single { it.type == "text" }
        assertEquals("双击编辑文本", text.body)
        assertEquals("serif", text.fontFamily)
        assertEquals("normal", text.fontStyle)
        assertEquals(700, text.fontWeight)
        assertEquals(40f, text.fontSize)
        assertEquals(listOf(TextSpan(0, 6, TextSpan.BOLD or TextSpan.ITALIC or TextSpan.UNDERLINE)), text.textSpans)
    }

    @Test
    fun readsFormat24PageWithTextDefaultsAndUpgradesTo25() {
        val fixture = LegacyPageFixtures.bytes(24)
        assertEquals(24, ((fixture[0].toInt() and 0xff) shl 24) or ((fixture[1].toInt() and 0xff) shl 16) or
            ((fixture[2].toInt() and 0xff) shl 8) or (fixture[3].toInt() and 0xff))
        val page = Page.decode(fixture)
        assertTrue(page.shapes.isNotEmpty())
        page.shapes.forEach {
            assertEquals("sans", it.fontFamily)
            assertEquals("normal", it.fontStyle)
            assertEquals(400, it.fontWeight)
            assertTrue(it.textSpans.isEmpty())
        }
        val upgraded = page.encode()
        assertEquals(25, ((upgraded[3].toInt() and 0xff)))
        val restored = Page.decode(upgraded)
        assertEquals(page.shapes.size, restored.shapes.size)
    }
}
