package com.example.tabletworkspace

import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Classic color circles shared by every color control in the app. */
object ColorPalette {
    val CLASSIC = listOf(
        0xffef7076.toInt(), 0xfff08c43.toInt(), 0xffe8c64b.toInt(), 0xff57b85e.toInt(),
        0xff79b8ef.toInt(), 0xff6656d9.toInt(), 0xffcf69b4.toInt(), 0xff9a9a9a.toInt(),
        0xff111111.toInt(), 0xffffffff.toInt()
    )
}

/**
 * Parses a typed color sequence: #RRGGBB / RGB / 8-digit hex / rgb(r,g,b),
 * or a 1-based index into [palette].
 */
fun parseColorSequence(input: String, palette: List<Int> = emptyList()): Int? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return null
    var body = trimmed
    if (body.startsWith("rgb(", ignoreCase = true) && body.endsWith(")")) {
        body = body.substring(4, body.length - 1)
    }
    val parts = body.split(',', ' ', ';').filter { it.isNotBlank() }
    if (parts.size == 3) {
        val values = parts.map { it.toIntOrNull() }
        if (values.all { it != null && it in 0..255 }) {
            return (0xFF shl 24) or (values[0]!! shl 16) or (values[1]!! shl 8) or values[2]!!
        }
    }
    val hex = body.removePrefix("#")
    when {
        hex.matches(Regex("^[0-9a-fA-F]{3}$")) -> {
            val r = hex[0].digitToInt(16) * 17
            val g = hex[1].digitToInt(16) * 17
            val b = hex[2].digitToInt(16) * 17
            return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
        hex.matches(Regex("^[0-9a-fA-F]{6}$")) -> return (0xFF shl 24) or hex.toInt(16)
        hex.matches(Regex("^[0-9a-fA-F]{8}$")) -> return (0xFF shl 24) or hex.takeLast(6).toInt(16)
    }
    val index = trimmed.toIntOrNull()
    if (index != null && index in 1..palette.size) return palette[index - 1] or (0xFF shl 24)
    return null
}

/** User-defined swatches, shared across settings and canvas toolbars. */
object CustomColorStore {
    private const val KEY = "custom_colors"
    private const val MAX = 12
    private var preferences: SharedPreferences? = null
    val colors = mutableStateListOf<Int>()

    fun attach(prefs: SharedPreferences) {
        if (preferences === prefs) return
        preferences = prefs
        load()
    }

    fun add(color: Int): Boolean {
        val normalized = color or 0xFF000000.toInt()
        if (colors.any { sameRgb(it, normalized) }) return false
        if (colors.size >= MAX) colors.removeAt(0)
        colors.add(normalized)
        save()
        return true
    }

    fun remove(color: Int) {
        colors.removeAll { sameRgb(it, color) }
        save()
    }

    fun clear() {
        colors.clear()
        save()
    }

    private fun load() {
        colors.clear()
        val raw = preferences?.getString(KEY, "").orEmpty()
        raw.split(',').mapNotNull { part ->
            part.trim().takeIf { it.isNotEmpty() }?.toLongOrNull(16)?.toInt()?.or(0xFF000000.toInt())
        }.distinctBy { it and 0xFFFFFF }.take(MAX).forEach { colors.add(it) }
    }

    private fun save() {
        preferences?.edit()?.putString(KEY, colors.joinToString(",") { Integer.toHexString(it) })?.apply()
    }
}

fun sameRgb(a: Int, b: Int): Boolean = (a and 0xFFFFFF) == (b and 0xFFFFFF)

fun toHexRgb(color: Int): String =
    String.format("#%06X", color and 0xFFFFFF)

fun rgbParts(color: Int): Triple<Int, Int, Int> =
    Triple((color shr 16) and 0xFF, (color shr 8) and 0xFF, color and 0xFF)

fun rgbToHsv(color: Int): Triple<Float, Float, Float> {
    val (ri, gi, bi) = rgbParts(color)
    val r = ri / 255f
    val g = gi / 255f
    val b = bi / 255f
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    val h = when {
        delta == 0f -> 0f
        max == r -> 60f * (((g - b) / delta) % 6f)
        max == g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }.let { if (it < 0f) it + 360f else it }
    val s = if (max == 0f) 0f else delta / max
    return Triple(h, s * 100f, max * 100f)
}

fun hsvToRgb(hue: Float, saturation: Float, value: Float): Int {
    val h = ((hue % 360f) + 360f) % 360f
    val s = (saturation / 100f).coerceIn(0f, 1f)
    val v = (value / 100f).coerceIn(0f, 1f)
    val c = v * s
    val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
    val m = v - c
    val (r1, g1, b1) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val r = ((r1 + m) * 255f).roundToInt().coerceIn(0, 255)
    val g = ((g1 + m) * 255f).roundToInt().coerceIn(0, 255)
    val b = ((b1 + m) * 255f).roundToInt().coerceIn(0, 255)
    return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
}

@Composable
fun ColorSwatch(
    color: Int,
    selected: Boolean,
    description: String,
    size: Dp = 24.dp,
    hitPadding: Dp = 3.dp,
    onClick: () -> Unit
) {
    Box(
        Modifier.size(size + hitPadding * 2).clip(CircleShape).clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(size).background(Color(color or 0xFF000000.toInt()), CircleShape).then(
                if (selected) Modifier.border(2.dp, LingyeColors.Primary, CircleShape)
                else Modifier.border(1.dp, LingyeColors.Line, CircleShape)
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPaletteRow(
    selected: Int,
    onPick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    leading: List<Pair<Int, String>> = emptyList(),
    onRequestCustom: (() -> Unit)? = null,
    swatchSize: Dp = 24.dp,
    swatchHitPadding: Dp = 3.dp
) {
    val leadingColors = leading.distinctBy { it.first and 0xFFFFFF }
    val classicColors = ColorPalette.CLASSIC.filter { color -> leadingColors.none { sameRgb(it.first, color) } }
    FlowRow(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        leadingColors.forEach { (color, label) ->
            ColorSwatch(color, sameRgb(selected, color), label, swatchSize, swatchHitPadding) { onPick(color) }
        }
        if (leadingColors.isNotEmpty() && classicColors.isNotEmpty()) {
            Box(Modifier.width(1.dp).height(22.dp).background(LingyeColors.Line))
        }
        classicColors.forEachIndexed { index, color ->
            ColorSwatch(
                color, sameRgb(selected, color),
                "预设颜色 ${index + 1}", swatchSize, swatchHitPadding
            ) { onPick(color) }
        }
        TransparentSwatch(
            selected = (selected ushr 24) == 0,
            description = "透明颜色",
            size = swatchSize,
            hitPadding = swatchHitPadding
        ) { onPick(0x00000000) }
        RainbowColorButton(
            selected = selected,
            size = swatchSize,
            hitPadding = swatchHitPadding,
            onPick = onPick,
            onRequestCustom = onRequestCustom
        )
    }
}

@Composable
private fun TransparentSwatch(
    selected: Boolean,
    description: String,
    size: Dp,
    hitPadding: Dp,
    onClick: () -> Unit
) {
    Box(
        Modifier.size(size + hitPadding * 2).clip(CircleShape).clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(size).clip(CircleShape)) {
            drawRect(PickerWhite)
            val cell = this.size.minDimension / 4f
            for (row in 0 until 4) {
                for (col in 0 until 4) {
                    if ((row + col) % 2 == 1) {
                        drawRect(
                            Color(0xFFE2E2E2),
                            topLeft = Offset(col * cell, row * cell),
                            size = androidx.compose.ui.geometry.Size(cell, cell)
                        )
                    }
                }
            }
        }
        Box(
            Modifier.size(size).border(
                if (selected) 2.dp else 1.dp,
                if (selected) PickerAccent else LingyeColors.Line,
                CircleShape
            )
        )
    }
}

@Composable
private fun RainbowColorButton(
    selected: Int,
    size: Dp,
    hitPadding: Dp,
    onPick: (Int) -> Unit,
    onRequestCustom: (() -> Unit)?
) {
    Box(
        Modifier.size(size + hitPadding * 2).clip(CircleShape)
            .clickable {
                CustomColorDialogHost.show(selected, onPick)
                onRequestCustom?.invoke()
            }
            .semantics { contentDescription = "自定义颜色" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(size).clip(CircleShape)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 2f
            drawCircle(
                Brush.sweepGradient(
                    listOf(
                        Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                        Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000)
                    ),
                    center
                ),
                radius,
                center
            )
            drawCircle(PickerWhite.copy(alpha = 0.9f), radius, center, style = Stroke(1.dp.toPx()))
        }
    }
}

/** Hosts the custom colour dialog outside of any menu, so opening it can close the menu behind. */
data class CustomColorRequest(val initial: Int, val onPick: (Int) -> Unit)

object CustomColorDialogHost {
    var request by mutableStateOf<CustomColorRequest?>(null)
        private set

    fun show(initial: Int, onPick: (Int) -> Unit) {
        request = CustomColorRequest(initial, onPick)
    }

    fun dismiss() {
        request = null
    }
}

@Composable
fun CustomColorDialogHostView() {
    CustomColorDialogHost.request?.let { current ->
        ColorEditorDialog(
            initialColor = current.initial,
            onDismiss = { CustomColorDialogHost.dismiss() },
            onConfirm = { color ->
                current.onPick(color)
                CustomColorDialogHost.dismiss()
            }
        )
    }
}

@Composable
fun ColorPaletteMenu(selected: Int, onPick: (Int) -> Unit, onRequestCustom: (() -> Unit)? = null) {
    ColorPaletteRow(
        selected,
        onPick,
        Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        onRequestCustom = onRequestCustom,
        swatchSize = 18.dp,
        swatchHitPadding = 1.dp
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LegacyColorEditorDialog(
    initialColor: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var color by remember { mutableIntStateOf(initialColor or 0xFF000000.toInt()) }
    var rgbOpen by remember { mutableStateOf(true) }
    var hsvOpen by remember { mutableStateOf(false) }
    var saveToPalette by remember { mutableStateOf(true) }
    var showCustomPalette by remember { mutableStateOf(false) }
    var sequenceInput by remember { mutableStateOf("") }
    var sequenceError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val (r0, g0, b0) = rgbParts(color)
    val (h0, s0, v0) = rgbToHsv(color)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            Modifier.widthIn(max = 440.dp).fillMaxWidth(0.86f),
            shape = RoundedCornerShape(22.dp),
            color = LingyeColors.SurfaceHigh,
            shadowElevation = 20.dp
        ) {
            Column(Modifier.padding(vertical = 18.dp, horizontal = 20.dp)) {
                Text("自定义颜色", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = LingyeColors.Ink)
                Text("选择 RGB 或 HSV 调整，并可将颜色保存为色卡。", fontSize = 12.sp, color = LingyeColors.Muted)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).background(Color(color), CircleShape)
                            .border(1.dp, LingyeColors.Line, CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(toHexRgb(color), fontSize = 14.sp, fontFamily = FontFamily.Monospace, color = LingyeColors.Ink)
                        Text("RGB ${r0}, ${g0}, ${b0}", fontSize = 11.sp, color = LingyeColors.Muted)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("颜色序列", Modifier.width(64.dp), fontSize = 12.sp, color = LingyeColors.Muted)
                    Box(
                        Modifier.weight(1f).height(34.dp)
                            .background(LingyeColors.Canvas, RoundedCornerShape(7.dp))
                            .border(
                                1.dp,
                                if (sequenceError) LingyeColors.Coral else LingyeColors.Line,
                                RoundedCornerShape(7.dp)
                            )
                            .padding(horizontal = 9.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = sequenceInput,
                            onValueChange = { raw ->
                                val next = raw.take(32)
                                sequenceInput = next
                                val parsed = parseColorSequence(next, CustomColorStore.colors)
                                if (parsed != null) {
                                    color = parsed
                                    sequenceError = false
                                } else {
                                    sequenceError = next.isNotBlank()
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp, color = LingyeColors.Ink, fontFamily = FontFamily.Monospace),
                            cursorBrush = SolidColor(LingyeColors.Primary),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            decorationBox = { inner ->
                                Box {
                                    if (sequenceInput.isEmpty()) {
                                        Text("#893F3F / 137,63,63 / 1", fontSize = 12.sp, color = LingyeColors.Muted)
                                    }
                                    inner()
                                }
                            }
                        )
                    }
                }
                Text(
                    if (sequenceError) "无法识别，请输入 #RRGGBB、R,G,B 或色卡序号"
                    else "输入颜色序列后立即取色；数字对应自定义色卡顺序",
                    Modifier.padding(start = 64.dp, top = 3.dp),
                    fontSize = 11.sp,
                    color = if (sequenceError) LingyeColors.Coral else LingyeColors.Muted
                )
                Spacer(Modifier.height(10.dp))
                ColorSectionHeader(
                    "RGB",
                    "R $r0   G $g0   B $b0",
                    rgbOpen
                ) { rgbOpen = !rgbOpen }
                if (rgbOpen) {
                    Spacer(Modifier.height(6.dp))
                    ColorSlider("R", r0.toFloat(), 0f..255f) { value ->
                        val (_, g, b) = rgbParts(color)
                        color = (0xFF shl 24) or (value.roundToInt() shl 16) or (g shl 8) or b
                    }
                    ColorSlider("G", g0.toFloat(), 0f..255f) { value ->
                        val (r, _, b) = rgbParts(color)
                        color = (0xFF shl 24) or (r shl 16) or (value.roundToInt() shl 8) or b
                    }
                    ColorSlider("B", b0.toFloat(), 0f..255f) { value ->
                        val (r, g, _) = rgbParts(color)
                        color = (0xFF shl 24) or (r shl 16) or (g shl 8) or value.roundToInt()
                    }
                }
                Spacer(Modifier.height(8.dp))
                ColorSectionHeader(
                    "HSV",
                    "H ${h0.roundToInt()}°   S ${s0.roundToInt()}%   V ${v0.roundToInt()}%",
                    hsvOpen
                ) { hsvOpen = !hsvOpen }
                if (hsvOpen) {
                    Spacer(Modifier.height(8.dp))
                    SaturationValueField(
                        color,
                        Modifier.fillMaxWidth().height(140.dp)
                    ) { saturation, value ->
                        color = hsvToRgb(h0, saturation, value)
                    }
                    Spacer(Modifier.height(8.dp))
                    HueBar(h0) { hue ->
                        color = hsvToRgb(hue, s0, v0)
                    }
                }
                Row(
                    Modifier.fillMaxWidth().toggleable(
                        value = saveToPalette,
                        role = Role.Switch,
                        onValueChange = { saveToPalette = it }
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("保存到自定义色卡", Modifier.weight(1f), fontSize = 13.sp, color = LingyeColors.Ink)
                    CompactSwitch(checked = saveToPalette)
                }
                Row(
                    Modifier.fillMaxWidth().toggleable(
                        value = showCustomPalette,
                        role = Role.Switch,
                        onValueChange = { showCustomPalette = it }
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("自定义色卡", Modifier.weight(1f), fontSize = 13.sp, color = LingyeColors.Ink)
                    if (CustomColorStore.colors.isNotEmpty()) {
                        Text("${CustomColorStore.colors.size}", Modifier.padding(end = 8.dp), fontSize = 11.sp, color = LingyeColors.Muted)
                    }
                    CompactSwitch(checked = showCustomPalette)
                }
                if (showCustomPalette) {
                    if (CustomColorStore.colors.isEmpty()) {
                        Text("还没有保存的颜色", Modifier.padding(vertical = 4.dp), fontSize = 12.sp, color = LingyeColors.Muted)
                    } else {
                        FlowRow(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            CustomColorStore.colors.forEachIndexed { index, swatch ->
                                ColorSwatch(swatch, sameRgb(color, swatch), "自定义颜色 ${index + 1}", 20.dp) { color = swatch }
                            }
                            TextButton(onClick = { CustomColorStore.clear() }, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)) {
                                Text("清空", color = LingyeColors.Muted, fontSize = 11.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("取消", color = LingyeColors.Muted) }
                    TextButton(onClick = {
                        if (saveToPalette) CustomColorStore.add(color)
                        onConfirm(color)
                    }) { Text("使用", color = LingyeColors.Primary, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
private fun SaturationValueField(
    color: Int,
    modifier: Modifier = Modifier,
    onChange: (saturation: Float, value: Float) -> Unit
) {
    val (hue, saturation, value) = rgbToHsv(color)
    val latestOnChange by rememberUpdatedState(onChange)
    fun update(position: Offset, width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        latestOnChange(
            (position.x / width * 100f).coerceIn(0f, 100f),
            ((1f - position.y / height) * 100f).coerceIn(0f, 100f)
        )
    }
    Canvas(
        modifier.clip(RoundedCornerShape(10.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { update(it, size.width, size.height) },
                    onDrag = { change, _ ->
                        change.consume()
                        update(change.position, size.width, size.height)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { update(it, size.width, size.height) }
            }
    ) {
        drawRect(Color(hsvToRgb(hue, 100f, 100f)))
        drawRect(Brush.horizontalGradient(listOf(Color.White, Color.Transparent)))
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        drawCircle(
            Color.White,
            radius = 7.dp.toPx(),
            center = Offset(saturation / 100f * size.width, (1f - value / 100f) * size.height),
            style = Stroke(2.dp.toPx())
        )
    }
}

@Composable
private fun HueBar(hue: Float, onChange: (Float) -> Unit) {
    val latestOnChange by rememberUpdatedState(onChange)
    fun update(position: Offset, width: Int) {
        if (width <= 0) return
        latestOnChange((position.x / width * 360f).coerceIn(0f, 360f))
    }
    Canvas(
        Modifier.fillMaxWidth().height(18.dp).clip(RoundedCornerShape(9.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { update(it, size.width) },
                    onDrag = { change, _ ->
                        change.consume()
                        update(change.position, size.width)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { update(it, size.width) }
            }
    ) {
        drawRect(
            Brush.horizontalGradient(
                listOf(
                    Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                    Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF),
                    Color(0xFFFF0000)
                )
            )
        )
        drawCircle(
            Color.White,
            radius = 7.dp.toPx(),
            center = Offset(hue / 360f * size.width, size.height / 2f),
            style = Stroke(2.dp.toPx())
        )
    }
}

@Composable
private fun ColorSectionHeader(
    title: String,
    summary: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(LingyeColors.Subtle)
            .clickable(onClick = onToggle).padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LingyeColors.Ink)
        Spacer(Modifier.width(8.dp))
        Text(summary, Modifier.weight(1f), fontSize = 11.sp, color = LingyeColors.Muted, maxLines = 1)
        Text(if (expanded) "▾" else "▸", fontSize = 13.sp, color = LingyeColors.Muted)
    }
}

@Composable
private fun SegmentedMode(
    selected: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    Row(Modifier.background(LingyeColors.Chip, RoundedCornerShape(14.dp)).padding(4.dp)) {
        options.forEach { (value, label) ->
            val active = value == selected
            Surface(
                onClick = { onSelect(value) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = if (active) LingyeColors.ControlSelected else Color.Transparent,
                shadowElevation = if (active) 2.dp else 0.dp
            ) {
                Box(Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text(
                        label,
                        color = if (active) LingyeColors.Primary else LingyeColors.Muted,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    suffix: String = "",
    onChange: (Float) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.width(20.dp), fontSize = 12.sp, color = LingyeColors.Muted)
        CompactSlider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${value.roundToInt()}$suffix",
            Modifier.width(52.dp),
            fontSize = 12.sp,
            color = LingyeColors.Ink,
            fontFamily = FontFamily.Monospace
        )
    }
}

private val PickerWhite = Color.White
private val PickerPanel = Color(0xFFF3F3F3)
private val PickerInk = Color(0xFF202020)
private val PickerMuted = Color(0xFF7A7A7A)
private val PickerLine = Color(0xFFE6E6E6)
private val PickerAccent = Color(0xFF4F91E8)
private val PickerError = Color(0xFFE05A5A)

private val PickerDefaultHistory = listOf(
    0xFF000000.toInt(), 0xFFE3C27A.toInt(), 0xFFF2C9A6.toInt(), 0xFFE8D7BE.toInt(),
    0xFFF7E2C8.toInt(), 0xFFFFFFFF.toInt(), 0xFF6B4A3A.toInt(), 0xFF3E2A22.toInt(),
    0xFF8B4F3A.toInt(), 0xFF5C3B32.toInt()
)

private val PickerNewborn: List<Int> = buildList {
    val hues = listOf(45f, 30f, 18f, 2f, 340f, 320f, 285f, 250f, 225f)
    listOf(28f, 45f, 62f).forEach { saturation ->
        hues.forEach { hue -> add(hsvToRgb(hue, saturation, 98f)) }
    }
}

private enum class ColorPickerTab(val label: String) {
    WHEEL("色盘"),
    RING("色环"),
    SLIDERS("滑块"),
    SWATCHES("色卡")
}

@Composable
fun ColorEditorDialog(
    initialColor: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var color by remember { mutableIntStateOf(initialColor or 0xFF000000.toInt()) }
    var tab by remember { mutableStateOf(ColorPickerTab.SLIDERS) }
    var sequenceInput by remember { mutableStateOf("") }
    var sequenceError by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val contentMax = (maxHeight * 0.76f - 200.dp).coerceAtLeast(150.dp)
            val density = LocalDensity.current
            val maxOffsetX = with(density) { 100.dp.toPx() }
            val maxOffsetY = with(density) { 24.dp.toPx() }
            Surface(
                Modifier.widthIn(max = 400.dp).fillMaxWidth(0.88f)
                    .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) },
                shape = RoundedCornerShape(24.dp),
                color = PickerWhite,
                shadowElevation = 22.dp
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Box(
                    Modifier.align(Alignment.CenterHorizontally).width(140.dp).height(20.dp)
                        .pointerInput(maxOffsetX, maxOffsetY) {
                            detectDragGestures { change, amount ->
                                change.consume()
                                dragOffset = Offset(
                                    (dragOffset.x + amount.x).coerceIn(-maxOffsetX, maxOffsetX),
                                    (dragOffset.y + amount.y).coerceIn(-maxOffsetY, maxOffsetY)
                                )
                            }
                        }
                        .semantics { contentDescription = "拖动颜色弹窗" },
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.width(34.dp).height(3.dp).background(PickerAccent, RoundedCornerShape(2.dp)))
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("颜色", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PickerInk)
                    Spacer(Modifier.weight(1f))
                    PickerPreview(initialColor, 22.dp)
                    Spacer(Modifier.width(6.dp))
                    PickerPreview(color, 26.dp)
                }
                Spacer(Modifier.height(10.dp))
                PickerTabs(tab) { tab = it }
                Spacer(Modifier.height(12.dp))
                Column(Modifier.heightIn(max = contentMax).verticalScroll(rememberScrollState())) {
                    when (tab) {
                        ColorPickerTab.WHEEL -> PickerWheelPage(color) { color = it }
                        ColorPickerTab.RING -> PickerRingPage(color) { color = it }
                        ColorPickerTab.SLIDERS -> PickerSliderPage(color) { color = it }
                        ColorPickerTab.SWATCHES -> Text(
                            "从下方历史色彩或新生配色中选择",
                            Modifier.padding(vertical = 4.dp),
                            fontSize = 12.sp,
                            color = PickerMuted
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    PickerHistory(color) { color = it }
                    Spacer(Modifier.height(12.dp))
                    PickerNewbornSection(color) { color = it }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Hex", Modifier.width(32.dp), fontSize = 12.sp, color = PickerMuted)
                    Box(
                        Modifier.weight(1f).height(32.dp)
                            .background(PickerPanel, RoundedCornerShape(8.dp))
                            .border(1.dp, if (sequenceError) PickerError else PickerLine, RoundedCornerShape(8.dp))
                            .padding(horizontal = 9.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = sequenceInput,
                            onValueChange = { raw ->
                                val next = raw.take(32)
                                sequenceInput = next
                                val parsed = parseColorSequence(next, CustomColorStore.colors)
                                if (parsed != null) {
                                    color = parsed
                                    sequenceError = false
                                } else {
                                    sequenceError = next.isNotBlank()
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp, color = PickerInk, fontFamily = FontFamily.Monospace),
                            cursorBrush = SolidColor(PickerAccent),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            decorationBox = { inner ->
                                Box {
                                    if (sequenceInput.isEmpty()) {
                                        Text("#893F3F / 137,63,63 / 1", fontSize = 12.sp, color = PickerMuted)
                                    }
                                    inner()
                                }
                            }
                        )
                    }
                }
                Text(
                    if (sequenceError) "无法识别，请输入 #RRGGBB、R,G,B 或色卡序号"
                    else "输入颜色序列后立即取色；数字对应自定义色卡顺序",
                    Modifier.padding(start = 32.dp, top = 3.dp),
                    fontSize = 10.5.sp,
                    color = if (sequenceError) PickerError else PickerMuted
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("取消", color = PickerMuted) }
                    TextButton(onClick = {
                        CustomColorStore.add(color)
                        onConfirm(color)
                    }) { Text("使用", color = PickerAccent, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}
}

@Composable
private fun PickerPreview(color: Int, size: Dp) {
    Box(
        Modifier.size(size).background(Color(color or 0xFF000000.toInt()), RoundedCornerShape(6.dp))
            .border(1.dp, PickerLine, RoundedCornerShape(6.dp))
    )
}

@Composable
private fun PickerTabs(selected: ColorPickerTab, onSelect: (ColorPickerTab) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(PickerPanel, RoundedCornerShape(18.dp)).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        ColorPickerTab.values().forEach { tab ->
            val active = tab == selected
            Surface(
                onClick = { onSelect(tab) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(15.dp),
                color = if (active) PickerWhite else Color.Transparent,
                shadowElevation = if (active) 2.dp else 0.dp
            ) {
                Box(Modifier.height(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        tab.label,
                        fontSize = 13.sp,
                        color = if (active) PickerInk else PickerMuted,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerSliderPage(color: Int, onChange: (Int) -> Unit) {
    val (hue, saturation, value) = rgbToHsv(color)
    Column {
        SaturationValueField(color, Modifier.fillMaxWidth().height(130.dp)) { s, v ->
            onChange(hsvToRgb(hue, s, v))
        }
        Spacer(Modifier.height(10.dp))
        PickerGradientSlider(
            "H", hue / 360f,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                    Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000)
                )
            ),
            "${hue.roundToInt()}°"
        ) { onChange(hsvToRgb((it * 360f).coerceIn(0f, 360f), saturation, value)) }
        Spacer(Modifier.height(6.dp))
        PickerGradientSlider(
            "S", saturation / 100f,
            Brush.horizontalGradient(listOf(PickerWhite, Color(hsvToRgb(hue, 100f, 100f)))),
            "${saturation.roundToInt()}%"
        ) { onChange(hsvToRgb(hue, (it * 100f).coerceIn(0f, 100f), value)) }
        Spacer(Modifier.height(6.dp))
        PickerGradientSlider(
            "V", value / 100f,
            Brush.horizontalGradient(listOf(Color.Black, Color(hsvToRgb(hue, saturation, 100f)))),
            "${value.roundToInt()}%"
        ) { onChange(hsvToRgb(hue, saturation, (it * 100f).coerceIn(0f, 100f))) }
    }
}

@Composable
private fun PickerWheelPage(color: Int, onChange: (Int) -> Unit) {
    val (hue, saturation, value) = rgbToHsv(color)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PickerColorWheel(color, Modifier.size(180.dp)) { h, s ->
            onChange(hsvToRgb(h, s, value))
        }
        Spacer(Modifier.height(10.dp))
        PickerGradientSlider(
            "V", value / 100f,
            Brush.horizontalGradient(listOf(Color.Black, Color(hsvToRgb(hue, saturation, 100f)))),
            "${value.roundToInt()}%"
        ) { onChange(hsvToRgb(hue, saturation, (it * 100f).coerceIn(0f, 100f))) }
    }
}

@Composable
private fun PickerRingPage(color: Int, onChange: (Int) -> Unit) {
    val (hue, saturation, value) = rgbToHsv(color)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PickerHueRing(hue, color, Modifier.size(170.dp)) { h ->
            onChange(hsvToRgb(h, saturation, value))
        }
        Spacer(Modifier.height(10.dp))
        PickerGradientSlider(
            "S", saturation / 100f,
            Brush.horizontalGradient(listOf(PickerWhite, Color(hsvToRgb(hue, 100f, 100f)))),
            "${saturation.roundToInt()}%"
        ) { onChange(hsvToRgb(hue, (it * 100f).coerceIn(0f, 100f), value)) }
        Spacer(Modifier.height(6.dp))
        PickerGradientSlider(
            "V", value / 100f,
            Brush.horizontalGradient(listOf(Color.Black, Color(hsvToRgb(hue, saturation, 100f)))),
            "${value.roundToInt()}%"
        ) { onChange(hsvToRgb(hue, saturation, (it * 100f).coerceIn(0f, 100f))) }
    }
}

@Composable
private fun PickerGradientSlider(
    label: String,
    fraction: Float,
    brush: Brush,
    valueText: String,
    onChange: (Float) -> Unit
) {
    val latestOnChange by rememberUpdatedState(onChange)
    fun update(position: Offset, width: Int) {
        if (width <= 0) return
        latestOnChange((position.x / width).coerceIn(0f, 1f))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.width(14.dp), fontSize = 11.sp, color = PickerMuted)
        Canvas(
            Modifier.weight(1f).height(18.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { update(it, size.width) },
                        onDrag = { change, _ ->
                            change.consume()
                            update(change.position, size.width)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { update(it, size.width) }
                }
        ) {
            val barHeight = 14.dp.toPx()
            val top = (size.height - barHeight) / 2f
            drawRoundRect(
                brush = brush,
                topLeft = Offset(0f, top),
                size = androidx.compose.ui.geometry.Size(size.width, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(7.dp.toPx())
            )
            val x = fraction.coerceIn(0f, 1f) * size.width
            drawCircle(PickerWhite, 8.dp.toPx(), Offset(x, size.height / 2f), style = Stroke(2.dp.toPx()))
            drawCircle(PickerAccent, 3.dp.toPx(), Offset(x, size.height / 2f))
        }
        Text(
            valueText,
            Modifier.width(44.dp),
            fontSize = 11.sp,
            color = PickerInk,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun PickerColorWheel(color: Int, modifier: Modifier = Modifier, onChange: (Float, Float) -> Unit) {
    val (hue, saturation, _) = rgbToHsv(color)
    val latestOnChange by rememberUpdatedState(onChange)
    val density = LocalDensity.current.density
    fun update(position: Offset, width: Int, height: Int) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = (minOf(width, height) / 2f) - 4f * density
        if (radius <= 0f) return
        val dx = position.x - cx
        val dy = position.y - cy
        val distance = kotlin.math.hypot(dx, dy)
        val angle = ((atan2(dy, dx) * 180f / PI.toFloat()) + 360f) % 360f
        latestOnChange(angle, (distance / radius * 100f).coerceIn(0f, 100f))
    }
    Canvas(
        modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { update(it, size.width, size.height) },
                    onDrag = { change, _ ->
                        change.consume()
                        update(change.position, size.width, size.height)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { update(it, size.width, size.height) }
            }
    ) {
        val radius = minOf(size.width, size.height) / 2f - 4.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
            Brush.sweepGradient(
                listOf(
                    Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                    Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000)
                ),
                center
            ),
            radius,
            center
        )
        drawCircle(Brush.radialGradient(listOf(PickerWhite, Color.Transparent), center, radius), radius, center)
        val angle = hue * PI.toFloat() / 180f
        val distance = saturation / 100f * radius
        val thumb = Offset(center.x + cos(angle) * distance, center.y + sin(angle) * distance)
        drawCircle(PickerWhite, 7.dp.toPx(), thumb, style = Stroke(2.dp.toPx()))
    }
}

@Composable
private fun PickerHueRing(hue: Float, color: Int, modifier: Modifier = Modifier, onChange: (Float) -> Unit) {
    val latestOnChange by rememberUpdatedState(onChange)
    fun update(position: Offset, width: Int, height: Int) {
        val dx = position.x - width / 2f
        val dy = position.y - height / 2f
        val angle = ((atan2(dy, dx) * 180f / PI.toFloat()) + 360f) % 360f
        latestOnChange(angle)
    }
    Canvas(
        modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { update(it, size.width, size.height) },
                    onDrag = { change, _ ->
                        change.consume()
                        update(change.position, size.width, size.height)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { update(it, size.width, size.height) }
            }
    ) {
        val outerRadius = minOf(size.width, size.height) / 2f - 4.dp.toPx()
        val stroke = 24.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
            Brush.sweepGradient(
                listOf(
                    Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                    Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000)
                ),
                center
            ),
            outerRadius - stroke / 2f,
            center,
            style = Stroke(stroke)
        )
        drawCircle(PickerWhite, outerRadius - stroke - 2.dp.toPx(), center)
        drawCircle(Color(color), outerRadius - stroke - 10.dp.toPx(), center)
        val angle = hue * PI.toFloat() / 180f
        val thumb = Offset(
            center.x + cos(angle) * (outerRadius - stroke / 2f),
            center.y + sin(angle) * (outerRadius - stroke / 2f)
        )
        drawCircle(PickerWhite, 8.dp.toPx(), thumb, style = Stroke(2.dp.toPx()))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PickerHistory(color: Int, onPick: (Int) -> Unit) {
    val colors = if (CustomColorStore.colors.isEmpty()) PickerDefaultHistory else CustomColorStore.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("历史色彩", fontSize = 12.sp, color = PickerMuted)
        Spacer(Modifier.weight(1f))
        if (CustomColorStore.colors.isNotEmpty()) {
            TextButton(
                onClick = { CustomColorStore.clear() },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
            ) { Text("清空", fontSize = 11.sp, color = PickerMuted) }
        }
    }
    Spacer(Modifier.height(4.dp))
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        colors.take(12).forEachIndexed { index, swatch ->
            PickerSwatch(swatch, sameRgb(color, swatch), "历史颜色 ${index + 1}") { onPick(swatch) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PickerNewbornSection(color: Int, onPick: (Int) -> Unit) {
    Text("新生", fontSize = 12.sp, color = PickerMuted)
    Spacer(Modifier.height(4.dp))
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PickerNewborn.forEachIndexed { index, swatch ->
            PickerSwatch(swatch, sameRgb(color, swatch), "新生颜色 ${index + 1}") { onPick(swatch) }
        }
    }
}

@Composable
private fun PickerSwatch(color: Int, selected: Boolean, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(26.dp).clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(20.dp)
                .background(Color(color or 0xFF000000.toInt()), RoundedCornerShape(6.dp))
                .border(
                    if (selected) 2.dp else 1.dp,
                    if (selected) PickerAccent else PickerLine,
                    RoundedCornerShape(6.dp)
                )
        )
    }
}
