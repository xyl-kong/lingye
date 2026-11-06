package com.example.tabletworkspace

/**
 * Linked-document cards on the canvas. A link stores the target page's stable UUID in
 * [Shape.body]; the visible title is always resolved from the live page list so renames
 * and deletions are reflected without rewriting the link.
 */
object PageLinks {
    const val SHAPE_TYPE = "pageLink"
    const val CARD_KIND = "linked-doc"
    val ID_PATTERN = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    data class Resolved(val title: String, val missing: Boolean)

    fun targetId(shape: Shape): String? =
        if (shape.type == SHAPE_TYPE && shape.body.isNotBlank() && ID_PATTERN.matches(shape.body)) shape.body else null

    fun resolve(shape: Shape, pages: List<Page>): Resolved {
        val id = targetId(shape) ?: return Resolved(shape.title.ifBlank { "Linked document" }, true)
        val page = pages.firstOrNull { it.id == id && !it.trashed } ?: return Resolved(shape.title.ifBlank { "Linked document" }, true)
        return Resolved(page.title.ifBlank { "Untitled page" }, false)
    }

    /** Source pages that contain at least one live canvas link card pointing at [target]. */
    fun backlinks(target: Page, pages: List<Page>): List<Page> =
        pages.filter { source ->
            source.id != target.id && !source.trashed &&
                source.shapes.any { shape -> targetId(shape) == target.id }
        }

    /**
     * Rewrites linked-document cards after a restore. [idMap] maps the ids in the backup to
     * the new page ids; links to pages outside the backup are left untouched.
     */
    fun remapPageLinks(page: Page, idMap: Map<String, String>) {
        for (i in page.shapes.indices) {
            val shape = page.shapes[i]
            val target = targetId(shape) ?: continue
            val mapped = idMap[target] ?: continue
            page.shapes[i] = shape.copy(body = mapped)
        }
    }

    /** Pages with only canvas content open as whiteboards; anything with text opens as a document. */
    fun opensAsWhiteboard(page: Page, currentWhiteboard: Boolean): Boolean = when {
        page.blocks.isNotEmpty() || page.text.isNotBlank() -> false
        page.shapes.isNotEmpty() || page.connectors.isNotEmpty() || page.images.isNotEmpty() || page.strokes.isNotEmpty() -> true
        else -> currentWhiteboard
    }
}

fun Page.addLinkedDoc(target: Page, x: Float, y: Float, color: Int = 0xfff7f8fb.toInt()): Int {
    require(target.id != id) { "不能链接当前页面" }
    require(PageLinks.ID_PATTERN.matches(target.id)) { "目标页面 ID 无效" }
    shapes.add(
        Shape(
            PageLinks.SHAPE_TYPE, x, y, 260f, 112f, color,
            title = target.title.ifBlank { "Untitled page" },
            body = target.id,
            borderColor = 0xffdfe3ec.toInt(),
            borderStyle = "solid",
            borderWidth = 1f,
            cardKind = PageLinks.CARD_KIND
        )
    )
    return shapes.lastIndex
}
