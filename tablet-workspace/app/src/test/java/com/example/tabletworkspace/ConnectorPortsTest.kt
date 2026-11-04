package com.example.tabletworkspace
import org.junit.Assert.*
import org.junit.Test
class ConnectorPortsTest {
 @Test fun portBindingMovesWithShapeAndRoundTrips() {
  val p=Page.create("p"); p.addShape("rect",0f,0f,100f,80f); p.addShape("rect",300f,0f,100f,80f)
  p.addConnector(0f,0f,0f,0f); p.bindConnectorEnd(0,true,0,"right"); p.bindConnectorEnd(0,false,1,"left")
  assertEquals(100f,p.resolvedConnector(0).x1,0.001f); assertEquals(300f,p.resolvedConnector(0).x2,0.001f)
  p.moveSelection(CanvasSelection("shape",0),50f,10f)
  val saved=Page.decode(p.encode()); assertEquals(150f,saved.resolvedConnector(0).x1,0.001f); assertEquals("right",saved.connectors[0].startPort)
 }
 @Test fun detachPreservesPositionAndAnimationFlag() {
  val p=Page.create("p"); p.addShape("rect",0f,0f,100f,80f); p.addConnector(0f,0f,200f,0f); p.bindConnectorEnd(0,true,0,"top")
  p.connectors[0]=p.connectors[0].copy(animated=true); p.detachConnectorEnd(0,true,Point(55f,66f))
  val saved=Page.decode(p.encode()); assertEquals(55f,saved.resolvedConnector(0).x1,0.001f); assertTrue(saved.connectors[0].animated); assertEquals("",saved.connectors[0].startShape)
 }
}
