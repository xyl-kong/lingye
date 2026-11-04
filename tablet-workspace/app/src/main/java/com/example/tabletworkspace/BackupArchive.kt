package com.example.tabletworkspace
import com.example.tabletworkspace.data.metadata.PageMetadataSnapshot
import com.example.tabletworkspace.data.metadata.PageMetadataSnapshotCodec

import java.io.*
import java.util.zip.*

data class BackupContents(val pages: List<Page>, val images: Map<String, ByteArray>, val folders: List<String> = emptyList(), val templates: List<CardTemplate> = emptyList(), val sharedBlocks:List<SharedBlock> = emptyList(), val metadata: PageMetadataSnapshot? = null)
object BackupArchive {
    private const val MAX_TOTAL = 100_000_000
    private const val MAX_ENTRY = 20_000_000
    fun encode(pages: List<Page>, images: Map<String, ByteArray>, folders: List<String> = emptyList(), templates: List<CardTemplate> = emptyList(), sharedBlocks:List<SharedBlock> = emptyList(), metadata: PageMetadataSnapshot? = null): ByteArray {
        require(pages.isNotEmpty()) { "没有可备份的页面" }
        val out = ByteArrayOutputStream(); var total = 0L
        ZipOutputStream(out).use { zip ->
            fun write(name: String, data: ByteArray) { total += data.size; require(data.size <= MAX_ENTRY && total <= MAX_TOTAL) { "备份内容过大" }; zip.putNextEntry(ZipEntry(name)); zip.write(data); zip.closeEntry() }
            write("format.txt", "lingye-backup-2".toByteArray()); write("folders.dat", FolderCatalog.encode(folders)); write("templates.dat",TemplateCatalog.encode(templates))
            require(pages.flatMap { it.blocks }.filter { it.type=="shared" }.all { block->sharedBlocks.any { it.id==block.content } });write("shared.dat",SharedBlockCatalog.encode(sharedBlocks))
            if (metadata != null && !metadata.isEmpty()) write("metadata.dat", PageMetadataSnapshotCodec.encode(metadata))
            pages.forEachIndexed { i, page -> write("pages/$i.page", page.encode()) }
            images.toSortedMap().forEach { (name, data) -> require(validImage(name)); write("images/$name", data) }
        }
        return out.toByteArray()
    }
    fun decode(data: ByteArray): BackupContents {
        require(data.size <= MAX_TOTAL) { "备份文件过大" }
        val entries = linkedMapOf<String, ByteArray>(); var total = 0
        ZipInputStream(ByteArrayInputStream(data)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break; val name = entry.name
                require(!entry.isDirectory && (name == "format.txt" || name == "folders.dat" || name == "templates.dat" || name == "shared.dat" || name == "metadata.dat" || name.matches(Regex("pages/[0-9]+\\.page")) || (name.startsWith("images/") && validImage(name.removePrefix("images/"))))) { "备份包含无效路径" }
                require(!entries.containsKey(name) && entries.size < 10000) { "备份条目无效" }
                val out = ByteArrayOutputStream(); val buffer = ByteArray(8192); var count = 0
                while (true) { val n = zip.read(buffer); if (n < 0) break; count += n; total += n; require(count <= MAX_ENTRY && total <= MAX_TOTAL) { "备份内容过大" }; out.write(buffer, 0, n) }
                entries[name] = out.toByteArray(); zip.closeEntry()
            }
        }
        require(entries["format.txt"]?.toString(Charsets.UTF_8) in setOf("lingye-backup-1", "lingye-backup-2")) { "备份格式无效" }
        val pages = entries.filterKeys { it.startsWith("pages/") }.toSortedMap().values.map { Page.decode(it) }
        val images = entries.filterKeys { it.startsWith("images/") }.mapKeys { it.key.removePrefix("images/") }
        require(pages.isNotEmpty()) { "备份没有页面" }
        pages.forEach { p -> (p.images.map { it.name } + p.blocks.filter { it.type == "image" }.map { it.content }).forEach { require(images.containsKey(it)) { "备份缺少图片" } } }
        val folders = entries["folders.dat"]?.let(FolderCatalog::decode) ?: emptyList()
        val templates=entries["templates.dat"]?.let(TemplateCatalog::decode) ?: emptyList()
        templates.forEach { t -> t.canvasPage()?.images?.forEach { require(images.containsKey(it.name)) { "备份缺少模板图片" } } }
        val shared=entries["shared.dat"]?.let(SharedBlockCatalog::decode) ?: emptyList()
        shared.flatMap { it.imageNames() }.forEach { require(images.containsKey(it)) { "备份缺少共享图片" } }
        require(pages.flatMap { it.blocks }.filter { it.type=="shared" }.all { block->shared.any { it.id==block.content } }) { "备份缺少共享块" }
        val metadata=entries["metadata.dat"]?.let { PageMetadataSnapshotCodec.decode(it) }
        return BackupContents(pages, images, folders, templates,shared,metadata)
    }
    fun validImage(name: String) = name.matches(Regex("[a-zA-Z0-9-]+\\.img"))
}


