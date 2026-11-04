package com.example.tabletworkspace
import org.junit.Test
import org.junit.Assert.*
import java.util.UUID
class SharedBlocksTest {
 @Test fun overlappingRangeFormatsAndEditsPreserveSurvivingText() {
  val p=SharedBlock(UUID.randomUUID().toString(),"abcdef").toggleRange(0,4,"bold").toggleRange(2,6,"italic")
  assertEquals(listOf(SharedSpan(0,2,bold=true),SharedSpan(2,4,bold=true,italic=true),SharedSpan(4,6,italic=true)),p.spans)
  val replaced=p.withTextEdit("abXYef");assertEquals(listOf(SharedSpan(0,2,bold=true),SharedSpan(4,6,italic=true)),replaced.spans)
  val toggled=p.toggleRange(0,4,"bold");assertEquals(listOf(SharedSpan(2,6,italic=true)),toggled.spans)
 }
 @Test fun unboldingSelectionInsideBoldBlockKeepsOutsideBold() {
  val p=SharedBlock(UUID.randomUUID().toString(),"abcdef",bold=true).toggleRange(2,4,"bold")
  assertFalse(p.bold);assertEquals(listOf(SharedSpan(0,2,bold=true),SharedSpan(4,6,bold=true)),p.spans)
 }
 @Test fun emojiSelectionsAndReplacementDoNotSplitSurrogatePairs() {
  val p=SharedBlock(UUID.randomUUID().toString(),"a😀b").toggleRange(2,3,"bold")
  assertEquals(listOf(SharedSpan(1,3,bold=true)),p.spans)
  val changed=p.withTextEdit("a😁b");assertTrue(changed.spans.none { it.start==2 || it.end==2 })
 }
 @Test fun imageUndoHistoryIsRemappedAndRetainedInBackup() {
  val a=SharedBlock(UUID.randomUUID().toString(),"a.img",kind="image");val b=a.copy(text="b.img").withHistoryFrom(a);val p=Page.create("ref");p.addBlock("shared",a.id)
  val archive=BackupArchive.encode(listOf(p),mapOf("a.img" to byteArrayOf(1),"b.img" to byteArrayOf(2)),sharedBlocks=listOf(b))
  val restored=BackupArchive.decode(archive).sharedBlocks.single().remapImages(mapOf("a.img" to "new-a.img","b.img" to "new-b.img"))
  assertEquals("new-b.img",restored.text);assertEquals("new-a.img",restored.undoShared()!!.text)
 }
 @Test fun sharedDatabaseUndoRedoSurvivesReload() {
  val id=UUID.randomUUID().toString();val initial=SharedBlock(id,DatabaseCodec.encode(LocalDatabase("db",emptyList())),kind="database")
  val next=initial.copy(text=DatabaseCodec.encode(LocalDatabase("db",listOf(DatabaseRow("row","task","todo"))))).withHistoryFrom(initial)
  val reloaded=SharedBlockCatalog.decode(SharedBlockCatalog.encode(listOf(next))).single();val undone=reloaded.undoShared()!!
  assertTrue(DatabaseCodec.decode(undone.text).rows.isEmpty());val restarted=SharedBlockCatalog.decode(SharedBlockCatalog.encode(listOf(undone))).single();assertEquals("task",DatabaseCodec.decode(restarted.redoShared()!!.text).rows.single().title)
 }
 @Test fun rangeFormattingAndHistorySurviveCatalogRoundTrip() {
  val id=UUID.randomUUID().toString();val first=SharedBlock(id,"hello world")
  val formatted=first.toggleRange(0,5,"bold");assertTrue(formatted.spans.any { it.start==0 && it.end==5 && it.bold })
  val edited=formatted.withTextEdit("hello brave world").withHistoryFrom(formatted)
  val restored=SharedBlockCatalog.decode(SharedBlockCatalog.encode(listOf(edited))).single()
  assertEquals(formatted,restored.undoShared()!!.copy(undoHistory=emptyList(),redoHistory=emptyList()))
  assertEquals(5,restored.spans.single().end)
 }
 @Test fun sharedRichTypesAndUndoRedoRoundTrip() {
  val id=UUID.randomUUID().toString();val text=SharedBlock(id,"bold",kind="paragraph",bold=true)
  val image=SharedBlock(UUID.randomUUID().toString(),"a.img",kind="image")
  val db=SharedBlock(UUID.randomUUID().toString(),DatabaseCodec.encode(LocalDatabase("db",emptyList())),kind="database")
  assertEquals(listOf(text,image,db),SharedBlockCatalog.decode(SharedBlockCatalog.encode(listOf(text,image,db))))
  val history=SharedBlockHistory(text);history.record(text.copy(text="next"));assertEquals(text,history.undo());assertEquals("next",history.redo()!!.text)
 }
 @Test fun legacySharedTextAndImageBackup() {
  val id=UUID.randomUUID().toString();val bytes=java.io.ByteArrayOutputStream();java.io.DataOutputStream(bytes).use {out->out.writeInt(1);out.writeInt(1);out.writeUTF(id);val text="legacy".toByteArray();out.writeInt(text.size);out.write(text)}
  assertEquals(SharedBlock(id,"legacy"),SharedBlockCatalog.decode(bytes.toByteArray()).single())
  val image=SharedBlock(UUID.randomUUID().toString(),"test.img",kind="image");val page=Page.create("page");page.addBlock("shared",image.id)
  val archive=BackupArchive.encode(listOf(page),mapOf("test.img" to byteArrayOf(1,2)),sharedBlocks=listOf(image));assertEquals(image,BackupArchive.decode(archive).sharedBlocks.single())
  try {BackupArchive.decode(BackupArchive.encode(listOf(page),emptyMap(),sharedBlocks=listOf(image)));fail("missing shared image")} catch (_:IllegalArgumentException) {}
 }
 @Test fun sharedBlocksRoundTripAndRejectDuplicateIds() {
  val block=SharedBlock(UUID.randomUUID().toString(),"共享文字\nsecond line")
  assertEquals(listOf(block),SharedBlockCatalog.decode(SharedBlockCatalog.encode(listOf(block))))
  try {SharedBlockCatalog.encode(listOf(block,block));fail("duplicate id accepted")} catch (_:IllegalArgumentException) {}
 }
 @Test fun backupRetainsSharedBlockReferencedByPages() {
  val shared=SharedBlock(UUID.randomUUID().toString(),"sync")
  val page=Page.create("page");page.addBlock("shared",shared.id)
  val restored=BackupArchive.decode(BackupArchive.encode(listOf(page),emptyMap(),sharedBlocks=listOf(shared)))
  assertEquals(shared,restored.sharedBlocks.single());assertEquals(shared.id,restored.pages.single().blocks.single().content)
  val newId=UUID.randomUUID().toString();restored.pages.single().remapSharedReferences(mapOf(shared.id to newId));assertEquals(newId,restored.pages.single().blocks.single().content)
 }
 @Test fun searchFindsSharedContentInEveryReferencingPage() {
  val shared=SharedBlock(UUID.randomUUID().toString(),"Important text");val a=Page.create("a");val b=Page.create("b");a.addBlock("shared",shared.id);b.addBlock("shared",shared.id)
  assertEquals(setOf(a.id,b.id),filterPages(listOf(a,b),"important",null,false,listOf(shared)).map { it.id }.toSet())
 }
}
