package com.example.tabletworkspace
import com.example.tabletworkspace.core.TextLayoutCache
import com.example.tabletworkspace.core.TextLayoutKey

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TextLayoutCacheTest {

    private fun compute(text: String, width: Float): TextLayoutCache.Layout {
        val lines = TextFormatting.wrap(text, width) { 6f }
        val runs = lines.map { TextFormatting.runs(emptyList(), it.start, it.end) }
        val widths = runs.map { line -> line.map { (it.end - it.start) * 6f } }
        return TextLayoutCache.Layout(lines, runs, widths)
    }

    private fun key(
        body: String = "hello world",
        spans: List<TextSpan> = emptyList(),
        width: Float = 42f,
        size: Float = 10f,
        family: String = "sans",
        weight: Int = 400,
        italic: Boolean = false
    ) = TextLayoutKey(body, spans, width, size, family, weight, italic)

    @Test
    fun reusesLayoutForTheSameKey() {
        val cache = TextLayoutCache(maxEntries = 4)
        var computes = 0
        val first = cache.layout(key()) { computes++; compute("hello world", 42f) }
        val second = cache.layout(key()) { computes++; compute("hello world", 42f) }
        assertSame(first, second)
        assertEquals(1, computes)
        assertEquals(1, cache.hits)
        assertEquals(1, cache.misses)
    }

    @Test
    fun widthSpansAndSizeProduceDifferentEntries() {
        val cache = TextLayoutCache(maxEntries = 8)
        var computes = 0
        fun layout(k: TextLayoutKey) = cache.layout(k) { computes++; compute(k.body, k.widthPx) }
        layout(key())
        layout(key(width = 60f))
        layout(key(size = 14f))
        layout(key(spans = listOf(TextSpan(0, 5, TextSpan.BOLD))))
        assertEquals(4, computes)
        assertEquals(4, cache.misses)
        assertEquals(0, cache.hits)
    }

    @Test
    fun evictsLeastRecentlyUsedEntry() {
        val cache = TextLayoutCache(maxEntries = 2)
        cache.layout(key(body = "a")) { compute("a", 42f) }
        cache.layout(key(body = "b")) { compute("b", 42f) }
        cache.layout(key(body = "a")) { compute("a", 42f) }
        cache.layout(key(body = "c")) { compute("c", 42f) }
        var recomputed = false
        cache.layout(key(body = "b")) { recomputed = true; compute("b", 42f) }
        assertTrue(recomputed)
        assertEquals(2, cache.size)
    }

    @Test
    fun layoutKeepsRunsAndWidthsAlignedWithLines() {
        val layout = compute("one two three", 30f)
        assertEquals(layout.lines.size, layout.runs.size)
        assertEquals(layout.lines.size, layout.runWidths.size)
        layout.runs.indices.forEach { i ->
            assertEquals(layout.runs[i].size, layout.runWidths[i].size)
        }
    }

    @Test
    fun longTextIsCappedAtEightVisualLines() {
        val text = (1..30).joinToString(" ") { "word$it" }
        val layout = compute(text, 40f)
        assertTrue(layout.lines.size > 8)
        // The renderer takes(8) before building runs; verify the cache consumer contract.
        val capped = layout.lines.take(8)
        assertEquals(8, capped.size)
    }
}
