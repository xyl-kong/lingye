package com.example.tabletworkspace

import org.junit.Assert.*
import org.junit.Test

class WorkspaceManagementTest {
    @Test fun legacyWorkspaceTemplateGetsDefaultSize() {
        val bytes=java.io.ByteArrayOutputStream(); java.io.DataOutputStream(bytes).use { out -> out.writeInt(1);out.writeInt(1);out.writeUTF("legacy");out.writeInt(1);out.write(byteArrayOf(65));out.writeInt(1);out.write(byteArrayOf(66));out.writeInt(3) }
        val template=TemplateCatalog.decode(bytes.toByteArray()).single()
        assertEquals(240f,template.width,0.001f);assertEquals(150f,template.height,0.001f);assertEquals("A",template.title)
    }
    @Test fun workspaceTemplatePreservesCardSize() {
        val t=CardTemplate("wide","Title","Body",123,360f,210f)
        val loaded=TemplateCatalog.decode(TemplateCatalog.encode(listOf(t))).single()
        val page=Page.create("target"); page.insertWorkspaceTemplate(loaded,5f,7f)
        assertEquals(360f,page.shapes.single().width,0.001f); assertEquals(210f,page.shapes.single().height,0.001f)
    }
    @Test fun blockOrderFormattingAndFolderSurviveRoundTrip() {
        val p = Page.create("Notes")
        p.addBlock("paragraph", "first"); p.addBlock("paragraph", "second")
        p.blocks[1].bold = true; p.blocks[1].italic = true; p.blocks[1].underline = true
        p.moveBlock(1, 0); p.folder = "项目"
        val restored = Page.decode(p.encode())
        assertEquals(listOf("second", "first"), restored.blocks.map { it.content })
        assertTrue(restored.blocks[0].bold && restored.blocks[0].italic && restored.blocks[0].underline)
        assertEquals("项目", restored.folder)
    }
    @Test fun searchFiltersFolderAndTrashWithoutTouchingData() {
        val a=Page.create("Meeting"); a.folder="项目"; a.addBlock("paragraph", "Important notes")
        val b=Page.create("Other"); b.folder="个人"; b.text="Important detail"
        val c=Page.create("Important deleted"); c.trashed=true
        assertEquals(listOf(a.id), filterPages(listOf(a,b,c), "important", "项目", false).map { it.id })
        assertEquals(listOf(c.id), filterPages(listOf(a,b,c), "important", null, true).map { it.id })
        val restored=Page.decode(c.encode()); assertTrue(restored.trashed); restored.trashed=false; assertFalse(restored.trashed)
    }
    @Test fun folderCatalogAndBackupKeepEmptyFoldersAndTrash() {
        val folders=listOf("项目", "空文件夹")
        assertEquals(folders, FolderCatalog.decode(FolderCatalog.encode(folders)))
        val p=Page.create("Archive"); p.folder="项目"; p.trashed=true
        val data=BackupArchive.encode(listOf(p), emptyMap(), folders)
        val decoded=BackupArchive.decode(data)
        assertEquals(folders, decoded.folders)
        assertTrue(decoded.pages.single().trashed)
        assertEquals("项目", decoded.pages.single().folder)
    }
    @Test fun oldPagesDefaultToUnfiledAndNotTrashed() {
        val out=java.io.ByteArrayOutputStream()
        java.io.DataOutputStream(out).use { d -> d.writeInt(1); d.writeUTF("old"); d.writeUTF("old"); d.writeInt(0); d.writeFloat(0f); d.writeFloat(0f); d.writeInt(0) }
        val p=Page.decode(out.toByteArray()); assertEquals("", p.folder); assertFalse(p.trashed)
    }
    @Test fun versionFourPageKeepsInkAndDefaultsNewMetadata() {
        val p=Page.create("v4"); p.addBlock("paragraph", "legacy"); p.addStyledStroke(listOf(Point(1f,2f)), 0xff123456.toInt(), 8f)
        val encoded=p.encode(); val legacy=encoded.copyOf(encoded.size - (3 * p.blocks.size) - 5); legacy[3]=4
        val result=Page.decode(legacy)
        assertEquals("legacy", result.blocks.single().content); assertFalse(result.blocks.single().bold)
        assertEquals(8f, result.strokeStyles.single().width, 0.001f); assertEquals("", result.folder); assertFalse(result.trashed)
    }
    @Test fun catalogRejectsDuplicateAndInvalidNames() {
        try { FolderCatalog.encode(listOf("Plan", "plan")); fail("duplicate accepted") } catch (_: IllegalArgumentException) { }
        try { FolderCatalog.encode(listOf("bad\nname")); fail("invalid name accepted") } catch (_: IllegalArgumentException) { }
    }}

