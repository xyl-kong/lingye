package com.example.tabletworkspace

/** A one-use copy of visual inline style, scoped to the current canvas page. */
internal data class TextFormatPaint(
    val sourceShapeId: String,
    val sourceStart: Int,
    val sourceEnd: Int,
    val flags: Int
) {
    fun isSource(shapeId: String, start: Int, end: Int): Boolean =
        sourceShapeId == shapeId && sourceStart == start && sourceEnd == end
}

internal object TextFormatPainter {
    // Links, equations and comments refer to independent content metadata, not visual style.
    const val VISUAL_MASK = TextSpan.BOLD or TextSpan.ITALIC or TextSpan.UNDERLINE or
        TextSpan.STRIKE or TextSpan.CODE or TextSpan.FG_MASK or TextSpan.BG_MASK

    fun capture(shape: Shape, start: Int, end: Int): TextFormatPaint? {
        if (shape.type != "text" || start < 0 || start >= end || end > shape.body.length) return null
        return TextFormatPaint(shape.id, start, end, TextFormatting.flagsAt(shape.textSpans, start) and VISUAL_MASK)
    }

    fun apply(spans: List<TextSpan>, start: Int, end: Int, length: Int, paint: TextFormatPaint): List<TextSpan> {
        if (start < 0 || start >= end || end > length) return spans
        val points = sortedSetOf(0, length, start, end)
        spans.forEach { points.add(it.start.coerceIn(0, length)); points.add(it.end.coerceIn(0, length)) }
        val boundaries = points.toList()
        val result = mutableListOf<TextSpan>()
        for (index in 0 until boundaries.lastIndex) {
            val from = boundaries[index]
            val to = boundaries[index + 1]
            if (from == to) continue
            val previous = TextFormatting.flagsAt(spans, from)
            val flags = if (from >= start && to <= end)
                (previous and VISUAL_MASK.inv()) or paint.flags
            else previous
            if (flags != 0) result.add(TextSpan(from, to, flags))
        }
        return TextFormatting.normalize(result, length)
    }
}
