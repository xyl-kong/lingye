package com.example.tabletworkspace
import org.junit.Assert.*
import org.junit.Test
class StableObjectTest {
 @Test fun connectorIdSurvivesRoundTripAndIndexChanges() {
  val p=Page.create("p"); p.addConnector(0f,0f,5f,5f); p.addConnector(10f,10f,20f,20f); val id=p.connectors[1].id
  p.deleteSelection(CanvasSelection("connector",0)); val saved=Page.decode(p.encode())
  assertEquals(id,saved.connectors[0].id); assertEquals(CanvasSelection("connector",0),saved.findStableObject(id))
 }
 @Test fun shapeAndImageIdsResolveAfterDeletion() {
  val p=Page.create("p");p.addShape("rect",0f,0f,20f,20f);p.addShape("rect",30f,0f,20f,20f);val id=p.shapes[1].id;p.deleteSelection(CanvasSelection("shape",0))
  assertEquals(CanvasSelection("shape",0),p.findStableObject(id))
 }
 @Test fun selectionIdsRestoreAcrossObjectReordering() {
  val p=Page.create("p");p.addShape("rect",0f,0f,20f,20f);p.addShape("ellipse",30f,0f,20f,20f);val ids=p.selectionStableIds(listOf(CanvasSelection("shape",0),CanvasSelection("shape",1)))
  p.shapes.add(0,p.shapes.removeAt(1))
  assertEquals(ids,p.selectionStableIds(p.restoreSelections(ids)))
  assertEquals(listOf("shape", "shape"),p.restoreSelections(ids).map { it.type })
 }
}
