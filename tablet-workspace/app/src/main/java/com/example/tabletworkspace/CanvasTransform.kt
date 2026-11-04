package com.example.tabletworkspace
import kotlin.math.*
import java.util.UUID
enum class LayerMove { TO_FRONT, FORWARD, BACKWARD, TO_BACK }
data class CanvasBounds(val left: Float, val top: Float, val right: Float, val bottom: Float)
fun Page.alignLeft(items: Collection<CanvasSelection>) {
 val members=items.flatMap { selectionMembers(it) }.distinct()
 val left=selectionBounds(members)?.left ?: return
 members.forEach { s -> when(s.type) {
  "shape" -> shapes.getOrNull(s.index)?.let { shapes[s.index]=it.copy(x=left) }
  "image" -> images.getOrNull(s.index)?.let { images[s.index]=it.copy(x=left) }
 } }
}
fun Page.snapSelectionsToGrid(items: Collection<CanvasSelection>, spacing: Float) {
 require(spacing.isFinite() && spacing>0f)
 val b=selectionBounds(items) ?: return
 val x=round(b.left/spacing)*spacing; val y=round(b.top/spacing)*spacing
 moveSelections(items,x-b.left,y-b.top)
}
fun Page.alignSelections(items: Collection<CanvasSelection>, edge: String) {
 require(edge in setOf("left","right","top","bottom"))
 val members=items.distinct().filter { (it.type=="shape" && shapes.getOrNull(it.index)?.locked != true) || it.type=="image" }
 val bounds=selectionBounds(members) ?: return
 members.forEach { item -> val b=selectionBounds(listOf(item)) ?: return@forEach
  val dx=when(edge) { "left"->bounds.left-b.left; "right"->bounds.right-b.right; else->0f }
  val dy=when(edge) { "top"->bounds.top-b.top; "bottom"->bounds.bottom-b.bottom; else->0f }
  moveSelections(listOf(item),dx,dy)
 }
}
fun gridPhase(offset: Float, spacing: Float): Float {
 require(spacing.isFinite() && spacing > 0f) { "Grid spacing must be positive" }
 return ((offset % spacing) + spacing) % spacing
}
fun Page.selectionBounds(items: Collection<CanvasSelection>): CanvasBounds? {
 val bounds=items.flatMap { selectionMembers(it) }.distinct().mapNotNull { s -> when(s.type) {
  "shape" -> shapes.getOrNull(s.index)?.let { CanvasBounds(it.x,it.y,it.x+it.width,it.y+it.height) }
  "image" -> images.getOrNull(s.index)?.let { CanvasBounds(it.x,it.y,it.x+it.width,it.y+it.height) }
  "stroke" -> strokes.getOrNull(s.index)?.takeIf { it.isNotEmpty() }?.let { pts -> CanvasBounds(pts.minOf { it.x },pts.minOf { it.y },pts.maxOf { it.x },pts.maxOf { it.y }) }
  else -> null
 } }
 if(bounds.isEmpty()) return null
 return CanvasBounds(bounds.minOf{it.left},bounds.minOf{it.top},bounds.maxOf{it.right},bounds.maxOf{it.bottom})
}
fun Page.resizeSelections(items: Collection<CanvasSelection>, factor: Float) {
 val members=items.flatMap { selectionMembers(it) }.distinct(); val b=selectionBounds(members) ?: return
 resizeSelectionsAnchored(members,factor,Point(b.left,b.top))
}
fun Page.resizeSelectionsAnchored(items: Collection<CanvasSelection>, factor: Float, anchor: Point) {
 val members=items.flatMap { selectionMembers(it) }.distinct()
 if(!factor.isFinite() || factor <= 0f) return
 val dims=members.mapNotNull { s -> when(s.type) { "shape" -> shapes.getOrNull(s.index)?.let { it.width to it.height }; "image" -> images.getOrNull(s.index)?.let { it.width to it.height }; "stroke" -> selectionBounds(listOf(s))?.let { max(12f,it.right-it.left) to max(12f,it.bottom-it.top) }; else -> null } }
 if(dims.isEmpty()) return
 val f=factor.coerceIn(dims.maxOf { max(12f/it.first,12f/it.second) },dims.minOf { min(2000f/it.first,2000f/it.second) })
 members.forEach { s -> when(s.type) {
  "shape" -> shapes.getOrNull(s.index)?.let { if (!it.locked) shapes[s.index]=it.copy(x=anchor.x+(it.x-anchor.x)*f,y=anchor.y+(it.y-anchor.y)*f,width=it.width*f,height=it.height*f) }
  "image" -> images.getOrNull(s.index)?.let { images[s.index]=it.copy(x=anchor.x+(it.x-anchor.x)*f,y=anchor.y+(it.y-anchor.y)*f,width=it.width*f,height=it.height*f) }
  "stroke" -> strokes.getOrNull(s.index)?.let { pts -> strokes[s.index]=pts.map { Point(anchor.x+(it.x-anchor.x)*f,anchor.y+(it.y-anchor.y)*f) } }
 } }
}

/**
 * Geometry captured when a corner handle is grabbed. Free resize always replays from this
 * snapshot so horizontal and vertical factors stay independent and drags never accumulate error.
 */
data class FreeResizeSnapshot(
 val bounds: CanvasBounds,
 val anchor: Point,
 val shapes: List<Pair<Int, Shape>>,
 val images: List<Pair<Int, CanvasImage>>,
 val strokes: List<Pair<Int, List<Point>>>
)

fun Page.freeResizeSnapshot(items: Collection<CanvasSelection>, anchor: Point): FreeResizeSnapshot? {
 val members=items.flatMap { selectionMembers(it) }.distinct()
 val bounds=selectionBounds(members) ?: return null
 if(bounds.right-bounds.left<1f || bounds.bottom-bounds.top<1f) return null
 val shapeSnapshots=members.filter { it.type=="shape" }.mapNotNull { s -> shapes.getOrNull(s.index)?.let { s.index to it } }
 val imageSnapshots=members.filter { it.type=="image" }.mapNotNull { s -> images.getOrNull(s.index)?.let { s.index to it } }
 val strokeSnapshots=members.filter { it.type=="stroke" }.mapNotNull { s -> strokes.getOrNull(s.index)?.let { s.index to it.toList() } }
 if(shapeSnapshots.isEmpty() && imageSnapshots.isEmpty() && strokeSnapshots.isEmpty()) return null
 return FreeResizeSnapshot(bounds,anchor,shapeSnapshots,imageSnapshots,strokeSnapshots)
}

fun Page.applyFreeResize(snapshot: FreeResizeSnapshot, pointer: Point, axis: String? = null, minSize: Float = 24f) {
 val b=snapshot.bounds; val anchor=snapshot.anchor
 val draggedLeft=anchor.x>=b.right-0.01f
 val draggedTop=anchor.y>=b.bottom-0.01f
 val draggedX=if(draggedLeft) min(pointer.x,anchor.x-minSize) else max(pointer.x,anchor.x+minSize)
 val draggedY=if(draggedTop) min(pointer.y,anchor.y-minSize) else max(pointer.y,anchor.y+minSize)
 val oldW=max(1f,b.right-b.left); val oldH=max(1f,b.bottom-b.top)
 val fx=if(axis=="y") 1f else (abs(anchor.x-draggedX)/oldW).coerceIn(0.01f,100f)
 val fy=if(axis=="x") 1f else (abs(anchor.y-draggedY)/oldH).coerceIn(0.01f,100f)
 snapshot.shapes.forEach { (index,start) ->
  shapes.getOrNull(index)?.let { current ->
   if(!current.locked) shapes[index]=current.copy(
    x=anchor.x+(start.x-anchor.x)*fx,
    y=anchor.y+(start.y-anchor.y)*fy,
    width=max(1f,start.width*fx),
    height=max(1f,start.height*fy)
   )
  }
 }
 snapshot.images.forEach { (index,start) ->
  images.getOrNull(index)?.let { current ->
   images[index]=current.copy(
    x=anchor.x+(start.x-anchor.x)*fx,
    y=anchor.y+(start.y-anchor.y)*fy,
    width=max(1f,start.width*fx),
    height=max(1f,start.height*fy)
   )
  }
 }
 snapshot.strokes.forEach { (index,startPoints) ->
  strokes.getOrNull(index)?.let {
   strokes[index]=startPoints.map { p -> Point(anchor.x+(p.x-anchor.x)*fx,anchor.y+(p.y-anchor.y)*fy) }
  }
 }
}
fun Page.selectInBox(a: Point,b: Point): List<CanvasSelection> {
 val left=min(a.x,b.x); val right=max(a.x,b.x); val top=min(a.y,b.y); val bottom=max(a.y,b.y); val contained=b.x>=a.x
 fun hit(x:Float,y:Float,w:Float,h:Float)=if(contained) x>=left && y>=top && x+w<=right && y+h<=bottom else x<=right && x+w>=left && y<=bottom && y+h>=top
 fun inBox(p:Point)=p.x in left..right && p.y in top..bottom
 fun crossesBox(a:Point,b:Point):Boolean {
  var lo=0f; var hi=1f
  fun clip(p:Float,q:Float):Boolean {
   if(p==0f) return q>=0f
   val t=q/p
   if(p<0f) { if(t>hi) return false; lo=max(lo,t) } else { if(t<lo) return false; hi=min(hi,t) }
   return true
  }
  val dx=b.x-a.x; val dy=b.y-a.y
  return clip(-dx,a.x-left) && clip(dx,right-a.x) && clip(-dy,a.y-top) && clip(dy,bottom-a.y)
 }
 val shapeHits=shapes.indices.filter { shapes[it].let { s -> hit(s.x,s.y,s.width,s.height) } }.map { CanvasSelection("shape",it) }
 val imageHits=images.indices.filter { images[it].let { s -> hit(s.x,s.y,s.width,s.height) } }.map { CanvasSelection("image",it) }
 val connectorHits=connectors.indices.filter { i ->
  val samples=connectorSamples(resolvedConnector(i))
  if(contained) samples.all(::inBox) else samples.any(::inBox) || samples.zipWithNext().any { (a,b) -> crossesBox(a,b) }
 }.map { CanvasSelection("connector",it) }
 val strokeHits=strokes.indices.filter { i -> val pts=strokes[i]; pts.isNotEmpty() && if(contained) pts.all(::inBox) else pts.any(::inBox) || pts.zipWithNext().any { (a,b)->crossesBox(a,b) } }.map { CanvasSelection("stroke",it) }
 return (connectorHits + shapeHits + imageHits + strokeHits).flatMap { selectionMembers(it) }.distinct()
}
fun Page.duplicateSelections(items: Collection<CanvasSelection>, offset: Point = Point(24f,24f)): List<CanvasSelection> {
 val members=withFrameMembers(items.flatMap { selectionMembers(it) })
 val created=mutableListOf<CanvasSelection>()
 members.forEach { item ->
  when(item.type) {
   "shape" -> shapes.getOrNull(item.index)?.let { shape ->
    shapes.add(shape.copy(x=shape.x+offset.x,y=shape.y+offset.y,id=UUID.randomUUID().toString(),group="",locked=false))
    created.add(CanvasSelection("shape",shapes.lastIndex))
   }
   "image" -> images.getOrNull(item.index)?.let { image ->
    images.add(image.copy(x=image.x+offset.x,y=image.y+offset.y,id=UUID.randomUUID().toString(),group=""))
    created.add(CanvasSelection("image",images.lastIndex))
   }
  }
 }
 return created
}
fun Page.reorderSelection(selection: CanvasSelection, move: LayerMove): CanvasSelection {
 val id=when(selection.type) { "shape"->shapes.getOrNull(selection.index)?.id;"image"->images.getOrNull(selection.index)?.id;"connector"->connectors.getOrNull(selection.index)?.id;"stroke"->strokeIds.getOrNull(selection.index);else->null } ?: return selection
 val order=orderedLayerIds(); val from=order.indexOf(id); val to=when(move) { LayerMove.TO_FRONT->order.lastIndex;LayerMove.TO_BACK->0;LayerMove.FORWARD->(from+1).coerceAtMost(order.lastIndex);LayerMove.BACKWARD->(from-1).coerceAtLeast(0) }
 layerIds.add(to,layerIds.removeAt(from))
 if(selection.type=="shape" || selection.type=="image") reorderWithinType(selection,move)
 return findStableObject(id) ?: selection
}
private fun Page.reorderWithinType(selection: CanvasSelection, move: LayerMove): CanvasSelection = when(selection.type) {
 "shape" -> {
  require(selection.index in shapes.indices)
  val target=when(move) { LayerMove.TO_FRONT->shapes.lastIndex; LayerMove.FORWARD->(selection.index+1).coerceAtMost(shapes.lastIndex); LayerMove.BACKWARD->(selection.index-1).coerceAtLeast(0); LayerMove.TO_BACK->0 }
  if(target!=selection.index) shapes.add(target,shapes.removeAt(selection.index))
  CanvasSelection("shape",target)
 }
 "image" -> {
  require(selection.index in images.indices)
  val target=when(move) { LayerMove.TO_FRONT->images.lastIndex; LayerMove.FORWARD->(selection.index+1).coerceAtMost(images.lastIndex); LayerMove.BACKWARD->(selection.index-1).coerceAtLeast(0); LayerMove.TO_BACK->0 }
  if(target!=selection.index) images.add(target,images.removeAt(selection.index))
  CanvasSelection("image",target)
 }
 else -> selection
}
