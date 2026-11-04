package com.example.tabletworkspace.data.images

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import com.example.tabletworkspace.BackupArchive
import com.example.tabletworkspace.CardTemplate
import com.example.tabletworkspace.Page
import com.example.tabletworkspace.SharedBlock
import com.example.tabletworkspace.platform.image.PlatformImageDecoder
import java.io.File
import java.util.UUID

/**
 * Owns the private image folder: import/validation, the decoded-bitmap LRU cache, orphan
 * detection (including page-history references) and deletion. PageStore delegates here so
 * page-file and image-file responsibilities can evolve independently.
 */
class PageImageStore(context: Context, private val historyDir: File) {
    private val resolver = context.applicationContext.contentResolver
    internal val directory = File(context.applicationContext.filesDir, "images").apply { mkdirs() }

    private val imageCache = object : LruCache<String, Bitmap>(imageCacheBudgetKb()) {
        override fun sizeOf(key: String, value: Bitmap): Int = (value.byteCount / 1024).coerceAtLeast(1)
    }

    private fun imageCacheBudgetKb(): Int {
        val maxKb = Runtime.getRuntime().maxMemory() / 1024
        return (maxKb / 8).toInt().coerceIn(4 * 1024, 24 * 1024)
    }

    fun importImage(uri: Uri): String {
        val bytes = resolver.openInputStream(uri)?.use { input ->
            val out = java.io.ByteArrayOutputStream(); val buffer = ByteArray(8192); var total = 0
            while (true) { val n = input.read(buffer); if (n < 0) break; total += n; require(total <= 20_000_000) { "图片超过 20 MB" }; out.write(buffer, 0, n) }; out.toByteArray()
        } ?: error("无法读取图片")
        return importImageBytes(bytes)
    }

    fun importImageBytes(bytes: ByteArray): String {
        require(PlatformImageDecoder.size(bytes) != null) {
            "不支持该图片格式（支持 JPEG、PNG、WebP、GIF、BMP、HEIF/HEIC、AVIF；HEIF 需 Android 8+，AVIF 需 Android 14+ 且取决于系统解码器）"
        }
        val name = "${UUID.randomUUID()}.img"; File(directory, name).writeBytes(bytes); return name
    }

    fun image(name: String, maxDimension: Int = 1600): Bitmap? {
        if (!name.matches(Regex("[a-f0-9-]+\\.img"))) return null
        val bucket = maxDimension.coerceAtLeast(1)
        val key = "$name@$bucket"
        imageCache.get(key)?.let { return it }
        val file = File(directory, name); if (!file.isFile) return null
        val bitmap = PlatformImageDecoder.decodeFile(file, bucket) ?: return null
        return imageCache.put(key, bitmap) ?: bitmap
    }

    /** Bitmap cache counters for the storage page (sizes are in kilobytes internally). */
    fun imageCacheStats(): ImageCacheStats =
        ImageCacheStats(
            entries = imageCache.snapshot().size,
            bytes = imageCache.size().toLong() * 1024L,
            maxBytes = imageCache.maxSize().toLong() * 1024L,
            hits = imageCache.hitCount(),
            misses = imageCache.missCount()
        )

    /** Names of every stored image file, sorted for stable UI counts. */
    fun imageFileNames(): List<String> =
        directory.listFiles { file -> file.isFile && BackupArchive.validImage(file.name) }?.map { it.name }?.sorted().orEmpty()

    /**
     * Image files not referenced by any page (including trashed pages), workspace template,
     * shared block, or page history snapshot. History files are searched as raw bytes
     * because PageHistory stores encoded page snapshots, so an undo can never bring back a
     * page whose image was just deleted.
     */
    fun orphanImages(pages: List<Page>, templates: List<CardTemplate>, shared: List<SharedBlock>): List<String> {
        val referenced = ImageReferences.referenced(pages, templates, shared)
        val candidates = imageFileNames().filterNot { it in referenced }
        if (candidates.isEmpty()) return emptyList()
        val historyFiles = historyDir.listFiles { file -> file.isFile && file.name.endsWith(".history") }.orEmpty()
        val historyBytes = historyFiles.mapNotNull { runCatching { it.readBytes() }.getOrNull() }
        return candidates.filterNot { name ->
            val needle = name.toByteArray(Charsets.UTF_8)
            historyBytes.any { it.containsBytes(needle) }
        }
    }

    fun imageBytes(names: List<String>): Long =
        names.sumOf { name -> File(directory, name).takeIf { it.isFile }?.length() ?: 0L }

    /** Deletes the given image files; returns how many were removed. */
    fun deleteImages(names: List<String>): Int {
        var removed = 0
        names.forEach { name ->
            require(BackupArchive.validImage(name)) { "图片名称无效" }
            val file = File(directory, name)
            if (file.isFile) {
                if (!file.delete()) error("无法删除图片")
                imageCache.snapshot().keys
                    .filter { it == name || it.startsWith("$name@") }
                    .forEach { imageCache.remove(it) }
                removed++
            }
        }
        return removed
    }
}
