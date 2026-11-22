package com.example.tabletworkspace

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput

/** Adds two-finger viewport navigation without changing the active drawing or selection tool. */
internal fun Modifier.canvasTwoFingerNavigationInput(
    page: Page,
    tool: String,
    presentingFrameId: String?,
    state: CanvasEditorState,
    cancelGesture: () -> Unit
): Modifier {
    // These modes already own transform gestures, including their one-finger behavior.
    if (tool == "移动" || presentingFrameId != null) return this
    return pointerInput(page, tool) {
        var lockedUntilRelease = false
        try {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val fingers = event.changes.filter { it.pressed && it.type == PointerType.Touch }
                    if (!lockedUntilRelease && CanvasViewportGestures.mayStart(
                            fingers.size,
                            event.changes.any { it.pressed && (it.type == PointerType.Stylus || it.type == PointerType.Eraser) }
                        )) {
                        lockedUntilRelease = true
                        state.viewportGestureActive.value = true
                        state.viewportGestureGeneration.intValue++
                        cancelGesture()
                        state.points.value = emptyList()
                        state.connectorDraft.value = null
                        state.endpointDrag.value = null
                        state.endpointPreview.value = null
                        state.endpointSnap.value = null
                        state.resizeAnchor.value = null
                        state.resizeSnapshot.value = null
                        state.resizeGrabOffset.value = Point(0f, 0f)
                        state.resizeAxis.value = null
                        state.boxStart.value = null
                        state.boxEnd.value = null
                        state.dragging.value = false
                        state.snapGuides.value = emptyList()
                        state.dragSnapStart.value = null
                        state.dragSnapOthers.value = emptyList()
                        state.dragSnapAccum.value = Point(0f, 0f)
                    }
                    if (lockedUntilRelease) {
                        val established = fingers.filter { it.previousPressed }.take(2)
                        if (established.size == 2) {
                            val oldScale = state.scale.floatValue
                            val result = CanvasViewportGestures.transform(
                                oldScale, state.pan.value,
                                established[0].previousPosition, established[1].previousPosition,
                                established[0].position, established[1].position
                            )
                            state.scale.floatValue = result.scale
                            state.pan.value = result.pan
                        }
                        // Keep the remaining finger inert after a pinch until it lifts.
                        event.changes.filter { it.type == PointerType.Touch }.forEach { it.consume() }
                        if (fingers.isEmpty()) {
                            // Other handlers run on the Main pass. Keep the lock through
                            // the release event so they cannot finish an interrupted edit.
                            awaitPointerEvent(PointerEventPass.Final)
                            lockedUntilRelease = false
                            state.viewportGestureActive.value = false
                        }
                    }
                }
            }
        } finally {
            state.viewportGestureActive.value = false
        }
    }
}
