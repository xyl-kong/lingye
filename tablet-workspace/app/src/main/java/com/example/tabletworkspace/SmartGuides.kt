package com.example.tabletworkspace

import kotlin.math.abs

/** A purple smart guide in world coordinates. Vertical guides run along x = [position]. */
data class SnapGuide(val vertical: Boolean, val position: Float, val start: Float, val end: Float)

data class SnapOutcome(val bounds: CanvasBounds, val dx: Float, val dy: Float, val guides: List<SnapGuide>)

/**
 * Edge/centre snapping for a dragged bounding box. Pure logic: no Compose or page state.
 * Each axis independently picks the closest edge/centre pair within [threshold].
 */
object SmartGuides {
    const val DEFAULT_THRESHOLD = 5f
    private const val EPS = 0.01f

    fun snap(
        moving: CanvasBounds,
        others: List<CanvasBounds>,
        threshold: Float = DEFAULT_THRESHOLD
    ): SnapOutcome {
        if (threshold <= 0f || others.isEmpty() ||
            moving.right <= moving.left || moving.bottom <= moving.top
        ) return SnapOutcome(moving, 0f, 0f, emptyList())

        val xMatch = bestMatch(edges(moving, vertical = true), others, threshold, vertical = true)
        val yMatch = bestMatch(edges(moving, vertical = false), others, threshold, vertical = false)
        val dx = xMatch?.delta ?: 0f
        val dy = yMatch?.delta ?: 0f
        val snapped = CanvasBounds(
            moving.left + dx,
            moving.top + dy,
            moving.right + dx,
            moving.bottom + dy
        )
        val guides = buildList {
            if (xMatch != null) {
                val position = xMatch.position
                val matched = others.filter { hasEdge(it, position, vertical = true) }
                if (matched.isNotEmpty()) {
                    add(
                        SnapGuide(
                            vertical = true,
                            position = position,
                            start = minOf(snapped.top, matched.minOf { it.top }),
                            end = maxOf(snapped.bottom, matched.maxOf { it.bottom })
                        )
                    )
                }
            }
            if (yMatch != null) {
                val position = yMatch.position
                val matched = others.filter { hasEdge(it, position, vertical = false) }
                if (matched.isNotEmpty()) {
                    add(
                        SnapGuide(
                            vertical = false,
                            position = position,
                            start = minOf(snapped.left, matched.minOf { it.left }),
                            end = maxOf(snapped.right, matched.maxOf { it.right })
                        )
                    )
                }
            }
        }
        return SnapOutcome(snapped, dx, dy, guides)
    }

    private data class AxisMatch(val delta: Float, val position: Float)

    private fun bestMatch(
        movingEdges: List<Float>,
        others: List<CanvasBounds>,
        threshold: Float,
        vertical: Boolean
    ): AxisMatch? {
        var best: AxisMatch? = null
        for (movingEdge in movingEdges) {
            for (other in others) {
                for (edge in edges(other, vertical)) {
                    val delta = edge - movingEdge
                    if (abs(delta) <= threshold &&
                        (best == null || abs(delta) < abs(best.delta) - EPS)
                    ) {
                        best = AxisMatch(delta, edge)
                    }
                }
            }
        }
        return best
    }

    private fun edges(bounds: CanvasBounds, vertical: Boolean): List<Float> = if (vertical) {
        listOf(bounds.left, (bounds.left + bounds.right) / 2f, bounds.right)
    } else {
        listOf(bounds.top, (bounds.top + bounds.bottom) / 2f, bounds.bottom)
    }

    private fun hasEdge(bounds: CanvasBounds, position: Float, vertical: Boolean): Boolean =
        edges(bounds, vertical).any { abs(it - position) <= EPS }
}

/** Bounds of all shapes/images that are not part of the current selection. */
fun Page.snapTargetBounds(exclude: Collection<CanvasSelection>): List<CanvasBounds> {
    val excluded = exclude.flatMap { selectionMembers(it) }.toSet()
    val result = mutableListOf<CanvasBounds>()
    shapes.forEachIndexed { index, shape ->
        if (CanvasSelection("shape", index) !in excluded) {
            result.add(CanvasBounds(shape.x, shape.y, shape.x + shape.width, shape.y + shape.height))
        }
    }
    images.forEachIndexed { index, image ->
        if (CanvasSelection("image", index) !in excluded) {
            result.add(CanvasBounds(image.x, image.y, image.x + image.width, image.y + image.height))
        }
    }
    return result
}
