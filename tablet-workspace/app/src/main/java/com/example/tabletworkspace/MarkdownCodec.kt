package com.example.tabletworkspace

data class MarkdownDocument(
    val title: String,
    val lead: String,
    val blocks: List<Block>
) {
    fun toPage(fallbackTitle: String = "Imported page"): Page =
        Page.create(title.ifBlank { fallbackTitle }).also { page ->
            page.updateText(lead)
            page.blocks.addAll(blocks.map { it.copy() })
        }
}

object MarkdownCodec {
    fun encode(page: Page, sharedBlocks: List<SharedBlock> = emptyList()): String = buildString {
        val sharedById = sharedBlocks.associateBy { it.id }
        append("# ").append(page.title.ifBlank { "Untitled" }).append("\n")
        if (page.text.isNotBlank()) {
            append('\n').append(escapeTextBlock(page.text)).append('\n')
        }
        page.blocks.forEach { block ->
            if (block.content.isBlank() && block.type != "divider") return@forEach
            append('\n')
            val headingLevel = HeadingBlocks.level(block.type)
            if (headingLevel != null) {
                append("#".repeat(minOf(headingLevel, MAX_BLOCK_HEADING_LEVEL) + 1)).append(' ').append(block.content.replace('\n', ' '))
                append('\n')
                return@forEach
            }
            when (block.type) {
                "todo" -> append(if (block.checked) "- [x] " else "- [ ] ").append(block.content.replace('\n', ' '))
                "bulleted" -> append("- ").append(block.content.replace('\n', ' '))
                "numbered" -> append("1. ").append(block.content.replace('\n', ' '))
                "quote" -> block.content.lines().forEachIndexed { index, line ->
                    if (index > 0) append('\n')
                    append("> ").append(line)
                }
                "code" -> append("```\n").append(block.content).append("\n```")
                "divider" -> append("---")
                "image" -> append("![](").append(block.content).append(")")
                "database" -> append("> Database: ").append(
                    runCatching { DatabaseCodec.decode(block.content).name }.getOrDefault(block.content)
                )
                "shared" -> {
                    val shared = sharedById[block.content]
                    when (shared?.kind) {
                        "image" -> append("![Shared image](").append(shared.text).append(")")
                        "database" -> append("> Shared database: ").append(
                            runCatching { DatabaseCodec.decode(shared.text).name }.getOrDefault("Database")
                        )
                        else -> append(escapeTextBlock(shared?.text ?: "[Missing shared block]"))
                    }
                }
                else -> append(escapeTextBlock(block.content))
            }
            append('\n')
        }
    }

    fun decode(markdown: String, fallbackTitle: String = "Imported page", imageExists: (String) -> Boolean = { false }): MarkdownDocument {
        val lines = markdown.replace("\r\n", "\n").split('\n')
        var title = fallbackTitle
        var sawTitle = false
        var lead = ""
        val blocks = mutableListOf<Block>()
        val paragraph = mutableListOf<String>()
        val codeLines = mutableListOf<String>()
        var inCode = false

        fun flushParagraph() {
            if (paragraph.isNotEmpty()) {
                val content = paragraph.joinToString("\n").trim()
                if (content.isNotEmpty()) {
                    if (sawTitle && lead.isBlank() && blocks.isEmpty()) lead = content
                    else blocks += Block("paragraph", content)
                }
                paragraph.clear()
            }
        }

        lines.forEach { raw ->
            val line = raw.trimEnd()
            when {
                line.trimStart().startsWith("```") -> {
                    if (inCode) {
                        blocks += Block("code", codeLines.joinToString("\n"))
                        codeLines.clear()
                        inCode = false
                    } else {
                        flushParagraph()
                        inCode = true
                    }
                }
                inCode -> codeLines += raw
                !sawTitle && line.startsWith("# ") -> {
                    title = line.removePrefix("# ").trim().ifBlank { fallbackTitle }
                    sawTitle = true
                }
                BLOCK_HEADING.matches(line) -> {
                    flushParagraph()
                    val hashes = line.takeWhile { it == '#' }.length
                    val level = hashes - 1
                    blocks += Block(if (level == 1) "heading" else "heading$level", line.substring(hashes).trim())
                }
                line.startsWith("- [ ] ") -> {
                    flushParagraph()
                    blocks += Block("todo", line.removePrefix("- [ ] ").trim(), checked = false)
                }
                line.startsWith("- [x] ") || line.startsWith("- [X] ") -> {
                    flushParagraph()
                    blocks += Block("todo", line.substring(6).trim(), checked = true)
                }
                line.startsWith("- ") -> {
                    flushParagraph()
                    blocks += Block("bulleted", line.removePrefix("- ").trim())
                }
                Regex("^\\d+\\.\\s+.*").matches(line) -> {
                    flushParagraph()
                    blocks += Block("numbered", line.substringAfter(". ").trim())
                }
                line.startsWith("> ") -> {
                    flushParagraph()
                    blocks += Block("quote", line.removePrefix("> ").trim())
                }
                line.trim() == "---" -> {
                    flushParagraph()
                    blocks += Block("divider", "")
                }
                IMAGE_LINK.matches(line.trim()) -> {
                    val name = IMAGE_LINK.matchEntire(line.trim())?.groupValues?.get(1).orEmpty()
                    if (imageExists(name)) {
                        flushParagraph()
                        blocks += Block("image", name)
                    } else {
                        paragraph += line
                    }
                }
                line.isBlank() -> flushParagraph()
                else -> paragraph += line.removePrefix("\\")
            }
        }
        flushParagraph()
        return MarkdownDocument(title, lead, blocks)
    }

    private fun escapeTextBlock(value: String): String =
        value.lines().joinToString("\n") { line ->
            if (
                LEADING_HASHES.matches(line) ||
                line.startsWith("- [ ] ") || line.startsWith("- [x] ") ||
                line.startsWith("- [X] ") || line.startsWith("- ") ||
                line.startsWith("> ") || line.trim() == "---" || line.trim() == "```"
            ) "\\$line" else line
        }

    /** The page title owns `#`, so block headings use `##`..`######`; H5 and H6 share the deepest level. */
    private const val MAX_BLOCK_HEADING_LEVEL = 5
    private val BLOCK_HEADING = Regex("^#{2,6} .*")
    private val LEADING_HASHES = Regex("^#{1,6} .*")
    private val IMAGE_LINK = Regex("^!\\[[^\\]]*\\]\\(([^)]+)\\)$")
}
