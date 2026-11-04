package com.example.tabletworkspace

import java.util.Base64
import java.util.UUID

fun CardTemplate.canvasPage(): Page? = if(canvasData.isEmpty()) null else Page.decode(Base64.getDecoder().decode(canvasData))
fun CardTemplate.remapTemplateImages(mapping:Map<String,String>):CardTemplate {
 val page=canvasPage() ?: return this
 page.images.replaceAll { it.copy(name=mapping[it.name] ?: error("备份缺少模板图片")) }
 return copy(canvasData=Base64.getEncoder().encodeToString(page.encode()))
}
fun Page.makeCanvasTemplate(name:String, items:Collection<CanvasSelection>):CardTemplate {
 require(name.isNotBlank() && name.trim().length<=100)
 val members=items.flatMap { selectionMembers(it) }.distinct(); val bounds=selectionBounds(members) ?: error("请选择图形、图片或笔迹")
 val fragment=Page.create("template");val layerMap=mutableMapOf<String,String>();val shapeIds=members.filter { it.type=="shape" }.mapNotNull { shapes.getOrNull(it.index)?.id }.toSet()
 members.forEach { item -> when(item.type) {
  "shape" -> shapes.getOrNull(item.index)?.let { fragment.shapes.add(it.copy(x=it.x-bounds.left,y=it.y-bounds.top));layerMap[it.id]=it.id }
  "image" -> images.getOrNull(item.index)?.let { fragment.images.add(it.copy(x=it.x-bounds.left,y=it.y-bounds.top));layerMap[it.id]=it.id }
  "stroke" -> strokes.getOrNull(item.index)?.let { pts -> fragment.addStyledStroke(pts.map { Point(it.x-bounds.left,it.y-bounds.top) },strokeStyles[item.index].color,strokeStyles[item.index].width);layerMap[strokeIds[item.index]]=fragment.strokeIds.last() }
 } }
 connectors.forEachIndexed { index,line -> if((line.startShape in shapeIds && line.endShape in shapeIds && line.startShape.isNotEmpty() && line.endShape.isNotEmpty()) || CanvasSelection("connector",index) in members) { val r=resolvedConnector(index);fragment.connectors.add(r.copy(x1=r.x1-bounds.left,y1=r.y1-bounds.top,x2=r.x2-bounds.left,y2=r.y2-bounds.top,startShape=line.startShape.takeIf { it in shapeIds } ?: "",endShape=line.endShape.takeIf { it in shapeIds } ?: ""));layerMap[line.id]=line.id } }
 fragment.layerIds.addAll(orderedLayerIds().mapNotNull { layerMap[it] })
 return CardTemplate(name.trim(),"","",0,canvasData=Base64.getEncoder().encodeToString(fragment.encode()))
}
fun Page.insertWorkspaceTemplate(t:CardTemplate,x:Float,y:Float) {
 val source=t.canvasPage() ?: run { shapes.add(Shape("card",x,y,t.width,t.height,t.color,title=t.title,body=t.body));return }
 orderedLayerIds();val layerMap=mutableMapOf<String,String>()
 val ids=source.shapes.associate { it.id to UUID.randomUUID().toString() };val groups=(source.shapes.map { it.group }+source.images.map { it.group }).filter { it.isNotEmpty() }.distinct().associateWith { UUID.randomUUID().toString() }
 source.shapes.forEach { shapes.add(it.copy(id=ids.getValue(it.id),group=groups[it.group] ?: "",x=x+it.x,y=y+it.y));layerMap[it.id]=ids.getValue(it.id) }
 source.images.forEach { val id=UUID.randomUUID().toString();images.add(it.copy(id=id,group=groups[it.group] ?: "",x=x+it.x,y=y+it.y));layerMap[it.id]=id }
 source.connectors.forEach { val id=UUID.randomUUID().toString();connectors.add(it.copy(id=id,x1=x+it.x1,y1=y+it.y1,x2=x+it.x2,y2=y+it.y2,startShape=ids[it.startShape] ?: "",endShape=ids[it.endShape] ?: ""));layerMap[it.id]=id }
 source.strokes.forEachIndexed { i,pts -> addStyledStroke(pts.map { Point(x+it.x,y+it.y) },source.strokeStyles[i].color,source.strokeStyles[i].width);layerMap[source.strokeIds[i]]=strokeIds.last() }
 layerIds.addAll(source.orderedLayerIds().mapNotNull { layerMap[it] })
}
