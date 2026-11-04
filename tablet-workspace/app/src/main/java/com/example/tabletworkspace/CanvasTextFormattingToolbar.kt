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
import androidx.compose.ui.text.input.TextFieldValue

@Composable
internal fun CanvasTextFormattingToolbar(
    page: Page,
    pages: List<Page>,
    language: AppLanguage,
    scale: Float,
    viewport: Viewport,
    canvasWidthDp: Dp,
    canvasHeightDp: Dp,
    imeBottomDp: Dp,
    objectKind: CanvasObjectKind?,
    editIndex: Int,
    editShape: Shape,
    editValue: TextFieldValue,
    editSelectionStart: Int,
    editSelectionEnd: Int,
    textEditValueState: MutableState<TextFieldValue?>,
    hideKeyboard: () -> Unit,
    onChanged: () -> Unit,
    onCardEditChange: (Int?) -> Unit,
    onSelectionChange: (CanvasSelection?, List<CanvasSelection>) -> Unit,
    changedText: (String?) -> Unit
) {
    var textEditValue by textEditValueState
            var textStyleMenu by remember(editIndex) { mutableStateOf(false) }
            var textAlignMenu by remember(editIndex) { mutableStateOf(false) }
            var textMoreMenu by remember(editIndex) { mutableStateOf(false) }
            var textHighlightMenu by remember(editIndex) { mutableStateOf(false) }
            var textLinkPopover by remember(editIndex) { mutableStateOf(false) }
            var textLinkedDocMenu by remember(editIndex) { mutableStateOf(false) }
            var textLinkDraft by remember(editIndex) { mutableStateOf("") }
            var textEquationPopover by remember(editIndex) { mutableStateOf(false) }
            var textEquationDraft by remember(editIndex) { mutableStateOf("") }
            var textCommentPopover by remember(editIndex) { mutableStateOf(false) }
            var textCommentDraft by remember(editIndex) { mutableStateOf("") }
            val bounds = page.selectionBounds(listOf(CanvasSelection("shape", editIndex))) ?: return
            val at = viewport.toScreen(Point(bounds.left, bounds.top))
            val selectionContext = LocalContext.current
            val selectionClipboard = selectionContext.getSystemService(android.content.ClipboardManager::class.java)
            val commonFlags = TextFormatting.rangeFlags(editShape.textSpans, editSelectionStart, editSelectionEnd)
            fun toggleFlag(flag: Int) {
                val current = page.shapes.getOrNull(editIndex) ?: return
                var updated = current.copy(
                    textSpans = TextFormatting.toggleRange(
                        current.textSpans, editSelectionStart, editSelectionEnd, flag, current.body.length
                    )
                )
                if (textAutoHeight(updated)) {
                    updated = updated.copy(height = autoFitTextHeight(updated.body, updated.width, updated.fontSize))
                }
                page.shapes[editIndex] = updated
                textEditValue = textEditValue?.copy(
                    selection = androidx.compose.ui.text.TextRange(editSelectionStart, editSelectionEnd)
                )
                changedText("text-format:${current.id}")
            }
            fun applyTextStyle(style: String) {
                val current = page.shapes.getOrNull(editIndex) ?: return
                val (size, weight, family) = when (style) {
                    "h1" -> Triple(32f, 700, "sans")
                    "h2" -> Triple(28f, 700, "sans")
                    "h3" -> Triple(24f, 700, "sans")
                    "h4" -> Triple(20f, 700, "sans")
                    "h5" -> Triple(18f, 700, "sans")
                    "h6" -> Triple(16f, 700, "sans")
                    "code" -> Triple(14f, 400, "mono")
                    else -> Triple(16f, 400, "sans")
                }
                var updated = current.copy(fontSize = size, fontWeight = weight, fontFamily = family)
                if (textAutoHeight(updated)) {
                    updated = updated.copy(height = autoFitTextHeight(updated.body, updated.width, updated.fontSize))
                }
                page.shapes[editIndex] = updated
                onChanged()
                textStyleMenu = false
            }
            fun applyColor(index: Int, foreground: Boolean) {
                val current = page.shapes.getOrNull(editIndex) ?: return
                var updated = current.copy(
                    textSpans = TextFormatting.setColorRange(
                        current.textSpans, editSelectionStart, editSelectionEnd,
                        if (foreground) TextSpan.FG_MASK else TextSpan.BG_MASK,
                        if (foreground) TextSpan.FG_SHIFT else TextSpan.BG_SHIFT,
                        index, current.body.length
                    )
                )
                if (textAutoHeight(updated)) {
                    updated = updated.copy(height = autoFitTextHeight(updated.body, updated.width, updated.fontSize))
                }
                page.shapes[editIndex] = updated
                textEditValue = textEditValue?.copy(
                    selection = androidx.compose.ui.text.TextRange(editSelectionStart, editSelectionEnd)
                )
                changedText("text-color:${current.id}")
            }
            fun applyLink(rawUrl: String) {
                val current = page.shapes.getOrNull(editIndex) ?: return
                val value = rawUrl.trim()
                if (value.isEmpty()) return
                val url = if ("://" in value) value else "https://$value"
                var updated = current.copy(
                    textSpans = TextFormatting.addFlagRange(
                        current.textSpans, editSelectionStart, editSelectionEnd, TextSpan.LINK, current.body.length
                    ),
                    cardKind = TextLinks.withLink(current.cardKind, editSelectionStart, editSelectionEnd, url)
                )
                if (textAutoHeight(updated)) {
                    updated = updated.copy(height = autoFitTextHeight(updated.body, updated.width, updated.fontSize))
                }
                page.shapes[editIndex] = updated
                textLinkPopover = false
                textEditValue = textEditValue?.copy(
                    selection = androidx.compose.ui.text.TextRange(editSelectionStart, editSelectionEnd)
                )
                changedText("text-link:${current.id}")
            }
            fun openLinkPopover() {
                hideKeyboard()
                textLinkDraft = TextLinks.linkAt(editShape.cardKind, editSelectionStart)?.url ?: ""
                textLinkPopover = true
            }
            fun commentSpans(shape: Shape, cardKind: String): List<TextSpan> {
                var spans = TextFormatting.normalize(
                    shape.textSpans.map { TextSpan(it.start, it.end, it.flags and TextSpan.COMMENT.inv()) },
                    shape.body.length
                )
                TextComments.parse(cardKind).forEach { comment ->
                    spans = TextFormatting.addFlagRange(spans, comment.start, comment.end, TextSpan.COMMENT, shape.body.length)
                }
                return spans
            }
            fun applyComment(text: String) {
                val current = page.shapes.getOrNull(editIndex) ?: return
                val value = text.trim()
                if (value.isEmpty()) return
                val cardKind = TextComments.withComment(current.cardKind, editSelectionStart, editSelectionEnd, value)
                val updated = current.copy(cardKind = cardKind, textSpans = commentSpans(current, cardKind))
                page.shapes[editIndex] = updated
                textCommentDraft = ""
                textEditValue = textEditValue?.copy(
                    selection = androidx.compose.ui.text.TextRange(editSelectionStart, editSelectionEnd)
                )
                changedText("text-comment:${current.id}")
            }
            fun deleteComment(comment: TextComment) {
                val current = page.shapes.getOrNull(editIndex) ?: return
                val cardKind = TextComments.withoutComment(current.cardKind, comment)
                page.shapes[editIndex] = current.copy(cardKind = cardKind, textSpans = commentSpans(current, cardKind))
                changedText("text-comment:${current.id}")
            }
            fun openEquationPopover() {
                hideKeyboard()
                val existing = TextEquations.at(editShape.cardKind, editSelectionStart)
                textEquationDraft = existing?.tex
                    ?: editValue.text.substring(editSelectionStart, editSelectionEnd).takeIf { it.isNotBlank() }
                    ?: ""
                textEquationPopover = true
            }
            fun applyEquation(rawTex: String) {
                val tex = rawTex.trim()
                if (tex.isEmpty()) return
                val current = page.shapes.getOrNull(editIndex) ?: return
                val existing = TextEquations.parse(current.cardKind)
                    .firstOrNull { it.start < editSelectionEnd && editSelectionStart < it.end }
                val start = existing?.start ?: editSelectionStart
                val end = existing?.end ?: editSelectionEnd
                if (start >= end) return
                val rendered = TextInlineMath.render(tex)
                if (rendered.isBlank()) return
                val newBody = current.body.replaceRange(start, end, rendered)
                val newEnd = start + rendered.length
                var spans = TextFormatting.adjustForEdit(current.textSpans, current.body, newBody)
                spans = TextFormatting.removeFlagRange(spans, start, newEnd, TextSpan.MATH or TextSpan.LINK or TextSpan.COMMENT, newBody.length)
                spans = TextFormatting.addFlagRange(spans, start, newEnd, TextSpan.MATH, newBody.length)
                var cardKind = TextLinks.adjust(current.cardKind, current.body, newBody)
                cardKind = TextComments.adjust(cardKind, current.body, newBody)
                cardKind = TextEquations.adjust(cardKind, current.body, newBody)
                cardKind = TextLinks.withoutOverlapping(cardKind, start, newEnd)
                cardKind = TextComments.withoutOverlapping(cardKind, start, newEnd)
                cardKind = TextEquations.withoutOverlapping(cardKind, start, newEnd)
                cardKind = TextEquations.withEquation(cardKind, start, newEnd, tex)
                var updated = current.copy(body = newBody, textSpans = spans, cardKind = cardKind)
                if (textAutoHeight(updated)) {
                    updated = updated.copy(height = autoFitTextHeight(updated.body, updated.width, updated.fontSize))
                }
                page.shapes[editIndex] = updated
                textEquationPopover = false
                textEditValue = TextFieldValue(newBody, androidx.compose.ui.text.TextRange(start, newEnd))
                changedText("text-equation:${current.id}")
            }
            fun openCommentPopover() {
                hideKeyboard()
                textCommentDraft = ""
                textCommentPopover = true
            }
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
                Box {
                    TextSelectionToolbar(
                        language = language,
                        bold = commonFlags and TextSpan.BOLD != 0,
                        italic = commonFlags and TextSpan.ITALIC != 0,
                        underline = commonFlags and TextSpan.UNDERLINE != 0,
                        strike = commonFlags and TextSpan.STRIKE != 0,
                        code = commonFlags and TextSpan.CODE != 0,
                        styleMenuExpanded = textStyleMenu,
                        onDismissStyleMenu = { textStyleMenu = false },
                        styleMenu = {
                            TextToolbarMenuItem(tr(language, "正文", "Text"), InlineToolbarIcons.Text) { applyTextStyle("text") }
                            TextToolbarMenuItem(tr(language, "标题 1", "Heading 1"), InlineToolbarIcons.Heading1) { applyTextStyle("h1") }
                            TextToolbarMenuItem(tr(language, "标题 2", "Heading 2"), InlineToolbarIcons.Heading2) { applyTextStyle("h2") }
                            TextToolbarMenuItem(tr(language, "标题 3", "Heading 3"), InlineToolbarIcons.Heading3) { applyTextStyle("h3") }
                            TextToolbarMenuItem(tr(language, "标题 4", "Heading 4"), InlineToolbarIcons.Heading4) { applyTextStyle("h4") }
                            TextToolbarMenuItem(tr(language, "标题 5", "Heading 5"), InlineToolbarIcons.Heading5) { applyTextStyle("h5") }
                            TextToolbarMenuItem(tr(language, "标题 6", "Heading 6"), InlineToolbarIcons.Heading6) { applyTextStyle("h6") }
                            TextToolbarMenuItem(tr(language, "项目符号", "Bulleted List"), InlineToolbarIcons.BulletedList, enabled = false) {}
                            TextToolbarMenuItem(tr(language, "编号列表", "Numbered List"), InlineToolbarIcons.NumberedList, enabled = false) {}
                            TextToolbarMenuItem(tr(language, "待办列表", "To-do List"), InlineToolbarIcons.TodoList, enabled = false) {}
                            TextToolbarMenuItem(tr(language, "代码块", "Code Block"), InlineToolbarIcons.CodeBlock) { applyTextStyle("code") }
                            TextToolbarMenuItem(tr(language, "公式", "Equation"), InlineToolbarIcons.EquationBlock, enabled = false) {}
                            TextToolbarMenuItem(tr(language, "引用", "Quote"), InlineToolbarIcons.Quote, enabled = false) {}
                        },
                        alignMenuExpanded = textAlignMenu,
                        onDismissAlignMenu = { textAlignMenu = false },
                        alignMenu = {
                            listOf(
                                Triple("left", tr(language, "左对齐", "Align left"), InlineToolbarIcons.AlignLeft),
                                Triple("center", tr(language, "居中", "Align center"), InlineToolbarIcons.AlignCenter),
                                Triple("right", tr(language, "右对齐", "Align right"), InlineToolbarIcons.AlignRight)
                            ).forEach { (value, label, icon) ->
                                TextToolbarMenuItem(label, icon, selected = editShape.textAlign == value) {
                                    CanvasPropertyEditor.updateShape(page, editIndex, onChanged) { it.copy(textAlign = value) }
                                    textAlignMenu = false
                                }
                            }
                        },
                        highlightMenuExpanded = textHighlightMenu,
                        onDismissHighlightMenu = { textHighlightMenu = false },
                        highlightMenu = {
                            val colorNames = listOf(
                                tr(language, "红色", "Red"), tr(language, "橙色", "Orange"), tr(language, "黄色", "Yellow"),
                                tr(language, "绿色", "Green"), tr(language, "青色", "Teal"), tr(language, "蓝色", "Blue"),
                                tr(language, "紫色", "Purple"), tr(language, "灰色", "Grey")
                            )
                            TextToolbarMenuHeading(tr(language, "颜色", "Color"))
                            for (i in 0..8) {
                                TextToolbarColorMenuItem(
                                    label = if (i == 0) tr(language, "默认颜色", "Default Color") else colorNames[i - 1],
                                    color = if (i == 0) LingyeColors.Ink else Color(TextHighlightPalette.foreground[i]),
                                    selected = TextSpan.foreground(commonFlags) == i
                                ) { applyColor(i, true); textHighlightMenu = false }
                            }
                            TextToolbarMenuHeading(tr(language, "背景", "Background"))
                            for (i in 0..8) {
                                TextToolbarColorMenuItem(
                                    label = if (i == 0) tr(language, "默认背景", "Default Background") else colorNames[i - 1],
                                    color = if (i == 0) LingyeColors.Muted else Color(TextHighlightPalette.background[i]),
                                    selected = TextSpan.background(commonFlags) == i
                                ) { applyColor(i, false); textHighlightMenu = false }
                            }
                        },
                        equationPopoverExpanded = textEquationPopover,
                        onDismissEquationPopover = { textEquationPopover = false },
                        equationPopover = {
                            Row(
                                Modifier.width(334.dp).height(30.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.weight(1f).height(30.dp)
                                        .border(1.dp, LingyeColors.Primary, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (textEquationDraft.isEmpty()) {
                                        Text(tr(language, "TeX，如 x^2 + 1", "TeX, e.g. x^2 + 1"), fontSize = 14.sp, color = LingyeColors.Muted, maxLines = 1)
                                    }
                                    BasicTextField(
                                        value = textEquationDraft,
                                        onValueChange = { textEquationDraft = it.take(2000) },
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = LingyeColors.Ink),
                                        cursorBrush = SolidColor(LingyeColors.Primary),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    Modifier.width(28.dp).height(30.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable(enabled = textEquationDraft.isNotBlank()) { applyEquation(textEquationDraft) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    SvgPathIcon(
                                        InlineToolbarIcons.LinkConfirm,
                                        if (textEquationDraft.isNotBlank()) LingyeColors.Ink else LingyeColors.Muted.copy(alpha = 0.4f),
                                        Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        commentPopoverExpanded = textCommentPopover,
                        onDismissCommentPopover = { textCommentPopover = false },
                        commentPopover = {
                            TextToolbarMenuHeading(tr(language, "评论", "Comments"))
                            val comments = TextComments.parse(editShape.cardKind)
                            if (comments.isEmpty()) {
                                Text(
                                    tr(language, "选中文字后添加评论", "Select text to add a comment"),
                                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 13.sp,
                                    color = LingyeColors.Muted
                                )
                            }
                            comments.takeLast(8).forEach { comment ->
                                Row(
                                    Modifier.width(304.dp).padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        comment.text,
                                        Modifier.weight(1f),
                                        fontSize = 13.sp,
                                        color = LingyeColors.Ink,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        tr(language, "删除", "Delete"),
                                        Modifier.padding(start = 8.dp).clickable { deleteComment(comment) },
                                        fontSize = 12.sp,
                                        color = LingyeColors.Coral
                                    )
                                }
                            }
                            Row(
                                Modifier.width(304.dp).height(30.dp).padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.weight(1f).height(30.dp)
                                        .border(1.dp, LingyeColors.Line, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (textCommentDraft.isEmpty()) {
                                        Text(tr(language, "添加评论…", "Add a comment…"), fontSize = 13.sp, color = LingyeColors.Muted, maxLines = 1)
                                    }
                                    BasicTextField(
                                        value = textCommentDraft,
                                        onValueChange = { textCommentDraft = it.take(4000) },
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = LingyeColors.Ink),
                                        cursorBrush = SolidColor(LingyeColors.Primary),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    Modifier.width(28.dp).height(30.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable(enabled = textCommentDraft.isNotBlank()) { applyComment(textCommentDraft) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    SvgPathIcon(
                                        InlineToolbarIcons.Comment,
                                        if (textCommentDraft.isNotBlank()) LingyeColors.Ink else LingyeColors.Muted.copy(alpha = 0.4f),
                                        Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        linkMenuExpanded = textLinkPopover,
                        onDismissLinkMenu = { textLinkPopover = false },
                        linkMenu = {
                            Row(
                                Modifier.width(334.dp).height(30.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.weight(1f).height(30.dp)
                                        .border(1.dp, LingyeColors.Primary, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (textLinkDraft.isEmpty()) {
                                        Text("Paste or type a link", fontSize = 14.sp, color = LingyeColors.Muted, maxLines = 1)
                                    }
                                    BasicTextField(
                                        value = textLinkDraft,
                                        onValueChange = { textLinkDraft = it.take(2048) },
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = LingyeColors.Ink),
                                        cursorBrush = SolidColor(LingyeColors.Primary),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    Modifier.width(28.dp).height(30.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable(enabled = textLinkDraft.isNotBlank()) { applyLink(textLinkDraft) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    SvgPathIcon(
                                        InlineToolbarIcons.LinkConfirm,
                                        if (textLinkDraft.isNotBlank()) LingyeColors.Ink else LingyeColors.Muted.copy(alpha = 0.4f),
                                        Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        linkedMenuExpanded = textLinkedDocMenu,
                        onDismissLinkedMenu = { textLinkedDocMenu = false },
                        linkedMenu = {
                            TextToolbarMenuHeading(tr(language, "链接到文档", "Link to doc"))
                            val targets = pages.filter { it.id != page.id && !it.trashed }
                                .sortedBy { it.title.lowercase() }.take(8)
                            if (targets.isEmpty()) {
                                Text(
                                    tr(language, "没有可链接的文档", "No documents to link"),
                                    Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    fontSize = 13.sp,
                                    color = LingyeColors.Muted
                                )
                            }
                            targets.forEach { target ->
                                TextToolbarMenuItem(
                                    target.title.ifBlank { tr(language, "未命名文档", "Untitled document") },
                                    InlineToolbarIcons.LinkedDoc
                                ) {
                                    applyLink("lingye://page/${target.id}")
                                    textLinkedDocMenu = false
                                }
                            }
                        },
                        moreMenuExpanded = textMoreMenu,
                        onDismissMoreMenu = { textMoreMenu = false },
                        moreMenu = {
                            TextToolbarMenuItem(tr(language, "复制", "Copy"), InlineToolbarIcons.Copy) {
                                val selected = editValue.text.substring(editSelectionStart, editSelectionEnd)
                                selectionClipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Lingye", selected))
                                android.widget.Toast.makeText(selectionContext, tr(language, "已复制", "Copied"), android.widget.Toast.LENGTH_SHORT).show()
                                textMoreMenu = false
                            }
                            TextToolbarMenuItem(tr(language, "复制为 Markdown", "Copy as Markdown"), InlineToolbarIcons.CopyMarkdown) {
                                val selected = editValue.text.substring(editSelectionStart, editSelectionEnd)
                                selectionClipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Lingye", selected))
                                textMoreMenu = false
                            }
                            TextToolbarMenuItem(tr(language, "复制块链接", "Copy link to block"), InlineToolbarIcons.CopyLink) {
                                selectionClipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Lingye", "lingye://page/${page.id}"))
                                android.widget.Toast.makeText(selectionContext, tr(language, "已复制链接", "Link copied"), android.widget.Toast.LENGTH_SHORT).show()
                                textMoreMenu = false
                            }
                            TextToolbarMenuItem(tr(language, "创建副本", "Duplicate"), InlineToolbarIcons.Duplicate) {
                                val copied = page.duplicateSelections(listOf(CanvasSelection("shape", editIndex)))
                                onCardEditChange(null)
                                onSelectionChange(copied.lastOrNull(), copied)
                                textMoreMenu = false
                                onChanged()
                            }
                            Spacer(Modifier.height(8.dp))
                            TextToolbarMenuItem(tr(language, "删除", "Delete"), InlineToolbarIcons.Delete, danger = true) {
                                page.deleteSelection(CanvasSelection("shape", editIndex))
                                onCardEditChange(null)
                                onSelectionChange(null, emptyList())
                                textMoreMenu = false
                                onChanged()
                            }
                        },
                        onTextStyle = { hideKeyboard(); textStyleMenu = true },
                        onAlignment = { hideKeyboard(); textAlignMenu = true },
                        onBold = { toggleFlag(TextSpan.BOLD) },
                        onCode = { toggleFlag(TextSpan.CODE) },
                        onEquation = { openEquationPopover() },
                        onItalic = { toggleFlag(TextSpan.ITALIC) },
                        onLink = { openLinkPopover() },
                        onStrike = { toggleFlag(TextSpan.STRIKE) },
                        onUnderline = { toggleFlag(TextSpan.UNDERLINE) },
                        onHighlight = { hideKeyboard(); textHighlightMenu = true },
                        onLinkedDoc = { hideKeyboard(); textLinkedDocMenu = true },
                        onComment = { openCommentPopover() },
                        onMore = { hideKeyboard(); textMoreMenu = true }
                    )
                }
            }
}
