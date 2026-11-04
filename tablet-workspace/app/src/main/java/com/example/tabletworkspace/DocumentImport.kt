package com.example.tabletworkspace

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

/** Result of parsing an imported file; headings are expressed as Markdown. */
data class ImportedPage(val title: String, val markdown: String)

/**
 * Offline import helpers for the formats shown in the import dialog.
 * Markdown-like formats are normalized to Markdown and then decoded by [MarkdownCodec].
 */
object DocumentImport {
    private const val MAX_ENTRY_BYTES = 20_000_000
    private const val MAX_ENTRIES = 300

    fun markdownZip(bytes: ByteArray): List<ImportedPage> =
        zipEntries(bytes, setOf("md", "markdown", "txt")).mapNotNull { (name, text) ->
            if (text.isBlank()) null else ImportedPage(titleOf(name), text)
        }

    fun htmlZip(bytes: ByteArray): List<ImportedPage> =
        zipEntries(bytes, setOf("html", "htm", "md", "txt")).mapNotNull { (name, text) ->
            when (name.substringAfterLast('.', "").lowercase()) {
                "html", "htm" -> html(text, titleOf(name))
                else -> if (text.isBlank()) null else ImportedPage(titleOf(name), text)
            }
        }

    fun html(html: String, fallbackTitle: String): ImportedPage {
        val title = Regex("(?is)<title[^>]*>(.*?)</title>").find(html)
            ?.groupValues?.get(1)?.let(::plainText)?.trim()
            ?.ifBlank { null } ?: fallbackTitle
        return ImportedPage(title, "# $title\n\n${htmlToMarkdownBody(html)}")
    }

    fun docx(bytes: ByteArray, fallbackTitle: String): ImportedPage {
        val xml = readZipEntry(bytes, "word/document.xml")
            ?: error("不是有效的 Word (.docx) 文件")
        return ImportedPage(fallbackTitle, "# $fallbackTitle\n\n${docxXmlToText(xml)}")
    }

    fun snapshot(json: String, fallbackTitle: String): ImportedPage {
        val title = jsonString(json, "title")?.trim()?.ifBlank { null } ?: fallbackTitle
        val body = jsonString(json, "text").orEmpty()
        val blocks = Regex(
            """\{\s*"type"\s*:\s*"([^"]*)"\s*,\s*"content"\s*:\s*"((?:\\.|[^"\\])*)"(?:\s*,\s*"checked"\s*:\s*(true|false))?\s*}"""
        ).findAll(json).mapNotNull { match ->
            val type = match.groupValues[1]
            val content = unescapeJson(match.groupValues[2])
            val checked = match.groupValues[3] == "true"
            when (type) {
                "heading1", "heading" -> "# $content"
                "heading2" -> "## $content"
                "heading3" -> "### $content"
                "bulleted" -> "- $content"
                "numbered" -> "1. $content"
                "todo" -> if (checked) "- [x] $content" else "- [ ] $content"
                "quote" -> "> $content"
                "code" -> "```\n$content\n```"
                "divider" -> "---"
                else -> content
            }.takeIf { it.isNotBlank() }
        }.toList()
        val bodyText = if (blocks.isEmpty()) {
            Regex("\"text\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").findAll(json)
                .map { unescapeJson(it.groupValues[1]) }
                .filter { it.isNotBlank() }
                .joinToString("\n\n")
        } else body
        return ImportedPage(title, buildString {
            append("# ").append(title).append("\n\n")
            if (bodyText.isNotBlank()) append(bodyText).append("\n\n")
            blocks.forEach { append(it).append("\n\n") }
        })
    }

    /** Markdown bundle inside a zip, used by Markdown-with-media, Obsidian and Bear. */
    private fun zipEntries(bytes: ByteArray, extensions: Set<String>): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            var count = 0
            while (entry != null && count < MAX_ENTRIES) {
                val name = entry.name
                if (!entry.isDirectory && !name.startsWith("__MACOSX/") && name.substringAfterLast('.', "").lowercase() in extensions) {
                    val text = readEntryText(zip)
                    if (text != null) result += name to text
                    count++
                }
                entry = zip.nextEntry
            }
        }
        return result
    }

    private fun readZipEntry(bytes: ByteArray, wanted: String): String? {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name == wanted) return readEntryText(zip)
                entry = zip.nextEntry
            }
        }
        return null
    }

    private fun readEntryText(zip: ZipInputStream): String? {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0
        while (true) {
            val read = zip.read(buffer)
            if (read < 0) break
            total += read
            if (total > MAX_ENTRY_BYTES) return null
            out.write(buffer, 0, read)
        }
        return out.toString(Charsets.UTF_8.name())
    }

    private fun docxXmlToText(xml: String): String {
        val paragraphs = Regex("(?s)<w:p[ >].*?</w:p>").findAll(xml).map { match ->
            Regex("(?s)<w:t[^>]*>(.*?)</w:t>").findAll(match.value)
                .map { decodeXml(it.groupValues[1]) }
                .joinToString("")
        }.filter { it.isNotBlank() }.toList()
        if (paragraphs.isNotEmpty()) return paragraphs.joinToString("\n\n")
        return Regex("(?s)<w:t[^>]*>(.*?)</w:t>").findAll(xml)
            .map { decodeXml(it.groupValues[1]) }
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
    }

    private fun htmlToMarkdownBody(html: String): String {
        var text = html
            .replace(Regex("(?is)<(script|style|head)[^>]*>.*?</\\1>"), "")
            .replace(Regex("(?is)<!--.*?-->"), "")
        text = Regex("(?is)<h([1-6])[^>]*>(.*?)</h\\1>").replace(text) { match ->
            val level = match.groupValues[1].toIntOrNull() ?: 1
            "\n\n${"#".repeat((level + 1).coerceAtMost(6))} ${plainText(match.groupValues[2])}\n\n"
        }
        text = Regex("(?is)<li[^>]*>(.*?)</li>").replace(text) { match ->
            "\n- ${plainText(match.groupValues[1])}\n"
        }
        text = text
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?is)</(p|div|section|article|tr|blockquote|ul|ol|table)>"), "\n\n")
        text = text.replace(Regex("(?is)<[^>]+>"), "")
        return decodeXml(text)
            .replace("\r\n", "\n")
            .replace(Regex("[ \t]+\n"), "\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    private fun plainText(fragment: String): String =
        decodeXml(fragment.replace(Regex("(?is)<[^>]+>"), "")).trim()

    private fun titleOf(path: String): String =
        path.substringAfterLast('/').substringBeforeLast('.').ifBlank { "Imported page" }

    private fun decodeXml(value: String): String = value
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace(Regex("&#(\\d+);")) { it.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: it.value }

    private fun jsonString(json: String, key: String): String? =
        Regex("\"${Regex.escape(key)}\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
            .find(json)?.groupValues?.get(1)?.let(::unescapeJson)

    private fun unescapeJson(value: String): String = buildString {
        var index = 0
        while (index < value.length) {
            val ch = value[index]
            if (ch == '\\' && index + 1 < value.length) {
                when (val next = value[index + 1]) {
                    'n' -> append('\n')
                    'r' -> append('\r')
                    't' -> append('\t')
                    '"' -> append('"')
                    '\\' -> append('\\')
                    '/' -> append('/')
                    'u' -> {
                        val hex = value.substring(index + 2, (index + 6).coerceAtMost(value.length))
                        val code = hex.toIntOrNull(16)
                        if (code != null) { append(code.toChar()); index += 4 } else append(next)
                    }
                    else -> append(next)
                }
                index += 2
            } else {
                append(ch)
                index++
            }
        }
    }
}

/** HTML export for the page "Export > Export as HTML" action. */
object HtmlExporter {
    fun encode(page: Page): String = buildString {
        append("<!doctype html><html><head><meta charset=\"utf-8\"><title>")
        append(escape(page.title.ifBlank { "Untitled" }))
        append("</title></head><body>")
        append("<h1>").append(escape(page.title.ifBlank { "Untitled" })).append("</h1>")
        if (page.text.isNotBlank()) append("<p>").append(escape(page.text)).append("</p>")
        page.blocks.forEach { block ->
            when (val level = HeadingBlocks.level(block.type)) {
                null -> when (block.type) {
                    "bulleted" -> append("<ul><li>").append(escape(block.content)).append("</li></ul>")
                    "numbered" -> append("<ol><li>").append(escape(block.content)).append("</li></ol>")
                    "todo" -> append("<p>").append(if (block.checked) "☑ " else "☐ ").append(escape(block.content)).append("</p>")
                    "quote" -> append("<blockquote>").append(escape(block.content)).append("</blockquote>")
                    "code" -> append("<pre><code>").append(escape(block.content)).append("</code></pre>")
                    "divider" -> append("<hr>")
                    "image" -> append("<p>[Image] ").append(escape(block.content)).append("</p>")
                    else -> if (block.content.isNotBlank()) append("<p>").append(escape(block.content)).append("</p>")
                }
                else -> append("<h").append(level.coerceIn(1, 6)).append('>')
                    .append(escape(block.content)).append("</h").append(level.coerceIn(1, 6)).append('>')
            }
        }
        append("</body></html>")
    }

    private fun escape(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("\n", "<br>")
}

/** JSON snapshot of a page used by "Export to Snapshot" and the snapshot import format. */
object SnapshotCodec {
    fun encode(page: Page): String = buildString {
        append("{\n  \"format\": \"lingye-snapshot-1\",\n")
        append("  \"title\": \"").append(jsonEscape(page.title)).append("\",\n")
        append("  \"text\": \"").append(jsonEscape(page.text)).append("\",\n")
        append("  \"blocks\": [")
        page.blocks.forEachIndexed { index, block ->
            if (index > 0) append(',')
            append("\n    {\"type\": \"").append(jsonEscape(block.type))
                .append("\", \"content\": \"").append(jsonEscape(block.content))
                .append("\", \"checked\": ").append(block.checked).append('}')
        }
        append("\n  ]\n}")
    }

    private fun jsonEscape(value: String): String = buildString {
        value.forEach { ch ->
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (ch < ' ') append(String.format("\\u%04x", ch.code)) else append(ch)
            }
        }
    }
}
