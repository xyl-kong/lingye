package com.example.tabletworkspace
import org.junit.Assert.*
import org.junit.Test
class CanvasImprovementsTest {
 @Test fun curveLeavesPortsInTheirOutwardDirection() {
  val c=Connector(100f,100f,0f,200f,startPort="right",endPort="left",curved=true)
  assertTrue(connectorPoint(c,0.05f).x > c.x1); assertTrue(connectorPoint(c,0.95f).x < c.x2)
 }
 @Test fun searchFindsCardTextWithoutFindingNonCardText() {
  val p=Page.create("x"); p.addCard("笔记",0f,0f); p.shapes[0]=p.shapes[0].copy(body="DeepFind")
  assertEquals(listOf(p),filterPages(listOf(p),"deepfind",null,false))
 }
 @Test fun customTemplateCanBeRenamedAndDeletedWithoutChangingCard() {
  val p=Page.create("x"); p.addCard("笔记",0f,0f); p.saveCardTemplate(0,"one"); p.renameCardTemplate(0,"two"); p.deleteCardTemplate(0)
  assertEquals(0,p.cardTemplates.size); assertEquals("笔记",p.shapes[0].title)
 }
 @Test fun selectedShapeStyleRoundTrips() {
  val p=Page.create("styles")
  p.addShape("rect",1f,2f,30f,40f)
  p.shapes[0]=p.shapes[0].copy(
   borderColor=0xff336699.toInt(),
   borderStyle="dashed",
   borderWidth=3f,
   cornerRadius=18f,
   textColor=0xff112233.toInt()
  )
  val restored=Page.decode(p.encode()).shapes.single()
  assertEquals(0xff336699.toInt(),restored.borderColor)
  assertEquals("dashed",restored.borderStyle)
  assertEquals(3f,restored.borderWidth,0.001f)
  assertEquals(18f,restored.cornerRadius,0.001f)
  assertEquals(0xff112233.toInt(),restored.textColor)
 }
 @Test fun invalidShapeBorderStyleIsRejected() {
  val p=Page.create("styles")
  p.addShape("rect",1f,2f,30f,40f)
  p.shapes[0]=p.shapes[0].copy(borderStyle="zigzag")
  try {
   p.encode()
   fail("invalid border style accepted")
  } catch (_:IllegalArgumentException) {
  }
 }
 @Test fun shapeLockRoundTrips() {
  val p=Page.create("lock")
  p.addShape("rect",1f,2f,30f,40f)
  p.shapes[0]=p.shapes[0].copy(locked=true)
  assertTrue(Page.decode(p.encode()).shapes.single().locked)
 }
 @Test fun pageCardTextSupportsDirectEditing() {
  val p=Page.create("before")
  p.updatePageCardText("after title","after body")
  assertEquals("after title",p.title)
  assertEquals("after body",p.text)
 }
 @Test fun shapeCardTextSupportsDirectEditing() {
  val p=Page.create("cards")
  p.addCard(CardPreset.NOTE,1f,2f)
  p.updateShapeCardText(0,"Direct title","Direct body")
  assertEquals("Direct title",p.shapes.single().title)
  assertEquals("Direct body",p.shapes.single().body)
 }
 @Test fun cardKindAndTextStyleRoundTrip() {
  val p=Page.create("cards")
  p.addCard(CardPreset.TODO,1f,2f)
  p.addTextBox(20f,30f,"Text box")
  p.shapes[1]=p.shapes[1].copy(fontSize=20f,textAlign="center",textColor=0xff336699.toInt())
  val restored=Page.decode(p.encode())
  assertEquals("todo",restored.shapes[0].cardKind)
  assertEquals("text",restored.shapes[1].type)
  assertEquals(20f,restored.shapes[1].fontSize,0.001f)
  assertEquals("center",restored.shapes[1].textAlign)
 }
 @Test fun todoCardCanCheckNextOpenItem() {
  val p=Page.create("todo")
  p.addCard(CardPreset.TODO,1f,2f)
  p.updateShapeCardText(0,"Todo","☑ First\n☐ Second")
  assertTrue(p.checkNextTodoItem(0))
  assertEquals("☑ First\n☑ Second",p.shapes.single().body)
 }
}
