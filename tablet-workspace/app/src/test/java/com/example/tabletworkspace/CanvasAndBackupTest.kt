package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageHistory

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CanvasAndBackupTest {
    @Test fun backupRejectsMissingTemplateImageAndRemapsItOnRestore() {
        val p=Page.create("template");p.addCanvasImage("a.img",0f,0f,30f,30f)
        val template=p.makeCanvasTemplate("Images",listOf(CanvasSelection("image",0)))
        val data=BackupArchive.encode(listOf(Page.create("page")),mapOf("a.img" to byteArrayOf(1,2,3)),templates=listOf(template))
        val loaded=BackupArchive.decode(data).templates.single().remapTemplateImages(mapOf("a.img" to "b.img"))
        assertEquals("b.img",loaded.canvasPage()!!.images.single().name)
        try { BackupArchive.decode(BackupArchive.encode(listOf(Page.create("page")),emptyMap(),templates=listOf(template)));fail("missing template image") } catch (_:IllegalArgumentException) {}
    }
    @Test fun imageAndStyleRoundTrip() {
        val p = Page.create("画板")
        p.addCanvasImage("abc.img", 10f, 20f, 150f, 120f)
        p.addShape("rect", 5f, 6f, 80f, 90f)
        p.shapes[0] = p.shapes[0].copy(color = 0xff4488cc.toInt())
        p.addConnector(1f, 2f, 3f, 4f)
        p.connectors[0] = p.connectors[0].copy(color = 0xff112233.toInt(), width = 4f)
        val restored = Page.decode(p.encode())
        assertEquals("abc.img", restored.images.single().name)
        assertEquals(150f, restored.images[0].width, 0.001f)
        assertEquals(0xff4488cc.toInt(), restored.shapes[0].color)
        assertEquals(4f, restored.connectors[0].width, 0.001f)
    }
    @Test fun undoRedoRestoresDistinctCanvasActions() {
        val p = Page.create("test"); val history = PageHistory(p)
        p.addShape("rect", 1f, 2f, 30f, 40f); history.record(p)
        p.addConnector(0f, 0f, 20f, 20f); history.record(p)
        val undone = history.undo()!!
        assertEquals(1, undone.shapes.size); assertTrue(undone.connectors.isEmpty())
        assertEquals(1, history.redo()!!.connectors.size)
    }
    @Test fun backupPreservesPagesAndImages() {
        val p = Page.create("备份"); p.addCanvasImage("abc.img", 0f, 0f, 20f, 20f)
        val data = BackupArchive.encode(listOf(p), mapOf("abc.img" to byteArrayOf(1,2,3)))
        val loaded = BackupArchive.decode(data)
        assertEquals("备份", loaded.pages.single().title)
        assertArrayEquals(byteArrayOf(1,2,3), loaded.images["abc.img"])
    }
    @Test fun backupRejectsTraversalAndMissingImage() {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip -> zip.putNextEntry(ZipEntry("../evil")); zip.write(1); zip.closeEntry() }
        try { BackupArchive.decode(bytes.toByteArray()); fail("unsafe path accepted") } catch (_: IllegalArgumentException) { }
        val p = Page.create("broken"); p.addCanvasImage("missing.img", 0f,0f,10f,10f)
        try { BackupArchive.decode(BackupArchive.encode(listOf(p), emptyMap())); fail("missing image accepted") } catch (_: IllegalArgumentException) { }
    }
    @Test fun imageSelectionCanMoveResizeAndUndo() {
        val p = Page.create("image"); p.addCanvasImage("abc.img", 5f, 10f, 100f, 80f)
        val history = PageHistory(p); val selected = p.hitTest(Point(20f, 20f), 0f)!!
        assertEquals(CanvasSelection("image", 0), selected)
        p.moveSelection(selected, 30f, 20f); p.resizeSelection(selected, 1.5f); history.record(p)
        assertEquals(150f, p.images.single().width, 0.001f)
        assertEquals(5f, history.undo()!!.images.single().x, 0.001f)
        assertEquals(35f, history.redo()!!.images.single().x, 0.001f)
    }
    @Test fun rejectsEmptyBackupBeforeExport() {
        try { BackupArchive.encode(emptyList(), emptyMap()); fail("empty backup accepted") } catch (_: IllegalArgumentException) { }
    }}

