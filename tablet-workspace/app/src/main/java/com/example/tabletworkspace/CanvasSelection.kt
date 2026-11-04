package com.example.tabletworkspace
import com.example.tabletworkspace.core.ConnectorSampleCache

import kotlin.math.*

data class CanvasSelection(val type: String, val index: Int)
fun Page.hitTest(point: Point, tolerance: Float): CanvasSelection? {
    var connectorHit: CanvasSelection? = null
    // Built once per hit instead of scanning every list for each layer id (formerly O(n²)).
    val indexById = HashMap<String, CanvasSelection>(strokeIds.size + shapes.size + images.size + connectors.size)
    strokeIds.forEachIndexed { i, id -> indexById[id] = CanvasSelection("stroke", i) }
    shapes.forEachIndexed { i, shape -> indexById[shape.id] = CanvasSelection("shape", i) }
    images.forEachIndexed { i, image -> indexById[image.id] = CanvasSelection("image", i) }
    connectors.forEachIndexed { i, connector -> indexById[connector.id] = CanvasSelection("connector", i) }
    val shapesById by lazy { shapes.associateBy { it.id } }
    for(id in orderedLayerIds().asReversed()) { val item=indexById[id] ?: continue
      when(item.type) {
       "stroke" -> { val pts=strokes[item.index];val radius=tolerance+strokeStyles[item.index].width/2; if(pts.any { hypot(point.x-it.x,point.y-it.y)<=radius } || pts.zipWithNext().any { (a,b)->val dx=b.x-a.x;val dy=b.y-a.y;val length=dx*dx+dy*dy;val t=if(length==0f) 0f else (((point.x-a.x)*dx+(point.y-a.y)*dy)/length).coerceIn(0f,1f);hypot(point.x-a.x-t*dx,point.y-a.y-t*dy)<=radius }) return item }
       "image" -> {val a=images[item.index];if(point.x>=a.x-tolerance && point.x<=a.x+a.width+tolerance && point.y>=a.y-tolerance && point.y<=a.y+a.height+tolerance) return item}
       "shape" -> {val a=shapes[item.index];if(a.type=="frame") { if(FrameSemantics.hit(a,point,tolerance)) return item } else if(point.x>=a.x-tolerance && point.x<=a.x+a.width+tolerance && point.y>=a.y-tolerance && point.y<=a.y+a.height+tolerance) return item}
       "connector" -> {
           if (connectorHit == null) {
               val samples=ConnectorSampleCache.shared.samples(resolvedConnector(item.index, shapesById))
               if(samples.zipWithNext().any { (a,b)->val dx=b.x-a.x;val dy=b.y-a.y;val length=dx*dx+dy*dy;val t=if(length==0f) 0f else (((point.x-a.x)*dx+(point.y-a.y)*dy)/length).coerceIn(0f,1f);hypot(point.x-a.x-t*dx,point.y-a.y-t*dy)<=tolerance }) connectorHit=item
           }
       }
      }
    }
    return connectorHit
}
fun Page.moveSelection(selection: CanvasSelection, dx: Float, dy: Float) = moveSelections(listOf(selection), dx, dy)
fun Page.deleteSelection(selection: CanvasSelection) {
    if (selection.type == "shape" && shapes.getOrNull(selection.index)?.locked == true) return
    if(selection.type=="stroke" && selection.index in strokes.indices) { strokes.removeAt(selection.index); strokeStyles.removeAt(selection.index); strokeIds.removeAt(selection.index); return }
    when (selection.type) { "image" -> if (selection.index in images.indices) images.removeAt(selection.index); "shape" -> if (selection.index in shapes.indices) { val id = shapes[selection.index].id; for (i in connectors.indices) { val line = connectors[i]; val resolved = resolvedConnector(i); connectors[i] = resolved.copy(startShape = if (line.startShape == id) "" else line.startShape, endShape = if (line.endShape == id) "" else line.endShape) }; shapes.removeAt(selection.index) }; "connector" -> if (selection.index in connectors.indices) connectors.removeAt(selection.index) }
}
fun Page.resizeSelection(selection: CanvasSelection, factor: Float) {
    when (selection.type) {
        "shape" -> shapes.getOrNull(selection.index)?.let { shapes[selection.index] = it.copy(width=(it.width*factor).coerceIn(12f, 2000f),height=(it.height*factor).coerceIn(12f, 2000f)) }
        "image" -> images.getOrNull(selection.index)?.let { images[selection.index] = it.copy(width=(it.width*factor).coerceIn(12f, 2000f),height=(it.height*factor).coerceIn(12f, 2000f)) }
        "connector" -> connectors.getOrNull(selection.index)?.let { connectors[selection.index] = it.copy(x2=it.x1+(it.x2-it.x1)*factor,y2=it.y1+(it.y2-it.y1)*factor) }
    }
}
fun Page.colorSelection(selection: CanvasSelection, color: Int) {
    when (selection.type) {
        "shape" -> shapes.getOrNull(selection.index)?.let { shapes[selection.index] = it.copy(color=color) }
        "connector" -> connectors.getOrNull(selection.index)?.let { connectors[selection.index] = it.copy(color=color) }
    }
}


fun Page.findStableObject(id: String): CanvasSelection? {
 val stroke=strokeIds.indexOf(id); if(stroke>=0) return CanvasSelection("stroke",stroke)
 val s=shapes.indexOfFirst { it.id==id }; if(s>=0) return CanvasSelection("shape",s)
 val i=images.indexOfFirst { it.id==id }; if(i>=0) return CanvasSelection("image",i)
 val c=connectors.indexOfFirst { it.id==id }; if(c>=0) return CanvasSelection("connector",c)
 return null
}

fun Page.stableIdOf(selection: CanvasSelection): String? = when (selection.type) {
    "shape" -> shapes.getOrNull(selection.index)?.id
    "image" -> images.getOrNull(selection.index)?.id
    "connector" -> connectors.getOrNull(selection.index)?.id
    "stroke" -> strokeIds.getOrNull(selection.index)
    else -> null
}

fun Page.selectionStableIds(items: Collection<CanvasSelection>): List<String> =
    items.mapNotNull { stableIdOf(it) }.distinct()

fun Page.restoreSelections(ids: Collection<String>): List<CanvasSelection> =
    ids.distinct().mapNotNull { findStableObject(it) }
