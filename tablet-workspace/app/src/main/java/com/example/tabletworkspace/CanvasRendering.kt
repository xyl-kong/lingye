@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.tabletworkspace

import com.example.tabletworkspace.core.ConnectorSampleCache
import com.example.tabletworkspace.core.TextLayoutCache
import com.example.tabletworkspace.core.TextLayoutKey
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope


internal fun DrawScope.drawCanvasContent(
    page: Page,
    pages: List<Page>,
    language: AppLanguage,
    settings: WorkspaceSettings,
    scale: Float,
    pan: Offset,
    density: Float,
    viewport: Viewport,
    canvasBitmaps: Map<String, ImageBitmap?>,
    canvasDarkTheme: Boolean,
    presentingFrameId: String?,
    revision: Int,
    points: List<Point>,
    tool: String,
    eraserArea: Boolean,
    activeInkColor: Int,
    activeInkWidth: Float,
    selection: CanvasSelection?,
    selectedItems: List<CanvasSelection>,
    dragging: Boolean,
    boxStart: Point?,
    boxEnd: Point?,
    connectorQuickSource: Int?,
    snapGuides: List<SnapGuide>,
    endpointPreview: Point?,
    connectorDraft: ConnectorDraft?,
    endpointDrag: Boolean?,
    endpointSnap: ConnectorSnapResult?,
    cardEdit: Int?,
    toolbarMenuOpen: Boolean,
    textEditValue: TextFieldValue?,
    motionEnabled: Boolean,
    motionTime: Long,
    systemMotion: Boolean
) {
            if (revision < 0) return
            val gridSpacing = settings.gridStep.dp.toPx() * scale
            if (settings.showGrid && gridSpacing >= 7.dp.toPx()) {
                val startX = gridPhase(pan.x, gridSpacing)
                val startY = gridPhase(pan.y, gridSpacing)
                val dotRadius = (0.9.dp.toPx() * sqrt(scale)).coerceIn(0.45.dp.toPx(), 1.25.dp.toPx())
                var x = startX; while (x < size.width) { var y = startY; while (y < size.height) { drawCircle(if (canvasDarkTheme) Color(0xFF565B6B).copy(alpha = 0.72f) else Color(0xFFCDD2E2).copy(alpha = 0.82f), dotRadius, Offset(x,y)); y += gridSpacing }; x += gridSpacing }
            }
            fun pos(p: Point): Offset { val q = viewport.toScreen(p); return Offset(q.x*density, q.y*density) }
            val viewportMargin=80.dp.toPx()
            val cardPaint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(40,48,65) }
            val cardPaintBg=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { style=android.graphics.Paint.Style.FILL }
            fun inViewport(a:Offset,boxSize:Size)=a.x<=size.width+viewportMargin && a.y<=size.height+viewportMargin && a.x+boxSize.width>=-viewportMargin && a.y+boxSize.height>=-viewportMargin
            fun selectionFrame(a: Offset, boxSize: Size, textFrame: Boolean = false) {
                val radius = if (textFrame) 4.dp.toPx() else 8.dp.toPx()
                drawRoundRect(LingyeColors.Primary, a, boxSize, androidx.compose.ui.geometry.CornerRadius(radius), style = Stroke(1.5.dp.toPx()))
                listOf(Offset(a.x,a.y),Offset(a.x+boxSize.width,a.y),Offset(a.x,a.y+boxSize.height),Offset(a.x+boxSize.width,a.y+boxSize.height)).forEach { handle ->
                    drawCircle(Color.White, 6.dp.toPx(), handle)
                    drawCircle(LingyeColors.Primary, 6.dp.toPx(), handle, style=Stroke(1.6.dp.toPx()))
                }
                listOf(
                    Offset(a.x+boxSize.width/2f,a.y),
                    Offset(a.x+boxSize.width/2f,a.y+boxSize.height),
                    Offset(a.x,a.y+boxSize.height/2f),
                    Offset(a.x+boxSize.width,a.y+boxSize.height/2f)
                ).forEach { handle ->
                    drawCircle(Color.White, 5.5.dp.toPx(), handle)
                    drawCircle(LingyeColors.Primary, 5.5.dp.toPx(), handle, style=Stroke(1.5.dp.toPx()))
                }
                if (textFrame) {
                    val dotRadius = 1.25.dp.toPx()
                    val gap = 4.5.dp.toPx()
                    val start = Offset(a.x - 18.dp.toPx(), a.y + 7.dp.toPx())
                    repeat(3) { row ->
                        repeat(2) { column ->
                            drawCircle(LingyeColors.Muted.copy(alpha = 0.55f), dotRadius, Offset(start.x + column * gap, start.y + row * gap))
                        }
                    }
                }
            }
            fun endpointMarker(tip: Offset, from: Offset, marker: String, color: Color) {
                if (marker == "none") return
                val angle = atan2(tip.y - from.y, tip.x - from.x)
                val ux = cos(angle); val uy = sin(angle)
                val px = -uy; val py = ux
                when (marker) {
                    "dot" -> {
                        drawCircle(Color.White, 6.dp.toPx(), tip)
                        drawCircle(color, 3.8.dp.toPx(), tip)
                    }
                    "arrow" -> {
                        val length = 10.dp.toPx(); val spread = 0.48f
                        val left = Offset(tip.x - cos(angle - spread) * length, tip.y - sin(angle - spread) * length)
                        val right = Offset(tip.x - cos(angle + spread) * length, tip.y - sin(angle + spread) * length)
                        drawPath(Path().apply { moveTo(tip.x, tip.y); lineTo(left.x, left.y); lineTo(right.x, right.y); close() }, color)
                    }
                    "triangle" -> {
                        val length = 11.dp.toPx(); val halfWidth = 6.dp.toPx()
                        val base = Offset(tip.x - ux * length, tip.y - uy * length)
                        drawPath(Path().apply {
                            moveTo(tip.x, tip.y)
                            lineTo(base.x + px * halfWidth, base.y + py * halfWidth)
                            lineTo(base.x - px * halfWidth, base.y - py * halfWidth)
                            close()
                        }, color)
                    }
                    "diamond" -> {
                        val front = Offset(tip.x + ux * 2.dp.toPx(), tip.y + uy * 2.dp.toPx())
                        val center = Offset(tip.x - ux * 5.dp.toPx(), tip.y - uy * 5.dp.toPx())
                        val back = Offset(tip.x - ux * 12.dp.toPx(), tip.y - uy * 12.dp.toPx())
                        val halfWidth = 5.dp.toPx()
                        drawPath(Path().apply {
                            moveTo(front.x, front.y)
                            lineTo(center.x + px * halfWidth, center.y + py * halfWidth)
                            lineTo(back.x, back.y)
                            lineTo(center.x - px * halfWidth, center.y - py * halfWidth)
                            close()
                        }, color)
                    }
                }
            }
            fun drawShape(i: Int, s: Shape) {
                val a = pos(Point(s.x, s.y))
                val boxSize = Size(s.width * scale * density, s.height * scale * density)
                if (!inViewport(a, boxSize)) return
                val radius = androidx.compose.ui.geometry.CornerRadius(s.cornerRadius.dp.toPx() * scale)
                val border = if (s.borderStyle != "none" && s.borderWidth > 0f) Stroke(
                    width = s.borderWidth.dp.toPx() * scale,
                    pathEffect = if (s.borderStyle == "dashed") PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())) else null
                ) else null
                when (s.type) {
                    "text" -> {
                        val fill = Color(s.color)
                        if (fill.alpha > 0f) {
                            when (s.shadowStyle) {
                                "none" -> Unit
                                "sticker" -> drawRoundRect(
                                    Color.Black.copy(alpha = 0.18f),
                                    a + Offset(5.dp.toPx(), 5.dp.toPx()),
                                    boxSize,
                                    cornerRadius = radius
                                )
                                "paper" -> {
                                    drawRoundRect(
                                        Color.White.copy(alpha = 0.9f),
                                        a - Offset(2.dp.toPx(), 2.dp.toPx()),
                                        Size(boxSize.width + 4.dp.toPx(), boxSize.height + 4.dp.toPx()),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius.x + 2.dp.toPx())
                                    )
                                }
                                "float" -> {
                                    drawRoundRect(
                                        Color.Black.copy(alpha = 0.08f),
                                        a + Offset(0f, 3.dp.toPx()),
                                        boxSize,
                                        cornerRadius = radius
                                    )
                                    drawRoundRect(
                                        Color.Black.copy(alpha = 0.06f),
                                        a + Offset(0f, 6.dp.toPx()),
                                        boxSize,
                                        cornerRadius = radius
                                    )
                                }
                                "film" -> drawRoundRect(
                                    Color.Black.copy(alpha = 0.22f),
                                    a + Offset(2.dp.toPx(), 2.dp.toPx()),
                                    boxSize,
                                    cornerRadius = radius
                                )
                                else -> drawRoundRect(
                                    Color.Black.copy(alpha = 0.10f),
                                    a + Offset(0f, 2.dp.toPx()),
                                    boxSize,
                                    cornerRadius = radius
                                )
                            }
                            drawRoundRect(fill, a, boxSize, cornerRadius = radius)
                        }
                        if (s.borderStyle != "none" && s.borderWidth > 0f) {
                            border?.let { drawRoundRect(Color(s.borderColor), a, boxSize, cornerRadius = radius, style = it) }
                        }
                        val sizePx = s.fontSize.sp.toPx() * scale
                        cardPaint.color = s.textColor
                        cardPaint.textSize = sizePx
                        cardPaint.textAlign = android.graphics.Paint.Align.LEFT
                        val baseFamilyIndex = CanvasTypefaces.familyIndex(s.fontFamily)
                        val lineHeight = sizePx
                        val textPad = 24.dp.toPx() * scale
                        val innerX = a.x + textPad
                        val innerY = a.y + textPad
                        val innerWidth = (boxSize.width - textPad * 2f).coerceAtLeast(1f)
                        val innerHeight = (boxSize.height - textPad * 2f).coerceAtLeast(1f)
                        drawContext.canvas.nativeCanvas.save()
                        drawContext.canvas.nativeCanvas.clipRect(innerX, innerY, innerX + innerWidth, innerY + innerHeight)
                        fun stylePaint(flags: Int) {
                            val code = flags and TextSpan.CODE != 0
                            val math = flags and TextSpan.MATH != 0
                            val familyIndex = when {
                                code -> 2
                                math -> 1
                                else -> baseFamilyIndex
                            }
                            val italic = math || s.fontStyle == "italic" || flags and TextSpan.ITALIC != 0
                            val weight = if (flags and TextSpan.BOLD != 0) maxOf(s.fontWeight, 700) else s.fontWeight
                            cardPaint.typeface = CanvasTypefaces.get(familyIndex, weight, italic)
                            cardPaint.color = if (flags and TextSpan.LINK != 0) 0xFF1E96EB.toInt()
                            else TextHighlightPalette.foregroundColor(TextSpan.foreground(flags)) ?: s.textColor
                            cardPaint.isUnderlineText = flags and TextSpan.UNDERLINE != 0 || flags and TextSpan.LINK != 0
                            cardPaint.isStrikeThruText = flags and TextSpan.STRIKE != 0
                        }
                        fun charWidth(index: Int): Float {
                            stylePaint(TextFormatting.flagsAt(s.textSpans, index))
                            val step = if (index + 1 < s.body.length && Character.isHighSurrogate(s.body[index]) &&
                                Character.isLowSurrogate(s.body[index + 1])
                            ) 2 else 1
                            return cardPaint.measureText(s.body, index, index + step)
                        }
                        val layout = TextLayoutCache.shared.layout(
                            TextLayoutKey(
                                body = s.body,
                                spans = s.textSpans,
                                widthPx = innerWidth,
                                sizePx = sizePx,
                                family = s.fontFamily,
                                weight = s.fontWeight,
                                italic = s.fontStyle == "italic"
                            )
                        ) {
                            val lines = TextFormatting.wrap(s.body, innerWidth, ::charWidth).take(8)
                            val runs = lines.map { TextFormatting.runs(s.textSpans, it.start, it.end) }
                            val widths = runs.map { lineRuns ->
                                lineRuns.map { run ->
                                    stylePaint(run.flags)
                                    cardPaint.measureText(s.body, run.start, run.end)
                                }
                            }
                            TextLayoutCache.Layout(lines, runs, widths)
                        }
                        val textHeight = lineHeight * layout.lines.size
                        val textTop = when (textVerticalAlign(s)) {
                            "middle" -> innerY + ((innerHeight - textHeight) / 2f).coerceAtLeast(0f)
                            "bottom" -> innerY + (innerHeight - textHeight).coerceAtLeast(0f)
                            else -> innerY
                        }
                        layout.lines.forEachIndexed { lineIndex, _ ->
                            val runs = layout.runs[lineIndex]
                            val widths = layout.runWidths[lineIndex]
                            val lineWidth = widths.sum()
                            var cursor = when (s.textAlign) {
                                "center" -> innerX + (innerWidth - lineWidth) / 2f
                                "right" -> innerX + innerWidth - lineWidth
                                else -> innerX
                            }
                            runs.forEachIndexed { runIndex, run ->
                                stylePaint(run.flags)
                                val runWidth = widths[runIndex]
                                val background = TextHighlightPalette.backgroundColor(TextSpan.background(run.flags))
                                    ?: if (run.flags and TextSpan.COMMENT != 0) 0xFFFFF3C4.toInt()
                                    else if (run.flags and TextSpan.CODE != 0) 0xFFF1F1F1.toInt() else null
                                if (background != null) {
                                    val top = textTop + lineHeight * lineIndex
                                    cardPaintBg.color = background
                                    drawContext.canvas.nativeCanvas.drawRect(cursor, top + 1f, cursor + runWidth, top + lineHeight - 1f, cardPaintBg)
                                }
                                drawContext.canvas.nativeCanvas.drawText(s.body, run.start, run.end, cursor, textTop + lineHeight * (lineIndex + 1), cardPaint)
                                cursor += runWidth
                            }
                        }
                        cardPaint.isUnderlineText = false
                        cardPaint.isStrikeThruText = false
                        cardPaint.color = s.textColor
                        cardPaint.typeface = android.graphics.Typeface.DEFAULT
                        drawContext.canvas.nativeCanvas.restore()
                    }
                    "ellipse" -> {
                        drawOval(Color(s.color), a, boxSize)
                        border?.let { drawOval(Color(s.borderColor), a, boxSize, style = it) }
                    }
                    "triangle" -> {
                        val path = Path().apply {
                            moveTo(a.x + boxSize.width / 2f, a.y)
                            lineTo(a.x + boxSize.width, a.y + boxSize.height)
                            lineTo(a.x, a.y + boxSize.height)
                            close()
                        }
                        drawPath(path, Color(s.color))
                        border?.let { drawPath(path, Color(s.borderColor), style = it) }
                    }
                    "diamond" -> {
                        val path = Path().apply {
                            moveTo(a.x + boxSize.width / 2f, a.y)
                            lineTo(a.x + boxSize.width, a.y + boxSize.height / 2f)
                            lineTo(a.x + boxSize.width / 2f, a.y + boxSize.height)
                            lineTo(a.x, a.y + boxSize.height / 2f)
                            close()
                        }
                        drawPath(path, Color(s.color))
                        border?.let { drawPath(path, Color(s.borderColor), style = it) }
                    }
                    "frame" -> {
                        val fill = Color(s.color)
                        drawRoundRect(if (fill == Color.White) fill.copy(alpha = 0.72f) else fill.copy(alpha = 0.88f), a, boxSize, cornerRadius = radius)
                        drawRoundRect(Color(0xffd7d7d7), a, boxSize, cornerRadius = radius, style = Stroke(1.dp.toPx()))
                        if (presentingFrameId == null) {
                            cardPaint.color = (if (LingyeColors.dark) {
                                if (fill.luminance() > 0.5f) Color(0xFF5E5E64) else Color(0xFFB8B8C0)
                            } else LingyeColors.Muted).toArgb()
                            cardPaint.textSize = 12.sp.toPx() * scale
                            cardPaint.isFakeBoldText = true
                            drawContext.canvas.nativeCanvas.drawText(s.title.ifBlank { "Frame" }, a.x, (a.y - 7.dp.toPx()).coerceAtLeast(12.dp.toPx()), cardPaint)
                        }
                    }
                    "pageLink" -> {
                        val resolved = PageLinks.resolve(s, pages)
                        drawRoundRect(LingyeColors.SurfaceHigh.copy(alpha = 0.98f), a, boxSize, cornerRadius = radius)
                        drawRoundRect(Color(s.borderColor), a, boxSize, cornerRadius = radius, style = Stroke(1.dp.toPx()))
                        drawRoundRect(
                            LingyeColors.Primary.copy(alpha = 0.85f),
                            a + Offset(0f, 14.dp.toPx() * scale),
                            Size(3.dp.toPx() * scale, (boxSize.height - 28.dp.toPx() * scale).coerceAtLeast(0f)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                        )
                        cardPaint.textAlign = android.graphics.Paint.Align.LEFT
                        cardPaint.isFakeBoldText = true
                        cardPaint.color = LingyeColors.Ink.toArgb()
                        cardPaint.textSize = 14.sp.toPx() * scale
                        drawContext.canvas.nativeCanvas.drawText(resolved.title.take(24), a.x + 14.dp.toPx() * scale, a.y + 38.dp.toPx() * scale, cardPaint)
                        cardPaint.isFakeBoldText = false
                        cardPaint.color = if (resolved.missing) LingyeColors.Coral.toArgb() else LingyeColors.Muted.toArgb()
                        cardPaint.textSize = 11.sp.toPx() * scale
                        val subtitle = if (resolved.missing) tr(language, "目标页面不存在", "Linked page missing") else tr(language, "链接文档", "Linked document")
                        drawContext.canvas.nativeCanvas.drawText(subtitle, a.x + 14.dp.toPx() * scale, a.y + 60.dp.toPx() * scale, cardPaint)
                    }
                    "mindNode" -> {
                        drawRoundRect(Color(s.color), a, boxSize, cornerRadius = radius)
                        border?.let { drawRoundRect(Color(s.borderColor), a, boxSize, cornerRadius = radius, style = it) }
                        cardPaint.textAlign = android.graphics.Paint.Align.LEFT
                        cardPaint.isFakeBoldText = true
                        cardPaint.color = s.textColor
                        cardPaint.textSize = (settings.editorFontSize).sp.toPx() * scale
                        drawContext.canvas.nativeCanvas.save()
                        drawContext.canvas.nativeCanvas.clipRect(a.x, a.y, a.x + boxSize.width, a.y + boxSize.height)
                        drawContext.canvas.nativeCanvas.drawText(
                            s.title.ifBlank { tr(language, "节点", "Node") }.take(18),
                            a.x + 12.dp.toPx() * scale,
                            a.y + boxSize.height / 2f + cardPaint.textSize / 3f,
                            cardPaint
                        )
                        drawContext.canvas.nativeCanvas.restore()
                    }
                    "card" -> {
                        when (s.shadowStyle) {
                            "none" -> Unit
                            "sticker" -> drawRoundRect(Color.Black.copy(alpha = 0.12f), a + Offset(0f, 6.dp.toPx()), boxSize, cornerRadius = radius)
                            else -> drawRoundRect(Color.Black.copy(alpha = 0.07f), a + Offset(0f, 4.dp.toPx()), boxSize, cornerRadius = radius)
                        }
                        drawRoundRect(Color(s.color), a, boxSize, cornerRadius = radius)
                        border?.let { drawRoundRect(Color(s.borderColor), a, boxSize, cornerRadius = radius, style = it) }
                        cardPaint.color = s.textColor
                        cardPaint.textSize = (settings.editorFontSize + 2).sp.toPx() * scale
                        cardPaint.isFakeBoldText = true
                        drawContext.canvas.nativeCanvas.save()
                        drawContext.canvas.nativeCanvas.clipRect(a.x, a.y, a.x + boxSize.width, a.y + boxSize.height)
                        drawContext.canvas.nativeCanvas.drawText(s.title.take(28), a.x + 12.dp.toPx() * scale, a.y + 26.dp.toPx() * scale, cardPaint)
                        cardPaint.isFakeBoldText = false
                        cardPaint.textSize = (settings.editorFontSize - 3).sp.toPx() * scale
                        s.body.lines().take(5).forEachIndexed { j, line ->
                            val baseline = a.y + (50 + 20 * j).dp.toPx() * scale
                            val checked = todoChecked(line)
                            if (checked != null) {
                                val box = 11.dp.toPx() * scale
                                val left = a.x + 12.dp.toPx() * scale
                                val top = baseline - 10.dp.toPx() * scale
                                drawRoundRect(
                                    if (checked) LingyeColors.Primary else LingyeColors.SurfaceHigh,
                                    Offset(left, top),
                                    Size(box, box),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx() * scale)
                                )
                                drawRoundRect(
                                    if (checked) LingyeColors.Primary else LingyeColors.Muted.copy(alpha = 0.55f),
                                    Offset(left, top),
                                    Size(box, box),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx() * scale),
                                    style = Stroke(1.dp.toPx() * scale)
                                )
                                if (checked) {
                                    drawLine(
                                        Color.White,
                                        Offset(left + box * 0.24f, top + box * 0.52f),
                                        Offset(left + box * 0.44f, top + box * 0.72f),
                                        strokeWidth = 1.6.dp.toPx() * scale,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        Color.White,
                                        Offset(left + box * 0.44f, top + box * 0.72f),
                                        Offset(left + box * 0.78f, top + box * 0.3f),
                                        strokeWidth = 1.6.dp.toPx() * scale,
                                        cap = StrokeCap.Round
                                    )
                                }
                                drawContext.canvas.nativeCanvas.drawText(todoContent(line).take(30), a.x + 30.dp.toPx() * scale, baseline, cardPaint)
                            } else {
                                drawContext.canvas.nativeCanvas.drawText(line.take(32), a.x + 12.dp.toPx() * scale, baseline, cardPaint)
                            }
                        }
                        drawContext.canvas.nativeCanvas.restore()
                    }
                    else -> {
                        if (s.type == "roundRect") drawRoundRect(Color(s.color), a, boxSize, cornerRadius = radius)
                        else drawRect(Color(s.color), a, boxSize)
                        border?.let {
                            if (s.type == "roundRect") drawRoundRect(Color(s.borderColor), a, boxSize, cornerRadius = radius, style = it)
                            else drawRect(Color(s.borderColor), a, boxSize, style = it)
                        }
                    }
                }
                if (CanvasSelection("shape", i) in selectedItems && !s.locked && i != cardEdit) selectionFrame(a, boxSize, textFrame = s.type == "text")
            }
            val shapesById = page.shapes.associateBy { it.id }
            val connectorRenders = HashMap<Int, ConnectorRenderData?>()
            fun computeConnectorRender(i: Int): ConnectorRenderData? {
                val line=page.resolvedConnector(i, shapesById); val selected=selection == CanvasSelection("connector",i)
                val a=if(selected && endpointDrag == true) endpointPreview ?: Point(line.x1,line.y1) else Point(line.x1,line.y1)
                val b=if(selected && endpointDrag == false) endpointPreview ?: Point(line.x2,line.y2) else Point(line.x2,line.y2)
                val x=pos(a); val y=pos(b)
                val displayLine=line.copy(x1=a.x,y1=a.y,x2=b.x,y2=b.y)
                val rawSamples=ConnectorSampleCache.shared.samples(displayLine).map(::pos)
                val screenSamples = if (settings.roughConnectors) rawSamples.mapIndexed { index, p ->
                    if (index == 0 || index == rawSamples.lastIndex) p
                    else p + Offset(sin(index * 1.7f) * 1.1f, cos(index * 1.3f) * 1.1f)
                } else rawSamples
                val minX=screenSamples.minOf { it.x };val maxX=screenSamples.maxOf { it.x };val minY=screenSamples.minOf { it.y };val maxY=screenSamples.maxOf { it.y }
                if(maxX<-viewportMargin || maxY<-viewportMargin || minX>size.width+viewportMargin || minY>size.height+viewportMargin) return null
                val path=Path().apply { val first=screenSamples.first(); moveTo(first.x,first.y); screenSamples.drop(1).forEach { lineTo(it.x, it.y) } }
                return ConnectorRenderData(line, displayLine, path, x, y, screenSamples, selected)
            }
            fun connectorRender(i: Int): ConnectorRenderData? {
                if (connectorRenders.containsKey(i)) return connectorRenders[i]
                val computed = computeConnectorRender(i)
                connectorRenders[i] = computed
                return computed
            }
            // Two passes keep white outlines from covering other connectors' colored strokes.
            fun drawConnectorUnderlay(i: Int) {
                val render = connectorRender(i) ?: return
                if (render.selected) drawPath(render.path, LingyeColors.Primary.copy(alpha=0.16f), style=Stroke(12.dp.toPx(),cap=StrokeCap.Round))
                drawPath(render.path, Color.White.copy(alpha=0.92f), style=Stroke((render.line.width+4f).dp.toPx()*scale,cap=StrokeCap.Round))
            }
            fun drawConnectorContent(i: Int, raw: Connector) {
                val render = connectorRender(i) ?: return
                val x = render.x; val y = render.y; val c = if(render.selected) LingyeColors.Primary else Color(render.line.color)
                drawPath(render.path, c, style=Stroke(render.line.width.dp.toPx()*scale,cap=StrokeCap.Round))
                endpointMarker(x, render.samples[2], raw.startMarker, c)
                endpointMarker(y, render.samples[render.samples.size-3], raw.endMarker, c)
                if (raw.label.isNotBlank()) {
                    val midpoint = pos(connectorProgress(render.displayLine, 0.5f)) + Offset(0f, if (render.selected) 52.dp.toPx() else 0f)
                    cardPaint.color = android.graphics.Color.rgb(32, 41, 58)
                    cardPaint.textSize = 12.sp.toPx() * scale
                    cardPaint.textAlign = android.graphics.Paint.Align.CENTER
                    val width = cardPaint.measureText(raw.label)
                    drawRoundRect(Color.White.copy(alpha=0.94f), midpoint-Offset(width/2f+6.dp.toPx(),16.dp.toPx()), Size(width+12.dp.toPx(),24.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()))
                    drawContext.canvas.nativeCanvas.drawText(raw.label, midpoint.x, midpoint.y+4.dp.toPx(), cardPaint)
                }
                if(raw.animated && motionEnabled && systemMotion && (y-x).getDistance()>20.dp.toPx()) {
                    val t=(motionTime % 2800000000L)/2800000000f; val fade=min(1f,min(t*8f,(1f-t)*8f)); val at=pos(connectorProgress(render.displayLine,t))
                    drawCircle(c.copy(alpha=0.16f*fade),7.dp.toPx(),at); drawCircle(Color.White.copy(alpha=0.9f*fade),3.dp.toPx(),at)
                }
                if(render.selected) { listOf(x,y).forEach { handle -> drawCircle(Color.White,8.dp.toPx(),handle); drawCircle(LingyeColors.Primary,8.dp.toPx(),handle,style=Stroke(2.dp.toPx())) } }
                if (endpointDrag != null) {
                    val handle = if (endpointDrag == true) x else y
                    when (endpointSnap?.kind) {
                        ConnectorSnapKind.ANCHOR -> {
                            drawCircle(LingyeColors.Primary.copy(alpha = 0.22f), 18.dp.toPx(), handle)
                            drawCircle(Color.White, 9.dp.toPx(), handle)
                            drawCircle(LingyeColors.Primary, 7.dp.toPx(), handle)
                        }
                        ConnectorSnapKind.EDGE -> {
                            drawCircle(LingyeColors.Primary.copy(alpha = 0.15f), 12.dp.toPx(), handle)
                            drawCircle(Color.White, 8.dp.toPx(), handle)
                            drawCircle(LingyeColors.Primary, 6.dp.toPx(), handle)
                        }
                        else -> Unit
                    }
                }
            }
            fun drawCanvasImage(i: Int, image: CanvasImage) { val bitmap = canvasBitmaps[image.name]; val at = pos(Point(image.x, image.y)); val w = (image.width * scale * density).roundToInt().coerceAtLeast(1); val h = (image.height * scale * density).roundToInt().coerceAtLeast(1); if(!inViewport(at,Size(w.toFloat(),h.toFloat()))) return; if (bitmap != null) { val sourceW=if(image.rotation%2==0) w else h;val sourceH=if(image.rotation%2==0) h else w;withTransform({ translate(at.x+w/2f,at.y+h/2f);scale(if(image.flipX)-1f else 1f,if(image.flipY)-1f else 1f,Offset.Zero);rotate(image.rotation*90f,Offset.Zero) }) { val sx=(bitmap.width*image.cropLeft).toInt().coerceIn(0,bitmap.width-1);val sy=(bitmap.height*image.cropTop).toInt().coerceIn(0,bitmap.height-1);val ex=(bitmap.width*image.cropRight).roundToInt().coerceIn(sx+1,bitmap.width);val ey=(bitmap.height*image.cropBottom).roundToInt().coerceIn(sy+1,bitmap.height);drawImage(bitmap,srcOffset=IntOffset(sx,sy),srcSize=IntSize(ex-sx,ey-sy),dstOffset=IntOffset(-sourceW/2,-sourceH/2),dstSize=IntSize(sourceW,sourceH),filterQuality=if(settings.smoothImages) FilterQuality.Medium else FilterQuality.None) } }; if (CanvasSelection("image", i) in selectedItems) selectionFrame(at,Size(w.toFloat(),h.toFloat())) }
            fun renderInk(stroke: List<Point>, style: InkStyle) {
                if (stroke.isEmpty()) return
                val color = Color(style.color); val width = style.width.dp.toPx() * scale
                if (stroke.size == 1) drawCircle(color, width / 2f, pos(stroke.first()))
                else { val path = Path().apply { val first = pos(stroke.first()); moveTo(first.x, first.y); stroke.drop(1).forEach { val p = pos(it); lineTo(p.x, p.y) } }; drawPath(path, color, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round)) }
            }
            fun drawCanvasStroke(i: Int, stroke: List<Point>) { val bounds=page.selectionBounds(listOf(CanvasSelection("stroke",i))) ?: return;val a=pos(Point(bounds.left,bounds.top));val z=pos(Point(bounds.right,bounds.bottom));if(!inViewport(a,Size((z.x-a.x).coerceAtLeast(1f),(z.y-a.y).coerceAtLeast(1f)))) return;renderInk(stroke, page.strokeStyles[i]); if(CanvasSelection("stroke",i) in selectedItems) selectionFrame(a,Size((z.x-a.x).coerceAtLeast(1f),(z.y-a.y).coerceAtLeast(1f))) }
            fun drawLayerItemById(id: String) { page.findStableObject(id)?.let { item -> when(item.type) { "shape" -> drawShape(item.index,page.shapes[item.index]); "connector" -> drawConnectorContent(item.index,page.connectors[item.index]); "image" -> drawCanvasImage(item.index,page.images[item.index]); "stroke" -> drawCanvasStroke(item.index,page.strokes[item.index]) } } }
            fun isFrameLayerId(id: String): Boolean { val item=page.findStableObject(id); return item?.type=="shape" && page.shapes[item.index].type=="frame" }
            fun isConnectorLayerId(id: String): Boolean = page.findStableObject(id)?.type == "connector"
            fun frameAreaOf(id: String): Float { val item=page.findStableObject(id) ?: return 0f; val s=page.shapes.getOrNull(item.index) ?: return 0f; return s.width*s.height }
            val layerIds=page.orderedLayerIds()
            layerIds.filter(::isFrameLayerId).sortedByDescending { frameAreaOf(it) }.forEach(::drawLayerItemById)
            // Connectors always render under text boxes, cards and other objects so they never cover content.
            // Outlines are drawn for every connector first, then all colored strokes, so overlapping
            // connectors cannot paint each other's white outline over a colored line.
            val connectorLayerIds = layerIds.filter(::isConnectorLayerId)
            connectorLayerIds.forEach { id ->
                page.findStableObject(id)?.let { if (it.type == "connector") drawConnectorUnderlay(it.index) }
            }
            connectorLayerIds.forEach(::drawLayerItemById)
            layerIds.filterNot { isFrameLayerId(it) || isConnectorLayerId(it) }.forEach(::drawLayerItemById)
            snapGuides.forEach { guide ->
                val guideColor = Color(0xFF8B5CF6)
                if (guide.vertical) {
                    val x = pos(Point(guide.position, 0f)).x
                    val y1 = pos(Point(0f, guide.start)).y
                    val y2 = pos(Point(0f, guide.end)).y
                    drawLine(guideColor, Offset(x, y1), Offset(x, y2), strokeWidth = 1.dp.toPx())
                } else {
                    val y = pos(Point(0f, guide.position)).y
                    val x1 = pos(Point(guide.start, 0f)).x
                    val x2 = pos(Point(guide.end, 0f)).x
                    drawLine(guideColor, Offset(x1, y), Offset(x2, y), strokeWidth = 1.dp.toPx())
                }
            }
            val selectedShapeIndices=selectedItems.filter { it.type=="shape" }.map { it.index }.toSet()
            val showAllPorts=tool == "连线" || endpointDrag != null
            val targetPort = endpointSnap?.takeIf { endpointDrag != null && it.kind == ConnectorSnapKind.ANCHOR }
                ?.let { snap -> snap.shapeIndex?.let { it to snap.port } }
                ?: connectorDraft?.let { draft ->
                    if (draft.snapKind == ConnectorSnapKind.ANCHOR) draft.targetShape?.let { it to draft.targetPort } else null
                }
            page.shapes.forEachIndexed { shapeIndex, shape ->
                if ((showAllPorts || shapeIndex in selectedShapeIndices) && shape.type != "text") listOf("left","right","top","bottom").forEach { port ->
                    val p=pos(shapePort(shape,port)); val target=targetPort?.let { it.first==shapeIndex && it.second==port } == true
                    if(target) drawCircle(LingyeColors.Mint.copy(alpha=0.2f),13.dp.toPx(),p)
                    val fill=when(port){"left"->LingyeColors.Mint;"right"->LingyeColors.Primary;else->Color.White}
                    drawCircle(fill,5.5.dp.toPx(),p); drawCircle(if(target)LingyeColors.Mint else LingyeColors.Primary,5.5.dp.toPx(),p,style=Stroke(2.dp.toPx()))
                }
            }
            connectorDraft?.let { draft ->
                val p2 = pos(draft.end)
                val endPort = when (draft.snapKind) {
                    ConnectorSnapKind.ANCHOR -> draft.targetPort
                    ConnectorSnapKind.EDGE -> draft.snapEdge.ifBlank { "center" }
                    ConnectorSnapKind.NONE -> "center"
                }
                val previewLine = Connector(
                    draft.start.x, draft.start.y, draft.end.x, draft.end.y,
                    pathStyle = settings.connectorPathStyle, startPort = draft.sourcePort, endPort = endPort
                )
                val samples = connectorSamples(previewLine).map(::pos)
                val path = Path().apply {
                    val first = samples.first(); moveTo(first.x, first.y)
                    samples.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(
                    path,
                    LingyeColors.Primary.copy(alpha = 0.55f),
                    style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())))
                )
                if (draft.snapKind == ConnectorSnapKind.ANCHOR) {
                    drawCircle(LingyeColors.Primary.copy(alpha = 0.22f), 18.dp.toPx(), p2)
                    drawCircle(Color.White, 9.dp.toPx(), p2)
                    drawCircle(LingyeColors.Primary, 7.dp.toPx(), p2)
                } else if (draft.snapKind == ConnectorSnapKind.EDGE) {
                    drawCircle(LingyeColors.Primary.copy(alpha = 0.15f), 12.dp.toPx(), p2)
                    drawCircle(Color.White, 8.dp.toPx(), p2)
                    drawCircle(LingyeColors.Primary, 6.dp.toPx(), p2)
                } else {
                    drawCircle(Color.White, 6.dp.toPx(), p2)
                    drawCircle(LingyeColors.Primary, 6.dp.toPx(), p2, style = Stroke(2.dp.toPx()))
                }
                draft.targetShape?.let { idx -> page.shapes.getOrNull(idx)?.let { targetShape ->
                    val at2 = pos(Point(targetShape.x, targetShape.y))
                    val size2 = Size(targetShape.width * scale * density, targetShape.height * scale * density)
                    drawRoundRect(LingyeColors.Mint.copy(alpha = 0.12f), at2, size2, androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()))
                    drawRoundRect(LingyeColors.Mint, at2, size2, androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()), style = Stroke(1.5.dp.toPx()))
                } }
            }
            connectorQuickSource?.let { idx -> page.shapes.getOrNull(idx)?.let { sourceShape ->
                val at2 = pos(Point(sourceShape.x, sourceShape.y))
                val size2 = Size(sourceShape.width * scale * density, sourceShape.height * scale * density)
                drawRoundRect(LingyeColors.Primary, at2, size2, androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()), style = Stroke(2.dp.toPx()))
            } }
            if (selectedItems.size > 1) page.selectionBounds(selectedItems)?.let { b -> val a=pos(Point(b.left,b.top)); val z=pos(Point(b.right,b.bottom)); selectionFrame(a,Size(z.x-a.x,z.y-a.y)) }
            val boxA=boxStart; val boxB=boxEnd
            if (boxA != null && boxB != null) { val a=pos(boxA); val b=pos(boxB); val topLeft=Offset(min(a.x,b.x),min(a.y,b.y)); val boxSize=Size(abs(a.x-b.x),abs(a.y-b.y)); drawRect(Color(0x224185DF),topLeft,boxSize); drawRect(Color(0xFF4185DF),topLeft,boxSize,style=Stroke(1.dp.toPx())) }
            if (tool == "画笔") renderInk(points, InkStyle(activeInkColor, activeInkWidth))
            if (tool == "橡皮" && eraserArea && points.isNotEmpty()) {
                val trailWidth = 32f * scale * density
                val trailColor = Color(0xFF9AA0A6).copy(alpha = 0.35f)
                if (points.size == 1) drawCircle(trailColor, trailWidth / 2f, pos(points.first()))
                else {
                    val path = Path().apply { val first = pos(points.first()); moveTo(first.x, first.y); points.drop(1).forEach { val q = pos(it); lineTo(q.x, q.y) } }
                    drawPath(path, trailColor, style = Stroke(trailWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
}
