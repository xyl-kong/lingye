package com.example.tabletworkspace.data.pages

import com.example.tabletworkspace.BackupArchive
import com.example.tabletworkspace.CardTemplate
import com.example.tabletworkspace.FolderCatalog
import com.example.tabletworkspace.Page
import com.example.tabletworkspace.PageLoadResult
import com.example.tabletworkspace.SharedBlock
import com.example.tabletworkspace.SharedBlockCatalog
import com.example.tabletworkspace.TemplateCatalog
import com.example.tabletworkspace.loadPageEntries
import com.example.tabletworkspace.pageFileNames
import com.example.tabletworkspace.pageFormatVersion
import com.example.tabletworkspace.data.images.ImageCacheStats
import com.example.tabletworkspace.data.images.PageImageStore
import com.example.tabletworkspace.data.metadata.PageMetadataSnapshot

import android.content.Context
import android.net.Uri
import android.util.AtomicFile
import java.io.File
import java.util.UUID

class PageStore(private val context: Context) {
    data class PageFormatMigration(val migrated: Int, val alreadyCurrent: Int, val failed: List<String>)
    private val dir = File(context.filesDir, "pages").apply { mkdirs() }
    private val historyDir = File(context.filesDir, "history").apply { mkdirs() }
    private val imageStore = PageImageStore(context, historyDir)
    private val folderJournal = File(context.filesDir, "folder-transaction.dat")
    private val backupStore by lazy { com.example.tabletworkspace.data.backup.BackupStore(this) }
    internal val appContext get() = context
    internal val pagesDirectory get() = dir
    internal val imagesDirectory get() = imageStore.directory
    private fun writeAtomic(target: File, bytes: ByteArray) { val f=AtomicFile(target);val out=f.startWrite();try {out.write(bytes);f.finishWrite(out)} catch(e:Exception) {f.failWrite(out);throw e} }
    private fun recoverFolderTransaction() {
        val journal=AtomicFile(folderJournal)
        if(!folderJournal.exists() && !File(folderJournal.path+".bak").exists()) return
        java.io.DataInputStream(journal.openRead()).use { input ->
            require(input.readInt()==1);val folderSize=input.readInt();require(folderSize in 0..1_000_000);val catalog=ByteArray(folderSize);input.readFully(catalog);FolderCatalog.decode(catalog)
            val count=input.readInt();require(count in 0..100_000)
            val entries=List(count) { val id=input.readUTF();require(runCatching { UUID.fromString(id).toString()==id }.getOrDefault(false));val n=input.readInt();require(n in 0..20_000_000);val bytes=ByteArray(n);input.readFully(bytes);require(Page.decode(bytes).id==id);id to bytes }
            require(input.read()==-1)
            entries.forEach { (id,bytes)->writeAtomic(File(dir,"$id.page"),bytes) };writeAtomic(File(context.filesDir,"folders.dat"),catalog)
        }
        check(folderJournal.delete() || !folderJournal.exists()) { "无法清除文件夹恢复日志" }
    }
    fun moveFolderContents(old:String, replacement:String, pages:List<Page>, folders:List<String>):List<String> {
        check(!folderJournal.exists() && !File(folderJournal.path+".bak").exists()) { "存在尚未恢复的文件夹事务，请重启应用" }
        require(old in folders && (replacement.isEmpty() || FolderCatalog.valid(replacement)))
        require(replacement.isEmpty() || folders.none { it!=old && it.equals(replacement,true) })
        val affected=pages.filter { old in it.folders }; val newFolders=if(replacement.isEmpty()) folders.filterNot { it==old } else folders.map { if(it==old) replacement else it }
        val out=java.io.ByteArrayOutputStream();java.io.DataOutputStream(out).use { data -> val catalog=FolderCatalog.encode(folders);data.writeInt(1);data.writeInt(catalog.size);data.write(catalog);data.writeInt(affected.size);affected.forEach { page -> require(runCatching { UUID.fromString(page.id).toString()==page.id }.getOrDefault(false));val bytes=page.encode();require(bytes.size<=20_000_000);data.writeUTF(page.id);data.writeInt(bytes.size);data.write(bytes) } }
        require(out.size()<=100_000_000);writeAtomic(folderJournal,out.toByteArray())
        try {
            affected.forEach { page ->
                val copy = Page.decode(page.encode())
                copy.folders.remove(old)
                if (replacement.isNotEmpty()) copy.folders.add(replacement)
                save(copy)
            }
            saveFolders(newFolders)
            check(folderJournal.delete()) { "无法完成文件夹事务" }
        }
        catch(e:Exception) { try {recoverFolderTransaction()} catch(rollback:Exception) {e.addSuppressed(rollback)};throw e }
        return newFolders
    }
    fun loadAll(): PageLoadResult {
        try { recoverFolderTransaction() } catch(e:Exception) { return PageLoadResult(emptyList(),listOf("folder-transaction.dat")) }
        runCatching { migratePagesToFormat25() }
        val names=pageFileNames(dir.listFiles().orEmpty().filter { it.isFile }.map { it.name })
        return loadPageEntries(names) { name -> AtomicFile(File(dir,name)).openRead().use { it.readBytes() } }
    }
    /**
     * Upgrade every stored page in place. A page that fails to decode is left untouched so
     * [loadPageEntries] can still report it and the user can recover it from a backup.
     */
    fun migratePagesToFormat25(): PageFormatMigration {
        var migrated = 0
        var alreadyCurrent = 0
        val failed = mutableListOf<String>()
        dir.listFiles().orEmpty().filter { it.isFile && it.name.endsWith(".page") }.sortedBy { it.name }.forEach { file ->
            runCatching {
                val bytes = AtomicFile(file).openRead().use { it.readBytes() }
                val version = pageFormatVersion(bytes)
                when {
                    version == 25 -> alreadyCurrent++
                    version != null && version in 1..24 -> {
                        val upgraded = Page.decode(bytes).encode()
                        require(pageFormatVersion(upgraded) == 25)
                        writeAtomic(file, upgraded)
                        migrated++
                    }
                    else -> error("unknown page format")
                }
            }.onFailure { failed.add(file.name) }
        }
        return PageFormatMigration(migrated, alreadyCurrent, failed)
    }
    fun save(page: Page) {
        saveEncoded(page.id, page.encode())
    }
    /** Permanently remove a page that is already in the trash, including its history file. */
    fun deletePermanently(pageId: String) {
        require(runCatching { UUID.fromString(pageId).toString() == pageId }.getOrDefault(false))
        val pageFile = File(dir, "$pageId.page")
        val historyFile = File(historyDir, "$pageId.history")
        if (pageFile.exists()) check(pageFile.delete()) { "无法删除页面文件" }
        File(pageFile.path + ".bak").takeIf { it.exists() }?.let { check(it.delete()) { "无法删除页面备份" } }
        if (historyFile.exists()) check(historyFile.delete()) { "无法删除页面历史" }
        File(historyFile.path + ".bak").takeIf { it.exists() }?.let { check(it.delete()) { "无法删除页面历史备份" } }
    }
    fun saveEncoded(pageId: String, bytes: ByteArray) {
        require(runCatching { UUID.fromString(pageId).toString() == pageId }.getOrDefault(false))
        require(bytes.isNotEmpty())
        writeAtomic(File(dir, "$pageId.page"), bytes)
    }
    fun loadHistory(page: Page): PageHistory? {
        val file = AtomicFile(File(historyDir, "${page.id}.history"))
        if (!file.baseFile.exists() && !File(file.baseFile.path + ".bak").exists()) return null
        return runCatching { file.openRead().use { PageHistory.fromEncoded(page, it.readBytes()) } }
            .getOrNull()
    }
    fun saveHistory(pageId: String, history: PageHistory) {
        saveHistoryEncoded(pageId, history.encode())
    }
    fun saveHistoryEncoded(pageId: String, bytes: ByteArray) {
        require(runCatching { UUID.fromString(pageId).toString() == pageId }.getOrDefault(false))
        require(bytes.isNotEmpty())
        writeAtomic(File(historyDir, "$pageId.history"), bytes)
    }
    fun pageModifiedTimes(): Map<String, Long> =
        dir.listFiles { file -> file.name.endsWith(".page") }.orEmpty()
            .associate { file -> file.name.removeSuffix(".page") to file.lastModified() }
    /** Best-effort creation time kept outside the page file; existing pages seed from file mtime. */
    fun pageCreatedAt(pageId: String, fallback: Long): Long {
        val prefs = context.getSharedPreferences("page_meta", Context.MODE_PRIVATE)
        val key = "created_$pageId"
        val existing = prefs.getLong(key, 0L)
        if (existing > 0L) return existing
        val value = fallback.coerceAtLeast(0L)
        if (value > 0L) prefs.edit().putLong(key, value).apply()
        return value
    }
    fun storageBytes(pages: List<Page>): Long {
        val pageBytes = pages.sumOf { it.encode().size.toLong() }
        val imageNames = pages.flatMap { page -> page.images.map { it.name } + page.blocks.filter { it.type == "image" }.map { it.content } }.toSet()
        return pageBytes + imageNames.sumOf { name -> File(imageStore.directory, name).takeIf { BackupArchive.validImage(name) }?.length() ?: 0L }
    }
    fun importImage(uri: Uri): String = imageStore.importImage(uri)
    fun importImageBytes(bytes: ByteArray): String = imageStore.importImageBytes(bytes)
    fun image(name: String, maxDimension: Int = 1600) = imageStore.image(name, maxDimension)
    fun imageCacheStats(): ImageCacheStats = imageStore.imageCacheStats()
    fun imageFileNames(): List<String> = imageStore.imageFileNames()
    fun orphanImages(pages: List<Page>, templates: List<CardTemplate>, shared: List<SharedBlock>): List<String> =
        imageStore.orphanImages(pages, templates, shared)
    fun imageBytes(names: List<String>): Long = imageStore.imageBytes(names)
    fun deleteImages(names: List<String>): Int = imageStore.deleteImages(names)
    fun loadFolders(): List<String> { val file = AtomicFile(File(context.filesDir, "folders.dat")); return if (file.baseFile.exists() || File(file.baseFile.path + ".bak").exists()) file.openRead().use { FolderCatalog.decode(it.readBytes()) } else emptyList() }
    fun saveFolders(folders: List<String>) { val file = AtomicFile(File(context.filesDir, "folders.dat")); val bytes = FolderCatalog.encode(folders); val stream = file.startWrite(); try { stream.write(bytes); file.finishWrite(stream) } catch (e: Exception) { file.failWrite(stream); throw e } }
    fun loadTemplates(): List<CardTemplate> { val f=AtomicFile(File(context.filesDir,"templates.dat")); return if(f.baseFile.exists() || File(f.baseFile.path+".bak").exists()) f.openRead().use { TemplateCatalog.decode(it.readBytes()) } else emptyList() }
    fun saveTemplates(items: List<CardTemplate>) { val f=AtomicFile(File(context.filesDir,"templates.dat")); val bytes=TemplateCatalog.encode(items); val output=f.startWrite(); try { output.write(bytes); f.finishWrite(output) } catch(e:Exception) { f.failWrite(output); throw e } }
    fun loadSharedBlocks():List<SharedBlock> { val f=AtomicFile(File(context.filesDir,"shared.dat"));return if(f.baseFile.exists() || File(f.baseFile.path+".bak").exists()) f.openRead().use { SharedBlockCatalog.decode(it.readBytes()) } else emptyList() }
    fun saveSharedBlocks(items:List<SharedBlock>) { writeAtomic(File(context.filesDir,"shared.dat"),SharedBlockCatalog.encode(items)) }
    data class RestoreResult(val pages: List<Page>, val folders: List<String>, val templates: List<CardTemplate>,val sharedBlocks:List<SharedBlock>)
    fun backup(pages: List<Page>, folders: List<String> = emptyList(), metadata: PageMetadataSnapshot? = null): ByteArray =
        backupStore.backup(pages, folders, metadata)
    fun restore(data: ByteArray, applyMetadata: (PageMetadataSnapshot, List<Page>, List<Page>) -> Unit = { _, _, _ -> }): RestoreResult =
        backupStore.restore(data, applyMetadata)
    fun writeBackup(uri: Uri, data: ByteArray) = backupStore.writeBackup(uri, data)
    fun readBackup(uri: Uri): ByteArray = backupStore.readBackup(uri)
    fun writeText(uri: Uri, text: String) = backupStore.writeText(uri, text)
    fun readText(uri: Uri): String = backupStore.readText(uri)
}




