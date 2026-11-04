package com.example.tabletworkspace.core

import com.example.tabletworkspace.TextLine
import com.example.tabletworkspace.TextRun
import com.example.tabletworkspace.TextSpan

/**
 * Everything [TextFormatting.wrap] depends on for one on-canvas text object. Span list,
 * body and the resolved pixel size are part of the key, so any edit, style change or zoom
 * produces a different entry while a static canvas reuses the previous layout.
 */
internal data class TextLayoutKey(
    val body: String,
    val spans: List<TextSpan>,
    val widthPx: Float,
    val sizePx: Float,
    val family: String,
    val weight: Int,
    val italic: Boolean
)

/**
 * Bounded LRU cache for wrapped text lines, styled runs and their measured widths. The
 * canvas renderer used to wrap and measure every text object on every frame, which meant
 * a Typeface lookup and Paint.measureText call per character; layouts now survive frames
 * until the text, style, width or zoom changes.
 */
internal class TextLayoutCache(private val maxEntries: Int = 400) {
    class Layout(
        val lines: List<TextLine>,
        val runs: List<List<TextRun>>,
        val runWidths: List<List<Float>>
    )

    private val entries = object : LinkedHashMap<TextLayoutKey, Layout>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<TextLayoutKey, Layout>): Boolean =
            size > maxEntries
    }

    var hits: Int = 0
        private set
    var misses: Int = 0
        private set

    fun layout(key: TextLayoutKey, compute: () -> Layout): Layout {
        entries[key]?.let {
            hits++
            return it
        }
        misses++
        return compute().also { entries[key] = it }
    }

    fun clear() {
        entries.clear()
        hits = 0
        misses = 0
    }

    val size: Int
        get() = entries.size

    companion object {
        val shared = TextLayoutCache()
    }
}
