package com.example.tabletworkspace

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp

/** 画布上所有对象工具条的宿主：按当前单选对象类型渲染对应工具条，并在文本选区变化时叠加格式浮层。 */
@Composable
internal fun CanvasObjectToolbarHost(
    page: Page,
    pages: List<Page>,
    language: AppLanguage,
    scale: Float,
    viewport: Viewport,
    canvasWidthDp: Dp,
    canvasHeightDp: Dp,
    imeBottomDp: Dp,
    objectKind: CanvasObjectKind?,
    tool: String,
    toolbarMenuOpen: Boolean,
    dragging: Boolean,
    geometryRevision: Int,
    selectedItemsCount: Int,
    selectedCardIndex: Int?,
    selectedShapeIndex: Int?,
    selectedPageLinkIndex: Int?,
    selectedMindNodeIndex: Int?,
    selectedFrameIndex: Int?,
    selectedTextIndex: Int?,
    selectedImageIndex: Int?,
    selectedStrokeIndex: Int?,
    selectedConnectorIndex: Int?,
    cardEdit: Int?,
    textEditValueState: MutableState<TextFieldValue?>,
    onCardEditChange: (Int?) -> Unit,
    onSaveTemplate: (Int) -> Unit,
    onSelect: (CanvasSelection?, List<CanvasSelection>) -> Unit,
    onChanged: () -> Unit,
    onOpenLinkedPage: (String) -> Unit,
    onParentPicker: (String) -> Unit,
    onDeleteNode: (String) -> Unit,
    onRenameFrame: (Int, String) -> Unit,
    onFitFrame: (Shape) -> Unit,
    onFrameNavigator: () -> Unit,
    onPresentFrame: (Int) -> Unit,
    onCropImage: (String, Float, Float, Float, Float) -> Unit,
    onDrawConnector: (Int) -> Unit,
    hideKeyboard: () -> Unit,
    changedText: (String?) -> Unit
) {
    val standardVisible = tool == "选择" && !toolbarMenuOpen && !dragging
    CanvasCardToolbar(
        index = selectedCardIndex,
        visible = selectedCardIndex != null && standardVisible,
        page = page, language = language, scale = scale, viewport = viewport,
        canvasWidthDp = canvasWidthDp, canvasHeightDp = canvasHeightDp, imeBottomDp = imeBottomDp,
        objectKind = objectKind,
        onEditCard = onCardEditChange,
        onSaveTemplate = onSaveTemplate,
        onSelect = onSelect,
        onChanged = onChanged
    )
    CanvasShapeToolbar(
        index = selectedShapeIndex,
        visible = selectedShapeIndex != null && standardVisible,
        page = page, language = language, scale = scale, viewport = viewport,
        canvasWidthDp = canvasWidthDp, canvasHeightDp = canvasHeightDp, imeBottomDp = imeBottomDp,
        objectKind = objectKind,
        onDrawConnector = onDrawConnector,
        onSelect = onSelect,
        onChanged = onChanged
    )
    CanvasPageLinkToolbar(
        index = selectedPageLinkIndex,
        visible = selectedPageLinkIndex != null && standardVisible,
        page = page,
        pages = pages,
        language = language,
        scale = scale,
        viewport = viewport,
        canvasWidthDp = canvasWidthDp,
        canvasHeightDp = canvasHeightDp,
        imeBottomDp = imeBottomDp,
        objectKind = objectKind,
        onOpenLinkedPage = onOpenLinkedPage,
        onSelect = onSelect,
        onChanged = onChanged
    )
    CanvasMindNodeToolbar(
        index = selectedMindNodeIndex,
        visible = selectedMindNodeIndex != null && standardVisible,
        page = page,
        language = language,
        scale = scale,
        viewport = viewport,
        canvasWidthDp = canvasWidthDp,
        canvasHeightDp = canvasHeightDp,
        imeBottomDp = imeBottomDp,
        objectKind = objectKind,
        onEditCard = onCardEditChange,
        onParentPicker = onParentPicker,
        onDeleteNode = onDeleteNode,
        onChanged = onChanged
    )
    CanvasFrameToolbar(
        index = selectedFrameIndex,
        visible = selectedFrameIndex != null && standardVisible,
        page = page,
        language = language,
        scale = scale,
        viewport = viewport,
        canvasWidthDp = canvasWidthDp,
        canvasHeightDp = canvasHeightDp,
        imeBottomDp = imeBottomDp,
        objectKind = objectKind,
        onRenameFrame = onRenameFrame,
        onFitFrame = onFitFrame,
        onFrameNavigator = onFrameNavigator,
        onPresentFrame = onPresentFrame,
        onSelect = onSelect,
        onChanged = onChanged
    )
    val editShape = cardEdit?.let { page.shapes.getOrNull(it) }
    val editValue = textEditValueState.value
    val editSelectionStart = editValue?.selection?.let { minOf(it.start, it.end) } ?: 0
    val editSelectionEnd = editValue?.selection?.let { maxOf(it.start, it.end) } ?: 0
    if (cardEdit != null && editShape?.type == "text" && editValue != null &&
        editSelectionStart < editSelectionEnd && !toolbarMenuOpen
    ) {
        CanvasTextFormattingToolbar(
            page = page,
            pages = pages,
            language = language,
            scale = scale,
            viewport = viewport,
            canvasWidthDp = canvasWidthDp,
            canvasHeightDp = canvasHeightDp,
            imeBottomDp = imeBottomDp,
            objectKind = objectKind,
            editIndex = cardEdit,
            editShape = editShape,
            editValue = editValue,
            editSelectionStart = editSelectionStart,
            editSelectionEnd = editSelectionEnd,
            textEditValueState = textEditValueState,
            hideKeyboard = hideKeyboard,
            onChanged = onChanged,
            onCardEditChange = onCardEditChange,
            onSelectionChange = onSelect,
            changedText = changedText
        )
    }
    CanvasTextToolbar(
        index = selectedTextIndex,
        visible = selectedTextIndex != null && standardVisible && cardEdit == null &&
            page.shapes.getOrNull(selectedTextIndex ?: -1)?.locked != true,
        page = page, language = language, scale = scale, viewport = viewport,
        canvasWidthDp = canvasWidthDp, canvasHeightDp = canvasHeightDp, imeBottomDp = imeBottomDp,
        objectKind = objectKind,
        onSelect = onSelect,
        onChanged = onChanged,
        onDrawConnector = onDrawConnector
    )
    val adjacentIndex = selectedTextIndex ?: selectedCardIndex
    val adjacentAnchor = remember(geometryRevision, adjacentIndex) {
        adjacentIndex?.let { i ->
            page.shapes.getOrNull(i)?.takeIf { it.type == "text" || it.type == "card" }?.let { shape ->
                AdjacentTextAnchor(shape.x, shape.y, shape.width, shape.height, shape.locked)
            }
        }
    }
    CanvasTextAdjacentControls(
        index = adjacentIndex,
        anchor = adjacentAnchor,
        selectedItemsCount = selectedItemsCount,
        cardEditActive = cardEdit != null,
        toolIsSelect = tool == "选择",
        toolbarMenuOpen = toolbarMenuOpen,
        dragging = dragging,
        page = page, language = language, scale = scale, viewport = viewport,
        canvasWidthDp = canvasWidthDp, canvasHeightDp = canvasHeightDp,
        onSelect = onSelect,
        onEditCard = onCardEditChange,
        onChanged = onChanged
    )
    CanvasImageToolbar(
        index = selectedImageIndex,
        visible = selectedImageIndex != null && standardVisible,
        page = page,
        language = language,
        scale = scale,
        viewport = viewport,
        canvasWidthDp = canvasWidthDp,
        canvasHeightDp = canvasHeightDp,
        imeBottomDp = imeBottomDp,
        objectKind = objectKind,
        onCropImage = onCropImage,
        onSelect = onSelect,
        onChanged = onChanged
    )
    CanvasStrokeToolbar(
        index = selectedStrokeIndex,
        visible = selectedStrokeIndex != null && standardVisible,
        page = page,
        language = language,
        scale = scale,
        viewport = viewport,
        canvasWidthDp = canvasWidthDp,
        canvasHeightDp = canvasHeightDp,
        imeBottomDp = imeBottomDp,
        objectKind = objectKind,
        onSelect = onSelect,
        onChanged = onChanged
    )
    CanvasConnectorToolbar(
        index = selectedConnectorIndex,
        visible = selectedConnectorIndex != null && standardVisible,
        page = page,
        language = language,
        viewport = viewport,
        canvasWidthDp = canvasWidthDp,
        canvasHeightDp = canvasHeightDp,
        imeBottomDp = imeBottomDp,
        objectKind = objectKind,
        onSelect = onSelect,
        onChanged = onChanged,
        changedText = changedText
    )
}
