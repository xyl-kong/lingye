package com.example.tabletworkspace
import org.junit.Assert.*
import org.junit.Test
class CurveCardTest {
 @Test fun curvePassesEndpointsAndHitTestsItsRenderedPath() {
  val p=Page.create("p"); p.addConnector(0f,0f,100f,100f); val c=p.connectors[0]; val middle=connectorPoint(c,0.5f)
  assertEquals(Point(0f,0f),connectorPoint(c,0f)); assertEquals(Point(100f,100f),connectorPoint(c,1f))
  assertEquals(CanvasSelection("connector",0),p.hitTest(middle,2f))
 }
 @Test fun editableCardTemplateIsIndependentAndSurvivesBackup() {
  val p=Page.create("p"); p.addCard("笔记",100f,100f); val initial=p.shapes[0]
  p.shapes[0]=initial.copy(title="我的卡",body="内容"); p.saveCardTemplate(0,"我的模板"); p.shapes[0]=p.shapes[0].copy(body="修改")
  val restored=BackupArchive.decode(BackupArchive.encode(listOf(p),emptyMap())).pages[0]
  restored.insertCardTemplate(0,300f,100f)
  assertEquals("内容",restored.shapes.last().body); assertNotEquals(restored.shapes[0].id,restored.shapes.last().id)
  restored.addConnector(0f,0f,0f,0f); restored.bindConnectorEnd(0,true,1,"left"); assertEquals(300f,restored.resolvedConnector(0).x1,0.01f)
 }
}
