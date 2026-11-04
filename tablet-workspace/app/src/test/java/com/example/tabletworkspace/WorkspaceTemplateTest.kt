package com.example.tabletworkspace
import org.junit.Assert.*
import org.junit.Test
class WorkspaceTemplateTest {
 @Test fun templatesSurviveBackupAndKeepDuplicatesIndependent() {
  val p=Page.create("first"); val t=CardTemplate("plan","Title","Body",1)
  val data=BackupArchive.encode(listOf(p),emptyMap(),emptyList(),listOf(t))
  val restored=BackupArchive.decode(data); assertEquals(listOf(t),restored.templates)
  val q=Page.create("second"); q.insertWorkspaceTemplate(restored.templates[0],20f,30f); q.shapes[0]=q.shapes[0].copy(body="changed")
  assertEquals("Body",restored.templates[0].body)
 }
 @Test fun catalogRejectsDuplicateNamesAndRoundTripsUnicode() {
  val t=CardTemplate("计划","标题","内容",123); assertEquals(listOf(t),TemplateCatalog.decode(TemplateCatalog.encode(listOf(t))))
  try { TemplateCatalog.encode(listOf(t,t)); fail("duplicate accepted") } catch (_: IllegalArgumentException) { }
 }
}
