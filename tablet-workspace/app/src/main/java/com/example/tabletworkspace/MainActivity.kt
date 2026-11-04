@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageStore
import com.example.tabletworkspace.data.images.OrphanImages
import com.example.tabletworkspace.data.images.ImageCacheStats
import com.example.tabletworkspace.data.pages.PageRepository
import com.example.tabletworkspace.data.pages.PageHistory
import com.example.tabletworkspace.data.metadata.PageMetadataRepository
import com.example.tabletworkspace.ui.model.PageRowModel

import com.example.tabletworkspace.core.PageSearchIndex
import com.example.tabletworkspace.core.filterPagesIndexed
import com.example.tabletworkspace.core.journalDateLabel
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

class MainActivity : ComponentActivity() {
    private var saveOnStop: () -> Unit = {}
    private var shortcutHandler: (android.view.KeyEvent) -> Boolean = { false }
    override fun onStop() { saveOnStop(); super.onStop() }
    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent): Boolean {
        if (event.repeatCount == 0 && shortcutHandler(event)) return true
        return super.onKeyDown(keyCode, event)
    }
    fun registerShortcutHandler(handler: (android.view.KeyEvent) -> Boolean) { shortcutHandler = handler }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = PageStore(this)
        val mode = getSharedPreferences("ink_preferences", MODE_PRIVATE).getString("theme_mode", "system").orEmpty()
        val systemDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        LingyeColors.setDark(when (mode) { "dark" -> true; "light" -> false; else -> systemDark })
        setContent { LingyeTheme { Box { Workspace(store) { saveOnStop = it }; CustomColorDialogHostView() } } }
    }
}

@Composable
internal fun LingyeDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp),
    containerColor: Color = LingyeColors.SurfaceHigh,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 6.dp,
    border: BorderStroke? = BorderStroke(1.dp, LingyeColors.Line),
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        shape = shape,
        containerColor = containerColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        content = content
    )
}

internal fun Rect.toIntRectPx() = IntRect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

private class ExportSubmenuPositionProvider(
    private val menuLeft: Int,
    private val menuTop: Int,
    private val rowTopInMenu: Int,
    private val gapPx: Int
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)
        val x = (menuLeft - gapPx - popupContentSize.width).coerceIn(0, maxX)
        val y = (menuTop + rowTopInMenu).coerceIn(0, maxY)
        return IntOffset(x, y)
    }
}

@Composable
private fun ExportSubmenuPanel(
    language: AppLanguage,
    onExportHtml: () -> Unit,
    onExportMarkdown: () -> Unit,
    onCopyMarkdown: () -> Unit,
    onExportSnapshot: () -> Unit
) {
    Surface(
        Modifier.width(248.dp),
        shape = RoundedCornerShape(12.dp),
        color = LingyeColors.SurfaceHigh,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, LingyeColors.Line)
    ) {
        Column {
            MenuRow(tr(language, "导出为 HTML", "Export as HTML"), onExportHtml, icon = AppIcon.CODE_BLOCK)
            MenuRow(tr(language, "导出为 Markdown", "Export as Markdown"), onExportMarkdown, icon = AppIcon.SAVE)
            MenuRow(tr(language, "复制为 Markdown", "Copy as Markdown"), onCopyMarkdown, icon = AppIcon.COPY)
            MenuRow("Export to Snapshot", onExportSnapshot, icon = AppIcon.RECORD)
        }
    }
}


@Composable
private fun SidebarNavigationRow(    icon: AppIcon,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    startPadding: Dp = 10.dp,
    iconSize: Dp = 21.dp,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    val background by animateColorAsState(
        targetValue = if (selected) LingyeColors.Subtle else Color.Transparent,
        animationSpec = tween(150),
        label = "sidebar-row-background"
    )
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = background
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 46.dp).padding(start = startPadding, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WorkspaceIcon(icon, contentDescription = null, tint = LingyeColors.Ink, modifier = Modifier.size(iconSize))
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                Modifier.weight(1f),
                color = LingyeColors.Ink,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            trailing()
        }
    }
}

@Composable
private fun SidebarSquareAction(
    icon: AppIcon,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    showBorder: Boolean = true,
    buttonSize: Dp = 38.dp,
    iconSize: Dp = 17.dp
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(buttonSize),
        shape = RoundedCornerShape(10.dp),
        color = LingyeColors.SurfaceHigh,
        border = if (showBorder) BorderStroke(1.dp, LingyeColors.Line) else null,
        enabled = enabled
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            WorkspaceIcon(
                icon,
                contentDescription = description,
                tint = LingyeColors.Muted.copy(alpha = if (enabled) 1f else 0.38f),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
private fun SidebarFolderIcon(expanded: Boolean, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.affine_folder_icon))
    val progress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(360),
        label = "sidebar-folder-lottie"
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}

@Composable
private fun SidebarChevron(expanded: Boolean, modifier: Modifier = Modifier) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(160),
        label = "sidebar-chevron-rotation"
    )
    Canvas(modifier) {
        val pivot = center
        val path = Path().apply {
            moveTo(size.width * 0.38f, size.height * 0.27f)
            lineTo(size.width * 0.62f, size.height * 0.5f)
            lineTo(size.width * 0.38f, size.height * 0.73f)
        }
        withTransform({ rotate(rotation, pivot) }) {
            drawPath(path, LingyeColors.Muted, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SidebarPageItem(
    model: PageRowModel,
    selected: Boolean,
    trashMode: Boolean,
    language: AppLanguage,
    pageActionsExpanded: Boolean,
    onActionsExpandedChange: (Boolean) -> Unit,
    onOpen: () -> Unit,
    onRestore: () -> Unit,
    onRename: () -> Unit,
    onInfo: () -> Unit,
    onAddLinkedDoc: () -> Unit = {},
    onDuplicate: () -> Unit = {},
    onOpenInNewTab: () -> Unit = {},
    onFavorite: () -> Unit,
    onRemoveFolder: () -> Unit,
    onTrash: () -> Unit,
    indented: Boolean = false
) {
    val itemColor by animateColorAsState(
        if (selected && !trashMode) LingyeColors.Subtle else LingyeColors.SurfaceHigh,
        tween(150),
        label = "page-item"
    )
    val content: @Composable (() -> Boolean) -> Unit = { revealed ->
        Surface(
            onClick = { if (!revealed()) onOpen() },
            shape = RoundedCornerShape(10.dp),
            color = itemColor
        ) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 44.dp).padding(start = if (indented) 38.dp else 10.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (trashMode) WorkspaceIcon(AppIcon.TRASH, null, Modifier.size(20.dp), LingyeColors.Ink)
                else PageGlyph(model.icon, model.canvasIcon, 20.dp, LingyeColors.Ink)
                Spacer(Modifier.width(12.dp))
                if (trashMode) {
                    Text(model.title.ifBlank { tr(language, "未命名页面", "Untitled page") }, Modifier.weight(1f), maxLines = 1)
                    IconTextAction(AppIcon.RESTORE, "", { onRestore() }, tint = LingyeColors.Mint)
                } else {
                    Column(Modifier.weight(1f)) {
                        Text(
                            model.title.ifBlank { tr(language, "未命名页面", "Untitled page") },
                            maxLines = 1,
                            color = LingyeColors.Ink,
                            fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                        )
                        if (model.tags.isNotEmpty()) {
                            Text(model.tags.joinToString(" ") { "#$it" }, maxLines = 1, fontSize = 10.sp, color = LingyeColors.Muted)
                        }
                    }
                    PageRowActions(
                        model,
                        pageActionsExpanded,
                        onActionsExpandedChange,
                        language,
                        onRename,
                        onInfo,
                        onAddLinkedDoc,
                        onDuplicate,
                        onOpenInNewTab,
                        onFavorite,
                        onRemoveFolder,
                        onTrash
                    )
                }
            }
        }
    }
    if (trashMode) {
        content { false }
    } else {
        SwipeActionRow(
            key = model.id,
            revealWidth = 132.dp,
            actionVerticalOffset = 7.dp,
            actions = {
                SidebarSwipeAction(AppIcon.EDIT, tr(language, "重命名", "Rename"), Color(0xff52b788)) {
                    onRename()
                }
                SidebarSwipeAction(
                    if (model.favorite) AppIcon.FAVORITED else AppIcon.FAVORITE,
                    if (model.favorite) tr(language, "取消收藏", "Remove favorite") else tr(language, "收藏", "Favorite"),
                    Color(0xffd4a72c)
                ) {
                    onFavorite()
                }
                SidebarSwipeAction(AppIcon.TRASH, tr(language, "删除", "Delete"), Color(0xffdb5a4f)) {
                    onTrash()
                }
            },
            content = { revealed -> content(revealed) }
        )
    }
}

@Composable
private fun SidebarSwipeAction(icon: AppIcon, description: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(color, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        WorkspaceIcon(icon, null, Modifier.size(17.dp), Color.White)
    }
}


@Composable
fun Workspace(store: PageStore, registerStopSave: (() -> Unit) -> Unit) {
    var error by remember { mutableStateOf<String?>(null) }; var notice by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var ioBusy by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val density = LocalDensity.current
    val preferences = remember { context.getSharedPreferences("ink_preferences", android.content.Context.MODE_PRIVATE) }
    val workspaceRepository = remember(store, preferences) { WorkspaceRepository(store, SharedPreferencesWorkspacePreferences(preferences)) }
    val metadataRepository = remember(context) { PageMetadataRepository(context) }
    var metaRevision by remember { mutableIntStateOf(0) }
    LaunchedEffect(preferences) { CustomColorStore.attach(preferences) }
    val legacyMotionPreferences = remember { context.getSharedPreferences("canvas_motion", android.content.Context.MODE_PRIVATE) }
    val restoredSession = remember(workspaceRepository) { workspaceRepository.readSession() }
    val systemLanguage = if (java.util.Locale.getDefault().language.equals("zh", true)) "zh" else "en"
    var language by remember { mutableStateOf(workspaceRepository.readLanguage(systemLanguage)) }
    fun text(chinese: String, english: String) = tr(language, chinese, english)
    fun failure(prefixZh: String, prefixEn: String, e: Exception) = "${text(prefixZh, prefixEn)}：${localizedError(language, e)}"
    val repository = remember(store) {
        PageRepository(store) { cause ->
            error = failure("保存失败", "Save failed", cause as? Exception ?: Exception(cause))
        }
    }
    val bootstrapState by produceState<WorkspaceBootstrap?>(initialValue = null, workspaceRepository) { value = workspaceRepository.load() }
    var contentLoaded by remember { mutableStateOf(false) }
    val pages = remember { mutableStateListOf<Page>() }
    var modifiedTimes by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var failedPages by remember { mutableStateOf(emptyList<String>()) }
    var templatesAvailable by remember { mutableStateOf(true) }
    var sharedAvailable by remember { mutableStateOf(true) }
    val sharedBlocks=remember { mutableStateListOf<SharedBlock>() }
    fun replaceShared(next:List<SharedBlock>):Boolean = try { store.saveSharedBlocks(next);sharedBlocks.clear();sharedBlocks.addAll(next);true } catch(e:Exception) { error=failure("共享块保存失败","Shared block save failed",e);false }
    fun editShared(value:SharedBlock):Boolean { val before=sharedBlocks.firstOrNull { it.id==value.id } ?: return false;val next=value.withHistoryFrom(before);return replaceShared(sharedBlocks.map { if(it.id==value.id) next else it }) }
    fun stepShared(id:String,undo:Boolean) { val before=sharedBlocks.firstOrNull { it.id==id } ?: return;val next=if(undo) before.undoShared() else before.redoShared();if(next!=null) replaceShared(sharedBlocks.map { if(it.id==id) next else it }) }
    val workspaceTemplates = remember { mutableStateListOf<CardTemplate>() }
    fun replaceTemplates(next: List<CardTemplate>) { try { store.saveTemplates(next); workspaceTemplates.clear(); workspaceTemplates.addAll(next) } catch(e:Exception) { error=failure("模板保存失败","Template save failed",e) } }
    var foldersAvailable by remember { mutableStateOf(true) }
    val folders = remember { mutableStateListOf<String>() }
    var search by remember { mutableStateOf("") }; var folderFilter by remember { mutableStateOf<String?>(null) }
    var trashMode by remember { mutableStateOf(false) }; var favoriteMode by remember { mutableStateOf(false) }
    var journalMode by remember { mutableStateOf(false) }; var templateMode by remember { mutableStateOf(false) }
    var journalSelectedDate by remember { mutableStateOf(java.time.LocalDate.now().toString()) }
    var allDocsHome by remember { mutableStateOf(true) }
    var tagFilter by remember { mutableStateOf<String?>(null) }
    var folderMenu by remember { mutableStateOf(false) }; var moveMenu by remember { mutableStateOf(false) }
    var createFolder by remember { mutableStateOf(false) }; var folderName by remember { mutableStateOf("") }; var folderError by remember { mutableStateOf<String?>(null) }
    var deleteEmptyFolder by remember { mutableStateOf(false) }
    var renameEmptyFolder by remember { mutableStateOf(false) }
    var tagEditor by remember { mutableStateOf(false) }; var tagDraft by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Page?>(pages.firstOrNull { !it.trashed }) }
    val openTabs = remember { mutableStateListOf<String>() }
    var pageActionsMenu by remember { mutableStateOf<String?>(null) }
    var allDocsSelectionRequest by remember { mutableStateOf<Set<String>>(emptySet()) }
    var trashMenu by remember { mutableStateOf(false) }
    var trashSelectionDialog by remember { mutableStateOf(false) }
    var trashSelectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var trashClearConfirm by remember { mutableStateOf(false) }
    var trashDeleteConfirm by remember { mutableStateOf<Set<String>?>(null) }
    var imageCleanupConfirm by remember { mutableStateOf(false) }
    var imageCleanupRevision by remember { mutableIntStateOf(0) }
    var pageRenameTarget by remember { mutableStateOf<String?>(null) }
    var pageRenameDraft by remember { mutableStateOf("") }
    var pageInfoTarget by remember { mutableStateOf<String?>(null) }
    var linkedDocSourceId by remember { mutableStateOf<String?>(null) }
    var importDialog by remember { mutableStateOf(false) }
    var importRequestKind by remember { mutableStateOf<String?>(null) }
    var pageMoreMenu by remember { mutableStateOf(false) }
    var exportSubmenu by remember { mutableStateOf(false) }
    var exportMenuLocal by remember { mutableStateOf<IntRect?>(null) }
    var exportRowLocal by remember { mutableStateOf<IntRect?>(null) }
    var exportMenuLeft by remember { mutableIntStateOf(0) }
    var exportMenuTop by remember { mutableIntStateOf(0) }
    var exportRowTopInMenu by remember { mutableIntStateOf(0) }
    var historyVersionsDialog by remember { mutableStateOf(false) }
    var frameNavigatorRequest by remember { mutableStateOf(false) }
    var iconTargetId by remember { mutableStateOf<String?>(null) }
    var sessionDocumentScroll by remember { mutableIntStateOf(restoredSession.documentScroll) }
    var sessionDocumentScrollIndex by remember { mutableIntStateOf(restoredSession.documentScrollIndex) }
    var sessionPageListIndex by remember { mutableIntStateOf(restoredSession.pageListIndex) }
    var sessionPageListOffset by remember { mutableIntStateOf(restoredSession.pageListOffset) }
    val documentScrollState = remember(selected?.id) { androidx.compose.foundation.lazy.LazyListState(sessionDocumentScrollIndex, sessionDocumentScroll) }
    val pageListState = rememberLazyListState(sessionPageListIndex, sessionPageListOffset)
    var renderRevision by remember { mutableIntStateOf(0) }; var gesture: PageGesture? by remember { mutableStateOf(null) }
    var revision by remember { mutableIntStateOf(0) }; var savedRevision by remember { mutableIntStateOf(0) }
    val journalDates = remember(pages.size, metaRevision) { pages.associate { it.id to metadataRepository.journalDate(it.id) } }
    val templatePageIds = remember(pages.size, metaRevision) { pages.filter { metadataRepository.isTemplate(it.id) }.map { it.id }.toSet() }
    LaunchedEffect(selected?.id, openTabs.size, revision) {
        val id = selected?.id
        if (id != null && openTabs.isNotEmpty() && id !in openTabs && pages.any { it.id == id && !it.trashed }) {
            openTabs.add(id)
        }
        openTabs.removeAll { tabId -> pages.none { it.id == tabId && !it.trashed } }
    }
    var canvas by remember { mutableStateOf(restoredSession.view == WorkspaceView.WHITEBOARD) }
    var overview by remember { mutableStateOf(restoredSession.view == WorkspaceView.OVERVIEW) }
    var settingsPage by remember { mutableStateOf(restoredSession.view == WorkspaceView.SETTINGS) }
    var tool by remember { mutableStateOf(restoredSession.tool) }
    var sessionCanvasScale by remember { mutableFloatStateOf(restoredSession.canvasScale) }
    var sessionCanvasOffsetX by remember { mutableFloatStateOf(restoredSession.canvasOffsetX) }
    var sessionCanvasOffsetY by remember { mutableFloatStateOf(restoredSession.canvasOffsetY) }
    var sessionSelectedObjectIds by remember { mutableStateOf(restoredSession.selectedObjectIds) }
    var canvasFocusMode by remember { mutableStateOf(false) }
    var linkOriginId by remember { mutableStateOf<String?>(null) }
    var linkOriginCanvas by remember { mutableStateOf(false) }
    var linkTargetId by remember { mutableStateOf<String?>(null) }
    val initialCanvasTools = remember(workspaceRepository) { workspaceRepository.readCanvasTools() }
    var penOnly by remember { mutableStateOf(initialCanvasTools.penOnly) }
    var eraserArea by remember { mutableStateOf(initialCanvasTools.eraserArea) }
    var brushKind by remember { mutableStateOf(initialCanvasTools.brushKind) }
    var highlighterColor by remember { mutableIntStateOf(initialCanvasTools.highlighterColor) }
    var highlighterWidth by remember { mutableFloatStateOf(initialCanvasTools.highlighterWidth) }
    var inkColor by remember { mutableIntStateOf(initialCanvasTools.inkColor) }
    var inkWidth by remember { mutableFloatStateOf(initialCanvasTools.inkWidth) }
    var workspaceSettings by remember { mutableStateOf(workspaceRepository.readSettings(legacyMotionPreferences.getBoolean("enabled", true))) }
    val systemDarkTheme = isSystemInDarkTheme()
    val resolvedDarkTheme = when (workspaceSettings.themeMode) {
        "dark" -> true
        "light" -> false
        else -> systemDarkTheme
    }
    LaunchedEffect(resolvedDarkTheme) { LingyeColors.setDark(resolvedDarkTheme) }
    fun updateWorkspaceSettings(next: WorkspaceSettings) {
        val normalized = next.normalized()
        workspaceSettings = normalized
        workspaceRepository.writeSettings(normalized)
    }
    var showSidebar by remember { mutableStateOf(true) }
    var rootCreateMenu by remember { mutableStateOf(false) }
    var expandedFolders by remember { mutableStateOf(setOf<String>()) }
    var folderAddMenu by remember { mutableStateOf<String?>(null) }
    var folderOptionsMenu by remember { mutableStateOf<String?>(null) }
    val compactWorkspace = LocalConfiguration.current.screenWidthDp < 900
    var compactSidebarOpen by remember(compactWorkspace) { mutableStateOf(false) }
    val sidebarVisible = showSidebar && !canvasFocusMode && (!compactWorkspace || compactSidebarOpen)
    fun toggleSidebar() {
        if (compactWorkspace) compactSidebarOpen = !compactSidebarOpen else showSidebar = !showSidebar
    }
    val searchFocusRequester = remember { FocusRequester() }
    var searchFocusRequest by remember { mutableIntStateOf(0) }
    LaunchedEffect(searchFocusRequest, sidebarVisible) {
        if (searchFocusRequest > 0 && sidebarVisible) {
            delay(80)
            runCatching { searchFocusRequester.requestFocus() }
        }
    }
    fun focusSearch() {
        settingsPage = false
        canvasFocusMode = false
        if (compactWorkspace) compactSidebarOpen = true else showSidebar = true
        searchFocusRequest++
    }
    var imageTarget by remember { mutableStateOf<Page?>(null) }; var imageForCanvas by remember { mutableStateOf(false) }; var imageInsertIndex by remember { mutableStateOf<Int?>(null) }; val current = selected
    var pageSidebarOpen by remember { mutableStateOf(false) }
    var pageSidebarRevision by remember { mutableIntStateOf(0) }
    val sidebarProperties = remember(current?.id, pageSidebarRevision) {
        current?.let { metadataRepository.docProperties(it.id) } ?: emptyList()
    }
    val pageHistories = remember { mutableStateMapOf<String, PageHistory>() }
    val history = remember(current?.id) {
        current?.let { page -> pageHistories.getOrPut(page.id) { PageHistory(page) } }
    }
    LaunchedEffect(bootstrapState) {
        val state=bootstrapState ?: return@LaunchedEffect
        if(contentLoaded) return@LaunchedEffect
        pages.clear();pages.addAll(state.loadedPages.pages)
        failedPages=state.loadedPages.failed
        folders.clear();folders.addAll(state.folders)
        workspaceTemplates.clear();workspaceTemplates.addAll(state.templates)
        sharedBlocks.clear();sharedBlocks.addAll(state.sharedBlocks)
        pageHistories.clear();pageHistories.putAll(state.histories)
        foldersAvailable=state.folderError==null
        templatesAvailable=state.templateError==null
        sharedAvailable=state.sharedError==null
        selected=pages.firstOrNull { !it.trashed && it.id == restoredSession.pageId }
            ?: pages.firstOrNull { !it.trashed }
        state.folderError?.let { error=failure("文件夹读取失败","Folder load failed",IllegalStateException(it)) }
        state.templateError?.let { error=failure("模板读取失败","Template load failed",IllegalStateException(it)) }
        state.sharedError?.let { error=failure("共享块读取失败","Shared block load failed",IllegalStateException(it)) }
        contentLoaded=true
    }
    LaunchedEffect(pages.size, revision) {
        if (contentLoaded) modifiedTimes = withContext(Dispatchers.IO) { store.pageModifiedTimes() }
    }
    fun changeGrouped(key: String?) { current?.let { history?.record(it, key) }; revision++ }
    fun change() = changeGrouped(null)
    fun redraw() { renderRevision++ }
    fun beginGesture() { gesture?.cancel(); current?.let { gesture=PageGesture(it) } }
    fun cancelGesture() { gesture?.cancel(); gesture=null; redraw() }
    fun commitGesture() { gesture?.commit(); gesture=null }
    fun applyHistory(page: Page?) { if (page != null && current != null) { val i = pages.indexOfFirst { it.id == current.id }; if (i >= 0) { pages[i] = page; selected = page; revision++ } } }
    fun saveCurrent(): Boolean {
        return try {
            if (gesture != null) cancelGesture()
            if (current != null && revision == savedRevision) {
                true
            } else {
                current?.let { page -> repository.saveAll(page, history) }
                savedRevision = revision
                true
            }
        } catch (e: Exception) {
            error = failure("保存失败","Save failed",e)
            false
        }
    }
    fun createPageFromShortcut(whiteboard: Boolean) {
        if (!saveCurrent()) return
        val page = Page.create(newPageTitle(language, workspaceSettings))
        try {
            repository.save(page)
            pages.add(page)
            selected = page
            allDocsHome = false
            canvas = whiteboard
            overview = false
            settingsPage = false
            trashMode = false
            favoriteMode = false
            folderFilter = null
            search = ""
            revision++
            savedRevision = revision
        } catch (e: Exception) {
            error = failure("创建失败", "Create failed", e)
        }
    }
    fun handleAppShortcut(event: android.view.KeyEvent): Boolean {
        if (event.action != android.view.KeyEvent.ACTION_DOWN) return false
        val ctrl = event.isCtrlPressed || event.isMetaPressed
        val letter = when (event.keyCode) {
            android.view.KeyEvent.KEYCODE_K -> 'K'
            android.view.KeyEvent.KEYCODE_N -> 'N'
            android.view.KeyEvent.KEYCODE_COMMA -> ','
            else -> return false
        }
        val action = WorkspaceShortcuts.appActionFor(letter, ctrl, event.isShiftPressed, event.isAltPressed) ?: return false
        if (settingsPage && action != WorkspaceShortcuts.AppAction.SETTINGS) return false
        return when (action) {
            WorkspaceShortcuts.AppAction.SEARCH -> { focusSearch(); true }
            WorkspaceShortcuts.AppAction.NEW_DOCUMENT -> { createPageFromShortcut(whiteboard = false); true }
            WorkspaceShortcuts.AppAction.NEW_WHITEBOARD -> { createPageFromShortcut(whiteboard = true); true }
            WorkspaceShortcuts.AppAction.SETTINGS -> { settingsPage = true; true }
        }
    }
    val shortcutActivity = context as? MainActivity
    SideEffect { shortcutActivity?.registerShortcutHandler { event -> handleAppShortcut(event) } }
    DisposableEffect(shortcutActivity) {
        onDispose { shortcutActivity?.registerShortcutHandler { false } }
    }
    fun openLinkedPage(id: String) {
        val target = pages.firstOrNull { it.id == id && !it.trashed } ?: return
        if (current == null || target.id == current.id) return
        if (!saveCurrent()) return
        if (workspaceSettings.linkReturnToSource) {
            linkOriginId = current.id
            linkOriginCanvas = canvas
            linkTargetId = target.id
        } else {
            linkOriginId = null
            linkTargetId = null
        }
        selected = target
        canvas = PageLinks.opensAsWhiteboard(target, canvas)
        allDocsHome = false
        overview = false
        settingsPage = false
        revision++
        savedRevision = revision
    }
    fun returnToOriginPage(): Boolean {
        val originId = linkOriginId ?: return false
        val origin = pages.firstOrNull { it.id == originId && !it.trashed } ?: return false
        if (current?.id == origin.id) { linkOriginId = null; linkTargetId = null; return false }
        if (!saveCurrent()) return false
        selected = origin
        canvas = linkOriginCanvas
        linkOriginId = null
        linkTargetId = null
        allDocsHome = false
        overview = false
        settingsPage = false
        revision++
        savedRevision = revision
        return true
    }
    fun clearLinkOrigin() { linkOriginId = null; linkTargetId = null }
    fun togglePageFavorite(page: Page) {
        pageActionsMenu = null
        if (!saveCurrent()) return
        val previous = page.favorite
        page.favorite = !previous
        try {
            repository.save(page)
            revision++
            savedRevision = revision
            notice = when {
                favoriteMode -> null
                page.favorite -> text("已加入收藏", "Added to favorites")
                else -> text("已取消收藏", "Removed from favorites")
            }
        } catch (e: Exception) {
            page.favorite = previous
            error = failure("收藏更新失败", "Favorite update failed", e)
        }
    }
    fun openLibraryPage(page: Page) {
        if (!saveCurrent()) return
        clearLinkOrigin()
        selected = page
        favoriteMode = false
        trashMode = false
        journalMode = false
        templateMode = false
        allDocsHome = false
        overview = false
        settingsPage = false
        canvas = false
        revision++
        savedRevision = revision
    }
    fun createJournalFor(date: String) {
        if (!saveCurrent()) return
        val existing = pages.firstOrNull { !it.trashed && metadataRepository.journalDate(it.id) == date }
        if (existing != null) { openLibraryPage(existing); return }
        val page = Page.create(journalDateLabel(date, language))
        metadataRepository.setJournalDate(page.id, date)
        try {
            repository.save(page)
            pages.add(page)
            metaRevision++
            selected = page
            favoriteMode = false
            trashMode = false
            journalMode = false
            templateMode = false
            allDocsHome = false
            overview = false
            settingsPage = false
            canvas = false
            revision++
            savedRevision = revision
            notice = text("已创建日志", "Journal created")
        } catch (e: Exception) {
            metadataRepository.setJournalDate(page.id, null)
            error = failure("日志创建失败", "Journal creation failed", e)
        }
    }
    fun createTemplatePage() {
        if (!saveCurrent()) return
        val page = Page.create(text("未命名模板", "Untitled template"))
        metadataRepository.setTemplate(page.id, true)
        try {
            repository.save(page)
            pages.add(page)
            metaRevision++
            selected = page
            favoriteMode = false
            trashMode = false
            journalMode = false
            templateMode = false
            allDocsHome = false
            overview = false
            settingsPage = false
            canvas = false
            revision++
            savedRevision = revision
            notice = text("已创建模板", "Template created")
        } catch (e: Exception) {
            metadataRepository.setTemplate(page.id, false)
            error = failure("模板创建失败", "Template creation failed", e)
        }
    }
    fun useTemplatePage(source: Page) {
        if (!saveCurrent()) return
        val copyTitle = (source.title.ifBlank { text("未命名模板", "Untitled template") } + text(" 副本", " copy")).take(120)
        try {
            val copy = source.duplicate(copyTitle)
            metadataRepository.setTemplate(copy.id, false)
            metadataRepository.setJournalDate(copy.id, null)
            repository.save(copy)
            pages.add(copy)
            metaRevision++
            selected = copy
            favoriteMode = false
            trashMode = false
            journalMode = false
            templateMode = false
            allDocsHome = false
            overview = false
            settingsPage = false
            canvas = false
            revision++
            savedRevision = revision
            notice = text("已从模板创建页面", "Page created from template")
        } catch (e: Exception) {
            error = failure("使用模板失败", "Use template failed", e)
        }
    }
    fun movePageToFolder(page: Page, folder: String?) {        pageActionsMenu = null
        if (!saveCurrent()) return
        val previous = page.folders.toSet()
        page.folders.clear()
        if (!folder.isNullOrBlank()) page.folders.add(folder)
        if (page.folders.toSet() == previous) return
        try {
            repository.save(page)
            revision++
            savedRevision = revision
            notice = if (folder.isNullOrBlank()) text("已移至未分类", "Moved to unfiled") else "${text("已移至","Moved to")} $folder"
        } catch (e: Exception) {
            page.folders.clear()
            page.folders.addAll(previous)
            error = failure("移动失败", "Move failed", e)
        }
    }
    fun removePageFromFolder(page: Page) {
        pageActionsMenu = null
        if (!saveCurrent()) return
        val previous = page.folders.toSet()
        val currentFolder = folderFilter
        if (currentFolder.isNullOrEmpty()) page.folders.clear() else page.folders.remove(currentFolder)
        if (page.folders.toSet() == previous) return
        try {
            repository.save(page)
            revision++
            savedRevision = revision
            notice = text("已从文件夹移出", "Removed from folder")
        } catch (e: Exception) {
            page.folders.clear()
            page.folders.addAll(previous)
            error = failure("移出文件夹失败", "Remove from folder failed", e)
        }
    }
    fun trashPage(page: Page) {
        pageActionsMenu = null
        if (!saveCurrent()) return
        page.trashed = true
        try {
            repository.save(page)
            if (selected?.id == page.id) {
                selected = pages.firstOrNull { !it.trashed && (!favoriteMode || it.favorite) }
                allDocsHome = true
            }
            revision++
            savedRevision = revision
            notice = null
        } catch (e: Exception) {
            page.trashed = false
            error = failure("删除失败", "Delete failed", e)
        }
    }
    fun duplicatePage(source: Page) {
        pageActionsMenu = null
        if (source.trashed || !saveCurrent()) return
        val copyTitle = (source.title.ifBlank { text("未命名页面", "Untitled page") } + text(" 副本", " copy")).take(120)
        try {
            val copy = source.duplicate(copyTitle)
            repository.save(copy)
            pages.add(copy)
            selected = copy
            allDocsHome = false
            overview = false
            settingsPage = false
            canvas = false
            revision++
            savedRevision = revision
            error = null
            notice = text("已复制页面", "Page duplicated")
        } catch (e: Exception) {
            error = failure("复制失败", "Duplicate failed", e)
        }
    }
    fun openPageInNewTab(page: Page) {
        pageActionsMenu = null
        if (page.trashed || !saveCurrent()) return
        if (page.id !in openTabs) openTabs.add(page.id)
        selected = page
        allDocsHome = false
        overview = false
        settingsPage = false
        canvas = false
        revision++
        savedRevision = revision
    }
    fun addLinkedDocToPage(source: Page, target: Page) {
        pageActionsMenu = null
        if (source.trashed || target.trashed || source.id == target.id || !saveCurrent()) return
        try {
            val offset = (source.shapes.size % 6) * 32f
            source.addLinkedDoc(target, 220f + offset, 180f + offset)
            repository.save(source)
            selected = source
            allDocsHome = false
            overview = false
            settingsPage = false
            canvas = true
            revision++
            savedRevision = revision
            error = null
            notice = text("已添加链接文档", "Linked document added")
        } catch (e: Exception) {
            error = failure("添加链接文档失败", "Add linked document failed", e)
        }
    }
    fun trashPages(ids: Set<String>): Boolean {        if (ids.isEmpty()) return true
        if (!saveCurrent()) return false
        val targets = pages.filter { it.id in ids && !it.trashed }
        if (targets.isEmpty()) return true
        var failed = 0
        targets.forEach { page ->
            page.trashed = true
            try {
                repository.save(page)
            } catch (e: Exception) {
                page.trashed = false
                failed++
                error = failure("部分文档删除失败", "Some documents could not be moved to trash", e)
            }
        }
        if (selected?.id in ids) {
            selected = pages.firstOrNull { !it.trashed }
        }
        revision++
        savedRevision = revision
        if (failed == 0) {
            notice = null
        }
        return failed == 0
    }
    fun restoreTrashedPages(ids: Set<String>): Boolean {
        if (ids.isEmpty()) return true
        if (!saveCurrent()) return false
        val targets = pages.filter { it.id in ids && it.trashed }
        var failed = 0
        targets.forEach { page ->
            page.trashed = false
            try {
                repository.save(page)
            } catch (e: Exception) {
                page.trashed = true
                failed++
                error = failure("部分文档恢复失败", "Some documents could not be restored", e)
            }
        }
        revision++
        savedRevision = revision
        if (failed == 0) notice = text("已恢复所选文档", "Selected documents restored")
        return failed == 0
    }
    fun permanentlyDeleteTrashedPages(ids: Set<String>): Boolean {
        if (ids.isEmpty()) return true
        if (selected?.id !in ids && !saveCurrent()) return false
        val targets = pages.filter { it.id in ids && it.trashed }
        var failed = 0
        targets.forEach { page ->
            try {
                repository.deletePermanently(page.id)
                pages.remove(page)
            } catch (e: Exception) {
                failed++
                error = failure("部分文档永久删除失败", "Some documents could not be deleted permanently", e)
            }
        }
        if (selected?.id in ids) selected = pages.firstOrNull { !it.trashed }
        revision++
        savedRevision = revision
        if (failed == 0) notice = text("已永久删除所选文档", "Selected documents permanently deleted")
        return failed == 0
    }
    fun createPageInFolder(name: String) {
        if (!saveCurrent()) return
        val page = Page.create(newPageTitle(language, workspaceSettings))
        page.folders.add(name)
        try {
            repository.save(page)
            pages.add(page)
            selected = page
            expandedFolders = expandedFolders + name
            trashMode = false
            favoriteMode = false
            allDocsHome = false
            canvas = workspaceSettings.defaultPageMode == DefaultPageMode.WHITEBOARD
            overview = false
            settingsPage = false
            revision++
            savedRevision = revision
        } catch (e: Exception) {
            error = failure("创建失败", "Create failed", e)
        }
    }
    LaunchedEffect(documentScrollState.firstVisibleItemIndex, documentScrollState.firstVisibleItemScrollOffset) {
        delay(250)
        sessionDocumentScroll = documentScrollState.firstVisibleItemScrollOffset
        sessionDocumentScrollIndex = documentScrollState.firstVisibleItemIndex
    }
    LaunchedEffect(pageListState.firstVisibleItemIndex, pageListState.firstVisibleItemScrollOffset) {
        delay(250)
        sessionPageListIndex = pageListState.firstVisibleItemIndex
        sessionPageListOffset = pageListState.firstVisibleItemScrollOffset
    }
    LaunchedEffect(contentLoaded, selected?.id, canvas, overview, settingsPage, tool, sessionCanvasScale, sessionCanvasOffsetX, sessionCanvasOffsetY, sessionSelectedObjectIds, sessionDocumentScroll, sessionDocumentScrollIndex, sessionPageListIndex, sessionPageListOffset) {
        if (!contentLoaded) return@LaunchedEffect
        val view = when {
            settingsPage -> WorkspaceView.SETTINGS
            overview -> WorkspaceView.OVERVIEW
            canvas -> WorkspaceView.WHITEBOARD
            else -> WorkspaceView.DOCUMENT
        }
        workspaceRepository.writeSession(
            WorkspaceSession(
                pageId = selected?.id.orEmpty(),
                view = view,
                tool = tool,
                canvasScale = sessionCanvasScale,
                canvasOffsetX = sessionCanvasOffsetX,
                canvasOffsetY = sessionCanvasOffsetY,
                selectedObjectIds = sessionSelectedObjectIds,
                documentScroll = sessionDocumentScroll,
                documentScrollIndex = sessionDocumentScrollIndex,
                pageListIndex = sessionPageListIndex,
                pageListOffset = sessionPageListOffset
            )
        )
    }
    BackHandler(
        enabled = !canvas && (
            compactSidebarOpen || settingsPage || overview || folderMenu || moveMenu || tagEditor ||
                createFolder || renameEmptyFolder || deleteEmptyFolder
            )
    ) {
        when {
            compactSidebarOpen -> compactSidebarOpen = false
            tagEditor -> tagEditor = false
            deleteEmptyFolder -> deleteEmptyFolder = false
            renameEmptyFolder -> renameEmptyFolder = false
            createFolder -> createFolder = false
            moveMenu -> moveMenu = false
            folderMenu -> folderMenu = false
            settingsPage -> settingsPage = false
            overview -> overview = false
        }
    }
    BackHandler(enabled = !canvas && !allDocsHome && !settingsPage && !overview && current != null) {
        if (linkTargetId != null && current?.id == linkTargetId && returnToOriginPage()) return@BackHandler
        saveCurrent()
        allDocsHome = true
    }
    BackHandler(enabled = allDocsHome && (favoriteMode || journalMode || templateMode)) {
        favoriteMode = false
        journalMode = false
        templateMode = false
        trashMode = false
        folderFilter = null
    }
    LaunchedEffect(allDocsHome) { if (allDocsHome) clearLinkOrigin() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val target = imageTarget
        val forCanvas = imageForCanvas
        if (uri != null && target != null) { ioBusy = true; scope.launch {
            try {
                val (name, ratio) = withContext(Dispatchers.IO) { val name = store.importImage(uri); val bitmap = if (forCanvas) store.image(name) ?: error("图片无法解码") else null; name to (bitmap?.let { it.height.toFloat() / it.width } ?: 1f) }
                if (forCanvas) target.addCanvasImage(name, 400f, 120f, 240f, (240f * ratio).coerceIn(40f, 700f)) else {
                    val at = imageInsertIndex
                    if (at != null && at in 0..target.blocks.size) target.blocks.add(at, Block("image", name)) else target.addBlock("image", name)
                }
                if (selected === target) change() else repository.save(target)
            } catch (e: Exception) { error = failure("图片导入失败","Image import failed",e) } finally { ioBusy = false }
        } }
        imageTarget = null
        imageInsertIndex = null
    }
    var sharedImageTarget by remember { mutableStateOf<String?>(null) }
    val sharedImagePicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> val id=sharedImageTarget;sharedImageTarget=null;if(uri!=null && id!=null) { ioBusy=true;scope.launch { try { val name=withContext(Dispatchers.IO) {store.importImage(uri)};sharedBlocks.firstOrNull {it.id==id && it.kind=="image"}?.let { editShared(it.copy(text=name)) } } catch(e:Exception) {error=failure("图片导入失败","Image import failed",e)} finally {ioBusy=false} } } }
    fun replaceSharedImage(id:String) { sharedImageTarget=id;sharedImagePicker.launch(arrayOf("image/*", "application/octet-stream")) }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) { ioBusy = true; scope.launch { try {
            check(failedPages.isEmpty()) { text("存在无法读取的页面，请先恢复原始文件，避免导出不完整的备份","Some pages could not be read; recover them before exporting a complete backup") }
            check(templatesAvailable && foldersAvailable && sharedAvailable) { text("模板、共享块或文件夹无法读取","Templates, shared blocks or folders could not be read") }
            if (revision != savedRevision && !saveCurrent()) error(text("当前页面保存失败","Current page could not be saved"))
            val snapshot = pages.map { it.snapshot() }; val folderSnapshot = folders.toList()
            withContext(Dispatchers.IO) {
                val metadata = metadataRepository.snapshot(snapshot)
                store.writeBackup(uri, store.backup(snapshot, folderSnapshot, metadata))
            }
            error = null; notice = text("备份已导出","Backup exported")
        } catch (e: Exception) { error = failure("备份导出失败","Backup export failed",e) } finally { ioBusy = false } } }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { ioBusy = true; scope.launch { try {
            if (revision != savedRevision && !saveCurrent()) error(text("当前页面保存失败","Current page could not be saved"))
            val imported = withContext(Dispatchers.IO) { check(repository.flush()) { "保存队列尚未清空" }; store.restore(store.readBackup(uri), metadataRepository::applyRestored) }
            pages.addAll(imported.pages); folders.clear(); folders.addAll(imported.folders); workspaceTemplates.clear(); workspaceTemplates.addAll(imported.templates);sharedBlocks.clear();sharedBlocks.addAll(imported.sharedBlocks);foldersAvailable = true;sharedAvailable=true; error = null; notice = text("已恢复 ${imported.pages.size} 个新页面","Restored ${imported.pages.size} new pages")
        } catch (e: Exception) { error = failure("备份恢复失败","Backup restore failed",e) } finally { ioBusy = false } } }
    }
    val markdownExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { uri ->
        val page = current
        if (uri != null && page != null) {
            ioBusy = true
            scope.launch {
                try {
                    val snapshot = page.snapshot()
                    val sharedSnapshot = sharedBlocks.toList()
                    val content = withContext(Dispatchers.Default) { MarkdownCodec.encode(snapshot, sharedSnapshot) }
                    withContext(Dispatchers.IO) { store.writeText(uri, content) }
                    error = null
                    notice = text("Markdown 已导出","Markdown exported")
                } catch (e: Exception) {
                    error = failure("Markdown 导出失败","Markdown export failed",e)
                } finally {
                    ioBusy = false
                }
            }
        }
    }
    val markdownImporter = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            ioBusy = true
            scope.launch {
                try {
                    if (revision != savedRevision && !saveCurrent()) error = text("当前页面保存失败","Current page could not be saved")
                    val content = withContext(Dispatchers.IO) { store.readText(uri) }
                    val document = withContext(Dispatchers.Default) {
                        MarkdownCodec.decode(content, text("导入页面","Imported page"))
                    }
                    val page = document.toPage(text("导入页面","Imported page"))
                    if (!trashMode && folderFilter != null) page.folders.add(folderFilter!!)
                    repository.save(page)
                    pages.add(page)
                    selected = page
                    trashMode = false
                    favoriteMode = false
                    tagFilter = null
                    canvas = false
                    overview = false
                    settingsPage = false
                    revision++
                    savedRevision = revision
                    error = null
                    notice = text("Markdown 已导入","Markdown imported")
                } catch (e: Exception) {
                    error = failure("Markdown 导入失败","Markdown import failed",e)
                } finally {
                    ioBusy = false
                }
            }
        }
    }
    fun addImportedPages(imported: List<ImportedPage>, source: String) {
        if (imported.isEmpty()) {
            error = text("未找到可导入的内容", "No importable content found")
            return
        }
        val created = imported.map { item ->
            val document = MarkdownCodec.decode(item.markdown, item.title)
            val page = document.toPage(item.title)
            if (!trashMode && folderFilter != null) page.folders.add(folderFilter!!)
            repository.save(page)
            pages.add(page)
            page
        }
        selected = created.last()
        trashMode = false
        favoriteMode = false
        tagFilter = null
        allDocsHome = false
        canvas = false
        overview = false
        settingsPage = false
        revision++
        savedRevision = revision
        error = null
        notice = text("已从 $source 导入 ${created.size} 个页面", "Imported ${created.size} page(s) from $source")
    }
    val documentImporter = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val kind = importRequestKind
        importRequestKind = null
        if (uri != null && kind != null) {
            ioBusy = true
            scope.launch {
                try {
                    if (revision != savedRevision && !saveCurrent()) error = text("当前页面保存失败", "Current page could not be saved")
                    val bytes = withContext(Dispatchers.IO) { store.readBackup(uri) }
                    val imported = withContext(Dispatchers.Default) {
                        when (kind) {
                            "markdownZip", "obsidian", "bear" -> DocumentImport.markdownZip(bytes)
                            "html" -> listOf(DocumentImport.html(bytes.toString(Charsets.UTF_8), "Imported page"))
                            "notion" -> if (bytes.size > 1 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()) {
                                DocumentImport.htmlZip(bytes)
                            } else {
                                listOf(DocumentImport.html(bytes.toString(Charsets.UTF_8), "Imported page"))
                            }
                            "docx" -> listOf(DocumentImport.docx(bytes, "Imported page"))
                            "snapshot" -> listOf(DocumentImport.snapshot(bytes.toString(Charsets.UTF_8), "Imported page"))
                            else -> emptyList()
                        }
                    }
                    addImportedPages(
                        imported,
                        when (kind) {
                            "markdownZip" -> text("Markdown 压缩包", "Markdown zip")
                            "html" -> text("HTML 文件", "HTML file")
                            "notion" -> "Notion"
                            "obsidian" -> "Obsidian"
                            "bear" -> "Bear"
                            "docx" -> text("Word 文档", "Word document")
                            "snapshot" -> text("快照", "Snapshot")
                            else -> kind
                        }
                    )
                } catch (e: Exception) {
                    error = failure("导入失败", "Import failed", e)
                } finally {
                    ioBusy = false
                }
            }
        }
    }
    val htmlExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/html")) { uri ->
        val page = current
        if (uri != null && page != null) {
            ioBusy = true
            scope.launch {
                try {
                    val content = withContext(Dispatchers.Default) { HtmlExporter.encode(page.snapshot()) }
                    withContext(Dispatchers.IO) { store.writeText(uri, content) }
                    error = null
                    notice = text("HTML 已导出", "HTML exported")
                } catch (e: Exception) {
                    error = failure("HTML 导出失败", "HTML export failed", e)
                } finally {
                    ioBusy = false
                }
            }
        }
    }
    val snapshotExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val page = current
        if (uri != null && page != null) {
            ioBusy = true
            scope.launch {
                try {
                    val content = withContext(Dispatchers.Default) { SnapshotCodec.encode(page.snapshot()) }
                    withContext(Dispatchers.IO) { store.writeText(uri, content) }
                    error = null
                    notice = text("快照已导出", "Snapshot exported")
                } catch (e: Exception) {
                    error = failure("快照导出失败", "Snapshot export failed", e)
                } finally {
                    ioBusy = false
                }
            }
        }
    }
    LaunchedEffect(current, revision, gesture) {
        val page=current
        if(page!=null && gesture==null && revision!=savedRevision) {
            delay(400)
            val snapshotRevision=revision
            try {
                repository.saveAsync(page) {
                    error = null
                    if (selected?.id == page.id && snapshotRevision > savedRevision) savedRevision = snapshotRevision
                }
                val historyRef=history
                historyRef?.let { repository.saveHistoryAsync(page.id, it) }
            } catch(e:Exception) { error=failure("保存失败","Save failed",e) }
        }
    }
    SideEffect { registerStopSave { cancelGesture(); if (revision != savedRevision) saveCurrent(); repository.flush(3_000) } }
    if(!contentLoaded) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(LingyeColors.LoadingGradientStart, LingyeColors.LoadingGradientEnd))),
            contentAlignment = Alignment.Center
        ) {
            AmbientBackdrop()
            Surface(shape=RoundedCornerShape(24.dp),color=LingyeColors.SurfaceHigh.copy(alpha=0.96f),shadowElevation=10.dp) {
                Row(Modifier.padding(horizontal=28.dp,vertical=22.dp),verticalAlignment=Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(28.dp),strokeWidth=3.dp)
                    Spacer(Modifier.width(14.dp))
                    Text(text("正在载入工作区…","Loading workspace…"),fontWeight=FontWeight.SemiBold)
                }
            }
        }
        return
    }
    Box(Modifier.fillMaxSize().background(LingyeColors.Background)) {
      Row(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = sidebarVisible, enter = fadeIn(tween(220)) + slideInHorizontally(tween(260)) { -it }, exit = fadeOut(tween(160)) + slideOutHorizontally(tween(200)) { -it }) {
          Surface(Modifier.width(250.dp).fillMaxHeight(), shape = RoundedCornerShape(0.dp), color = LingyeColors.Sidebar, shadowElevation = 0.dp, border = BorderStroke(1.dp, LingyeColors.Line)) {
          Column(Modifier.fillMaxSize().padding(start = 12.dp, end = 8.dp, top = 12.dp, bottom = 8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp))) {
                    Image(painterResource(R.drawable.ic_launcher_background), contentDescription = null, modifier = Modifier.fillMaxSize())
                    Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(workspaceSettings.workspaceName, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = LingyeColors.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(workspaceSettings.workspaceSubtitle, fontSize = 11.sp, color = LingyeColors.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(
                    Modifier.size(38.dp).clickable(onClick = ::toggleSidebar),
                    contentAlignment = Alignment.Center
                ) {
                    WorkspaceIcon(
                        AppIcon.SIDEBAR,
                        contentDescription = text("隐藏侧栏", "Hide sidebar"),
                        tint = LingyeColors.Muted,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth().heightIn(min = 46.dp), verticalAlignment = Alignment.CenterVertically) {
                WorkspaceIcon(AppIcon.SEARCH, null, Modifier.padding(start = 10.dp).size(21.dp), LingyeColors.Ink)
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f)) {
                    if (search.isEmpty()) Text(text("快速搜索","Quick search"), color = LingyeColors.Muted, fontSize = 15.sp)
                    BasicTextField(
                        value = search,
                        onValueChange = { search = it },
                        modifier = Modifier.fillMaxWidth().focusRequester(searchFocusRequester),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(color = LingyeColors.Ink, fontSize = 15.sp),
                        cursorBrush = SolidColor(LingyeColors.Primary)
                    )
                }
                Box {
                    SidebarSquareAction(AppIcon.ADD, text("新建文档或文件夹","Create document or folder"), { rootCreateMenu = true })
                    LingyeDropdownMenu(expanded = rootCreateMenu, onDismissRequest = { rootCreateMenu = false }, offset = DpOffset(10.dp, 6.dp), shape = RoundedCornerShape(12.dp), containerColor = LingyeColors.SurfaceHigh, tonalElevation = 0.dp, shadowElevation = 6.dp, border = BorderStroke(1.dp, LingyeColors.Line)) {
                        MenuRow(text("新建文档","New document"), {
                            rootCreateMenu = false
                            if (saveCurrent()) { val page = Page.create(newPageTitle(language, workspaceSettings)); try { repository.save(page); pages.add(page); selected = page; folderFilter = null; trashMode = false; favoriteMode = false; journalMode = false; templateMode = false; allDocsHome = false; canvas = workspaceSettings.defaultPageMode == DefaultPageMode.WHITEBOARD; overview = false; settingsPage = false; revision++; savedRevision = revision } catch (e: Exception) { error = failure("创建失败","Create failed",e) } }
                        }, icon = AppIcon.DOCUMENT)
                        MenuRow(text("新建文件夹","New folder"), { rootCreateMenu = false; folderName = ""; folderError = null; createFolder = true }, icon = AppIcon.FOLDER, enabled = foldersAvailable)
                    }
                }
            }
            if (!folderFilter.isNullOrEmpty() && foldersAvailable) Row { TextButton(onClick={deleteEmptyFolder=true}) { Text(text("删除文件夹","Delete folder")) }; TextButton(onClick={folderName=folderFilter.orEmpty();folderError=null;renameEmptyFolder=true}) { Text(text("重命名文件夹","Rename folder")) } }
            Column(Modifier.fillMaxWidth().padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SidebarNavigationRow(AppIcon.PAGES, text("全部文档","All documents"), !trashMode && !favoriteMode && !journalMode && !templateMode && folderFilter == null, {
                    folderFilter = null
                    trashMode = false
                    favoriteMode = false
                    journalMode = false
                    templateMode = false
                    allDocsHome = true
                    overview = false
                    settingsPage = false
                })
                SidebarNavigationRow(AppIcon.FAVORITE, text("收藏","Favorites"), favoriteMode, {
                    folderFilter = null
                    trashMode = false
                    favoriteMode = true
                    journalMode = false
                    templateMode = false
                    allDocsHome = true
                    overview = false
                    settingsPage = false
                    canvas = false
                })
                SidebarNavigationRow(AppIcon.RECORD, text("Journals","Journals"), journalMode, {
                    folderFilter = null
                    trashMode = false
                    favoriteMode = false
                    templateMode = false
                    journalMode = true
                    allDocsHome = true
                    overview = false
                    settingsPage = false
                    canvas = false
                    journalSelectedDate = java.time.LocalDate.now().toString()
                })
                SidebarNavigationRow(AppIcon.CARD, text("模板","Templates"), templateMode, {
                    folderFilter = null
                    trashMode = false
                    favoriteMode = false
                    journalMode = false
                    templateMode = true
                    allDocsHome = true
                    overview = false
                    settingsPage = false
                    canvas = false
                })
                SidebarNavigationRow(AppIcon.TRASH, text("回收站","Trash"), trashMode, {
                    trashMode = true
                    favoriteMode = false
                    journalMode = false
                    templateMode = false
                    allDocsHome = false
                    overview = false
                    settingsPage = false
                }, trailing = {
                    Box {
                        CompactIconAction(
                            AppIcon.MORE,
                            text("回收站选项", "Trash options"),
                            { trashMenu = true },
                            LingyeColors.Muted,
                            selected = trashMenu
                        )
                        LingyeDropdownMenu(
                            expanded = trashMenu,
                            onDismissRequest = { trashMenu = false },
                            modifier = Modifier.width(208.dp)
                        ) {
                            val hasTrash = pages.any { it.trashed }
                            MenuRow(text("清空回收站", "Empty trash"), { trashMenu = false; trashSelectionDialog = false; trashClearConfirm = true }, icon = AppIcon.TRASH, enabled = hasTrash, danger = true)
                            MenuRow(text("多选", "Select multiple"), {
                                trashMenu = false
                                trashSelectedIds = emptySet()
                                trashSelectionDialog = true
                            }, icon = AppIcon.SELECT, enabled = hasTrash)
                        }
                    }
                })
                SidebarNavigationRow(AppIcon.IMPORT, text("导入","Import"), false, { importDialog = true })
            }
            val allTags = remember(pages.size, revision) {
                pages.filterNot { it.trashed }.flatMap { it.tags }.distinct().sorted()
            }
            if (allTags.isNotEmpty() && !trashMode) Row(
                Modifier.fillMaxWidth().padding(top = 8.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { tagFilter = null }) {
                    Text(text("全部标签","All tags"), fontSize = 11.sp, color = if (tagFilter == null) LingyeColors.Primary else LingyeColors.Muted)
                }
                allTags.forEach { tag ->
                    Surface(
                        onClick = { tagFilter = if (tagFilter == tag) null else tag },
                        shape = RoundedCornerShape(50),
                        color = if (tagFilter == tag) LingyeColors.PrimarySoft else LingyeColors.SubtleAlt
                    ) {
                        Text("#$tag", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 11.sp, color = if (tagFilter == tag) LingyeColors.Primary else LingyeColors.Muted)
                    }
                }
            }
            val pageSearchIndex = remember { PageSearchIndex() }
            val visiblePages = remember(pages.size, revision, search, folderFilter, trashMode, sharedBlocks.size, tagFilter, favoriteMode) {
                if (search.isBlank()) {
                    filterPages(
                        pages,
                        search,
                        if (trashMode) null else folderFilter,
                        trashMode,
                        sharedBlocks,
                        tagFilter,
                        favoriteMode
                    )
                } else {
                    pageSearchIndex.prepare(pages.toList(), sharedBlocks.toList(), revision)
                    filterPagesIndexed(
                        pages,
                        pageSearchIndex,
                        search,
                        if (trashMode) null else folderFilter,
                        trashMode,
                        tagFilter,
                        favoriteMode
                    )
                }
            }
            val pageRowModels = remember(pages.size, revision, metaRevision) {
                pages.associate { it.id to PageRowModel.of(it) }
            }
            val showFolderTree = !trashMode && !favoriteMode && folderFilter == null
            val linkedDocs = remember(current?.id, pages, revision, workspaceSettings.showLinkedDocs) {
                val page = current
                if (page == null || !workspaceSettings.showLinkedDocs) emptyList()
                else page.shapes
                    .filter { it.type == PageLinks.SHAPE_TYPE }
                    .mapNotNull { PageLinks.targetId(it) }
                    .distinct()
                    .mapNotNull { id -> pages.firstOrNull { it.id == id && !it.trashed } }
            }
            val backlinks = remember(current?.id, pages, revision, workspaceSettings.showLinkedDocs) {
                val page = current
                if (page == null || !workspaceSettings.showLinkedDocs) emptyList()
                else PageLinks.backlinks(page, pages)
            }
            LazyColumn(state = pageListState, modifier = Modifier.weight(1f).padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
              if (linkedDocs.isNotEmpty() && !trashMode) {
                item(key = "linked-docs-header", contentType = "linked-header") {
                  Text(
                      text("链接的文档", "Linked documents"),
                      Modifier.padding(start = 12.dp, top = 2.dp, bottom = 2.dp),
                      color = LingyeColors.Muted,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.SemiBold
                  )
                }
                items(linkedDocs, key = { "linked:${it.id}" }, contentType = { "linked-doc" }) { target ->
                  Row(
                      Modifier.fillMaxWidth().heightIn(min = 40.dp).clip(RoundedCornerShape(10.dp))
                          .clickable { openLinkedPage(target.id) }
                          .padding(horizontal = 12.dp, vertical = 6.dp),
                      verticalAlignment = Alignment.CenterVertically
                  ) {
                      WorkspaceIcon(AppIcon.LINK, null, Modifier.size(17.dp), LingyeColors.Muted)
                      Spacer(Modifier.width(9.dp))
                      Text(target.title.ifBlank { text("未命名页面", "Untitled page") }, fontSize = 14.sp, color = LingyeColors.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                  }
                }
              }
              if (backlinks.isNotEmpty() && !trashMode) {
                item(key = "backlinks-header", contentType = "backlink-header") {
                  Text(
                      text("反向链接", "Backlinks"),
                      Modifier.padding(start = 12.dp, top = 8.dp, bottom = 2.dp),
                      color = LingyeColors.Muted,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.SemiBold
                  )
                }
                items(backlinks, key = { "backlink:${it.id}:${it.icon}:${it.title}" }, contentType = { "backlink" }) { source ->
                  Row(
                      Modifier.fillMaxWidth().heightIn(min = 40.dp).clip(RoundedCornerShape(10.dp))
                          .clickable { openLinkedPage(source.id) }
                          .padding(horizontal = 12.dp, vertical = 6.dp),
                      verticalAlignment = Alignment.CenterVertically
                  ) {
                      PageGlyph(source, 18.dp)
                      Spacer(Modifier.width(9.dp))
                      Text(source.title.ifBlank { text("未命名页面", "Untitled page") }, fontSize = 14.sp, color = LingyeColors.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                  }
                }
              }
              if (showFolderTree) folders.forEach { name ->
                item(key = "folder:$name", contentType = "folder") {
                  val expanded = name in expandedFolders || search.isNotBlank()
                  Row(Modifier.fillMaxWidth().heightIn(min = 46.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { expandedFolders = if (name in expandedFolders) expandedFolders - name else expandedFolders + name }.padding(start = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                      Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        SidebarChevron(expanded, Modifier.size(20.dp))
                      }
                      Spacer(Modifier.width(4.dp)); SidebarFolderIcon(expanded, Modifier.size(20.dp)); Spacer(Modifier.width(12.dp))
                      Text(name, Modifier.weight(1f), maxLines = 1, color = LingyeColors.Ink, fontSize = 15.sp)
                    }
                    Box {
                      CompactIconAction(AppIcon.ADD, text("文件夹新增：","Add to folder: ") + name, { folderAddMenu = name }, LingyeColors.Muted)
                      LingyeDropdownMenu(expanded = folderAddMenu == name, onDismissRequest = { folderAddMenu = null }, modifier = Modifier.width(220.dp), offset = DpOffset(10.dp, 6.dp), shape = RoundedCornerShape(12.dp), containerColor = LingyeColors.SurfaceHigh, tonalElevation = 0.dp, shadowElevation = 6.dp, border = BorderStroke(1.dp, LingyeColors.Line)) {
                        MenuRow(text("新建文档","New document"), {
                          folderAddMenu = null
                          createPageInFolder(name)
                        }, icon = AppIcon.DOCUMENT)
                        MenuRow(text("新建文件夹","New folder"), {}, icon = AppIcon.FOLDER, enabled = false)
                        MenuRow(text("导入文件","Import file"), {}, icon = AppIcon.RESTORE, enabled = false)
                        MenuRow(text("从模板创建","Create from template"), {}, icon = AppIcon.CARD, enabled = false)
                      }
                    }
                    Box {
                      CompactIconAction(AppIcon.MORE, text("文件夹选项：","Folder options: ") + name, { folderOptionsMenu = name }, LingyeColors.Muted)
                      LingyeDropdownMenu(expanded = folderOptionsMenu == name, onDismissRequest = { folderOptionsMenu = null }, modifier = Modifier.width(220.dp), offset = DpOffset(10.dp, 6.dp), shape = RoundedCornerShape(12.dp), containerColor = LingyeColors.SurfaceHigh, tonalElevation = 0.dp, shadowElevation = 6.dp, border = BorderStroke(1.dp, LingyeColors.Line)) {
                        MenuRow(text("重命名","Rename"), { folderOptionsMenu = null; folderFilter = name; folderName = name; folderError = null; renameEmptyFolder = true }, icon = AppIcon.EDIT)
                        MenuRow(text("创建子文件夹","Create subfolder"), {}, icon = AppIcon.FOLDER, enabled = false)
                        MenuRow(text("添加文档","Add document"), { folderOptionsMenu = null; createPageInFolder(name) }, icon = AppIcon.DOCUMENT)
                        MenuRow(text("添加其他","Add other"), {}, icon = AppIcon.ADD, enabled = false)
                        MenuRow(text("加入收藏夹","Add to favorites"), {}, icon = AppIcon.FAVORITE, enabled = false)
                        HorizontalDivider(Modifier.padding(vertical = 5.dp), color = LingyeColors.Line)
                        MenuRow(text("删除文件夹","Delete folder"), { folderOptionsMenu = null; folderFilter = name; deleteEmptyFolder = true }, icon = AppIcon.TRASH, danger = true)
                      }
                    }
                  }
                }
                if (name in expandedFolders || search.isNotBlank()) items(visiblePages.filter { name in it.folders }, key = { "folder-page:$name:${it.id}" }, contentType = { "folder-page" }) { page ->
                  val rowModel = pageRowModels[page.id] ?: PageRowModel.of(page)
                  SidebarPageItem(
                      model = rowModel,
                      selected = selected?.id == page.id && !settingsPage && !allDocsHome,
                      trashMode = false,
                      language = language,
                      pageActionsExpanded = pageActionsMenu == page.id,
                      onActionsExpandedChange = { open -> pageActionsMenu = if (open) page.id else null },
                      onOpen = { if (saveCurrent()) { clearLinkOrigin(); selected = page; favoriteMode = false; trashMode = false; journalMode = false; templateMode = false; allDocsHome = false; overview = false; settingsPage = false; revision++; savedRevision = revision } },
                      onRestore = {},
                      onRename = { pageActionsMenu = null; pageRenameDraft = page.title; pageRenameTarget = page.id },
                      onInfo = { pageActionsMenu = null; pageInfoTarget = page.id },
                      onAddLinkedDoc = { pageActionsMenu = null; linkedDocSourceId = page.id },
                      onDuplicate = { duplicatePage(page) },
                      onOpenInNewTab = { openPageInNewTab(page) },
                      onFavorite = { togglePageFavorite(page) },
                      onRemoveFolder = { removePageFromFolder(page) },
                      onTrash = { trashPage(page) },
                      indented = true
                  )
                }
              }
              items(if (showFolderTree) visiblePages.filter { it.folders.isEmpty() } else visiblePages, key = { "page:${it.id}" }, contentType = { if (trashMode) "trash-page" else "page" }) { page ->
                val rowModel = pageRowModels[page.id] ?: PageRowModel.of(page)
                SidebarPageItem(
                    model = rowModel,
                    selected = selected?.id == page.id && !trashMode && !settingsPage && !allDocsHome,
                    trashMode = trashMode,
                    language = language,
                    pageActionsExpanded = pageActionsMenu == page.id,
                    onActionsExpandedChange = { open -> pageActionsMenu = if (open) page.id else null },
                    onOpen = { if (saveCurrent()) { clearLinkOrigin(); selected = page; favoriteMode = false; trashMode = false; journalMode = false; templateMode = false; allDocsHome = false; overview = false; settingsPage = false; revision++; savedRevision = revision } },
                    onRestore = {
                        if (saveCurrent()) {
                            page.trashed = false
                            try {
                                repository.save(page)
                                revision++
                                savedRevision = revision
                                notice = text("页面已恢复", "Page restored")
                            } catch (e: Exception) {
                                page.trashed = true
                                error = failure("恢复失败", "Restore failed", e)
                            }
                        }
                    },
                    onRename = { pageActionsMenu = null; pageRenameDraft = page.title; pageRenameTarget = page.id },
                    onInfo = { pageActionsMenu = null; pageInfoTarget = page.id },
                    onAddLinkedDoc = { pageActionsMenu = null; linkedDocSourceId = page.id },
                    onDuplicate = { duplicatePage(page) },
                    onOpenInNewTab = { openPageInNewTab(page) },
                    onFavorite = { togglePageFavorite(page) },
                    onRemoveFolder = { removePageFromFolder(page) },
                    onTrash = { trashPage(page) }
                )
            } }
            HorizontalDivider(color=LingyeColors.Line.copy(alpha=0.7f),modifier=Modifier.padding(vertical=6.dp))
            SidebarNavigationRow(AppIcon.SETTINGS, text("设置","Settings"), settingsPage, { settingsPage=true })
          } }
        }
        Surface(
            Modifier.weight(1f).fillMaxHeight(),
            shape = RoundedCornerShape(0.dp),
            color = LingyeColors.Surface,
            shadowElevation = 0.dp
        ) {
        Column(Modifier.fillMaxSize()) {
            if (openTabs.isNotEmpty() && current != null && !allDocsHome && !settingsPage && !overview) {
                val tabPages = openTabs.mapNotNull { id -> pages.firstOrNull { it.id == id && !it.trashed } }
                if (tabPages.isNotEmpty()) {
                    PageTabStrip(
                        tabs = tabPages.map { PageRowModel.of(it) },
                        activeId = current.id,
                        language = language,
                        onSelect = { tab ->
                            val page = pages.firstOrNull { it.id == tab.id && !it.trashed }
                            if (page != null && saveCurrent()) {
                                selected = page
                                allDocsHome = false
                                overview = false
                                settingsPage = false
                                canvas = false
                                revision++
                                savedRevision = revision
                            }
                        },
                        onClose = { tab ->
                            openTabs.remove(tab.id)
                            if (selected?.id == tab.id) {
                                val next = openTabs.lastOrNull()?.let { id -> pages.firstOrNull { it.id == id && !it.trashed } }
                                if (next != null && saveCurrent()) {
                                    selected = next
                                    allDocsHome = false
                                    canvas = false
                                    overview = false
                                    settingsPage = false
                                    revision++
                                    savedRevision = revision
                                } else {
                                    saveCurrent()
                                    allDocsHome = true
                                }
                            }
                        }
                    )
                }
            }
            val editorHeaderVisible = !canvasFocusMode && !allDocsHome && !settingsPage && !overview && current != null
            if (editorHeaderVisible) {
                Row(
                    Modifier.fillMaxWidth().height(62.dp).padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!sidebarVisible) {
                        CompactIconAction(AppIcon.SIDEBAR, text("展开侧栏", "Expand sidebar"), ::toggleSidebar)
                        Spacer(Modifier.width(4.dp))
                    }
                    Row(
                        Modifier.background(LingyeColors.ModeSwitchBg, RoundedCornerShape(11.dp)).padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompactIconAction(
                            AppIcon.DOCUMENT,
                            text("文档", "Document"),
                            { canvas = false; overview = false; settingsPage = false },
                            selected = !canvas,
                            selectedBackground = Color.White
                        )
                        CompactIconAction(
                            AppIcon.CANVAS,
                            text("白板", "Whiteboard"),
                            { canvas = true; overview = false; settingsPage = false },
                            selected = canvas,
                            selectedBackground = Color.White
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    if (current.icon.isNotBlank()) {
                        Box(
                            Modifier.size(32.dp).clip(RoundedCornerShape(9.dp))
                                .clickable { iconTargetId = current.id }
                                .semantics { contentDescription = text("更改页面图标", "Change page icon") },
                            contentAlignment = Alignment.Center
                        ) { PageGlyph(current, 22.dp) }
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        current.title.ifBlank { text("未命名页面", "Untitled") },
                        Modifier.weight(1f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LingyeColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    CompactIconAction(
                        if (current.favorite) AppIcon.FAVORITED else AppIcon.FAVORITE,
                        text("收藏页面", "Favorite page"),
                        { togglePageFavorite(current) },
                        if (current.favorite) LingyeColors.Primary else LingyeColors.Muted,
                        selected = current.favorite
                    )
                    if (workspaceSettings.displayDocInfo) {
                        CompactIconAction(AppIcon.INFO, text("查看信息", "View info"), {
                            pageInfoTarget = current.id
                        })
                    }
                    Box {
                        CompactIconAction(
                            AppIcon.MORE,
                            text("更多", "More"),
                            { pageMoreMenu = true },
                            LingyeColors.Muted,
                            selected = pageMoreMenu
                        )
                        LingyeDropdownMenu(
                            expanded = pageMoreMenu,
                            onDismissRequest = { pageMoreMenu = false; exportSubmenu = false },
                            modifier = Modifier.width(264.dp),
                            offset = DpOffset(8.dp, 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                            border = null
                        ) {
                            val popupView = LocalView.current
                            Surface(
                                    Modifier.width(264.dp).onGloballyPositioned { exportMenuLocal = it.boundsInWindow().toIntRectPx() },
                                    shape = RoundedCornerShape(12.dp),
                                    color = LingyeColors.SurfaceHigh,
                                    shadowElevation = 6.dp,
                                    border = BorderStroke(1.dp, LingyeColors.Line)
                                ) {
                                    Column {
                            MenuRow(text("重命名", "Rename"), {
                                pageMoreMenu = false; exportSubmenu = false
                                pageRenameDraft = current.title
                                pageRenameTarget = current.id
                            }, icon = AppIcon.EDIT)
                            MenuRow(
                                if (current.favorite) text("取消收藏", "Remove from saved") else text("加入收藏", "Add to saved"),
                                { pageMoreMenu = false; exportSubmenu = false; togglePageFavorite(current) },
                                icon = if (current.favorite) AppIcon.FAVORITED else AppIcon.FAVORITE
                            )
                            HorizontalDivider(Modifier.padding(vertical = 5.dp), color = LingyeColors.Line)
                            MenuRow(text("查看信息", "View info"), {
                                pageMoreMenu = false; exportSubmenu = false; pageInfoTarget = current.id
                            }, icon = AppIcon.INFO)
                            MenuRow(text("查看所有的 Frames", "View all frames"), {
                                pageMoreMenu = false; exportSubmenu = false
                                frameNavigatorRequest = true
                                canvas = true; overview = false; settingsPage = false; allDocsHome = false
                            }, icon = AppIcon.FRAME)
                            MenuRow(text("查看历史版本", "View version history"), {
                                pageMoreMenu = false; exportSubmenu = false; historyVersionsDialog = true
                            }, icon = AppIcon.RECORD)
                            HorizontalDivider(Modifier.padding(vertical = 5.dp), color = LingyeColors.Line)
                            MenuRow(text("复制", "Duplicate"), {
                                pageMoreMenu = false; exportSubmenu = false
                                if (saveCurrent()) {
                                    val copyTitle = (current.title.ifBlank { text("未命名页面", "Untitled page") } + text(" 副本", " copy")).take(120)
                                    try {
                                        val copy = current.duplicate(copyTitle)
                                        repository.save(copy)
                                        pages.add(copy)
                                        selected = copy
                                        revision++
                                        savedRevision = revision
                                        error = null
                                        notice = text("已复制页面", "Page duplicated")
                                    } catch (e: Exception) {
                                        error = failure("复制失败", "Duplicate failed", e)
                                    }
                                }
                            }, icon = AppIcon.COPY)
                            MenuRow(text("导入", "Import"), {
                                pageMoreMenu = false; exportSubmenu = false; importDialog = true
                            }, icon = AppIcon.IMPORT)
                            DropdownMenuItem(
                                text = { Text("Export") },
                                leadingIcon = { WorkspaceIcon(AppIcon.SAVE, null, Modifier.size(19.dp), LingyeColors.Muted) },
                                trailingIcon = { Text("›", fontSize = 16.sp, color = LingyeColors.Muted) },
                                modifier = Modifier.onGloballyPositioned { exportRowLocal = it.boundsInWindow().toIntRectPx() },
                                onClick = {
                                    val loc = IntArray(2)
                                    popupView.getLocationOnScreen(loc)
                                    exportMenuLocal?.let { menu ->
                                        exportMenuLeft = loc[0] + menu.left
                                        exportMenuTop = loc[1] + menu.top
                                        exportRowLocal?.let { row -> exportRowTopInMenu = row.top - menu.top }
                                    }
                                    exportSubmenu = true
                                }
                            )
                            HorizontalDivider(Modifier.padding(vertical = 5.dp), color = LingyeColors.Line)
                            MenuRow(text("移动到回收站", "Move to trash"), {
                                pageMoreMenu = false; exportSubmenu = false; trashPage(current)
                            }, icon = AppIcon.TRASH, danger = true)
                                    }
                                }
                        }
                        if (pageMoreMenu && exportSubmenu && exportMenuLocal != null) {
                            val gapPx = with(density) { 8.dp.roundToPx() }
                            Popup(
                                popupPositionProvider = remember(exportMenuLeft, exportMenuTop, exportRowTopInMenu, gapPx) {
                                    ExportSubmenuPositionProvider(exportMenuLeft, exportMenuTop, exportRowTopInMenu, gapPx)
                                },
                                onDismissRequest = { exportSubmenu = false },
                                properties = PopupProperties(
                                    focusable = false,
                                    dismissOnBackPress = false,
                                    dismissOnClickOutside = false
                                )
                            ) {
                                val exportName = current.title.ifBlank { "page" }
                                    .replace(Regex("[\\\\/:*?\"<>|]"), "_").take(80)
                                ExportSubmenuPanel(
                                    language = language,
                                    onExportHtml = {
                                        pageMoreMenu = false; exportSubmenu = false
                                        htmlExporter.launch("$exportName.html")
                                    },
                                    onExportMarkdown = {
                                        pageMoreMenu = false; exportSubmenu = false
                                        markdownExporter.launch("$exportName.md")
                                    },
                                    onCopyMarkdown = {
                                        pageMoreMenu = false; exportSubmenu = false
                                        val page = current
                                        scope.launch {
                                            val content = withContext(Dispatchers.Default) {
                                                MarkdownCodec.encode(page.snapshot(), sharedBlocks.toList())
                                            }
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Markdown", content))
                                            notice = text("Markdown 已复制", "Markdown copied")
                                        }
                                    },
                                    onExportSnapshot = {
                                        pageMoreMenu = false; exportSubmenu = false
                                        snapshotExporter.launch("$exportName.json")
                                    }
                                )
                            }
                        }
                    }
                    CompactIconAction(
                        AppIcon.SIDEBAR,
                        text("页面属性", "Page properties"),
                        { pageSidebarOpen = !pageSidebarOpen },
                        LingyeColors.Muted,
                        selected = pageSidebarOpen
                    )
                }
                HorizontalDivider(color = LingyeColors.Line.copy(alpha = 0.7f))
            }
            if (failedPages.isNotEmpty()) Row(Modifier.fillMaxWidth().background(LingyeColors.ErrorSoft).padding(8.dp)) { Text(text("部分页面无法读取，原始文件已保留：","Some pages could not be read. Original files were kept: ") + failedPages.joinToString(), color=LingyeColors.ErrorInk) }
            error?.let { message -> Row(Modifier.fillMaxWidth().background(LingyeColors.ErrorSoft).padding(8.dp), verticalAlignment = Alignment.CenterVertically) { Text(message, Modifier.weight(1f), color = LingyeColors.ErrorInk); TextButton(onClick = { saveCurrent() }) { Text(text("重试","Retry")) } } }
            if (overview) {
                val storage by produceState(initialValue = 0L, pages.size, revision) {
                    value = withContext(Dispatchers.IO) { store.storageBytes(pages) }
                }
                WorkspaceOverview(
                    pages,
                    folders.size,
                    workspaceTemplates.size,
                    modifiedTimes,
                    storage,
                    language,
                    onBack = { overview = false; canvas = false }
                ) { page -> clearLinkOrigin(); selected = page; allDocsHome=false; overview=false; canvas=false; revision++; savedRevision=revision }
            } else if (journalMode && allDocsHome) {
                JournalsHome(
                    pages = pages,
                    journalDates = journalDates,
                    language = language,
                    selectedDate = journalSelectedDate,
                    onSelectDate = { journalSelectedDate = it },
                    onOpenPage = ::openLibraryPage,
                    onCreateForDate = ::createJournalFor,
                    showSidebarToggle = !sidebarVisible,
                    onToggleSidebar = ::toggleSidebar
                )
            } else if (templateMode && allDocsHome) {
                TemplatesHome(
                    pages = pages,
                    templatePageIds = templatePageIds,
                    journalDates = journalDates,
                    modifiedTimes = modifiedTimes,
                    language = language,
                    onOpenPage = ::openLibraryPage,
                    onCreateTemplate = ::createTemplatePage,
                    onUseTemplate = ::useTemplatePage,
                    showSidebarToggle = !sidebarVisible,
                    onToggleSidebar = ::toggleSidebar
                )
            } else if (allDocsHome) {
                AllDocumentsHome(
                    pages = pages,
                    modifiedTimes = modifiedTimes,
                    language = language,
                    showSidebarToggle = !sidebarVisible,
                    onToggleSidebar = ::toggleSidebar,
                    onOpenPage = { page ->
                        if (saveCurrent()) {
                            clearLinkOrigin()
                            selected = page
                            favoriteMode = false
                            trashMode = false
                            journalMode = false
                            templateMode = false
                            allDocsHome = false
                            overview = false
                            settingsPage = false
                            canvas = false
                            revision++
                            savedRevision = revision
                        }
                    },
                    onToggleFavorite = ::togglePageFavorite,
                    onCreatePage = { whiteboard ->
                        if (saveCurrent()) {
                            val page = Page.create(newPageTitle(language, workspaceSettings))
                            if (favoriteMode) page.favorite = true
                            try {
                                repository.save(page)
                                pages.add(page)
                                selected = page
                                allDocsHome = false
                                canvas = whiteboard
                                overview = false
                                settingsPage = false
                                revision++
                                savedRevision = revision
                            } catch (e: Exception) {
                                error = failure("创建失败", "Create failed", e)
                            }
                        }
                    },
                    folders = folders,
                    onMovePageToFolder = ::movePageToFolder,
                    onTrashPages = ::trashPages,
                    contentRevision = revision,
                    startInSelectionMode = allDocsSelectionRequest.isNotEmpty(),
                    initialSelectedPageId = allDocsSelectionRequest.firstOrNull(),
                    onInitialSelectionConsumed = { allDocsSelectionRequest = emptySet() },
                    favoritesOnly = favoriteMode
                )
            } else if (current == null) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text("创建一个页面，开始记录想法","Create a page to start capturing ideas"), color = LingyeColors.Muted) }
            else if (!canvas) {
                val editorFont = editorFontFamilyOf(workspaceSettings.editorFontFamily)
                val updatedMillis = modifiedTimes[current.id] ?: 0L
                val createdMillis = remember(current.id, updatedMillis) {
                    store.pageCreatedAt(current.id, updatedMillis.takeIf { it > 0L } ?: System.currentTimeMillis())
                }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                BlocksEditor(
                    current, store, revision, language, workspaceSettings.editorFontSize, editorFont,
                    workspaceSettings.smoothImages, workspaceSettings.codeBlockLineNumbers,
                    sharedBlocks, sharedAvailable, ::replaceShared, ::editShared, ::stepShared,
                    ::replaceSharedImage, ::change, ::changeGrouped,
                    scrollState = documentScrollState,
                    addImageAfter = { index ->
                        imageTarget = current
                        imageForCanvas = false
                        imageInsertIndex = index + 1
                        picker.launch(arrayOf("image/*", "application/octet-stream"))
                    },
                    modifier = Modifier.fillMaxHeight()
                        .then(if (workspaceSettings.documentFullWidth) Modifier.fillMaxWidth() else Modifier.widthIn(max = 860.dp).fillMaxWidth())
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    header = {
                        Column(Modifier.padding(start = 72.dp)) {
                            Row(
                                Modifier.clickable { iconTargetId = current.id }.padding(bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (current.icon.isBlank()) {
                                    WorkspaceIcon(AppIcon.ADD, null, Modifier.size(16.dp), LingyeColors.Muted)
                                } else {
                                    PageGlyph(current, 18.dp)
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text(if (current.icon.isBlank()) "添加图标" else "更改图标", if (current.icon.isBlank()) "Add icon" else "Change icon"),
                                    fontSize = 12.sp,
                                    color = LingyeColors.Muted
                                )
                            }
                            BasicTextField(value = current.title, onValueChange = { current.title = it; changeGrouped("page-title:${current.id}") }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).semantics { contentDescription = text("标题","Title") }, singleLine = true, textStyle = LocalTextStyle.current.copy(fontSize = (workspaceSettings.editorFontSize + 14).sp, fontWeight = FontWeight.Bold, fontFamily = editorFont, color = LingyeColors.Ink), cursorBrush = SolidColor(LingyeColors.Primary), decorationBox = { inner -> Box { if (current.title.isEmpty()) Text(text("未命名页面","Untitled page"), color = LingyeColors.Muted, fontSize = (workspaceSettings.editorFontSize + 14).sp, fontFamily = editorFont); inner() } })
                            DocumentInfoCard(
                                page = current,
                                language = language,
                                properties = sidebarProperties,
                                onPropertiesChange = { list ->
                                    metadataRepository.saveDocProperties(current.id, list)
                                    pageSidebarRevision++
                                },
                                createdMillis = createdMillis,
                                updatedMillis = updatedMillis,
                                canvasMode = canvas,
                                onCanvasModeChange = { canvas = it; overview = false; allDocsHome = false },
                                fullWidth = workspaceSettings.documentFullWidth,
                                onFullWidthChange = { updateWorkspaceSettings(workspaceSettings.copy(documentFullWidth = it)) },
                                canvasTheme = workspaceSettings.canvasTheme,
                                onCanvasThemeChange = { updateWorkspaceSettings(workspaceSettings.copy(canvasTheme = it)) },
                                onAddProperty = { pageSidebarOpen = true },
                                onEditTags = {
                                    tagDraft = current.tags.joinToString(", ")
                                    tagEditor = true
                                },
                                onEditIcon = { iconTargetId = current.id },
                                journalDate = journalDates[current.id],
                                onJournalDateChange = { date ->
                                    if (date == null) {
                                        metadataRepository.setJournalDate(current.id, null)
                                        metaRevision++
                                    } else if (saveCurrent()) {
                                        metadataRepository.setJournalDate(current.id, date)
                                        metaRevision++
                                        journalSelectedDate = date
                                        journalMode = true
                                        templateMode = false
                                        favoriteMode = false
                                        trashMode = false
                                        allDocsHome = true
                                        overview = false
                                        settingsPage = false
                                        canvas = false
                                    }
                                },
                                templatePage = templatePageIds.contains(current.id),
                                onTemplateChange = { value ->
                                    metadataRepository.setTemplate(current.id, value)
                                    metaRevision++
                                }
                            )
                            Spacer(Modifier.height(16.dp))
                            BasicTextField(value = current.text, onValueChange = { current.updateText(it); changeGrouped("page-text:${current.id}") }, modifier = Modifier.fillMaxWidth().heightIn(min = 70.dp).semantics { contentDescription = text("正文","Page text") }, textStyle = LocalTextStyle.current.copy(fontSize = workspaceSettings.editorFontSize.sp, fontFamily = editorFont, color = LingyeColors.Ink), cursorBrush = SolidColor(LingyeColors.Primary), decorationBox = { inner -> Box { if (current.text.isEmpty()) Text(text("写下你的想法…","Write down your idea…"), color = LingyeColors.Muted, fontFamily = editorFont); inner() } })
                            current.shapes.filter { it.type == "text" && textDisplayInPage(it) && it.body.isNotBlank() }.forEach { shape ->
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    shape.body,
                                    Modifier.fillMaxWidth(),
                                    color = Color(shape.textColor),
                                    fontSize = shape.fontSize.sp,
                                    fontFamily = when (shape.fontFamily) {
                                        "serif" -> FontFamily.Serif
                                        "mono" -> FontFamily.Monospace
                                        else -> editorFont
                                    },
                                    fontWeight = FontWeight(shape.fontWeight),
                                    fontStyle = if (shape.fontStyle == "italic") FontStyle.Italic else FontStyle.Normal,
                                    textAlign = when (shape.textAlign) {
                                        "center" -> TextAlign.Center
                                        "right" -> TextAlign.End
                                        else -> TextAlign.Start
                                    }
                                )
                            }
                        }
                    },
                    footer = { DocumentLinksPanel(current, pages, language) },
                )
                }
            } else CanvasEditor(current, store, pages, workspaceTemplates, sharedBlocks, templatesAvailable, ::replaceTemplates, revision + renderRevision, language, workspaceSettings, canvasFocusMode, { canvasFocusMode = it }, ::beginGesture, ::commitGesture, ::cancelGesture, tool, { tool = it }, penOnly, { penOnly = it; workspaceRepository.savePenOnly(it) }, eraserArea, { eraserArea = it; workspaceRepository.saveEraserArea(it) }, brushKind, { brushKind = it; workspaceRepository.saveBrushKind(it) }, highlighterColor, { highlighterColor = it; workspaceRepository.saveHighlighterColor(it) }, highlighterWidth, { highlighterWidth = it; workspaceRepository.saveHighlighterWidth(it) }, inkColor, { inkColor = it; workspaceRepository.saveInkColor(it) }, inkWidth, { inkWidth = it; workspaceRepository.saveInkWidth(it) }, ::updateWorkspaceSettings, ::change, ::changeGrouped, ::redraw, { imageTarget = current; imageForCanvas = true; picker.launch(arrayOf("image/*", "application/octet-stream")) }, { applyHistory(history?.undo()) }, { applyHistory(history?.redo()) }, { if (!returnToOriginPage()) canvas = false }, { id -> openLinkedPage(id) }, restoredSession.canvasScale, restoredSession.canvasOffsetX, restoredSession.canvasOffsetY, restoredSession.selectedObjectIds, { scale, offsetX, offsetY -> sessionCanvasScale=scale;sessionCanvasOffsetX=offsetX;sessionCanvasOffsetY=offsetY }, { ids -> sessionSelectedObjectIds=ids }, showFrameNavigator = frameNavigatorRequest, onFrameNavigatorShown = { frameNavigatorRequest = false })
        }
        }
        if (pageSidebarOpen && current != null && !allDocsHome && !settingsPage && !overview) {
            val sidePage = current!!
            DocPropertiesSidebar(
                language = language,
                pageTitle = sidePage.title.ifBlank { text("未命名页面", "Untitled page") },
                properties = sidebarProperties,
                onPropertiesChange = { list ->
                    metadataRepository.saveDocProperties(sidePage.id, list)
                    pageSidebarRevision++
                },
                folders = folders.toList(),
                onClose = { pageSidebarOpen = false },
                onRename = {
                    pageSidebarOpen = false
                    pageRenameDraft = sidePage.title
                    pageRenameTarget = sidePage.id
                },
                onChangeIcon = {
                    pageSidebarOpen = false
                    iconTargetId = sidePage.id
                },
                onEditTags = {
                    pageSidebarOpen = false
                    tagDraft = sidePage.tags.joinToString(", ")
                    tagEditor = true
                },
                onOverview = {
                    pageSidebarOpen = false
                    overview = true
                    canvas = false
                },
                onMultiSelect = {
                    pageSidebarOpen = false
                    allDocsSelectionRequest = setOf(sidePage.id)
                    trashMode = false; favoriteMode = false
                    allDocsHome = true; overview = false; settingsPage = false; canvas = false
                },
                onUndo = if (!canvas) ({ applyHistory(history?.undo()) }) else null,
                onRedo = if (!canvas) ({ applyHistory(history?.redo()) }) else null,
                onExport = {
                    val name = sidePage.title.ifBlank { "page" }.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(80)
                    markdownExporter.launch("$name.md")
                },
                onTrash = {
                    pageSidebarOpen = false
                    trashPage(sidePage)
                },
                onMoveToFolder = { folder -> movePageToFolder(sidePage, folder) }
            )
        }
      }
    }
    if (settingsPage) {
        val settingsStorage by produceState(initialValue = 0L, pages.size, revision, imageCleanupRevision) {
            value = withContext(Dispatchers.IO) { store.storageBytes(pages) }
        }
        val imageStats by produceState(
            initialValue = OrphanImages(emptyList(), 0L),
            pages.size, revision, imageCleanupRevision, workspaceTemplates.size, sharedBlocks.size
        ) {
            value = withContext(Dispatchers.IO) {
                val names = store.orphanImages(pages.toList(), workspaceTemplates.toList(), sharedBlocks.toList())
                OrphanImages(names, store.imageBytes(names))
            }
        }
        val imageCache by produceState(initialValue = ImageCacheStats(0, 0, 0, 0, 0), revision, imageCleanupRevision) {
            value = withContext(Dispatchers.IO) { store.imageCacheStats() }
        }
        Dialog(
            onDismissRequest = { settingsPage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.92f),
                shape = RoundedCornerShape(18.dp),
                color = LingyeColors.Surface,
                shadowElevation = 16.dp
            ) {
                SettingsPage(
                    language = language,
                    onLanguageChange = { language=it;workspaceRepository.writeLanguage(it);notice=null;error=null },
                    settings = workspaceSettings,
                    onSettingsChange = ::updateWorkspaceSettings,
                    penOnly = penOnly,
                    onPenOnlyChange = { penOnly=it;workspaceRepository.savePenOnly(it) },
                    inkColor = inkColor,
                    onInkColorChange = { inkColor=it;workspaceRepository.saveInkColor(it) },
                    inkWidth = inkWidth,
                    onInkWidthChange = { inkWidth=it;workspaceRepository.saveInkWidth(it) },
                    pageCount = pages.size,
                    trashedPageCount = pages.count { it.trashed },
                    folderCount = folders.size,
                    templateCount = workspaceTemplates.size,
                    storageBytes = settingsStorage,
                    unusedImageCount = imageStats.names.size,
                    unusedImageBytes = imageStats.bytes,
                    imageCache = imageCache,
                    onCleanupImages = { imageCleanupConfirm = true },
                    onBackup = { if (foldersAvailable && saveCurrent()) exporter.launch("lingye-backup.zip") },
                    onRestore = { if (foldersAvailable && saveCurrent()) importer.launch(arrayOf("application/zip", "application/octet-stream")) },
                    onImportMarkdown = { markdownImporter.launch(arrayOf("text/markdown", "text/plain", "text/*")) },
                    onExportMarkdown = {
                        val page = current
                        if (page == null) error = text("请先打开一个页面","Open a page first")
                        else {
                            val name = page.title.ifBlank { "page" }
                                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                                .take(80)
                            markdownExporter.launch("$name.md")
                        }
                    },
                    onClose = { settingsPage = false }
                )
            }
        }
        if (imageCleanupConfirm) {
            AlertDialog(
                onDismissRequest = { imageCleanupConfirm = false },
                title = { Text(text("清理未使用图片？", "Clean up unused images?")) },
                text = {
                    Text(
                        text(
                            "将删除 ${imageStats.names.size} 个未被引用的图片（${formatStorage(imageStats.bytes, language)}）。删除后不可恢复，建议先导出备份。",
                            "Delete ${imageStats.names.size} unreferenced image(s) (${formatStorage(imageStats.bytes, language)})? This cannot be undone, so export a backup first."
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        imageCleanupConfirm = false
                        if (failedPages.isNotEmpty()) {
                            error = text("有页面无法读取，无法安全清理图片。", "Some pages could not be read; image cleanup is unavailable.")
                        } else {
                            val names = imageStats.names
                            ioBusy = true
                            scope.launch {
                                try {
                                    val removed = withContext(Dispatchers.IO) { store.deleteImages(names) }
                                    imageCleanupRevision++
                                    notice = text("已清理 $removed 张未使用图片", "Removed $removed unused image(s)")
                                    error = null
                                } catch (e: Exception) {
                                    error = failure("图片清理失败", "Image cleanup failed", e)
                                } finally {
                                    ioBusy = false
                                }
                            }
                        }
                    }) { Text(text("删除", "Delete")) }
                },
                dismissButton = {
                    TextButton(onClick = { imageCleanupConfirm = false }) { Text(text("取消", "Cancel")) }
                }
            )
        }
    }
    if (ioBusy) Dialog(onDismissRequest = {}) { Surface(shape = RoundedCornerShape(20.dp)) { Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(28.dp)); Spacer(Modifier.width(16.dp)); Text(text("正在处理，请稍候…","Processing, please wait…")) } } }
    if(deleteEmptyFolder) AlertDialog(onDismissRequest={deleteEmptyFolder=false},title={Text(text("删除文件夹？","Delete folder?"))},text={Text(text("页面会从该文件夹移除；其他文件夹归属保持不变。","Pages are removed from this folder; other folder memberships remain."))},confirmButton={TextButton(onClick={ val name=folderFilter; if(name!=null && name.isNotEmpty()) { if(failedPages.isNotEmpty()) error=text("有页面无法读取，无法安全修改文件夹。","Some pages could not be read; folder changes are unavailable.") else if(saveCurrent()) try { val next=store.moveFolderContents(name,"",pages.toList(),folders.toList());pages.filter { name in it.folders }.forEach { it.folders.remove(name) };folders.clear();folders.addAll(next);folderFilter=null;revision++;savedRevision=revision;notice=text("文件夹已删除","Folder deleted") } catch(e:Exception) { error=failure("删除文件夹失败","Folder deletion failed",e) } };deleteEmptyFolder=false }) { Text(text("删除","Delete")) }},dismissButton={TextButton(onClick={deleteEmptyFolder=false}) {Text(text("取消","Cancel"))}})
    if(renameEmptyFolder) AlertDialog(onDismissRequest={renameEmptyFolder=false},title={Text(text("重命名文件夹","Rename folder"))},text={Column { OutlinedTextField(folderName,{folderName=it;folderError=null},label={Text(text("文件夹名称","Folder name"))},singleLine=true);folderError?.let { Text(it,color=LingyeColors.ErrorInk) } }},confirmButton={TextButton(onClick={val old=folderFilter;val name=folderName.trim();when { old.isNullOrEmpty()->renameEmptyFolder=false; failedPages.isNotEmpty() -> folderError=text("有页面无法读取，无法安全修改文件夹。","Some pages could not be read; folder changes are unavailable."); !FolderCatalog.valid(name)->folderError=text("请输入 1–100 个字符的名称","Enter a name with 1–100 characters"); folders.any { it!=old && it.equals(name,true) }->folderError=text("文件夹名称已存在","That folder name already exists"); else->try { check(saveCurrent());val next=store.moveFolderContents(old,name,pages.toList(),folders.toList());pages.filter { old in it.folders }.forEach { it.folders.remove(old); it.folders.add(name) };folders.clear();folders.addAll(next);folderFilter=name;revision++;savedRevision=revision;renameEmptyFolder=false } catch(e:Exception) { folderError=failure("重命名失败","Rename failed",e) } } }) {Text(text("保存","Save"))}},dismissButton={TextButton(onClick={renameEmptyFolder=false}) {Text(text("取消","Cancel"))}})
    if (trashSelectionDialog) {
        val trashedPages = pages.filter { it.trashed }.sortedBy { it.title.lowercase() }
        Dialog(
            onDismissRequest = { trashSelectionDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                Modifier.widthIn(min = 360.dp, max = 560.dp).fillMaxWidth().heightIn(max = 620.dp),
                shape = RoundedCornerShape(18.dp),
                color = LingyeColors.Surface,
                shadowElevation = 16.dp
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(text("回收站多选", "Select in trash"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LingyeColors.Ink)
                            Text(
                                "${trashSelectedIds.size} " + text("项已选择", "selected"),
                                fontSize = 12.sp,
                                color = LingyeColors.Muted
                            )
                        }
                        TextButton(onClick = {
                            trashSelectedIds =
                                if (trashedPages.isNotEmpty() && trashSelectedIds.size == trashedPages.size) emptySet()
                                else trashedPages.map { it.id }.toSet()
                        }) {
                            Text(text("全选", "Select all"), color = LingyeColors.Primary)
                        }
                        CompactIconAction(AppIcon.CLOSE, text("关闭", "Close"), { trashSelectionDialog = false }, LingyeColors.Muted)
                    }
                    HorizontalDivider(color = LingyeColors.Line, modifier = Modifier.padding(vertical = 8.dp))
                    if (trashedPages.isEmpty()) {
                        Text(
                            text("回收站是空的", "Trash is empty"),
                            fontSize = 14.sp,
                            color = LingyeColors.Muted,
                            modifier = Modifier.padding(vertical = 28.dp)
                        )
                    } else {
                        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 358.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            items(trashedPages, key = { it.id }) { page ->
                                val checked = page.id in trashSelectedIds
                                Row(
                                    Modifier.fillMaxWidth().height(58.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (checked) LingyeColors.Subtle else Color.Transparent)
                                        .clickable {
                                            trashSelectedIds =
                                                if (checked) trashSelectedIds - page.id else trashSelectedIds + page.id
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked, onCheckedChange = { on ->
                                        trashSelectedIds =
                                            if (on) trashSelectedIds + page.id else trashSelectedIds - page.id
                                    })
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        page.title.ifBlank { text("未命名文档", "Untitled document") },
                                        fontSize = 14.sp,
                                        color = LingyeColors.Ink,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = LingyeColors.Line, modifier = Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            enabled = trashSelectedIds.isNotEmpty(),
                            onClick = { if (restoreTrashedPages(trashSelectedIds)) trashSelectedIds = emptySet() }
                        ) {
                            Text(text("恢复所选", "Restore selected"), color = if (trashSelectedIds.isEmpty()) LingyeColors.Muted else LingyeColors.Primary)
                        }
                        Spacer(Modifier.weight(1f))
                        Button(
                            enabled = trashSelectedIds.isNotEmpty(),
                            onClick = { trashDeleteConfirm = trashSelectedIds },
                            colors = ButtonDefaults.buttonColors(containerColor = LingyeColors.Coral)
                        ) {
                            Text(text("永久删除", "Delete permanently"))
                        }
                    }
                }
            }
        }
    }
    if (trashClearConfirm) {
        val trashedCount = pages.count { it.trashed }
        AlertDialog(
            onDismissRequest = { trashClearConfirm = false },
            title = { Text(text("清空回收站？", "Empty trash?")) },
            text = { Text(text("将永久删除回收站中的 $trashedCount 个文档及其历史记录，此操作不可撤销。", "This permanently deletes $trashedCount documents in the trash and their history. It cannot be undone.")) },
            confirmButton = {
                TextButton(onClick = {
                    trashClearConfirm = false
                    trashSelectionDialog = false
                    permanentlyDeleteTrashedPages(pages.filter { it.trashed }.map { it.id }.toSet())
                }) { Text(text("永久删除", "Delete permanently"), color = LingyeColors.Coral) }
            },
            dismissButton = { TextButton(onClick = { trashClearConfirm = false }) { Text(text("取消", "Cancel")) } }
        )
    }
    trashDeleteConfirm?.let { ids ->
        AlertDialog(
            onDismissRequest = { trashDeleteConfirm = null },
            title = { Text(text("永久删除所选文档？", "Delete selected permanently?")) },
            text = { Text(text("将永久删除 ${ids.size} 个文档及其历史记录，此操作不可撤销。", "This permanently deletes ${ids.size} documents and their history. It cannot be undone.")) },
            confirmButton = {
                TextButton(onClick = {
                    trashDeleteConfirm = null
                    if (permanentlyDeleteTrashedPages(ids)) {
                        trashSelectedIds = emptySet()
                        if (pages.none { it.trashed }) trashSelectionDialog = false
                    }
                }) { Text(text("永久删除", "Delete permanently"), color = LingyeColors.Coral) }
            },
            dismissButton = { TextButton(onClick = { trashDeleteConfirm = null }) { Text(text("取消", "Cancel")) } }
        )
    }
    pageRenameTarget?.let { id ->
        val target = pages.firstOrNull { it.id == id }
        if (target != null) {
            AlertDialog(
                onDismissRequest = { pageRenameTarget = null },
                title = { Text(text("重命名文档", "Rename document")) },
                text = {
                    OutlinedTextField(
                        pageRenameDraft,
                        { pageRenameDraft = it.take(100) },
                        label = { Text(text("文档名称", "Document name")) },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = pageRenameDraft.trim().isNotEmpty(),
                        onClick = {
                            if (!saveCurrent()) return@TextButton
                            val previous = target.title
                            target.title = pageRenameDraft.trim()
                            try {
                                repository.save(target)
                                pageRenameTarget = null
                                revision++
                                savedRevision = revision
                                notice = text("文档已重命名", "Document renamed")
                            } catch (e: Exception) {
                                target.title = previous
                                error = failure("重命名失败", "Rename failed", e)
                            }
                        }
                    ) { Text(text("保存", "Save")) }
                },
                dismissButton = { TextButton(onClick = { pageRenameTarget = null }) { Text(text("取消", "Cancel")) } }
            )
        }
    }
    pageInfoTarget?.let { id ->
        val target = pages.firstOrNull { it.id == id }
        if (target != null) {
            val characters = target.text.length + target.blocks.sumOf { it.content.length }
            val backlinks = PageLinks.backlinks(target, pages)
            AlertDialog(
                onDismissRequest = { pageInfoTarget = null },
                title = { Text(text("文档信息", "Document info")) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PageGlyph(target, 22.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(target.title.ifBlank { text("未命名页面", "Untitled page") }, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(text("字符", "Characters") + " · $characters", color = LingyeColors.Muted)
                        Text(text("文字块", "Text blocks") + " · ${target.blocks.size}", color = LingyeColors.Muted)
                        Text(text("图片", "Images") + " · ${target.images.size}", color = LingyeColors.Muted)
                        Text(text("画布对象", "Canvas objects") + " · ${target.shapes.size + target.connectors.size + target.strokes.size}", color = LingyeColors.Muted)
                        Text(text("文件夹", "Folders") + " · ${target.folders.joinToString().ifBlank { text("未分类", "Unfiled") }}", color = LingyeColors.Muted)
                        Text(text("标签", "Tags") + " · ${target.tags.joinToString().ifBlank { "—" }}", color = LingyeColors.Muted)
                        Text(text("收藏", "Saved") + " · " + if (target.favorite) text("✓", "Yes") else text("✕", "No"), color = LingyeColors.Muted)
                        Text(text("反向链接", "Backlinks") + " · ${backlinks.size}", color = LingyeColors.Muted)
                        backlinks.take(6).forEach { source ->
                            Text(
                                source.title.ifBlank { text("未命名页面", "Untitled page") },
                                color = LingyeColors.Primary,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable {
                                    pageInfoTarget = null
                                    openLinkedPage(source.id)
                                }
                            )
                        }
                        if (backlinks.size > 6) {
                            Text(text("还有 ${backlinks.size - 6} 个", "${backlinks.size - 6} more"), color = LingyeColors.Muted, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { pageInfoTarget = null }) { Text(text("关闭", "Close")) } }
            )
        }
    }
    linkedDocSourceId?.let { sourceId ->
        val source = pages.firstOrNull { it.id == sourceId }
        if (source != null) {
            val candidates = pages.filter { !it.trashed && it.id != source.id }
            AlertDialog(
                onDismissRequest = { linkedDocSourceId = null },
                title = { Text(text("添加链接文档", "Add linked document")) },
                text = {
                    Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                        if (candidates.isEmpty()) {
                            Text(text("没有其他可链接的页面", "No other pages to link"), color = LingyeColors.Muted)
                        } else {
                            candidates.forEach { target ->
                                Row(
                                    Modifier.fillMaxWidth().clickable {
                                        linkedDocSourceId = null
                                        addLinkedDocToPage(source, target)
                                    }.padding(vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PageGlyph(target, 20.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            target.title.ifBlank { text("未命名页面", "Untitled page") },
                                            fontSize = 14.sp,
                                            color = LingyeColors.Ink,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (target.tags.isNotEmpty()) {
                                            Text(target.tags.joinToString(" ") { "#$it" }, fontSize = 11.sp, color = LingyeColors.Muted, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { linkedDocSourceId = null }) { Text(text("关闭", "Close")) } }
            )
        }
    }
    if (importDialog) {
        ImportDialog(
            language = language,
            onDismiss = { importDialog = false },
            onSelect = { format ->
                importDialog = false
                when (format) {
                    ImportFormat.MARKDOWN -> markdownImporter.launch(arrayOf("text/markdown", "text/plain", "text/*"))
                    ImportFormat.MARKDOWN_ZIP -> {
                        importRequestKind = "markdownZip"
                        documentImporter.launch(arrayOf("application/zip", "application/octet-stream"))
                    }
                    ImportFormat.HTML -> {
                        importRequestKind = "html"
                        documentImporter.launch(arrayOf("text/html", "text/plain", "text/*", "application/octet-stream"))
                    }
                    ImportFormat.NOTION -> {
                        importRequestKind = "notion"
                        documentImporter.launch(arrayOf("application/zip", "application/octet-stream", "text/html", "text/*"))
                    }
                    ImportFormat.OBSIDIAN -> {
                        importRequestKind = "obsidian"
                        documentImporter.launch(arrayOf("application/zip", "application/octet-stream"))
                    }
                    ImportFormat.BEAR -> {
                        importRequestKind = "bear"
                        documentImporter.launch(arrayOf("application/zip", "application/octet-stream"))
                    }
                    ImportFormat.DOCX -> {
                        importRequestKind = "docx"
                        documentImporter.launch(
                            arrayOf(
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/zip",
                                "application/octet-stream"
                            )
                        )
                    }
                    ImportFormat.SNAPSHOT -> {
                        importRequestKind = "snapshot"
                        documentImporter.launch(arrayOf("application/json", "text/plain", "text/*", "application/octet-stream"))
                    }
                    ImportFormat.ONENOTE -> Unit
                }
            }
        )
    }
    if (historyVersionsDialog && current != null) {
        val historyRef = history
        val count = historyRef?.undoCount() ?: 0
        AlertDialog(
            onDismissRequest = { historyVersionsDialog = false },
            title = { Text(text("历史版本", "Version history")) },
            text = {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    if (count == 0) {
                        Text(text("当前页面还没有可恢复的历史版本。", "No restorable versions yet."), color = LingyeColors.Muted)
                    } else {
                        (count - 1 downTo 0).take(30).forEach { index ->
                            val version = historyRef?.undoVersion(index)
                            if (version != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(version.title.ifBlank { text("未命名页面", "Untitled page") }, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            text(
                                                "版本 ${index + 1} · ${version.blocks.size} 个文字块",
                                                "Version ${index + 1} · ${version.blocks.size} blocks"
                                            ),
                                            fontSize = 12.sp,
                                            color = LingyeColors.Muted
                                        )
                                    }
                                    TextButton(onClick = {
                                        val restored = historyRef?.undoTo(index)
                                        if (restored != null) {
                                            applyHistory(restored)
                                            historyVersionsDialog = false
                                            notice = text("已恢复历史版本", "Version restored")
                                        }
                                    }) { Text(text("恢复", "Restore"), color = LingyeColors.Primary) }
                                }
                                HorizontalDivider(color = LingyeColors.Line.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { historyVersionsDialog = false }) { Text(text("关闭", "Close")) } }
        )
    }
    iconTargetId?.let { id ->
        pages.firstOrNull { it.id == id }?.let { target ->
            PageIconPicker(
                page = target,
                onSelect = { icon ->
                    target.icon = icon
                    change()
                    iconTargetId = null
                },
                onDismiss = { iconTargetId = null }
            )
        }
    }
    if (createFolder) AlertDialog(onDismissRequest = { createFolder = false }, title = { Text(text("新建文件夹","New folder")) }, text = {
        Column { OutlinedTextField(folderName, { folderName = it; folderError = null }, label = { Text(text("文件夹名称","Folder name")) }, singleLine = true); folderError?.let { Text(it, color = LingyeColors.ErrorInk) } }
    }, confirmButton = { TextButton(onClick = {
        val name = folderName.trim()
        if (!FolderCatalog.valid(name)) folderError = text("请输入 1–100 个字符的名称","Enter a name with 1–100 characters")
        else if (folders.any { it.equals(name, ignoreCase = true) }) folderError = text("文件夹名称已存在","That folder name already exists")
        else try { val next = folders.toList() + name; store.saveFolders(next); folders.add(name); expandedFolders = expandedFolders + name; folderFilter = null; trashMode = false; favoriteMode = false; createFolder = false } catch (e: Exception) { folderError = "${text("保存失败","Save failed")}：${localizedError(language,e)}" }
    }) { Text(text("创建","Create")) } }, dismissButton = { TextButton(onClick = { createFolder = false }) { Text(text("取消","Cancel")) } })
    if (tagEditor && current != null) {
        val draftTags = tagDraft.split(',', '，').map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val invalidTags = draftTags.filterNot(TagCatalog::valid)
        AlertDialog(
            onDismissRequest = { tagEditor = false },
            title = { Text(text("编辑标签","Edit tags")) },
            text = {
                Column {
                    Text(text("用逗号分隔多个标签，例如：设计, 重要","Separate tags with commas, for example: design, important"), color = LingyeColors.Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tagDraft,
                        onValueChange = { tagDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text("标签","Tags")) },
                        singleLine = true
                    )
                    if (invalidTags.isNotEmpty()) Text(text("每个标签需为 1–40 个非控制字符","Each tag must contain 1–40 visible characters"), color = LingyeColors.Coral, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(
                    enabled = invalidTags.isEmpty() && draftTags.size <= 200,
                    onClick = {
                        current.tags.clear()
                        current.tags.addAll(draftTags)
                        change()
                        tagEditor = false
                    }
                ) { Text(text("保存","Save")) }
            },
            dismissButton = { TextButton(onClick = { tagEditor = false }) { Text(text("取消","Cancel")) } }
        )
    }
}
