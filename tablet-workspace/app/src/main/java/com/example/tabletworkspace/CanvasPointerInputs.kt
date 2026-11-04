@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.tabletworkspace

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

internal fun Modifier.canvasGestureInput(
    page: Page,
    language: AppLanguage,
    settings: WorkspaceSettings,
    tool: String,
    penOnly: Boolean,
    eraserArea: Boolean,
    brushKind: String,
    highlighterColor: Int,
    highlighterWidth: Float,
    inkColor: Int,
    inkWidth: Float,
    snapToGrid: Boolean,
    multiSelect: Boolean,
    pendingTextKind: String,
    frameRatio: String,
    presentingFrameId: String?,
    density: Float,
    state: CanvasEditorState,
    beginGesture: () -> Unit,
    commitGesture: () -> Unit,
    cancelGesture: () -> Unit,
    changed: () -> Unit,
    redraw: () -> Unit,
    chooseTool: (String) -> Unit
): Modifier {
    var scale by state.scale
    var pan by state.pan
    var points by state.points
    var selection by state.selection
    var selectedItems by state.selectedItems
    var endpointDrag by state.endpointDrag
    var endpointPreview by state.endpointPreview
    var endpointSnap by state.endpointSnap
    var connectorDraft by state.connectorDraft
    var connectorQuickSource by state.connectorQuickSource
    var snapGuides by state.snapGuides
    var dragSnapStart by state.dragSnapStart
    var dragSnapOthers by state.dragSnapOthers
    var dragSnapAccum by state.dragSnapAccum
    var resizeAnchor by state.resizeAnchor
    var resizeSnapshot by state.resizeSnapshot
    var resizeGrabOffset by state.resizeGrabOffset
    var resizeAxis by state.resizeAxis
    var boxStart by state.boxStart
    var boxEnd by state.boxEnd
    var dragging by state.dragging
    fun world(p: Offset): Point = Viewport(scale, pan.x / density, pan.y / density).toWorld(Point(p.x / density, p.y / density))
    fun isLocked(selection: CanvasSelection): Boolean =
        selection.type == "shape" && page.shapes.getOrNull(selection.index)?.locked == true
    val activeInkColor = BrushSettings.paintColor(brushKind, if (brushKind == BrushSettings.KIND_HIGHLIGHTER) highlighterColor else inkColor)
    val activeInkWidth = BrushSettings.paintWidth(brushKind, inkWidth, highlighterWidth)
    return this.pointerInput(page, tool, penOnly, eraserArea, brushKind, highlighterColor, highlighterWidth, inkColor, inkWidth, multiSelect, snapToGrid, presentingFrameId) {
            if (presentingFrameId != null) detectTransformGestures { centroid, delta, zoom, _ ->
                val oldScale = scale; val next = (scale * zoom).coerceIn(FrameSemantics.PRESENTATION_MIN_SCALE, FrameSemantics.PRESENTATION_MAX_SCALE); val factor = next / oldScale
                pan = centroid - (centroid - pan) * factor + delta; scale = next
            } else if (tool == "画笔" || tool == "橡皮") {
                try { awaitPointerEventScope {
                    var active: PointerId? = null
                    var erasing = false
                    var previous: Point? = null
                    var didErase = false
                    val areaHits = mutableSetOf<CanvasSelection>()
                    while (true) {
                        val event = awaitPointerEvent()
                        if (active == null) {
                            val down = event.changes.firstOrNull { change ->
                                val kind = when (change.type) { PointerType.Stylus -> "stylus"; PointerType.Eraser -> "eraser"; PointerType.Touch -> "touch"; PointerType.Mouse -> "mouse"; else -> "unknown" }
                                change.pressed && !change.previousPressed && inkAcceptsPointer(penOnly, kind)
                            }
                            if (down != null) {
                                beginGesture(); active = down.id; erasing = tool == "橡皮" || down.type == PointerType.Eraser
                                val p = world(down.position); previous = p; didErase = false; areaHits.clear()
                                if (erasing) {
                                    if (eraserArea) { areaHits.addAll(AreaEraser.hits(page, p, p, 16f / scale)); points = listOf(p); redraw() }
                                    else { didErase = page.eraseAt(p, 16f / scale); if (didErase) redraw() }
                                } else points = listOf(p)
                                down.consume()
                            }
                        } else {
                            val change = event.changes.firstOrNull { it.id == active }
                            if (change == null) { cancelGesture(); active = null; previous = null; points = emptyList(); didErase=false; areaHits.clear() }
                            else {
                                val p = world(change.position)
                                if (change.pressed) {
                                    if (erasing) {
                                        val before = previous ?: p
                                        if (eraserArea) {
                                            val found = AreaEraser.hits(page, before, p, 16f / scale)
                                            if (found.isNotEmpty()) areaHits.addAll(found)
                                            if (points.lastOrNull() != p) points = points + p
                                            redraw()
                                        } else if (page.eraseBetween(before, p, 16f / scale)) { didErase = true; redraw() }
                                    } else if (points.lastOrNull() != p) points = points + p
                                    previous = p; change.consume()
                                } else if (change.previousPressed) {
                                    if (erasing) {
                                        if (eraserArea) { if (areaHits.isNotEmpty()) AreaEraser.erase(page, areaHits); commitGesture(); if (areaHits.isNotEmpty()) changed() }
                                        else { commitGesture(); if (didErase) changed() }
                                    }
                                    else if (points.isNotEmpty()) { page.addStyledStroke(points, activeInkColor, activeInkWidth); commitGesture(); changed() } else commitGesture()
                                    points = emptyList(); previous = null; active = null; didErase = false; areaHits.clear(); change.consume()
                                }
                            }
                        }
                    }
                } } finally { cancelGesture(); points=emptyList() }
            } else if (tool == "移动") detectTransformGestures { centroid, delta, zoom, _ ->
                val oldScale = scale; val next = CanvasZoom.clamp(scale * zoom); val factor = next / oldScale
                pan = centroid - (centroid - pan) * factor + delta; scale = next
            } else if (tool == "选择") detectDragGestures(onDragStart = { start ->
                beginGesture(); val p = world(start); val chosen = selection
                val line = chosen?.takeIf { it.type == "connector" }?.let { page.connectors.getOrNull(it.index)?.let { _ -> page.resolvedConnector(it.index) } }
                endpointDrag = line?.let { if (hypot(it.x1-p.x,it.y1-p.y)<32f/scale) true else if (hypot(it.x2-p.x,it.y2-p.y)<32f/scale) false else null }
                endpointPreview = if (endpointDrag != null) p else null
                endpointSnap = null
                val bounds = page.selectionBounds(selectedItems)
                val handleRadius = 28f / scale
                resizeAxis = null
                resizeAnchor = bounds?.let { b ->
                    val midX=(b.left+b.right)/2f
                    val midY=(b.top+b.bottom)/2f
                    when {
                        hypot(b.left-p.x,b.top-p.y) < handleRadius -> Point(b.right,b.bottom)
                        hypot(b.right-p.x,b.top-p.y) < handleRadius -> Point(b.left,b.bottom)
                        hypot(b.left-p.x,b.bottom-p.y) < handleRadius -> Point(b.right,b.top)
                        hypot(b.right-p.x,b.bottom-p.y) < handleRadius -> Point(b.left,b.top)
                        hypot(b.left-p.x,midY-p.y) < handleRadius -> { resizeAxis="x"; Point(b.right,b.top) }
                        hypot(b.right-p.x,midY-p.y) < handleRadius -> { resizeAxis="x"; Point(b.left,b.top) }
                        hypot(midX-p.x,b.top-p.y) < handleRadius -> { resizeAxis="y"; Point(b.left,b.bottom) }
                        hypot(midX-p.x,b.bottom-p.y) < handleRadius -> { resizeAxis="y"; Point(b.left,b.top) }
                        else -> null
                    }
                }
                resizeSnapshot = null
                resizeGrabOffset = Point(0f, 0f)
                resizeAnchor?.let { anchor ->
                    val targets = if (selectedItems.isEmpty()) listOfNotNull(selection) else selectedItems
                    if (targets.none(::isLocked)) {
                        val snapshot = page.freeResizeSnapshot(targets, anchor)
                        resizeSnapshot = snapshot
                        if (snapshot != null) {
                            val b = snapshot.bounds
                            val dragged = when (resizeAxis) {
                                "x" -> Point(
                                    if (anchor.x >= b.right - 0.01f) b.left else b.right,
                                    (b.top + b.bottom) / 2f
                                )
                                "y" -> Point(
                                    (b.left + b.right) / 2f,
                                    if (anchor.y >= b.bottom - 0.01f) b.top else b.bottom
                                )
                                else -> Point(
                                    if (anchor.x >= b.right - 0.01f) b.left else b.right,
                                    if (anchor.y >= b.bottom - 0.01f) b.top else b.bottom
                                )
                            }
                            resizeGrabOffset = Point(p.x - dragged.x, p.y - dragged.y)
                        }
                    }
                }
                val hit = if (resizeAnchor != null || endpointDrag != null) null else page.hitTest(p, 14f / scale)
                boxStart = if (resizeAnchor == null && endpointDrag == null && hit == null) p else null; boxEnd = boxStart
                if (hit != null && hit !in selectedItems) { selection = hit; selectedItems = page.selectionMembers(hit) }
                dragging = false
                val snapTargets = if (selectedItems.isEmpty()) listOfNotNull(selection) else selectedItems
                val snapStart = if (resizeAnchor == null && endpointDrag == null && boxStart == null) page.selectionBounds(snapTargets) else null
                dragSnapStart = snapStart
                dragSnapOthers = if (snapStart != null) page.snapTargetBounds(snapTargets) else emptyList()
                dragSnapAccum = Point(0f, 0f)
                snapGuides = emptyList()
            }, onDragEnd = {
                val a=boxStart; val b=boxEnd
                val end=endpointDrag; val preview=endpointPreview; val chosen=selection
                if (dragging && snapToGrid && resizeAnchor == null && endpointDrag == null && boxStart == null) page.snapSelectionsToGrid(selectedItems, settings.gridStep)
                commitGesture()
                if (end != null && preview != null && chosen?.type == "connector") {
                    val snap = endpointSnap
                    when (snap?.kind) {
                        ConnectorSnapKind.ANCHOR -> {
                            val shape = snap.shapeIndex
                            if (shape != null) page.bindConnectorEnd(chosen.index, end, shape, snap.port)
                            else page.detachConnectorEnd(chosen.index, end, snap.point)
                        }
                        ConnectorSnapKind.EDGE -> {
                            page.detachConnectorEnd(chosen.index, end, snap.point)
                            val line = page.connectors[chosen.index]
                            page.connectors[chosen.index] = if (end) line.copy(startPort = snap.edge) else line.copy(endPort = snap.edge)
                        }
                        else -> page.detachConnectorEnd(chosen.index, end, preview)
                    }
                    endpointSnap = null
                    changed()
                }
                else if (a != null && b != null) { selectedItems = page.selectInBox(a,b); selection = selectedItems.lastOrNull() }
                else if (dragging) changed()
                boxStart=null; boxEnd=null; endpointDrag=null; endpointPreview=null; endpointSnap=null; resizeAnchor=null; resizeSnapshot=null; resizeGrabOffset=Point(0f,0f); resizeAxis=null; dragging = false
                snapGuides = emptyList(); dragSnapStart = null; dragSnapOthers = emptyList(); dragSnapAccum = Point(0f, 0f)
            }, onDragCancel = { cancelGesture(); boxStart=null; boxEnd=null; endpointDrag=null; endpointPreview=null; endpointSnap=null; resizeAnchor=null; resizeSnapshot=null; resizeGrabOffset=Point(0f,0f); resizeAxis=null; dragging=false; snapGuides = emptyList(); dragSnapStart = null; dragSnapOthers = emptyList() }) { event, delta ->
                event.consume()
                if (endpointDrag != null) {
                    val chosen = selection
                    val line = chosen?.takeIf { it.type == "connector" }?.let { page.connectors.getOrNull(it.index) }
                    val otherId = if (endpointDrag == true) line?.endShape else line?.startShape
                    val otherShape = page.shapes.indexOfFirst { it.id == otherId }.takeIf { it >= 0 }
                    val snap = ConnectorSnapping.snap(
                        world(event.position),
                        page.connectorSnapRects(otherShape),
                        anchorThreshold = 24f / (density * scale),
                        edgeThreshold = 30f / (density * scale)
                    )
                    endpointSnap = snap
                    endpointPreview = snap.point
                } else if (boxStart != null) { boxEnd=world(event.position) }
                else selection?.let {
                    val anchor=resizeAnchor
                    val targets = if (selectedItems.isEmpty()) listOf(it) else selectedItems
                    if (targets.any(::isLocked)) return@let
                    if (anchor != null) {
                        val current=world(event.position)
                        val snapshot=resizeSnapshot
                        if (snapshot != null) page.applyFreeResize(snapshot,Point(current.x-resizeGrabOffset.x,current.y-resizeGrabOffset.y),resizeAxis)
                    } else {
                        val step = Point(delta.x / density / scale, delta.y / density / scale)
                        val start = dragSnapStart
                        if (start != null && !snapToGrid) {
                            val accum = Point(dragSnapAccum.x + step.x, dragSnapAccum.y + step.y)
                            dragSnapAccum = accum
                            val candidate = CanvasBounds(start.left + accum.x, start.top + accum.y, start.right + accum.x, start.bottom + accum.y)
                            val outcome = SmartGuides.snap(candidate, dragSnapOthers, 5f / scale)
                            val current = page.selectionBounds(targets)
                            if (current != null) page.moveSelections(targets, outcome.bounds.left - current.left, outcome.bounds.top - current.top)
                            snapGuides = outcome.guides
                        } else {
                            page.moveSelections(targets, step.x, step.y)
                        }
                    }
                    dragging = true; redraw()
                }
            } else if (tool == "连线") {
                try { awaitPointerEventScope {
                    var active: PointerId? = null
                    var moved = false
                    while (true) {
                        val event = awaitPointerEvent()
                        if (active == null) {
                            val down = event.changes.firstOrNull { it.pressed && !it.previousPressed } ?: continue
                            val p = world(down.position)
                            active = down.id; moved = false
                            beginGesture()
                            val source = page.shapeIndexAt(p, 12f / scale)
                            val (port, anchor) = source?.let { page.nearestPortOnShape(it, p) } ?: ("center" to p)
                            connectorDraft = ConnectorDraft(anchor, anchor, source, null, port, "center")
                            redraw()
                            down.consume()
                        } else {
                            val change = event.changes.firstOrNull { it.id == active }
                            if (change == null) { connectorDraft = null; active = null; cancelGesture(); continue }
                            val p = world(change.position)
                            if (change.pressed) {
                                val draft = connectorDraft
                                if (draft != null && hypot(p.x - draft.start.x, p.y - draft.start.y) > 3f / scale) moved = true
                                if (draft != null) {
                                    val snap = ConnectorSnapping.snap(
                                        p,
                                        page.connectorSnapRects(draft.sourceShape),
                                        anchorThreshold = 24f / (density * scale),
                                        edgeThreshold = 30f / (density * scale)
                                    )
                                    connectorDraft = draft.copy(
                                        end = snap.point,
                                        targetShape = snap.shapeIndex,
                                        targetPort = snap.port,
                                        snapKind = snap.kind,
                                        snapEdge = snap.edge
                                    )
                                }
                                redraw()
                                change.consume()
                            } else {
                                val draft = connectorDraft
                                if (draft != null && moved) {
                                    page.addConnector(draft.start.x, draft.start.y, draft.end.x, draft.end.y, settings.connectorColor, settings.connectorPathStyle, settings.connectorEndMarker, width = settings.connectorWidth)
                                    val index = page.connectors.lastIndex
                                    draft.sourceShape?.let { page.bindConnectorEnd(index, true, it, draft.sourcePort) }
                                    if (draft.snapKind == ConnectorSnapKind.ANCHOR) {
                                        draft.targetShape?.let { page.bindConnectorEnd(index, false, it, draft.targetPort) }
                                    } else if (draft.snapKind == ConnectorSnapKind.EDGE) {
                                        page.connectors[index] = page.connectors[index].copy(endPort = draft.snapEdge.ifBlank { "center" })
                                    }
                                    commitGesture(); changed()
                                    val created = CanvasSelection("connector", index)
                                    selection = created; selectedItems = listOf(created)
                                    connectorQuickSource = null
                                    chooseTool("选择")
                                } else {
                                    cancelGesture()
                                    val hit = page.shapeIndexAt(p, 12f / scale)
                                    val source = connectorQuickSource
                                    if (source == null && hit != null) {
                                        connectorQuickSource = hit
                                        redraw()
                                    } else if (source != null && hit != null && hit != source) {
                                        beginGesture()
                                        val targetCenter = shapePort(page.shapes[hit], "center")
                                        val start = page.nearestPortOnShape(source, targetCenter)
                                        val end = page.nearestPortOnShape(hit, start.second)
                                        page.addConnector(start.second.x, start.second.y, end.second.x, end.second.y, settings.connectorColor, settings.connectorPathStyle, settings.connectorEndMarker, width = settings.connectorWidth)
                                        val index = page.connectors.lastIndex
                                        page.bindConnectorEnd(index, true, source, start.first)
                                        page.bindConnectorEnd(index, false, hit, end.first)
                                        commitGesture(); changed()
                                        val created = CanvasSelection("connector", index)
                                        selection = created; selectedItems = listOf(created)
                                        connectorQuickSource = null
                                        chooseTool("选择")
                                    } else {
                                        connectorQuickSource = null
                                        redraw()
                                    }
                                }
                                connectorDraft = null
                                active = null
                                change.consume()
                            }
                        }
                    }
                } } finally { connectorDraft = null }
            } else detectDragGestures(onDragStart = { points = listOf(world(it)) }, onDragEnd = {
                if (points.isNotEmpty()) { val a = points.first(); val b = points.last(); when (tool) {
                    "矩形", "圆角矩形", "圆形", "三角形", "菱形" -> page.addShape(
                        when (tool) { "圆形" -> "ellipse"; "圆角矩形" -> "roundRect"; "三角形" -> "triangle"; "菱形" -> "diamond"; else -> "rect" },
                        min(a.x,b.x), min(a.y,b.y), abs(b.x-a.x).coerceAtLeast(12f), abs(b.y-a.y).coerceAtLeast(12f), settings.shapeColor
                    )
                    "Frame" -> {
                        var width = abs(b.x-a.x).coerceAtLeast(120f)
                        var height = abs(b.y-a.y).coerceAtLeast(90f)
                        val ratio = when(frameRatio) { "1:1" -> 1f; "4:3" -> 4f/3f; "16:9" -> 16f/9f; "2:1" -> 2f; else -> null }
                        if (ratio != null) {
                            if (width / height > ratio) width = height * ratio else height = width / ratio
                        }
                        page.addShape("frame", min(a.x,b.x), min(a.y,b.y), width, height, settings.frameColor)
                        val frameIndex = page.shapes.lastIndex
                        page.shapes[frameIndex] = page.shapes[frameIndex].copy(title = tr(language, "框架", "Frame"), borderColor = 0xffd8d8d8.toInt(), borderStyle = "solid", borderWidth = 1f, cornerRadius = 10f)
                    }
                    "文本" -> {
                        val x = min(a.x, b.x)
                        val y = min(a.y, b.y)
                        val width = abs(b.x - a.x).coerceAtLeast(120f)
                        val height = abs(b.y - a.y).coerceAtLeast(56f)
                        if (pendingTextKind == "body") {
                            page.addTextBox(
                                x, y, tr(language, "双击编辑文本", "Double-tap to edit"),
                                width = width, height = height,
                                textColor = settings.defaultTextColor,
                                fontSize = settings.defaultTextSize,
                                textAlign = settings.defaultTextAlign
                            )
                        } else {
                            page.addCanvasTextBlock(
                                pendingTextKind, x, y, language, settings.defaultTextColor,
                                width = width, height = height
                            )
                        }
                        val created = CanvasSelection("shape", page.shapes.lastIndex)
                        selection = created
                        selectedItems = listOf(created)
                        chooseTool("选择")
                    }
                }; changed() }; points = emptyList()
            }, onDragCancel = { points = emptyList() }) { event, _ -> event.consume(); points = points + world(event.position) }
    }
}
