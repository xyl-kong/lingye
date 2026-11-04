package com.example.tabletworkspace

import java.util.Base64

data class TextComment(val start: Int, val end: Int, val text: String)

/** Local comment threads anchored to text ranges, stored in [Shape.cardKind]. */
object TextComments {
    private const val PREFIX = "comment:"

    fun parse(cardKind: String): List<TextComment> = cardKind.split(';').mapNotNull { token ->
        if (!token.startsWith(PREFIX)) return@mapNotNull null
        val parts = token.split(':')
        if (parts.size != 4) return@mapNotNull null
        val start = parts[1].toIntOrNull() ?: return@mapNotNull null
        val end = parts[2].toIntOrNull() ?: return@mapNotNull null
        val text = decode(parts[3]) ?: return@mapNotNull null
        if (start < 0 || start >= end || text.isBlank() || text.length > 4000) null else TextComment(start, end, text)
    }

    fun withoutComments(cardKind: String): String =
        cardKind.split(';').filterNot { it.startsWith(PREFIX) }.joinToString(";")

    fun tokens(cardKind: String): String =
        cardKind.split(';').filter { it.startsWith(PREFIX) }.joinToString("") { ";$it" }

    fun withComment(cardKind: String, start: Int, end: Int, text: String): String {
        if (start >= end || text.isBlank()) return cardKind
        val clean = text.trim().take(4000)
        val kept = parse(cardKind).filterNot { it.start < end && start < it.end }
        return withoutComments(cardKind) + kept.joinToString("") { ";$PREFIX${it.start}:${it.end}:${encode(it.text)}" } +
            ";$PREFIX$start:$end:${encode(clean)}"
    }

    fun withoutComment(cardKind: String, comment: TextComment): String {
        val kept = parse(cardKind).filterNot {
            it.start == comment.start && it.end == comment.end && it.text == comment.text
        }
        return withoutComments(cardKind) + kept.joinToString("") { ";$PREFIX${it.start}:${it.end}:${encode(it.text)}" }
    }

    fun withoutOverlapping(cardKind: String, start: Int, end: Int): String {
        val kept = parse(cardKind).filterNot { it.start < end && start < it.end }
        return withoutComments(cardKind) + kept.joinToString("") { ";$PREFIX${it.start}:${it.end}:${encode(it.text)}" }
    }

    fun at(cardKind: String, offset: Int): List<TextComment> =
        parse(cardKind).filter { offset >= it.start && offset < it.end }

    fun adjust(cardKind: String, oldText: String, newText: String): String {
        val comments = parse(cardKind)
        if (comments.isEmpty()) return cardKind
        val oldLen = oldText.length
        val newLen = newText.length
        var prefix = 0
        while (prefix < oldLen && prefix < newLen && oldText[prefix] == newText[prefix]) prefix++
        var suffix = 0
        while (suffix < oldLen - prefix && suffix < newLen - prefix &&
            oldText[oldLen - 1 - suffix] == newText[newLen - 1 - suffix]
        ) suffix++
        val changeStart = prefix
        val oldChangeEnd = oldLen - suffix
        val newChangeEnd = newLen - suffix
        val delta = newChangeEnd - oldChangeEnd
        val mapped = comments.mapNotNull { comment ->
            val a = when {
                comment.start <= changeStart -> comment.start
                comment.start >= oldChangeEnd -> comment.start + delta
                else -> changeStart
            }
            val b = when {
                comment.end <= changeStart -> comment.end
                comment.end >= oldChangeEnd -> comment.end + delta
                else -> changeStart
            }
            val start = a.coerceIn(0, newLen)
            val end = b.coerceIn(0, newLen)
            if (start < end) TextComment(start, end, comment.text) else null
        }
        return withoutComments(cardKind) + mapped.joinToString("") { ";$PREFIX${it.start}:${it.end}:${encode(it.text)}" }
    }

    private fun encode(value: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))

    private fun decode(value: String): String? = runCatching {
        String(Base64.getUrlDecoder().decode(value), Charsets.UTF_8)
    }.getOrNull()
}
