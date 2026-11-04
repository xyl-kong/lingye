package com.example.tabletworkspace

enum class WorkspaceView(val storedValue: String) {
    DOCUMENT("document"),
    WHITEBOARD("whiteboard"),
    OVERVIEW("overview"),
    SETTINGS("settings");

    companion object {
        fun fromStored(value: String?): WorkspaceView =
            entries.firstOrNull { it.storedValue == value } ?: DOCUMENT
    }
}

data class WorkspaceSession(
    val pageId: String = "",
    val view: WorkspaceView = WorkspaceView.DOCUMENT,
    val tool: String = "移动",
    val canvasScale: Float = 1f,
    val canvasOffsetX: Float = 0f,
    val canvasOffsetY: Float = 0f,
    val selectedObjectIds: List<String> = emptyList(),
    val documentScroll: Int = 0,
    val documentScrollIndex: Int = 0,
    val pageListIndex: Int = 0,
    val pageListOffset: Int = 0
) {
    fun encode(): Map<String, String> = mapOf(
        WorkspaceSessionCodec.PAGE_ID to pageId,
        WorkspaceSessionCodec.VIEW to view.storedValue,
        WorkspaceSessionCodec.TOOL to tool,
        WorkspaceSessionCodec.SCALE to canvasScale.toString(),
        WorkspaceSessionCodec.OFFSET_X to canvasOffsetX.toString(),
        WorkspaceSessionCodec.OFFSET_Y to canvasOffsetY.toString(),
        WorkspaceSessionCodec.SELECTION_IDS to selectedObjectIds.joinToString(","),
        WorkspaceSessionCodec.DOCUMENT_SCROLL to documentScroll.toString(),
        WorkspaceSessionCodec.DOCUMENT_SCROLL_INDEX to documentScrollIndex.toString(),
        WorkspaceSessionCodec.PAGE_LIST_INDEX to pageListIndex.toString(),
        WorkspaceSessionCodec.PAGE_LIST_OFFSET to pageListOffset.toString()
    )
}

object WorkspaceSessionCodec {
    const val PAGE_ID = "workspace_session_page_id"
    const val VIEW = "workspace_session_view"
    const val TOOL = "workspace_session_tool"
    const val SCALE = "workspace_session_scale"
    const val OFFSET_X = "workspace_session_offset_x"
    const val OFFSET_Y = "workspace_session_offset_y"
    const val SELECTION_IDS = "workspace_session_selection_ids"
    const val DOCUMENT_SCROLL = "workspace_session_document_scroll"
    const val DOCUMENT_SCROLL_INDEX = "workspace_session_document_scroll_index"
    const val PAGE_LIST_INDEX = "workspace_session_page_list_index"
    const val PAGE_LIST_OFFSET = "workspace_session_page_list_offset"

    private val validTools = setOf("移动", "选择", "画笔", "橡皮", "矩形", "圆形", "连线")

    fun decode(values: Map<String, String?>): WorkspaceSession {
        val scale = values[SCALE]?.toFloatOrNull()?.takeIf { it.isFinite() }?.let { CanvasZoom.clamp(it) } ?: 1f
        val offsetX = values[OFFSET_X]?.toFloatOrNull()?.takeIf { it.isFinite() && kotlin.math.abs(it) <= 10_000_000f } ?: 0f
        val offsetY = values[OFFSET_Y]?.toFloatOrNull()?.takeIf { it.isFinite() && kotlin.math.abs(it) <= 10_000_000f } ?: 0f
        val selectedIds = values[SELECTION_IDS].orEmpty()
            .split(',', '\n', ';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(100)
        val documentScroll = values[DOCUMENT_SCROLL]?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val documentScrollIndex = values[DOCUMENT_SCROLL_INDEX]?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val pageListIndex = values[PAGE_LIST_INDEX]?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val pageListOffset = values[PAGE_LIST_OFFSET]?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        return WorkspaceSession(
            pageId = values[PAGE_ID].orEmpty(),
            view = WorkspaceView.fromStored(values[VIEW]),
            tool = values[TOOL]?.takeIf { it in validTools } ?: "移动",
            canvasScale = scale,
            canvasOffsetX = offsetX,
            canvasOffsetY = offsetY,
            selectedObjectIds = selectedIds,
            documentScroll = documentScroll,
            documentScrollIndex = documentScrollIndex,
            pageListIndex = pageListIndex,
            pageListOffset = pageListOffset
        )
    }
}

enum class BackAction {
    DISMISS_OVERLAY,
    FINISH_INLINE_EDIT,
    EXIT_FULLSCREEN,
    LEAVE_SETTINGS,
    LEAVE_OVERVIEW,
    CLEAR_SELECTION,
    RESET_TOOL,
    LEAVE_WHITEBOARD,
    EXIT_APP
}

data class BackState(
    val overlayOpen: Boolean = false,
    val inlineEditing: Boolean = false,
    val fullscreen: Boolean = false,
    val selectionActive: Boolean = false,
    val activeTool: String = "移动",
    val view: WorkspaceView = WorkspaceView.DOCUMENT
)

object BackNavigationPolicy {
    fun resolve(state: BackState): BackAction = when {
        state.overlayOpen -> BackAction.DISMISS_OVERLAY
        state.inlineEditing -> BackAction.FINISH_INLINE_EDIT
        state.fullscreen -> BackAction.EXIT_FULLSCREEN
        state.view == WorkspaceView.SETTINGS -> BackAction.LEAVE_SETTINGS
        state.view == WorkspaceView.OVERVIEW -> BackAction.LEAVE_OVERVIEW
        state.selectionActive -> BackAction.CLEAR_SELECTION
        state.activeTool != "移动" -> BackAction.RESET_TOOL
        state.view == WorkspaceView.WHITEBOARD -> BackAction.LEAVE_WHITEBOARD
        else -> BackAction.EXIT_APP
    }
}
