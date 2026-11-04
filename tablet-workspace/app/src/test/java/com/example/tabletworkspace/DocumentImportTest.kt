package com.example.tabletworkspace

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentImportTest {
    @Test
    fun htmlImportExtractsTitleHeadingsAndText() {
        val html = """
            <html><head><title>My Page</title><style>p{color:red}</style></head>
            <body><h1>Heading</h1><p>Hello &amp; bye</p><ul><li>One</li><li>Two</li></ul></body></html>
        """.trimIndent()
        val page = DocumentImport.html(html, "Fallback")
        assertEquals("My Page", page.title)
        assertTrue(page.markdown.startsWith("# My Page"))
        assertTrue(page.markdown.contains("## Heading"))
        assertTrue(page.markdown.contains("Hello & bye"))
        assertTrue(page.markdown.contains("- One"))
        assertTrue(page.markdown.contains("- Two"))
    }

    @Test
    fun markdownZipImportsMarkdownEntries() {
        val zip = zipOf(
            "notes/a.md" to "# Note A\n\nBody",
            "notes/skip.png" to "not markdown",
            "__MACOSX/._a.md" to "ignored"
        )
        val pages = DocumentImport.markdownZip(zip)
        assertEquals(1, pages.size)
        assertEquals("a", pages.single().title)
        assertTrue(pages.single().markdown.contains("Body"))
    }

    @Test
    fun docxImportExtractsParagraphText() {
        val xml = """
            <w:document><w:body>
              <w:p><w:r><w:t>Hello</w:t></w:r></w:p>
              <w:p><w:r><w:t>World &amp; friends</w:t></w:r></w:p>
            </w:body></w:document>
        """.trimIndent()
        val pages = DocumentImport.docx(zipOf("word/document.xml" to xml), "Doc")
        assertTrue(pages.markdown.contains("Hello"))
        assertTrue(pages.markdown.contains("World & friends"))
    }

    @Test
    fun snapshotImportReadsTitleTextAndBlocks() {
        val json = """
            {"format":"lingye-snapshot-1","title":"Snap","text":"Lead",
             "blocks":[{"type":"paragraph","content":"First","checked":false},
                       {"type":"todo","content":"Task","checked":true}]}
        """.trimIndent()
        val page = DocumentImport.snapshot(json, "Fallback")
        assertEquals("Snap", page.title)
        assertTrue(page.markdown.contains("Lead"))
        assertTrue(page.markdown.contains("First"))
        assertTrue(page.markdown.contains("- [x] Task"))
    }

    @Test
    fun snapshotCodecRoundTripsThroughImport() {
        val page = Page.create("Round trip").also {
            it.updateText("Lead text")
            it.addBlock("paragraph", "Body & <tag>")
            it.addBlock("todo", "Task")
        }
        val imported = DocumentImport.snapshot(SnapshotCodec.encode(page), "Fallback")
        assertEquals("Round trip", imported.title)
        assertTrue(imported.markdown.contains("Lead text"))
        assertTrue(imported.markdown.contains("Body & <tag>"))
        assertTrue(imported.markdown.contains("- [ ] Task"))
    }

    @Test
    fun htmlExportEscapesBlocks() {
        val page = Page.create("Title & <tag>").also { it.addBlock("paragraph", "a < b") }
        val html = HtmlExporter.encode(page)
        assertTrue(html.contains("<title>Title &amp; &lt;tag&gt;</title>"))
        assertTrue(html.contains("a &lt; b"))
    }

    private fun zipOf(vararg entries: Pair<String, String>): ByteArray {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            entries.forEach { (name, content) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        return bytes.toByteArray()
    }
}
