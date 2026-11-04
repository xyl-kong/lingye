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

@Composable
internal fun CanvasCardToolbar(
    index: Int?, visible: Boolean, page: Page, language: AppLanguage, scale: Float, viewport: Viewport,
    canvasWidthDp: Dp, canvasHeightDp: Dp, imeBottomDp: Dp, objectKind: CanvasObjectKind?,
    onEditCard: (Int) -> Unit, onSaveTemplate: (Int) -> Unit,
    onSelect: (CanvasSelection?, List<CanvasSelection>) -> Unit, onChanged: () -> Unit
) {
        AnimatedVisibility(visible = visible, enter = fadeIn(tween(150)) + slideInVertically(tween(220)) { -it / 3 }, exit = fadeOut(tween(100)) + slideOutVertically(tween(140)) { -it / 3 }) {
            index?.let { index ->
                page.shapes.getOrNull(index)?.let { shape ->
                    var layerMenu by remember(index) { mutableStateOf(false) }
                    var colorMenu by remember(index) { mutableStateOf(false) }
                    val at=viewport.toScreen(Point(shape.x,shape.y))
                    ContextualToolbarHost(
                        anchor = CanvasToolbarAnchor(
                            x = at.x.dp,
                            y = at.y.dp,
                            bottom = (at.y + shape.height * scale).dp
                        ),
                        viewportWidth = canvasWidthDp,
                        viewportHeight = canvasHeightDp,
                        imeBottom = imeBottomDp, objectKind = objectKind
                    ) {
                        Row(Modifier.padding(horizontal=6.dp,vertical=3.dp),verticalAlignment=Alignment.CenterVertically) {
                            CompactIconAction(AppIcon.EDIT,tr(language,"原位编辑卡片","Edit card in place"),{onEditCard(index)},LingyeColors.Primary)
                            if (shape.cardKind == "todo") {
                                CompactIconAction(AppIcon.RECORD, tr(language, "勾选下一项", "Check next item"), { if (page.checkNextTodoItem(index)) onChanged() }, LingyeColors.Muted)
                            }
                            CompactIconAction(AppIcon.CARD,tr(language,"保存模板","Save template"),{onSaveTemplate(index)})
                            CompactIconAction(AppIcon.COPY,tr(language,"复制卡片","Duplicate card"),{val copied=page.duplicateSelections(listOf(CanvasSelection("shape",index)));onSelect(copied.lastOrNull(), copied);onChanged()})
                            Text("${(scale*100).roundToInt()}%",fontSize=11.sp,color=LingyeColors.Muted,modifier=Modifier.padding(horizontal=7.dp))
                            Box {
                                Surface(
                                    onClick = { colorMenu = true },
                                    shape = RoundedCornerShape(9.dp),
                                    color = Color(shape.color),
                                    border = BorderStroke(1.dp, LingyeColors.Line),
                                    modifier = Modifier.padding(horizontal = 3.dp).size(26.dp)
                                ) {
                                    Box(Modifier.semantics { contentDescription = tr(language, "笔记背景色", "Note background") })
                                }
                                LingyeDropdownMenu(expanded = colorMenu, onDismissRequest = { colorMenu = false }) {
                                    ColorPaletteMenu(selected = shape.color, onPick = { color ->
                                        CanvasPropertyEditor.updateShape(page, index, onChanged) { it.copy(color = color) }
                                        colorMenu = false
                                    }, onRequestCustom = { colorMenu = false })
                                }
                            }
                            Box {
                                CompactIconAction(AppIcon.MORE,tr(language,"图层与更多","Layers and more"),{layerMenu=true},LingyeColors.Muted)
                                LingyeDropdownMenu(expanded=layerMenu,onDismissRequest={layerMenu=false}) {
                                    LayerMenuItems(language, showIcons = true) { move ->
                                        val moved = page.reorderSelection(CanvasSelection("shape", index), move)
                                        onSelect(moved, listOf(moved))
                                        layerMenu = false
                                        onChanged()
                                    }
                                    HorizontalDivider(Modifier.padding(vertical=4.dp), color=LingyeColors.Line)
                                    MenuSectionLabel(tr(language,"笔记样式","Note style"))
                                    MenuSectionLabel(tr(language,"阴影","Shadow"))
                                    listOf(
                                        "none" to tr(language,"无阴影","No shadow"),
                                        "box" to tr(language,"卡片阴影","Card shadow"),
                                        "sticker" to tr(language,"贴纸阴影","Sticker shadow")
                                    ).forEach { (value,label) ->
                                        MenuCheckItem(label, shape.shadowStyle == value) {
                                            CanvasPropertyEditor.updateShape(page,index,onChanged){ it.copy(shadowStyle=value) };layerMenu=false
                                        }
                                    }
                                    MenuSectionLabel(tr(language,"圆角","Corners"))
                                    listOf(
                                        0f to tr(language,"无","None"),
                                        8f to tr(language,"小","Small"),
                                        16f to tr(language,"中","Medium"),
                                        24f to tr(language,"大","Large"),
                                        32f to tr(language,"超大","Huge")
                                    ).forEach { (radius,label) ->
                                        val active = kotlin.math.abs(shape.cornerRadius - radius) < 0.5f
                                        MenuCheckItem(label, active) {
                                            CanvasPropertyEditor.updateShape(page,index,onChanged){ it.copy(cornerRadius=radius) };layerMenu=false
                                        }
                                    }
                                }
                            }
                            CompactIconAction(AppIcon.DOCUMENT, tr(language,"插入到文档","Insert into page"), {
                                if (!shape.locked) {
                                    val imported = CardDocumentImport.blocks(shape.title, shape.body)
                                    if (imported.isNotEmpty()) {
                                        page.blocks.addAll(imported)
                                        onChanged()
                                    }
                                }
                            }, LingyeColors.Muted)
                            CompactIconAction(AppIcon.LOCK, tr(language,"锁定卡片","Lock card"), { page.shapes[index]=shape.copy(locked=!shape.locked); onChanged() }, LingyeColors.Muted, shape.locked)
                            CompactIconAction(AppIcon.TRASH,tr(language,"删除卡片","Delete card"),{page.deleteSelection(CanvasSelection("shape",index));onSelect(null, emptyList());onChanged()},LingyeColors.Coral)
                        }
                    }
                }
            }
        }
}

@Composable
internal fun CanvasShapeToolbar(
    index: Int?, visible: Boolean, page: Page, language: AppLanguage, scale: Float, viewport: Viewport,
    canvasWidthDp: Dp, canvasHeightDp: Dp, imeBottomDp: Dp, objectKind: CanvasObjectKind?,
    onDrawConnector: (Int) -> Unit,
    onSelect: (CanvasSelection?, List<CanvasSelection>) -> Unit, onChanged: () -> Unit
) {
        AnimatedVisibility(visible = visible, enter = fadeIn(tween(140)) + slideInVertically(tween(210)) { -it / 3 }, exit = fadeOut(tween(100)) + slideOutVertically(tween(130)) { -it / 3 }) {
            index?.let { index ->
                page.shapes.getOrNull(index)?.let { shape ->
                    var shapeMenu by remember(index) { mutableStateOf(false) }
                    var fillMenu by remember(index) { mutableStateOf(false) }
                    var borderMenu by remember(index) { mutableStateOf(false) }
                    var borderColorMenu by remember(index) { mutableStateOf(false) }
                    var layerMenu by remember(index) { mutableStateOf(false) }
                    val bounds = page.selectionBounds(listOf(CanvasSelection("shape", index))) ?: return@let
                    val at = viewport.toScreen(Point(bounds.left, bounds.top))
                    ContextualToolbarHost(
                        anchor = CanvasToolbarAnchor(
                            x = at.x.dp,
                            y = at.y.dp,
                            bottom = (at.y + (bounds.bottom - bounds.top) * scale).dp
                        ),
                        viewportWidth = canvasWidthDp,
                        viewportHeight = canvasHeightDp,
                        imeBottom = imeBottomDp, objectKind = objectKind,
                        border = BorderStroke(1.dp, LingyeColors.Line.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            Modifier.height(36.dp).padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                val shapeIcon = when (shape.type) {
                                    "ellipse" -> AppIcon.ELLIPSE
                                    "diamond" -> AppIcon.DIAMOND
                                    "triangle" -> AppIcon.TRIANGLE
                                    "roundRect" -> AppIcon.ROUNDED_RECTANGLE
                                    else -> AppIcon.RECTANGLE
                                }
                                Row(
                                    Modifier
                                        .width(40.dp).height(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { shapeMenu = true }
                                        .semantics { contentDescription = tr(language, "修改形状", "Change shape") },
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    WorkspaceIcon(shapeIcon, null, Modifier.size(20.dp), LingyeColors.Ink)
                                    Spacer(Modifier.width(2.dp))
                                    SvgPathIcon(InlineToolbarIcons.ChevronDown, LingyeColors.Muted, Modifier.size(12.dp))
                                }
                                LingyeDropdownMenu(
                                    expanded = shapeMenu,
                                    onDismissRequest = { shapeMenu = false },
                                    modifier = Modifier.width(292.dp),
                                    offset = DpOffset((-126).dp, 18.dp)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        listOf(
                                            "rect" to (tr(language, "矩形", "Rectangle") to AppIcon.RECTANGLE),
                                            "ellipse" to (tr(language, "圆形", "Ellipse") to AppIcon.ELLIPSE),
                                            "diamond" to (tr(language, "菱形", "Diamond") to AppIcon.DIAMOND),
                                            "triangle" to (tr(language, "三角形", "Triangle") to AppIcon.TRIANGLE),
                                            "roundRect" to (tr(language, "圆角矩形", "Rounded rectangle") to AppIcon.ROUNDED_RECTANGLE)
                                        ).forEach { (type, spec) ->
                                            ToolIconButton(spec.second, spec.first, shape.type == type, false) {
                                                CanvasPropertyEditor.updateShape(page, index, onChanged) { it.copy(type = type) }
                                                shapeMenu = false
                                            }
                                        }
                                    }
                                }
                            }
                            TextToolbarSeparator()
                            Box {
                                TextToolbarIconButton(
                                    description = tr(language, "填充颜色", "Fill color"),
                                    onClick = { fillMenu = true }
                                ) {
                                    Box(
                                        Modifier.size(20.dp).background(Color(shape.color), CircleShape)
                                            .border(1.dp, LingyeColors.Line, CircleShape)
                                    )
                                }
                                LingyeDropdownMenu(
                                    expanded = fillMenu,
                                    onDismissRequest = { fillMenu = false },
                                    modifier = Modifier.width(292.dp),
                                    offset = DpOffset((-134).dp, 18.dp)
                                ) {
                                    ColorPaletteMenu(selected = shape.color, onPick = { color ->
                                        CanvasPropertyEditor.updateShape(page, index, onChanged) { it.copy(color = color) }
                                        fillMenu = false
                                    }, onRequestCustom = { fillMenu = false })
                                }
                            }
                            Box {
                                TextToolbarIconButton(
                                    description = tr(language, "边框颜色", "Border color"),
                                    onClick = { borderColorMenu = true }
                                ) {
                                    Box(
                                        Modifier.size(20.dp).background(Color.White, CircleShape)
                                            .border(4.dp, Color(shape.borderColor), CircleShape)
                                    )
                                }
                                LingyeDropdownMenu(
                                    expanded = borderColorMenu,
                                    onDismissRequest = { borderColorMenu = false },
                                    modifier = Modifier.width(292.dp),
                                    offset = DpOffset((-134).dp, 18.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(tr(language, "同填充", "Same as fill")) },
                                        leadingIcon = { Box(Modifier.size(16.dp).background(Color(shape.color), RoundedCornerShape(5.dp)).border(1.dp, LingyeColors.Line, RoundedCornerShape(5.dp))) },
                                        onClick = {
                                            CanvasPropertyEditor.updateShape(page, index, onChanged) { it.copy(borderColor = it.color) }
                                            borderColorMenu = false
                                        }
                                    )
                                    ColorPaletteMenu(selected = shape.borderColor, onPick = { color ->
                                        CanvasPropertyEditor.updateShape(page, index, onChanged) { it.copy(borderColor = color) }
                                        borderColorMenu = false
                                    }, onRequestCustom = { borderColorMenu = false })
                                }
                            }
                            Box {
                                TextToolbarIconButton(
                                    description = tr(language, "边框", "Border"),
                                    selected = shape.borderStyle != "none",
                                    onClick = { borderMenu = true }
                                ) { tint -> WorkspaceIcon(AppIcon.FIELD, null, Modifier.size(20.dp), tint) }
                                LingyeDropdownMenu(
                                    expanded = borderMenu,
                                    onDismissRequest = { borderMenu = false },
                                    modifier = Modifier.width(208.dp)
                                ) {
                                    listOf(
                                        "solid" to tr(language, "实线", "Solid"),
                                        "dashed" to tr(language, "虚线", "Dashed"),
                                        "none" to tr(language, "无边框", "No border")
                                    ).forEach { (style, label) ->
                                        DropdownMenuItem(text = { Text(label) }, onClick = { CanvasPropertyEditor.updateShape(page, index, onChanged) { it.copy(borderStyle = style) }; borderMenu = false })
                                    }
                                }
                            }
                            Box(
                                Modifier
                                    .width(52.dp).height(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        CanvasPropertyEditor.updateShape(page, index, onChanged) {
                                            it.copy(borderWidth = when {
                                                it.borderWidth < 1.5f -> 2f
                                                it.borderWidth < 3f -> 4f
                                                else -> 1f
                                            })
                                        }
                                    }
                                    .semantics { contentDescription = "${shape.borderWidth.roundToInt()} px" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${shape.borderWidth.roundToInt()} px",
                                    fontSize = 12.sp,
                                    color = LingyeColors.Ink,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            TextToolbarIconButton(
                                description = tr(language, "绘制连接线", "Draw connector"),
                                onClick = { onDrawConnector(index) }
                            ) { tint -> TextToolGlyph(TextToolGlyphKind.Connector, tint) }
                            TextToolbarSeparator()
                            TextToolbarIconButton(
                                description = tr(language, "复制图形", "Duplicate shape"),
                                onClick = {
                                    val copied = page.duplicateSelections(listOf(CanvasSelection("shape", index)))
                                    onSelect(copied.lastOrNull(), copied)
                                    onChanged()
                                }
                            ) { tint -> WorkspaceIcon(AppIcon.COPY, null, Modifier.size(20.dp), tint) }
                            Box {
                                TextToolbarIconButton(
                                    description = tr(language, "图层", "Layer"),
                                    onClick = { layerMenu = true }
                                ) { tint -> WorkspaceIcon(AppIcon.MORE, null, Modifier.size(20.dp), tint) }
                                LingyeDropdownMenu(
                                    expanded = layerMenu,
                                    onDismissRequest = { layerMenu = false },
                                    modifier = Modifier.width(208.dp)
                                ) {
                                    LayerMenuItems(language, showIcons = false) { move ->
                                        val moved = page.reorderSelection(CanvasSelection("shape", index), move)
                                        onSelect(moved, listOf(moved))
                                        layerMenu = false
                                        onChanged()
                                    }
                                }
                            }
                            TextToolbarSeparator()
                            TextToolbarIconButton(
                                description = tr(language, "锁定图形", "Lock shape"),
                                selected = shape.locked,
                                onClick = {
                                    page.shapes[index] = shape.copy(locked = !shape.locked)
                                    onChanged()
                                }
                            ) { tint -> WorkspaceIcon(AppIcon.LOCK, null, Modifier.size(20.dp), tint) }
                            TextToolbarIconButton(
                                description = tr(language, "删除图形", "Delete shape"),
                                onClick = {
                                    page.deleteSelection(CanvasSelection("shape", index))
                                    onSelect(null, emptyList())
                                    onChanged()
                                }
                            ) { _ -> WorkspaceIcon(AppIcon.TRASH, null, Modifier.size(20.dp), LingyeColors.Coral) }
                        }
                    }
                }
            }
        }
}
