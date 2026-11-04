package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageHistory
import org.junit.Assert.*
import org.junit.Test
class CardTodoTest {
 @Test fun toggleTodoSurvivesSaveAndUndoWithoutChangingOtherLines() {
  val p=Page.create("test"); p.addCard("待办",0f,0f); p.shapes[0]=p.shapes[0].copy(body="☐ first\n☑ second\nplain"); val h=PageHistory(p)
  p.toggleCardTodo(0,0); h.record(p)
  assertEquals("☑ first\n☑ second\nplain",Page.decode(p.encode()).shapes[0].body)
  assertEquals("☐ first\n☑ second\nplain",h.undo()!!.shapes[0].body)
 }
 @Test fun nonTodoLineCannotBeToggled() { val p=Page.create("x");p.addCard("笔记",0f,0f); val body=p.shapes[0].body; assertFalse(p.toggleCardTodo(0,0)); assertEquals(body,p.shapes[0].body) }

 @Test fun cardTodoAtFindsVisibleTodoLinesOnly() {
  val p=Page.create("hit"); p.addCard("待办",0f,0f); p.shapes[0]=p.shapes[0].copy(body="☐ first\n☑ second\nplain")
  assertEquals(CardTodoTarget(0,0), p.cardTodoAt(Point(20f,50f)))
  assertEquals(CardTodoTarget(0,1), p.cardTodoAt(Point(80f,70f)))
  assertEquals(null, p.cardTodoAt(Point(20f,90f)))
  assertEquals(null, p.cardTodoAt(Point(300f,50f)))
  p.shapes[0]=p.shapes[0].copy(locked=true)
  assertEquals(null, p.cardTodoAt(Point(20f,50f)))
 }

 @Test fun markdownTodoMarkersToggleAndHitTest() {
  val p=Page.create("md"); p.addCard("待办",0f,0f); p.shapes[0]=p.shapes[0].copy(body="[ ] first\n[x] second")
  assertEquals(CardTodoTarget(0,0), p.cardTodoAt(Point(20f,50f)))
  assertTrue(p.toggleCardTodo(0,0))
  assertEquals("[x] first\n[x] second", p.shapes[0].body)
  assertTrue(p.toggleCardTodo(0,1))
  assertEquals("[x] first\n[ ] second", p.shapes[0].body)
 }

 @Test fun emptyBracketTodoMarkerIsSupported() {
  val p=Page.create("empty-bracket"); p.addCard("待办",0f,0f); p.shapes[0]=p.shapes[0].copy(body="[] task")
  assertEquals(CardTodoTarget(0,0), p.cardTodoAt(Point(20f,50f)))
  assertTrue(p.toggleCardTodo(0,0))
  assertEquals("[x] task", p.shapes[0].body)
 }

 @Test fun screenHitTestMatchesRenderedLineSpacing() {
  val p=Page.create("screen"); p.addCard("待办",400f,200f); p.shapes[0]=p.shapes[0].copy(body="Item A\n[] task")
  val unit=2f
  val left: (Int)->Float={i->p.shapes[i].x*unit}
  val top: (Int)->Float={i->p.shapes[i].y*unit}
  assertEquals(CardTodoTarget(0,1), p.cardTodoAtScreen(420f*unit, 270f*unit, left, top, unit))
  assertEquals(null, p.cardTodoAtScreen(420f*unit, 250f*unit, left, top, unit))
 }
}
