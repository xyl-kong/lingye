package com.example.tabletworkspace
import org.junit.Assert.*
import org.junit.Test
class StableSelectionStateTest {
 @Test fun selectionFollowsObjectWhenEarlierItemRemoved() {
  val p=Page.create("p");p.addShape("rect",0f,0f,10f,10f);p.addShape("rect",30f,0f,10f,10f)
  val state=StableSelectionState(p);state.set(listOf(CanvasSelection("shape",1)));p.deleteSelection(CanvasSelection("shape",0))
  assertEquals(listOf(CanvasSelection("shape",0)),state.get());p.deleteSelection(CanvasSelection("shape",0));assertTrue(state.get().isEmpty())
 }
}
