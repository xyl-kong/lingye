package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageHistory
import org.junit.Assert.*
import org.junit.Test
class CanvasTransformTest {
 @Test fun imageRotationAndFlipSurviveRoundTrip() {
  val page=Page.create("image");page.addCanvasImage("a.img",10f,20f,120f,60f)
  page.rotateCanvasImage(0);page.flipCanvasImage(0,true)
  val saved=Page.decode(page.encode()).images.single()
  assertEquals(40f,saved.x,0.001f);assertEquals(-10f,saved.y,0.001f);assertEquals(60f,saved.width,0.001f);assertEquals(120f,saved.height,0.001f)
  assertEquals(1,saved.rotation);assertTrue(saved.flipX);assertFalse(saved.flipY)
 }
 @Test fun imageCropIsNonDestructiveAndResettable() {
  val page=Page.create("image");page.addCanvasImage("a.img",10f,20f,200f,100f)
  page.cropCanvasImage(0,0.25f,0f,0.75f,1f);var image=Page.decode(page.encode()).images.single()
  assertEquals("a.img",image.name);assertEquals(100f,image.width,0.001f);assertEquals(0.25f,image.cropLeft,0.001f)
  page.cropCanvasImage(0,0f,0f,1f,1f);image=page.images.single();assertEquals(200f,image.width,0.001f);assertEquals(1f,image.cropRight,0.001f)
 }
 @Test fun cropAfterRotationUsesOriginalImageAxesAndUndoRestores() {
  val page=Page.create("image");page.addCanvasImage("a.img",0f,0f,200f,100f);page.rotateCanvasImage(0);val original=page.encode();val history=PageHistory(page)
  page.cropCanvasImage(0,0.25f,0f,0.75f,1f);history.record(page);assertEquals(100f,page.images[0].width,0.001f);assertEquals(100f,page.images[0].height,0.001f)
  assertArrayEquals(original,history.undo()!!.encode());assertEquals(0.25f,history.redo()!!.images[0].cropLeft,0.001f)
 }
 @Test fun templateRetainsImageCropRotationAndFlip() {
  val page=Page.create("source");page.addCanvasImage("a.img",0f,0f,200f,100f);page.cropCanvasImage(0,0.1f,0.2f,0.9f,0.8f);page.rotateCanvasImage(0);page.flipCanvasImage(0,true)
  val template=page.makeCanvasTemplate("crop",listOf(CanvasSelection("image",0)));val restored=TemplateCatalog.decode(TemplateCatalog.encode(listOf(template))).single();val target=Page.create("target");target.insertWorkspaceTemplate(restored,50f,50f)
  val source=page.images.single();val image=target.images.single();assertNotEquals(source.id,image.id);assertEquals(source.rotation,image.rotation);assertEquals(source.flipX,image.flipX);assertEquals(source.cropTop,image.cropTop,0.001f);assertEquals(source.cropRight,image.cropRight,0.001f)
 }
 @Test fun complexTemplateRecreatesImagesAndBoundConnectionsIndependently() {
  val p=Page.create("source");p.addShape("rect",0f,0f,40f,40f);p.addShape("rect",100f,0f,40f,40f);p.addBoundConnector(0,1);p.addCanvasImage("a.img",30f,80f,50f,50f)
  val t=p.makeCanvasTemplate("flow",listOf(CanvasSelection("shape",0),CanvasSelection("shape",1),CanvasSelection("image",0)))
  val saved=TemplateCatalog.decode(TemplateCatalog.encode(listOf(t))).single();val dest=Page.create("dest");dest.insertWorkspaceTemplate(saved,200f,300f)
  assertEquals(2,dest.shapes.size);assertEquals("a.img",dest.images.single().name);assertEquals(1,dest.connectors.size)
  assertEquals(dest.shapes[0].id,dest.connectors[0].startShape);assertEquals(dest.shapes[1].id,dest.connectors[0].endShape);assertNotEquals(p.shapes[0].id,dest.shapes[0].id)
 }
 @Test fun layersPersistAcrossObjectTypesAndHitTopmost() {
  val p=Page.create("layers"); p.addShape("rect",0f,0f,100f,100f);p.addCanvasImage("a.img",0f,0f,100f,100f)
  assertEquals("image",p.hitTest(Point(50f,50f),0f)?.type)
  p.reorderSelection(CanvasSelection("shape",0),LayerMove.TO_FRONT)
  assertEquals("shape",p.hitTest(Point(50f,50f),0f)?.type)
  assertEquals("shape",Page.decode(p.encode()).hitTest(Point(50f,50f),0f)?.type)
 }
 @Test fun alignRightAndBottomUseObjectBounds() {
  val p=Page.create("align");p.addShape("rect",0f,0f,20f,10f);p.addShape("rect",30f,40f,40f,30f)
  val both=listOf(CanvasSelection("shape",0),CanvasSelection("shape",1))
  p.alignSelections(both,"right");p.alignSelections(both,"bottom")
  assertEquals(50f,p.shapes[0].x,0.001f);assertEquals(60f,p.shapes[0].y,0.001f)
  assertEquals(30f,p.shapes[1].x,0.001f);assertEquals(40f,p.shapes[1].y,0.001f)
 }
 @Test fun gridSnapMovesSelectionTogetherAndKeepsRelativePositions() {
  val p=Page.create("snap"); p.addShape("rect",31f,17f,20f,20f); p.addShape("rect",63f,55f,20f,20f)
  p.snapSelectionsToGrid(listOf(CanvasSelection("shape",0),CanvasSelection("shape",1)),28f)
  assertEquals(28f,p.shapes[0].x,0.001f); assertEquals(28f,p.shapes[0].y,0.001f); assertEquals(60f,p.shapes[1].x,0.001f); assertEquals(66f,p.shapes[1].y,0.001f)
 }
 @Test fun inkBoxSelectionSurvivesSerializationAndCanMove() {
  val p=Page.create("ink"); p.addStroke(listOf(Point(1f,1f),Point(5f,5f))); p.addStroke(listOf(Point(30f,30f),Point(40f,40f)))
  val saved=Page.decode(p.encode()); val selected=saved.selectInBox(Point(0f,0f),Point(10f,10f))
  assertTrue(CanvasSelection("stroke",0) in selected); val id=saved.strokeIds[0]
  saved.moveSelections(selected,10f,20f); assertEquals(Point(11f,21f),saved.strokes[0][0]); assertEquals(id,Page.decode(saved.encode()).strokeIds[0])
 }
 @Test fun alignLeftMovesShapesWithoutChangingTheirSize() {
  val p=Page.create("test"); p.addShape("rect",30f,0f,20f,20f); p.addShape("rect",5f,40f,30f,30f)
  p.alignLeft(listOf(CanvasSelection("shape",0),CanvasSelection("shape",1)))
  assertEquals(5f,p.shapes[0].x,0.001f); assertEquals(5f,p.shapes[1].x,0.001f)
  assertEquals(20f,p.shapes[0].width,0.001f); assertEquals(40f,p.shapes[1].y,0.001f)
 }
 @Test fun smallCrossingSelectionDetectsLineBetweenSamplePoints() {
  val p=Page.create("test"); p.addConnector(0f,20f,10000f,20f); p.connectors[0]=p.connectors[0].copy(pathStyle="straight")
  assertTrue(CanvasSelection("connector",0) in p.selectInBox(Point(503f,21f),Point(502f,19f)))
  assertFalse(CanvasSelection("connector",0) in p.selectInBox(Point(503f,18f),Point(502f,17f)))
 }
 @Test fun groupResizeScalesPositionsAndBoundLine() {
  val p=Page.create("test"); p.addShape("rect",0f,0f,20f,20f); p.addShape("rect",100f,0f,20f,20f); p.addBoundConnector(0,1)
  val items=listOf(CanvasSelection("shape",0),CanvasSelection("shape",1)); p.groupSelections(items)
  p.resizeSelections(items,2f)
  assertEquals(200f,p.shapes[1].x,0.001f); assertEquals(40f,p.shapes[0].width,0.001f); assertEquals(220f,p.resolvedConnector(0).x2,0.001f)
 }
 @Test fun boxDirectionDistinguishesContainedAndIntersecting() {
  val p=Page.create("test"); p.addShape("rect",0f,0f,20f,20f); p.addShape("rect",30f,0f,20f,20f)
  assertEquals(listOf(CanvasSelection("shape",0)),p.selectInBox(Point(-5f,-5f),Point(35f,25f)))
  assertEquals(2,p.selectInBox(Point(35f,-5f),Point(-5f,25f)).size)
 }
 @Test fun cornerResizeKeepsOppositeCornerFixed() {
  val p=Page.create("test"); p.addShape("rect",10f,20f,20f,10f)
  p.resizeSelectionsAnchored(listOf(CanvasSelection("shape",0)),2f,Point(30f,30f))
  assertEquals(-10f,p.shapes[0].x,0.001f); assertEquals(10f,p.shapes[0].y,0.001f)
  assertEquals(40f,p.shapes[0].width,0.001f); assertEquals(20f,p.shapes[0].height,0.001f)
 }
 @Test fun freeResizeChangesWidthAndHeightIndependently() {
  val p=Page.create("test"); p.addShape("rect",10f,20f,100f,60f)
  val snapshot=p.freeResizeSnapshot(listOf(CanvasSelection("shape",0)),Point(110f,80f))!!
  p.applyFreeResize(snapshot,Point(60f,40f))
  assertEquals(60f,p.shapes[0].x,0.001f); assertEquals(40f,p.shapes[0].y,0.001f)
  assertEquals(50f,p.shapes[0].width,0.001f); assertEquals(40f,p.shapes[0].height,0.001f)
  assertEquals(110f,p.shapes[0].x+p.shapes[0].width,0.001f); assertEquals(80f,p.shapes[0].y+p.shapes[0].height,0.001f)
 }
 @Test fun freeResizeClampsToOriginalSideAndMinimumSize() {
  val p=Page.create("test"); p.addShape("rect",0f,0f,100f,100f)
  val snapshot=p.freeResizeSnapshot(listOf(CanvasSelection("shape",0)),Point(100f,100f))!!
  p.applyFreeResize(snapshot,Point(200f,200f))
  assertEquals(76f,p.shapes[0].x,0.001f); assertEquals(76f,p.shapes[0].y,0.001f)
  assertEquals(24f,p.shapes[0].width,0.001f); assertEquals(24f,p.shapes[0].height,0.001f)
  p.applyFreeResize(snapshot,Point(-50f,-50f))
  assertEquals(-50f,p.shapes[0].x,0.001f); assertEquals(-50f,p.shapes[0].y,0.001f)
  assertEquals(150f,p.shapes[0].width,0.001f); assertEquals(150f,p.shapes[0].height,0.001f)
 }
 @Test fun freeResizeScalesGroupAxesIndependently() {
  val p=Page.create("test"); p.addShape("rect",0f,0f,20f,20f); p.addShape("rect",60f,0f,40f,40f)
  val items=listOf(CanvasSelection("shape",0),CanvasSelection("shape",1))
  val snapshot=p.freeResizeSnapshot(items,Point(100f,40f))!!
  p.applyFreeResize(snapshot,Point(50f,10f))
  assertEquals(50f,p.shapes[0].x,0.001f); assertEquals(10f,p.shapes[0].y,0.001f)
  assertEquals(10f,p.shapes[0].width,0.001f); assertEquals(15f,p.shapes[0].height,0.001f)
  assertEquals(80f,p.shapes[1].x,0.001f); assertEquals(10f,p.shapes[1].y,0.001f)
  assertEquals(20f,p.shapes[1].width,0.001f); assertEquals(30f,p.shapes[1].height,0.001f)
 }
 @Test fun freeResizeHorizontalEdgeOnlyChangesWidth() {
  val p=Page.create("test"); p.addShape("rect",10f,20f,100f,60f)
  val snapshot=p.freeResizeSnapshot(listOf(CanvasSelection("shape",0)),Point(110f,20f))!!
  p.applyFreeResize(snapshot,Point(60f,999f),axis="x")
  assertEquals(60f,p.shapes[0].x,0.001f); assertEquals(20f,p.shapes[0].y,0.001f)
  assertEquals(50f,p.shapes[0].width,0.001f); assertEquals(60f,p.shapes[0].height,0.001f)
 }
 @Test fun freeResizeVerticalEdgeOnlyChangesHeight() {
  val p=Page.create("test"); p.addShape("rect",10f,20f,100f,60f)
  val snapshot=p.freeResizeSnapshot(listOf(CanvasSelection("shape",0)),Point(10f,80f))!!
  p.applyFreeResize(snapshot,Point(999f,40f),axis="y")
  assertEquals(10f,p.shapes[0].x,0.001f); assertEquals(40f,p.shapes[0].y,0.001f)
  assertEquals(100f,p.shapes[0].width,0.001f); assertEquals(40f,p.shapes[0].height,0.001f)
 }
 @Test fun duplicateSelectionCreatesIndependentObject() {
  val p=Page.create("test"); p.addCard("笔记",10f,20f); val original=p.shapes.single()
  val created=p.duplicateSelections(listOf(CanvasSelection("shape",0)),Point(24f,18f))
  assertEquals(listOf(CanvasSelection("shape",1)),created); assertEquals(2,p.shapes.size)
  assertEquals(34f,p.shapes[1].x,0.001f); assertEquals(38f,p.shapes[1].y,0.001f)
  assertEquals(original.title,p.shapes[1].title); assertNotEquals(original.id,p.shapes[1].id)
 }
 @Test fun boxSelectionIncludesConnectorPath() {
  val p=Page.create("test"); p.addConnector(0f,20f,100f,20f); p.connectors[0]=p.connectors[0].copy(pathStyle="straight")
  val selected=p.selectInBox(Point(-5f,15f),Point(105f,25f))
  assertTrue(CanvasSelection("connector",0) in selected)
  val crossing=p.selectInBox(Point(55f,30f),Point(45f,10f))
  assertTrue(CanvasSelection("connector",0) in crossing)
 }
 @Test fun layerMoveReordersShapeAndReturnsStableSelection() {
  val p=Page.create("test"); repeat(3) { p.addShape("rect",it*30f,0f,20f,20f) }
  val ids=p.shapes.map { it.id }
  val selected=p.reorderSelection(CanvasSelection("shape",0),LayerMove.TO_FRONT)
  assertEquals(listOf(ids[1],ids[2],ids[0]),p.shapes.map { it.id })
  assertEquals(ids[0],p.shapes[selected.index].id)
  p.reorderSelection(selected,LayerMove.BACKWARD)
  assertEquals(listOf(ids[1],ids[0],ids[2]),p.shapes.map { it.id })
 }
}
