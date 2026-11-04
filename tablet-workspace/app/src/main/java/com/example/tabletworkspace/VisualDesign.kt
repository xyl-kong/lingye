package com.example.tabletworkspace

import android.app.Activity
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.annotation.DrawableRes
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import kotlin.math.roundToInt
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

object LingyeColors {
    private var darkState by mutableStateOf(false)
    val dark: Boolean get() = darkState

    fun setDark(value: Boolean) {
        if (darkState != value) darkState = value
    }

    private fun pick(light: Long, dark: Long): Color = Color(if (darkState) dark else light)

    val Ink get() = pick(0xFF242424, 0xFFE9E9EC)
    val Muted get() = pick(0xFF777777, 0xFF9B9BA3)
    val Primary get() = pick(0xFF4F91E8, 0xFF7FB2F5)
    val PrimarySoft get() = pick(0xFFEAF3FF, 0xFF1D2C40)
    val Mint get() = pick(0xFF008F88, 0xFF5FD3C8)
    val MintSoft get() = pick(0xFFDDF7F3, 0xFF18322F)
    val Coral get() = pick(0xFFF26B63, 0xFFFF8C84)
    val Canvas get() = pick(0xFFF8F8F8, 0xFF111113)
    val Line get() = pick(0xFFE5E5E5, 0xFF34343A)

    val Background get() = pick(0xFFFFFFFF, 0xFF121214)
    val Surface get() = pick(0xFFFFFFFF, 0xFF1B1B1E)
    val SurfaceHigh get() = pick(0xFFFFFFFF, 0xFF26262B)
    val Sidebar get() = pick(0xFFFAFAFA, 0xFF171719)
    val SettingsSidebar get() = pick(0xFFF7F7F9, 0xFF171719)
    val Subtle get() = pick(0xFFF0F0F0, 0xFF2A2A2F)
    val SubtleAlt get() = pick(0xFFF4F3FA, 0xFF26262B)
    val Chip get() = pick(0xFFF3F3F9, 0xFF2A2A30)
    val ModeSwitchBg get() = pick(0xFFF5F5F5, 0xFF2A2A2F)
    val ButtonSoft get() = pick(0xFFF2F2F2, 0xFF2A2A2F)
    val ControlSelected get() = pick(0xFFFFFFFF, 0xFF34343B)
    val SliderTrack get() = pick(0xFFE9E9F0, 0xFF333338)
    val DotInactive get() = pick(0xFFB4B7BD, 0xFF77777F)
    val GridDot get() = pick(0xFFE7E7E7, 0xFF2E2E33)
    val NavSelected get() = pick(0xFFECECF2, 0xFF2C2C32)

    val SegmentSelected get() = pick(0xFF0B0B0B, 0xFFF1F1F3)
    val OnSegmentSelected get() = pick(0xFFFFFFFF, 0xFF101013)
    val SegmentInk get() = pick(0xFF1A1A1A, 0xFFD6D6DA)
    val DisabledInk get() = pick(0xFF9C9C9C, 0xFF6E6E76)
    val SegmentBorder get() = pick(0xFFE4E4E4, 0xFF3A3A40)

    val SwitchDisabled get() = pick(0xFFF1F1F2, 0xFF2A2A2E)
    val SwitchOn get() = pick(0xFF0B0B0B, 0xFFF1F1F3)
    val SwitchOff get() = pick(0xFFE9E9EC, 0xFF2C2C31)
    val SwitchOffBorder get() = pick(0xFFD5D5DA, 0xFF3A3A40)
    val SwitchThumbOn get() = pick(0xFFFFFFFF, 0xFF111113)
    val SwitchThumbOff get() = pick(0xFFFFFFFF, 0xFF8A8A92)
    val SwitchThumbBorder get() = pick(0xFFB8B8C0, 0xFF55555C)

    val Tooltip get() = pick(0xFF20293A, 0xFF2E2E34)
    val ErrorSoft get() = pick(0xFFFFE6E6, 0xFF3A1E1E)
    val ErrorInk get() = pick(0xFF9B2222, 0xFFFFB4AE)
    val SuccessSoft get() = pick(0xFFE5F5EA, 0xFF17301F)
    val SuccessInk get() = pick(0xFF226637, 0xFF9FD9AE)
    val DangerSoft get() = pick(0xFFFFF0EF, 0xFF3A2321)
    val SurfaceBlue get() = pick(0xFFF0F4FC, 0xFF1E2633)
    val SurfaceSoft get() = pick(0xFFF5F5FA, 0xFF242429)
    val PreviewBg get() = pick(0xFFF2F2F7, 0xFF1A1A1E)
    val LoadingGradientStart get() = pick(0xFFF8F6FF, 0xFF15151A)
    val LoadingGradientEnd get() = pick(0xFFF2FBF9, 0xFF101816)

    val OnPrimary get() = pick(0xFFFFFFFF, 0xFF101013)
    val OnSecondary get() = pick(0xFFFFFFFF, 0xFF101013)
    val SurfaceVariant get() = pick(0xFFF0F1F7, 0xFF2A2A2F)
}

@Composable
fun LingyeTheme(content: @Composable () -> Unit) {
    val dark = LingyeColors.dark
    val view = LocalView.current
    val context = LocalContext.current
    SideEffect {
        val window = (context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !dark
    }
    val scheme = if (dark) darkColorScheme(
        primary = LingyeColors.Primary,
        onPrimary = LingyeColors.OnPrimary,
        primaryContainer = LingyeColors.PrimarySoft,
        onPrimaryContainer = LingyeColors.Ink,
        secondary = LingyeColors.Mint,
        onSecondary = LingyeColors.OnSecondary,
        secondaryContainer = LingyeColors.MintSoft,
        onSecondaryContainer = LingyeColors.Ink,
        error = LingyeColors.Coral,
        onError = LingyeColors.OnPrimary,
        background = LingyeColors.Canvas,
        onBackground = LingyeColors.Ink,
        surface = LingyeColors.Surface,
        surfaceContainer = LingyeColors.SurfaceHigh,
        surfaceContainerLow = LingyeColors.Surface,
        surfaceContainerHigh = LingyeColors.SurfaceHigh,
        surfaceContainerHighest = LingyeColors.SurfaceHigh,
        onSurface = LingyeColors.Ink,
        surfaceVariant = LingyeColors.SurfaceVariant,
        onSurfaceVariant = LingyeColors.Muted,
        outline = LingyeColors.Line
    ) else lightColorScheme(
        primary = LingyeColors.Primary,
        onPrimary = LingyeColors.OnPrimary,
        primaryContainer = LingyeColors.PrimarySoft,
        onPrimaryContainer = LingyeColors.Ink,
        secondary = LingyeColors.Mint,
        onSecondary = LingyeColors.OnSecondary,
        secondaryContainer = LingyeColors.MintSoft,
        error = LingyeColors.Coral,
        background = LingyeColors.Canvas,
        onBackground = LingyeColors.Ink,
        surface = LingyeColors.Surface,
        surfaceContainer = LingyeColors.SurfaceHigh,
        surfaceContainerLow = LingyeColors.Surface,
        surfaceContainerHigh = LingyeColors.SurfaceHigh,
        onSurface = LingyeColors.Ink,
        surfaceVariant = LingyeColors.SurfaceVariant,
        outline = LingyeColors.Line
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
fun AmbientBackdrop() {
    val motionAllowed = Settings.Global.getFloat(
        LocalContext.current.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    ) != 0f
    val phase by produceState(initialValue = 0f, motionAllowed) {
        if (!motionAllowed) return@produceState
        var lastUpdate = 0L
        while (true) withFrameNanos { now ->
            if (lastUpdate == 0L) lastUpdate = now
            if (now - lastUpdate >= 80_000_000L) {
                value = ((now % 22_000_000_000L) / 22_000_000_000.0 * PI * 2.0).toFloat()
                lastUpdate = now
            }
        }
    }
    val angle = if (motionAllowed) phase else 0.8f
    val blueGlow = if (LingyeColors.dark) Color(0x33355FD6) else Color(0x265E54DA)
    val tealGlow = if (LingyeColors.dark) Color(0x2A18A89A) else Color(0x1F09A69A)
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(blueGlow, Color.Transparent),
                center = Offset(w * (0.12f + sin(angle) * 0.025f), h * (0.16f + cos(angle) * 0.03f)),
                radius = min(w, h) * 0.38f
            ),
            radius = min(w, h) * 0.38f,
            center = Offset(w * (0.12f + sin(angle) * 0.025f), h * (0.16f + cos(angle) * 0.03f))
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(tealGlow, Color.Transparent),
                center = Offset(w * (0.92f - cos(angle) * 0.03f), h * (0.78f + sin(angle) * 0.025f)),
                radius = min(w, h) * 0.34f
            ),
            radius = min(w, h) * 0.34f,
            center = Offset(w * (0.92f - cos(angle) * 0.03f), h * (0.78f + sin(angle) * 0.025f))
        )
    }
}

@Composable
fun GradientAddButton(text: String, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(8.dp),
        color = LingyeColors.ButtonSoft,
        shadowElevation = 0.dp
    ) {
        Row(
            Modifier
                .padding(horizontal = 16.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WorkspaceIcon(AppIcon.ADD, contentDescription = null, tint = LingyeColors.Ink, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(text, color = LingyeColors.Ink, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private class AboveAnchorPositionProvider(private val gapPx: Int) : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
        val x = (anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2).coerceAtLeast(4)
        val y = (anchorBounds.top - popupContentSize.height - gapPx).coerceAtLeast(4)
        return IntOffset(x, y)
    }
}

@Composable
private fun ShortcutTooltip(label: String, shortcut: String?) {
    Surface(color = LingyeColors.Tooltip, shape = RoundedCornerShape(8.dp), shadowElevation = 6.dp) {
        Row(
            Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(label, color = Color.White, fontSize = 12.sp, maxLines = 1)
            if (!shortcut.isNullOrBlank()) {
                Text(
                    shortcut,
                    color = Color.White.copy(alpha = 0.94f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/** Shows the official-style tooltip (name + keycap) on hover or long press. */
@Composable
private fun ShortcutTooltipHost(label: String, shortcut: String?, content: @Composable (onLongPress: () -> Unit) -> Unit) {
    var hovered by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(hovered) { if (hovered) { delay(320); visible = true } else visible = false }
    val onLongPress: () -> Unit = {
        scope.launch {
            visible = true
            delay(1600)
            if (!hovered) visible = false
        }
    }
    val gap = with(LocalDensity.current) { 10.dp.roundToPx() }
    Box(Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                when (event.type) {
                    PointerEventType.Enter -> hovered = true
                    PointerEventType.Exit -> hovered = false
                    else -> Unit
                }
            }
        }
    }) {
        content(onLongPress)
        if (visible) {
            Popup(
                popupPositionProvider = AboveAnchorPositionProvider(gap),
                properties = PopupProperties(focusable = false, dismissOnClickOutside = false, dismissOnBackPress = false, clippingEnabled = false)
            ) { ShortcutTooltip(label, shortcut) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolIconButton(
    icon: AppIcon,
    label: String,
    selected: Boolean,
    showLabel: Boolean = true,
    shortcut: String? = null,
    onClick: () -> Unit
) {
    val background = if (selected) LingyeColors.Subtle else Color.Transparent
    ShortcutTooltipHost(label, shortcut) { onLongPress ->
        Box(
            modifier = Modifier
                .width(if (showLabel) 60.dp else CanvasUiTokens.toolbarButtonWidth)
                .height(CanvasUiTokens.toolbarButtonHeight)
                .combinedClickable(onClick = onClick, onLongClick = onLongPress)
                .semantics { contentDescription = label },
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(if (showLabel) 46.dp else 32.dp, 32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(background)
            )
            Column(
                Modifier.fillMaxSize().padding(horizontal = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                WorkspaceIcon(
                    icon,
                    contentDescription = null,
                    tint = if (selected) LingyeColors.Primary else LingyeColors.Muted,
                    modifier = Modifier.size(20.dp)
                )
                if (showLabel) {
                    Spacer(Modifier.height(1.dp))
                    Text(
                        label,
                        color = if (selected) LingyeColors.Primary else LingyeColors.Muted,
                        fontSize = 9.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun IconTextAction(
    icon: AppIcon,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = LingyeColors.Ink
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Row(
            Modifier.heightIn(min = 30.dp).padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            WorkspaceIcon(
                icon,
                contentDescription = null,
                tint = if (enabled) tint else LingyeColors.Muted.copy(alpha = 0.45f),
                modifier = Modifier.size(16.dp)
            )
            if (label.isNotEmpty()) {
                Text(
                    label,
                    color = if (enabled) tint else LingyeColors.Muted.copy(alpha = 0.45f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CompactIconAction(
    icon: AppIcon,
    description: String,
    onClick: () -> Unit,
    tint: Color = LingyeColors.Muted,
    selected: Boolean = false,
    enabled: Boolean = true,
    selectedBackground: Color = LingyeColors.PrimarySoft
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(CanvasUiTokens.compactIconSize),
        shape = RoundedCornerShape(50),
        color = if (selected) selectedBackground else Color.Transparent,
        enabled = enabled
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            WorkspaceIcon(
                icon,
                contentDescription = description,
                tint = (if (selected) LingyeColors.Primary else tint).copy(alpha = if (enabled) 1f else 0.38f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SwipeActionRow(
    key: Any,
    revealWidth: Dp,
    actionVerticalOffset: Dp = 0.dp,
    modifier: Modifier = Modifier,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
    content: @Composable (revealed: () -> Boolean) -> Unit
) {
    val revealPx = with(androidx.compose.ui.platform.LocalDensity.current) { revealWidth.toPx() }
    var dragOffset by remember(key) { mutableStateOf(0f) }
    val renderedOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = tween(110),
        label = "swipe-action-offset"
    )
    Box(modifier.fillMaxWidth().clipToBounds()) {
        if (renderedOffset < -0.5f) {
            Row(
                Modifier.fillMaxSize().offset(y = actionVerticalOffset).padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .zIndex(1f)
                .offset { IntOffset(renderedOffset.roundToInt(), 0) }
                .pointerInput(key, revealPx) {
                    detectHorizontalDragGestures(
                        onDragCancel = {
                            dragOffset = if (dragOffset < -revealPx / 3f) -revealPx else 0f
                        },
                        onDragEnd = {
                            dragOffset = if (dragOffset < -revealPx / 3f) -revealPx else 0f
                        }
                    ) { change, amount ->
                        change.consume()
                        dragOffset = (dragOffset + amount).coerceIn(-revealPx, 0f)
                    }
                }
        ) {
            content { dragOffset < -0.5f }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IllustratedToolButton(
    @DrawableRes imageRes: Int,
    label: String,
    selected: Boolean,
    shortcut: String? = null,
    onClick: () -> Unit
) {
    val background = if (selected) LingyeColors.Subtle else Color.Transparent
    ShortcutTooltipHost(label, shortcut) { onLongPress ->
        Box(
            modifier = Modifier
                .size(CanvasUiTokens.illustratedButtonSize)
                .combinedClickable(onClick = onClick, onLongClick = onLongPress)
                .semantics { contentDescription = label },
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(background)
            )
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun CompactSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    require(valueRange.start < valueRange.endInclusive) { "滑块范围无效" }
    val latestOnChange by rememberUpdatedState(onValueChange)
    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    fun snap(raw: Float): Float {
        if (steps <= 0) return raw.coerceIn(valueRange.start, valueRange.endInclusive)
        val step = (valueRange.endInclusive - valueRange.start) / (steps + 1)
        return (valueRange.start + ((raw - valueRange.start) / step).roundToInt() * step)
            .coerceIn(valueRange.start, valueRange.endInclusive)
    }
    val interaction = if (enabled) {
        Modifier
            .pointerInput(valueRange, steps) {
                detectDragGestures(
                    onDragStart = { position ->
                        val inset = 8.dp.toPx()
                        val usable = (size.width - 2 * inset).coerceAtLeast(1f)
                        latestOnChange(snap(valueRange.start + ((position.x - inset) / usable).coerceIn(0f, 1f) * (valueRange.endInclusive - valueRange.start)))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val inset = 8.dp.toPx()
                        val usable = (size.width - 2 * inset).coerceAtLeast(1f)
                        latestOnChange(snap(valueRange.start + ((change.position.x - inset) / usable).coerceIn(0f, 1f) * (valueRange.endInclusive - valueRange.start)))
                    }
                )
            }
            .pointerInput(valueRange, steps) {
                detectTapGestures { position ->
                    val inset = 8.dp.toPx()
                    val usable = (size.width - 2 * inset).coerceAtLeast(1f)
                    latestOnChange(snap(valueRange.start + ((position.x - inset) / usable).coerceIn(0f, 1f) * (valueRange.endInclusive - valueRange.start)))
                }
            }
    } else Modifier
    Canvas(
        modifier
            .height(26.dp)
            .fillMaxWidth()
            .then(interaction)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                progressBarRangeInfo = ProgressBarRangeInfo(coerced, valueRange, steps)
                if (enabled) {
                    setProgress { target ->
                        latestOnChange(snap(target))
                        true
                    }
                }
            }
    ) {
        val inset = 8.dp.toPx()
        val trackHeight = 3.dp.toPx()
        val y = size.height / 2f
        val usable = (size.width - 2 * inset).coerceAtLeast(1f)
        val fraction = ((coerced - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
        val alpha = if (enabled) 1f else 0.45f
        drawRoundRect(
            LingyeColors.SliderTrack.copy(alpha = alpha),
            topLeft = Offset(inset, y - trackHeight / 2f),
            size = Size(usable, trackHeight),
            cornerRadius = CornerRadius(trackHeight / 2f)
        )
        if (fraction > 0f) {
            drawRoundRect(
                LingyeColors.Primary.copy(alpha = alpha),
                topLeft = Offset(inset, y - trackHeight / 2f),
                size = Size(usable * fraction, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f)
            )
        }
        drawCircle(
            LingyeColors.Primary.copy(alpha = alpha),
            radius = 5.dp.toPx(),
            center = Offset(inset + usable * fraction, y)
        )
    }
}

@Composable
fun CompactSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled -> LingyeColors.SwitchDisabled
            checked -> LingyeColors.SwitchOn
            else -> LingyeColors.SwitchOff
        },
        animationSpec = tween(160),
        label = "compact-switch-track"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 19.dp else 3.dp,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 700f),
        label = "compact-switch-thumb"
    )
    val interaction = if (onCheckedChange != null) {
        Modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Switch,
            onValueChange = onCheckedChange
        )
    } else Modifier
    Box(
        modifier
            .size(width = 44.dp, height = 30.dp)
            .then(interaction),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .width(38.dp)
                .height(22.dp)
                .clip(CircleShape)
                .background(trackColor)
                .border(
                    1.dp,
                    if (checked) Color.Transparent else LingyeColors.SwitchOffBorder,
                    CircleShape
                )
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = thumbOffset)
                    .size(16.dp)
                    .shadow(1.dp, CircleShape)
                    .background(
                        when {
                            !enabled -> LingyeColors.SwitchThumbOff.copy(alpha = 0.7f)
                            checked -> LingyeColors.SwitchThumbOn
                            else -> LingyeColors.SwitchThumbOff
                        },
                        CircleShape
                    )
                    .border(
                        0.5.dp,
                        if (checked) Color.Transparent else LingyeColors.SwitchThumbBorder,
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun ModeSwitcherButton(
    icon: AppIcon,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (selected) LingyeColors.PrimarySoft else Color.Transparent,
        animationSpec = tween(200),
        label = "mode-background"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) LingyeColors.Primary else LingyeColors.Muted,
        animationSpec = tween(180),
        label = "mode-icon-color"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 0.92f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = 620f),
        label = "mode-icon-scale"
    )
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = background
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 9.dp),
            horizontalArrangement = if (centered) Arrangement.Center else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WorkspaceIcon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            )
            Spacer(Modifier.width(7.dp))
            Text(
                label,
                color = if (selected) LingyeColors.Primary else LingyeColors.Muted,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

enum class AppIcon {
    DOCUMENT, CANVAS, SEARCH, BACKUP, RESTORE, FOLDER, PAGES, TRASH, SIDEBAR,
    UNDO, REDO, MOVE, SELECT, PEN, ERASER, RECTANGLE, ELLIPSE, CONNECT, CURVE, STEP, STRAIGHT, REVERSE, SPARK,
    IMAGE, CARD, RESET, LANGUAGE, ADD, IMPORT, MINUS, MENU, EDIT, DATABASE, TABLE, BOARD, FRAME, SHAPE, TEXT, DIAMOND, TRIANGLE, ROUNDED_RECTANGLE,
    RECORD, FIELD, CLOSE, SAVE, MORE, END_DOT, END_ARROW, END_DIAMOND, END_TRIANGLE, END_NONE, COPY,
    OVERVIEW, SETTINGS, LAYER_UP, LAYER_DOWN, LOCK, EXPAND_FULL, EXPAND_CLOSE,
    FAVORITE, FAVORITED, TAG, INFO, LINK, BULLETED_LIST, NUMBERED_LIST, QUOTE, CODE_BLOCK, DIVIDER
}

private val affineIconPathCache = mutableMapOf<AppIcon, List<Path>>()

private fun cachedAffinePaths(icon: AppIcon): List<Path> = synchronized(affineIconPathCache) {
    affineIconPathCache.getOrPut(icon) {
        affinePaths(icon).map { pathData ->
            PathParser().parsePathString(pathData).toPath().also { path ->
                path.fillType = PathFillType.EvenOdd
            }
        }
    }
}

@Composable
fun WorkspaceIcon(
    icon: AppIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LingyeColors.Ink
) {
    val glyphPaths = remember(icon) { cachedAffinePaths(icon) }
    Canvas(modifier.semantics { if (contentDescription != null) this.contentDescription = contentDescription }) {
        val glyphScale = size.minDimension / 24f
        withTransform({
            translate(
                (size.width - 24f * glyphScale) / 2f,
                (size.height - 24f * glyphScale) / 2f
            )
            scale(glyphScale, glyphScale, Offset.Zero)
        }) {
            glyphPaths.forEach { path -> drawPath(path, tint) }
        }
    }
}

