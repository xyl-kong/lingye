package com.example.tabletworkspace.core

import com.example.tabletworkspace.Page
import com.example.tabletworkspace.SharedBlock
import com.example.tabletworkspace.searchText
import java.util.Locale

/**
 * Lower-cased searchable text per page, rebuilt only when the page revision (or the set of
 * shared blocks) changes. Quick-search keystrokes reuse the index instead of lowercasing
 * every page body and block on each key press.
 */
class PageSearchIndex {
    private var revision: Int = Int.MIN_VALUE
    private var sharedCount: Int = -1
    private var entries: Map<String, List<String>> = emptyMap()

    var buildCount: Int = 0
        private set

    fun prepare(pages: List<Page>, sharedBlocks: List<SharedBlock>, pageRevision: Int) {
        if (pageRevision == revision && sharedBlocks.size == sharedCount) return
        revision = pageRevision
        sharedCount = sharedBlocks.size
        buildCount++
        entries = pages.associate { page -> page.id to searchableFields(page, sharedBlocks) }
    }

    fun contains(pageId: String, lowerNeedle: String): Boolean =
        entries[pageId]?.any { it.contains(lowerNeedle) } == true

    val size: Int
        get() = entries.size

    private companion object {
        fun searchableFields(page: Page, sharedBlocks: List<SharedBlock>): List<String> {
            val fields = mutableListOf(page.title.lowercase(Locale.ROOT), page.text.lowercase(Locale.ROOT))
            page.blocks.forEach { block ->
                when (block.type) {
                    "image" -> Unit
                    "shared" -> sharedBlocks.firstOrNull { it.id == block.content }
                        ?.searchText()?.lowercase(Locale.ROOT)?.let { fields += it }
                    else -> fields += block.content.lowercase(Locale.ROOT)
                }
            }
            page.shapes.forEach { shape ->
                if (shape.type == "card") {
                    fields += shape.title.lowercase(Locale.ROOT)
                    fields += shape.body.lowercase(Locale.ROOT)
                }
            }
            return fields
        }
    }
}

/** Same filters as [filterPages] but uses a prebuilt [PageSearchIndex] for the query part. */
fun filterPagesIndexed(
    pages: List<Page>,
    index: PageSearchIndex,
    query: String,
    folder: String?,
    trash: Boolean,
    tag: String? = null,
    favoritesOnly: Boolean = false
): List<Page> {
    val needle = query.trim().lowercase(Locale.ROOT)
    return pages.filter { page ->
        page.trashed == trash &&
            (!favoritesOnly || page.favorite) &&
            (folder == null || if (folder.isEmpty()) page.folders.isEmpty() else folder in page.folders) &&
            (tag == null || tag in page.tags) &&
            (needle.isEmpty() || index.contains(page.id, needle))
    }
}
