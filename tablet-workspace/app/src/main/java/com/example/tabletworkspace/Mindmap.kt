package com.example.tabletworkspace

import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin

/**
 * Node-level mind map built from existing serialized fields so the page format stays at 22:
 * nodes are `mindNode` shapes whose `cardKind` is `mindmap:<layout>:<mapId>`, edges are bound
 * connectors whose id starts with `mm-`. `group` is left untouched for ordinary grouping.
 */
object Mindmap {
    const val NODE_TYPE = "mindNode"
    const val EDGE_PREFIX = "mm-"
    const val KIND_PREFIX = "mindmap:"
    const val NODE_W = 170f
    const val NODE_H = 64f
    const val DEFAULT_LAYOUT = "right"
    val LAYOUTS = listOf("bubble", "right", "down", "tree", "outline")

    val EDGE_COLOR = 0xff9aa4b2.toInt()
    const val CHILD_COLOR = 0xfff2f5ff.toInt()

    fun isNode(shape: Shape): Boolean = shape.type == NODE_TYPE
    fun isEdge(connector: Connector): Boolean = connector.id.startsWith(EDGE_PREFIX)
    private fun newEdgeId(): String = EDGE_PREFIX + UUID.randomUUID()

    fun kindOf(layout: String, mapId: String): String = "$KIND_PREFIX$layout:$mapId"

    /** Legacy nodes written before map ids existed only carry `mindmap:<layout>`. */
    fun parseKind(shape: Shape): Pair<String, String?>? {
        if (!shape.cardKind.startsWith(KIND_PREFIX)) return null
        val rest = shape.cardKind.removePrefix(KIND_PREFIX)
        val layout = rest.substringBefore(':')
        if (layout !in LAYOUTS) return null
        val mapId = rest.substringAfter(':', "").ifBlank { null }
        return layout to mapId
    }

    fun mapIdOf(shape: Shape): String? = parseKind(shape)?.second

    fun nodes(page: Page, mapId: String): List<Shape> =
        page.shapes.filter { it.type == NODE_TYPE && mapIdOf(it) == mapId }

    fun parent(page: Page, node: Shape): Shape? {
        val edge = page.connectors.lastOrNull { isEdge(it) && it.endShape == node.id } ?: return null
        return page.shapes.firstOrNull { it.id == edge.startShape }
    }

    fun children(page: Page, node: Shape): List<Shape> {
        val ids = page.connectors.filter { isEdge(it) && it.startShape == node.id }.map { it.endShape }
        return page.shapes.filter { it.id in ids }
    }

    fun descendants(page: Page, node: Shape): List<Shape> {
        val result = mutableListOf<Shape>()
        val seen = mutableSetOf(node.id)
        val queue = ArrayDeque(children(page, node))
        while (queue.isNotEmpty()) {
            val next = queue.removeFirst()
            if (!seen.add(next.id)) continue
            result += next
            queue.addAll(children(page, next))
        }
        return result
    }

    fun root(page: Page, node: Shape): Shape {
        var current = node
        val seen = mutableSetOf(node.id)
        while (true) {
            val parent = parent(page, current) ?: return current
            if (!seen.add(parent.id)) return current
            current = parent
        }
    }

    fun layoutOf(node: Shape, fallback: String = DEFAULT_LAYOUT): String =
        parseKind(node)?.first ?: fallback

    /** Re-runs the layout for the node's own map; safe to call with any node. */
    fun reflow(page: Page, node: Shape, layout: String = layoutOf(node)): Int =
        mapIdOf(node)?.let { layout(page, it, layout) } ?: 0

    fun createRoot(page: Page, x: Float, y: Float, text: String, color: Int, layout: String = DEFAULT_LAYOUT): Shape {
        require(layout in LAYOUTS) { "不支持的思维导图布局" }
        val root = Shape(
            NODE_TYPE, x, y, NODE_W, NODE_H, color,
            title = text,
            borderColor = 0xffdfe3ec.toInt(),
            borderStyle = "solid",
            borderWidth = 1f,
            cornerRadius = 14f,
            cardKind = kindOf(layout, UUID.randomUUID().toString())
        )
        page.shapes.add(root)
        return root
    }

    fun addNode(page: Page, parent: Shape, text: String, color: Int = CHILD_COLOR): Shape {
        require(isNode(parent)) { "只能在思维导图节点下添加" }
        require(children(page, parent).size < 100) { "单个节点最多 100 个子节点" }
        val kind = parseKind(parent)
        val node = Shape(
            NODE_TYPE, parent.x + 230f, parent.y + 80f, NODE_W, NODE_H, color,
            title = text,
            borderColor = 0xffdfe3ec.toInt(),
            borderStyle = "solid",
            borderWidth = 1f,
            cornerRadius = 14f,
            cardKind = kind?.let { kindOf(it.first, it.second ?: UUID.randomUUID().toString()) } ?: kindOf(DEFAULT_LAYOUT, UUID.randomUUID().toString())
        )
        page.shapes.add(node)
        bindEdge(page, parent, node)
        return node
    }

    /** New sibling keeps the same parent; the root gains a child instead. */
    fun addSibling(page: Page, node: Shape, text: String, color: Int = CHILD_COLOR): Shape =
        addNode(page, parent(page, node) ?: node, text, color)

    fun reparent(page: Page, node: Shape, newParent: Shape): Boolean {
        if (!isNode(node) || !isNode(newParent)) return false
        if (node.id == newParent.id) return false
        if (mapIdOf(node) == null || mapIdOf(node) != mapIdOf(newParent)) return false
        if (descendants(page, node).any { it.id == newParent.id }) return false
        val old = parent(page, node)
        if (old?.id == newParent.id) return false
        page.connectors.removeAll { isEdge(it) && it.endShape == node.id }
        bindEdge(page, newParent, node)
        return true
    }

    /** Deletes the node and its whole subtree (edges included); returns the number of nodes removed. */
    fun deleteSubtree(page: Page, node: Shape): Int {
        val ids = (descendants(page, node) + node).map { it.id }.toSet()
        page.connectors.removeAll { isEdge(it) && (it.startShape in ids || it.endShape in ids) }
        val before = page.shapes.size
        page.shapes.removeAll { it.id in ids }
        page.layerIds.removeAll { it in ids }
        return before - page.shapes.size
    }

    private fun bindEdge(page: Page, parent: Shape, child: Shape) {
        val cx = { s: Shape -> s.x + s.width / 2f }
        val cy = { s: Shape -> s.y + s.height / 2f }
        page.connectors.add(
            Connector(
                cx(parent), cy(parent), cx(child), cy(child),
                color = EDGE_COLOR, width = 1.6f,
                startShape = parent.id, endShape = child.id,
                pathStyle = "curve", curved = true,
                id = newEdgeId(),
                startMarker = "none", endMarker = "none"
            )
        )
    }

    /** Recalculates node positions for the given layout; edges follow because they are bound. Returns moved nodes. */
    fun layout(page: Page, mapId: String, layout: String): Int {
        if (layout !in LAYOUTS) return 0
        val nodes = nodes(page, mapId)
        if (nodes.isEmpty()) return 0
        val root = nodes.firstOrNull { parent(page, it) == null } ?: nodes.first()
        val positions = mutableMapOf<String, Pair<Float, Float>>() // center x/y
        val rootCx = root.x + root.width / 2f
        val rootCy = root.y + root.height / 2f

        when (layout) {
            "right", "down", "tree" -> {
                val centers = mutableMapOf<String, Float>()
                val depthOf = mutableMapOf<String, Int>()
                var cursor = 0f
                val vGap = if (layout == "tree") 120f else 100f
                val depthGap = when (layout) { "right" -> 240f; "down" -> 120f; else -> 150f }
                fun place(node: Shape, depth: Int): Float {
                    depthOf[node.id] = depth
                    val kids = children(page, node)
                    val axial = if (kids.isEmpty()) {
                        cursor += vGap
                        cursor - vGap / 2f
                    } else {
                        val ys = kids.map { place(it, depth + 1) }
                        (ys.first() + ys.last()) / 2f
                    }
                    centers[node.id] = axial
                    return axial
                }
                place(root, 0)
                val base = centers[root.id] ?: 0f
                centers.forEach { (id, axial) ->
                    val depth = depthOf[id] ?: 0
                    val offset = axial - base
                    positions[id] = if (layout == "right") {
                        (rootCx + depth * depthGap) to (rootCy + offset)
                    } else {
                        (rootCx + offset) to (rootCy + depth * depthGap)
                    }
                }
            }
            "outline" -> {
                var index = 0
                fun walk(node: Shape, depth: Int) {
                    positions[node.id] = (rootCx + depth * 48f) to (rootCy + index * 82f)
                    index++
                    children(page, node).forEach { walk(it, depth + 1) }
                }
                walk(root, 0)
            }
            else -> {
                val kids = children(page, root)
                if (kids.isEmpty()) {
                    positions[root.id] = rootCx to rootCy
                } else {
                    val radius = 300f
                    kids.forEachIndexed { i, kid ->
                        val angle = -Math.PI / 2 + 2 * Math.PI * i / kids.size
                        positions[kid.id] = (rootCx + radius * cos(angle)).toFloat() to (rootCy + radius * sin(angle)).toFloat()
                        fun fan(node: Shape, baseAngle: Double, depth: Int) {
                            val childList = children(page, node)
                            childList.forEachIndexed { j, child ->
                                val spread = (j - (childList.size - 1) / 2f) * 0.34
                                val angle2 = baseAngle + spread
                                val r = radius + depth * 170f
                                positions[child.id] = (rootCx + r * cos(angle2)).toFloat() to (rootCy + r * sin(angle2)).toFloat()
                                fan(child, angle2, depth + 1)
                            }
                        }
                        fan(kid, angle, 1)
                    }
                }
            }
        }

        var moved = 0
        page.shapes.forEachIndexed { index, shape ->
            val center = positions[shape.id] ?: return@forEachIndexed
            val x = center.first - shape.width / 2f
            val y = center.second - shape.height / 2f
            if (shape.x != x || shape.y != y) {
                page.shapes[index] = shape.copy(x = x, y = y)
                moved++
            }
        }
        // Remember the chosen layout on every node so it survives reopen.
        page.shapes.forEachIndexed { index, shape ->
            if (shape.type == NODE_TYPE && mapIdOf(shape) == mapId && layoutOf(shape) != layout) {
                page.shapes[index] = shape.copy(cardKind = kindOf(layout, mapId))
            }
        }
        return moved
    }
}
