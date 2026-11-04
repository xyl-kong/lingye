package com.example.tabletworkspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
internal fun CanvasPageLinkToolbar(
    index: Int?,
    visible: Boolean,
    page: Page,
    pages: List<Page>,
    language: AppLanguage,
    scale: Float,
    viewport: Viewport,
    canvasWidthDp: Dp,
    canvasHeightDp: Dp,
    imeBottomDp: Dp,
    objectKind: CanvasObjectKind?,
    onOpenLinkedPage: (String) -> Unit,
    onSelect: (CanvasSelection?, List<CanvasSelection>) -> Unit,
    onChanged: () -> Unit
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(140)) + slideInVertically(tween(210)) { -it / 3 }, exit = fadeOut(tween(100)) + slideOutVertically(tween(130)) { -it / 3 }) {
        index?.let { linkIndex ->
            page.shapes.getOrNull(linkIndex)?.let { link ->
                val resolved = PageLinks.resolve(link, pages)
                val bounds = page.selectionBounds(listOf(CanvasSelection("shape", linkIndex))) ?: return@let
                val at = viewport.toScreen(Point(bounds.left, bounds.top))
                ContextualToolbarHost(
                    anchor = CanvasToolbarAnchor(
                        x = at.x.dp,
                        y = at.y.dp,
                        bottom = (at.y + (bounds.bottom - bounds.top) * scale).dp
                    ),
                    viewportWidth = canvasWidthDp,
                    viewportHeight = canvasHeightDp,
                    imeBottom = imeBottomDp, objectKind = objectKind
                ) {
                    Row(Modifier.padding(horizontal = 5.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (resolved.missing) {
                            Text(tr(language, "目标页面不存在", "Linked page missing"), color = LingyeColors.Coral, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        } else {
                            CompactIconAction(AppIcon.LINK, tr(language, "打开链接文档", "Open linked document"), {
                                PageLinks.targetId(link)?.let(onOpenLinkedPage)
                            }, LingyeColors.Primary)
                        }
                        CompactIconAction(AppIcon.COPY, tr(language, "复制链接", "Duplicate link"), {
                            val copied = page.duplicateSelections(listOf(CanvasSelection("shape", linkIndex)))
                            onSelect(copied.lastOrNull(), copied)
                            onChanged()
                        }, LingyeColors.Muted)
                        CompactIconAction(AppIcon.TRASH, tr(language, "删除链接", "Delete link"), {
                            page.deleteSelection(CanvasSelection("shape", linkIndex))
                            onSelect(null, emptyList())
                            onChanged()
                        }, LingyeColors.Coral)
                    }
                }
            }
        }
    }
}

@Composable
internal fun CanvasMindNodeToolbar(
    index: Int?,
    visible: Boolean,
    page: Page,
    language: AppLanguage,
    scale: Float,
    viewport: Viewport,
    canvasWidthDp: Dp,
    canvasHeightDp: Dp,
    imeBottomDp: Dp,
    objectKind: CanvasObjectKind?,
    onEditCard: (Int) -> Unit,
    onParentPicker: (String) -> Unit,
    onDeleteNode: (String) -> Unit,
    onChanged: () -> Unit
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(140)) + slideInVertically(tween(210)) { -it / 3 }, exit = fadeOut(tween(100)) + slideOutVertically(tween(130)) { -it / 3 }) {
        index?.let { nodeIndex ->
            page.shapes.getOrNull(nodeIndex)?.let { node ->
                var mindLayoutMenu by remember(nodeIndex) { mutableStateOf(false) }
                val bounds = page.selectionBounds(listOf(CanvasSelection("shape", nodeIndex))) ?: return@let
                val at = viewport.toScreen(Point(bounds.left, bounds.top))
                ContextualToolbarHost(
                    anchor = CanvasToolbarAnchor(
                        x = at.x.dp,
                        y = at.y.dp,
                        bottom = (at.y + (bounds.bottom - bounds.top) * scale).dp
                    ),
                    viewportWidth = canvasWidthDp,
                    viewportHeight = canvasHeightDp,
                    imeBottom = imeBottomDp, objectKind = objectKind
                ) {
                    Row(Modifier.padding(horizontal = 5.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        CompactIconAction(AppIcon.ADD, tr(language, "添加子节点", "Add child node"), {
                            Mindmap.addNode(page, node, tr(language, "新节点", "New node"))
                            Mindmap.reflow(page, node)
                            onChanged()
                        }, LingyeColors.Primary)
                        CompactIconAction(AppIcon.COPY, tr(language, "添加同级节点", "Add sibling node"), {
                            Mindmap.addSibling(page, node, tr(language, "新节点", "New node"))
                            Mindmap.reflow(page, node)
                            onChanged()
                        }, LingyeColors.Muted)
                        CompactIconAction(AppIcon.EDIT, tr(language, "编辑节点文字", "Edit node text"), { onEditCard(nodeIndex) }, LingyeColors.Muted)
                        Box {
                            CompactIconAction(AppIcon.MENU, tr(language, "切换布局", "Change layout"), { mindLayoutMenu = true }, LingyeColors.Muted)
                            LingyeDropdownMenu(expanded = mindLayoutMenu, onDismissRequest = { mindLayoutMenu = false }) {
                                Mindmap.LAYOUTS.forEach { layout ->
                                    DropdownMenuItem(
                                        text = { Text(tr(language, when (layout) { "bubble" -> "气泡图"; "right" -> "向右树"; "down" -> "向下树"; "tree" -> "组织图"; else -> "大纲" }, layout)) },
                                        onClick = { Mindmap.reflow(page, node, layout); mindLayoutMenu = false; onChanged() }
                                    )
                                }
                            }
                        }
                        CompactIconAction(AppIcon.CONNECT, tr(language, "改接上级节点", "Reparent node"), { onParentPicker(node.id) }, LingyeColors.Muted)
                        CompactIconAction(AppIcon.TRASH, tr(language, "删除节点", "Delete node"), { onDeleteNode(node.id) }, LingyeColors.Coral)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CanvasFrameToolbar(
    index: Int?,
    visible: Boolean,
    page: Page,
    language: AppLanguage,
    scale: Float,
    viewport: Viewport,
    canvasWidthDp: Dp,
    canvasHeightDp: Dp,
    imeBottomDp: Dp,
    objectKind: CanvasObjectKind?,
    onRenameFrame: (Int, String) -> Unit,
    onFitFrame: (Shape) -> Unit,
    onFrameNavigator: () -> Unit,
    onPresentFrame: (Int) -> Unit,
    onSelect: (CanvasSelection?, List<CanvasSelection>) -> Unit,
    onChanged: () -> Unit
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(140)) + slideInVertically(tween(210)) { -it / 3 }, exit = fadeOut(tween(100)) + slideOutVertically(tween(130)) { -it / 3 }) {
        index?.let { frameIndex ->
            page.shapes.getOrNull(frameIndex)?.let { frame ->
                var frameColorMenu by remember(frameIndex) { mutableStateOf(false) }
                val bounds = page.selectionBounds(listOf(CanvasSelection("shape", frameIndex))) ?: return@let
                val at = viewport.toScreen(Point(bounds.left, bounds.top))
                ContextualToolbarHost(
                    anchor = CanvasToolbarAnchor(
                        x = at.x.dp,
                        y = at.y.dp,
                        bottom = (at.y + (bounds.bottom - bounds.top) * scale).dp
                    ),
                    viewportWidth = canvasWidthDp,
                    viewportHeight = canvasHeightDp,
                    imeBottom = imeBottomDp, objectKind = objectKind
                ) {
                    Row(Modifier.padding(horizontal = 5.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        CompactIconAction(AppIcon.EDIT, tr(language, "重命名 Frame", "Rename frame"), {
                            onRenameFrame(frameIndex, frame.title.ifBlank { tr(language, "框架", "Frame") })
                        }, LingyeColors.Muted)
                        Box {
                            Surface(
                                onClick = { frameColorMenu = true },
                                modifier = Modifier.padding(horizontal = 3.dp).size(30.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(frame.color),
                                border = BorderStroke(1.dp, LingyeColors.Line)
                            ) {
                                Box(Modifier.semantics { contentDescription = tr(language, "背景颜色", "Background color") })
                            }
                            LingyeDropdownMenu(expanded = frameColorMenu, onDismissRequest = { frameColorMenu = false }) {
                                ColorPaletteMenu(selected = frame.color, onPick = { color ->
                                    CanvasPropertyEditor.updateShape(page, frameIndex, onChanged) { it.copy(color = color) }
                                    frameColorMenu = false
                                }, onRequestCustom = { frameColorMenu = false })
                            }
                        }
                        CompactIconAction(AppIcon.RESET, tr(language, "定位 Frame", "Focus frame"), { onFitFrame(frame) }, LingyeColors.Muted)
                        CompactIconAction(AppIcon.MENU, tr(language, "Frame 导航", "Frame navigator"), { onFrameNavigator() }, LingyeColors.Muted)
                        CompactIconAction(AppIcon.EXPAND_FULL, tr(language, "演示 Frame", "Present frame"), { onPresentFrame(frameIndex) }, LingyeColors.Muted)
                        CompactIconAction(AppIcon.COPY, tr(language, "复制 Frame", "Duplicate frame"), {
                            val copied = page.duplicateSelections(listOf(CanvasSelection("shape", frameIndex)))
                            onSelect(copied.firstOrNull(), copied)
                            onChanged()
                        }, LingyeColors.Muted)
                        CompactIconAction(AppIcon.TRASH, tr(language, "删除 Frame", "Delete frame"), {
                            page.deleteSelection(CanvasSelection("shape", frameIndex))
                            onSelect(null, emptyList())
                            onChanged()
                        }, LingyeColors.Coral)
                    }
                }
            }
        }
    }
}

@Composable
internal fun CanvasImageToolbar(
    index: Int?,
    visible: Boolean,
    page: Page,
    language: AppLanguage,
    scale: Float,
    viewport: Viewport,
    canvasWidthDp: Dp,
    canvasHeightDp: Dp,
    imeBottomDp: Dp,
    objectKind: CanvasObjectKind?,
    onCropImage: (String, Float, Float, Float, Float) -> Unit,
    onSelect: (CanvasSelection?, List<CanvasSelection>) -> Unit,
    onChanged: () -> Unit
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(140)) + slideInVertically(tween(210)) { -it / 3 }, exit = fadeOut(tween(100)) + slideOutVertically(tween(130)) { -it / 3 }) {
        index?.let { imageIndex ->
            page.images.getOrNull(imageIndex)?.let { image ->
                var layerMenu by remember(imageIndex) { mutableStateOf(false) }
                val bounds = page.selectionBounds(listOf(CanvasSelection("image", imageIndex))) ?: return@let
                val at = viewport.toScreen(Point(bounds.left, bounds.top))
                ContextualToolbarHost(
                    anchor = CanvasToolbarAnchor(
                        x = at.x.dp,
                        y = at.y.dp,
                        bottom = (at.y + (bounds.bottom - bounds.top) * scale).dp
                    ),
                    viewportWidth = canvasWidthDp,
                    viewportHeight = canvasHeightDp,
                    imeBottom = imeBottomDp, objectKind = objectKind
                ) {
                    Row(Modifier.padding(horizontal = 5.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        CompactIconAction(AppIcon.IMAGE, tr(language, "裁剪图片", "Crop image"), {
                            onCropImage(image.id, image.cropLeft, image.cropTop, image.cropRight, image.cropBottom)
                        }, LingyeColors.Primary)
                        CompactIconAction(AppIcon.RESET, tr(language, "旋转 90 度", "Rotate 90 degrees"), { page.rotateCanvasImage(imageIndex); onChanged() }, LingyeColors.Muted)
                        CompactIconAction(AppIcon.REVERSE, tr(language, "水平翻转", "Flip horizontal"), { page.flipCanvasImage(imageIndex, true); onChanged() }, LingyeColors.Muted)
                        CompactIconAction(AppIcon.STEP, tr(language, "垂直翻转", "Flip vertical"), { page.flipCanvasImage(imageIndex, false); onChanged() }, LingyeColors.Muted)
                        CompactIconAction(AppIcon.COPY, tr(language, "复制图片", "Duplicate image"), {
                            val copied = page.duplicateSelections(listOf(CanvasSelection("image", imageIndex)))
                            onSelect(copied.lastOrNull(), copied)
                            onChanged()
                        }, LingyeColors.Muted)
                        Box {
                            CompactIconAction(AppIcon.MORE, tr(language, "图层", "Layer"), { layerMenu = true }, LingyeColors.Muted)
                            LingyeDropdownMenu(expanded = layerMenu, onDismissRequest = { layerMenu = false }) {
                                LayerMenuItems(language, showIcons = false) { move ->
                                    val moved = page.reorderSelection(CanvasSelection("image", imageIndex), move)
                                    onSelect(moved, listOf(moved))
                                    layerMenu = false
                                    onChanged()
                                }
                            }
                        }
                        CompactIconAction(AppIcon.TRASH, tr(language, "删除图片", "Delete image"), {
                            page.deleteSelection(CanvasSelection("image", imageIndex))
                            onSelect(null, emptyList())
                            onChanged()
                        }, LingyeColors.Coral)
                    }
                }
            }
        }
    }
}

@Composable
internal fun CanvasStrokeToolbar(
    index: Int?,
    visible: Boolean,
    page: Page,
    language: AppLanguage,
    scale: Float,
    viewport: Viewport,
    canvasWidthDp: Dp,
    canvasHeightDp: Dp,
    imeBottomDp: Dp,
    objectKind: CanvasObjectKind?,
    onSelect: (CanvasSelection?, List<CanvasSelection>) -> Unit,
    onChanged: () -> Unit
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(140)) + slideInVertically(tween(210)) { -it / 3 }, exit = fadeOut(tween(100)) + slideOutVertically(tween(130)) { -it / 3 }) {
        index?.let { strokeIndex ->
            page.strokes.getOrNull(strokeIndex)?.let {
                var layerMenu by remember(strokeIndex) { mutableStateOf(false) }
                val bounds = page.selectionBounds(listOf(CanvasSelection("stroke", strokeIndex))) ?: return@let
                val at = viewport.toScreen(Point(bounds.left, bounds.top))
                val style = page.strokeStyles[strokeIndex]
                ContextualToolbarHost(
                    anchor = CanvasToolbarAnchor(
                        x = at.x.dp,
                        y = at.y.dp,
                        bottom = (at.y + (bounds.bottom - bounds.top) * scale).dp
                    ),
                    viewportWidth = canvasWidthDp,
                    viewportHeight = canvasHeightDp,
                    imeBottom = imeBottomDp, objectKind = objectKind
                ) {
                    Row(Modifier.padding(horizontal = 5.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        ColorPaletteRow(selected = style.color, onPick = { color ->
                            page.strokeStyles[strokeIndex] = style.copy(color = color)
                            onChanged()
                        })
                        CompactIconAction(AppIcon.MINUS, tr(language, "减细笔迹", "Thinner stroke"), {
                            page.strokeStyles[strokeIndex] = style.copy(width = (style.width - 1f).coerceAtLeast(1f))
                            onChanged()
                        })
                        Text("${style.width.roundToInt()} px", color = LingyeColors.Muted, fontSize = 11.sp)
                        CompactIconAction(AppIcon.ADD, tr(language, "加粗笔迹", "Thicker stroke"), {
                            page.strokeStyles[strokeIndex] = style.copy(width = (style.width + 1f).coerceAtMost(20f))
                            onChanged()
                        })
                        Box {
                            CompactIconAction(AppIcon.MORE, tr(language, "图层", "Layer"), { layerMenu = true }, LingyeColors.Muted)
                            LingyeDropdownMenu(expanded = layerMenu, onDismissRequest = { layerMenu = false }) {
                                LayerMenuItems(language, showIcons = false) { move ->
                                    val moved = page.reorderSelection(CanvasSelection("stroke", strokeIndex), move)
                                    onSelect(moved, listOf(moved))
                                    layerMenu = false
                                    onChanged()
                                }
                            }
                        }
                        CompactIconAction(AppIcon.TRASH, tr(language, "删除笔迹", "Delete stroke"), {
                            page.deleteSelection(CanvasSelection("stroke", strokeIndex))
                            onSelect(null, emptyList())
                            onChanged()
                        }, LingyeColors.Coral)
                    }
                }
            }
        }
    }
}
