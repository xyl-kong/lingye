package com.example.tabletworkspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

object TextToolbarTokens {
    val toolbarHeight = 36.dp
    val iconButtonWidth = 24.dp
    val iconSize = 20.dp
    val gap = 8.dp
    val radius = 8.dp
}

/** Exact 24x24 glyphs copied from the live AFFiNE toolbar. */
object OfficialTextPaths {
    val DisplayInPage = listOf(
        "M4.25 6A2.75 2.75 0 0 1 7 3.25h10A2.75 2.75 0 0 1 19.75 6v6a.75.75 0 0 1-1.5 0V6c0-.69-.56-1.25-1.25-1.25H7c-.69 0-1.25.56-1.25 1.25v12c0 .69.56 1.25 1.25 1.25h5a.75.75 0 0 1 0 1.5H7A2.75 2.75 0 0 1 4.25 18zm4 2A.75.75 0 0 1 9 7.25h3a.75.75 0 0 1 0 1.5H9A.75.75 0 0 1 8.25 8m0 3.5a.75.75 0 0 1 .75-.75h6a.75.75 0 0 1 0 1.5H9a.75.75 0 0 1-.75-.75m9.427 2.288a.75.75 0 0 1 0 1.06l-1.902 1.902h5.26a.75.75 0 0 1 0 1.5h-5.26l1.902 1.902a.75.75 0 0 1-1.06 1.06l-3.183-3.182a.75.75 0 0 1 0-1.06l3.182-3.182a.75.75 0 0 1 1.06 0M8.25 15a.75.75 0 0 1 .75-.75h2.5a.75.75 0 0 1 0 1.5H9a.75.75 0 0 1-.75-.75"
    )
    val Palette = listOf(
        "M12 2.25c-5.385 0-9.75 4.365-9.75 9.75s4.365 9.75 9.75 9.75a3.9 3.9 0 0 0 3.9-3.9v-.585c0-.4.003-.486.012-.543a.975.975 0 0 1 .81-.81c.057-.009.143-.012.543-.012h.585a3.9 3.9 0 0 0 3.9-3.9c0-5.385-4.365-9.75-9.75-9.75M9.075 7.613a1.462 1.462 0 1 1 2.925 0 1.462 1.462 0 0 1-2.925 0m4.875.974a1.463 1.463 0 1 1 2.925 0 1.463 1.463 0 0 1-2.925 0M7.613 10.05a1.463 1.463 0 1 0 0 2.925 1.463 1.463 0 0 0 0-2.925"
    )
    val Size = listOf(
        "M3.25 12a.75.75 0 0 1 .75-.75h16a.75.75 0 0 1 0 1.5H4a.75.75 0 0 1-.75-.75M12.53 10.53a.75.75 0 0 1-1.06 0l-3-3a.75.75 0 0 1 1.06-1.06L12 8.94l2.47-2.47a.75.75 0 1 1 1.06 1.06zM15.53 17.53a.75.75 0 0 1-1.06 0L12 15.06l-2.47 2.47a.75.75 0 0 1-1.06-1.06l3-3a.75.75 0 0 1 1.06 0l3 3a.75.75 0 0 1 0 1.06",
        "M12 2.25a.75.75 0 0 1 .75.75v6a.75.75 0 0 1-1.5 0V3a.75.75 0 0 1 .75-.75M11.25 21v-7h1.5v7a.75.75 0 0 1-1.5 0"
    )
    val Connector = listOf(
        "M15.97 2.02a.75.75 0 0 1 1.06 0l4.88 4.879a.85.85 0 0 1 0 1.202l-4.88 4.88a.75.75 0 1 1-1.06-1.062l3.67-3.669H18c-3.151 0-4.552.758-5.25 1.662-.745.963-.873 2.28-1.002 3.898l-.008.101c-.119 1.486-.259 3.245-1.303 4.595C9.323 19.946 7.349 20.75 4 20.75a.75.75 0 0 1 0-1.5c3.151 0 4.552-.758 5.25-1.662.745-.963.873-2.28 1.002-3.898l.008-.101c.119-1.486.259-3.245 1.303-4.595C12.677 7.554 14.651 6.75 18 6.75h1.64l-3.67-3.67a.75.75 0 0 1 0-1.06"
    )
    val ChevronDown = listOf(
        "M17.58 8.97a.75.75 0 0 1 0 1.06l-5 5a.75.75 0 0 1-1.06 0l-5-5a.75.75 0 1 1 1.06-1.06l4.47 4.47 4.47-4.47a.75.75 0 0 1 1.06 0"
    )
}

@Composable
fun SvgPathIcon(paths: List<String>, tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier) {
        val factor = size.width / 24f
        withTransform({ scale(factor, factor, pivot = Offset.Zero) }) {
            paths.forEach { data ->
                runCatching {
                    PathParser().parsePathString(data).toPath().also { it.fillType = PathFillType.EvenOdd }
                }.getOrNull()?.let { path -> drawPath(path, tint) }
            }
        }
    }
}

@Composable
fun TextToolbarScaleButton(label: String, onClick: () -> Unit) {
    Row(
        Modifier.width(65.dp).height(TextToolbarTokens.toolbarHeight)
            .clip(RoundedCornerShape(6.dp)).clickable { onClick() }
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = LingyeColors.Ink)
        Spacer(Modifier.width(2.dp))
        SvgPathIcon(OfficialTextPaths.ChevronDown, LingyeColors.Ink, Modifier.size(16.dp))
    }
}

@Composable
fun TextToolbarSeparator() {
    Box(Modifier.width(4.dp).height(36.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.width(1.dp).height(20.dp).background(LingyeColors.Line.copy(alpha = 0.85f)))
    }
}

@Composable
fun TextToolbarIconButton(
    description: String,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable (Color) -> Unit
) {
    val tint = if (enabled) LingyeColors.Ink else LingyeColors.Muted.copy(alpha = 0.35f)
    Box(
        Modifier
            .size(TextToolbarTokens.iconButtonWidth, TextToolbarTokens.toolbarHeight)
            .clickable(enabled = enabled) { onClick() }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(TextToolbarTokens.iconButtonWidth, 28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (selected && enabled) LingyeColors.Subtle else Color.Transparent)
        )
        content(tint)
    }
}

@Composable
fun TextToolbarLabelButton(
    description: String,
    label: String,
    icon: (@Composable (Color) -> Unit)? = null,
    width: Dp? = null,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .height(TextToolbarTokens.toolbarHeight)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 2.dp)
            .semantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            icon(LingyeColors.Ink)
            Spacer(Modifier.width(4.dp))
        }
        Text(label, fontSize = 14.sp, color = LingyeColors.Ink, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
    }
}

enum class TextToolGlyphKind { Palette, Size, Connector, Highlight, LinkedDoc, Bold, Code, Equation, Italic, Link, Strike, Underline, Align, More }

@Composable
fun TextToolGlyph(kind: TextToolGlyphKind, tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    when (kind) {
        TextToolGlyphKind.Bold -> Text("B", modifier, color = tint, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        TextToolGlyphKind.Italic -> Text("I", modifier, color = tint, fontSize = 15.sp, fontWeight = FontWeight.Medium, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, textAlign = TextAlign.Center)
        TextToolGlyphKind.Underline -> Text("U", modifier, color = tint, fontSize = 15.sp, fontWeight = FontWeight.Medium, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline, textAlign = TextAlign.Center)
        TextToolGlyphKind.Strike -> Text("S", modifier, color = tint, fontSize = 15.sp, fontWeight = FontWeight.Medium, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough, textAlign = TextAlign.Center)
        TextToolGlyphKind.Code -> Text("</>", modifier, color = tint, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        TextToolGlyphKind.Equation -> Text("Σ", modifier, color = tint, fontSize = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        TextToolGlyphKind.Palette -> SvgPathIcon(OfficialTextPaths.Palette, tint, modifier)
        TextToolGlyphKind.Size -> SvgPathIcon(OfficialTextPaths.Size, tint, modifier)
        TextToolGlyphKind.Connector -> SvgPathIcon(OfficialTextPaths.Connector, tint, modifier)
        TextToolGlyphKind.Highlight -> Canvas(modifier) {
            val stroke = 1.5.dp.toPx()
            drawLine(tint, Offset(size.width * 0.25f, size.height * 0.62f), Offset(size.width * 0.65f, size.height * 0.22f), stroke, StrokeCap.Round)
            drawLine(tint, Offset(size.width * 0.22f, size.height * 0.74f), Offset(size.width * 0.42f, size.height * 0.82f), stroke, StrokeCap.Round)
            drawLine(tint.copy(alpha = 0.45f), Offset(size.width * 0.2f, size.height * 0.86f), Offset(size.width * 0.72f, size.height * 0.86f), 3.dp.toPx(), StrokeCap.Round)
        }
        TextToolGlyphKind.LinkedDoc -> Canvas(modifier) {
            val stroke = 1.5.dp.toPx()
            drawRoundRect(tint, Offset(size.width * 0.2f, size.height * 0.12f), Size(size.width * 0.5f, size.height * 0.7f), CornerRadius(2.dp.toPx()), style = Stroke(stroke))
            drawLine(tint, Offset(size.width * 0.55f, size.height * 0.82f), Offset(size.width * 0.82f, size.height * 0.55f), stroke, StrokeCap.Round)
            drawCircle(tint, 4.dp.toPx(), Offset(size.width * 0.55f, size.height * 0.82f), style = Stroke(stroke))
            drawCircle(tint, 4.dp.toPx(), Offset(size.width * 0.82f, size.height * 0.55f), style = Stroke(stroke))
        }
        TextToolGlyphKind.Link -> Canvas(modifier) {
            val stroke = 1.5.dp.toPx()
            drawLine(tint, Offset(size.width * 0.32f, size.height * 0.64f), Offset(size.width * 0.68f, size.height * 0.36f), stroke, StrokeCap.Round)
            drawCircle(tint, 5.dp.toPx(), Offset(size.width * 0.3f, size.height * 0.68f), style = Stroke(stroke))
            drawCircle(tint, 5.dp.toPx(), Offset(size.width * 0.7f, size.height * 0.32f), style = Stroke(stroke))
        }
        TextToolGlyphKind.Align -> OfficialTextAlignGlyph("left", tint, modifier)
        TextToolGlyphKind.More -> Canvas(modifier) {
            repeat(3) { i -> drawCircle(tint, 1.6.dp.toPx(), Offset(size.width * 0.5f, size.height * (0.3f + i * 0.2f))) }
        }
    }
}

@Composable
fun OfficialTextAlignGlyph(align: String, tint: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier) {
        val stroke = 1.6.dp.toPx()
        val gap = 3.dp.toPx()
        listOf(1f, 0.68f, 0.86f).forEachIndexed { index, fraction ->
            val width = size.width * fraction
            val x = when (align) {
                "center" -> (size.width - width) / 2f
                "right" -> size.width - width
                else -> 0f
            }
            val y = gap + index * ((size.height - gap * 2f) / 2f)
            drawLine(tint, Offset(x, y), Offset(x + width, y), stroke, StrokeCap.Round)
        }
    }
}

@Composable
fun OfficialTextVerticalAlignGlyph(align: String, tint: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier) {
        val line = 1.2.dp.toPx()
        val frame = 1.dp.toPx()
        drawRoundRect(
            tint.copy(alpha = 0.35f),
            Offset(frame, frame),
            Size(size.width - frame * 2f, size.height - frame * 2f),
            CornerRadius(2.5.dp.toPx()),
            style = Stroke(frame)
        )
        val y = when (align) {
            "middle" -> size.height / 2f
            "bottom" -> size.height - 2.5.dp.toPx()
            else -> 2.5.dp.toPx()
        }
        drawLine(tint, Offset(3.dp.toPx(), y), Offset(size.width - 3.dp.toPx(), y), line, StrokeCap.Round)
    }
}

@Composable
fun TextToolbarMenuButton(
    description: String,
    width: Dp,
    icon: List<String>,
    onClick: () -> Unit
) {
    Row(
        Modifier.width(width).height(36.dp)
            .clip(RoundedCornerShape(6.dp)).clickable { onClick() }
            .semantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        SvgPathIcon(icon, LingyeColors.Ink, Modifier.size(20.dp))
        Spacer(Modifier.width(2.dp))
        SvgPathIcon(OfficialTextPaths.ChevronDown, LingyeColors.Muted, Modifier.size(16.dp))
    }
}

@Composable
fun TextToolbarPopup(
    expanded: Boolean,
    width: Dp = 198.dp,
    gap: Dp = 5.dp,
    xShift: Dp = 0.dp,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!expanded) return
    val density = LocalDensity.current
    val yPx = with(density) { (36.dp + gap).roundToPx() }
    val xPx = with(density) { xShift.roundToPx() }
    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(xPx, yPx),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            Modifier.width(width),
            shape = RoundedCornerShape(8.dp),
            color = LingyeColors.SurfaceHigh,
            border = BorderStroke(1.dp, LingyeColors.Line),
            shadowElevation = 8.dp
        ) {
            Column(Modifier.padding(8.dp), content = content)
        }
    }
}

@Composable
fun TextToolbarMenuHeading(label: String) {
    Text(
        label,
        Modifier.height(22.dp).padding(horizontal = 8.dp),
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = LingyeColors.Muted
    )
}

@Composable
fun TextToolbarMenuItem(
    label: String,
    icon: List<String>? = null,
    iconTint: Color = LingyeColors.Muted,
    selected: Boolean = false,
    enabled: Boolean = true,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val color = when {
        !enabled -> LingyeColors.Muted.copy(alpha = 0.45f)
        danger -> LingyeColors.Coral
        else -> LingyeColors.Ink
    }
    Row(
        Modifier.width(176.dp).height(30.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected && enabled) LingyeColors.Subtle else Color.Transparent)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            SvgPathIcon(icon, if (enabled) iconTint else color, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(label, fontSize = 14.sp, color = color, maxLines = 1)
    }
}

@Composable
fun TextToolbarHighlightSwatch(color: Color) {
    Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
        Text("A", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
fun TextToolbarColorMenuItem(
    label: String,
    color: Color,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        Modifier.width(176.dp).height(30.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) LingyeColors.Subtle else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextToolbarHighlightSwatch(color)
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, color = LingyeColors.Ink, maxLines = 1)
    }
}

@Composable
private fun TextToolbarAnchoredMenu(
    expanded: Boolean,
    width: Dp = 198.dp,
    gap: Dp = 5.dp,
    xShift: Dp = 0.dp,
    onDismissRequest: () -> Unit,
    button: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box {
        button()
        TextToolbarPopup(expanded, width, gap, xShift, onDismissRequest, content)
    }
}

@Composable
fun TextSelectionToolbar(
    language: AppLanguage,
    bold: Boolean,
    italic: Boolean,
    underline: Boolean,
    strike: Boolean,
    code: Boolean,
    styleMenuExpanded: Boolean,
    onDismissStyleMenu: () -> Unit,
    styleMenu: @Composable ColumnScope.() -> Unit,
    alignMenuExpanded: Boolean,
    onDismissAlignMenu: () -> Unit,
    alignMenu: @Composable ColumnScope.() -> Unit,
    highlightMenuExpanded: Boolean,
    onDismissHighlightMenu: () -> Unit,
    highlightMenu: @Composable ColumnScope.() -> Unit,
    equationPopoverExpanded: Boolean,
    onDismissEquationPopover: () -> Unit,
    equationPopover: @Composable ColumnScope.() -> Unit,
    commentPopoverExpanded: Boolean,
    onDismissCommentPopover: () -> Unit,
    commentPopover: @Composable ColumnScope.() -> Unit,
    linkMenuExpanded: Boolean,
    onDismissLinkMenu: () -> Unit,
    linkMenu: @Composable ColumnScope.() -> Unit,
    linkedMenuExpanded: Boolean,
    onDismissLinkedMenu: () -> Unit,
    linkedMenu: @Composable ColumnScope.() -> Unit,
    moreMenuExpanded: Boolean,
    onDismissMoreMenu: () -> Unit,
    moreMenu: @Composable ColumnScope.() -> Unit,
    onTextStyle: () -> Unit,
    onAlignment: () -> Unit,
    onBold: () -> Unit,
    onCode: () -> Unit,
    onEquation: () -> Unit,
    onItalic: () -> Unit,
    onLink: () -> Unit,
    onStrike: () -> Unit,
    onUnderline: () -> Unit,
    onHighlight: () -> Unit,
    onLinkedDoc: () -> Unit,
    onComment: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        Modifier.height(38.dp).padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextToolbarAnchoredMenu(
            expanded = styleMenuExpanded,
            onDismissRequest = onDismissStyleMenu,
            button = { TextToolbarMenuButton(tr(language, "文字样式", "Text style"), 40.dp, InlineToolbarIcons.Text) { onTextStyle() } },
            content = styleMenu
        )
        TextToolbarSeparator()
        TextToolbarAnchoredMenu(
            expanded = alignMenuExpanded,
            onDismissRequest = onDismissAlignMenu,
            button = { TextToolbarMenuButton(tr(language, "对齐", "Alignment"), 40.dp, InlineToolbarIcons.AlignLeft) { onAlignment() } },
            content = alignMenu
        )
        TextToolbarSeparator()
        TextToolbarIconButton(tr(language, "粗体", "Bold"), selected = bold, onClick = { onBold() }) { tint -> SvgPathIcon(InlineToolbarIcons.Bold, tint) }
        TextToolbarIconButton(tr(language, "代码", "Code"), selected = code, onClick = { onCode() }) { tint -> SvgPathIcon(InlineToolbarIcons.Code, tint) }
        TextToolbarAnchoredMenu(
            expanded = equationPopoverExpanded,
            width = 350.dp,
            onDismissRequest = onDismissEquationPopover,
            button = { TextToolbarIconButton(tr(language, "行内公式", "Inline Equation"), onClick = { onEquation() }) { tint -> SvgPathIcon(InlineToolbarIcons.Equation, tint) } },
            content = equationPopover
        )
        TextToolbarIconButton(tr(language, "斜体", "Italic"), selected = italic, onClick = { onItalic() }) { tint -> SvgPathIcon(InlineToolbarIcons.Italic, tint) }
        TextToolbarAnchoredMenu(
            expanded = linkMenuExpanded,
            width = 350.dp,
            gap = 41.dp,
            onDismissRequest = onDismissLinkMenu,
            button = { TextToolbarIconButton(tr(language, "链接", "Link"), onClick = { onLink() }) { tint -> SvgPathIcon(InlineToolbarIcons.Link, tint) } },
            content = linkMenu
        )
        TextToolbarIconButton(tr(language, "删除线", "Strikethrough"), selected = strike, onClick = { onStrike() }) { tint -> SvgPathIcon(InlineToolbarIcons.Strike, tint) }
        TextToolbarIconButton(tr(language, "下划线", "Underline"), selected = underline, onClick = { onUnderline() }) { tint -> SvgPathIcon(InlineToolbarIcons.Underline, tint) }
        TextToolbarSeparator()
        TextToolbarAnchoredMenu(
            expanded = highlightMenuExpanded,
            onDismissRequest = onDismissHighlightMenu,
            button = { TextToolbarMenuButton(tr(language, "高亮", "Highlight"), 40.dp, InlineToolbarIcons.Highlighter) { onHighlight() } },
            content = highlightMenu
        )
        TextToolbarSeparator()
        TextToolbarAnchoredMenu(
            expanded = linkedMenuExpanded,
            onDismissRequest = onDismissLinkedMenu,
            button = { TextToolbarIconButton(tr(language, "创建链接文档", "Create Linked Doc"), onClick = { onLinkedDoc() }) { tint -> SvgPathIcon(InlineToolbarIcons.LinkedDoc, tint) } },
            content = linkedMenu
        )
        TextToolbarAnchoredMenu(
            expanded = commentPopoverExpanded,
            width = 320.dp,
            onDismissRequest = onDismissCommentPopover,
            button = { TextToolbarIconButton(tr(language, "评论", "Comment"), onClick = { onComment() }) { tint -> SvgPathIcon(InlineToolbarIcons.Comment, tint) } },
            content = commentPopover
        )
        TextToolbarSeparator()
        TextToolbarAnchoredMenu(
            expanded = moreMenuExpanded,
            onDismissRequest = onDismissMoreMenu,
            button = { TextToolbarIconButton(tr(language, "更多", "More"), onClick = { onMore() }) { tint -> SvgPathIcon(InlineToolbarIcons.More, tint) } },
            content = moreMenu
        )
    }
}

@Composable
fun TextToolbarSectionTitle(label: String) {
    Text(label, Modifier.height(22.dp), fontSize = 14.sp, fontWeight = FontWeight.Normal, color = LingyeColors.Muted)
}

private fun notePalette(): List<Int> = if (LingyeColors.dark) listOf(
    0xff460606.toInt(), 0xff843b06.toInt(), 0xff704200.toInt(), 0xff3b5315.toInt(),
    0xff004b7b.toInt(), 0xff312e81.toInt(), 0xff5f0a35.toInt(), 0xff252525.toInt()
) else listOf(
    0xfff3b6b6.toInt(), 0xfff4c28d.toInt(), 0xfff2d66f.toInt(), 0xffb8dfb4.toInt(),
    0xffb5d8f1.toInt(), 0xffc8b9f2.toInt(), 0xfff1b7dc.toInt(), 0xffffffff.toInt()
)

@Composable
fun NoteStylePanel(
    fillColor: Int,
    shadowStyle: String,
    borderWidth: Float,
    borderStyle: String,
    cornerRadius: Float,
    language: AppLanguage,
    onFill: (Int) -> Unit,
    onShadow: (String) -> Unit,
    onBorderWidth: (Float) -> Unit,
    onBorderStyle: (String) -> Unit,
    onCornerRadius: (Float) -> Unit
) {
    var customFill by remember { mutableStateOf(false) }
    var radiusText by remember(cornerRadius) { mutableStateOf(cornerRadius.roundToInt().toString()) }
    val palette = notePalette()
    val transparent = Color(fillColor).alpha == 0f
    Column(Modifier.width(292.dp).padding(horizontal = 8.dp, vertical = 4.dp)) {
        TextToolbarSectionTitle(tr(language, "填充颜色", "Fill color"))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            palette.forEachIndexed { index, color ->
                val active = !transparent && sameRgb(fillColor, color)
                Box(
                    Modifier.size(24.dp).clip(CircleShape)
                        .background(Color(color))
                        .border(if (active) 2.dp else 1.dp, if (active) LingyeColors.Primary else LingyeColors.Line, CircleShape)
                        .clickable { onFill(color) }
                        .semantics { contentDescription = tr(language, "填充颜色", "Fill color") + " ${index + 1}" }
                )
            }
            Box(
                Modifier.size(24.dp).clip(CircleShape)
                    .border(if (transparent) 2.dp else 1.dp, if (transparent) LingyeColors.Primary else LingyeColors.Line, CircleShape)
                    .clickable { onFill(0x00ffffff) }
                    .semantics { contentDescription = tr(language, "透明", "Transparent") },
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(18.dp).clip(CircleShape)) {
                    drawRect(Color.White)
                    val cell = size.width / 2f
                    for (row in 0..1) for (column in 0..1) {
                        if ((row + column) % 2 == 0) drawRect(Color(0xffdedede), Offset(column * cell, row * cell), Size(cell, cell))
                    }
                }
            }
            Box(
                Modifier.size(24.dp).clip(CircleShape).clickable { customFill = true }
                    .semantics { contentDescription = tr(language, "自定义颜色", "Custom color") },
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    drawCircle(
                        Brush.sweepGradient(
                            listOf(
                                Color(0xffef7076), Color(0xfff08c43), Color(0xffe8c64b),
                                Color(0xff57b85e), Color(0xff79b8ef), Color(0xff6656d9),
                                Color(0xffcf69b4), Color(0xffef7076)
                            )
                        ),
                        radius,
                        center
                    )
                    drawCircle(Color.White, radius * 0.3f, center)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        TextToolbarSectionTitle(tr(language, "阴影", "Shadow"))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(
                "none" to tr(language, "无", "None"),
                "box" to tr(language, "盒状", "Box"),
                "sticker" to tr(language, "贴纸", "Sticker"),
                "paper" to tr(language, "纸张", "Paper"),
                "float" to tr(language, "悬浮", "Float"),
                "film" to tr(language, "胶片", "Film")
            ).forEach { (value, label) ->
                ShadowSample(value, label, shadowStyle == value) { onShadow(value) }
            }
        }
        Spacer(Modifier.height(8.dp))
        TextToolbarSectionTitle(tr(language, "边框", "Border"))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val borderValues = listOf(2f, 4f, 6f, 8f, 10f, 12f)
            Box(Modifier.width(168.dp).height(24.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val y = size.height / 2f
                    drawLine(LingyeColors.Line, Offset(8.dp.toPx(), y), Offset(size.width - 8.dp.toPx(), y), 1.dp.toPx())
                    borderValues.forEachIndexed { index, value ->
                        val x = 8.dp.toPx() + (size.width - 16.dp.toPx()) * index / (borderValues.size - 1).toFloat()
                        val active = borderWidth == value
                        drawCircle(if (active) LingyeColors.Ink else LingyeColors.Muted, if (active) 5.dp.toPx() else 3.5.dp.toPx(), Offset(x, y))
                    }
                }
                Row(Modifier.fillMaxSize()) {
                    borderValues.forEach { width ->
                        Box(
                            Modifier.weight(1f).fillMaxHeight().clickable {
                                onBorderWidth(width)
                                if (borderStyle == "none") onBorderStyle("solid")
                            }.semantics { contentDescription = width.roundToInt().toString() }
                        )
                    }
                }
            }
            listOf("solid" to "solid", "dashed" to "dashed", "none" to "none").forEach { (value, _) ->
                Box(
                    Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)).clickable { onBorderStyle(value) }
                        .background(if (borderStyle == value) LingyeColors.Subtle else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.size(16.dp, 10.dp)) {
                        val y = size.height / 2f
                        when (value) {
                            "none" -> {
                                drawLine(LingyeColors.Muted.copy(alpha = 0.5f), Offset(1f, y), Offset(size.width - 1f, y), 1.2.dp.toPx())
                                drawLine(LingyeColors.Muted, Offset(1f, 1f), Offset(size.width - 1f, size.height - 1f), 1.4.dp.toPx())
                            }
                            "dashed" -> drawLine(LingyeColors.Ink, Offset(1f, y), Offset(size.width - 1f, y), 1.4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())))
                            else -> drawLine(LingyeColors.Ink, Offset(1f, y), Offset(size.width - 1f, y), 1.4.dp.toPx())
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        TextToolbarSectionTitle(tr(language, "圆角", "Corner radius"))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val radiusValues = listOf(0, 4, 8, 16, 24, 32)
            Box(Modifier.width(168.dp).height(24.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val y = size.height / 2f
                    drawLine(LingyeColors.Line, Offset(8.dp.toPx(), y), Offset(size.width - 8.dp.toPx(), y), 1.dp.toPx())
                    radiusValues.forEachIndexed { index, value ->
                        val x = 8.dp.toPx() + (size.width - 16.dp.toPx()) * index / (radiusValues.size - 1).toFloat()
                        val active = cornerRadius.roundToInt() == value
                        drawCircle(if (active) LingyeColors.Ink else LingyeColors.Muted, if (active) 5.dp.toPx() else 3.5.dp.toPx(), Offset(x, y))
                    }
                }
                Row(Modifier.fillMaxSize()) {
                    radiusValues.forEach { value ->
                        Box(
                            Modifier.weight(1f).fillMaxHeight().clickable { onCornerRadius(value.toFloat()) }
                                .semantics { contentDescription = value.toString() }
                        )
                    }
                }
            }
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier.width(88.dp).height(24.dp)
                    .background(LingyeColors.Surface, RoundedCornerShape(4.dp))
                    .border(0.5.dp, LingyeColors.Line, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    radiusText,
                    { value ->
                        if (value.length <= 2 && value.all(Char::isDigit)) {
                            radiusText = value
                            value.toIntOrNull()?.let { if (it <= 32) onCornerRadius(it.toFloat()) }
                        }
                    },
                    Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = LingyeColors.Ink)
                )
            }
        }
    }
    if (customFill) {
        ColorEditorDialog(
            initialColor = if (transparent) palette[7] else fillColor,
            onDismiss = { customFill = false },
            onConfirm = { onFill(it); customFill = false }
        )
    }
}

@Composable
private fun ShadowSample(style: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.width(42.dp).height(50.dp).clip(RoundedCornerShape(4.dp))
            .border(if (selected) 1.dp else 0.dp, if (selected) LingyeColors.Primary else Color.Transparent, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .semantics { contentDescription = label },
        contentAlignment = Alignment.TopCenter
    ) {
        Box(Modifier.size(30.dp, 36.dp).padding(top = 5.dp), contentAlignment = Alignment.Center) {
            val shadow = when (style) {
                "none" -> Color.Transparent
                "box" -> Color.Black.copy(alpha = 0.12f)
                "sticker" -> Color.Black.copy(alpha = 0.20f)
                "paper" -> Color.Black.copy(alpha = 0.10f)
                "float" -> Color.Black.copy(alpha = 0.16f)
                else -> Color.Black.copy(alpha = 0.25f)
            }
            if (style != "none") Box(Modifier.offset(y = if (style == "sticker" || style == "film") 4.dp else 2.dp).size(24.dp, 28.dp).background(shadow, RoundedCornerShape(3.dp)))
            Box(
                Modifier.size(24.dp, 28.dp).background(LingyeColors.SurfaceHigh, RoundedCornerShape(3.dp))
                    .border(
                        when (style) {
                            "paper" -> 2.dp
                            "film" -> 1.dp
                            "none" -> 0.5.dp
                            else -> 0.5.dp
                        },
                        when (style) {
                            "none" -> LingyeColors.Line
                            "film" -> Color.Black
                            "paper" -> LingyeColors.Muted.copy(alpha = 0.55f)
                            else -> LingyeColors.Muted.copy(alpha = 0.4f)
                        },
                        RoundedCornerShape(3.dp)
                    )
            )
            if (style == "none") {
                Canvas(Modifier.size(24.dp, 28.dp)) {
                    drawCircle(LingyeColors.Muted, 5.dp.toPx(), center, style = Stroke(1.dp.toPx()))
                    drawLine(LingyeColors.Muted, Offset(size.width * 0.28f, size.height * 0.72f), Offset(size.width * 0.72f, size.height * 0.28f), 1.dp.toPx())
                }
            }
        }
    }
}

@Composable
fun TextMenuRow(
    label: String,
    enabled: Boolean = true,
    selected: Boolean = false,
    danger: Boolean = false,
    icon: (@Composable (Color) -> Unit)? = null,
    onClick: () -> Unit
) {
    val color = when {
        danger -> LingyeColors.Coral
        enabled -> LingyeColors.Ink
        else -> LingyeColors.Muted.copy(alpha = 0.45f)
    }
    Row(
        Modifier.fillMaxWidth().height(30.dp)
            .background(if (selected) LingyeColors.Subtle else Color.Transparent)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            icon(color)
            Spacer(Modifier.width(8.dp))
        }
        Text(label, fontSize = 13.sp, color = color, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
    }
}

@Composable
fun TextMenuGroupDivider() {
    Spacer(Modifier.height(8.dp))
    HorizontalDivider(color = LingyeColors.Line.copy(alpha = 0.55f))
    Spacer(Modifier.height(8.dp))
}
