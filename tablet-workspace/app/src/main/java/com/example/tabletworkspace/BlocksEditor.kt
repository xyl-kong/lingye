package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageStore

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth

internal fun blockTypeIcon(type: String): AppIcon = when (type) {
    "paragraph" -> AppIcon.FIELD
    "heading" -> AppIcon.DOCUMENT
    "todo" -> AppIcon.CARD
    "bulleted" -> AppIcon.BULLETED_LIST
    "numbered" -> AppIcon.NUMBERED_LIST
    "quote" -> AppIcon.QUOTE
    "code" -> AppIcon.CODE_BLOCK
    "divider" -> AppIcon.DIVIDER
    "image" -> AppIcon.IMAGE
    "database" -> AppIcon.DATABASE
    "shared" -> AppIcon.CARD
    else -> AppIcon.FIELD
}

@Composable
@androidx.compose.runtime.NonSkippableComposable
internal fun BlocksEditor(page: Page, store: PageStore, revision: Int, language: AppLanguage, editorFontSize: Float, editorFontFamily: FontFamily, smoothImages: Boolean, codeLineNumbers: Boolean, sharedBlocks:List<SharedBlock>, sharedAvailable:Boolean, replaceShared:(List<SharedBlock>)->Boolean, editShared:(SharedBlock)->Boolean, stepShared:(String,Boolean)->Unit, replaceSharedImage:(String)->Unit, changed: () -> Unit, changedText:(String?)->Unit, scrollState: LazyListState, addImageAfter:(Int)->Unit, modifier: Modifier = Modifier, header: @Composable () -> Unit = {}, footer: @Composable () -> Unit = {}) {
    val blockCenters = remember(page.id) { mutableStateMapOf<Int, Float>() }
    var draggingIndex by remember(page.id) { mutableStateOf<Int?>(null) }
    var dragTargetIndex by remember(page.id) { mutableStateOf<Int?>(null) }
    var dragOffset by remember(page.id) { mutableFloatStateOf(0f) }
    var dragOriginY by remember(page.id) { mutableFloatStateOf(0f) }
    var sharedPickerIndex by remember(page.id) { mutableStateOf<Int?>(null) }
    @Composable
    fun BlockActions(index: Int, block: Block, actionsMenu: Boolean, onActionsMenu: (Boolean) -> Unit, typeMenu: Boolean, onTypeMenu: (Boolean) -> Unit) {
        Box {
            CompactIconAction(AppIcon.MORE, tr(language, "块操作", "Block actions"), { onActionsMenu(true) }, LingyeColors.Muted, selected = actionsMenu)
            LingyeDropdownMenu(expanded = actionsMenu, onDismissRequest = { onActionsMenu(false) }, modifier = Modifier.widthIn(min = 228.dp)) {
                if (page.canChangeBlockType(index)) {
                    MenuRow(tr(language, "更改块类型", "Change block type"), { onActionsMenu(false); onTypeMenu(true) }, icon = AppIcon.FIELD)
                }
                MenuSectionLabel(tr(language, "顺序", "Order"))
                MenuRow(tr(language, "上移", "Move up"), { page.moveBlock(index, index - 1); onActionsMenu(false); changed() }, icon = AppIcon.LAYER_UP, enabled = index > 0)
                MenuRow(tr(language, "下移", "Move down"), { page.moveBlock(index, index + 1); onActionsMenu(false); changed() }, icon = AppIcon.LAYER_DOWN, enabled = index < page.blocks.lastIndex)
                if (page.canFormatBlockText(index)) {
                    HorizontalDivider(Modifier.padding(vertical = 5.dp), color = LingyeColors.Line)
                    MenuSectionLabel(tr(language, "文字样式", "Text style"))
                    MenuRow(tr(language, "粗体", "Bold"), { block.bold = !block.bold; changed() }, leading = { Text("B", fontWeight = FontWeight.Bold) })
                    MenuRow(tr(language, "斜体", "Italic"), { block.italic = !block.italic; changed() }, leading = { Text("I", fontStyle = FontStyle.Italic) })
                    MenuRow(tr(language, "下划线", "Underline"), { block.underline = !block.underline; changed() }, leading = { Text("U", textDecoration = TextDecoration.Underline) })
                }
                if (block.type in setOf("paragraph", "heading", "todo", "image", "database")) {
                    HorizontalDivider(Modifier.padding(vertical = 5.dp), color = LingyeColors.Line)
                    MenuRow(tr(language, "转为共享块", "Make shared"), {
                        val shared = SharedBlock(java.util.UUID.randomUUID().toString(), block.content, block.type, block.bold || block.type == "heading", block.italic, block.underline, block.checked)
                        if (replaceShared(sharedBlocks + shared)) { if (index in page.blocks.indices) page.blocks[index] = Block("shared", shared.id); onActionsMenu(false); changed() }
                    }, icon = AppIcon.COPY, enabled = sharedAvailable)
                }
                if (block.type == "shared") {
                    val shared = sharedBlocks.firstOrNull { it.id == block.content }
                    if (shared != null) {
                        HorizontalDivider(Modifier.padding(vertical = 5.dp), color = LingyeColors.Line)
                        MenuSectionLabel(tr(language, "共享同步", "Shared sync"))
                        MenuRow(tr(language, "共享撤销", "Undo shared"), { stepShared(shared.id, true); onActionsMenu(false) }, icon = AppIcon.UNDO)
                        MenuRow(tr(language, "共享重做", "Redo shared"), { stepShared(shared.id, false); onActionsMenu(false) }, icon = AppIcon.REDO)
                        if (shared.kind in setOf("paragraph", "heading", "todo")) {
                            MenuRow(tr(language, "共享文字粗体", "Shared text bold"), { editShared(shared.copy(bold = !shared.bold)) }, leading = { Text("B", fontWeight = FontWeight.Bold) })
                            MenuRow(tr(language, "共享文字斜体", "Shared text italic"), { editShared(shared.copy(italic = !shared.italic)) }, leading = { Text("I", fontStyle = FontStyle.Italic) })
                            MenuRow(tr(language, "共享文字下划线", "Shared text underline"), { editShared(shared.copy(underline = !shared.underline)) }, leading = { Text("U", textDecoration = TextDecoration.Underline) })
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 5.dp), color = LingyeColors.Line)
                MenuRow(tr(language, "删除块", "Delete block"), { if (index in page.blocks.indices) page.blocks.removeAt(index); onActionsMenu(false); changed() }, icon = AppIcon.TRASH, danger = true)
            }
            LingyeDropdownMenu(expanded = typeMenu, onDismissRequest = { onTypeMenu(false) }, modifier = Modifier.widthIn(min = 228.dp)) {
                MenuSectionLabel(tr(language, "转换类型", "Change type"))
                listOf(
                    Triple("paragraph", tr(language, "段落", "Paragraph"), AppIcon.FIELD),
                    Triple("heading", tr(language, "标题", "Heading"), AppIcon.DOCUMENT),
                    Triple("todo", tr(language, "待办", "To-do"), AppIcon.CARD),
                    Triple("bulleted", tr(language, "项目符号", "Bulleted list"), AppIcon.BULLETED_LIST),
                    Triple("numbered", tr(language, "编号列表", "Numbered list"), AppIcon.NUMBERED_LIST),
                    Triple("quote", tr(language, "引用", "Quote"), AppIcon.QUOTE),
                    Triple("code", tr(language, "代码块", "Code"), AppIcon.CODE_BLOCK),
                    Triple("divider", tr(language, "分隔线", "Divider"), AppIcon.DIVIDER)
                ).forEach { (type, label, icon) ->
                    MenuRow(label, {
                        if (block.type != type) {
                            page.changeBlockType(index, type)
                            changed()
                        }
                        onTypeMenu(false)
                    }, icon = icon, checked = block.type == type)
                }
            }
        }
    }
    fun handleDocumentShortcut(event: KeyEvent, index: Int): Boolean {
        if (event.type != KeyEventType.KeyDown) return false
        val ctrl = event.isCtrlPressed || event.isMetaPressed
        val alt = event.isAltPressed
        val letter = when (event.key) {
            Key.B -> 'B'
            Key.I -> 'I'
            Key.U -> 'U'
            Key.Zero -> '0'
            Key.One -> '1'
            Key.Two -> '2'
            Key.Three -> '3'
            Key.Four -> '4'
            Key.Five -> '5'
            Key.Six -> '6'
            else -> return false
        }
        val action = WorkspaceShortcuts.documentActionFor(letter, ctrl, alt) ?: return false
        return when (action) {
            WorkspaceShortcuts.DocumentAction.BOLD,
            WorkspaceShortcuts.DocumentAction.ITALIC,
            WorkspaceShortcuts.DocumentAction.UNDERLINE -> {
                if (!page.canFormatBlockText(index)) false else {
                    val block = page.blocks.getOrNull(index) ?: return false
                    when (action) {
                        WorkspaceShortcuts.DocumentAction.BOLD -> block.bold = !block.bold
                        WorkspaceShortcuts.DocumentAction.ITALIC -> block.italic = !block.italic
                        else -> block.underline = !block.underline
                    }
                    changed()
                    true
                }
            }
            WorkspaceShortcuts.DocumentAction.PARAGRAPH -> {
                if (!page.canChangeBlockType(index)) false else {
                    page.changeBlockType(index, "paragraph")
                    changed()
                    true
                }
            }
            else -> {
                val level = WorkspaceShortcuts.headingLevel(action) ?: return false
                if (!page.canChangeBlockType(index)) false else {
                    page.changeBlockType(index, WorkspaceShortcuts.headingType(level))
                    changed()
                    true
                }
            }
        }
    }
    val blockSnapshot = page.blocks.toList()
    LazyColumn(state = scrollState, modifier = modifier) {
        item(key = "document-header") { header() }
        if (blockSnapshot.isEmpty()) {
            item(key = "empty-block") {
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp).clickable {
                        page.addBlock("paragraph", "")
                        changed()
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WorkspaceIcon(AppIcon.ADD, null, Modifier.padding(horizontal = 10.dp).size(18.dp), LingyeColors.Muted)
                    Text(tr(language, "输入文字或 /", "Type text or /"), color = LingyeColors.Muted, fontSize = editorFontSize.sp)
                }
            }
        }
        itemsIndexed(blockSnapshot, key = { index, _ -> index }) { index, block ->
            var actionsMenu by remember(index, block.type) { mutableStateOf(false) }
            var typeMenu by remember(index, block.type) { mutableStateOf(false) }
            var insertMenu by remember(index, block.type) { mutableStateOf(false) }
            val isDragging = draggingIndex == index
            val emptyEditableBlock = block.content.isEmpty() && block.type !in setOf("image", "database", "shared", "divider")
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffset else 0f
                        alpha = if (isDragging) 0.82f else 1f
                        shadowElevation = if (isDragging) 10.dp.toPx() else 0f
                    }
                    .onGloballyPositioned { coordinates ->
                        if (draggingIndex != index) {
                            blockCenters[index] = coordinates.boundsInWindow().center.y
                        }
                    }
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    var handleCenterY by remember(index) { mutableFloatStateOf(0f) }
                    Box(
                        Modifier.width(if (emptyEditableBlock) 76.dp else 38.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CompactIconAction(AppIcon.ADD, tr(language, "插入块", "Insert block"), { insertMenu = true }, LingyeColors.Muted)
                        BlockInsertMenu(
                            expanded = insertMenu,
                            onDismiss = { insertMenu = false },
                            language = language,
                            sharedAvailable = sharedAvailable && sharedBlocks.isNotEmpty(),
                            onAddText = { type ->
                                page.blocks.add((index + 1).coerceIn(0, page.blocks.size), Block(type, ""))
                                insertMenu = false
                                changed()
                            },
                            onAddImage = { insertMenu = false; addImageAfter(index) },
                            onAddDatabase = {
                                page.addDatabaseAfter(index, tr(language, "数据库", "Database"))
                                insertMenu = false
                                changed()
                            },
                            onAddShared = { insertMenu = false; sharedPickerIndex = index }
                        )
                    }
                    if (!emptyEditableBlock) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .onGloballyPositioned { coordinates -> handleCenterY = coordinates.boundsInWindow().center.y }
                            .pointerInput(index, page.blocks.size) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggingIndex = index
                                        dragTargetIndex = index
                                        dragOffset = 0f
                                        dragOriginY = handleCenterY
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragOffset += amount.y
                                        val currentCenters = page.blocks.indices.mapNotNull { blockIndex ->
                                            blockCenters[blockIndex]?.let { blockIndex to it }
                                        }.toMap()
                                        dragTargetIndex = BlockReorderCalculator.targetIndex(
                                            index,
                                            dragOriginY + dragOffset,
                                            currentCenters
                                        )
                                    },
                                    onDragEnd = {
                                        val target = dragTargetIndex
                                        if (target != null && target != index && target in page.blocks.indices) {
                                            page.moveBlock(index, target)
                                            changed()
                                        }
                                        draggingIndex = null
                                        dragTargetIndex = null
                                        dragOffset = 0f
                                    },
                                    onDragCancel = {
                                        draggingIndex = null
                                        dragTargetIndex = null
                                        dragOffset = 0f
                                    }
                                )
                            }
                            .semantics { contentDescription = tr(language, "拖动块", "Drag block") },
                        contentAlignment = Alignment.Center
                    ) {
                        WorkspaceIcon(
                            AppIcon.MOVE,
                            contentDescription = null,
                            tint = if (isDragging) LingyeColors.Primary else LingyeColors.Muted.copy(alpha = 0.72f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    }
                    if (block.type == "todo") Checkbox(block.checked, { block.checked = it; changed() })
                    if (block.type == "divider") {
                        HorizontalDivider(Modifier.weight(1f).padding(vertical = 12.dp), color = LingyeColors.Line)
                    } else if (block.type == "image") {
                        val bitmap = rememberStoredImage(store, block.content)
                        if (bitmap != null) Image(bitmap, contentDescription = tr(language,"已导入图片","Imported image"), modifier = Modifier.weight(1f).heightIn(max = 220.dp), filterQuality = if (smoothImages) FilterQuality.Medium else FilterQuality.None) else Text(tr(language,"图片不可用","Image unavailable"), Modifier.weight(1f))
                    } else if (block.type == "shared") { val shared=sharedBlocks.firstOrNull { it.id==block.content }; if(shared==null) Text(tr(language,"共享块不可用","Shared block unavailable"),Modifier.weight(1f)) else SharedContentEditor(shared,store,language,editorFontSize,smoothImages,sharedAvailable,editShared,replaceSharedImage,Modifier.weight(1f)) } else if (block.type == "database") DatabaseEditor(block, Modifier.weight(1f), language, changed) else {
                        val itemIcon = when (block.type) {
                            "bulleted" -> AppIcon.BULLETED_LIST
                            "numbered" -> AppIcon.NUMBERED_LIST
                            "quote" -> AppIcon.QUOTE
                            "code" -> AppIcon.CODE_BLOCK
                            else -> null
                        }
                        if (itemIcon != null) WorkspaceIcon(itemIcon, null, Modifier.padding(end = 8.dp).size(18.dp), LingyeColors.Muted)
                        val slashQuery = SlashCommandCatalog.query(block.content)
                        var dismissedSlash by remember(index, block.type) { mutableStateOf<String?>(null) }
                        LaunchedEffect(slashQuery) { if (slashQuery == null) dismissedSlash = null }
                        Row(Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                        if (block.type == "code" && codeLineNumbers) {
                            val lineCount = block.content.count { it == '\n' } + 1
                            Text(
                                (1..lineCount).joinToString("\n"),
                                Modifier.padding(top = 12.dp, end = 10.dp).widthIn(min = 18.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = (editorFontSize - 1).sp,
                                lineHeight = ((editorFontSize - 1) * 1.2f).sp,
                                color = LingyeColors.Muted.copy(alpha = 0.7f)
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            BasicTextField(
                                block.content,
                                { page.updateBlock(index, it); changedText("block:${page.id}:$index") },
                                Modifier.fillMaxWidth().heightIn(min = 42.dp).padding(vertical = 8.dp)
                                    .onPreviewKeyEvent { event -> handleDocumentShortcut(event, index) }
                                    .semantics { contentDescription = tr(language, "文本块", "Text block") },
                                cursorBrush = SolidColor(LingyeColors.Primary),
                                decorationBox = { inner -> Box { if (block.content.isEmpty()) Text(tr(language,"输入文字或 /","Type text or /"), color = LingyeColors.Muted); inner() } },
                                textStyle = LocalTextStyle.current.copy(
                                    fontFamily = if (block.type == "code") FontFamily.Monospace else editorFontFamily,
                                    fontSize = when {
                                        block.type == "heading" || block.type == "heading1" -> (editorFontSize + 10).sp
                                        block.type == "heading2" -> (editorFontSize + 8).sp
                                        block.type == "heading3" -> (editorFontSize + 6).sp
                                        block.type == "heading4" -> (editorFontSize + 4).sp
                                        block.type == "heading5" -> (editorFontSize + 2).sp
                                        block.type == "heading6" -> editorFontSize.sp
                                        block.type == "code" -> (editorFontSize - 1).sp
                                        else -> editorFontSize.sp
                                    },
                                    fontWeight = if (block.bold || block.type.startsWith("heading")) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (block.italic) FontStyle.Italic else FontStyle.Normal,
                                    lineHeight = if (block.type == "code") ((editorFontSize - 1) * 1.2f).sp else TextUnit.Unspecified,
                                    textDecoration = if (block.underline) TextDecoration.Underline else TextDecoration.None
                                )
                            )
                            val matchingCommands = slashQuery?.let(SlashCommandCatalog::matches).orEmpty()
                                .filter { it.type != "shared" || (sharedAvailable && sharedBlocks.isNotEmpty()) }
                            LingyeDropdownMenu(
                                expanded = slashQuery != null && dismissedSlash != slashQuery,
                                onDismissRequest = { dismissedSlash = slashQuery },
                                modifier = Modifier.widthIn(min = 260.dp)
                            ) {
                                Column(Modifier.heightIn(max = 430.dp).verticalScroll(rememberScrollState())) {
                                Text(
                                    tr(language,"插入内容块","Insert a block"),
                                    Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LingyeColors.Muted
                                )
                                if (matchingCommands.isEmpty()) {
                                    MenuRow(tr(language,"没有匹配的块类型","No matching block type"), {}, enabled = false)
                                } else {
                                    matchingCommands.forEach { command ->
                                        MenuRow(
                                            tr(language, command.zhLabel, command.enLabel),
                                            {
                                                page.updateBlock(index, "")
                                                when (command.type) {
                                                    "image" -> { addImageAfter(index); changed() }
                                                    "database" -> {
                                                        page.addDatabaseAfter(index, tr(language, "数据库", "Database"))
                                                        changed()
                                                    }
                                                    "shared" -> {
                                                        sharedPickerIndex = index
                                                        changed()
                                                    }
                                                    else -> {
                                                        if (block.type != command.type) page.changeBlockType(index, command.type)
                                                        changed()
                                                    }
                                                }
                                            },
                                            icon = blockTypeIcon(command.type)
                                        )
                                    }
                                }
                                }
                            }
                        }
                        }
                    }
                    BlockActions(index, block, actionsMenu, { actionsMenu = it }, typeMenu, { typeMenu = it })
                }
                if (draggingIndex != null && dragTargetIndex == index && draggingIndex != index) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(2.dp).background(LingyeColors.Primary, RoundedCornerShape(50)))
                }
            }
        }
        item(key = "document-footer") { footer() }
    }
    sharedPickerIndex?.let { index ->
        AlertDialog(
            onDismissRequest = { sharedPickerIndex = null },
            title = { Text(tr(language, "插入共享块", "Insert shared block")) },
            text = {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    if (sharedBlocks.isEmpty()) {
                        Text(tr(language, "暂无共享块", "No shared blocks available"), color = LingyeColors.Muted)
                    } else {
                        sharedBlocks.forEach { item ->
                            val label = when (item.kind) {
                                "image" -> tr(language, "共享图片", "Shared image")
                                "database" -> tr(language, "共享数据库", "Shared database")
                                else -> item.text.take(60).ifBlank { tr(language, "空共享块", "Empty shared block") }
                            }
                            TextButton(onClick = {
                                page.blocks.add((index + 1).coerceIn(0, page.blocks.size), Block("shared", item.id))
                                sharedPickerIndex = null
                                changed()
                            }) { Text(label, Modifier.fillMaxWidth(), color = LingyeColors.Ink) }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { sharedPickerIndex = null }) { Text(tr(language, "取消", "Cancel")) }
            }
        )
    }
}
@Composable
internal fun BlockInsertMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    language: AppLanguage,
    sharedAvailable: Boolean,
    onAddText: (String) -> Unit,
    onAddImage: () -> Unit,
    onAddDatabase: () -> Unit,
    onAddShared: () -> Unit
) {
    LingyeDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(min = 240.dp)
    ) {
        Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
        Text(
            tr(language, "插入块", "Insert block"),
            Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = LingyeColors.Muted
        )
        SlashCommandCatalog.commands
            .filter { it.type !in setOf("image", "database", "shared") }
            .forEach { command ->
                MenuRow(tr(language, command.zhLabel, command.enLabel), { onAddText(command.type) }, icon = blockTypeIcon(command.type))
            }
        HorizontalDivider(Modifier.padding(vertical = 4.dp), color = LingyeColors.Line)
        MenuRow(tr(language, "图片", "Image"), onAddImage, icon = AppIcon.IMAGE)
        MenuRow(tr(language, "数据库", "Database"), onAddDatabase, icon = AppIcon.DATABASE)
        MenuRow(tr(language, "共享块", "Shared block"), onAddShared, icon = AppIcon.CARD, enabled = sharedAvailable)
        }
    }
}

@Composable
internal fun DocumentLinksPanel(page: Page, pages: List<Page>, language: AppLanguage) {
    var visible by remember(page.id) { mutableStateOf(true) }
    val backlinks = PageLinks.backlinks(page, pages).size
    val outgoing = page.shapes.count { PageLinks.targetId(it) != null }
    Column(Modifier.fillMaxWidth().padding(start = 72.dp, top = 28.dp, bottom = 48.dp)) {
        HorizontalDivider(color = LingyeColors.Line)
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Bi-Directional Links", Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LingyeColors.Ink)
            TextButton(onClick = { visible = !visible }) {
                Text(
                    if (visible) tr(language, "隐藏", "Hide") else tr(language, "显示", "Show"),
                    color = LingyeColors.Ink,
                    fontSize = 12.sp
                )
            }
        }
        if (visible) {
            HorizontalDivider(color = LingyeColors.Line)
            Text("${tr(language, "反向链接", "Backlinks")} · $backlinks", Modifier.padding(top = 12.dp), fontSize = 12.sp, color = LingyeColors.Muted)
            Text("${tr(language, "外链", "Outgoing links")} · $outgoing", Modifier.padding(top = 10.dp), fontSize = 12.sp, color = LingyeColors.Muted)
        }
    }
}

@Composable
internal fun SharedContentEditor(shared:SharedBlock,store:PageStore,language:AppLanguage,editorFontSize:Float,smoothImages:Boolean,enabled:Boolean,edit:(SharedBlock)->Boolean,replaceImage:(String)->Unit,modifier:Modifier=Modifier) {
 when(shared.kind) {
  "image" -> Column(modifier) { val bitmap=rememberStoredImage(store,shared.text);if(bitmap!=null) Image(bitmap,contentDescription=tr(language,"共享图片","Shared image"),modifier=Modifier.fillMaxWidth().heightIn(max=220.dp),filterQuality=if(smoothImages) FilterQuality.Medium else FilterQuality.None) else Text(tr(language,"图片不可用","Image unavailable"));TextButton(enabled=enabled,onClick={replaceImage(shared.id)}) {Text(tr(language,"替换共享图片","Replace shared image"))} }
  "database" -> { val dbBlock=remember(shared.id) { Block("database",shared.text) };dbBlock.content=shared.text;DatabaseEditor(dbBlock,modifier,language) { edit(shared.copy(text=dbBlock.content)) } }
  else -> { var draft by remember(shared.id) { mutableStateOf(TextFieldValue(annotatedSharedText(shared))) };LaunchedEffect(shared.text,shared.spans) { val annotated=annotatedSharedText(shared);if(draft.annotatedString!=annotated) draft=draft.copy(annotatedString=annotated,selection=androidx.compose.ui.text.TextRange(draft.selection.start.coerceAtMost(shared.text.length),draft.selection.end.coerceAtMost(shared.text.length)),composition=null) }
   Column(modifier) { Row(verticalAlignment=Alignment.CenterVertically) { if(shared.kind=="todo") Checkbox(shared.checked,{edit(shared.copy(checked=it))},enabled=enabled);OutlinedTextField(draft,{ value-> if(value.text==shared.text) draft=value else if(edit(shared.withTextEdit(value.text))) draft=value },Modifier.weight(1f),label={Text(tr(language,"共享文字","Shared text"))},enabled=enabled,textStyle=LocalTextStyle.current.copy(fontSize=if(shared.kind=="heading") (editorFontSize+4).sp else editorFontSize.sp,fontWeight=if(shared.bold) FontWeight.Bold else FontWeight.Normal,fontStyle=if(shared.italic) FontStyle.Italic else FontStyle.Normal,textDecoration=if(shared.underline) TextDecoration.Underline else TextDecoration.None)) }
    Row(Modifier.horizontalScroll(rememberScrollState())) { listOf("bold" to tr(language,"选区粗体","Selection bold"),"italic" to tr(language,"选区斜体","Selection italic"),"underline" to tr(language,"选区下划线","Selection underline")).forEach { (style,label)->TextButton(enabled=enabled && !draft.selection.collapsed,onClick={ val a=minOf(draft.selection.start,draft.selection.end);val b=maxOf(draft.selection.start,draft.selection.end);edit(shared.toggleRange(a,b,style)) }) {Text(label)} } }
   }
  }
 }
}
internal fun annotatedSharedText(shared:SharedBlock):AnnotatedString = buildAnnotatedString { append(shared.text);shared.spans.forEach { span -> addStyle(SpanStyle(fontWeight=if(span.bold) FontWeight.Bold else null,fontStyle=if(span.italic) FontStyle.Italic else null,textDecoration=if(span.underline) TextDecoration.Underline else null),span.start,span.end) } }
@Composable
internal fun NumericDatabaseField(value:String, label:String, onValid:(String)->Unit) {
 var draft by remember { mutableStateOf(value) }; var focused by remember { mutableStateOf(false) }
 LaunchedEffect(value,focused) { if(!focused) draft=value }
 OutlinedTextField(draft,{ input -> if(input.length<=1000 && input.matches(Regex("-?\\d*(\\.\\d*)?"))) { draft=input; if(input.isEmpty() || input.toDoubleOrNull()?.isFinite()==true) onValid(input) } },Modifier.fillMaxWidth().then(Modifier.onFocusChanged { if(focused && !it.isFocused && draft.isNotEmpty() && draft.toDoubleOrNull()?.isFinite()!=true) draft=value; focused=it.isFocused }),label={Text(label)},singleLine=true)
}

@Composable
@androidx.compose.runtime.NonSkippableComposable
internal fun DatabaseEditor(block: Block, modifier: Modifier, language: AppLanguage, changed: () -> Unit) {
 val db=runCatching { DatabaseCodec.decode(block.content) }.getOrNull()
 if(db==null) { Text(tr(language,"数据库内容无法读取","Database content could not be loaded"),modifier); return }
 var query by remember(block) { mutableStateOf("") }; var fieldName by remember(block) { mutableStateOf("") }; var fieldType by remember(block) { mutableStateOf("text") }; var fieldTypeMenu by remember(block) { mutableStateOf(false) }; var removeField by remember(block) { mutableStateOf<DatabaseField?>(null) }; var renameField by remember(block) { mutableStateOf<DatabaseField?>(null) }; var renameValue by remember(block) { mutableStateOf("") }
 var sortMenu by remember(block) { mutableStateOf(false) }
 val context=LocalContext.current
 var month by remember(block) { mutableStateOf(java.time.YearMonth.now()) }; var dateMenu by remember(block) { mutableStateOf(false) }
 fun update(next:LocalDatabase) { block.content=DatabaseCodec.encode(next); changed() }
 fun editRow(row:DatabaseRow) { update(db.copy(rows=db.rows.map { if(it.id==row.id) row else it })) }
 Column(modifier.padding(8.dp)) {
   OutlinedTextField(db.name,{update(db.copy(name=it.take(500)))},Modifier.fillMaxWidth(),label={Text(tr(language,"数据库名称","Database name"))},singleLine=true)
   OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),label={Text(tr(language,"筛选记录","Filter records"))},singleLine=true)
   Row(verticalAlignment=Alignment.CenterVertically) { Box { TextButton(onClick={sortMenu=true}) { Text(tr(language,"排序：","Sort: ") + when(db.sortField) { "@title"->tr(language,"名称","Name"); "@status"->tr(language,"状态","Status"); ""->tr(language,"手动","Manual"); else->db.fields.firstOrNull { it.id==db.sortField }?.name ?: tr(language,"手动","Manual") } + "  ") }; LingyeDropdownMenu(expanded=sortMenu,onDismissRequest={sortMenu=false}) { (listOf("" to tr(language,"手动","Manual"),"@title" to tr(language,"名称","Name"),"@status" to tr(language,"状态","Status"))+db.fields.map { it.id to it.name }).forEach { (id,label)->DropdownMenuItem(text={Text(label)},onClick={update(db.copy(sortField=id));sortMenu=false}) } } }; TextButton(enabled=db.sortField.isNotEmpty(),onClick={update(db.copy(descending=!db.descending))}) { Text(if(db.descending) tr(language,"降序","Descending") else tr(language,"升序","Ascending")) } }
   Row(Modifier.horizontalScroll(rememberScrollState())) { ModeSwitcherButton(AppIcon.TABLE, tr(language,"表格","Table"), db.viewMode=="table", Modifier.width(112.dp)) { update(db.copy(viewMode="table")) }; ModeSwitcherButton(AppIcon.BOARD, tr(language,"看板","Board"), db.viewMode=="board", Modifier.width(112.dp)) { update(db.copy(viewMode="board")) }; ModeSwitcherButton(AppIcon.PAGES, tr(language,"列表","List"), db.viewMode=="list", Modifier.width(112.dp)) { update(db.copy(viewMode="list")) }; ModeSwitcherButton(AppIcon.PAGES,tr(language,"日历","Calendar"),db.viewMode=="calendar",Modifier.width(112.dp)) {update(db.copy(viewMode="calendar"))} }
   if(db.viewMode=="board") {
    val rows=db.sortedRows(query,db.sortField,db.descending); Row(Modifier.horizontalScroll(rememberScrollState())) { (listOf("待办","进行中","完成")+rows.map { it.status.ifBlank { "未设置" } }).distinct().forEach { status -> Column(Modifier.width(200.dp).padding(4.dp)) { Text(statusLabel(language,status),fontWeight=FontWeight.Bold); rows.filter { it.status.ifBlank { "未设置" }==status }.forEach { row -> Surface(Modifier.fillMaxWidth().padding(vertical=4.dp),shape=MaterialTheme.shapes.small,color=LingyeColors.SurfaceBlue) { Column(Modifier.padding(8.dp)) { Text(row.title.ifBlank { tr(language,"未命名文档","Untitled") }); TextButton(onClick={val next=when(row.status) { "待办"->"进行中"; "进行中"->"完成"; else->"待办" };editRow(row.copy(status=next))}) { Text(tr(language,"移至下一列","Move to next column")) } } } } } } }
   } else if(db.viewMode=="calendar") {
    val available=db.fields.filter { it.type=="date" }; val selected=available.firstOrNull { it.id==db.calendarField } ?: available.firstOrNull()
   if(selected==null) Text(tr(language,"请先在表格中添加日期字段。","Add a date field in the table first.")) else {
    Row(verticalAlignment=Alignment.CenterVertically) { TextButton(onClick={month=month.minusMonths(1)}) {Text("‹")};Text(month.toString());TextButton(onClick={month=month.plusMonths(1)}) {Text("›")};Box { TextButton(onClick={dateMenu=true}) {Text(selected.name+"  ")};LingyeDropdownMenu(expanded=dateMenu,onDismissRequest={dateMenu=false}) {available.forEach { f->DropdownMenuItem(text={Text(f.name)},onClick={update(db.copy(calendarField=f.id));dateMenu=false})}} } }
    val entries=db.calendar(selected.id,month,query); val first=month.atDay(1).dayOfWeek.value-1; val count=month.lengthOfMonth(); (0 until ((first+count+6)/7)).forEach { week -> Row(Modifier.fillMaxWidth()) { (0..6).forEach { day -> val number=week*7+day-first+1; Column(Modifier.weight(1f).heightIn(min=90.dp).padding(3.dp)) { if(number in 1..count) {Text(number.toString(),fontWeight=FontWeight.Bold);entries[month.atDay(number)].orEmpty().forEach { Text(it.title.ifBlank { tr(language,"未命名文档","Untitled") },maxLines=2,fontSize=12.sp) } } } } } }
   }
   } else if(db.viewMode=="list") {
    db.sortedRows(query,db.sortField,db.descending).forEach { row -> key(row.id) { Surface(Modifier.fillMaxWidth().padding(vertical=3.dp),shape=MaterialTheme.shapes.small,color=LingyeColors.SurfaceSoft) { Column(Modifier.padding(10.dp)) { Text(row.title.ifBlank { tr(language,"未命名文档","Untitled") },fontWeight=FontWeight.SemiBold); Text(statusLabel(language,row.status),color=LingyeColors.Muted); db.fields.forEach { field -> val value=row.values[field.id].orEmpty(); if(value.isNotBlank()) Text("${field.name}: $value") } } } } }
    TextButton(onClick={update(db.copy(viewMode="table"))}) { Text(tr(language,"在表格中编辑","Edit in table")) }
  } else {
    db.sortedRows(query,db.sortField,db.descending).forEach { row -> key(row.id) { Column { Row(verticalAlignment=Alignment.CenterVertically) {
     OutlinedTextField(row.title,{editRow(row.copy(title=it.take(1000)))},Modifier.weight(1f),label={Text(tr(language,"名称","Name"))},singleLine=true)
     OutlinedTextField(statusLabel(language,row.status),{editRow(row.copy(status=statusValue(language,it).take(1000)))},Modifier.weight(1f),label={Text(tr(language,"状态","Status"))},singleLine=true)
     TextButton(onClick={update(db.moveRecord(row.id,-1))},enabled=db.sortField.isEmpty() && db.rows.indexOf(row)>0) {Text("↑")}
     TextButton(onClick={update(db.moveRecord(row.id,1))},enabled=db.sortField.isEmpty() && db.rows.indexOf(row)<db.rows.lastIndex) {Text("↓")}
     TextButton(onClick={update(db.copy(rows=db.rows.filterNot { it.id==row.id }))}) {Text(tr(language,"删除","Delete"))}
    }; db.fields.forEach { field -> if(field.type=="date") Row(verticalAlignment=Alignment.CenterVertically) { Text(field.name+": "); TextButton(onClick={ val current=runCatching { java.time.LocalDate.parse(row.values[field.id].orEmpty()) }.getOrDefault(java.time.LocalDate.now()); android.app.DatePickerDialog(context,{ _,year,month,day -> editRow(row.copy(values=row.values+(field.id to java.time.LocalDate.of(year,month+1,day).toString()))) },current.year,current.monthValue-1,current.dayOfMonth).show() }) { Text(row.values[field.id]?.ifEmpty { tr(language,"选择日期","Select date") } ?: tr(language,"选择日期","Select date")) }; TextButton(onClick={editRow(row.copy(values=row.values+(field.id to "")))}) {Text(tr(language,"清除","Clear"))} } else if(field.type=="checkbox") Row(verticalAlignment=Alignment.CenterVertically) { Checkbox(row.values[field.id]=="true",{editRow(row.copy(values=row.values+(field.id to it.toString())))}); Text(field.name) } else if(field.type=="number") NumericDatabaseField(row.values[field.id] ?: "",field.name) { value -> editRow(row.copy(values=row.values+(field.id to value))) } else if(field.type=="multiline") OutlinedTextField(row.values[field.id] ?: "",{ value -> editRow(row.copy(values=row.values+(field.id to value.take(1000)))) },Modifier.fillMaxWidth(),label={Text(field.name)},minLines=3,maxLines=6) else OutlinedTextField(row.values[field.id] ?: "",{ value -> editRow(row.copy(values=row.values+(field.id to value.take(1000)))) },Modifier.fillMaxWidth(),label={Text(field.name)},singleLine=true) } } } }
    Row(verticalAlignment=Alignment.CenterVertically) { OutlinedTextField(fieldName,{fieldName=it.take(100)},Modifier.weight(1f),label={Text(tr(language,"新字段","New field"))},singleLine=true); Box { TextButton(onClick={fieldTypeMenu=true}) {Text(when(fieldType){"number"->tr(language,"数字 ▾","Number ▾");"checkbox"->tr(language,"复选","Checkbox ▾");"multiline"->tr(language,"多行 ▾","Multiline ▾");"date"->tr(language,"日期 ▾","Date ▾");else->tr(language,"文本 ▾","Text ▾")})}; LingyeDropdownMenu(expanded=fieldTypeMenu,onDismissRequest={fieldTypeMenu=false}) { listOf("text" to tr(language,"文本","Text"),"multiline" to tr(language,"多行","Multiline"),"number" to tr(language,"数字","Number"),"checkbox" to tr(language,"复选","Checkbox"),"date" to tr(language,"日期","Date")).forEach { (type,label)->DropdownMenuItem(text={Text(label)},onClick={fieldType=type;fieldTypeMenu=false}) } } }; TextButton(enabled=fieldName.isNotBlank() && db.fields.size<40,onClick={update(db.copy(fields=db.fields+DatabaseField(java.util.UUID.randomUUID().toString(),fieldName.trim(),fieldType)));fieldName=""}) {Text(tr(language,"添加字段","Add field"))} }
    db.fields.forEachIndexed { index,field -> Row(verticalAlignment=Alignment.CenterVertically) { Text(field.name,Modifier.weight(1f)); TextButton(onClick={update(db.moveField(field.id,-1))},enabled=index>0) {Text("↑")}; TextButton(onClick={update(db.moveField(field.id,1))},enabled=index<db.fields.lastIndex) {Text("↓")}; TextButton(onClick={renameValue=field.name;renameField=field}) {Text(tr(language,"重命名","Rename"))}; TextButton(onClick={removeField=field}) {Text(tr(language,"删除字段","Delete field"))} } }
  }
   IconTextAction(AppIcon.RECORD, tr(language,"新增记录","Add record"), { update(db.addRow()) }, tint = LingyeColors.Mint)
 }
 renameField?.let { field -> AlertDialog(onDismissRequest={renameField=null},title={Text(tr(language,"重命名字段","Rename field"))},text={OutlinedTextField(renameValue,{renameValue=it.take(100)},label={Text(tr(language,"字段名称","Field name"))},singleLine=true)},confirmButton={TextButton(enabled=renameValue.isNotBlank(),onClick={update(db.renameField(field.id,renameValue));renameField=null}) {Text(tr(language,"保存","Save"))}},dismissButton={TextButton(onClick={renameField=null}) {Text(tr(language,"取消","Cancel"))}}) }
 removeField?.let { field -> AlertDialog(onDismissRequest={removeField=null},title={Text(tr(language,"删除字段？","Delete field?"))},text={Text(tr(language,"此字段及其全部记录值将被删除，可通过撤销恢复。","This field and all of its values will be deleted. You can undo this action."))},confirmButton={TextButton(onClick={update(db.removeField(field.id));removeField=null}) {Text(tr(language,"删除","Delete"))}},dismissButton={TextButton(onClick={removeField=null}) {Text(tr(language,"取消","Cancel"))}}) }
}
