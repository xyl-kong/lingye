package com.example.tabletworkspace

import androidx.compose.ui.geometry.Offset

/**
 * Canvas editing commands that operate on the page and [CanvasEditorState] without being
 * tied to the composition: selection deletion/duplication/grouping, nudging, viewport
 * zoom and Frame presentation. Created per composition so the callbacks it uses
 * ([setTool], [changed], ...) always come from the current frame.
 */
internal class CanvasEditorCommands(
    private val page: Page,
    private val state: CanvasEditorState,
    private val density: Float,
    private val focusMode: Boolean,
    private val tool: String,
    private val setTool: (String) -> Unit,
    private val setFocusMode: (Boolean) -> Unit,
    private val changed: () -> Unit,
    private val closeOverlays: () -> Unit
) {
    fun currentSelectionBase(): List<CanvasSelection> {
        val selected = state.selectedItems.value
        val selection = state.selection.value
        return if (selected.isEmpty() && selection != null) listOf(selection) else selected
    }

    fun deleteSelection() {
        val targets = currentSelectionBase().flatMap { page.selectionMembers(it) }.distinct()
            .filterNot { CanvasObjectSpecs.isLocked(page, it) }
            .sortedWith(compareBy<CanvasSelection> { it.type }.thenByDescending { it.index })
        if (targets.isEmpty()) return
        targets.forEach { page.deleteSelection(it) }
        state.selection.value = null
        state.selectedItems.value = emptyList()
        state.cardEdit.value = null
        changed()
    }

    fun zoomBy(factor: Float) {
        val scale = state.scale.floatValue
        val next = CanvasZoom.clamp(scale * factor)
        if (next == scale) return
        val f = next / scale
        val viewport = state.viewportSizePx.value
        val cx = viewport.width / 2f
        val cy = viewport.height / 2f
        state.pan.value = Offset(cx - (cx - state.pan.value.x) * f, cy - (cy - state.pan.value.y) * f)
        state.scale.floatValue = next
    }

    fun nudge(dx: Float, dy: Float) {
        val movable = page.unlocked(currentSelectionBase())
        if (movable.isEmpty()) return
        page.moveSelections(movable, dx, dy)
        changed()
    }

    fun selectAll() {
        val items = mutableListOf<CanvasSelection>()
        page.shapes.forEachIndexed { i, s -> if (!s.locked) items += CanvasSelection("shape", i) }
        page.images.indices.forEach { items += CanvasSelection("image", it) }
        page.connectors.indices.forEach { items += CanvasSelection("connector", it) }
        page.strokes.indices.forEach { items += CanvasSelection("stroke", it) }
        state.selectedItems.value = items
        state.selection.value = items.firstOrNull()
    }

    fun duplicateSelection() {
        val base = currentSelectionBase()
        if (base.isEmpty()) return
        val copied = page.duplicateSelections(base)
        state.selection.value = copied.lastOrNull()
        state.selectedItems.value = copied
        changed()
    }

    fun groupSelection(ungroup: Boolean) {
        val base = currentSelectionBase().flatMap { page.selectionMembers(it) }.distinct()
        if (base.isEmpty() || (!ungroup && base.size < 2)) return
        if (page.hasLocked(base)) return
        if (ungroup) page.ungroupSelections(base) else page.groupSelections(base)
        val grouped = base.flatMap { page.selectionMembers(it) }.distinct()
        state.selectedItems.value = grouped
        state.selection.value = grouped.lastOrNull()
        changed()
    }

    fun reorderSelection(toFront: Boolean) {
        val base = currentSelectionBase()
        if (base.isEmpty()) return
        base.forEach { page.reorderSelection(it, if (toFront) LayerMove.TO_FRONT else LayerMove.TO_BACK) }
        changed()
    }

    fun alignSelection(edge: String) {
        val base = page.unlocked(currentSelectionBase())
        if (base.isEmpty()) return
        page.alignSelections(base, edge)
        changed()
    }

    fun fitFrameViewport(frame: Shape) {
        val viewport = state.viewportSizePx.value
        val fit = FrameSemantics.fit(frame, viewport.width / density, viewport.height / density) ?: return
        state.scale.floatValue = fit.scale
        state.pan.value = Offset(fit.offsetX * density, fit.offsetY * density)
        val index = page.shapes.indexOfFirst { it.id == frame.id }
        val selection = if (index >= 0) CanvasSelection("shape", index) else null
        state.selection.value = selection
        state.selectedItems.value = listOfNotNull(selection)
    }

    fun presentationFrames(): List<Shape> = page.shapes.filter { it.type == "frame" }

    fun applyPresentationView(frame: Shape) {
        val viewport = state.viewportSizePx.value
        val canvasW = viewport.width / density
        val canvasH = viewport.height / density
        if (canvasW <= 0f || canvasH <= 0f) return
        val result = (if (state.presentationFitMode.value == "fill") {
            FrameSemantics.fill(frame, canvasW, canvasH, FrameSemantics.PRESENTATION_MIN_SCALE, FrameSemantics.PRESENTATION_MAX_SCALE)
        } else {
            FrameSemantics.fit(frame, canvasW, canvasH, minScale = FrameSemantics.PRESENTATION_MIN_SCALE, maxScale = FrameSemantics.PRESENTATION_MAX_SCALE)
        }) ?: return
        state.scale.floatValue = result.scale
        state.pan.value = Offset(result.offsetX * density, result.offsetY * density)
    }

    fun startPresentation(index: Int) {
        val frame = page.shapes.getOrNull(index)?.takeIf { it.type == "frame" } ?: return
        state.presentationWasFocus.value = focusMode
        state.presentationPrevTool.value = tool
        state.presentationPrevScale.floatValue = state.scale.floatValue
        state.presentationPrevPan.value = state.pan.value
        state.presentingFrameId.value = frame.id
        state.selection.value = null
        state.selectedItems.value = emptyList()
        state.cardEdit.value = null
        closeOverlays()
        setTool("选择")
        if (!focusMode) setFocusMode(true)
        applyPresentationView(frame)
    }

    fun stopPresentation() {
        state.presentingFrameId.value = null
        setTool(state.presentationPrevTool.value)
        state.scale.floatValue = state.presentationPrevScale.floatValue
        state.pan.value = state.presentationPrevPan.value
        if (!state.presentationWasFocus.value) setFocusMode(false)
    }

    fun shiftPresentation(delta: Int) {
        val frames = presentationFrames()
        if (frames.isEmpty()) { stopPresentation(); return }
        val current = frames.indexOfFirst { it.id == state.presentingFrameId.value }
        val next = if (current < 0) 0 else (current + delta + frames.size) % frames.size
        state.presentingFrameId.value = frames[next].id
        applyPresentationView(frames[next])
    }
}
