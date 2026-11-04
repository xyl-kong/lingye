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
internal fun CanvasTextToolbar(
    index: Int?, visible: Boolean, page: Page, language: AppLanguage, scale: Float, viewport: Viewport,
    canvasWidthDp: Dp, canvasHeightDp: Dp, imeBottomDp: Dp, objectKind: CanvasObjectKind?,
    onSelect: (CanvasSelection?, List<CanvasSelection>) -> Unit, onChanged: () -> Unit,
    onDrawConnector: (Int) -> Unit
) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(140)) + slideInVertically(tween(210)) { -it / 3 },
            exit = fadeOut(tween(100)) + slideOutVertically(tween(130)) { -it / 3 }
        ) {
            index?.let { index ->
                page.shapes.getOrNull(index)?.let { text ->
                    var stylePanel by remember(index) { mutableStateOf(false) }
                    var scaleMenu by remember(index) { mutableStateOf(false) }
                    var scaleCustom by remember(index) { mutableStateOf(textScalePercent(text).toString()) }
                    var moreMenu by remember(index) { mutableStateOf(false) }
                    val context = LocalContext.current
                    val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
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
                        imeBottom = imeBottomDp,
                        objectKind = objectKind,
                        border = BorderStroke(1.dp, LingyeColors.Line.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            Modifier.height(36.dp).padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextToolbarLabelButton(
                                description = tr(language, "在页面中显示", "Display in Page"),
                                label = if (textDisplayInPage(text)) "Displayed in Page" else "Display in Page",
                                width = if (textDisplayInPage(text)) 172.dp else 136.dp,
                                icon = { tint -> SvgPathIcon(OfficialTextPaths.DisplayInPage, tint, Modifier.size(20.dp)) }
                            ) {
                                val next = !textDisplayInPage(text)
                                applyTextDisplayInPage(page, index, next, onChanged)
                                android.widget.Toast.makeText(
                                    context,
                                    tr(language, if (next) "已在页面中显示" else "已从页面移除", if (next) "Displayed in Page" else "Removed from Page"),
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                            TextToolbarSeparator()
                            Box {
                                TextToolbarIconButton(
                                    description = tr(language, "文本框样式", "Note Style"),
                                    onClick = { stylePanel = true }
                                ) { tint -> TextToolGlyph(TextToolGlyphKind.Palette, tint) }
                                LingyeDropdownMenu(
                                    expanded = stylePanel,
                                    onDismissRequest = { stylePanel = false },
                                    modifier = Modifier.width(292.dp),
                                    offset = DpOffset((-134).dp, 18.dp)
                                ) {
                                    NoteStylePanel(
                                        fillColor = text.color,
                                        shadowStyle = text.shadowStyle,
                                        borderWidth = text.borderWidth,
                                        borderStyle = text.borderStyle,
                                        cornerRadius = text.cornerRadius,
                                        language = language,
                                        onFill = { fill ->
                                            CanvasPropertyEditor.updateShape(page, index, onChanged) {
                                                it.copy(color = fill, textColor = readableTextColor(fill, it.textColor))
                                            }
                                        },
                                        onShadow = { value ->
                                            CanvasPropertyEditor.updateShape(page, index, onChanged) { it.copy(shadowStyle = value) }
                                        },
                                        onBorderWidth = { value ->
                                            CanvasPropertyEditor.updateShape(page, index, onChanged) {
                                                it.copy(borderWidth = value, borderStyle = if (it.borderStyle == "none") "solid" else it.borderStyle)
                                            }
                                        },
                                        onBorderStyle = { value ->
                                            CanvasPropertyEditor.updateShape(page, index, onChanged) {
                                                it.copy(borderStyle = value, borderWidth = if (value == "none") it.borderWidth else it.borderWidth.coerceAtLeast(2f))
                                            }
                                        },
                                        onCornerRadius = { value ->
                                            CanvasPropertyEditor.updateShape(page, index, onChanged) { it.copy(cornerRadius = value) }
                                        }
                                    )
                                }
                            }
                            TextToolbarSeparator()
                            TextToolbarIconButton(
                                description = "Auto height",
                                selected = textAutoHeight(text),
                                onClick = { toggleTextAutoHeight(page, index, onChanged) }
                            ) { tint -> TextToolGlyph(TextToolGlyphKind.Size, tint) }
                            TextToolbarSeparator()
                            Box {
                                TextToolbarScaleButton("${textScalePercent(text)}%") {
                                    scaleCustom = textScalePercent(text).toString()
                                    scaleMenu = true
                                }
                                LingyeDropdownMenu(
                                    expanded = scaleMenu,
                                    onDismissRequest = {
                                        scaleCustom.toIntOrNull()?.let { percent ->
                                            if (percent in 25..400) applyTextScale(page, index, percent, onChanged)
                                        }
                                        scaleMenu = false
                                    },
                                    modifier = Modifier.width(68.dp),
                                    offset = DpOffset((-1.5).dp, (-188).dp)
                                ) {
                                    Column(Modifier.padding(vertical = 4.dp)) {
                                        listOf(50, 100, 200).forEach { percent ->
                                            TextMenuRow(
                                                label = "$percent%",
                                                selected = textScalePercent(text) == percent
                                            ) {
                                                applyTextScale(page, index, percent, onChanged)
                                                scaleMenu = false
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Box(
                                            Modifier.fillMaxWidth().height(25.dp)
                                                .padding(horizontal = 4.dp)
                                                .background(LingyeColors.Surface, RoundedCornerShape(8.dp))
                                                .border(1.dp, LingyeColors.Line, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            BasicTextField(
                                                scaleCustom,
                                                { value -> if (value.length <= 3 && value.all(Char::isDigit)) scaleCustom = value },
                                                Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = LingyeColors.Ink),
                                                decorationBox = { inner ->
                                                    Box(contentAlignment = Alignment.CenterStart) {
                                                        if (scaleCustom.isEmpty()) Text("100", color = LingyeColors.Muted, fontSize = 13.sp)
                                                        inner()
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            TextToolbarSeparator()
                            TextToolbarIconButton(
                                description = tr(language, "绘制连接线", "Draw connector"),
                                onClick = { onDrawConnector(index) }
                            ) { tint -> TextToolGlyph(TextToolGlyphKind.Connector, tint) }
                            TextToolbarSeparator()
                            TextToolbarIconButton(
                                description = tr(language, "锁定文本", "Lock"),
                                onClick = {
                                    page.shapes[index] = text.copy(locked = true)
                                    onChanged()
                                }
                            ) { tint -> WorkspaceIcon(AppIcon.LOCK, null, Modifier.size(20.dp), tint) }
                            TextToolbarSeparator()
                            Box {
                                TextToolbarIconButton(
                                    description = tr(language, "更多", "More"),
                                    onClick = { moreMenu = true }
                                ) { tint -> TextToolGlyph(TextToolGlyphKind.More, tint) }
                                LingyeDropdownMenu(
                                    expanded = moreMenu,
                                    onDismissRequest = { moreMenu = false },
                                    modifier = Modifier.width(208.dp)
                                ) {
                                    Column(Modifier.padding(vertical = 4.dp)) {
                                        TextMenuRow(tr(language, "创建 Frame 区域", "Frame section"), enabled = false, icon = { tint -> WorkspaceIcon(AppIcon.FRAME, null, Modifier.size(18.dp), tint) }) {}
                                        TextMenuRow(tr(language, "创建分组区域", "Group section"), enabled = false, icon = { tint -> WorkspaceIcon(AppIcon.OVERVIEW, null, Modifier.size(18.dp), tint) }) {}
                                        TextMenuGroupDivider()
                                        layerMenuEntries(language).forEachIndexed { layerIndex, (move, label) ->
                                            TextMenuRow(label, icon = { tint -> WorkspaceIcon(if (layerIndex < 2) AppIcon.LAYER_UP else AppIcon.LAYER_DOWN, null, Modifier.size(18.dp), tint) }) {
                                                val moved = page.reorderSelection(CanvasSelection("shape", index), move)
                                                onSelect(moved, listOf(moved))
                                                moreMenu = false
                                                onChanged()
                                            }
                                        }
                                        TextMenuGroupDivider()
                                        TextMenuRow(tr(language, "复制", "Copy"), icon = { tint -> WorkspaceIcon(AppIcon.COPY, null, Modifier.size(18.dp), tint) }) {
                                            clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Lingye", text.body))
                                            android.widget.Toast.makeText(context, tr(language, "已复制", "Copied"), android.widget.Toast.LENGTH_SHORT).show()
                                            moreMenu = false
                                        }
                                        TextMenuRow(tr(language, "复制为图片", "Copy as Image"), enabled = false, icon = { tint -> WorkspaceIcon(AppIcon.IMAGE, null, Modifier.size(18.dp), tint) }) {}
                                        TextMenuRow(tr(language, "复制为 Markdown", "Copy as Markdown"), icon = { tint -> WorkspaceIcon(AppIcon.CODE_BLOCK, null, Modifier.size(18.dp), tint) }) {
                                            clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Lingye", text.body))
                                            android.widget.Toast.makeText(context, tr(language, "已复制为 Markdown", "Copied as Markdown"), android.widget.Toast.LENGTH_SHORT).show()
                                            moreMenu = false
                                        }
                                        TextMenuRow(tr(language, "复制块链接", "Copy link to block"), icon = { tint -> WorkspaceIcon(AppIcon.LINK, null, Modifier.size(18.dp), tint) }) {
                                            clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Lingye", "lingye://page/${page.id}?shape=${text.id}"))
                                            android.widget.Toast.makeText(context, tr(language, "已复制链接", "Link copied"), android.widget.Toast.LENGTH_SHORT).show()
                                            moreMenu = false
                                        }
                                        TextMenuRow(tr(language, "创建副本", "Duplicate"), icon = { tint -> WorkspaceIcon(AppIcon.COPY, null, Modifier.size(18.dp), tint) }) {
                                            val copied = page.duplicateSelections(listOf(CanvasSelection("shape", index)))
                                            onSelect(copied.lastOrNull(), copied)
                                            moreMenu = false
                                            onChanged()
                                        }
                                        TextMenuGroupDivider()
                                        TextMenuRow(tr(language, "转为链接文档", "Turn into linked doc"), enabled = false, icon = { tint -> WorkspaceIcon(AppIcon.DOCUMENT, null, Modifier.size(18.dp), tint) }) {}
                                        TextMenuGroupDivider()
                                        TextMenuRow(tr(language, "删除文本", "Delete"), danger = true, icon = { tint -> WorkspaceIcon(AppIcon.TRASH, null, Modifier.size(18.dp), tint) }) {
                                            page.deleteSelection(CanvasSelection("shape", index))
                                            onSelect(null, emptyList())
                                            moreMenu = false
                                            onChanged()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
}

/** Geometry snapshot the adjacent +/- controls position against; a value type so a moved
 *  or resized text object always recomposes them (the Page instance itself does not change). */
internal data class AdjacentTextAnchor(val x: Float, val y: Float, val width: Float, val height: Float, val locked: Boolean)

@Composable
internal fun CanvasTextAdjacentControls(
    index: Int?, anchor: AdjacentTextAnchor?, selectedItemsCount: Int, cardEditActive: Boolean, toolIsSelect: Boolean, toolbarMenuOpen: Boolean,
    dragging: Boolean,
    page: Page, language: AppLanguage, scale: Float, viewport: Viewport,
    canvasWidthDp: Dp, canvasHeightDp: Dp,
    onSelect: (CanvasSelection?, List<CanvasSelection>) -> Unit, onEditCard: (Int) -> Unit, onChanged: () -> Unit
) {
        if (index != null && anchor != null && !anchor.locked && selectedItemsCount <= 1 && !cardEditActive && toolIsSelect && !toolbarMenuOpen && !dragging) {
            index?.let { index ->
                fun addAdjacent(direction: Int) {
                    val source = page.shapes.getOrNull(index) ?: return
                    val gap = 24f
                    val x = if (direction < 0) (source.x - source.width - gap).coerceAtLeast(0f) else source.x + source.width + gap
                    page.shapes.add(
                        source.copy(
                            id = java.util.UUID.randomUUID().toString(),
                            x = x,
                            body = "",
                            title = "",
                            textSpans = emptyList()
                        )
                    )
                    val target = CanvasSelection("shape", page.shapes.lastIndex)
                    onSelect(target, listOf(target))
                    onEditCard(page.shapes.lastIndex)
                    onChanged()
                }
                val isCard = page.shapes.getOrNull(index)?.type == "card"
                val at = viewport.toScreen(Point(anchor.x, anchor.y))
                val boxWidth = (anchor.width * scale).dp
                val boxHeight = (anchor.height * scale).dp
                val plusSize = 24.dp
                val buttonY = (at.y.dp + boxHeight / 2 - plusSize / 2)
                    .coerceIn(8.dp, (canvasHeightDp - plusSize - 8.dp).coerceAtLeast(8.dp))
                val leftX = at.x.dp - plusSize - 16.dp
                val rightX = at.x.dp + boxWidth + 16.dp
                val leftLabel = if (isCard) tr(language, "向左添加笔记", "Add note left") else tr(language, "向左添加文本", "Add text left")
                val rightLabel = if (isCard) tr(language, "向右添加笔记", "Add note right") else tr(language, "向右添加文本", "Add text right")
                // Hide a side button instead of clamping it on top of the object at the canvas edge.
                if (leftX >= 8.dp) TextAdjacentPlus(leftX, buttonY, leftLabel) { addAdjacent(-1) }
                if (rightX + plusSize <= canvasWidthDp - 8.dp) TextAdjacentPlus(rightX, buttonY, rightLabel) { addAdjacent(1) }
            }
        }
        if (index != null && anchor != null && anchor.locked && !cardEditActive && toolIsSelect && !toolbarMenuOpen) {
            index?.let { index ->
                val shape = page.shapes.getOrNull(index) ?: return@let
                val at = viewport.toScreen(Point(anchor.x, anchor.y))
                val badgeX = (at.x.dp + (anchor.width * scale).dp - 28.dp)
                    .coerceIn(8.dp, (canvasWidthDp - 32.dp).coerceAtLeast(8.dp))
                val badgeY = (at.y.dp - 28.dp).coerceAtLeast(8.dp)
                Surface(
                    onClick = {
                        page.shapes[index] = shape.copy(locked = false)
                        onChanged()
                    },
                    modifier = Modifier.offset(x = badgeX, y = badgeY).size(24.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = LingyeColors.SurfaceHigh,
                    border = BorderStroke(1.dp, LingyeColors.Line),
                    shadowElevation = 2.dp
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        WorkspaceIcon(AppIcon.LOCK, tr(language, "解锁", "Unlock"), Modifier.size(14.dp), LingyeColors.Muted)
                    }
                }
            }
        }
}
