package com.example.tabletworkspace

/** A bold/italic/underline/strike/code/highlight range over a text body. Offsets are UTF-16 code units. */
data class TextSpan(val start: Int, val end: Int, val flags: Int) {
    companion object {
        const val BOLD = 1
        const val ITALIC = 2
        const val UNDERLINE = 4
        const val STRIKE = 8
        const val CODE = 16
        const val FG_SHIFT = 5
        const val FG_MASK = 0xF shl FG_SHIFT
        const val BG_SHIFT = 9
        const val BG_MASK = 0xF shl BG_SHIFT
        const val LINK = 1 shl 13
        const val MATH = 1 shl 14
        const val COMMENT = 1 shl 15
        const val ALL = BOLD or ITALIC or UNDERLINE or STRIKE or CODE or FG_MASK or BG_MASK or LINK or MATH or COMMENT
        fun foreground(flags: Int): Int = (flags and FG_MASK) shr FG_SHIFT
        fun background(flags: Int): Int = (flags and BG_MASK) shr BG_SHIFT
        fun withForeground(flags: Int, index: Int): Int =
            (flags and FG_MASK.inv()) or ((index and 0xF) shl FG_SHIFT)
        fun withBackground(flags: Int, index: Int): Int =
            (flags and BG_MASK.inv()) or ((index and 0xF) shl BG_SHIFT)
    }
}

/** Palette indexes shared by the selection toolbar and the canvas renderer. 0 means default. */
object TextHighlightPalette {
    val foreground = intArrayOf(
        0, 0xFFC62222.toInt(), 0xFFD34F0B.toInt(), 0xFFB67C04.toInt(), 0xFF149343.toInt(),
        0xFF0782A0.toInt(), 0xFF2159D3.toInt(), 0xFF842ED3.toInt(), 0xFF7A7A7A.toInt()
    )
    val background = intArrayOf(
        0, 0xFFFED5D5.toInt(), 0xFFFEDFBB.toInt(), 0xFFFEF3A1.toInt(), 0xFFE1FAB1.toInt(),
        0xFFADF8E9.toInt(), 0xFFCCE2FE.toInt(), 0xFFEDDDFF.toInt(), 0xFFEAECEF.toInt()
    )
    val names = listOf(
        "Default", "Red", "Orange", "Yellow", "Green", "Teal", "Blue", "Purple", "Grey"
    )
    fun foregroundColor(index: Int): Int? = foreground.getOrNull(index)?.takeIf { it != 0 }
    fun backgroundColor(index: Int): Int? = background.getOrNull(index)?.takeIf { it != 0 }
}

data class TextRun(val start: Int, val end: Int, val flags: Int)

/** A visual line range produced by wrapping a text body to the object's width. */
data class TextLine(val start: Int, val end: Int)

object TextFormatting {
    val FONT_FAMILIES = setOf("sans", "serif", "mono")
    val FONT_STYLES = setOf("normal", "italic")

    fun validWeight(weight: Int): Boolean = weight in 100..900 && weight % 100 == 0

    /** Clamp to the current body, sort, drop empties and merge adjacent identical flags. */
    fun normalize(spans: List<TextSpan>, length: Int): List<TextSpan> {
        val clipped = spans.mapNotNull { span ->
            val start = span.start.coerceIn(0, length)
            val end = span.end.coerceIn(0, length)
            val flags = span.flags and TextSpan.ALL
            if (start < end && flags != 0) TextSpan(start, end, flags) else null
        }.sortedWith(compareBy<TextSpan> { it.start }.thenBy { it.end })
        val result = mutableListOf<TextSpan>()
        clipped.forEach { span ->
            val last = result.lastOrNull()
            when {
                last == null -> result.add(span)
                span.start < last.end -> {
                    // Keep the model non-overlapping even if a caller supplied overlaps.
                    val end = maxOf(last.end, span.end)
                    result[result.lastIndex] = TextSpan(last.start, end, last.flags or span.flags)
                }
                span.start == last.end && span.flags == last.flags ->
                    result[result.lastIndex] = TextSpan(last.start, span.end, last.flags)
                else -> result.add(span)
            }
        }
        return result
    }

    fun isValid(spans: List<TextSpan>, length: Int): Boolean {
        var cursor = 0
        spans.forEach { span ->
            if (span.start < cursor || span.start >= span.end || span.end > length) return false
            if (span.flags and TextSpan.ALL.inv() != 0) return false
            cursor = span.end
        }
        return true
    }

    fun flagsAt(spans: List<TextSpan>, index: Int): Int =
        spans.firstOrNull { index >= it.start && index < it.end }?.flags ?: 0

    /** Flags shared by every character in [start,end); 0 when the range is empty or mixed. */
    fun rangeFlags(spans: List<TextSpan>, start: Int, end: Int): Int {
        if (start >= end) return 0
        var common = TextSpan.ALL
        runs(spans, start, end).forEach { common = common and it.flags }
        return common and TextSpan.ALL
    }

    /** Toggle one flag over a selection, splitting and merging the affected spans. */
    fun toggleRange(spans: List<TextSpan>, start: Int, end: Int, flag: Int, length: Int): List<TextSpan> {
        if (start >= end) return spans
        val remove = rangeFlags(spans, start, end) and flag != 0
        val boundaries = sortedSetOf(0, length, start.coerceIn(0, length), end.coerceIn(0, length))
        spans.forEach { span ->
            boundaries.add(span.start.coerceIn(0, length))
            boundaries.add(span.end.coerceIn(0, length))
        }
        val points = boundaries.toList()
        val result = mutableListOf<TextSpan>()
        for (i in 0 until points.lastIndex) {
            val a = points[i]
            val b = points[i + 1]
            if (a >= b) continue
            var flags = flagsAt(spans, a)
            if (a >= start && b <= end) flags = if (remove) flags and flag.inv() else flags or flag
            if (flags != 0) {
                val last = result.lastOrNull()
                if (last != null && last.end == a && last.flags == flags) {
                    result[result.lastIndex] = TextSpan(last.start, b, flags)
                } else {
                    result.add(TextSpan(a, b, flags))
                }
            }
        }
        return normalize(result, length)
    }

    /** Add one flag over a selection without toggling it off. */
    fun addFlagRange(spans: List<TextSpan>, start: Int, end: Int, flag: Int, length: Int): List<TextSpan> {
        if (start >= end) return spans
        val boundaries = sortedSetOf(0, length, start.coerceIn(0, length), end.coerceIn(0, length))
        spans.forEach { span ->
            boundaries.add(span.start.coerceIn(0, length))
            boundaries.add(span.end.coerceIn(0, length))
        }
        val points = boundaries.toList()
        val result = mutableListOf<TextSpan>()
        for (i in 0 until points.lastIndex) {
            val a = points[i]
            val b = points[i + 1]
            if (a >= b) continue
            var flags = flagsAt(spans, a)
            if (a >= start && b <= end) flags = flags or flag
            if (flags == 0) continue
            val last = result.lastOrNull()
            if (last != null && last.end == a && last.flags == flags) {
                result[result.lastIndex] = TextSpan(last.start, b, flags)
            } else {
                result.add(TextSpan(a, b, flags))
            }
        }
        return normalize(result, length)
    }

    /** Remove one flag over a selection without touching other flags. */
    fun removeFlagRange(spans: List<TextSpan>, start: Int, end: Int, flag: Int, length: Int): List<TextSpan> {
        if (start >= end) return spans
        val boundaries = sortedSetOf(0, length, start.coerceIn(0, length), end.coerceIn(0, length))
        spans.forEach { span ->
            boundaries.add(span.start.coerceIn(0, length))
            boundaries.add(span.end.coerceIn(0, length))
        }
        val points = boundaries.toList()
        val result = mutableListOf<TextSpan>()
        for (i in 0 until points.lastIndex) {
            val a = points[i]
            val b = points[i + 1]
            if (a >= b) continue
            var flags = flagsAt(spans, a)
            if (a >= start && b <= end) flags = flags and flag.inv()
            if (flags == 0) continue
            val last = result.lastOrNull()
            if (last != null && last.end == a && last.flags == flags) {
                result[result.lastIndex] = TextSpan(last.start, b, flags)
            } else {
                result.add(TextSpan(a, b, flags))
            }
        }
        return normalize(result, length)
    }

    /** Replace the 4-bit foreground/background color field over a selection. */    fun setColorRange(spans: List<TextSpan>, start: Int, end: Int, mask: Int, shift: Int, index: Int, length: Int): List<TextSpan> {
        if (start >= end) return spans
        val value = (index and 0xF) shl shift
        val boundaries = sortedSetOf(0, length, start.coerceIn(0, length), end.coerceIn(0, length))
        spans.forEach { span ->
            boundaries.add(span.start.coerceIn(0, length))
            boundaries.add(span.end.coerceIn(0, length))
        }
        val points = boundaries.toList()
        val result = mutableListOf<TextSpan>()
        for (i in 0 until points.lastIndex) {
            val a = points[i]
            val b = points[i + 1]
            if (a >= b) continue
            var flags = flagsAt(spans, a)
            if (a >= start && b <= end) flags = (flags and mask.inv()) or value
            if (flags == 0) continue
            val last = result.lastOrNull()
            if (last != null && last.end == a && last.flags == flags) {
                result[result.lastIndex] = TextSpan(last.start, b, flags)
            } else {
                result.add(TextSpan(a, b, flags))
            }
        }
        return normalize(result, length)
    }

    /**
     * Adjust spans after a text edit. Uses common prefix/suffix so insertion inside a span
     * extends it, deletion trims it, and edits outside simply shift offsets.
     */
    fun adjustForEdit(spans: List<TextSpan>, oldText: String, newText: String): List<TextSpan> {
        if (spans.isEmpty()) return spans
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
        val mapped = spans.mapNotNull { span ->
            val a = when {
                span.start <= changeStart -> span.start
                span.start >= oldChangeEnd -> span.start + delta
                else -> changeStart
            }
            val b = when {
                span.end <= changeStart -> span.end
                span.end >= oldChangeEnd -> span.end + delta
                else -> changeStart
            }
            val start = a.coerceIn(0, newLen)
            val end = b.coerceIn(0, newLen)
            if (start < end) TextSpan(start, end, span.flags) else null
        }
        return normalize(mapped, newLen)
    }

    /**
     * Break [text] into visual lines no wider than [maxWidth]. [charWidth] measures the code
     * point starting at an offset, so the caller can apply span styles. Explicit newlines
     * always break; otherwise the last space in the line is preferred, then a code point.
     */
    fun wrap(text: String, maxWidth: Float, charWidth: (Int) -> Float): List<TextLine> {
        val result = mutableListOf<TextLine>()
        var lineStart = 0
        while (true) {
            val newline = text.indexOf('\n', lineStart)
            val lineEnd = if (newline < 0) text.length else newline
            var segmentStart = lineStart
            var produced = false
            while (segmentStart < lineEnd) {
                var width = 0f
                var cursor = segmentStart
                var lastSpace = -1
                while (cursor < lineEnd) {
                    val step = codePointLength(text, cursor)
                    val w = charWidth(cursor)
                    if (width + w > maxWidth && cursor > segmentStart) break
                    width += w
                    if (text[cursor] == ' ') lastSpace = cursor
                    cursor += step
                }
                if (cursor >= lineEnd) {
                    result.add(TextLine(segmentStart, lineEnd))
                    produced = true
                    break
                }
                val breakAt = if (lastSpace > segmentStart) lastSpace else cursor
                val previousStart = segmentStart
                result.add(TextLine(segmentStart, breakAt))
                produced = true
                segmentStart = if (breakAt < lineEnd && text[breakAt] == ' ') breakAt + 1 else breakAt
                if (segmentStart <= previousStart) break
            }
            if (!produced) result.add(TextLine(lineStart, lineEnd))
            if (newline < 0) break
            lineStart = newline + 1
        }
        return result
    }

    private fun codePointLength(text: String, index: Int): Int =
        if (index + 1 < text.length && Character.isHighSurrogate(text[index]) &&
            Character.isLowSurrogate(text[index + 1])
        ) 2 else 1

    /** Split a line into styled runs for native canvas drawing. */
    fun runs(spans: List<TextSpan>, lineStart: Int, lineEnd: Int): List<TextRun> {        if (lineStart >= lineEnd) return emptyList()
        val points = sortedSetOf(lineStart, lineEnd)
        spans.forEach { span ->
            if (span.end > lineStart && span.start < lineEnd) {
                points.add(span.start.coerceIn(lineStart, lineEnd))
                points.add(span.end.coerceIn(lineStart, lineEnd))
            }
        }
        val result = mutableListOf<TextRun>()
        val list = points.toList()
        for (i in 0 until list.lastIndex) {
            val a = list[i]
            val b = list[i + 1]
            if (a >= b) continue
            val flags = flagsAt(spans, a)
            val last = result.lastOrNull()
            if (last != null && last.end == a && last.flags == flags) {
                result[result.lastIndex] = TextRun(last.start, b, flags)
            } else {
                result.add(TextRun(a, b, flags))
            }
        }
        return result
    }
}
