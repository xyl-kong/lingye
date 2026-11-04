package com.example.tabletworkspace

/**
 * Converts a canvas note card into document blocks without changing the page format.
 * The conversion understands the plain-text conventions already used by the note presets:
 * todo markers, bullet/numbered lists, quotes, heading markers, fences and dividers.
 */
object CardDocumentImport {
    private val heading = Regex("^(#{1,6})\\s+(.+)$")
    private val bullet = Regex("^[-*•]\\s+(.+)$")
    private val numbered = Regex("^\\d+[.)]\\s+(.+)$")
    private val quote = Regex("^>\\s?(.*)$")

    fun blocks(title: String, body: String): List<Block> {
        val result = mutableListOf<Block>()
        if (title.isNotBlank()) result.add(Block("heading", title.trim()))
        var code: MutableList<String>? = null
        fun flushCode() {
            code?.let { lines ->
                if (lines.isNotEmpty()) result.add(Block("code", lines.joinToString("\n")))
            }
            code = null
        }
        body.split('\n').forEach { raw ->
            val line = raw.trimEnd()
            if (code != null) {
                if (line.trim() == "```") flushCode() else code!!.add(raw)
                return@forEach
            }
            if (line.trim() == "```") { code = mutableListOf(); return@forEach }
            if (line.isBlank()) return@forEach
            val trimmed = line.trimStart()
            val checked = todoChecked(trimmed)
            when {
                checked != null -> result.add(Block("todo", todoContent(trimmed), checked = checked))
                heading.matches(trimmed) -> {
                    val match = heading.matchEntire(trimmed)!!
                    result.add(Block("heading${match.groupValues[1].length}", match.groupValues[2].trim()))
                }
                bullet.matches(trimmed) -> result.add(Block("bulleted", bullet.matchEntire(trimmed)!!.groupValues[1]))
                numbered.matches(trimmed) -> result.add(Block("numbered", numbered.matchEntire(trimmed)!!.groupValues[1]))
                quote.matches(trimmed) -> result.add(Block("quote", quote.matchEntire(trimmed)!!.groupValues[1]))
                trimmed in setOf("---", "***", "___") -> result.add(Block("divider", ""))
                else -> result.add(Block("paragraph", line))
            }
        }
        flushCode()
        return result
    }
}
