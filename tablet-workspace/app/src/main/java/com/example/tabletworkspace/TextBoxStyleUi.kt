package com.example.tabletworkspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

internal fun textScalePercent(shape: Shape): Int =
    Regex("(?:^|;)scale:(\\d+)").find(shape.cardKind)?.groupValues?.getOrNull(1)?.toIntOrNull()?.coerceIn(25, 400) ?: 100

internal fun textVerticalAlign(shape: Shape): String =
    Regex("(?:^|;)valign:(top|middle|bottom)(?:;|$)").find(shape.cardKind)?.groupValues?.getOrNull(1) ?: "top"

internal fun textDisplayInPage(shape: Shape): Boolean =
    Regex("(?:^|;)page:1(?:;|$)").containsMatchIn(shape.cardKind)

internal fun textAutoHeight(shape: Shape): Boolean =
    Regex("(?:^|;)autoheight:1(?:;|$)").containsMatchIn(shape.cardKind)

internal fun textStoredHeight(shape: Shape): Float? =
    Regex("(?:^|;)h:([0-9.]+)(?:;|$)").find(shape.cardKind)?.groupValues?.getOrNull(1)?.toFloatOrNull()

internal fun textCardKind(
    scale: Int,
    verticalAlign: String,
    displayInPage: Boolean = false,
    autoHeight: Boolean = false,
    storedHeight: Float? = null,
    preserved: String = ""
): String =
    ("scale:${scale.coerceIn(25, 400)}" +
        (if (verticalAlign == "top") "" else ";valign:$verticalAlign") +
        (if (displayInPage) ";page:1" else "") +
        (if (autoHeight) ";autoheight:1" + (storedHeight?.let { ";h:${it.roundToInt()}" } ?: "") else "") +
        preserved)

internal fun autoFitTextHeight(body: String, width: Float, fontSize: Float): Float {
    val text = body.ifEmpty { " " }
    val innerWidth = (width - 48f).coerceAtLeast(40f)
    val lines = TextFormatting.wrap(text, innerWidth) { index ->
        val cp = text.codePointAt(index)
        if (cp >= 0x2e80) fontSize else fontSize * 0.58f
    }
    return (lines.size.coerceAtLeast(1) * fontSize * 1.25f + 48f).coerceIn(88f, 4000f)
}

internal fun toggleTextAutoHeight(page: Page, index: Int, changed: () -> Unit) {
    val shape = page.shapes.getOrNull(index) ?: return
    val scale = textScalePercent(shape)
    val valign = textVerticalAlign(shape)
    val pageDisplay = textDisplayInPage(shape)
    if (textAutoHeight(shape)) {
        page.shapes[index] = shape.copy(
            height = (textStoredHeight(shape) ?: shape.height).coerceIn(40f, 4000f),
            cardKind = textCardKind(scale, valign, pageDisplay, autoHeight = false, preserved = TextExtras.tokens(shape.cardKind))
        )
    } else {
        page.shapes[index] = shape.copy(
            height = autoFitTextHeight(shape.body, shape.width, shape.fontSize),
            cardKind = textCardKind(scale, valign, pageDisplay, autoHeight = true, storedHeight = shape.height, preserved = TextExtras.tokens(shape.cardKind))
        )
    }
    changed()
}

internal fun applyTextScale(page: Page, index: Int, percent: Int, changed: () -> Unit) {
    val shape = page.shapes.getOrNull(index) ?: return
    val current = textScalePercent(shape)
    val factor = percent.toFloat() / current.toFloat()
    val nextFont = (shape.fontSize * factor).coerceIn(8f, 96f)
    val nextWidth = (shape.width * factor).coerceIn(40f, 4000f)
    val auto = textAutoHeight(shape)
    val stored = textStoredHeight(shape)?.let { (it * factor).coerceIn(40f, 4000f) }
    page.shapes[index] = shape.copy(
        fontSize = nextFont,
        width = nextWidth,
        height = if (auto) autoFitTextHeight(shape.body, nextWidth, nextFont) else (shape.height * factor).coerceIn(40f, 4000f),
        cardKind = textCardKind(percent, textVerticalAlign(shape), textDisplayInPage(shape), auto, stored, TextExtras.tokens(shape.cardKind))
    )
    changed()
}

internal fun applyTextVerticalAlign(page: Page, index: Int, verticalAlign: String, changed: () -> Unit) {
    val shape = page.shapes.getOrNull(index) ?: return
    page.shapes[index] = shape.copy(
        cardKind = textCardKind(
            textScalePercent(shape), verticalAlign, textDisplayInPage(shape),
            textAutoHeight(shape), textStoredHeight(shape), TextExtras.tokens(shape.cardKind)
        )
    )
    changed()
}

internal fun applyTextDisplayInPage(page: Page, index: Int, display: Boolean, changed: () -> Unit) {
    val shape = page.shapes.getOrNull(index) ?: return
    page.shapes[index] = shape.copy(
        cardKind = textCardKind(
            textScalePercent(shape), textVerticalAlign(shape), display,
            textAutoHeight(shape), textStoredHeight(shape), TextExtras.tokens(shape.cardKind)
        )
    )
    changed()
}

internal fun readableTextColor(fill: Int, fallback: Int): Int {
    if (Color(fill).alpha == 0f) return fallback
    val r = (fill shr 16) and 0xff
    val g = (fill shr 8) and 0xff
    val b = fill and 0xff
    val luminance = (0.2126f * r + 0.7152f * g + 0.0722f * b) / 255f
    return if (luminance < 0.5f) 0xfff2f2f2.toInt() else 0xff20293a.toInt()
}

@Composable
internal fun TextStyleSectionTitle(label: String) {
    Text(label, Modifier.padding(bottom = 7.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = LingyeColors.Muted)
}

@Composable
internal fun TextFormatChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        shape = RoundedCornerShape(9.dp),
        color = if (selected) LingyeColors.SegmentSelected else Color.Transparent,
        border = BorderStroke(1.dp, if (selected) Color.Transparent else LingyeColors.Line)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) LingyeColors.OnSegmentSelected else LingyeColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun TextAdjacentPlus(x: Dp, y: Dp, description: String, onClick: () -> Unit) {
    Box(
        Modifier.offset(x = x, y = y).size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(20.dp)) {
            val stroke = 1.6.dp.toPx()
            drawLine(LingyeColors.Muted, Offset(size.width / 2f, 2.dp.toPx()), Offset(size.width / 2f, size.height - 2.dp.toPx()), stroke, StrokeCap.Round)
            drawLine(LingyeColors.Muted, Offset(2.dp.toPx(), size.height / 2f), Offset(size.width - 2.dp.toPx(), size.height / 2f), stroke, StrokeCap.Round)
        }
    }
}

@Composable
internal fun TextAlignGlyph(align: String, tint: Color, modifier: Modifier = Modifier.size(18.dp)) {
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
internal fun TextVerticalAlignGlyph(align: String, tint: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier) {
        val line = 1.2.dp.toPx()
        val frame = 1.dp.toPx()
        drawRoundRect(
            tint.copy(alpha = 0.35f),
            Offset(frame, frame),
            Size(size.width - frame * 2f, size.height - frame * 2f),
            androidx.compose.ui.geometry.CornerRadius(2.5.dp.toPx()),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FillColorSwatches(selected: Int, language: AppLanguage, onPick: (Int) -> Unit) {
    var editorOpen by remember { mutableStateOf(false) }
    val transparent = Color(selected).alpha == 0f
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            Modifier.size(30.dp).clip(CircleShape).clickable { onPick(0x00ffffff) }
                .border(if (transparent) 2.dp else 1.dp, if (transparent) LingyeColors.Primary else LingyeColors.Line, CircleShape)
                .semantics { contentDescription = tr(language, "透明填充", "Transparent fill") },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(20.dp).clip(CircleShape)) {
                drawRect(Color.White)
                val cell = size.width / 2f
                for (row in 0..1) for (column in 0..1) {
                    if ((row + column) % 2 == 0) drawRect(Color(0xffdedede), Offset(column * cell, row * cell), Size(cell, cell))
                }
            }
        }
        ColorPalette.CLASSIC.forEachIndexed { index, color ->
            ColorSwatch(color, !transparent && sameRgb(selected, color), tr(language, "填充颜色", "Fill color") + " ${index + 1}", 24.dp) { onPick(color) }
        }
        Box(
            Modifier.size(30.dp).clip(CircleShape).background(LingyeColors.Chip).clickable { editorOpen = true }
                .semantics { contentDescription = tr(language, "自定义填充颜色", "Custom fill color") },
            contentAlignment = Alignment.Center
        ) {
            Text("+", fontSize = 16.sp, color = LingyeColors.Muted, fontWeight = FontWeight.Medium)
        }
    }
    if (editorOpen) {
        ColorEditorDialog(
            initialColor = if (transparent) 0xffffffff.toInt() else selected,
            onDismiss = { editorOpen = false },
            onConfirm = { color -> onPick(color); editorOpen = false }
        )
    }
}

@Composable
internal fun ShadowOptionCard(style: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(94.dp).height(72.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) LingyeColors.Primary else LingyeColors.Line)
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(44.dp, 30.dp), contentAlignment = Alignment.Center) {
                if (style != "none") {
                    Box(
                        Modifier.offset(y = 3.dp).size(38.dp, 24.dp)
                            .background(Color.Black.copy(alpha = if (style == "sticker") 0.18f else 0.08f), RoundedCornerShape(5.dp))
                    )
                }
                Box(Modifier.size(38.dp, 24.dp).background(LingyeColors.SurfaceHigh, RoundedCornerShape(5.dp)).border(1.dp, LingyeColors.Line, RoundedCornerShape(5.dp)))
            }
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, color = if (selected) LingyeColors.Primary else LingyeColors.Muted)
        }
    }
}

@Composable
internal fun BorderWidthDot(value: Float, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(30.dp).clip(CircleShape).clickable { onClick() }
            .border(if (selected) 2.dp else 1.dp, if (selected) LingyeColors.Primary else LingyeColors.Line, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size((5f + value * 3f).dp).background(if (selected) LingyeColors.Primary else LingyeColors.Muted, CircleShape))
    }
}

@Composable
internal fun BorderStyleButton(style: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(38.dp, 30.dp).clip(RoundedCornerShape(7.dp))
            .background(if (selected) LingyeColors.Subtle else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(24.dp, 12.dp)) {
            val y = size.height / 2f
            when (style) {
                "none" -> {
                    drawLine(LingyeColors.Muted.copy(alpha = 0.45f), Offset(2f, y), Offset(size.width - 2f, y), 1.4.dp.toPx())
                    drawLine(LingyeColors.Muted, Offset(2f, 2f), Offset(size.width - 2f, size.height - 2f), 1.5.dp.toPx())
                }
                "dashed" -> drawLine(
                    LingyeColors.Ink, Offset(1f, y), Offset(size.width - 1f, y), 1.6.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
                )
                else -> drawLine(LingyeColors.Ink, Offset(1f, y), Offset(size.width - 1f, y), 1.6.dp.toPx())
            }
        }
    }
}

@Composable
internal fun TextBoxStylePanel(text: Shape, language: AppLanguage, onUpdate: (Shape) -> Unit) {
    var cornerText by remember(text.id) { mutableStateOf(text.cornerRadius.roundToInt().toString()) }
    LaunchedEffect(text.cornerRadius) {
        val value = text.cornerRadius.roundToInt()
        if (cornerText.toIntOrNull() != value) cornerText = value.toString()
    }
    Column(
        Modifier.width(440.dp).heightIn(max = 600.dp).verticalScroll(rememberScrollState()).padding(14.dp)
    ) {
        TextStyleSectionTitle(tr(language, "填充颜色", "Fill color"))
        FillColorSwatches(text.color, language) { onUpdate(text.copy(color = it)) }
        Spacer(Modifier.height(16.dp))
        TextStyleSectionTitle(tr(language, "文字颜色", "Text color"))
        ColorPaletteRow(
            selected = text.textColor,
            onPick = { onUpdate(text.copy(textColor = it)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        TextStyleSectionTitle(tr(language, "阴影", "Shadow"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShadowOptionCard("none", tr(language, "无", "None"), text.shadowStyle == "none") { onUpdate(text.copy(shadowStyle = "none")) }
            ShadowOptionCard("box", tr(language, "柔和", "Soft"), text.shadowStyle == "box" || text.shadowStyle !in setOf("none", "sticker")) { onUpdate(text.copy(shadowStyle = "box")) }
            ShadowOptionCard("sticker", tr(language, "贴纸", "Sticker"), text.shadowStyle == "sticker") { onUpdate(text.copy(shadowStyle = "sticker")) }
        }
        Spacer(Modifier.height(16.dp))
        TextStyleSectionTitle(tr(language, "边框", "Border"))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            listOf(0f, 1f, 2f, 4f).forEach { width ->
                BorderWidthDot(width, text.borderWidth == width) {
                    val style = when {
                        width == 0f -> "none"
                        text.borderStyle == "none" -> "solid"
                        else -> text.borderStyle
                    }
                    onUpdate(text.copy(borderWidth = width, borderStyle = style))
                }
            }
            Spacer(Modifier.width(10.dp))
            listOf("solid", "dashed", "none").forEach { style ->
                BorderStyleButton(style, text.borderStyle == style) {
                    val width = if (style == "none") text.borderWidth else text.borderWidth.coerceAtLeast(1f)
                    onUpdate(text.copy(borderStyle = style, borderWidth = width))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        TextStyleSectionTitle(tr(language, "圆角", "Corner radius"))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0, 8, 16, 24).forEach { radius ->
                TextFormatChip(radius.toString(), text.cornerRadius.roundToInt() == radius, Modifier.width(46.dp)) {
                    onUpdate(text.copy(cornerRadius = radius.toFloat()))
                }
            }
            OutlinedTextField(
                cornerText,
                { value ->
                    if (value.length <= 2 && value.all(Char::isDigit)) {
                        cornerText = value
                        value.toIntOrNull()?.let { number -> if (number <= 64) onUpdate(text.copy(cornerRadius = number.toFloat())) }
                    }
                },
                Modifier.width(76.dp),
                singleLine = true,
                label = { Text("px", fontSize = 10.sp) },
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
            )
        }
    }
}
