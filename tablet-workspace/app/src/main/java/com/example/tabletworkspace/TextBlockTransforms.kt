package com.example.tabletworkspace

/** Pure text transforms used by the canvas text style menu. */
object TextBlockTransforms {
    private val numericPrefix = Regex("^\\s*\\d+\\.\\s+")
    private val bulletPrefix = Regex("^\\s*(•|☐|☑|-)\\s+")

    fun applyList(body: String, kind: String): String {
        val source = body.lines()
        val existing = source.filter { it.isNotBlank() }
        val applied = existing.isNotEmpty() && existing.all { line ->
            when (kind) {
                "numbered" -> line.trimStart().matches(Regex("^\\d+\\.\\s+.*"))
                "todo" -> line.trimStart().startsWith("☐ ") || line.trimStart().startsWith("☑ ")
                else -> line.trimStart().startsWith("• ")
            }
        }
        var number = 0
        return source.joinToString("\n") { line ->
            if (line.isBlank()) "" else {
                val text = strip(line)
                if (applied) text else when (kind) {
                    "numbered" -> "${++number}. $text"
                    "todo" -> "☐ $text"
                    else -> "• $text"
                }
            }
        }
    }

    fun applyQuote(body: String): String {
        val source = body.lines()
        val applied = source.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }
            ?.all { it.trimStart().startsWith("> ") } == true
        return source.joinToString("\n") { line ->
            if (line.isBlank()) "" else if (applied) line.trimStart().removePrefix("> ") else "> $line"
        }
    }

    private fun strip(line: String): String = line
        .replace(bulletPrefix, "")
        .replace(numericPrefix, "")
}
