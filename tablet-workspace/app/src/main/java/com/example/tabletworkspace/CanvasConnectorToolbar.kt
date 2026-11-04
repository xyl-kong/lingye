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
internal fun CanvasConnectorToolbar(
    index: Int?,
    visible: Boolean,
    page: Page,
    language: AppLanguage,
    viewport: Viewport,
    canvasWidthDp: Dp,
    canvasHeightDp: Dp,
    imeBottomDp: Dp,
    objectKind: CanvasObjectKind?,
    onSelect: (CanvasSelection?, List<CanvasSelection>) -> Unit,
    onChanged: () -> Unit,
    changedText: (String?) -> Unit
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(150)) + slideInVertically(tween(220)) { it / 3 }, exit = fadeOut(tween(100)) + slideOutVertically(tween(140)) { it / 3 }) {
        index?.let { index ->
                page.connectors.getOrNull(index)?.let { raw ->
                    var startMarkerMenu by remember(index) { mutableStateOf(false) }
                    var markerMenu by remember(index) { mutableStateOf(false) }
                    var pathMenu by remember(index) { mutableStateOf(false) }
                    var colorMenu by remember(index) { mutableStateOf(false) }
                    var widthMenu by remember(index) { mutableStateOf(false) }
                    var labelEditor by remember(index) { mutableStateOf(false) }
                    var labelDraft by remember(index) { mutableStateOf(raw.label) }
                    fun markerIcon(marker: String) = when (marker) {
                        "dot" -> AppIcon.END_DOT
                        "arrow" -> AppIcon.END_ARROW
                        "triangle" -> AppIcon.END_TRIANGLE
                        "diamond" -> AppIcon.END_DIAMOND
                        else -> AppIcon.END_NONE
                    }
                    val midpoint=viewport.toScreen(connectorProgress(page.resolvedConnector(index),0.5f))
                    ContextualToolbarHost(
                        anchor = CanvasToolbarAnchor(
                            x = midpoint.x.dp,
                            y = midpoint.y.dp,
                            bottom = midpoint.y.dp,
                            align = ToolbarAlign.Center
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
                                TextToolbarIconButton(
                                    description = tr(language, "描边颜色", "Stroke color"),
                                    onClick = { colorMenu = true }
                                ) {
                                    Box(Modifier.size(20.dp).background(Color(raw.color), CircleShape).border(1.dp, LingyeColors.Line, CircleShape))
                                }
                                LingyeDropdownMenu(
                                    expanded = colorMenu,
                                    onDismissRequest = { colorMenu = false },
                                    modifier = Modifier.width(292.dp),
                                    offset = DpOffset((-134).dp, 18.dp)
                                ) {
                                    ColorPaletteMenu(selected = raw.color, onPick = { color ->
                                        page.connectors[index] = raw.copy(color = color)
                                        onChanged()
                                        colorMenu = false
                                    })
                                }
                            }
                            Box {
                                Box(
                                    Modifier.width(48.dp).height(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { widthMenu = true }
                                        .semantics { contentDescription = "${raw.width.roundToInt()} px" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${raw.width.roundToInt()} px", fontSize = 12.sp, color = LingyeColors.Ink, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                                }
                                LingyeDropdownMenu(
                                    expanded = widthMenu,
                                    onDismissRequest = { widthMenu = false },
                                    modifier = Modifier.width(208.dp)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        listOf(2f, 4f, 6f, 8f, 10f, 12f).forEach { width ->
                                            CanvasParamWidthDot(width, raw.width == width) {
                                                page.connectors[index] = raw.copy(width = width)
                                                onChanged()
                                                widthMenu = false
                                            }
                                        }
                                    }
                                }
                            }
                            TextToolbarSeparator()
                            Box {
                                TextToolbarIconButton(
                                    description = tr(language, "起点样式", "Start point style"),
                                    onClick = { startMarkerMenu = true }
                                ) { tint -> WorkspaceIcon(markerIcon(raw.startMarker), null, Modifier.size(20.dp), tint) }
                                LingyeDropdownMenu(
                                    expanded = startMarkerMenu,
                                    onDismissRequest = { startMarkerMenu = false },
                                    modifier = Modifier.width(208.dp)
                                ) {
                                    ConnectorMarkerRow(language, raw.startMarker) { marker ->
                                        page.connectors[index] = raw.copy(startMarker = marker)
                                        onChanged()
                                        startMarkerMenu = false
                                    }
                                }
                            }
                            Box {
                                TextToolbarIconButton(
                                    description = tr(language, "终点样式", "End point style"),
                                    onClick = { markerMenu = true }
                                ) { tint -> WorkspaceIcon(markerIcon(raw.endMarker), null, Modifier.size(20.dp), tint) }
                                LingyeDropdownMenu(
                                    expanded = markerMenu,
                                    onDismissRequest = { markerMenu = false },
                                    modifier = Modifier.width(208.dp)
                                ) {
                                    ConnectorMarkerRow(language, raw.endMarker) { marker ->
                                        page.connectors[index] = raw.copy(endMarker = marker)
                                        onChanged()
                                        markerMenu = false
                                    }
                                }
                            }
                            TextToolbarIconButton(
                                description = tr(language, "翻转方向", "Flip direction"),
                                onClick = {
                                    page.connectors[index] = raw.copy(startMarker = raw.endMarker, endMarker = raw.startMarker)
                                    onChanged()
                                }
                            ) { tint -> WorkspaceIcon(AppIcon.REVERSE, null, Modifier.size(20.dp), tint) }
                            TextToolbarSeparator()
                            Box {
                                TextToolbarIconButton(
                                    description = tr(language, "连接形状", "Connector shape"),
                                    onClick = { pathMenu = true }
                                ) { tint ->
                                    WorkspaceIcon(
                                        if (raw.pathStyle == "straight") AppIcon.STRAIGHT else if (raw.pathStyle == "step") AppIcon.STEP else AppIcon.CURVE,
                                        null, Modifier.size(20.dp), tint
                                    )
                                }
                                LingyeDropdownMenu(
                                    expanded = pathMenu,
                                    onDismissRequest = { pathMenu = false },
                                    modifier = Modifier.width(208.dp)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        listOf(
                                            "curve" to (tr(language, "曲线", "Curve") to AppIcon.CURVE),
                                            "step" to (tr(language, "折线", "Elbow") to AppIcon.STEP),
                                            "straight" to (tr(language, "直线", "Straight") to AppIcon.STRAIGHT)
                                        ).forEach { (style, spec) ->
                                            ToolIconButton(spec.second, spec.first, raw.pathStyle == style, false) {
                                                page.connectors[index] = raw.copy(pathStyle = style, curved = style == "curve")
                                                onChanged()
                                                pathMenu = false
                                            }
                                        }
                                    }
                                }
                            }
                            TextToolbarIconButton(
                                description = tr(language, "添加文字", "Add text"),
                                onClick = { labelDraft = raw.label; labelEditor = true }
                            ) { tint -> WorkspaceIcon(AppIcon.EDIT, null, Modifier.size(20.dp), if (raw.label.isBlank()) tint else LingyeColors.Primary) }
                            TextToolbarIconButton(
                                description = tr(language, "光点动画", "Motion dot"),
                                selected = raw.animated,
                                onClick = { page.connectors[index] = raw.copy(animated = !raw.animated); onChanged() }
                            ) { tint -> WorkspaceIcon(AppIcon.SPARK, null, Modifier.size(20.dp), if (raw.animated) LingyeColors.Mint else tint) }
                            TextToolbarSeparator()
                            TextToolbarIconButton(
                                description = tr(language, "删除连线", "Delete connector"),
                                onClick = {
                                    page.connectors.removeAt(index)
                                    onSelect(null, emptyList())
                                    onChanged()
                                }
                            ) { _ -> WorkspaceIcon(AppIcon.TRASH, null, Modifier.size(20.dp), LingyeColors.Coral) }
                        }
                    }
                    if (labelEditor) AlertDialog(
                        onDismissRequest = { labelEditor = false },
                        title = { Text(tr(language, "连接线文字", "Connector label")) },
                        text = {
                            OutlinedTextField(
                                value = labelDraft,
                                onValueChange = { labelDraft = it.take(200) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(tr(language, "文字", "Text")) },
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                page.connectors[index] = page.connectors[index].copy(label = labelDraft.trim())
                                changedText("connector-label:${page.connectors[index].id}")
                                labelEditor = false
                            }) { Text(tr(language, "保存", "Save")) }
                        },
                        dismissButton = {
                            TextButton(onClick = { labelEditor = false }) { Text(tr(language, "取消", "Cancel")) }
                        }
                    )
                }
        }
    }
}
