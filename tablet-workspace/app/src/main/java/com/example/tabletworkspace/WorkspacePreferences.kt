package com.example.tabletworkspace

import androidx.compose.ui.text.font.FontFamily
import java.time.LocalDate

enum class DefaultPageMode(val storedValue: String) {
    DOCUMENT("document"),
    WHITEBOARD("whiteboard");

    companion object {
        fun fromStored(value: String?) = entries.firstOrNull { it.storedValue == value } ?: DOCUMENT
    }
}

data class WorkspaceSettings(
    val themeMode: String = "system",
    val defaultPageMode: DefaultPageMode = DefaultPageMode.DOCUMENT,
    val autoDateTitle: Boolean = false,
    val dateTitleFormat: String = "iso",
    val codeBlockLineNumbers: Boolean = false,
    val documentFullWidth: Boolean = false,
    val displayDocInfo: Boolean = true,
    val editorFontSize: Float = 16f,
    val editorFontFamily: String = "sans",
    val smoothImages: Boolean = true,
    val motionEnabled: Boolean = true,
    val showGrid: Boolean = true,
    val gridSize: String = "medium",
    val snapToGridDefault: Boolean = false,
    val canvasTheme: String = "light",
    val roughConnectors: Boolean = false,
    val defaultTextColor: Int = 0xff20293a.toInt(),
    val defaultTextSize: Float = 16f,
    val defaultTextAlign: String = "left",
    val noteBackground: Int = 0xfff2f5ff.toInt(),
    val noteCorner: String = "medium",
    val noteShadow: String = "box",
    val noteBorderStyle: String = "solid",
    val noteBorderWidth: Float = 1f,
    val showZoomButtons: Boolean = true,
    val showLinkedDocs: Boolean = true,
    val linkReturnToSource: Boolean = true,
    val workspaceName: String = "灵页",
    val workspaceSubtitle: String = "离线创意空间",
    val frameColor: Int = 0xffffffff.toInt(),
    val canvasBackground: Int = 0xffffffff.toInt(),
    val cardColor: Int = 0xfff2f5ff.toInt(),
    val shapeColor: Int = 0xffffd866.toInt(),
    val connectorColor: Int = 0xff62748a.toInt(),
    val connectorWidth: Float = 4f,
    val connectorPathStyle: String = "curve",
    val connectorEndMarker: String = "arrow"
) {
    val gridStep: Float
        get() = when (gridSize) {
            "small" -> 20f
            "large" -> 36f
            else -> 28f
        }

    val noteCornerRadius: Float
        get() = when (noteCorner) {
            "none" -> 0f
            "small" -> 8f
            "large" -> 24f
            "huge" -> 32f
            else -> 16f
        }

    fun normalized() = copy(
        themeMode = themeMode.takeIf { it in THEME_MODES } ?: "system",
        editorFontSize = editorFontSize.coerceIn(12f, 24f),
        editorFontFamily = editorFontFamily.takeIf { it in EDITOR_FONT_FAMILIES } ?: "sans",
        dateTitleFormat = dateTitleFormat.takeIf { it in DATE_TITLE_FORMATS } ?: "iso",
        gridSize = gridSize.takeIf { it in GRID_SIZES } ?: "medium",
        canvasTheme = canvasTheme.takeIf { it in CANVAS_THEMES } ?: "light",
        defaultTextSize = defaultTextSize.coerceIn(12f, 32f),
        defaultTextAlign = defaultTextAlign.takeIf { it in TEXT_ALIGNS } ?: "left",
        noteCorner = noteCorner.takeIf { it in NOTE_CORNERS } ?: "medium",
        noteShadow = noteShadow.takeIf { it in NOTE_SHADOWS } ?: "box",
        noteBorderStyle = noteBorderStyle.takeIf { it in setOf("solid", "dashed", "none") } ?: "solid",
        noteBorderWidth = noteBorderWidth.coerceIn(2f, 12f),
        workspaceName = workspaceName.trim().ifBlank { "灵页" }.take(40),
        workspaceSubtitle = workspaceSubtitle.trim().ifBlank { "离线创意空间" }.take(60),
        connectorWidth = connectorWidth.coerceIn(1f, 8f),
        connectorPathStyle = connectorPathStyle.takeIf { it in CONNECTOR_PATH_STYLES } ?: "curve",
        connectorEndMarker = connectorEndMarker.takeIf { it in CONNECTOR_END_MARKERS } ?: "arrow"
    )

    companion object {
        val THEME_MODES = setOf("system", "light", "dark")
        val CONNECTOR_PATH_STYLES = setOf("straight", "curve", "step")
        val CONNECTOR_END_MARKERS = setOf("dot", "arrow", "none")
        val EDITOR_FONT_FAMILIES = setOf("sans", "serif", "mono")
        val GRID_SIZES = setOf("small", "medium", "large")
        val DATE_TITLE_FORMATS = setOf("iso", "slash", "monthDay", "chinese")
        val CANVAS_THEMES = setOf("auto", "light", "dark")
        val TEXT_ALIGNS = setOf("left", "center", "right")
        val NOTE_CORNERS = setOf("none", "small", "medium", "large", "huge")
        val NOTE_SHADOWS = setOf("none", "box", "sticker")
    }
}

fun newPageTitle(language: AppLanguage, settings: WorkspaceSettings, date: LocalDate = LocalDate.now()): String =
    if (settings.autoDateTitle) formatDateTitle(settings.dateTitleFormat, date) else tr(language, "未命名页面", "Untitled page")

fun formatDateTitle(format: String, date: LocalDate): String = when (format) {
    "slash" -> String.format("%04d/%02d/%02d", date.year, date.monthValue, date.dayOfMonth)
    "monthDay" -> String.format("%02d-%02d", date.monthValue, date.dayOfMonth)
    "chinese" -> "${date.monthValue}月${date.dayOfMonth}日"
    else -> date.toString()
}

fun editorFontFamilyOf(family: String): FontFamily = when (family) {
    "serif" -> FontFamily.Serif
    "mono" -> FontFamily.Monospace
    else -> FontFamily.SansSerif
}

fun WorkspacePreferenceStore.readWorkspaceSettings(legacyMotionEnabled: Boolean? = null): WorkspaceSettings =
    WorkspaceSettings(
        themeMode = string(KEY_THEME_MODE, "system").orEmpty(),
        defaultPageMode = DefaultPageMode.fromStored(string(KEY_DEFAULT_PAGE_MODE, null)),
        autoDateTitle = boolean(KEY_AUTO_DATE_TITLE, false),
        dateTitleFormat = string(KEY_DATE_TITLE_FORMAT, "iso").orEmpty(),
        codeBlockLineNumbers = boolean(KEY_CODE_BLOCK_LINE_NUMBERS, false),
        documentFullWidth = boolean(KEY_DOCUMENT_FULL_WIDTH, false),
        displayDocInfo = boolean(KEY_DISPLAY_DOC_INFO, true),
        editorFontSize = float(KEY_EDITOR_FONT_SIZE, 16f),
        editorFontFamily = string(KEY_EDITOR_FONT_FAMILY, "sans").orEmpty(),
        smoothImages = boolean(KEY_SMOOTH_IMAGES, true),
        motionEnabled = boolean(KEY_MOTION_ENABLED, legacyMotionEnabled ?: true),
        showGrid = boolean(KEY_SHOW_GRID, true),
        gridSize = string(KEY_GRID_SIZE, "medium").orEmpty(),
        snapToGridDefault = boolean(KEY_SNAP_TO_GRID_DEFAULT, false),
        canvasTheme = string(KEY_CANVAS_THEME, "light").orEmpty(),
        roughConnectors = boolean(KEY_ROUGH_CONNECTORS, false),
        defaultTextColor = int(KEY_DEFAULT_TEXT_COLOR, 0xff20293a.toInt()),
        defaultTextSize = float(KEY_DEFAULT_TEXT_SIZE, 16f),
        defaultTextAlign = string(KEY_DEFAULT_TEXT_ALIGN, "left").orEmpty(),
        noteBackground = int(KEY_NOTE_BACKGROUND, 0xfff2f5ff.toInt()),
        noteCorner = string(KEY_NOTE_CORNER, "medium").orEmpty(),
        noteShadow = string(KEY_NOTE_SHADOW, "box").orEmpty(),
        noteBorderStyle = string(KEY_NOTE_BORDER_STYLE, "solid").orEmpty(),
        noteBorderWidth = float(KEY_NOTE_BORDER_WIDTH, 1f),
        showZoomButtons = boolean(KEY_SHOW_ZOOM_BUTTONS, true),
        showLinkedDocs = boolean(KEY_SHOW_LINKED_DOCS, true),
        linkReturnToSource = boolean(KEY_LINK_RETURN_TO_SOURCE, true),
        workspaceName = string(KEY_WORKSPACE_NAME, "灵页").orEmpty(),
        workspaceSubtitle = string(KEY_WORKSPACE_SUBTITLE, "离线创意空间").orEmpty(),
        frameColor = int(KEY_FRAME_COLOR, 0xffffffff.toInt()),
        canvasBackground = int(KEY_CANVAS_BACKGROUND, 0xffffffff.toInt())
            .let { if (it == 0xfffdfdfd.toInt()) 0xffffffff.toInt() else it },
        cardColor = int(KEY_CARD_COLOR, 0xfff2f5ff.toInt()),
        shapeColor = int(KEY_SHAPE_COLOR, 0xffffd866.toInt()),
        connectorColor = int(KEY_CONNECTOR_COLOR, 0xff62748a.toInt()),
        connectorWidth = float(KEY_CONNECTOR_WIDTH, 4f),
        connectorPathStyle = string(KEY_CONNECTOR_PATH_STYLE, "curve").orEmpty(),
        connectorEndMarker = string(KEY_CONNECTOR_END_MARKER, "arrow").orEmpty()
    ).normalized()

fun WorkspacePreferenceStore.writeWorkspaceSettings(settings: WorkspaceSettings) {
    val value = settings.normalized()
    write { put ->
        put.string(KEY_THEME_MODE, value.themeMode)
        put.string(KEY_DEFAULT_PAGE_MODE, value.defaultPageMode.storedValue)
        put.boolean(KEY_AUTO_DATE_TITLE, value.autoDateTitle)
        put.string(KEY_DATE_TITLE_FORMAT, value.dateTitleFormat)
        put.boolean(KEY_CODE_BLOCK_LINE_NUMBERS, value.codeBlockLineNumbers)
        put.boolean(KEY_DOCUMENT_FULL_WIDTH, value.documentFullWidth)
        put.boolean(KEY_DISPLAY_DOC_INFO, value.displayDocInfo)
        put.float(KEY_EDITOR_FONT_SIZE, value.editorFontSize)
        put.string(KEY_EDITOR_FONT_FAMILY, value.editorFontFamily)
        put.boolean(KEY_SMOOTH_IMAGES, value.smoothImages)
        put.boolean(KEY_MOTION_ENABLED, value.motionEnabled)
        put.boolean(KEY_SHOW_GRID, value.showGrid)
        put.string(KEY_GRID_SIZE, value.gridSize)
        put.boolean(KEY_SNAP_TO_GRID_DEFAULT, value.snapToGridDefault)
        put.string(KEY_CANVAS_THEME, value.canvasTheme)
        put.boolean(KEY_ROUGH_CONNECTORS, value.roughConnectors)
        put.int(KEY_DEFAULT_TEXT_COLOR, value.defaultTextColor)
        put.float(KEY_DEFAULT_TEXT_SIZE, value.defaultTextSize)
        put.string(KEY_DEFAULT_TEXT_ALIGN, value.defaultTextAlign)
        put.int(KEY_NOTE_BACKGROUND, value.noteBackground)
        put.string(KEY_NOTE_CORNER, value.noteCorner)
        put.string(KEY_NOTE_SHADOW, value.noteShadow)
        put.string(KEY_NOTE_BORDER_STYLE, value.noteBorderStyle)
        put.float(KEY_NOTE_BORDER_WIDTH, value.noteBorderWidth)
        put.boolean(KEY_SHOW_ZOOM_BUTTONS, value.showZoomButtons)
        put.boolean(KEY_SHOW_LINKED_DOCS, value.showLinkedDocs)
        put.boolean(KEY_LINK_RETURN_TO_SOURCE, value.linkReturnToSource)
        put.string(KEY_WORKSPACE_NAME, value.workspaceName)
        put.string(KEY_WORKSPACE_SUBTITLE, value.workspaceSubtitle)
        put.int(KEY_FRAME_COLOR, value.frameColor)
        put.int(KEY_CANVAS_BACKGROUND, value.canvasBackground)
        put.int(KEY_CARD_COLOR, value.cardColor)
        put.int(KEY_SHAPE_COLOR, value.shapeColor)
        put.int(KEY_CONNECTOR_COLOR, value.connectorColor)
        put.float(KEY_CONNECTOR_WIDTH, value.connectorWidth)
        put.string(KEY_CONNECTOR_PATH_STYLE, value.connectorPathStyle)
        put.string(KEY_CONNECTOR_END_MARKER, value.connectorEndMarker)
    }
}

private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_DEFAULT_PAGE_MODE = "default_page_mode"
private const val KEY_AUTO_DATE_TITLE = "auto_date_title"
private const val KEY_DATE_TITLE_FORMAT = "date_title_format"
private const val KEY_CODE_BLOCK_LINE_NUMBERS = "code_block_line_numbers"
private const val KEY_DOCUMENT_FULL_WIDTH = "document_full_width"
private const val KEY_DISPLAY_DOC_INFO = "display_doc_info"
private const val KEY_EDITOR_FONT_SIZE = "editor_font_size"
private const val KEY_EDITOR_FONT_FAMILY = "editor_font_family"
private const val KEY_SMOOTH_IMAGES = "smooth_images"
private const val KEY_MOTION_ENABLED = "motion_enabled"
private const val KEY_SHOW_GRID = "show_grid"
private const val KEY_GRID_SIZE = "grid_size"
private const val KEY_SNAP_TO_GRID_DEFAULT = "snap_to_grid_default"
private const val KEY_CANVAS_THEME = "canvas_theme"
private const val KEY_ROUGH_CONNECTORS = "rough_connectors"
private const val KEY_DEFAULT_TEXT_COLOR = "default_text_color"
private const val KEY_DEFAULT_TEXT_SIZE = "default_text_size"
private const val KEY_DEFAULT_TEXT_ALIGN = "default_text_align"
private const val KEY_NOTE_BACKGROUND = "note_background"
private const val KEY_NOTE_CORNER = "note_corner"
private const val KEY_NOTE_SHADOW = "note_shadow"
private const val KEY_NOTE_BORDER_STYLE = "note_border_style"
private const val KEY_NOTE_BORDER_WIDTH = "note_border_width"
private const val KEY_SHOW_ZOOM_BUTTONS = "show_zoom_buttons"
private const val KEY_SHOW_LINKED_DOCS = "show_linked_docs"
private const val KEY_LINK_RETURN_TO_SOURCE = "link_return_to_source"
private const val KEY_WORKSPACE_NAME = "workspace_name"
private const val KEY_WORKSPACE_SUBTITLE = "workspace_subtitle"
private const val KEY_FRAME_COLOR = "frame_color"
private const val KEY_CANVAS_BACKGROUND = "canvas_background"
private const val KEY_CARD_COLOR = "card_color"
private const val KEY_SHAPE_COLOR = "shape_color"
private const val KEY_CONNECTOR_COLOR = "connector_color"
private const val KEY_CONNECTOR_WIDTH = "connector_width"
private const val KEY_CONNECTOR_PATH_STYLE = "connector_path_style"
private const val KEY_CONNECTOR_END_MARKER = "connector_end_marker"
