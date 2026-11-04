package com.example.tabletworkspace.ui.model

import com.example.tabletworkspace.Page
import com.example.tabletworkspace.PageLinks

/**
 * Immutable snapshot of the fields a page row renders. Rows are keyed by page id and
 * receive a new model whenever the page revision changes, so a rename/icon/favorite/tag
 * update recomposes only that row instead of remounting it through a title-based key.
 */
data class PageRowModel(
    val id: String,
    val title: String,
    val tags: List<String>,
    val favorite: Boolean,
    val trashed: Boolean,
    val folders: List<String>,
    val icon: String,
    val canvasIcon: Boolean
) {
    companion object {
        fun of(page: Page) = PageRowModel(
            id = page.id,
            title = page.title,
            tags = page.tags.toList(),
            favorite = page.favorite,
            trashed = page.trashed,
            folders = page.folders.toList(),
            icon = page.icon,
            canvasIcon = PageLinks.opensAsWhiteboard(page, false)
        )
    }
}
