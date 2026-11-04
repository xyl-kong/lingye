package com.example.tabletworkspace

import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize

/**
 * Shared state for the canvas editor. Owns the gesture, selection, menu and dialog
 * state so the gesture handlers and dialog hosts can be implemented outside the
 * CanvasEditor composable without getter/setter adapters.
 */
internal class CanvasEditorState(
    page: Page,
    initialScale: Float,
    initialOffsetX: Float,
    initialOffsetY: Float
) {
    val scale: MutableFloatState = mutableFloatStateOf(initialScale)
    val pan: MutableState<Offset> = mutableStateOf(Offset(initialOffsetX, initialOffsetY))
    val points: MutableState<List<Point>> = mutableStateOf(emptyList())
    val selection: MutableState<CanvasSelection?> = StableSelectionDelegate(page)
    val selectedItems: MutableState<List<CanvasSelection>> = StableSelectionListDelegate(page)
    val multiSelect: MutableState<Boolean> = mutableStateOf(false)
    val endpointDrag: MutableState<Boolean?> = mutableStateOf(null)
    val endpointPreview: MutableState<Point?> = mutableStateOf(null)
    val endpointSnap: MutableState<ConnectorSnapResult?> = mutableStateOf(null)
    val connectorDraft: MutableState<ConnectorDraft?> = mutableStateOf(null)
    val connectorQuickSource: MutableState<Int?> = mutableStateOf(null)
    val snapGuides: MutableState<List<SnapGuide>> = mutableStateOf(emptyList())
    val dragSnapStart: MutableState<CanvasBounds?> = mutableStateOf(null)
    val dragSnapOthers: MutableState<List<CanvasBounds>> = mutableStateOf(emptyList())
    val dragSnapAccum: MutableState<Point> = mutableStateOf(Point(0f, 0f))
    val resizeAnchor: MutableState<Point?> = mutableStateOf(null)
    val resizeSnapshot: MutableState<FreeResizeSnapshot?> = mutableStateOf(null)
    val resizeGrabOffset: MutableState<Point> = mutableStateOf(Point(0f, 0f))
    val resizeAxis: MutableState<String?> = mutableStateOf(null)
    val boxStart: MutableState<Point?> = mutableStateOf(null)
    val boxEnd: MutableState<Point?> = mutableStateOf(null)
    val dragging: MutableState<Boolean> = mutableStateOf(false)
    val cropTarget: MutableState<String?> = mutableStateOf(null)
    val cropLeft: MutableFloatState = mutableFloatStateOf(0f)
    val cropTop: MutableFloatState = mutableFloatStateOf(0f)
    val cropRight: MutableFloatState = mutableFloatStateOf(1f)
    val cropBottom: MutableFloatState = mutableFloatStateOf(1f)
    val cardEdit: MutableState<Int?> = mutableStateOf(null)
    val textEditValue: MutableState<TextFieldValue?> = mutableStateOf(null)
    val frameRename: MutableState<Int?> = mutableStateOf(null)
    val frameTitleText: MutableState<String> = mutableStateOf("")
    val frameNavigator: MutableState<Boolean> = mutableStateOf(false)
    val presentingFrameId: MutableState<String?> = mutableStateOf(null)
    val mindmapParentPicker: MutableState<String?> = mutableStateOf(null)
    val mindmapDeleteId: MutableState<String?> = mutableStateOf(null)
    val manageTemplates: MutableState<Boolean> = mutableStateOf(false)
    val renameTemplate: MutableState<Int?> = mutableStateOf(null)
    val removeTemplate: MutableState<Int?> = mutableStateOf(null)
    val templateName: MutableState<String> = mutableStateOf("")
    val templateSave: MutableState<Int?> = mutableStateOf(null)
    val saveSelectionTemplate: MutableState<Boolean> = mutableStateOf(false)
    val templateGalleryOpen: MutableState<Boolean> = mutableStateOf(false)
    val templateGalleryCategory: MutableState<String> = mutableStateOf(TemplateGalleryData.categories.first().key)
    val moreToolsMenu: MutableState<Boolean> = mutableStateOf(false)
    val textToolsMenu: MutableState<Boolean> = mutableStateOf(false)
    val pendingTextKind: MutableState<String> = mutableStateOf("body")
    val linkedDocPicker: MutableState<Boolean> = mutableStateOf(false)
    val mindmapToolsMenu: MutableState<Boolean> = mutableStateOf(false)
    val activeMenuButton: MutableState<String?> = mutableStateOf(null)
    val selectionLayerMenu: MutableState<Boolean> = mutableStateOf(false)

    // Viewport and Frame-presentation bookkeeping used by the bottom bar, zoom controls
    // and CanvasEditorCommands.
    val viewportSizePx: MutableState<IntSize> = mutableStateOf(IntSize(0, 0))
    val frameRatio: MutableState<String> = mutableStateOf("custom")
    val lastNavigationTool: MutableState<String> = mutableStateOf("选择")
    val selectionRestored: MutableState<Boolean> = mutableStateOf(false)
    val presentationWasFocus: MutableState<Boolean> = mutableStateOf(false)
    val presentationPrevTool: MutableState<String> = mutableStateOf("选择")
    val presentationPrevScale: MutableFloatState = mutableFloatStateOf(1f)
    val presentationPrevPan: MutableState<Offset> = mutableStateOf(Offset.Zero)
    val presentationFitMode: MutableState<String> = mutableStateOf("fill")
}
