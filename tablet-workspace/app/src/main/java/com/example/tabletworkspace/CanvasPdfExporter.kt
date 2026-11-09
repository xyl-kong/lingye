package com.example.tabletworkspace

import android.graphics.Bitmap
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Vector PDF export for an edgeless page. Text, frames, shapes and connectors are
 * written as PDF drawing commands; images stay raster at their stored resolution.
 */
object CanvasPdfExporter {
    private const val MARGIN = 64f
    private const val MAX_SIDE = 3600f
    private const val MAX_SCALE = 4f

    fun encode(page: Page, background: Int, image: (String) -> Bitmap?): ByteArray {
        val bounds = contentBounds(page) ?: error("画布没有可导出的内容")
        val width = bounds.right - bounds.left
        val height = bounds.bottom - bounds.top
        require(width > 0f && height > 0f) { "画布尺寸无效" }
        val scale = min(MAX_SCALE, MAX_SIDE / max(width, height)).coerceAtLeast(0.01f)
        val pageWidth = ceil(width * scale + MARGIN * 2).toInt().coerceIn(64, 14400)
        val pageHeight = ceil(height * scale + MARGIN * 2).toInt().coerceIn(64, 14400)
        fun x(value: Float) = (value - bounds.left) * scale + MARGIN
        fun y(value: Float) = (value - bounds.top) * scale + MARGIN
        fun size(value: Float) = value * scale

        val document = PdfDocument()
        val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val pdfPage = document.startPage(info)
        val canvas = pdfPage.canvas
        canvas.drawColor(background or 0xFF000000.toInt())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        page.shapes.filter { it.type == "frame" }.forEach { frame ->
            drawFrame(canvas, frame, paint, ::x, ::y, ::size)
        }
        page.connectors.forEachIndexed { index, _ ->
            drawConnector(canvas, page.resolvedConnector(index), paint, ::x, ::y, ::size)
        }
        page.images.forEach { canvasImage ->
            val bitmap = runCatching { image(canvasImage.name) }.getOrNull() ?: return@forEach
            drawImage(canvas, canvasImage, bitmap, paint, ::x, ::y, ::size)
        }
        page.strokes.forEachIndexed { index, stroke ->
            drawStroke(canvas, stroke, page.strokeStyles.getOrNull(index) ?: InkStyle(0xff27374b.toInt(), 4f), paint, ::x, ::y, ::size)
        }
        page.shapes.filter { it.type != "frame" }.forEach { shape ->
            drawShape(canvas, shape, paint, ::x, ::y, ::size)
        }

        document.finishPage(pdfPage)
        val out = ByteArrayOutputStream()
        document.writeTo(out)
        document.close()
        return out.toByteArray()
    }

    private fun contentBounds(page: Page): RectF? {
        var left = Float.MAX_VALUE
        var top = Float.MAX_VALUE
        var right = -Float.MAX_VALUE
        var bottom = -Float.MAX_VALUE
        fun include(l: Float, t: Float, r: Float, b: Float) {
            left = min(left, l); top = min(top, t); right = max(right, r); bottom = max(bottom, b)
        }
        page.shapes.forEach { shape ->
            include(shape.x, shape.y - if (shape.type == "frame") FrameSemantics.TITLE_ZONE else 0f, shape.x + shape.width, shape.y + shape.height)
        }
        page.images.forEach { include(it.x, it.y, it.x + it.width, it.y + it.height) }
        page.strokes.forEach { stroke ->
            if (stroke.isNotEmpty()) include(stroke.minOf { it.x }, stroke.minOf { it.y }, stroke.maxOf { it.x }, stroke.maxOf { it.y })
        }
        page.connectors.forEachIndexed { index, _ ->
            val samples = connectorSamples(page.resolvedConnector(index))
            if (samples.isNotEmpty()) include(samples.minOf { it.x }, samples.minOf { it.y }, samples.maxOf { it.x }, samples.maxOf { it.y })
        }
        if (left == Float.MAX_VALUE) return null
        return RectF(left, top, right, bottom)
    }

    private fun typeface(shape: Shape): Typeface = when (shape.fontFamily) {
        "serif" -> Typeface.SERIF
        "mono" -> Typeface.MONOSPACE
        else -> Typeface.SANS_SERIF
    }.let { base ->
        if (shape.fontWeight >= 600) Typeface.create(base, Typeface.BOLD) else base
    }

    private fun textPaint(shape: Shape, paint: Paint, size: (Float) -> Float) {
        paint.reset()
        paint.isAntiAlias = true
        paint.color = shape.textColor
        paint.textSize = size(shape.fontSize)
        paint.typeface = typeface(shape)
        paint.textSkewX = if (shape.fontStyle == "italic") -0.2f else 0f
        paint.textAlign = when (shape.textAlign) {
            "center" -> Paint.Align.CENTER
            "right" -> Paint.Align.RIGHT
            else -> Paint.Align.LEFT
        }
        paint.isFakeBoldText = shape.fontWeight >= 600
    }

    private fun drawWrappedText(canvas: android.graphics.Canvas, text: String, left: Float, top: Float, width: Float, paint: Paint, lineGap: Float = 1.25f, maxLines: Int = 200) {
        if (text.isBlank()) return
        val lines = mutableListOf<String>()
        text.lines().forEach { paragraph ->
            var line = ""
            paragraph.forEach { ch ->
                val next = line + ch
                if (line.isNotEmpty() && paint.measureText(next) > width) {
                    lines += line
                    line = ch.toString()
                } else line = next
            }
            lines += line
        }
        var baseline = top + paint.textSize
        lines.take(maxLines).forEach { line ->
            canvas.drawText(line, left, baseline, paint)
            baseline += paint.textSize * lineGap
        }
    }

    private fun drawFrame(canvas: android.graphics.Canvas, frame: Shape, paint: Paint, x: (Float) -> Float, y: (Float) -> Float, size: (Float) -> Float) {
        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = frame.color
        canvas.drawRoundRect(RectF(x(frame.x), y(frame.y), x(frame.x + frame.width), y(frame.y + frame.height)), size(frame.cornerRadius), size(frame.cornerRadius), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size(frame.borderWidth).coerceAtLeast(1f)
        paint.color = frame.borderColor
        if (frame.borderStyle == "dashed") paint.pathEffect = DashPathEffect(floatArrayOf(size(8f), size(6f)), 0f) else paint.pathEffect = null
        canvas.drawRoundRect(RectF(x(frame.x), y(frame.y), x(frame.x + frame.width), y(frame.y + frame.height)), size(frame.cornerRadius), size(frame.cornerRadius), paint)
        textPaint(frame.copy(textColor = 0xff5f6673.toInt(), fontSize = 12f, fontFamily = "sans", fontWeight = 400, fontStyle = "normal"), paint, size)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(frame.title.ifBlank { "Frame" }, x(frame.x), y(frame.y - 8f), paint)
    }

    private fun drawShape(canvas: android.graphics.Canvas, shape: Shape, paint: Paint, x: (Float) -> Float, y: (Float) -> Float, size: (Float) -> Float) {
        paint.reset(); paint.isAntiAlias = true
        val left = x(shape.x)
        val top = y(shape.y)
        val right = x(shape.x + shape.width)
        val bottom = y(shape.y + shape.height)
        val radius = size(shape.cornerRadius)
        when (shape.type) {
            "text", "card", "pageLink", "mindNode" -> {
                if (shape.type != "text" || (shape.color ushr 24) != 0) {
                    paint.style = Paint.Style.FILL
                    paint.color = shape.color
                    canvas.drawRoundRect(RectF(left, top, right, bottom), radius, radius, paint)
                }
                if (shape.borderStyle != "none" && shape.borderWidth > 0f) {
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = size(shape.borderWidth).coerceAtLeast(1f)
                    paint.color = shape.borderColor
                    paint.pathEffect = if (shape.borderStyle == "dashed") DashPathEffect(floatArrayOf(size(8f), size(6f)), 0f) else null
                    canvas.drawRoundRect(RectF(left, top, right, bottom), radius, radius, paint)
                }
                val padding = size(12f)
                textPaint(shape, paint, size)
                val title = when (shape.type) {
                    "card" -> shape.title.ifBlank { shape.body.lineSequence().firstOrNull().orEmpty() }
                    "pageLink" -> shape.title.ifBlank { "Linked document" }
                    else -> ""
                }
                if (title.isNotBlank()) {
                    val titlePaint = Paint(paint).apply {
                        isFakeBoldText = true
                        textSize = size((shape.fontSize + 2f).coerceAtLeast(12f))
                        color = shape.textColor
                    }
                    val align = titlePaint.textAlign
                    val tx = when (align) {
                        Paint.Align.CENTER -> (left + right) / 2f
                        Paint.Align.RIGHT -> right - padding
                        else -> left + padding
                    }
                    canvas.drawText(title, tx, top + padding + titlePaint.textSize, titlePaint)
                }
                val bodyTop = top + padding + if (title.isNotBlank()) size((shape.fontSize + 2f) * 1.5f) else 0f
                if (shape.type == "pageLink") {
                    paint.textSize = size(11f)
                    paint.color = 0xff7d8490.toInt()
                    paint.textAlign = Paint.Align.LEFT
                    canvas.drawText(if (shape.body.isNotBlank()) "链接文档" else "", left + padding, bodyTop + paint.textSize, paint)
                } else {
                    val body = if (shape.type == "card") shape.body else shape.body
                    val bodyX = when (shape.textAlign) {
                        "center" -> (left + right) / 2f
                        "right" -> right - padding
                        else -> left + padding
                    }
                    drawWrappedText(canvas, body, bodyX, bodyTop, right - left - padding * 2, paint)
                }
            }
            else -> {
                val path = Path()
                when (shape.type) {
                    "ellipse" -> path.addOval(RectF(left, top, right, bottom), Path.Direction.CW)
                    "triangle" -> {
                        path.moveTo((left + right) / 2f, top)
                        path.lineTo(right, bottom)
                        path.lineTo(left, bottom)
                        path.close()
                    }
                    "diamond" -> {
                        path.moveTo((left + right) / 2f, top)
                        path.lineTo(right, (top + bottom) / 2f)
                        path.lineTo((left + right) / 2f, bottom)
                        path.lineTo(left, (top + bottom) / 2f)
                        path.close()
                    }
                    "roundRect" -> path.addRoundRect(RectF(left, top, right, bottom), radius, radius, Path.Direction.CW)
                    else -> path.addRect(RectF(left, top, right, bottom), Path.Direction.CW)
                }
                paint.style = Paint.Style.FILL
                paint.color = shape.color
                canvas.drawPath(path, paint)
                if (shape.borderStyle != "none" && shape.borderWidth > 0f) {
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = size(shape.borderWidth).coerceAtLeast(1f)
                    paint.color = shape.borderColor
                    paint.pathEffect = if (shape.borderStyle == "dashed") DashPathEffect(floatArrayOf(size(8f), size(6f)), 0f) else null
                    canvas.drawPath(path, paint)
                }
            }
        }
    }

    private fun drawConnector(canvas: android.graphics.Canvas, connector: Connector, paint: Paint, x: (Float) -> Float, y: (Float) -> Float, size: (Float) -> Float) {
        val samples = connectorSamples(connector)
        if (samples.size < 2) return
        val path = Path()
        path.moveTo(x(samples[0].x), y(samples[0].y))
        samples.drop(1).forEach { path.lineTo(x(it.x), y(it.y)) }
        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size(connector.width).coerceAtLeast(1f)
        paint.color = connector.color
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawPath(path, paint)
        drawMarker(canvas, samples[2], samples[0], connector.startMarker, connector.color, paint, x, y, size)
        drawMarker(canvas, samples[samples.size - 3], samples.last(), connector.endMarker, connector.color, paint, x, y, size)
        if (connector.label.isNotBlank()) {
            val mid = connectorProgress(connector, 0.5f)
            paint.style = Paint.Style.FILL
            paint.textSize = size(12f)
            paint.color = connector.color
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(connector.label, x(mid.x), y(mid.y) - size(4f), paint)
        }
    }

    private fun drawMarker(canvas: android.graphics.Canvas, tip: Point, from: Point, marker: String, color: Int, paint: Paint, x: (Float) -> Float, y: (Float) -> Float, size: (Float) -> Float) {
        if (marker == "none") return
        val tipX = x(tip.x)
        val tipY = y(tip.y)
        val fromX = x(from.x)
        val fromY = y(from.y)
        val angle = kotlin.math.atan2(tipY - fromY, tipX - fromX)
        val px = -kotlin.math.sin(angle)
        val py = kotlin.math.cos(angle)
        paint.reset(); paint.isAntiAlias = true; paint.style = Paint.Style.FILL; paint.color = color
        drawMarkerAt(canvas, tipX, tipY, angle, px, py, marker, paint, size)
    }

    private fun drawMarkerAt(canvas: android.graphics.Canvas, tipX: Float, tipY: Float, angle: Float, px: Float, py: Float, marker: String, paint: Paint, size: (Float) -> Float) {
        val length = size(10f)
        val halfWidth = size(6f)
        val path = Path()
        when (marker) {
            "dot" -> canvas.drawCircle(tipX, tipY, size(4f), paint)
            "arrow", "triangle" -> {
                path.moveTo(tipX, tipY)
                path.lineTo(tipX - kotlin.math.cos(angle - 0.48f) * length, tipY - kotlin.math.sin(angle - 0.48f) * length)
                path.lineTo(tipX - kotlin.math.cos(angle + 0.48f) * length, tipY - kotlin.math.sin(angle + 0.48f) * length)
                path.close()
                canvas.drawPath(path, paint)
            }
            "diamond" -> {
                path.moveTo(tipX + kotlin.math.cos(angle) * size(2f), tipY + kotlin.math.sin(angle) * size(2f))
                path.lineTo(tipX - kotlin.math.cos(angle) * size(5f) + px * halfWidth, tipY - kotlin.math.sin(angle) * size(5f) + py * halfWidth)
                path.lineTo(tipX - kotlin.math.cos(angle) * size(12f), tipY - kotlin.math.sin(angle) * size(12f))
                path.lineTo(tipX - kotlin.math.cos(angle) * size(5f) - px * halfWidth, tipY - kotlin.math.sin(angle) * size(5f) - py * halfWidth)
                path.close()
                canvas.drawPath(path, paint)
            }
        }
    }

    private fun drawImage(canvas: android.graphics.Canvas, image: CanvasImage, bitmap: Bitmap, paint: Paint, x: (Float) -> Float, y: (Float) -> Float, size: (Float) -> Float) {
        paint.reset(); paint.isAntiAlias = true; paint.isFilterBitmap = true
        val left = x(image.x)
        val top = y(image.y)
        val right = x(image.x + image.width)
        val bottom = y(image.y + image.height)
        val target = RectF(left, top, right, bottom)
        canvas.save()
        canvas.rotate(image.rotation * 90f, target.centerX(), target.centerY())
        if (image.flipX) canvas.scale(-1f, 1f, target.centerX(), target.centerY())
        if (image.flipY) canvas.scale(1f, -1f, target.centerX(), target.centerY())
        canvas.drawBitmap(bitmap, null, target, paint)
        canvas.restore()
    }

    private fun drawStroke(canvas: android.graphics.Canvas, points: List<Point>, style: InkStyle, paint: Paint, x: (Float) -> Float, y: (Float) -> Float, size: (Float) -> Float) {
        if (points.size < 2) return
        val path = Path()
        path.moveTo(x(points[0].x), y(points[0].y))
        points.drop(1).forEach { path.lineTo(x(it.x), y(it.y)) }
        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size(style.width).coerceAtLeast(1f)
        paint.color = style.color
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        canvas.drawPath(path, paint)
    }
}
