package com.example.tabletworkspace.ui.model

import com.example.tabletworkspace.Page
import com.example.tabletworkspace.PageLinks

/** Immutable display snapshot for one home-list row/card; changes drive recomposition. */
internal data class DocumentRowModel(
    val id: String,
    val title: String,
    val preview: String,
    val icon: String,
    val canvasIcon: Boolean,
    val favorite: Boolean,
    val folders: List<String>,
    val modifiedTime: Long?
)

internal fun documentRowModel(page: Page, modifiedTime: Long?): DocumentRowModel = DocumentRowModel(
    id = page.id,
    title = page.title,
    preview = page.previewText(),
    icon = page.icon,
    canvasIcon = PageLinks.opensAsWhiteboard(page, false),
    favorite = page.favorite,
    folders = page.folders.toList(),
    modifiedTime = modifiedTime
)

internal fun Page.previewText(): String {
    val value = text.ifBlank { blocks.firstOrNull { it.content.isNotBlank() }?.content.orEmpty() }
    return value.replace('\n', ' ').take(220)
}
