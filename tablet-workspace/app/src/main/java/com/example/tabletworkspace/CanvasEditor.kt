@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageStore
import com.example.tabletworkspace.platform.image.ImageSampleBuckets

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
internal fun MindmapLayoutPreview(layout: String, modifier: Modifier = Modifier) {    Canvas(modifier) {
        val ink = LingyeColors.Muted
        val stroke = 1.2.dp.toPx()
        val dot = 1.5.dp.toPx()
        fun p(x: Float, y: Float) = Offset(size.width * x, size.height * y)
        fun node(x: Float, y: Float, radius: Float = dot) { drawCircle(ink, radius, p(x, y)) }
        fun link(x1: Float, y1: Float, x2: Float, y2: Float) { drawLine(ink, p(x1, y1), p(x2, y2), stroke, StrokeCap.Round) }
        when (layout) {
            "bubble" -> {
                node(.5f, .5f, 2.dp.toPx())
                listOf(.5f to .1f, .5f to .9f, .1f to .5f, .9f to .5f).forEach { (x, y) ->
                    link(.5f, .5f, x, y); node(x, y)
                }
            }
            "down" -> {
                node(.5f, .12f, 2.dp.toPx())
                link(.5f, .12f, .5f, .46f)
                listOf(.22f, .78f).forEach { x ->
                    link(.5f, .46f, x, .46f); link(x, .46f, x, .86f); node(x, .86f)
                }
            }
            "tree" -> {
                node(.5f, .12f, 2.dp.toPx())
                link(.5f, .12f, .5f, .45f)
                link(.2f, .45f, .8f, .45f)
                listOf(.2f, .5f, .8f).forEach { x -> link(x, .45f, x, .78f); node(x, .78f) }
            }
            "outline" -> {
                listOf(.2f, .5f, .8f).forEach { y ->
                    node(.12f, y, 1.8.dp.toPx()); link(.24f, y, .88f, y)
                }
            }
            else -> {
                node(.14f, .5f, 2.dp.toPx())
                link(.14f, .5f, .44f, .5f)
                listOf(.28f, .72f).forEach { y ->
                    link(.44f, .5f, .44f, y); link(.44f, y, .86f, y); node(.86f, y)
                }
            }
        }
    }
}

internal class TextSpanVisualTransformation(private val spans: List<TextSpan>) :
    androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val styled = buildAnnotatedString {
            append(text)
            spans.forEach { span ->
                val start = span.start.coerceIn(0, text.length)
                val end = span.end.coerceIn(0, text.length)
                if (start < end) {
                    val decorations = buildList {
                        if (span.flags and TextSpan.UNDERLINE != 0 || span.flags and TextSpan.LINK != 0) add(TextDecoration.Underline)
                        if (span.flags and TextSpan.STRIKE != 0) add(TextDecoration.LineThrough)
                    }
                    val code = span.flags and TextSpan.CODE != 0
                    val math = span.flags and TextSpan.MATH != 0
                    val highlightBg = TextHighlightPalette.backgroundColor(TextSpan.background(span.flags))
                    val background = highlightBg
                        ?: if (span.flags and TextSpan.COMMENT != 0) 0xFFFFF3C4.toInt() else null
                        ?: if (code) 0xFFF1F1F1.toInt() else null
                    val foreground = if (span.flags and TextSpan.LINK != 0) 0xFF1E96EB.toInt()
                    else TextHighlightPalette.foregroundColor(TextSpan.foreground(span.flags))
                    addStyle(
                        SpanStyle(
                            fontWeight = if (span.flags and TextSpan.BOLD != 0) FontWeight.Bold else null,
                            fontStyle = if (math || span.flags and TextSpan.ITALIC != 0) FontStyle.Italic else null,
                            textDecoration = decorations.takeIf { it.isNotEmpty() }?.let { TextDecoration.combine(it) },
                            fontFamily = if (code) FontFamily.Monospace else if (math) FontFamily.Serif else null,
                            color = if (foreground != null) Color(foreground) else Color.Unspecified,
                            background = if (background != null) Color(background) else Color.Unspecified
                        ),
                        start,
                        end
                    )
                }
            }
        }
        return androidx.compose.ui.text.input.TransformedText(
            styled,
            androidx.compose.ui.text.input.OffsetMapping.Identity
        )
    }
}

@Composable
private fun SystemBarsEffect(focusMode: Boolean) {
    val activity = LocalContext.current as? Activity
    val dark = LingyeColors.dark
    DisposableEffect(focusMode, dark) {
        val controller = activity?.let { WindowCompat.getInsetsController(it.window, it.window.decorView) }
        controller?.isAppearanceLightStatusBars = !dark
        controller?.isAppearanceLightNavigationBars = !dark
        if (focusMode) {
            controller?.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            if (focusMode) controller?.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

internal data class ConnectorRenderData(
    val line: Connector,
    val displayLine: Connector,
    val path: Path,
    val x: Offset,
    val y: Offset,
    val samples: List<Offset>,
    val selected: Boolean
)

@Composable
@androidx.compose.runtime.NonSkippableComposable
internal fun CanvasEditor(page: Page, store: PageStore, pages: List<Page>, workspaceTemplates: List<CardTemplate>, sharedBlocks:List<SharedBlock>, templatesAvailable: Boolean, replaceTemplates: (List<CardTemplate>) -> Unit, revision: Int, language: AppLanguage, settings: WorkspaceSettings, focusMode: Boolean, setFocusMode: (Boolean) -> Unit, beginGesture: () -> Unit, commitGesture: () -> Unit, cancelGesture: () -> Unit, tool: String, setTool: (String) -> Unit, penOnly: Boolean, setPenOnly: (Boolean) -> Unit, eraserArea: Boolean, setEraserArea: (Boolean) -> Unit, brushKind: String, setBrushKind: (String) -> Unit, highlighterColor: Int, setHighlighterColor: (Int) -> Unit, highlighterWidth: Float, setHighlighterWidth: (Float) -> Unit, inkColor: Int, setInkColor: (Int) -> Unit, inkWidth: Float, setInkWidth: (Float) -> Unit, updateSettings: (WorkspaceSettings) -> Unit, changed: () -> Unit, changedText: (String?) -> Unit, redraw: () -> Unit, addImage: () -> Unit, undo: () -> Unit, redo: () -> Unit, exitCanvas: () -> Unit, onOpenLinkedPage: (String) -> Unit, initialScale: Float, initialOffsetX: Float, initialOffsetY: Float, initialSelectionIds: List<String>, onViewportChange: (Float, Float, Float) -> Unit, onSelectionChange: (List<String>) -> Unit, showFrameNavigator: Boolean, onFrameNavigatorShown: () -> Unit) {
    val editorState = remember(page.id) { CanvasEditorState(page, initialScale, initialOffsetX, initialOffsetY) }
    var scale by editorState.scale
    var pan by editorState.pan
    var snapToGrid by remember(page.id) { mutableStateOf(settings.snapToGridDefault) }
    LaunchedEffect(settings.snapToGridDefault) { snapToGrid = settings.snapToGridDefault }
    var selectionLayerMenu by editorState.selectionLayerMenu
    var cropTarget by editorState.cropTarget;var cropLeft by editorState.cropLeft;var cropTop by editorState.cropTop;var cropRight by editorState.cropRight;var cropBottom by editorState.cropBottom
    var points by editorState.points; var selection: CanvasSelection? by editorState.selection; var multiSelect by editorState.multiSelect; var selectedItems: List<CanvasSelection> by editorState.selectedItems; var endpointDrag by editorState.endpointDrag; var endpointPreview by editorState.endpointPreview; var endpointSnap by editorState.endpointSnap; var connectorDraft by editorState.connectorDraft; var connectorQuickSource by editorState.connectorQuickSource; var snapGuides by editorState.snapGuides; var dragSnapStart by editorState.dragSnapStart; var dragSnapOthers by editorState.dragSnapOthers; var dragSnapAccum by editorState.dragSnapAccum; var resizeAnchor by editorState.resizeAnchor; var resizeSnapshot by editorState.resizeSnapshot; var resizeGrabOffset by editorState.resizeGrabOffset; var resizeAxis by editorState.resizeAxis; var boxStart by editorState.boxStart; var boxEnd by editorState.boxEnd; var dragging by editorState.dragging; val density = LocalDensity.current.density
    var selectionRestored by editorState.selectionRestored
    var templateGalleryOpen by editorState.templateGalleryOpen
    var templateGalleryCategory by editorState.templateGalleryCategory
    var contentButtonBounds by remember(page) { mutableStateOf<IntRect?>(null) }
    val templateScope = rememberCoroutineScope()
    var moreToolsMenu by editorState.moreToolsMenu
    var textToolsMenu by editorState.textToolsMenu
    var pendingTextKind by editorState.pendingTextKind
    var linkedDocPicker by editorState.linkedDocPicker
    var mindmapToolsMenu by editorState.mindmapToolsMenu
    var activeMenuButton by editorState.activeMenuButton
    fun dismissToolMenus() {
        textToolsMenu = false
        templateGalleryOpen = false
        mindmapToolsMenu = false
        moreToolsMenu = false
        activeMenuButton = null
    }
    fun chooseTool(name: String) {
        dismissToolMenus()
        activeMenuButton = null
        setTool(name)
    }
    fun selectMenuOption(option: String) {
        dismissToolMenus()
        activeMenuButton = option
        setTool("选择")
    }
    val toolbarMenuOpen = textToolsMenu || templateGalleryOpen || mindmapToolsMenu || moreToolsMenu
    var frameRatio by editorState.frameRatio
    var viewportSizePx by editorState.viewportSizePx
    val canvasFocusRequester = remember { FocusRequester() }
    val inlineEditFocusRequester = remember { FocusRequester() }
    var frameRename by editorState.frameRename; var frameTitleText by editorState.frameTitleText
    var frameNavigator by editorState.frameNavigator
    LaunchedEffect(showFrameNavigator) {
        if (showFrameNavigator) {
            frameNavigator = true
            onFrameNavigatorShown()
        }
    }
    var presentingFrameId by editorState.presentingFrameId
    var presentationWasFocus by editorState.presentationWasFocus
    var presentationPrevTool by editorState.presentationPrevTool
    var presentationPrevScale by editorState.presentationPrevScale
    var presentationPrevPan by editorState.presentationPrevPan
    var presentationFitMode by editorState.presentationFitMode
    var mindmapParentPicker by editorState.mindmapParentPicker
    var mindmapDeleteId by editorState.mindmapDeleteId
    fun insertMindmap(layout: String) {
        val root = Mindmap.createRoot(page, 420f, 250f, tr(language, "中心主题", "Central topic"), settings.shapeColor, layout)
        (1..3).forEach { index ->
            Mindmap.addNode(page, root, tr(language, "分支 $index", "Branch $index"))
        }
        Mindmap.reflow(page, root, layout)
        changed()
        selection = null
        selectedItems = emptyList()
    }
    var cardEdit by editorState.cardEdit
    var textEditValue by editorState.textEditValue
    LaunchedEffect(cardEdit, page.id) {
        val shape = cardEdit?.let { page.shapes.getOrNull(it) }
        if (shape?.type == "text") {
            val body = shape.body
            textEditValue = TextFieldValue(body, androidx.compose.ui.text.TextRange(body.length))
        } else {
            textEditValue = null
        }
    }
    var manageTemplates by editorState.manageTemplates; var renameTemplate by editorState.renameTemplate; var removeTemplate by editorState.removeTemplate
    var templateName by editorState.templateName
    var templateSave by editorState.templateSave
    var saveSelectionTemplate by editorState.saveSelectionTemplate
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(cardEdit) {
        if (cardEdit != null) {
            withFrameNanos { }
            runCatching { inlineEditFocusRequester.requestFocus() }
            keyboardController?.show()
        }
    }
    LaunchedEffect(page.shapes.size, cardEdit) {
        val index = cardEdit ?: return@LaunchedEffect
        if (page.shapes.getOrNull(index)?.type !in setOf("card", "text", Mindmap.NODE_TYPE)) cardEdit = null
    }
    fun openTextLink(url: String) {
        val pageId = url.removePrefix("lingye://page/")
        if (url.startsWith("lingye://page/") && pageId.isNotBlank()) {
            onOpenLinkedPage(pageId)
        } else {
            runCatching {
                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
            }
        }
    }
    fun enterConnectorTool(quickSource: Int? = null) {
        if (cardEdit != null) cardEdit = null
        keyboardController?.hide()
        connectorQuickSource = quickSource
        chooseTool("连线")
    }
    LaunchedEffect(page.id) {
        val restored = page.restoreSelections(initialSelectionIds)
        if (restored.isNotEmpty()) {
            selectedItems = restored
            selection = restored.lastOrNull()
        }
        selectionRestored = true
    }
    LaunchedEffect(scale, pan) {
        delay(250)
        onViewportChange(scale, pan.x, pan.y)
    }
    SideEffect {
        if (selectionRestored) {
            onSelectionChange(page.selectionStableIds(selectedItems))
        }
    }
    SystemBarsEffect(focusMode)
    val motionEnabled = settings.motionEnabled
    var motionTime by remember { mutableLongStateOf(0L) }
    val systemMotion = android.provider.Settings.Global.getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    LaunchedEffect(page, revision, motionEnabled, systemMotion) {
        if (motionEnabled && systemMotion && page.connectors.any { it.animated }) while (true) {
            if ((context as? ComponentActivity)?.lifecycle?.currentState?.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) == true) withFrameNanos { motionTime=it } else delay(200)
        }
    }
    var lastNavigationTool by editorState.lastNavigationTool
    LaunchedEffect(tool) { if (tool == "选择" || tool == "移动") lastNavigationTool = tool }
    LaunchedEffect(tool) { if (tool != "选择") activeMenuButton = null }
    LaunchedEffect(tool) { snapGuides = emptyList(); dragSnapStart = null }
    LaunchedEffect(tool) { if (tool != "选择" && tool != "移动" && tool != "文本") textToolsMenu = false }
    val viewport = Viewport(scale, pan.x / density, pan.y / density)
    val toolbarDensity = LocalDensity.current
    val canvasWidthDp = (viewportSizePx.width / density).dp
    val canvasHeightDp = (viewportSizePx.height / density).dp
    val imeBottomDp = (WindowInsets.ime.getBottom(toolbarDensity) / density).dp
    val activeInkColor = BrushSettings.paintColor(brushKind, if (brushKind == BrushSettings.KIND_HIGHLIGHTER) highlighterColor else inkColor)
    val activeInkWidth = BrushSettings.paintWidth(brushKind, inkWidth, highlighterWidth)
    val canvasBitmaps = page.images.associate { image ->
        val target = ImageSampleBuckets.bucketFor(maxOf(image.width, image.height) * scale * density)
        image.name to rememberStoredImage(store, image.name, target)
    }
    fun world(p: Offset): Point = Viewport(scale, pan.x / density, pan.y / density).toWorld(Point(p.x / density, p.y / density))
    val targets = CanvasSelectionTargets.of(page, selection, selectedItems.size)
    val primaryObjectKind = CanvasObjectSpecs.kindOf(page, selection)
    fun closeCanvasOverlays() {
        moreToolsMenu = false; textToolsMenu = false; mindmapToolsMenu = false; templateGalleryOpen = false
        frameNavigator = false; linkedDocPicker = false; mindmapParentPicker = null; mindmapDeleteId = null
        frameRename = null; manageTemplates = false; renameTemplate = null; removeTemplate = null
        templateSave = null; saveSelectionTemplate = false; cropTarget = null
    }
    val commands = CanvasEditorCommands(
        page = page,
        state = editorState,
        density = density,
        focusMode = focusMode,
        tool = tool,
        setTool = setTool,
        setFocusMode = setFocusMode,
        changed = changed,
        closeOverlays = ::closeCanvasOverlays
    )
    val shortcutActions = CanvasShortcutActions(
        stopPresentation = commands::stopPresentation,
        shiftPresentation = commands::shiftPresentation,
        closeOverlays = ::closeCanvasOverlays,
        duplicateSelection = commands::duplicateSelection,
        groupSelection = commands::groupSelection,
        reorderSelection = commands::reorderSelection,
        alignSelection = commands::alignSelection,
        nudgeSelection = commands::nudge,
        undo = undo,
        redo = redo,
        deleteSelection = commands::deleteSelection,
        zoomBy = commands::zoomBy,
        selectAll = commands::selectAll,
        dismissMenus = ::dismissToolMenus,
        setTool = setTool,
        chooseTool = ::chooseTool,
        selectMenuOption = ::selectMenuOption,
        setBrushKind = setBrushKind,
        setFocusMode = setFocusMode
    )
    fun handleCanvasShortcut(event: KeyEvent): Boolean =
        handleCanvasShortcutEvent(event, editorState, tool, focusMode, shortcutActions)
    LaunchedEffect(cardEdit, moreToolsMenu, textToolsMenu, mindmapToolsMenu, templateGalleryOpen, manageTemplates,
        frameNavigator, frameRename, linkedDocPicker, mindmapParentPicker, mindmapDeleteId, templateSave, saveSelectionTemplate, cropTarget) {
        val blocked = cardEdit != null || moreToolsMenu || textToolsMenu || mindmapToolsMenu || templateGalleryOpen || manageTemplates ||
            frameNavigator || frameRename != null || linkedDocPicker || mindmapParentPicker != null || mindmapDeleteId != null ||
            templateSave != null || saveSelectionTemplate || cropTarget != null
        if (!blocked) runCatching { canvasFocusRequester.requestFocus() }
    }
    BackHandler {
        if (presentingFrameId != null) {
            commands.stopPresentation()
            return@BackHandler
        }
        val overlayOpen = moreToolsMenu || textToolsMenu || mindmapToolsMenu || templateGalleryOpen || manageTemplates ||
            renameTemplate != null || removeTemplate != null || templateSave != null ||
            saveSelectionTemplate || cropTarget != null || frameNavigator || frameRename != null || linkedDocPicker || mindmapParentPicker != null || mindmapDeleteId != null
        val action = BackNavigationPolicy.resolve(
            BackState(
                overlayOpen = overlayOpen,
                inlineEditing = cardEdit != null,
                fullscreen = focusMode,
                selectionActive = selection != null || selectedItems.isNotEmpty(),
                activeTool = tool,
                view = WorkspaceView.WHITEBOARD
            )
        )
        when (action) {
            BackAction.DISMISS_OVERLAY -> {
                moreToolsMenu = false
                textToolsMenu = false
                mindmapToolsMenu = false
                templateGalleryOpen = false
                activeMenuButton = null
                manageTemplates = false
                renameTemplate = null
                removeTemplate = null
                templateSave = null
                saveSelectionTemplate = false
                cropTarget = null
                mindmapParentPicker = null
                mindmapDeleteId = null
            }
            BackAction.FINISH_INLINE_EDIT -> {
                cardEdit = null
                keyboardController?.hide()
            }
            BackAction.EXIT_FULLSCREEN -> setFocusMode(false)
            BackAction.CLEAR_SELECTION -> {
                selection = null
                selectedItems = emptyList()
            }
            BackAction.RESET_TOOL -> setTool("移动")
            BackAction.LEAVE_WHITEBOARD -> exitCanvas()
            BackAction.LEAVE_SETTINGS,
            BackAction.LEAVE_OVERVIEW,
            BackAction.EXIT_APP -> exitCanvas()
        }
    }
    val canvasDarkTheme = settings.canvasTheme == "dark" || (settings.canvasTheme == "auto" && LingyeColors.dark)
    val canvasSurfaceColor = if (canvasDarkTheme) 0xff1c1d22.toInt() else settings.canvasBackground
    Box(Modifier.fillMaxSize().clipToBounds().background(Color(canvasSurfaceColor))) {
        if (presentingFrameId == null) Surface(
            onClick = { setFocusMode(!focusMode) },
            modifier = Modifier.align(Alignment.TopEnd).zIndex(3f).padding(12.dp).size(42.dp),
            shape = RoundedCornerShape(15.dp),
            color = LingyeColors.SurfaceHigh.copy(alpha = 0.96f),
            shadowElevation = 8.dp
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                WorkspaceIcon(
                    if (focusMode) AppIcon.EXPAND_CLOSE else AppIcon.EXPAND_FULL,
                    contentDescription = tr(language, if (focusMode) "退出全屏" else "全屏画布", if (focusMode) "Exit fullscreen" else "Fullscreen canvas"),
                    modifier = Modifier.size(20.dp),
                    tint = LingyeColors.Ink
                )
            }
        }
        Canvas(Modifier.fillMaxSize().onSizeChanged { viewportSizePx = it }.focusRequester(canvasFocusRequester).onPreviewKeyEvent { handleCanvasShortcut(it) }.focusable().onFocusChanged { if (!it.isFocused) { snapGuides = emptyList(); dragSnapStart = null } }.pointerInput(mindmapToolsMenu, templateGalleryOpen, moreToolsMenu) { if (mindmapToolsMenu || templateGalleryOpen || moreToolsMenu) awaitPointerEventScope { while (true) { val event = awaitPointerEvent(PointerEventPass.Initial); if (event.type == PointerEventType.Press) { chooseTool("选择"); event.changes.forEach { it.consume() } } } } }.pointerInput(tool) { if (tool == "Frame" || tool in setOf("矩形", "圆角矩形", "圆形", "三角形", "菱形")) awaitPointerEventScope { while (true) { val downEvent = awaitPointerEvent(PointerEventPass.Final); val down = downEvent.changes.firstOrNull { it.pressed && !it.previousPressed } ?: continue; val start = down.position; var moved = 0f; var ended = false; while (!ended) { val e = awaitPointerEvent(PointerEventPass.Final); val c = e.changes.firstOrNull { it.id == down.id } ?: break; moved = maxOf(moved, (c.position - start).getDistance()); if (!c.pressed) ended = true }; if (ended && moved <= viewConfiguration.touchSlop) chooseTool("选择") } } }.pointerInput(page, tool, multiSelect, presentingFrameId) { if (presentingFrameId == null && tool == "选择") detectTapGestures(onDoubleTap = { tap ->
            handleCanvasDoubleTap(
                tap = tap,
                page = page,
                scale = scale,
                worldPoint = world(tap),
                onOpenLinkedPage = onOpenLinkedPage,
                onSelect = { selected, items -> selection = selected; selectedItems = items },
                onEditCard = { cardEdit = it }
            )
        }, onTap = { tap ->
            handleCanvasTap(
                tap = tap,
                page = page,
                scale = scale,
                density = density,
                viewport = viewport,
                worldPoint = world(tap),
                selection = selection,
                selectedItems = selectedItems,
                multiSelect = multiSelect,
                onFocusCanvas = { runCatching { canvasFocusRequester.requestFocus() } },
                onCardEditChange = { cardEdit = it },
                onOpenTextLink = ::openTextLink,
                onSelect = { selected, items -> selection = selected; selectedItems = items },
                onChanged = changed
            )
        }) }
        .canvasGestureInput(
            page = page,
            language = language,
            settings = settings,
            tool = tool,
            penOnly = penOnly,
            eraserArea = eraserArea,
            brushKind = brushKind,
            highlighterColor = highlighterColor,
            highlighterWidth = highlighterWidth,
            inkColor = inkColor,
            inkWidth = inkWidth,
            snapToGrid = snapToGrid,
            multiSelect = multiSelect,
            pendingTextKind = pendingTextKind,
            frameRatio = frameRatio,
            presentingFrameId = presentingFrameId,
            density = density,
            state = editorState,
            beginGesture = beginGesture,
            commitGesture = commitGesture,
            cancelGesture = cancelGesture,
            changed = changed,
            redraw = redraw,
            chooseTool = ::chooseTool
        )
        ) {
            drawCanvasContent(
                page = page,
                pages = pages,
                language = language,
                settings = settings,
                scale = scale,
                pan = pan,
                density = density,
                viewport = viewport,
                canvasBitmaps = canvasBitmaps,
                canvasDarkTheme = canvasDarkTheme,
                presentingFrameId = presentingFrameId,
                revision = revision,
                points = points,
                tool = tool,
                eraserArea = eraserArea,
                activeInkColor = activeInkColor,
                activeInkWidth = activeInkWidth,
                selection = selection,
                selectedItems = selectedItems,
                dragging = dragging,
                boxStart = boxStart,
                boxEnd = boxEnd,
                connectorQuickSource = connectorQuickSource,
                snapGuides = snapGuides,
                endpointPreview = endpointPreview,
                connectorDraft = connectorDraft,
                endpointDrag = endpointDrag,
                endpointSnap = endpointSnap,
                cardEdit = cardEdit,
                toolbarMenuOpen = toolbarMenuOpen,
                textEditValue = textEditValue,
                motionEnabled = motionEnabled,
                motionTime = motionTime,
                systemMotion = systemMotion
            )
        }
        CanvasObjectToolbarHost(
            page = page,
            pages = pages,
            language = language,
            scale = scale,
            viewport = viewport,
            canvasWidthDp = canvasWidthDp,
            canvasHeightDp = canvasHeightDp,
            imeBottomDp = imeBottomDp,
            objectKind = primaryObjectKind,
            tool = tool,
            toolbarMenuOpen = toolbarMenuOpen,
            dragging = dragging,
            geometryRevision = revision,
            selectedItemsCount = selectedItems.size,
            selectedCardIndex = targets.card,
            selectedShapeIndex = targets.shape,
            selectedPageLinkIndex = targets.pageLink,
            selectedMindNodeIndex = targets.mindNode,
            selectedFrameIndex = targets.frame,
            selectedTextIndex = targets.text,
            selectedImageIndex = targets.image,
            selectedStrokeIndex = targets.stroke,
            selectedConnectorIndex = targets.connector,
            cardEdit = cardEdit,
            textEditValueState = editorState.textEditValue,
            onCardEditChange = { cardEdit = it },
            onSaveTemplate = { templateName = ""; templateSave = it },
            onSelect = { selected, items -> selection = selected; selectedItems = items },
            onChanged = changed,
            onOpenLinkedPage = onOpenLinkedPage,
            onParentPicker = { mindmapParentPicker = it },
            onDeleteNode = { mindmapDeleteId = it },
            onRenameFrame = { i, title -> frameTitleText = title; frameRename = i },
            onFitFrame = { commands.fitFrameViewport(it) },
            onFrameNavigator = { frameNavigator = true },
            onPresentFrame = { commands.startPresentation(it) },
            onCropImage = { id, left, top, right, bottom ->
                cropTarget = id
                cropLeft = left
                cropTop = top
                cropRight = right
                cropBottom = bottom
            },
            onDrawConnector = { enterConnectorTool(it) },
            hideKeyboard = { keyboardController?.hide() },
            changedText = changedText
        )
        val paramBarMaxWidth = CanvasLayout.parameterBarMaxWidth(viewportSizePx.width / density).dp
        val frameBarShift = if (paramBarMaxWidth.value > 420f) (-110).dp else 0.dp
        if (presentingFrameId == null) {
            CanvasBottomBar(
                page = page,
                store = store,
                context = context,
                templateScope = templateScope,
                workspaceTemplates = workspaceTemplates,
                templatesAvailable = templatesAvailable,
                language = language,
                settings = settings,
                updateSettings = updateSettings,
                focusMode = focusMode,
                setFocusMode = setFocusMode,
                tool = tool,
                setTool = setTool,
                chooseTool = ::chooseTool,
                enterConnectorTool = ::enterConnectorTool,
                dismissToolMenus = ::dismissToolMenus,
                selectMenuOption = ::selectMenuOption,
                insertMindmap = ::insertMindmap,
                addImage = addImage,
                changed = changed,
                undo = undo,
                redo = redo,
                startPresentation = commands::startPresentation,
                penOnly = penOnly,
                setPenOnly = setPenOnly,
                eraserArea = eraserArea,
                setEraserArea = setEraserArea,
                brushKind = brushKind,
                setBrushKind = setBrushKind,
                highlighterColor = highlighterColor,
                setHighlighterColor = setHighlighterColor,
                highlighterWidth = highlighterWidth,
                setHighlighterWidth = setHighlighterWidth,
                inkColor = inkColor,
                setInkColor = setInkColor,
                inkWidth = inkWidth,
                setInkWidth = setInkWidth,
                selectedItems = selectedItems,
                setSelection = { selection = it },
                setSelectedItems = { selectedItems = it },
                multiSelect = multiSelect,
                setMultiSelect = { multiSelect = it },
                snapToGrid = snapToGrid,
                setSnapToGrid = { snapToGrid = it },
                templateGalleryOpen = templateGalleryOpen,
                setTemplateGalleryOpen = { templateGalleryOpen = it },
                templateGalleryCategory = templateGalleryCategory,
                contentButtonBounds = contentButtonBounds,
                setContentButtonBounds = { contentButtonBounds = it },
                setManageTemplates = { manageTemplates = it },
                moreToolsMenu = moreToolsMenu,
                setMoreToolsMenu = { moreToolsMenu = it },
                textToolsMenu = textToolsMenu,
                setTextToolsMenu = { textToolsMenu = it },
                pendingTextKind = pendingTextKind,
                setPendingTextKind = { pendingTextKind = it },
                setLinkedDocPicker = { linkedDocPicker = it },
                mindmapToolsMenu = mindmapToolsMenu,
                setMindmapToolsMenu = { mindmapToolsMenu = it },
                activeMenuButton = activeMenuButton,
                setActiveMenuButton = { activeMenuButton = it },
                toolbarMenuOpen = toolbarMenuOpen,
                frameRatio = frameRatio,
                setFrameRatio = { frameRatio = it },
                frameNavigator = frameNavigator,
                setFrameNavigator = { frameNavigator = it },
                setTemplateName = { templateName = it },
                setSaveSelectionTemplate = { saveSelectionTemplate = it },
                lastNavigationTool = lastNavigationTool,
                selectedFrameIndex = targets.frame,
                setScale = { scale = it },
                setPan = { pan = it },
                viewportWidthPx = viewportSizePx.width,
                density = density
            )
        }
        if (settings.showZoomButtons && presentingFrameId == null) {
            CanvasZoomControls(
                language, scale,
                Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = CanvasUiTokens.zoomBottomPadding).zIndex(2f),
                onZoomIn = { commands.zoomBy(1.25f) },
                onZoomOut = { commands.zoomBy(0.8f) },
                onReset = { scale = 1f; pan = Offset.Zero }
            )
        }
        if (presentingFrameId != null) {
            val frames = commands.presentationFrames()
            val currentIndex = frames.indexOfFirst { it.id == presentingFrameId }
            if (currentIndex >= 0) {
                CanvasPresentationBar(
                    language = language,
                    currentIndex = currentIndex,
                    total = frames.size,
                    title = frames[currentIndex].title,
                    fitMode = presentationFitMode,
                    onShift = commands::shiftPresentation,
                    onToggleFit = {
                        presentationFitMode = if (presentationFitMode == "fill") "fit" else "fill"
                        commands.applyPresentationView(frames[currentIndex])
                    },
                    onExit = commands::stopPresentation,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
        CanvasInlineEditor(
            page = page,
            language = language,
            settings = settings,
            scale = scale,
            viewport = viewport,
            state = editorState,
            focusRequester = inlineEditFocusRequester,
            changedText = changedText,
            onExitEdit = {
                cardEdit = null
                runCatching { canvasFocusRequester.requestFocus() }
            }
        )
        CanvasDialogHost(
            page = page,
            pages = pages,
            language = language,
            store = store,
            workspaceTemplates = workspaceTemplates,
            templatesAvailable = templatesAvailable,
            replaceTemplates = replaceTemplates,
            selectedItems = selectedItems,
            state = editorState,
            onChanged = changed,
            onPresentFrame = commands::startPresentation,
            onFitFrame = commands::fitFrameViewport
        )
    }
}

@Composable
internal fun ConnectorMarkerRow(language: AppLanguage, selected: String, onPick: (String) -> Unit) {
    val options = listOf(
        "none" to tr(language, "无端点", "None"),
        "arrow" to tr(language, "箭头", "Arrow"),
        "triangle" to tr(language, "三角形", "Triangle"),
        "dot" to tr(language, "圆点", "Circle"),
        "diamond" to tr(language, "菱形", "Diamond")
    )
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { (marker, label) ->
            val icon = when (marker) {
                "dot" -> AppIcon.END_DOT
                "arrow" -> AppIcon.END_ARROW
                "triangle" -> AppIcon.END_TRIANGLE
                "diamond" -> AppIcon.END_DIAMOND
                else -> AppIcon.END_NONE
            }
            ToolIconButton(icon, label, selected == marker, false) { onPick(marker) }
        }
    }
}


















