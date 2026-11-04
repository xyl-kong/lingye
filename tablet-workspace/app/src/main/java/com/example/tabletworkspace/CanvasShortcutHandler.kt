@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.tabletworkspace

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.geometry.Offset

internal class CanvasShortcutActions(
    val stopPresentation: () -> Unit,
    val shiftPresentation: (Int) -> Unit,
    val closeOverlays: () -> Unit,
    val duplicateSelection: () -> Unit,
    val groupSelection: (Boolean) -> Unit,
    val reorderSelection: (Boolean) -> Unit,
    val alignSelection: (String) -> Unit,
    val nudgeSelection: (Float, Float) -> Unit,
    val undo: () -> Unit,
    val redo: () -> Unit,
    val deleteSelection: () -> Unit,
    val zoomBy: (Float) -> Unit,
    val selectAll: () -> Unit,
    val dismissMenus: () -> Unit,
    val setTool: (String) -> Unit,
    val chooseTool: (String) -> Unit,
    val selectMenuOption: (String) -> Unit,
    val setBrushKind: (String) -> Unit,
    val setFocusMode: (Boolean) -> Unit
)

internal fun handleCanvasShortcutEvent(
    event: KeyEvent,
    state: CanvasEditorState,
    tool: String,
    focusMode: Boolean,
    actions: CanvasShortcutActions
): Boolean {
        if (event.type != KeyEventType.KeyDown) return false
        if (state.presentingFrameId.value != null) {
            if (event.key == Key.Escape) { actions.stopPresentation(); return true }
            if (event.key == Key.DirectionLeft || event.key == Key.PageUp) { actions.shiftPresentation(-1); return true }
            if (event.key == Key.DirectionRight || event.key == Key.PageDown || event.key == Key.Spacebar) { actions.shiftPresentation(1); return true }
            return false
        }
        val overlay = state.moreToolsMenu.value || state.textToolsMenu.value || state.mindmapToolsMenu.value || state.templateGalleryOpen.value || state.manageTemplates.value ||
            state.renameTemplate.value != null || state.removeTemplate.value != null || state.templateSave.value != null || state.saveSelectionTemplate.value ||
            state.cropTarget.value != null || state.frameNavigator.value || state.frameRename.value != null || state.linkedDocPicker.value || state.mindmapParentPicker.value != null || state.mindmapDeleteId.value != null
        val ctrl = event.isCtrlPressed || event.isMetaPressed
        val shift = event.isShiftPressed
        val arrow = when (event.key) {
            Key.DirectionLeft -> CanvasShortcuts.Action.NUDGE_LEFT
            Key.DirectionRight -> CanvasShortcuts.Action.NUDGE_RIGHT
            Key.DirectionUp -> CanvasShortcuts.Action.NUDGE_UP
            Key.DirectionDown -> CanvasShortcuts.Action.NUDGE_DOWN
            else -> null
        }
        val letter = when (event.key) {
            Key.Delete, Key.Backspace -> '\u007F'
            Key.Escape -> '\u001B'
            Key.Z -> 'Z'
            Key.Y -> 'Y'
            Key.A -> 'A'
            Key.D -> 'D'
            Key.G -> 'G'
            Key.B -> 'B'
            Key.R -> 'R'
            Key.V -> 'V'
            Key.H -> 'H'
            Key.F -> 'F'
            Key.C -> 'C'
            Key.T -> 'T'
            Key.P -> 'P'
            Key.E -> 'E'
            Key.S -> 'S'
            Key.M -> 'M'
            Key.L -> 'L'
            Key.Equals -> '='
            Key.Minus -> '-'
            Key.Zero -> '0'
            Key.At -> '@'
            Key.LeftBracket -> '['
            Key.RightBracket -> ']'
            else -> event.utf16CodePoint.toChar()
        }
        if (overlay) {
            if (letter == '\u001B') { actions.closeOverlays(); return true }
            return false
        }
        if (state.cardEdit.value != null) {
            if (letter == '\u001B') { state.cardEdit.value = null; return true }
            return false
        }
        if (ctrl && letter == 'D' && !shift) { actions.duplicateSelection(); return true }
        if (ctrl && letter == 'G') { actions.groupSelection(shift); return true }
        if (ctrl && letter == ']' && !shift) { actions.reorderSelection(true); return true }
        if (ctrl && letter == '[' && !shift) { actions.reorderSelection(false); return true }
        if (ctrl && shift) {
            when (letter) {
                'L' -> { actions.alignSelection("left"); return true }
                'R' -> { actions.alignSelection("right"); return true }
                'T' -> { actions.alignSelection("top"); return true }
                'B' -> { actions.alignSelection("bottom"); return true }
            }
        }
        if (arrow != null) {
            val step = if (shift) 1f else 8f
            when (arrow) {
                CanvasShortcuts.Action.NUDGE_LEFT -> actions.nudgeSelection(-step, 0f)
                CanvasShortcuts.Action.NUDGE_RIGHT -> actions.nudgeSelection(step, 0f)
                CanvasShortcuts.Action.NUDGE_UP -> actions.nudgeSelection(0f, -step)
                CanvasShortcuts.Action.NUDGE_DOWN -> actions.nudgeSelection(0f, step)
                else -> Unit
            }
            return true
        }
        when (CanvasShortcuts.actionFor(letter, ctrl, shift)) {
            CanvasShortcuts.Action.UNDO -> { actions.undo(); return true }
            CanvasShortcuts.Action.REDO -> { actions.redo(); return true }
            CanvasShortcuts.Action.DELETE -> { actions.deleteSelection(); return true }
            CanvasShortcuts.Action.ESCAPE -> {
                if (state.cardEdit.value != null) state.cardEdit.value = null
                else if (state.selection.value != null || state.selectedItems.value.isNotEmpty()) { state.selection.value = null; state.selectedItems.value = emptyList() }
                else if (tool != "移动") actions.setTool("移动")
                else if (focusMode) actions.setFocusMode(false)
                return true
            }
            CanvasShortcuts.Action.ZOOM_IN -> { actions.zoomBy(1.25f); return true }
            CanvasShortcuts.Action.ZOOM_OUT -> { actions.zoomBy(0.8f); return true }
            CanvasShortcuts.Action.ZOOM_RESET -> { state.scale.floatValue = 1f; state.pan.value = Offset.Zero; return true }
            CanvasShortcuts.Action.SELECT_ALL -> { actions.selectAll(); return true }
            else -> Unit
        }
        if (ctrl || event.isAltPressed) return false
        val toolName = CanvasShortcuts.toolFor(letter, shift) ?: return false
        when (toolName) {
            "文本" -> { actions.dismissMenus(); state.activeMenuButton.value = null; actions.setTool("文本"); state.textToolsMenu.value = true }
            "思维导图" -> { actions.selectMenuOption("mindmap"); state.mindmapToolsMenu.value = true }
            "链接" -> { actions.dismissMenus(); state.activeMenuButton.value = null; state.linkedDocPicker.value = true }
            "高亮笔" -> { actions.setBrushKind(BrushSettings.KIND_HIGHLIGHTER); actions.chooseTool("画笔") }
            "画笔" -> { actions.setBrushKind(BrushSettings.KIND_BRUSH); actions.chooseTool("画笔") }
            else -> actions.chooseTool(toolName)
        }
        return true
}
