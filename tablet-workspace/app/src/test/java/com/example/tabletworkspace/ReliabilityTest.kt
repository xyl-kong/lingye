package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageHistory
import org.junit.Assert.*
import org.junit.Test
class ReliabilityTest {
 @org.junit.Test fun atomicBackupOnlyPagesAreDiscoveredOnce() {
  org.junit.Assert.assertEquals(listOf("a.page", "b.page"), pageFileNames(listOf("a.page", "a.page.bak", "b.page.bak", "ignore.tmp")))
 }
 @Test fun cancelledGestureRestoresShapesAndErasedStrokes() {
  val p=Page.create("x"); p.addShape("rect",0f,0f,20f,20f); p.addStroke(listOf(Point(5f,5f))); val original=p.encode()
  val tx=PageGesture(p); p.moveSelection(CanvasSelection("shape",0),50f,10f); p.eraseAt(Point(5f,5f),5f); tx.cancel()
  assertArrayEquals(original,p.encode()); assertEquals(1,p.strokes.size)
 }
 @Test fun commitKeepsChangesAndHistoryHasSingleUndo() {
  val p=Page.create("x"); p.addShape("rect",0f,0f,20f,20f); val history=PageHistory(p); val tx=PageGesture(p)
  repeat(10) { p.moveSelection(CanvasSelection("shape",0),1f,0f) }; tx.commit(); history.record(p)
  assertEquals(0f,history.undo()!!.shapes[0].x,0.001f); assertNull(history.undo())
 }
 @Test fun corruptPageDoesNotHideValidPages() {
  val p=Page.create("ok"); val data=mapOf("good" to p.encode(),"bad" to byteArrayOf(1,2,3))
  val result=loadPageEntries(data.keys.sorted()) { data.getValue(it) }
  assertEquals(listOf("ok"),result.pages.map { it.title }); assertEquals(listOf("bad"),result.failed)
 }
}
