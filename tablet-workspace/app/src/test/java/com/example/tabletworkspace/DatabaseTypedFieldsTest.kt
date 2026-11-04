package com.example.tabletworkspace
import org.junit.Assert.*
import org.junit.Test
import java.io.*
import java.util.Base64
class DatabaseTypedFieldsTest {
 @Test fun viewSortAndCalendarPreferencesRoundTrip() {
  val db=LocalDatabase(
   "x",
   listOf(DatabaseRow("1","a","待办",mapOf("d" to "2026-10-30"))),
   listOf(DatabaseField("d","日期","date")),
   viewMode="calendar",
   sortField="d",
   descending=true,
   calendarField="d"
  )
  val restored=DatabaseCodec.decode(DatabaseCodec.encode(db))
  assertEquals("calendar",restored.viewMode)
  assertEquals("d",restored.sortField)
  assertTrue(restored.descending)
  assertEquals("d",restored.calendarField)
 }
 @Test fun versionThreeDefaultsToTableAndManualSort() {
  val b=ByteArrayOutputStream();DataOutputStream(b).use { it.writeInt(3);it.writeUTF("old");it.writeInt(0);it.writeInt(0) }
  val db=DatabaseCodec.decode(Base64.getEncoder().encodeToString(b.toByteArray()))
  assertEquals("table",db.viewMode)
  assertEquals("",db.sortField)
  assertFalse(db.descending)
  assertEquals("",db.calendarField)
 }
 @Test fun calendarGroupsValidDatesAndRoundTrips() {
  val db=LocalDatabase("x",listOf(DatabaseRow("a","A","",mapOf("d" to "2026-10-30")),DatabaseRow("b","B","",mapOf("d" to "2026-11-01")),DatabaseRow("c","C","")),listOf(DatabaseField("d","日期","date")))
  val restored=DatabaseCodec.decode(DatabaseCodec.encode(db))
  assertEquals(listOf("a"),restored.calendar("d",java.time.YearMonth.of(2026,10))[java.time.LocalDate.of(2026,10,30)]?.map { it.id })
  assertEquals(1,restored.calendar("d",java.time.YearMonth.of(2026,10)).size)
  try { DatabaseCodec.encode(db.copy(rows=listOf(db.rows[0].copy(values=mapOf("d" to "2026-02-30")))));fail("invalid date") } catch (_:IllegalArgumentException) {}
 }
 @Test fun typedFieldsRoundTripAndRejectInvalidCheckboxValues() {
  val db=LocalDatabase("x",listOf(DatabaseRow("1","a","待办",mapOf("c" to "true","n" to "12.5"))),listOf(DatabaseField("c","完成","checkbox"),DatabaseField("n","数量","number")))
  assertEquals(db,DatabaseCodec.decode(DatabaseCodec.encode(db)))
  try { DatabaseCodec.encode(db.copy(rows=listOf(db.rows[0].copy(values=mapOf("c" to "maybe")))));fail("invalid checkbox") } catch (_:IllegalArgumentException) { }
 }
 @Test fun versionTwoFieldsRemainText() {
  val b=ByteArrayOutputStream(); DataOutputStream(b).use { it.writeInt(2);it.writeUTF("old");it.writeInt(1);it.writeUTF("r");it.writeUTF("row");it.writeUTF("待办");it.writeInt(1);it.writeUTF("f");it.writeUTF("field");it.writeInt(1);it.writeUTF("f");it.writeUTF("value") }
  val db=DatabaseCodec.decode(Base64.getEncoder().encodeToString(b.toByteArray())); assertEquals("text",db.fields[0].type);assertEquals("value",db.rows[0].values["f"])
 }
}
