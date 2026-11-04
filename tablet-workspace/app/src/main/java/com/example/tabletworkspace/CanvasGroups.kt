package com.example.tabletworkspace

import java.util.UUID

fun Page.addBoundConnector(start: Int, end: Int) {
    require(start in shapes.indices && end in shapes.indices && start != end)
    val a = shapes[start]; val b = shapes[end]
    connectors.add(Connector(a.x+a.width/2, a.y+a.height/2, b.x+b.width/2, b.y+b.height/2, startShape=a.id, endShape=b.id))
}
fun Page.resolvedConnector(index: Int): Connector = resolvedConnector(index, shapes.associateBy { it.id })

/** Same as [resolvedConnector] but reuses a per-frame id index instead of scanning shapes per connector. */
internal fun Page.resolvedConnector(index: Int, shapesById: Map<String, Shape>): Connector {
    val line = connectors[index]
    val a = shapesById[line.startShape]; val b = shapesById[line.endShape]
    val start = a?.let { shapePort(it, line.startPort) } ?: Point(line.x1,line.y1); val end = b?.let { shapePort(it, line.endPort) } ?: Point(line.x2,line.y2)
    return line.copy(x1=start.x,y1=start.y,x2=end.x,y2=end.y)
}
fun Page.groupSelections(items: List<CanvasSelection>) {
    val unique = items.flatMap { selectionMembers(it) }.distinct().filter { (it.type == "shape" && it.index in shapes.indices && !shapes[it.index].locked) || (it.type == "image" && it.index in images.indices) }
    if (unique.size < 2) return
    val group = UUID.randomUUID().toString()
    unique.forEach { when (it.type) { "shape" -> shapes[it.index] = shapes[it.index].copy(group=group); "image" -> images[it.index] = images[it.index].copy(group=group) } }
}
fun Page.ungroupSelections(items: List<CanvasSelection>) {
    val requested = items.mapNotNull { when (it.type) { "shape" -> shapes.getOrNull(it.index)?.group; "image" -> images.getOrNull(it.index)?.group; else -> null } }.filter { it.isNotEmpty() }.toSet()
    val lockedGroups = shapes.filter { it.locked && it.group in requested }.map { it.group }.toSet()
    val groups = requested - lockedGroups
    for (i in shapes.indices) if (shapes[i].group in groups) shapes[i] = shapes[i].copy(group="")
    for (i in images.indices) if (images[i].group in groups) images[i] = images[i].copy(group="")
}
fun Page.selectionMembers(item: CanvasSelection): List<CanvasSelection> {
    val group = when (item.type) { "shape" -> shapes.getOrNull(item.index)?.group; "image" -> images.getOrNull(item.index)?.group; else -> null }
    if (group.isNullOrEmpty()) return listOf(item)
    return shapes.indices.filter { shapes[it].group == group }.map { CanvasSelection("shape",it) } + images.indices.filter { images[it].group == group }.map { CanvasSelection("image",it) }
}
private data class FreeEdgeAttachment(val connector: Int, val start: Boolean)

fun Page.moveSelections(items: Collection<CanvasSelection>, dx: Float, dy: Float) {
    val expanded = withFrameMembers(items.flatMap { selectionMembers(it) }).distinct()
    val movedShapes = expanded.filter { it.type == "shape" && shapes.getOrNull(it.index)?.locked != true }.map { it.index }
    val movedConnectors = expanded.filter { it.type == "connector" }.map { it.index }.toSet()
    val movedWholeConnectors = movedConnectors.filter {
        connectors.getOrNull(it)?.let { line -> line.startShape.isEmpty() && line.endShape.isEmpty() } == true
    }.toSet()
    // Free endpoints that touch a moving shape's boundary follow that shape (page format 25 has no edge-position field).
    val attachments = mutableListOf<FreeEdgeAttachment>()
    if (dx != 0f || dy != 0f) {
        movedShapes.forEach { shapeIndex ->
            val shape = shapes.getOrNull(shapeIndex) ?: return@forEach
            connectors.forEachIndexed { connectorIndex, line ->
                if (connectorIndex in movedWholeConnectors) return@forEachIndexed
                if (line.startShape.isEmpty() && ConnectorSnapping.isPointOnRectEdge(
                        Point(line.x1, line.y1), shape.x, shape.y, shape.x + shape.width, shape.y + shape.height, 2f
                    )) attachments += FreeEdgeAttachment(connectorIndex, true)
                if (line.endShape.isEmpty() && ConnectorSnapping.isPointOnRectEdge(
                        Point(line.x2, line.y2), shape.x, shape.y, shape.x + shape.width, shape.y + shape.height, 2f
                    )) attachments += FreeEdgeAttachment(connectorIndex, false)
            }
        }
    }
    expanded.forEach { item ->
        when (item.type) {
            "stroke" -> strokes.getOrNull(item.index)?.let { stroke -> strokes[item.index]=stroke.map { Point(it.x+dx,it.y+dy) } }
            "shape" -> shapes.getOrNull(item.index)?.let { if (!it.locked) shapes[item.index] = it.copy(x=it.x+dx,y=it.y+dy) }
            "image" -> images.getOrNull(item.index)?.let { images[item.index] = it.copy(x=it.x+dx,y=it.y+dy) }
            "connector" -> connectors.getOrNull(item.index)?.let { line -> if (line.startShape.isEmpty() && line.endShape.isEmpty()) connectors[item.index] = line.copy(x1=line.x1+dx,y1=line.y1+dy,x2=line.x2+dx,y2=line.y2+dy) }
        }
    }
    attachments.distinct().forEach { attachment ->
        val line = connectors.getOrNull(attachment.connector) ?: return@forEach
        connectors[attachment.connector] = if (attachment.start) {
            line.copy(x1 = line.x1 + dx, y1 = line.y1 + dy)
        } else {
            line.copy(x2 = line.x2 + dx, y2 = line.y2 + dy)
        }
    }
}


fun shapePort(shape: Shape, port: String): Point = when(port) {
 "left" -> Point(shape.x,shape.y+shape.height/2); "right" -> Point(shape.x+shape.width,shape.y+shape.height/2)
 "top" -> Point(shape.x+shape.width/2,shape.y); "bottom" -> Point(shape.x+shape.width/2,shape.y+shape.height)
 else -> Point(shape.x+shape.width/2,shape.y+shape.height/2)
}
fun Page.bindConnectorEnd(index: Int, start: Boolean, shapeIndex: Int, port: String) {
 require(index in connectors.indices && shapeIndex in shapes.indices && port in setOf("center","left","right","top","bottom"))
 val l=connectors[index]; val id=shapes[shapeIndex].id
 connectors[index]=if(start) l.copy(startShape=id,startPort=port) else l.copy(endShape=id,endPort=port)
}
fun Page.detachConnectorEnd(index: Int, start: Boolean, at: Point) {
 val l=resolvedConnector(index)
 connectors[index]=if(start) l.copy(startShape="",startPort="center",x1=at.x,y1=at.y) else l.copy(endShape="",endPort="center",x2=at.x,y2=at.y)
}
fun Page.nearestShapePort(p: Point, radius: Float): Pair<Int,String>? {
 var result: Pair<Int,String>?=null; var best=radius
 shapes.forEachIndexed { i,s -> listOf("left","right","top","bottom").forEach { port -> val q=shapePort(s,port); val d=kotlin.math.hypot(p.x-q.x,p.y-q.y); if(d<=best) { best=d; result=i to port } } }; return result
}
