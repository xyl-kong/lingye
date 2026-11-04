package com.example.tabletworkspace

import kotlin.math.hypot
import kotlin.math.min

enum class ConnectorSnapKind { NONE, EDGE, ANCHOR }

/** One preset anchor on a snap target, in world coordinates. */
data class ConnectorAnchor(val port: String, val point: Point)

/**
 * A rectangular canvas element that a connector endpoint can snap to.
 * [shapeIndex] is null for images, which cannot be bound by the current page format.
 */
data class ConnectorSnapRect(
    val key: Int,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val anchors: List<ConnectorAnchor> = emptyList(),
    val shapeIndex: Int? = null
)

data class ConnectorSnapResult(
    val point: Point,
    val kind: ConnectorSnapKind,
    val shapeIndex: Int? = null,
    val port: String = "center",
    val edge: String = "",
    val distance: Float = Float.MAX_VALUE
)

/**
 * Only the four edge midpoints are magnetic anchors. Any other point on the four
 * edges is a valid drop point: when the pointer is within [edgeThreshold] of a
 * boundary it attaches to that exact edge point without magnetic pull. Points away
 * from the boundary stay exactly where the pointer is.
 */
object ConnectorSnapping {
    fun anchorsOf(left: Float, top: Float, right: Float, bottom: Float): List<ConnectorAnchor> {
        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f
        return listOf(
            ConnectorAnchor("left", Point(left, cy)),
            ConnectorAnchor("right", Point(right, cy)),
            ConnectorAnchor("top", Point(cx, top)),
            ConnectorAnchor("bottom", Point(cx, bottom))
        )
    }

    /** Closest point on a rectangle boundary plus the edge it belongs to. */
    fun closestPointOnRectEdge(point: Point, left: Float, top: Float, right: Float, bottom: Float): Pair<Point, String> {
        val clampedX = point.x.coerceIn(left, right)
        val clampedY = point.y.coerceIn(top, bottom)
        if (point.x < left || point.x > right || point.y < top || point.y > bottom) {
            val edge = when {
                point.x < left -> "left"
                point.x > right -> "right"
                point.y < top -> "top"
                else -> "bottom"
            }
            return Point(clampedX, clampedY) to edge
        }
        val toLeft = point.x - left
        val toRight = right - point.x
        val toTop = point.y - top
        val toBottom = bottom - point.y
        val nearest = min(min(toLeft, toRight), min(toTop, toBottom))
        return when {
            nearest == toLeft -> Point(left, point.y) to "left"
            nearest == toRight -> Point(right, point.y) to "right"
            nearest == toTop -> Point(point.x, top) to "top"
            else -> Point(point.x, bottom) to "bottom"
        }
    }

    /** True when [point] lies on (or within [tolerance] of) the rectangle boundary. */
    fun isPointOnRectEdge(
        point: Point,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        tolerance: Float
    ): Boolean {
        if (point.x < left - tolerance || point.x > right + tolerance ||
            point.y < top - tolerance || point.y > bottom + tolerance) return false
        val distance = minOf(
            kotlin.math.abs(point.x - left),
            kotlin.math.abs(point.x - right),
            kotlin.math.abs(point.y - top),
            kotlin.math.abs(point.y - bottom)
        )
        return distance <= tolerance
    }

    fun snap(
        point: Point,
        rects: List<ConnectorSnapRect>,
        anchorThreshold: Float,
        edgeThreshold: Float
    ): ConnectorSnapResult {
        var bestAnchor: ConnectorSnapResult? = null
        var bestEdge: ConnectorSnapResult? = null
        rects.forEach { rect ->
            rect.anchors.forEach { anchor ->
                val distance = hypot(point.x - anchor.point.x, point.y - anchor.point.y)
                if (distance <= anchorThreshold && distance < (bestAnchor?.distance ?: Float.MAX_VALUE)) {
                    bestAnchor = ConnectorSnapResult(
                        point = anchor.point,
                        kind = ConnectorSnapKind.ANCHOR,
                        shapeIndex = rect.shapeIndex,
                        port = anchor.port,
                        distance = distance
                    )
                }
            }
            val (edgePoint, edge) = closestPointOnRectEdge(point, rect.left, rect.top, rect.right, rect.bottom)
            val distance = hypot(point.x - edgePoint.x, point.y - edgePoint.y)
            if (distance <= edgeThreshold && distance < (bestEdge?.distance ?: Float.MAX_VALUE)) {
                bestEdge = ConnectorSnapResult(
                    point = edgePoint,
                    kind = ConnectorSnapKind.EDGE,
                    shapeIndex = rect.shapeIndex,
                    port = edge,
                    edge = edge,
                    distance = distance
                )
            }
        }
        return bestAnchor ?: bestEdge ?: ConnectorSnapResult(point = point, kind = ConnectorSnapKind.NONE)
    }
}

/** Snap targets for the live connector drag: every shape plus every image, minus the source shape. */
fun Page.connectorSnapRects(excludeShape: Int?): List<ConnectorSnapRect> {
    val result = mutableListOf<ConnectorSnapRect>()
    shapes.forEachIndexed { index, shape ->
        if (index != excludeShape) {
            result += ConnectorSnapRect(
                key = index,
                left = shape.x,
                top = shape.y,
                right = shape.x + shape.width,
                bottom = shape.y + shape.height,
                anchors = ConnectorSnapping.anchorsOf(shape.x, shape.y, shape.x + shape.width, shape.y + shape.height),
                shapeIndex = index
            )
        }
    }
    images.forEachIndexed { index, image ->
        result += ConnectorSnapRect(
            key = -index - 1,
            left = image.x,
            top = image.y,
            right = image.x + image.width,
            bottom = image.y + image.height,
            shapeIndex = null
        )
    }
    return result
}
