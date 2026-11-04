package com.example.tabletworkspace

import java.util.Base64

data class TextEquation(val start: Int, val end: Int, val tex: String)

/** Inline equations: the rendered Unicode lives in the body, the TeX source in cardKind. */
object TextEquations {
    private const val PREFIX = "math:"

    fun parse(cardKind: String): List<TextEquation> = cardKind.split(';').mapNotNull { token ->
        if (!token.startsWith(PREFIX)) return@mapNotNull null
        val parts = token.split(':')
        if (parts.size != 4) return@mapNotNull null
        val start = parts[1].toIntOrNull() ?: return@mapNotNull null
        val end = parts[2].toIntOrNull() ?: return@mapNotNull null
        val tex = decode(parts[3]) ?: return@mapNotNull null
        if (start < 0 || start >= end || tex.isBlank() || tex.length > 2000) null else TextEquation(start, end, tex)
    }

    fun withoutEquations(cardKind: String): String =
        cardKind.split(';').filterNot { it.startsWith(PREFIX) }.joinToString(";")

    fun tokens(cardKind: String): String =
        cardKind.split(';').filter { it.startsWith(PREFIX) }.joinToString("") { ";$it" }

    fun withEquation(cardKind: String, start: Int, end: Int, tex: String): String {
        if (start >= end || tex.isBlank()) return cardKind
        val kept = parse(cardKind).filterNot { it.start < end && start < it.end }
        return withoutEquations(cardKind) + kept.joinToString("") { ";$PREFIX${it.start}:${it.end}:${encode(it.tex)}" } +
            ";$PREFIX$start:$end:${encode(tex.trim().take(2000))}"
    }

    fun withoutOverlapping(cardKind: String, start: Int, end: Int): String {
        val kept = parse(cardKind).filterNot { it.start < end && start < it.end }
        return withoutEquations(cardKind) + kept.joinToString("") { ";$PREFIX${it.start}:${it.end}:${encode(it.tex)}" }
    }

    fun at(cardKind: String, offset: Int): TextEquation? =
        parse(cardKind).firstOrNull { offset >= it.start && offset < it.end }

    fun adjust(cardKind: String, oldText: String, newText: String): String {
        val equations = parse(cardKind)
        if (equations.isEmpty()) return cardKind
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
        val mapped = equations.mapNotNull { eq ->
            val a = when {
                eq.start <= changeStart -> eq.start
                eq.start >= oldChangeEnd -> eq.start + delta
                else -> changeStart
            }
            val b = when {
                eq.end <= changeStart -> eq.end
                eq.end >= oldChangeEnd -> eq.end + delta
                else -> changeStart
            }
            val start = a.coerceIn(0, newLen)
            val end = b.coerceIn(0, newLen)
            if (start < end) TextEquation(start, end, eq.tex) else null
        }
        return withoutEquations(cardKind) + mapped.joinToString("") { ";$PREFIX${it.start}:${it.end}:${encode(it.tex)}" }
    }

    private fun encode(value: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))

    private fun decode(value: String): String? = runCatching {
        String(Base64.getUrlDecoder().decode(value), Charsets.UTF_8)
    }.getOrNull()
}
