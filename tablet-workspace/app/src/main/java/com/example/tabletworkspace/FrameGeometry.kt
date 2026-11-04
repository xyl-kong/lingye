package com.example.tabletworkspace

import kotlin.math.max
import kotlin.math.min

/**
 * Frame section semantics, mirroring AFFiNE's edgeless frames as far as the flat
 * shape model allows. Membership is geometric (fully contained objects) rather than a
 * stored parent relation, so it is recalculated from the current layout.
 */
data class FrameFit(val scale: Float, val offsetX: Float, val offsetY: Float)

object FrameSemantics {
    /** Height of the title strip above a frame, in world units. */
    const val TITLE_ZONE = 28f
    const val MIN_SCALE = 0.25f
    const val MAX_SCALE = 2.5f
    const val PRESENTATION_MIN_SCALE = 0.1f
    const val PRESENTATION_MAX_SCALE = 16f
    const val MAX_FIT_MARGIN = 56f

    fun isFrame(shape: Shape): Boolean = shape.type == "frame"

    /** A frame is grabbed by its border band or its title strip; taps in the body pass through to its contents. */
    fun hit(shape: Shape, point: Point, tolerance: Float): Boolean {
        if (!isFrame(shape)) return false
        val tol = max(0f, tolerance)
        val insideTitle = point.x >= shape.x - tol && point.x <= shape.x + shape.width + tol &&
            point.y >= shape.y - TITLE_ZONE - tol && point.y < shape.y
        if (insideTitle) return true
        val insideWide = point.x >= shape.x - tol && point.x <= shape.x + shape.width + tol &&
            point.y >= shape.y - tol && point.y <= shape.y + shape.height + tol
        val insideCore = point.x >= shape.x + tol && point.x <= shape.x + shape.width - tol &&
            point.y >= shape.y + tol && point.y <= shape.y + shape.height - tol
        return insideWide && !insideCore
    }

    fun contains(outer: CanvasBounds, inner: CanvasBounds): Boolean =
        inner.left >= outer.left && inner.top >= outer.top &&
            inner.right <= outer.right && inner.bottom <= outer.bottom

    /**
     * Viewport that centers [frame] inside a canvas of [canvasWidth] x [canvasHeight] dp,
     * leaving room for the title strip and a margin.
     */
    fun fit(frame: Shape, canvasWidth: Float, canvasHeight: Float, margin: Float = MAX_FIT_MARGIN, minScale: Float = MIN_SCALE, maxScale: Float = MAX_SCALE): FrameFit? {
        if (!isFrame(frame) || frame.width <= 0f || frame.height <= 0f) return null
        if (!canvasWidth.isFinite() || !canvasHeight.isFinite() || canvasWidth <= 0f || canvasHeight <= 0f) return null
        val usableW = (canvasWidth - 2f * margin).coerceAtLeast(60f)
        val usableH = (canvasHeight - 2f * margin - TITLE_ZONE).coerceAtLeast(60f)
        val scale = min(usableW / frame.width, usableH / frame.height).coerceIn(minScale, maxScale)
        val centerX = frame.x + frame.width / 2f
        val centerY = frame.y + TITLE_ZONE / 2f + frame.height / 2f
        return FrameFit(scale, canvasWidth / 2f - centerX * scale, canvasHeight / 2f - centerY * scale)
    }

    /** Presentation "fill" view: the frame covers the whole canvas, cropping the overflow. */
    fun fill(frame: Shape, canvasWidth: Float, canvasHeight: Float, minScale: Float = MIN_SCALE, maxScale: Float = MAX_SCALE): FrameFit? {
        if (!isFrame(frame) || frame.width <= 0f || frame.height <= 0f) return null
        if (!canvasWidth.isFinite() || !canvasHeight.isFinite() || canvasWidth <= 0f || canvasHeight <= 0f) return null
        val scale = max(canvasWidth / frame.width, canvasHeight / frame.height).coerceIn(minScale, maxScale)
        val centerX = frame.x + frame.width / 2f
        val centerY = frame.y + frame.height / 2f
        return FrameFit(scale, canvasWidth / 2f - centerX * scale, canvasHeight / 2f - centerY * scale)
    }
}

/** Objects that currently sit fully inside the frame, including nested frames and their contents. */
fun Page.frameMembers(frameIndex: Int): List<CanvasSelection> {
    val frame = shapes.getOrNull(frameIndex) ?: return emptyList()
    if (!FrameSemantics.isFrame(frame)) return emptyList()
    val box = CanvasBounds(frame.x, frame.y, frame.x + frame.width, frame.y + frame.height)
    val result = mutableListOf<CanvasSelection>()
    shapes.forEachIndexed { i, s ->
        if (i != frameIndex &&
            FrameSemantics.contains(box, CanvasBounds(s.x, s.y, s.x + s.width, s.y + s.height))
        ) result += CanvasSelection("shape", i)
    }
    images.forEachIndexed { i, im ->
        if (FrameSemantics.contains(box, CanvasBounds(im.x, im.y, im.x + im.width, im.y + im.height))) result += CanvasSelection("image", i)
    }
    strokes.forEachIndexed { i, pts ->
        if (pts.isNotEmpty()) {
            val b = CanvasBounds(pts.minOf { it.x }, pts.minOf { it.y }, pts.maxOf { it.x }, pts.maxOf { it.y })
            if (FrameSemantics.contains(box, b)) result += CanvasSelection("stroke", i)
        }
    }
    connectors.forEachIndexed { i, c ->
        if (c.startShape.isEmpty() && c.endShape.isEmpty()) {
            val samples = connectorSamples(resolvedConnector(i))
            if (samples.isNotEmpty()) {
                val b = CanvasBounds(
                    samples.minOf { it.x }, samples.minOf { it.y },
                    samples.maxOf { it.x }, samples.maxOf { it.y }
                )
                if (FrameSemantics.contains(box, b)) result += CanvasSelection("connector", i)
            }
        }
    }
    return result
}

/** Expands frame selections to include their contained objects. */
fun Page.withFrameMembers(items: Collection<CanvasSelection>): List<CanvasSelection> {
    val base = items.distinct()
    val contained = base.filter { it.type == "shape" && shapes.getOrNull(it.index)?.let(FrameSemantics::isFrame) == true }
        .flatMap { frameMembers(it.index) }
    return (base + contained).distinct()
}
