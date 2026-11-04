package com.example.tabletworkspace
import com.example.tabletworkspace.ui.model.DocumentRowModel
import com.example.tabletworkspace.ui.model.documentRowModel
import com.example.tabletworkspace.core.formatModified

import android.graphics.BlurMaskFilter
import android.graphics.Paint as AndroidPaint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private enum class AllDocsLayout { LIST, CARDS }
private enum class AllDocsSort { UPDATED, TITLE }


@Composable
fun AllDocumentsHome(
    pages: List<Page>,
    modifiedTimes: Map<String, Long>,
    language: AppLanguage,
    showSidebarToggle: Boolean,
    onToggleSidebar: () -> Unit,
    onOpenPage: (Page) -> Unit,
    onToggleFavorite: (Page) -> Unit,
    onCreatePage: (Boolean) -> Unit,
    folders: List<String>,
    onMovePageToFolder: (Page, String?) -> Unit,
    onTrashPages: (Set<String>) -> Boolean,
    contentRevision: Int,
    favoritesOnly: Boolean = false,
    startInSelectionMode: Boolean = false,
    initialSelectedPageId: String? = null,
    onInitialSelectionConsumed: () -> Unit = {}
) {
    var layout by remember { mutableStateOf(AllDocsLayout.LIST) }
    var sort by remember { mutableStateOf(AllDocsSort.UPDATED) }
    var showMenu by remember { mutableStateOf(false) }
    var createMenu by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(startInSelectionMode) }
    var selectedIds by remember { mutableStateOf(initialSelectedPageId?.let { setOf(it) } ?: emptySet<String>()) }
    var pendingTrashIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(Unit) {
        if (startInSelectionMode || initialSelectedPageId != null) onInitialSelectionConsumed()
    }
    val visible = remember(pages, modifiedTimes, sort, favoritesOnly, contentRevision) {
        pages.filterNot { it.trashed }
            .filter { !favoritesOnly || it.favorite }
            .sortedWith(
                when (sort) {
                    AllDocsSort.UPDATED -> compareByDescending<Page> { modifiedTimes[it.id] ?: 0L }.thenBy { it.title.lowercase() }
                    AllDocsSort.TITLE -> compareBy<Page> { it.title.lowercase() }
                }
            )
    }
    val visibleIds = remember(visible) { visible.mapTo(linkedSetOf()) { it.id } }
    val rowModels = remember(visible, modifiedTimes, contentRevision) {
        visible.associate { page -> page.id to documentRowModel(page, modifiedTimes[page.id]) }
    }
    BackHandler(enabled = selectionMode) {
        selectionMode = false
        selectedIds = emptySet()
    }

    if (favoritesOnly && visible.isEmpty()) {
        FavoriteEmptyState(
            language = language,
            showSidebarToggle = showSidebarToggle,
            onToggleSidebar = onToggleSidebar,
            onCreateFavorite = { onCreatePage(false) }
        )
        return
    }

    Column(Modifier.fillMaxSize().padding(start = 28.dp, end = 28.dp, top = 20.dp, bottom = 14.dp)) {
        val viewModeToggle: @Composable () -> Unit = {
            AnimatedViewModeToggle(language, layout) { layout = it }
        }
        val selectAction: @Composable () -> Unit = {
            ViewModeButton(AppIcon.SELECT, tr(language, "多选", "Select multiple"), {
                selectionMode = true
                selectedIds = emptySet()
            }, false)
        }
        val sortAction: @Composable () -> Unit = {
            Box {
                Surface(onClick = { showMenu = true }, shape = RoundedCornerShape(10.dp), color = LingyeColors.SurfaceHigh, border = androidx.compose.foundation.BorderStroke(1.dp, LingyeColors.Line)) {
                    Text(tr(language, "排序", "Sort"), Modifier.padding(horizontal = 15.dp, vertical = 9.dp), fontSize = 13.sp, color = LingyeColors.Ink)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.width(180.dp), shape = RoundedCornerShape(12.dp), containerColor = LingyeColors.SurfaceHigh) {
                    MenuRow(tr(language, "最近修改", "Last updated"), { sort = AllDocsSort.UPDATED; showMenu = false }, checked = sort == AllDocsSort.UPDATED)
                    MenuRow(tr(language, "标题", "Title"), { sort = AllDocsSort.TITLE; showMenu = false }, checked = sort == AllDocsSort.TITLE)
                }
            }
        }
        val createAction: @Composable () -> Unit = {
            Box {
                Surface(onClick = { createMenu = true }, shape = RoundedCornerShape(10.dp), color = LingyeColors.SurfaceHigh, border = androidx.compose.foundation.BorderStroke(1.dp, LingyeColors.Line)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (favoritesOnly) tr(language, "添加收藏", "Add favorite") else tr(language, "新建文档", "New document"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = LingyeColors.Ink
                        )
                        Spacer(Modifier.width(10.dp))
                        DropdownChevron(createMenu, Modifier.size(14.dp))
                    }
                }
                DropdownMenu(expanded = createMenu, onDismissRequest = { createMenu = false }, shape = RoundedCornerShape(12.dp), containerColor = LingyeColors.SurfaceHigh) {
                    MenuRow(
                        tr(language, "创建新的页面", "New document"),
                        { createMenu = false; onCreatePage(false) },
                        icon = AppIcon.DOCUMENT,
                        subtitle = tr(language, "在空白页面书写", "Start with a blank page")
                    )
                    MenuRow(
                        tr(language, "新的无界页面", "New whiteboard"),
                        { createMenu = false; onCreatePage(true) },
                        icon = AppIcon.CANVAS,
                        subtitle = tr(language, "在空白白板画画", "Start with a blank canvas")
                    )
                }
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val compactHeader = maxWidth < 560.dp
            Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (showSidebarToggle) {
                ViewModeButton(AppIcon.SIDEBAR, tr(language, "展开侧栏", "Expand sidebar"), onToggleSidebar, false)
                Spacer(Modifier.width(10.dp))
            }
            if (selectionMode) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${selectedIds.size} " + tr(language, "项已选择", "selected"),
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = LingyeColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        tr(language, "点按文档继续选择", "Tap documents to change selection"),
                        fontSize = 12.sp,
                        color = LingyeColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TextButton(onClick = { selectedIds = if (selectedIds.size == visibleIds.size) emptySet() else visibleIds }) {
                    Text(tr(language, "全选", "Select all"), color = LingyeColors.Primary)
                }
                Spacer(Modifier.width(6.dp))
                Surface(
                    onClick = { if (selectedIds.isNotEmpty()) pendingTrashIds = selectedIds },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedIds.isEmpty()) LingyeColors.Subtle else LingyeColors.DangerSoft
                ) {
                    Row(Modifier.padding(horizontal = 13.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        WorkspaceIcon(AppIcon.TRASH, null, Modifier.size(18.dp), if (selectedIds.isEmpty()) LingyeColors.Muted else LingyeColors.Coral)
                        Spacer(Modifier.width(7.dp))
                        Text(tr(language, "移入回收站", "Move to trash"), fontSize = 13.sp, color = if (selectedIds.isEmpty()) LingyeColors.Muted else LingyeColors.Coral)
                    }
                }
                Spacer(Modifier.width(8.dp))
                ViewModeButton(AppIcon.CLOSE, tr(language, "退出多选", "Exit selection"), {
                    selectionMode = false
                    selectedIds = emptySet()
                }, false)
            } else {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (favoritesOnly) tr(language, "收藏", "Favorites") else tr(language, "全部文档", "All documents"),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = LingyeColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (favoritesOnly) "${visible.size} " + tr(language, "个收藏", "saved")
                        else "${visible.size} " + tr(language, "个文档", "documents"),
                        fontSize = 12.sp,
                        color = LingyeColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (compactHeader) {
                    selectAction()
                } else {
                    viewModeToggle()
                    Spacer(Modifier.width(8.dp))
                    selectAction()
                    Spacer(Modifier.width(10.dp))
                    sortAction()
                    Spacer(Modifier.width(8.dp))
                    createAction()
                }
            }
        }
                if (!selectionMode && compactHeader) {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        viewModeToggle()
                        Spacer(Modifier.width(8.dp))
                        sortAction()
                        Spacer(Modifier.width(8.dp))
                        createAction()
                    }
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        if (visible.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(tr(language, "还没有文档", "No documents yet"), color = LingyeColors.Muted)
            }
        } else if (layout == AllDocsLayout.LIST) {
            DocumentList(
                visible,
                rowModels,
                language,
                folders,
                selectionMode,
                selectedIds,
                onOpenPage,
                onToggleFavorite,
                onMovePageToFolder,
                onToggleSelection = { page ->
                    selectedIds = if (page.id in selectedIds) selectedIds - page.id else selectedIds + page.id
                },
                onTrashPage = { page -> pendingTrashIds = setOf(page.id) }
            )
        } else {
            DocumentCards(
                visible,
                rowModels,
                language,
                selectionMode,
                selectedIds,
                onOpenPage,
                onToggleFavorite,
                onToggleSelection = { page ->
                    selectedIds = if (page.id in selectedIds) selectedIds - page.id else selectedIds + page.id
                }
            )
        }
        if (pendingTrashIds.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { pendingTrashIds = emptySet() },
                title = { Text(tr(language, "移入回收站？", "Move to trash?")) },
                text = {
                    Text(
                        "${pendingTrashIds.size} " + tr(language, "个文档将移入回收站，页面文件会保留。", "documents will move to trash. Page data is retained.")
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val ids = pendingTrashIds
                        if (onTrashPages(ids)) {
                            pendingTrashIds = emptySet()
                            selectedIds = selectedIds - ids
                            if (selectedIds.isEmpty()) selectionMode = false
                        }
                    }) { Text(tr(language, "移入回收站", "Move to trash"), color = LingyeColors.Coral) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingTrashIds = emptySet() }) { Text(tr(language, "取消", "Cancel")) }
                }
            )
        }
    }
}

@Composable
private fun FavoriteEmptyState(
    language: AppLanguage,
    showSidebarToggle: Boolean,
    onToggleSidebar: () -> Unit,
    onCreateFavorite: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FavoriteCardsIllustration(Modifier.size(width = 520.dp, height = 250.dp))
            Spacer(Modifier.height(22.dp))
            Text(
                tr(language, "收藏管理", "Your favorites"),
                fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold,
                color = LingyeColors.Ink
            )
            Spacer(Modifier.height(9.dp))
            Text(
                tr(language, "在这里添加您的第一个收藏。", "Add your first favorite here."),
                fontSize = 15.sp,
                color = LingyeColors.Muted
            )
            Spacer(Modifier.height(24.dp))
            Surface(
                onClick = onCreateFavorite,
                shape = RoundedCornerShape(12.dp),
                color = LingyeColors.SurfaceHigh,
                border = androidx.compose.foundation.BorderStroke(1.dp, LingyeColors.Line),
                shadowElevation = 0.dp
            ) {
                Row(
                    Modifier.padding(horizontal = 20.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    WorkspaceIcon(AppIcon.PAGES, null, Modifier.size(18.dp), LingyeColors.Muted)
                    Spacer(Modifier.width(9.dp))
                    Text(
                        tr(language, "添加收藏", "Add favorite"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = LingyeColors.Ink
                    )
                }
            }
        }
        if (showSidebarToggle) {
            Box(Modifier.padding(start = 20.dp, top = 16.dp)) {
                ViewModeButton(AppIcon.SIDEBAR, tr(language, "展开侧栏", "Expand sidebar"), onToggleSidebar, false)
            }
        }
    }
}

@Composable
private fun FavoriteCardsIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val dotColor = LingyeColors.GridDot
        val dotRadius = 1.35.dp.toPx()
        val spacing = 23.dp.toPx()
        val blurRadius = 7.dp.toPx()
        val shadowPaint = AndroidPaint().apply {
            isAntiAlias = true
            color = android.graphics.Color.BLACK
            alpha = 22
            maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        }
        val gridLeft = size.width * 0.18f
        val gridTop = size.height * 0.10f
        val gridWidth = size.width * 0.64f
        val gridHeight = size.height * 0.76f
        var x = gridLeft
        while (x <= gridLeft + gridWidth) {
            var y = gridTop
            while (y <= gridTop + gridHeight) {
                drawCircle(dotColor, dotRadius, Offset(x, y))
                y += spacing
            }
            x += spacing
        }

        val leftPivot = Offset(size.width * 0.40f, size.height * 0.55f)
        withTransform({ rotate(-11f, leftPivot) }) {
            val left = size.width * 0.19f
            val top = size.height * 0.34f
            val cardWidth = size.width * 0.29f
            val cardHeight = size.height * 0.36f
            val corner = 18.dp.toPx()
            drawIntoCanvas {
                it.nativeCanvas.drawRoundRect(
                    left + 4.dp.toPx(),
                    top + 7.dp.toPx(),
                    left + cardWidth + 4.dp.toPx(),
                    top + cardHeight + 7.dp.toPx(),
                    corner,
                    corner,
                    shadowPaint
                )
            }
            drawRoundRect(
                color = LingyeColors.Surface,
                topLeft = Offset(left, top),
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(corner)
            )
            val stripLeft = size.width * 0.24f
            val stripTop = size.height * 0.23f
            val stripWidth = size.width * 0.19f
            val stripHeight = 9.dp.toPx()
            drawIntoCanvas {
                it.nativeCanvas.drawRoundRect(
                    stripLeft + 3.dp.toPx(),
                    stripTop + 4.dp.toPx(),
                    stripLeft + stripWidth + 3.dp.toPx(),
                    stripTop + stripHeight + 4.dp.toPx(),
                    5.dp.toPx(),
                    5.dp.toPx(),
                    shadowPaint
                )
            }
            drawRoundRect(
                color = LingyeColors.Surface,
                topLeft = Offset(stripLeft, stripTop),
                size = Size(stripWidth, stripHeight),
                cornerRadius = CornerRadius(5.dp.toPx())
            )
        }

        val rightPivot = Offset(size.width * 0.68f, size.height * 0.59f)
        withTransform({ rotate(-5f, rightPivot) }) {
            val left = size.width * 0.48f
            val top = size.height * 0.39f
            val cardWidth = size.width * 0.34f
            val cardHeight = size.height * 0.38f
            val corner = 19.dp.toPx()
            drawIntoCanvas {
                it.nativeCanvas.drawRoundRect(
                    left + 5.dp.toPx(),
                    top + 8.dp.toPx(),
                    left + cardWidth + 5.dp.toPx(),
                    top + cardHeight + 8.dp.toPx(),
                    corner,
                    corner,
                    shadowPaint
                )
            }
            drawRoundRect(
                color = LingyeColors.Surface,
                topLeft = Offset(left, top),
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(corner)
            )
            val stripLeft = size.width * 0.50f
            val stripTop = size.height * 0.28f
            val stripWidth = size.width * 0.22f
            val stripHeight = 9.dp.toPx()
            drawIntoCanvas {
                it.nativeCanvas.drawRoundRect(
                    stripLeft + 3.dp.toPx(),
                    stripTop + 4.dp.toPx(),
                    stripLeft + stripWidth + 3.dp.toPx(),
                    stripTop + stripHeight + 4.dp.toPx(),
                    5.dp.toPx(),
                    5.dp.toPx(),
                    shadowPaint
                )
            }
            drawRoundRect(
                color = LingyeColors.Surface,
                topLeft = Offset(stripLeft, stripTop),
                size = Size(stripWidth, stripHeight),
                cornerRadius = CornerRadius(5.dp.toPx())
            )
        }
    }
}

@Composable
private fun DropdownChevron(expanded: Boolean, modifier: Modifier = Modifier) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 270f else 90f,
        animationSpec = tween(160),
        label = "all-documents-create-chevron"
    )
    Canvas(modifier) {
        val pivot = center
        val path = Path().apply {
            moveTo(size.width * 0.38f, size.height * 0.27f)
            lineTo(size.width * 0.62f, size.height * 0.5f)
            lineTo(size.width * 0.38f, size.height * 0.73f)
        }
        withTransform({ rotate(rotation, pivot) }) {
            drawPath(path, LingyeColors.Muted, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun ViewModeButton(
    icon: AppIcon,
    description: String,
    onClick: () -> Unit,
    selected: Boolean,
    unselectedColor: Color = LingyeColors.Ink
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(34.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) LingyeColors.SegmentSelected else Color.Transparent
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            WorkspaceIcon(
                icon,
                description,
                Modifier.size(18.dp),
                if (selected) LingyeColors.OnSegmentSelected else unselectedColor
            )
        }
    }
}

@Composable
private fun AnimatedViewModeToggle(
    language: AppLanguage,
    selected: AllDocsLayout,
    onSelect: (AllDocsLayout) -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val density = LocalDensity.current
    val rowPadding = 2.dp
    val rowPaddingPx = with(density) { rowPadding.toPx() }
    val itemBounds = remember { mutableStateMapOf<AllDocsLayout, Rect>() }
    val indicatorLeft = remember { Animatable(0f) }
    val selectedBounds = itemBounds[selected]
    val targetLeft = selectedBounds?.left?.plus(rowPaddingPx)
    val shownLeft = if (indicatorLeft.value <= 0f) targetLeft ?: 0f else indicatorLeft.value

    LaunchedEffect(selectedBounds) {
        val target = selectedBounds ?: return@LaunchedEffect
        val newLeft = target.left + rowPaddingPx
        if (indicatorLeft.value <= 0f) indicatorLeft.snapTo(newLeft)
        else indicatorLeft.animateTo(newLeft, tween(220, easing = FastOutSlowInEasing))
    }

    Surface(
        shape = shape,
        color = LingyeColors.SurfaceHigh,
        border = androidx.compose.foundation.BorderStroke(1.dp, LingyeColors.SegmentBorder),
        shadowElevation = 3.dp
    ) {
        Box {
            if (shownLeft > 0f) {
                Box(
                    Modifier
                        .offset { IntOffset(shownLeft.roundToInt(), rowPaddingPx.roundToInt()) }
                        .size(34.dp)
                        .background(LingyeColors.SegmentSelected, RoundedCornerShape(10.dp))
                )
            }
            Row(Modifier.padding(rowPadding), verticalAlignment = Alignment.CenterVertically) {
                ViewModeSlot(
                    value = AllDocsLayout.CARDS,
                    icon = AppIcon.OVERVIEW,
                    description = tr(language, "卡片视图", "Card view"),
                    itemBounds = itemBounds,
                    indicatorLeft = shownLeft,
                    onClick = { onSelect(AllDocsLayout.CARDS) }
                )
                ViewModeSlot(
                    value = AllDocsLayout.LIST,
                    icon = AppIcon.PAGES,
                    description = tr(language, "列表视图", "List view"),
                    itemBounds = itemBounds,
                    indicatorLeft = shownLeft,
                    onClick = { onSelect(AllDocsLayout.LIST) }
                )
            }
        }
    }
}

@Composable
private fun ViewModeSlot(
    value: AllDocsLayout,
    icon: AppIcon,
    description: String,
    itemBounds: MutableMap<AllDocsLayout, Rect>,
    indicatorLeft: Float,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val bounds = itemBounds[value]
    val indicatorCenter = indicatorLeft + with(density) { 17.dp.toPx() }
    val covered = bounds != null && indicatorLeft > 0f &&
        indicatorCenter >= bounds.left && indicatorCenter <= bounds.right
    Box(Modifier.onGloballyPositioned { itemBounds[value] = it.boundsInParent() }) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(34.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color.Transparent
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                WorkspaceIcon(icon, description, Modifier.size(18.dp), if (covered) LingyeColors.OnSegmentSelected else LingyeColors.Ink)
            }
        }
    }
}

@Composable
private fun DocumentList(
    pages: List<Page>,
    models: Map<String, DocumentRowModel>,
    language: AppLanguage,
    folders: List<String>,
    selectionMode: Boolean,
    selectedIds: Set<String>,
    onOpenPage: (Page) -> Unit,
    onToggleFavorite: (Page) -> Unit,
    onMovePageToFolder: (Page, String?) -> Unit,
    onToggleSelection: (Page) -> Unit,
    onTrashPage: (Page) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        items(pages, key = { it.id }, contentType = { "document-row" }) { page ->
            val model = models[page.id] ?: documentRowModel(page, null)
            SwipeableDocumentRow(
                page = page,
                model = model,
                language = language,
                folders = folders,
                selectionMode = selectionMode,
                selected = page.id in selectedIds,
                onOpenPage = onOpenPage,
                onToggleFavorite = onToggleFavorite,
                onMovePageToFolder = onMovePageToFolder,
                onToggleSelection = onToggleSelection,
                onTrashPage = onTrashPage
            )
        }
    }
}

@Composable
private fun DocumentCards(
    pages: List<Page>,
    models: Map<String, DocumentRowModel>,
    language: AppLanguage,
    selectionMode: Boolean,
    selectedIds: Set<String>,
    onOpenPage: (Page) -> Unit,
    onToggleFavorite: (Page) -> Unit,
    onToggleSelection: (Page) -> Unit
) {
    LazyVerticalGrid(columns = GridCells.Adaptive(280.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        gridItems(pages, key = { it.id }, contentType = { "document-card" }) { page ->
            val model = models[page.id] ?: documentRowModel(page, null)
            val isSelected = page.id in selectedIds
            Surface(
                onClick = { if (selectionMode) onToggleSelection(page) else onOpenPage(page) },
                modifier = Modifier.fillMaxWidth().height(222.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) LingyeColors.PrimarySoft else LingyeColors.SurfaceHigh,
                border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) LingyeColors.Primary else LingyeColors.Line),
                shadowElevation = 1.dp
            ) {
                Column(Modifier.fillMaxSize().padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SelectionIndicator(
                            visible = selectionMode,
                            selected = isSelected,
                            description = if (isSelected) tr(language, "已选择", "Selected") else tr(language, "未选择", "Not selected"),
                            glyphIcon = model.icon,
                            glyphCanvas = model.canvasIcon
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(model.title.ifBlank { tr(language, "未命名文档", "Untitled") }, Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (!selectionMode) FavoriteAction(model.favorite, language) { onToggleFavorite(page) }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        model.preview,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = LingyeColors.Ink,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.weight(1f))
                    Text(formatModified(model.modifiedTime, language), fontSize = 11.sp, color = LingyeColors.Muted)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableDocumentRow(
    page: Page,
    model: DocumentRowModel,
    language: AppLanguage,
    folders: List<String>,
    selectionMode: Boolean,
    selected: Boolean,
    onOpenPage: (Page) -> Unit,
    onToggleFavorite: (Page) -> Unit,
    onMovePageToFolder: (Page, String?) -> Unit,
    onToggleSelection: (Page) -> Unit,
    onTrashPage: (Page) -> Unit
) {
    var folderMenu by remember(page.id) { mutableStateOf(false) }

    if (selectionMode) {
        Surface(
            onClick = { onToggleSelection(page) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = if (selected) LingyeColors.PrimarySoft else LingyeColors.SurfaceHigh
        ) {
            DocumentRowContent(page, model, language, true, selected, onToggleFavorite)
        }
    } else {
        SwipeActionRow(
            key = page.id,
            revealWidth = 204.dp,
            actionVerticalOffset = 11.dp,
            actions = {
                        SwipeCircleAction(AppIcon.DOCUMENT, tr(language, "打开", "Open"), Color(0xff52b788)) {
                            onOpenPage(page)
                        }
                        Box {
                            SwipeCircleAction(AppIcon.FOLDER, tr(language, "移动到", "Move to"), Color(0xff5ca7df)) {
                                folderMenu = true
                            }
                            DropdownMenu(expanded = folderMenu, onDismissRequest = { folderMenu = false }, containerColor = LingyeColors.SurfaceHigh) {
                                MenuRow(tr(language, "未分类", "Unfiled"), {
                                    folderMenu = false
                                    onMovePageToFolder(page, null)
                                })
                                folders.forEach { folder ->
                                    MenuRow(folder, {
                                        folderMenu = false
                                        onMovePageToFolder(page, folder)
                                    }, checked = folder in model.folders)
                                }
                            }
                        }
                        SwipeCircleAction(
                            if (model.favorite) AppIcon.FAVORITED else AppIcon.FAVORITE,
                            if (model.favorite) tr(language, "取消收藏", "Remove favorite") else tr(language, "收藏", "Favorite"),
                            Color(0xffd4a72c)
                        ) {
                            onToggleFavorite(page)
                        }
                        SwipeCircleAction(AppIcon.TRASH, tr(language, "删除", "Delete"), Color(0xffdb5a4f)) {
                            onTrashPage(page)
                        }
            },
            content = { revealed ->
                Surface(
                    onClick = { if (!revealed()) onOpenPage(page) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) LingyeColors.PrimarySoft else LingyeColors.SurfaceHigh
                ) {
                    DocumentRowContent(page, model, language, false, selected, onToggleFavorite)
                }
            }
        )
    }
}

@Composable
private fun DocumentRowContent(
    page: Page,
    model: DocumentRowModel,
    language: AppLanguage,
    selectionMode: Boolean,
    selected: Boolean,
    onToggleFavorite: (Page) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 62.dp).padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SelectionIndicator(
            visible = selectionMode,
            selected = selected,
            description = if (selected) tr(language, "已选择", "Selected") else tr(language, "未选择", "Not selected"),
            glyphIcon = model.icon,
            glyphCanvas = model.canvasIcon
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                model.title.ifBlank { tr(language, "未命名文档", "Untitled") },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (model.preview.isNotBlank()) {
                Text(model.preview, fontSize = 12.sp, color = LingyeColors.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Text(formatModified(model.modifiedTime, language), fontSize = 11.sp, color = LingyeColors.Muted)
        if (!selectionMode) {
            Spacer(Modifier.width(12.dp))
            FavoriteAction(model.favorite, language) { onToggleFavorite(page) }
        }
    }
}

@Composable
private fun SelectionIndicator(
    visible: Boolean,
    selected: Boolean,
    description: String,
    glyphIcon: String,
    glyphCanvas: Boolean
) {
    if (!visible) {
        PageGlyph(glyphIcon, glyphCanvas, 20.dp)
        return
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (selected) LingyeColors.Primary else LingyeColors.SurfaceHigh,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) LingyeColors.Primary else LingyeColors.Line),
        modifier = Modifier.size(20.dp).semantics { contentDescription = description }
    ) {
        if (selected) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SwipeCircleAction(icon: AppIcon, description: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(color, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        WorkspaceIcon(icon, null, Modifier.size(20.dp), Color.White)
    }
}

@Composable
private fun FavoriteAction(favorite: Boolean, language: AppLanguage, onClick: () -> Unit) {
    Box(
        Modifier
            .size(34.dp)
            .background(if (favorite) LingyeColors.PrimarySoft else Color.Transparent, RoundedCornerShape(50))
            .clickable { onClick() }
            .semantics {
                contentDescription = if (favorite) tr(language, "取消收藏", "Remove from saved") else tr(language, "加入收藏", "Add to saved")
            },
        contentAlignment = Alignment.Center
    ) {
        WorkspaceIcon(
            if (favorite) AppIcon.FAVORITED else AppIcon.FAVORITE,
            null,
            Modifier.size(18.dp),
            if (favorite) LingyeColors.Primary else LingyeColors.Muted
        )
    }
}

