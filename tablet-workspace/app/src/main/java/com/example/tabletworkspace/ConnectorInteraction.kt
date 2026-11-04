package com.example.tabletworkspace

import kotlin.math.hypot

/** Live state while dragging a new connector with the connector tool. */
data class ConnectorDraft(
    val start: Point,
    val end: Point,
    val sourceShape: Int?,
    val targetShape: Int?,
    val sourcePort: String,
    val targetPort: String,
    val snapKind: ConnectorSnapKind = ConnectorSnapKind.NONE,
    val snapEdge: String = ""
)

/** Topmost shape under [point], ignoring strokes and connectors (frames count when their band is hit). */
fun Page.shapeIndexAt(point: Point, tolerance: Float): Int? {
    for (id in orderedLayerIds().asReversed()) {
        val item = findStableObject(id) ?: continue
        if (item.type != "shape") continue
        val shape = shapes[item.index]
        if (shape.type == "frame") {
            if (FrameSemantics.hit(shape, point, tolerance)) return item.index
            continue
        }
        if (point.x >= shape.x - tolerance && point.x <= shape.x + shape.width + tolerance &&
            point.y >= shape.y - tolerance && point.y <= shape.y + shape.height + tolerance
        ) return item.index
    }
    return null
}

/** Nearest of the five supported anchor points on [shapeIndex] to [point]. */
fun Page.nearestPortOnShape(shapeIndex: Int, point: Point): Pair<String, Point> {
    val shape = shapes.getOrElse(shapeIndex) { return "center" to point }
    val ports = listOf("left", "right", "top", "bottom", "center")
    val best = ports.minBy { port ->
        val anchor = shapePort(shape, port)
        hypot(point.x - anchor.x, point.y - anchor.y)
    }
    return best to shapePort(shape, best)
}
