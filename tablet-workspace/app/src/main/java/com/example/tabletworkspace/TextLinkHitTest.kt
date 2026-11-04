package com.example.tabletworkspace

/**
 * Approximate link hit testing for a canvas text object. It mirrors the renderer's
 * wrap rules (CJK = 1 em, other code points = 0.58 em, line height = 1 em, at most 8 lines).
 */
object TextLinkHitTest {
    fun linkAt(shape: Shape, localX: Float, localY: Float): TextLink? {
        val links = TextLinks.parse(shape.cardKind)
        if (links.isEmpty() || shape.body.isEmpty()) return null
        val em = shape.fontSize.coerceAtLeast(1f)
        val pad = 24f
        val innerWidth = (shape.width - pad * 2f).coerceAtLeast(1f)
        val innerHeight = (shape.height - pad * 2f).coerceAtLeast(1f)
        val lines = TextFormatting.wrap(shape.body, innerWidth) { index ->
            val cp = shape.body.codePointAt(index)
            if (cp >= 0x2e80) em else em * 0.58f
        }.take(8)
        if (lines.isEmpty()) return null
        val textHeight = em * lines.size
        val textTop = pad + when (verticalAlign(shape.cardKind)) {
            "middle" -> ((innerHeight - textHeight) / 2f).coerceAtLeast(0f)
            "bottom" -> (innerHeight - textHeight).coerceAtLeast(0f)
            else -> 0f
        }
        val relativeY = localY - textTop
        if (relativeY < 0f) return null
        val lineIndex = (relativeY / em).toInt()
        val line = lines.getOrNull(lineIndex) ?: return null
        val lineWidth = measure(shape.body, line.start, line.end, em)
        val startX = pad + when (shape.textAlign) {
            "center" -> (innerWidth - lineWidth) / 2f
            "right" -> innerWidth - lineWidth
            else -> 0f
        }
        var cursor = startX
        var index = line.start
        while (index < line.end) {
            val step = if (index + 1 < shape.body.length && Character.isHighSurrogate(shape.body[index]) &&
                Character.isLowSurrogate(shape.body[index + 1])
            ) 2 else 1
            val width = measure(shape.body, index, index + step, em)
            if (localX in cursor..(cursor + width)) {
                return TextLinks.linkAt(shape.cardKind, index)
            }
            cursor += width
            index += step
        }
        return null
    }

    private fun measure(text: String, start: Int, end: Int, em: Float): Float {
        var width = 0f
        var i = start
        while (i < end) {
            val step = if (i + 1 < end && Character.isHighSurrogate(text[i]) && Character.isLowSurrogate(text[i + 1])) 2 else 1
            val cp = text.codePointAt(i)
            width += if (cp >= 0x2e80) em else em * 0.58f
            i += step
        }
        return width
    }

    private fun verticalAlign(cardKind: String): String =
        Regex("(?:^|;)valign:([a-zA-Z]+)(?:;|$)").find(cardKind)?.groupValues?.getOrNull(1) ?: "top"
}
