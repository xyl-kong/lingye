package com.example.tabletworkspace
import org.junit.Assert.*
import org.junit.Test
class DatabaseFieldsTest {
 @Test fun customFieldAndBoardGroupingSurviveRoundTrip() {
  val f=DatabaseField("priority","优先级"); val db=LocalDatabase("任务",listOf(DatabaseRow("1","设计","进行中",mapOf("priority" to "高")),DatabaseRow("2","测试","待办")),listOf(f))
  val saved=DatabaseCodec.decode(DatabaseCodec.encode(db)); assertEquals("高",saved.rows[0].values["priority"]); assertEquals(listOf("1"),saved.board()["进行中"]!!.map { it.id }); assertEquals(listOf("1"),saved.filter("高").map { it.id })
 }
 @Test fun deletingFieldRemovesOnlyItsValues() {
  val db=LocalDatabase("x",listOf(DatabaseRow("1","a","待办",mapOf("a" to "1","b" to "2"))),listOf(DatabaseField("a","A"),DatabaseField("b","B")))
  val changed=db.removeField("a"); assertEquals(mapOf("b" to "2"),changed.rows[0].values); assertEquals("a",db.fields[0].id)
 }
}
