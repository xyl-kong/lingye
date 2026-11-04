package com.example.tabletworkspace

import java.util.Base64

object TextExtras {
    fun tokens(cardKind: String): String =
        TextLinks.tokens(cardKind) + TextComments.tokens(cardKind) + TextEquations.tokens(cardKind)
}

data class TextLink(val start: Int, val end: Int, val url: String)

/**
 * Inline links are stored as `link:<start>:<end>:<base64url>` tokens in [Shape.cardKind].
 * The existing scale/valign/page/autoheight tokens are preserved untouched.
 */
object TextLinks {
    private const val PREFIX = "link:"
    private const val PAGE_PREFIX = "lingye://page/"

    fun parse(cardKind: String): List<TextLink> = cardKind.split(';').mapNotNull { token ->
        if (!token.startsWith(PREFIX)) return@mapNotNull null
        val parts = token.split(':')
        if (parts.size != 4) return@mapNotNull null
        val start = parts[1].toIntOrNull() ?: return@mapNotNull null
        val end = parts[2].toIntOrNull() ?: return@mapNotNull null
        val url = decode(parts[3]) ?: return@mapNotNull null
        if (start < 0 || start >= end || url.isBlank() || url.length > 2048) null else TextLink(start, end, url)
    }

    fun withoutLinks(cardKind: String): String =
        cardKind.split(';').filterNot { it.startsWith(PREFIX) }.joinToString(";")

    /** The link tokens only, prefixed with `;`, ready to append to another metadata string. */
    fun tokens(cardKind: String): String =
        cardKind.split(';').filter { it.startsWith(PREFIX) }.joinToString("") { ";$it" }

    fun withLink(cardKind: String, start: Int, end: Int, url: String): String {
        if (start >= end || url.isBlank()) return cardKind
        val kept = withoutLinks(cardKind)
        val links = parse(cardKind).filterNot { it.start < end && start < it.end }.plus(TextLink(start, end, url))
            .sortedBy { it.start }
        val suffix = links.joinToString("") { ";$PREFIX${it.start}:${it.end}:${encode(it.url)}" }
        return kept + suffix
    }

    fun withoutOverlapping(cardKind: String, start: Int, end: Int): String {
        val kept = parse(cardKind).filterNot { it.start < end && start < it.end }
        return withoutLinks(cardKind) + kept.joinToString("") { ";$PREFIX${it.start}:${it.end}:${encode(it.url)}" }
    }

    fun linkAt(cardKind: String, offset: Int): TextLink? =
        parse(cardKind).firstOrNull { offset >= it.start && offset < it.end }

    fun pageId(link: TextLink): String? =
        link.url.removePrefix(PAGE_PREFIX).takeIf { link.url.startsWith(PAGE_PREFIX) && it.isNotBlank() }

    /** Keep link offsets aligned when the body text changes. */
    fun adjust(cardKind: String, oldText: String, newText: String): String {
        val links = parse(cardKind)
        if (links.isEmpty()) return cardKind
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
        val mapped = links.mapNotNull { link ->
            val a = when {
                link.start <= changeStart -> link.start
                link.start >= oldChangeEnd -> link.start + delta
                else -> changeStart
            }
            val b = when {
                link.end <= changeStart -> link.end
                link.end >= oldChangeEnd -> link.end + delta
                else -> changeStart
            }
            val start = a.coerceIn(0, newLen)
            val end = b.coerceIn(0, newLen)
            if (start < end) TextLink(start, end, link.url) else null
        }
        val kept = withoutLinks(cardKind)
        return kept + mapped.joinToString("") { ";$PREFIX${it.start}:${it.end}:${encode(it.url)}" }
    }

    private fun encode(value: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))

    private fun decode(value: String): String? = runCatching {
        String(Base64.getUrlDecoder().decode(value), Charsets.UTF_8)
    }.getOrNull()
}
