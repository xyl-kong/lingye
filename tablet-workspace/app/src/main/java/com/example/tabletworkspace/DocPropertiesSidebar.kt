package com.example.tabletworkspace

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Right-hand document properties sidebar mirroring AFFiNE's info panel.
 * It manages property definitions/visibility and hosts the former page "more" actions.
 */
@Composable
fun DocPropertiesSidebar(
    language: AppLanguage,
    pageTitle: String,
    properties: List<DocPropertyInfo>,
    onPropertiesChange: (List<DocPropertyInfo>) -> Unit,
    folders: List<String>,
    onClose: () -> Unit,
    onRename: () -> Unit,
    onChangeIcon: () -> Unit,
    onEditTags: () -> Unit,
    onOverview: () -> Unit,
    onMultiSelect: () -> Unit,
    onUndo: (() -> Unit)?,
    onRedo: (() -> Unit)?,
    onExport: () -> Unit,
    onTrash: () -> Unit,
    onMoveToFolder: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var propertiesOpen by remember { mutableStateOf(true) }
    var addOpen by remember { mutableStateOf(true) }
    var actionsOpen by remember { mutableStateOf(true) }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragDelta by remember { mutableFloatStateOf(0f) }
    var dragStartCenter by remember { mutableFloatStateOf(0f) }
    var dropTargetId by remember { mutableStateOf<String?>(null) }
    var dropAfter by remember { mutableStateOf(false) }
    val rowCenters = remember { mutableMapOf<String, Float>() }
    val ordered = properties.sortedBy { it.order }

    fun finishDrag(commit: Boolean) {
        val id = draggingId
        val target = dropTargetId
        if (commit && id != null && target != null) {
            onPropertiesChange(DocPropertyCatalog.reorder(properties, id, target, dropAfter))
        }
        draggingId = null
        dragDelta = 0f
        dragStartCenter = 0f
        dropTargetId = null
        dropAfter = false
    }

    Surface(
        modifier
            .width(320.dp)
            .fillMaxHeight()
            .drawBehind {
                drawLine(
                    LingyeColors.Line,
                    Offset(0f, 0f),
                    Offset(0f, size.height),
                    1.dp.toPx()
                )
            },
        color = LingyeColors.SurfaceHigh,
        shadowElevation = 10.dp
    ) {
        Column(Modifier.fillMaxHeight()) {
            Row(
                Modifier.fillMaxWidth().height(56.dp).padding(start = 18.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    pageTitle.ifBlank { tr(language, "未命名页面", "Untitled page") },
                    Modifier.weight(1f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LingyeColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                CompactIconAction(AppIcon.CLOSE, tr(language, "关闭属性栏", "Close properties"), onClose, LingyeColors.Muted)
            }
            HorizontalDivider(color = LingyeColors.Line.copy(alpha = 0.85f))
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                SectionHeader(tr(language, "属性", "Properties"), propertiesOpen) { propertiesOpen = !propertiesOpen }
                if (propertiesOpen) {
                    ordered.forEach { property ->
                        PropertyRow(
                            language = language,
                            property = property,
                            properties = properties,
                            onPropertiesChange = onPropertiesChange,
                            dragging = draggingId == property.id,
                            dragOffset = if (draggingId == property.id) dragDelta else 0f,
                            dropBefore = draggingId != null && dropTargetId == property.id && !dropAfter,
                            dropAfter = draggingId != null && dropTargetId == property.id && dropAfter,
                            onBounds = { center -> rowCenters[property.id] = center },
                            onDragStart = {
                                draggingId = property.id
                                dragDelta = 0f
                                dragStartCenter = rowCenters[property.id] ?: 0f
                                dropTargetId = null
                                dropAfter = false
                            },
                            onDrag = { delta ->
                                dragDelta += delta
                                val center = dragStartCenter + dragDelta
                                val others = ordered.filterNot { it.id == draggingId }
                                if (others.isEmpty()) {
                                    dropTargetId = null
                                } else {
                                    val nearest = others.minByOrNull {
                                        abs((rowCenters[it.id] ?: center) - center)
                                    } ?: others.first()
                                    dropTargetId = nearest.id
                                    dropAfter = center > (rowCenters[nearest.id] ?: center)
                                }
                            },
                            onDragEnd = { finishDrag(true) },
                            onDragCancel = { finishDrag(false) }
                        )
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = LingyeColors.Line.copy(alpha = 0.8f))
                SectionHeader(tr(language, "添加更多属性", "Add more properties"), addOpen) { addOpen = !addOpen }
                if (addOpen) {
                    DocPropertyCatalog.types.forEach { definition ->
                        AddPropertyRow(language, definition, properties) {
                            val baseName = DocPropertyCatalog.name(definition, language)
                            val nextOrder = (properties.maxOfOrNull { it.order } ?: -1) + 1
                            onPropertiesChange(
                                properties + DocPropertyInfo(
                                    id = "prop-${definition.key}-${UUID.randomUUID()}",
                                    type = definition.key,
                                    name = DocPropertyStore.uniqueName(baseName, properties),
                                    show = definition.defaultShow ?: DocPropertyCatalog.ALWAYS_SHOW,
                                    order = nextOrder
                                )
                            )
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = LingyeColors.Line.copy(alpha = 0.8f))
                SectionHeader(tr(language, "页面操作", "Page actions"), actionsOpen) { actionsOpen = !actionsOpen }
                if (actionsOpen) {
                    ActionRow(AppIcon.EDIT, tr(language, "重命名", "Rename"), onRename)
                    ActionRow(AppIcon.SPARK, tr(language, "更改图标", "Change icon"), onChangeIcon)
                    ActionRow(AppIcon.TAG, tr(language, "编辑标签", "Edit tags"), onEditTags)
                    ActionRow(AppIcon.OVERVIEW, tr(language, "查看概览", "View overview"), onOverview)
                    ActionRow(AppIcon.SELECT, tr(language, "多选", "Select multiple"), onMultiSelect)
                    onUndo?.let { ActionRow(AppIcon.UNDO, tr(language, "撤销", "Undo"), it) }
                    onRedo?.let { ActionRow(AppIcon.REDO, tr(language, "重做", "Redo"), it) }
                    MoveToFolderRow(language, folders, onMoveToFolder)
                    ActionRow(AppIcon.SAVE, tr(language, "导出 Markdown", "Export Markdown"), onExport)
                    ActionRow(AppIcon.TRASH, tr(language, "移入回收站", "Move to trash"), onTrash, danger = true)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(42.dp).clickable(onClick = onToggle).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LingyeColors.Ink)
        Text(if (expanded) "▾" else "▸", fontSize = 13.sp, color = LingyeColors.Muted)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PropertyRow(
    language: AppLanguage,
    property: DocPropertyInfo,
    properties: List<DocPropertyInfo>,
    onPropertiesChange: (List<DocPropertyInfo>) -> Unit,
    dragging: Boolean = false,
    dragOffset: Float = 0f,
    dropBefore: Boolean = false,
    dropAfter: Boolean = false,
    onBounds: (Float) -> Unit = {},
    onDragStart: () -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {}
) {
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var nameDraft by remember(property.id) { mutableStateOf(property.name) }
    val focusManager = LocalFocusManager.current
    val displayName = if (property.id.startsWith("builtin-")) {
        DocPropertyCatalog.type(property.type)?.let { DocPropertyCatalog.name(it, language) } ?: property.name
    } else property.name

    fun update(copy: (DocPropertyInfo) -> DocPropertyInfo) {
        onPropertiesChange(properties.map { if (it.id == property.id) copy(it) else it })
    }

    fun commitName() {
        val trimmed = nameDraft.trim().take(100)
        if (trimmed.isNotEmpty() && trimmed != property.name) {
            nameDraft = trimmed
            update { it.copy(name = trimmed) }
        } else if (trimmed.isEmpty()) {
            nameDraft = property.name
        }
    }

    Box(
        Modifier.fillMaxWidth()
            .offset { IntOffset(0, dragOffset.roundToInt()) }
            .zIndex(if (dragging) 2f else 0f)
            .shadow(if (dragging) 10.dp else 0.dp, RoundedCornerShape(8.dp))
            .background(if (dragging) LingyeColors.SurfaceHigh else Color.Transparent, RoundedCornerShape(8.dp))
            .drawBehind {
                if (dropBefore) {
                    drawLine(LingyeColors.Primary, Offset(0f, 0f), Offset(size.width, 0f), 2.dp.toPx())
                }
                if (dropAfter) {
                    drawLine(LingyeColors.Primary, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx())
                }
            }
            .onGloballyPositioned { onBounds(it.boundsInWindow().center.y) }
    ) {
        Row(
            Modifier.fillMaxWidth().height(40.dp)
                .clickable { nameDraft = property.name; menuOpen = true }
                .padding(start = 8.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DragHandle(
                description = tr(language, "拖动排序", "Drag to reorder"),
                onDragStart = onDragStart,
                onDrag = onDrag,
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel
            )
            Spacer(Modifier.width(4.dp))
            DocPropertyGlyph(property, LingyeColors.Muted, 18.dp)
            Spacer(Modifier.width(10.dp))
            Text(displayName, Modifier.weight(1f), fontSize = 14.sp, color = LingyeColors.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(DocPropertyCatalog.showLabel(property.show, language), fontSize = 12.sp, color = LingyeColors.Muted)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                Text("⋯", fontSize = 16.sp, color = LingyeColors.Muted)
            }
        }
        LingyeDropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            modifier = Modifier.width(248.dp)
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DocPropertyGlyph(property.copy(icon = property.icon), LingyeColors.Muted, 18.dp)
                    Spacer(Modifier.width(8.dp))
                    if (DocPropertyCatalog.renameable(property.type)) {
                        Box(
                            Modifier.weight(1f).height(32.dp)
                                .background(LingyeColors.Canvas, RoundedCornerShape(6.dp))
                                .border(1.dp, LingyeColors.Line, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = nameDraft,
                                onValueChange = { nameDraft = it.take(100) },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 14.sp, color = LingyeColors.Ink),
                                cursorBrush = SolidColor(LingyeColors.Primary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { commitName(); focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Box(
                            Modifier.size(30.dp).clickable { commitName() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", fontSize = 16.sp, color = LingyeColors.Primary)
                        }
                    } else {
                        Text(displayName, Modifier.weight(1f), fontSize = 14.sp, color = LingyeColors.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(tr(language, "图标", "Icon"), fontSize = 11.sp, color = LingyeColors.Muted)
                Spacer(Modifier.height(2.dp))
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DocPropertyIcons.options.forEach { option ->
                        val selected = property.icon == option.key
                        Box(
                            Modifier.size(26.dp)
                                .background(
                                    if (selected) LingyeColors.Subtle else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { update { it.copy(icon = option.key) } },
                            contentAlignment = Alignment.Center
                        ) {
                            DocPropertyIconGlyph(
                                option.key,
                                if (selected) LingyeColors.Ink else LingyeColors.Muted,
                                16.dp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    tr(language, "类型 · ", "Type · ") +
                        (DocPropertyCatalog.type(property.type)?.let { DocPropertyCatalog.name(it, language) } ?: property.type),
                    fontSize = 11.sp,
                    color = LingyeColors.Muted
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp), color = LingyeColors.Line)
            listOf(
                DocPropertyCatalog.ALWAYS_SHOW to tr(language, "始终显示", "Always show"),
                DocPropertyCatalog.HIDE_WHEN_EMPTY to tr(language, "空时隐藏", "Hide when empty"),
                DocPropertyCatalog.ALWAYS_HIDE to tr(language, "始终隐藏", "Always hide")
            ).forEach { (show, label) ->
                MenuRow(label, {
                    menuOpen = false
                    update { it.copy(show = show) }
                }, checked = property.show == show)
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp), color = LingyeColors.Line)
            MenuRow(tr(language, "删除属性", "Delete property"), {
                menuOpen = false
                confirmDelete = true
            }, icon = AppIcon.TRASH, danger = true)
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(tr(language, "删除属性？", "Delete property?"), fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    tr(
                        language,
                        "「$displayName」将从属性列表中移除，此操作不可撤销。",
                        "\"$displayName\" will be removed from the property list. This cannot be undone."
                    ),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onPropertiesChange(properties.filterNot { it.id == property.id })
                }) { Text(tr(language, "删除", "Delete"), color = LingyeColors.Coral) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(tr(language, "取消", "Cancel")) }
            }
        )
    }
}

@Composable
private fun DragHandle(
    description: String,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    val start by rememberUpdatedState(onDragStart)
    val drag by rememberUpdatedState(onDrag)
    val end by rememberUpdatedState(onDragEnd)
    val cancel by rememberUpdatedState(onDragCancel)
    Box(
        Modifier.size(28.dp, 40.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { _ -> start() },
                    onDragEnd = { end() },
                    onDragCancel = { cancel() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        drag(dragAmount.y)
                    }
                )
            }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(10.dp, 16.dp)) {
            val radius = 1.4.dp.toPx()
            val xs = listOf(size.width * 0.3f, size.width * 0.7f)
            val ys = listOf(size.height * 0.2f, size.height * 0.5f, size.height * 0.8f)
            xs.forEach { x ->
                ys.forEach { y ->
                    drawCircle(LingyeColors.Muted.copy(alpha = 0.75f), radius, Offset(x, y))
                }
            }
        }
    }
}

@Composable
private fun AddPropertyRow(
    language: AppLanguage,
    definition: DocPropertyType,
    properties: List<DocPropertyInfo>,
    onAdd: () -> Unit
) {
    val added = definition.unique && properties.any { it.type == definition.key }
    Row(
        Modifier.fillMaxWidth().height(38.dp)
            .clickable(enabled = !added) { onAdd() }
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DocPropertyTypeGlyph(definition.key, LingyeColors.Muted, 18.dp)
        Spacer(Modifier.width(10.dp))
        Text(DocPropertyCatalog.name(definition, language), Modifier.weight(1f), fontSize = 14.sp, color = if (added) LingyeColors.Muted else LingyeColors.Ink)
        if (added) {
            Text(tr(language, "已添加", "Added"), fontSize = 12.sp, color = LingyeColors.Muted)
        } else {
            WorkspaceIcon(AppIcon.ADD, null, Modifier.size(16.dp), LingyeColors.Muted)
        }
    }
}

@Composable
private fun ActionRow(icon: AppIcon, label: String, onClick: () -> Unit, danger: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().height(42.dp).clickable(onClick = onClick).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WorkspaceIcon(icon, null, Modifier.size(18.dp), if (danger) LingyeColors.Coral else LingyeColors.Muted)
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 14.sp, color = if (danger) LingyeColors.Coral else LingyeColors.Ink)
    }
}

@Composable
private fun MoveToFolderRow(language: AppLanguage, folders: List<String>, onMove: (String?) -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier.fillMaxWidth().height(42.dp).clickable { menuOpen = true }.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WorkspaceIcon(AppIcon.MOVE, null, Modifier.size(18.dp), LingyeColors.Muted)
            Spacer(Modifier.width(10.dp))
            Text(tr(language, "移动到文件夹", "Move to folder"), Modifier.weight(1f), fontSize = 14.sp, color = LingyeColors.Ink)
            Text("▸", fontSize = 12.sp, color = LingyeColors.Muted)
        }
        LingyeDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, modifier = Modifier.width(208.dp)) {
            MenuRow(tr(language, "未分类", "Unfiled"), { menuOpen = false; onMove(null) })
            folders.forEach { folder ->
                MenuRow(folder, { menuOpen = false; onMove(folder) })
            }
        }
    }
}

/** Selectable icons for a property row; keys are persisted in [DocPropertyInfo.icon]. */
internal object DocPropertyIcons {
    data class Option(val key: String, val appIcon: AppIcon? = null, val glyph: String? = null)

    val options = listOf(
        Option("tag", AppIcon.TAG),
        Option("text", AppIcon.TEXT),
        Option("number", AppIcon.NUMBERED_LIST),
        Option("checkbox", glyph = "checkbox"),
        Option("date", glyph = "calendar"),
        Option("person", glyph = "person"),
        Option("clock", glyph = "clock"),
        Option("file", AppIcon.DOCUMENT),
        Option("theme", AppIcon.SPARK),
        Option("width", AppIcon.FIELD),
        Option("template", AppIcon.COPY),
        Option("folder", AppIcon.FOLDER),
        Option("star", AppIcon.FAVORITE),
        Option("link", AppIcon.LINK),
        Option("code", AppIcon.CODE_BLOCK),
        Option("image", AppIcon.IMAGE)
    )
}

@Composable
internal fun DocPropertyGlyph(property: DocPropertyInfo, tint: Color, size: Dp) {
    if (property.icon.isNotBlank()) DocPropertyIconGlyph(property.icon, tint, size)
    else DocPropertyTypeGlyph(property.type, tint, size)
}

@Composable
internal fun DocPropertyIconGlyph(key: String, tint: Color, size: Dp) {
    when (key) {
        "checkbox" -> CheckboxGlyph(tint, size)
        "calendar" -> CalendarGlyph(tint, size)
        "person" -> PersonGlyph(tint, size)
        "clock" -> ClockGlyph(tint, size)
        else -> {
            val icon = DocPropertyIcons.options.firstOrNull { it.key == key }?.appIcon ?: AppIcon.TAG
            WorkspaceIcon(icon, null, Modifier.size(size), tint)
        }
    }
}

@Composable
internal fun DocPropertyTypeGlyph(type: String, tint: Color, size: Dp) {
    when (type) {
        "tags" -> WorkspaceIcon(AppIcon.TAG, null, Modifier.size(size), tint)
        "docPrimaryMode" -> WorkspaceIcon(AppIcon.DOCUMENT, null, Modifier.size(size), tint)
        "template" -> WorkspaceIcon(AppIcon.COPY, null, Modifier.size(size), tint)
        "edgelessTheme" -> WorkspaceIcon(AppIcon.SPARK, null, Modifier.size(size), tint)
        "pageWidth" -> WorkspaceIcon(AppIcon.FIELD, null, Modifier.size(size), tint)
        "text" -> WorkspaceIcon(AppIcon.TEXT, null, Modifier.size(size), tint)
        "number" -> WorkspaceIcon(AppIcon.NUMBERED_LIST, null, Modifier.size(size), tint)
        "updatedAt" -> ClockGlyph(tint, size)
        "createdBy", "updatedBy" -> PersonGlyph(tint, size)
        "checkbox" -> CheckboxGlyph(tint, size)
        else -> CalendarGlyph(tint, size)
    }
}

@Composable
private fun CalendarGlyph(tint: Color, size: Dp) {
    Canvas(Modifier.size(size)) {
        val stroke = 1.5.dp.toPx()
        val left = this.size.width * 0.16f
        val top = this.size.height * 0.22f
        val width = this.size.width * 0.68f
        val height = this.size.height * 0.6f
        drawRoundRect(tint, Offset(left, top), Size(width, height), CornerRadius(2.dp.toPx()), style = Stroke(stroke))
        drawLine(tint, Offset(left, top + height * 0.3f), Offset(left + width, top + height * 0.3f), stroke)
        drawLine(tint, Offset(left + width * 0.28f, top - 2.dp.toPx()), Offset(left + width * 0.28f, top + 2.dp.toPx()), stroke, StrokeCap.Round)
        drawLine(tint, Offset(left + width * 0.72f, top - 2.dp.toPx()), Offset(left + width * 0.72f, top + 2.dp.toPx()), stroke, StrokeCap.Round)
    }
}

@Composable
private fun ClockGlyph(tint: Color, size: Dp) {
    Canvas(Modifier.size(size)) {
        val stroke = 1.5.dp.toPx()
        val radius = this.size.minDimension * 0.34f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        drawCircle(tint, radius, center, style = Stroke(stroke))
        drawLine(tint, center, Offset(center.x, center.y - radius * 0.55f), stroke, StrokeCap.Round)
        drawLine(tint, center, Offset(center.x + radius * 0.42f, center.y), stroke, StrokeCap.Round)
    }
}

@Composable
private fun PersonGlyph(tint: Color, size: Dp) {
    Canvas(Modifier.size(size)) {
        val stroke = 1.5.dp.toPx()
        val headRadius = this.size.minDimension * 0.16f
        val center = Offset(this.size.width / 2f, this.size.height * 0.36f)
        drawCircle(tint, headRadius, center, style = Stroke(stroke))
        drawArc(
            tint, 200f, 140f, false,
            topLeft = Offset(this.size.width * 0.2f, this.size.height * 0.5f),
            size = Size(this.size.width * 0.6f, this.size.height * 0.42f),
            style = Stroke(stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun CheckboxGlyph(tint: Color, size: Dp) {
    Canvas(Modifier.size(size)) {
        val stroke = 1.5.dp.toPx()
        val side = this.size.minDimension * 0.62f
        val topLeft = Offset((this.size.width - side) / 2f, (this.size.height - side) / 2f)
        drawRoundRect(tint, topLeft, Size(side, side), CornerRadius(3.dp.toPx()), style = Stroke(stroke))
        drawLine(tint, Offset(topLeft.x + side * 0.22f, topLeft.y + side * 0.55f), Offset(topLeft.x + side * 0.43f, topLeft.y + side * 0.75f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(topLeft.x + side * 0.43f, topLeft.y + side * 0.75f), Offset(topLeft.x + side * 0.8f, topLeft.y + side * 0.28f), stroke, StrokeCap.Round)
    }
}
