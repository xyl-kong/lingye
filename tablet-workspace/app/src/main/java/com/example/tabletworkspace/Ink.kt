package com.example.tabletworkspace

import kotlin.math.*

fun inkAcceptsPointer(penOnly: Boolean, kind: String): Boolean = when (kind) {
    "stylus", "eraser" -> true
    "touch", "mouse" -> !penOnly
    else -> false
}

/** Removes whole ink strokes intersecting an eraser sample; other canvas objects stay intact. */
fun Page.eraseAt(point: Point, radius: Float): Boolean {
    var removed = false
    for (i in strokes.indices.reversed()) {
        val stroke = strokes[i]; val threshold = radius + strokeStyles[i].width / 2f
        val hit = stroke.any { hypot(point.x - it.x, point.y - it.y) <= threshold } || stroke.zipWithNext().any { (a, b) ->
            val dx = b.x-a.x; val dy = b.y-a.y; val length = dx*dx+dy*dy
            val t = if (length == 0f) 0f else (((point.x-a.x)*dx+(point.y-a.y)*dy)/length).coerceIn(0f, 1f)
            hypot(point.x-(a.x+t*dx), point.y-(a.y+t*dy)) <= threshold
        }
        if (hit) { strokes.removeAt(i); strokeStyles.removeAt(i); strokeIds.removeAt(i); removed = true }
    }
    return removed
}

fun Page.eraseBetween(a: Point, b: Point, radius: Float): Boolean {
    val distance = hypot(b.x-a.x, b.y-a.y); val steps = max(1, ceil(distance / max(radius, 1f)).toInt())
    var removed = false
    for (i in 0..steps) { val t = i.toFloat()/steps; if (eraseAt(Point(a.x+(b.x-a.x)*t, a.y+(b.y-a.y)*t), radius)) removed = true }
    return removed
}
