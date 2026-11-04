package com.example.tabletworkspace

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Area eraser: any unlocked object touched by the eraser drag segment is removed as a whole,
 * matching the official BlockSuite eraser (it deletes intersecting elements, not pixels).
 */
object AreaEraser {
    /** Objects the eraser would delete for one drag segment; used for the live preview and on release. */
    fun hits(page: Page, a: Point, b: Point, radius: Float): List<CanvasSelection> {
        require(radius.isFinite() && radius >= 0f) { "橡皮半径无效" }
        val result = mutableListOf<CanvasSelection>()
        page.shapes.forEachIndexed { i, shape ->
            if (!shape.locked && segmentHitsRect(a, b, shape.x, shape.y, shape.width, shape.height, radius)) {
                result += CanvasSelection("shape", i)
            }
        }
        page.images.forEachIndexed { i, image ->
            if (segmentHitsRect(a, b, image.x, image.y, image.width, image.height, radius)) {
                result += CanvasSelection("image", i)
            }
        }
        page.strokes.forEachIndexed { i, stroke ->
            if (stroke.isNotEmpty() && strokeHits(stroke, page.strokeStyles.getOrNull(i)?.width ?: 0f, a, b, radius)) {
                result += CanvasSelection("stroke", i)
            }
        }
        page.connectors.forEachIndexed { i, _ ->
            val samples = connectorSamples(page.resolvedConnector(i))
            if (samples.any { pointSegmentDistance(it, a, b) <= radius }) result += CanvasSelection("connector", i)
        }
        return result
    }

    /** Deletes the given selections in a safe order (connectors before owners, higher indices first). */
    fun erase(page: Page, items: Collection<CanvasSelection>): Boolean {
        val targets = items.distinct().sortedWith(compareBy<CanvasSelection> { it.type }.thenByDescending { it.index })
        var removed = false
        targets.forEach { selection ->
            val valid = when (selection.type) {
                "shape" -> selection.index in page.shapes.indices
                "image" -> selection.index in page.images.indices
                "stroke" -> selection.index in page.strokes.indices
                "connector" -> selection.index in page.connectors.indices
                else -> false
            }
            if (valid) {
                page.deleteSelection(selection)
                removed = true
            }
        }
        return removed
    }

    fun pointSegmentDistance(p: Point, a: Point, b: Point): Float {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val length = dx * dx + dy * dy
        val t = if (length == 0f) 0f else (((p.x - a.x) * dx + (p.y - a.y) * dy) / length).coerceIn(0f, 1f)
        return hypot(p.x - (a.x + t * dx), p.y - (a.y + t * dy))
    }

    /** Segment vs axis-aligned rect inflated by [radius] (Liang–Barsky clip). */
    fun segmentHitsRect(a: Point, b: Point, x: Float, y: Float, w: Float, h: Float, radius: Float): Boolean {
        val left = x - radius
        val right = x + w + radius
        val top = y - radius
        val bottom = y + h + radius
        fun inside(p: Point) = p.x in left..right && p.y in top..bottom
        if (inside(a) || inside(b)) return true
        var t0 = 0f
        var t1 = 1f
        val dx = b.x - a.x
        val dy = b.y - a.y
        fun clip(p: Float, q: Float): Boolean {
            if (p == 0f) return q >= 0f
            val t = q / p
            if (p < 0f) {
                if (t > t1) return false
                if (t > t0) t0 = t
            } else {
                if (t < t0) return false
                if (t < t1) t1 = t
            }
            return true
        }
        return clip(-dx, a.x - left) && clip(dx, right - a.x) && clip(-dy, a.y - top) && clip(dy, bottom - a.y)
    }

    private fun strokeHits(points: List<Point>, width: Float, a: Point, b: Point, radius: Float): Boolean {
        val threshold = radius + width / 2f
        if (points.any { pointSegmentDistance(it, a, b) <= threshold }) return true
        return points.zipWithNext().any { (p, q) -> segmentsDistance(p, q, a, b) <= threshold }
    }

    private fun segmentsDistance(p1: Point, p2: Point, q1: Point, q2: Point): Float {
        if (segmentsIntersect(p1, p2, q1, q2)) return 0f
        return min(
            min(pointSegmentDistance(p1, q1, q2), pointSegmentDistance(p2, q1, q2)),
            min(pointSegmentDistance(q1, p1, p2), pointSegmentDistance(q2, p1, p2))
        )
    }

    private fun segmentsIntersect(p1: Point, p2: Point, q1: Point, q2: Point): Boolean {
        fun orient(a: Point, b: Point, c: Point): Float = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
        val d1 = orient(q1, q2, p1)
        val d2 = orient(q1, q2, p2)
        val d3 = orient(p1, p2, q1)
        val d4 = orient(p1, p2, q2)
        if (abs(d1) < 1e-4f && onSegment(q1, q2, p1)) return true
        if (abs(d2) < 1e-4f && onSegment(q1, q2, p2)) return true
        if (abs(d3) < 1e-4f && onSegment(p1, p2, q1)) return true
        if (abs(d4) < 1e-4f && onSegment(p1, p2, q2)) return true
        return (d1 > 0f) != (d2 > 0f) && (d3 > 0f) != (d4 > 0f)
    }

    private fun onSegment(a: Point, b: Point, p: Point): Boolean =
        p.x in min(a.x, b.x)..max(a.x, b.x) && p.y in min(a.y, b.y)..max(a.y, b.y)
}
