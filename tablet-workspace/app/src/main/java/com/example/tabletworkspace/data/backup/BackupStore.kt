package com.example.tabletworkspace.data.backup

import android.graphics.BitmapFactory
import android.net.Uri
import com.example.tabletworkspace.BackupArchive
import com.example.tabletworkspace.CardTemplate
import com.example.tabletworkspace.FolderCatalog
import com.example.tabletworkspace.Page
import com.example.tabletworkspace.SharedBlock
import com.example.tabletworkspace.canvasPage
import com.example.tabletworkspace.data.metadata.PageMetadataSnapshot
import com.example.tabletworkspace.data.pages.PageStore
import com.example.tabletworkspace.imageNames
import com.example.tabletworkspace.remapImages
import com.example.tabletworkspace.remapSharedReferences
import com.example.tabletworkspace.remapTemplateImages
import java.io.File
import java.util.UUID

/**
 * Backup ZIP assembly, restore merge and SAF URI read/write. Kept next to [PageStore] so
 * page/catalog storage only exposes the primitives this class needs; public PageStore
 * methods delegate here for compatibility.
 */
class BackupStore(private val store: PageStore) {
    private val resolver = store.appContext.contentResolver

    fun backup(pages: List<Page>, folders: List<String> = emptyList(), metadata: PageMetadataSnapshot? = null): ByteArray {
        val templates = store.loadTemplates()
        val shared = store.loadSharedBlocks()
        val names = (pages.flatMap { p -> p.images.map { it.name } + p.blocks.filter { it.type == "image" }.map { it.content } } + templates.flatMap { it.canvasPage()?.images?.map { image -> image.name } ?: emptyList() } + shared.flatMap { it.imageNames() }).toSet()
        val content = names.associateWith { name -> require(BackupArchive.validImage(name)); val file = File(store.imagesDirectory, name); require(file.isFile && file.length() <= 20_000_000) { "图片不存在或过大" }; file.readBytes() }
        require(content.values.sumOf { it.size.toLong() } <= 90_000_000L) { "图片总量超过备份限制" }
        return BackupArchive.encode(pages, content, folders, templates, shared, metadata)
    }

    fun restore(data: ByteArray, applyMetadata: (PageMetadataSnapshot, List<Page>, List<Page>) -> Unit = { _, _, _ -> }): PageStore.RestoreResult {
        val backup = BackupArchive.decode(data)
        val created = mutableListOf<File>()
        try {
            val mapping = backup.images.mapValues { (_, bytes) ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }; BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                require(options.outWidth > 0 && options.outHeight > 0) { "备份中的图片无效" }
                val name = "${UUID.randomUUID()}.img"; val file = File(store.imagesDirectory, name); check(!file.exists()); created.add(file); file.writeBytes(bytes); name
            }
            val existingFolders = store.loadFolders().toMutableList()
            val importedFolders = backup.folders + backup.pages.flatMap { it.folders }
            val canonical = mutableMapOf<String, String>()
            importedFolders.forEach { name ->
                require(FolderCatalog.valid(name)) { "备份中的文件夹名称无效" }
                val existing = existingFolders.firstOrNull { it.equals(name, ignoreCase = true) }
                val target = existing ?: name.also { existingFolders.add(it) }; canonical[name] = target
            }
            val oldShared = store.loadSharedBlocks(); val sharedMapping = backup.sharedBlocks.associate { it.id to UUID.randomUUID().toString() }; val mergedShared = oldShared + backup.sharedBlocks.map { it.remapImages(mapping).copy(id = sharedMapping.getValue(it.id)) }
            val imported = backup.pages.map { page ->
                page.duplicate(UUID.randomUUID().toString(), mapping).also { restored ->
                    restored.folders.clear()
                    restored.folders.addAll(page.folders.map { canonical.getValue(it) })
                    restored.remapSharedReferences(sharedMapping)
                }
            }
            imported.forEach { page -> val file = File(store.pagesDirectory, "${page.id}.page"); check(!file.exists()); created.add(file); store.save(page) }
            val previousTemplates = store.loadTemplates(); val merged = previousTemplates.toMutableList(); backup.templates.forEach { t -> if (merged.none { it.name.equals(t.name, true) }) merged.add(t.remapTemplateImages(mapping)) }
            store.saveTemplates(merged); try { store.saveSharedBlocks(mergedShared); store.saveFolders(existingFolders) } catch (e: Exception) { store.saveTemplates(previousTemplates); store.saveSharedBlocks(oldShared); throw e }
            backup.metadata?.let { applyMetadata(it, backup.pages, imported) }
            return PageStore.RestoreResult(imported, existingFolders, merged, mergedShared)
        } catch (e: Exception) { created.forEach { it.delete() }; throw e }
    }

    fun writeBackup(uri: Uri, data: ByteArray) { resolver.openOutputStream(uri, "wt")?.use { it.write(data) } ?: error("无法写入备份") }
    fun readBackup(uri: Uri): ByteArray = resolver.openInputStream(uri)?.use { input ->
        val out = java.io.ByteArrayOutputStream(); val buffer = ByteArray(8192); var total = 0
        while (true) { val n = input.read(buffer); if (n < 0) break; total += n; require(total <= 100_000_000) { "备份文件过大" }; out.write(buffer, 0, n) }; out.toByteArray()
    } ?: error("无法读取备份")

    fun writeText(uri: Uri, text: String) {
        val bytes = text.toByteArray(Charsets.UTF_8)
        require(bytes.size <= 20_000_000) { "文本文件超过 20 MB" }
        resolver.openOutputStream(uri, "wt")?.use { it.write(bytes) } ?: error("无法写入文本文件")
    }

    fun readText(uri: Uri): String = resolver.openInputStream(uri)?.use { input ->
        val out = java.io.ByteArrayOutputStream(); val buffer = ByteArray(8192); var total = 0
        while (true) {
            val n = input.read(buffer); if (n < 0) break
            total += n; require(total <= 20_000_000) { "文本文件超过 20 MB" }; out.write(buffer, 0, n)
        }
        out.toByteArray().toString(Charsets.UTF_8)
    } ?: error("无法读取文本文件")
}
