package com.example.tabletworkspace
import org.junit.Assert.*
import org.junit.Test
class DatabaseTest {
 @Test fun databaseRoundTripAndFiltering() {
  val db=LocalDatabase("任务", listOf(DatabaseRow("a","设计","进行中"),DatabaseRow("b","测试","完成")))
  val restored=DatabaseCodec.decode(DatabaseCodec.encode(db)); assertEquals(db,restored); assertEquals(listOf("a"),restored.filter("设计").map { it.id })
 }
 @Test fun databaseBlockSurvivesPageAndBackup() {
  val p=Page.create("p"); p.addDatabase("项目"); val d=DatabaseCodec.decode(p.blocks[0].content); p.blocks[0].content=DatabaseCodec.encode(d.copy(rows=listOf(DatabaseRow("1","A","待办"))))
  val restored=BackupArchive.decode(BackupArchive.encode(listOf(p),emptyMap())).pages[0]; assertEquals("A",DatabaseCodec.decode(restored.blocks[0].content).rows[0].title)
 }
}
