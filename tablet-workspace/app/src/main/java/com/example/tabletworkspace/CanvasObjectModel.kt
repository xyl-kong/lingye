package com.example.tabletworkspace

/**
 * Canonical object kinds for the canvas. This is the single place that maps the
 * serialized [Shape]/[Connector]/[CanvasImage]/stroke lists to a semantic object type.
 * It does not change the page format; all information is derived from existing fields.
 */
enum class CanvasObjectKind(val id: String) {
    PAGE_CARD("page-card"),
    TEXT("text"),
    NOTE("note"),
    SHAPE("shape"),
    IMAGE("image"),
    CONNECTOR("connector"),
    STROKE("stroke"),
    FRAME("frame"),
    MINDMAP("mindmap"),
    LINK("link");

    companion object {
        fun fromId(id: String): CanvasObjectKind? = entries.firstOrNull { it.id == id }
    }
}

/**
 * Capabilities are intentionally semantic, not button IDs. A toolbar/host can ask
 * whether an object kind supports editing/formatting without inspecting Shape fields.
 */
enum class CanvasCapability {
    EDIT_TEXT, TEXT_SELECTION, FONT_FAMILY, FONT_STYLE, FONT_WEIGHT, FONT_SIZE, TEXT_COLOR, TEXT_ALIGN,
    FILL, BORDER_COLOR, BORDER_STYLE, BORDER_WIDTH, CORNER_RADIUS, SHADOW,
    CROP, ROTATE, FLIP, REPLACE_IMAGE,
    PATH_STYLE, ENDPOINTS, LABEL, LABEL_STYLE, ANIMATION, ROUGH, COLOR, WIDTH,
    STYLUS_ONLY, PRESSURE, SMOOTHING, ERASE_STROKE, ERASE_AREA, ERASE_PIXEL,
    FRAME_RATIO, FRAME_BACKGROUND, FRAME_MEMBERS, FRAME_NAVIGATOR, FRAME_PRESENTATION, FRAME_NESTING,
    MINDMAP_LAYOUT, MINDMAP_STYLE, MINDMAP_CHILD, MINDMAP_SIBLING, MINDMAP_REPARENT, MINDMAP_DELETE,
    LINK_OPEN, BACKLINK, TEMPLATE, INSERT_TO_PAGE,
    DUPLICATE, LAYER, LOCK, DELETE, GROUP
}

object CanvasObjectSpecs {

    fun kindOf(page: Page, selection: CanvasSelection?): CanvasObjectKind? {
        if (selection == null) return null
        return when (selection.type) {
            "shape" -> page.shapes.getOrNull(selection.index)?.let { shape ->
                when (shape.type) {
                    "text" -> CanvasObjectKind.TEXT
                    "card" -> CanvasObjectKind.NOTE
                    "frame" -> CanvasObjectKind.FRAME
                    PageLinks.SHAPE_TYPE -> CanvasObjectKind.LINK
                    Mindmap.NODE_TYPE -> CanvasObjectKind.MINDMAP
                    else -> CanvasObjectKind.SHAPE
                }
            }
            "image" -> CanvasObjectKind.IMAGE
            "connector" -> CanvasObjectKind.CONNECTOR
            "stroke" -> CanvasObjectKind.STROKE
            else -> null
        }
    }

    fun capabilities(kind: CanvasObjectKind): Set<CanvasCapability> = when (kind) {
        CanvasObjectKind.PAGE_CARD -> setOf(
            CanvasCapability.EDIT_TEXT, CanvasCapability.DUPLICATE
        )
        CanvasObjectKind.TEXT -> setOf(
            CanvasCapability.EDIT_TEXT, CanvasCapability.TEXT_SELECTION,
            CanvasCapability.FONT_FAMILY, CanvasCapability.FONT_STYLE, CanvasCapability.FONT_WEIGHT,
            CanvasCapability.FONT_SIZE, CanvasCapability.TEXT_COLOR, CanvasCapability.TEXT_ALIGN,
            CanvasCapability.DUPLICATE, CanvasCapability.LAYER, CanvasCapability.LOCK, CanvasCapability.DELETE
        )
        CanvasObjectKind.NOTE -> setOf(
            CanvasCapability.EDIT_TEXT, CanvasCapability.FILL, CanvasCapability.SHADOW,
            CanvasCapability.CORNER_RADIUS, CanvasCapability.BORDER_STYLE, CanvasCapability.BORDER_WIDTH,
            CanvasCapability.TEMPLATE, CanvasCapability.INSERT_TO_PAGE,
            CanvasCapability.DUPLICATE, CanvasCapability.LAYER, CanvasCapability.LOCK, CanvasCapability.DELETE
        )
        CanvasObjectKind.SHAPE -> setOf(
            CanvasCapability.EDIT_TEXT, CanvasCapability.FILL, CanvasCapability.BORDER_COLOR,
            CanvasCapability.BORDER_STYLE, CanvasCapability.BORDER_WIDTH, CanvasCapability.CORNER_RADIUS,
            CanvasCapability.FONT_FAMILY, CanvasCapability.FONT_STYLE, CanvasCapability.FONT_WEIGHT,
            CanvasCapability.FONT_SIZE, CanvasCapability.TEXT_COLOR, CanvasCapability.TEXT_ALIGN,
            CanvasCapability.ROTATE, CanvasCapability.DUPLICATE, CanvasCapability.LAYER,
            CanvasCapability.LOCK, CanvasCapability.DELETE
        )
        CanvasObjectKind.IMAGE -> setOf(
            CanvasCapability.CROP, CanvasCapability.ROTATE, CanvasCapability.FLIP,
            CanvasCapability.REPLACE_IMAGE, CanvasCapability.DUPLICATE, CanvasCapability.LAYER,
            CanvasCapability.LOCK, CanvasCapability.DELETE
        )
        CanvasObjectKind.CONNECTOR -> setOf(
            CanvasCapability.PATH_STYLE, CanvasCapability.ENDPOINTS, CanvasCapability.LABEL,
            CanvasCapability.LABEL_STYLE, CanvasCapability.ANIMATION, CanvasCapability.ROUGH,
            CanvasCapability.COLOR, CanvasCapability.WIDTH, CanvasCapability.LAYER, CanvasCapability.DELETE
        )
        CanvasObjectKind.STROKE -> setOf(
            CanvasCapability.COLOR, CanvasCapability.WIDTH, CanvasCapability.LAYER,
            CanvasCapability.LOCK, CanvasCapability.DELETE
        )
        CanvasObjectKind.FRAME -> setOf(
            CanvasCapability.FRAME_RATIO, CanvasCapability.FRAME_BACKGROUND, CanvasCapability.FRAME_MEMBERS,
            CanvasCapability.FRAME_NAVIGATOR, CanvasCapability.FRAME_PRESENTATION, CanvasCapability.FRAME_NESTING,
            CanvasCapability.DUPLICATE, CanvasCapability.LOCK, CanvasCapability.DELETE
        )
        CanvasObjectKind.MINDMAP -> setOf(
            CanvasCapability.EDIT_TEXT, CanvasCapability.MINDMAP_LAYOUT, CanvasCapability.MINDMAP_STYLE,
            CanvasCapability.MINDMAP_CHILD, CanvasCapability.MINDMAP_SIBLING, CanvasCapability.MINDMAP_REPARENT,
            CanvasCapability.MINDMAP_DELETE, CanvasCapability.DUPLICATE, CanvasCapability.DELETE
        )
        CanvasObjectKind.LINK -> setOf(
            CanvasCapability.LINK_OPEN, CanvasCapability.BACKLINK, CanvasCapability.DUPLICATE,
            CanvasCapability.LAYER, CanvasCapability.DELETE
        )
    }

    fun supports(kind: CanvasObjectKind, capability: CanvasCapability): Boolean =
        capability in capabilities(kind)

    /** Only shape-backed objects have a persisted lock flag in page formats 24 and later. */
    fun supportsLock(page: Page, selection: CanvasSelection): Boolean =
        selection.type == "shape" && page.shapes.getOrNull(selection.index) != null

    fun isLocked(page: Page, selection: CanvasSelection): Boolean =
        selection.type == "shape" && page.shapes.getOrNull(selection.index)?.locked == true

    fun hasLocked(page: Page, items: Collection<CanvasSelection>): Boolean =
        items.any { isLocked(page, it) }

    fun unlocked(page: Page, items: Collection<CanvasSelection>): List<CanvasSelection> =
        items.filterNot { isLocked(page, it) }
}

fun Page.hasLocked(items: Collection<CanvasSelection>): Boolean =
    CanvasObjectSpecs.hasLocked(this, items)

fun Page.unlocked(items: Collection<CanvasSelection>): List<CanvasSelection> =
    CanvasObjectSpecs.unlocked(this, items)
