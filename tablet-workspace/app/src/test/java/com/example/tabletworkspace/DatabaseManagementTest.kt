package com.example.tabletworkspace
import org.junit.Assert.*
import org.junit.Test
class DatabaseManagementTest {
 @Test fun sortingNumericFieldsIsNumericStableAndDoesNotChangeRecords() {
  val db=LocalDatabase("x",listOf(DatabaseRow("a","A","",mapOf("n" to "10")),DatabaseRow("b","B","",mapOf("n" to "2")),DatabaseRow("c","C","",emptyMap())),listOf(DatabaseField("n","Number","number")))
  assertEquals(listOf("b","a","c"),db.sortedRows("", "n", false).map { it.id })
  assertEquals(listOf("a","b","c"),db.sortedRows("", "n", true).map { it.id })
  assertEquals(listOf("a","b","c"),db.rows.map { it.id })
 }
 @Test fun renamePreservesValuesAndRejectsBlank() {
  val db=LocalDatabase("x",listOf(DatabaseRow("1","a","",mapOf("f" to "高"))),listOf(DatabaseField("f","旧")))
  val renamed=db.renameField("f","优先级"); assertEquals("优先级",renamed.fields[0].name); assertEquals("高",renamed.rows[0].values["f"])
  try { db.renameField("f"," ");fail("blank accepted") } catch (_:IllegalArgumentException) { }
 }
 @Test fun boardIncludesEmptyStatusAndMovingRecordPreservesFields() {
  val row=DatabaseRow("1","a","",mapOf("f" to "v"));val db=LocalDatabase("x",listOf(row),listOf(DatabaseField("f","X")))
  assertEquals(listOf(row),db.board()["未设置"]); val moved=db.moveRow("1","完成"); assertEquals("完成",moved.rows[0].status); assertEquals("v",moved.rows[0].values["f"])
 }
 @Test fun fieldAndRowOrderingUseStableIdsAndPreserveValues() {
  val first=DatabaseRow("1","a","待办",mapOf("f1" to "one","f2" to "two"))
  val second=DatabaseRow("2","b","完成",mapOf("f1" to "three","f2" to "four"))
  val db=LocalDatabase("x",listOf(first,second),listOf(DatabaseField("f1","A"),DatabaseField("f2","B")))
  val fields=db.moveField("f2",-1)
  assertEquals(listOf("f2","f1"),fields.fields.map { it.id })
  assertEquals("two",fields.rows[0].values["f2"])
  val rows=fields.moveRecord("2",-1)
  assertEquals(listOf("2","1"),rows.rows.map { it.id })
  assertEquals("four",rows.rows[0].values["f2"])
  assertEquals(db.fields.map { it.id },db.moveField("f1",-1).fields.map { it.id })
  assertEquals(db.rows.map { it.id },db.moveRecord("1",-1).rows.map { it.id })
 }
 @Test fun multilineFieldsRoundTripAndAllowLineBreaks() {
  val db=LocalDatabase("x",listOf(DatabaseRow("1","a","待办",mapOf("notes" to "一\n二"))),listOf(DatabaseField("notes","备注","multiline")))
  assertEquals(db,DatabaseCodec.decode(DatabaseCodec.encode(db)))
 }
}
