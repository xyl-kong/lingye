package com.example.tabletworkspace.data.images

import com.example.tabletworkspace.BackupArchive
import com.example.tabletworkspace.CardTemplate
import com.example.tabletworkspace.Page
import com.example.tabletworkspace.SharedBlock
import com.example.tabletworkspace.canvasPage
import com.example.tabletworkspace.imageNames
import java.io.File

/** Unreferenced image files that can be removed from the private image folder. */
data class OrphanImages(val names: List<String>, val bytes: Long) {
    val isEmpty: Boolean get() = names.isEmpty()
}

/** Decoded-bitmap cache counters shown in the storage settings page. */
data class ImageCacheStats(
    val entries: Int,
    val bytes: Long,
    val maxBytes: Long,
    val hits: Int,
    val misses: Int
) {
    val lookups: Int get() = hits + misses
    val hitRate: Float get() = if (lookups == 0) 0f else hits.toFloat() / lookups
}

/**
 * Collects every image name still referenced by workspace content: canvas images and
 * document image blocks on all pages (including trashed pages), workspace template canvas
 * snapshots, and shared blocks including their undo/redo history.
 *
 * Page undo history is not parsed here because PageHistory keeps raw page snapshots;
 * [PageStore.orphanImages] additionally scans history files for the names so undo can
 * never restore a page pointing at a deleted image.
 */
object ImageReferences {
    fun referenced(pages: List<Page>, templates: List<CardTemplate>, shared: List<SharedBlock>): Set<String> {
        val names = mutableSetOf<String>()
        pages.forEach { page ->
            page.images.forEach { if (it.name.isNotEmpty()) names += it.name }
            page.blocks.forEach { if (it.type == "image" && it.content.isNotEmpty()) names += it.content }
        }
        templates.forEach { template ->
            template.canvasPage()?.let { page ->
                page.images.forEach { if (it.name.isNotEmpty()) names += it.name }
                page.blocks.forEach { if (it.type == "image" && it.content.isNotEmpty()) names += it.content }
            }
        }
        shared.forEach { names += it.imageNames().filter { name -> name.isNotEmpty() } }
        return names
    }
}

/** True when [needle] occurs in [haystack]; used to find image names in history snapshots. */
internal fun ByteArray.containsBytes(needle: ByteArray): Boolean {
    if (needle.isEmpty() || needle.size > size) return false
    var start = 0
    val last = size - needle.size
    while (start <= last) {
        var i = 0
        while (i < needle.size && this[start + i] == needle[i]) i++
        if (i == needle.size) return true
        start++
    }
    return false
}

internal fun File.isImageFile(): Boolean = isFile && BackupArchive.validImage(name)
