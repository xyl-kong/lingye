package com.example.tabletworkspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class ToolbarAlign { Start, Center }

/** Screen-space anchor for an object toolbar, expressed in dp relative to the canvas. */
data class CanvasToolbarAnchor(
    val x: Dp,
    val y: Dp,
    val bottom: Dp = y,
    val align: ToolbarAlign = ToolbarAlign.Start
)

data class ToolbarPlacement(val x: Float, val y: Float, val below: Boolean)

object ContextualToolbarTokens {
    val margin = 12.dp
    val gap = 8.dp
    /** Space reserved for the bottom tool strip so an object toolbar never covers it. */
    val bottomReserve = 92.dp
    val radius = 17.dp
    val shadow = 13.dp
    /** Trigger-anchored menus: standard object menus / bottom toolbar menus. */
    val menuGap = 8.dp
    val bottomMenuGap = 8.dp
}

/** Shared visual tokens for tool buttons, parameter bars and bottom chrome. */
object CanvasUiTokens {
    val toolbarButtonHeight = 40.dp
    val toolbarButtonWidth = 40.dp
    val illustratedButtonSize = 40.dp
    val compactIconSize = 34.dp
    val parameterSurfaceRadius = 14.dp
    val parameterChipHeight = 30.dp
    val parameterWidthDot = 28.dp
    val bottomBarBottomPadding = 28.dp
    val zoomBottomPadding = 36.dp
}

/**
 * Bottom-toolbar secondary menu. Opens above its trigger with the same 8dp surface
 * padding and 32dp rows as the text selection menus so every canvas popup shares one rhythm.
 */
@Composable
fun ContextualMenuHost(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    gap: Dp = ContextualToolbarTokens.menuGap,
    width: Dp = 228.dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!expanded) return
    val density = LocalDensity.current
    val gapPx = with(density) { gap.roundToPx() }
    val provider = remember(gapPx) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val x = anchorBounds.left.coerceIn(8, maxOf(8, windowSize.width - popupContentSize.width - 8))
                val above = anchorBounds.top - popupContentSize.height - gapPx
                val y = if (above >= 8) {
                    above
                } else {
                    (anchorBounds.bottom + gapPx).coerceAtMost(maxOf(8, windowSize.height - popupContentSize.height - 8))
                }
                return IntOffset(x, y)
            }
        }
    }
    Popup(
        popupPositionProvider = provider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = false, dismissOnClickOutside = false, dismissOnBackPress = false)
    ) {
        Surface(
            modifier.width(width),
            shape = RoundedCornerShape(8.dp),
            color = LingyeColors.SurfaceHigh,
            border = BorderStroke(1.dp, LingyeColors.Line),
            shadowElevation = 8.dp
        ) {
            Column(Modifier.padding(8.dp), content = content)
        }
    }
}

/** Shared row for bottom-toolbar secondary menus. */
@Composable
fun CanvasMenuRow(
    label: String,
    icon: (@Composable (Color) -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
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
        Modifier.fillMaxWidth().height(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) LingyeColors.Subtle else Color.Transparent)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            icon(if (enabled) LingyeColors.Muted else color)
            Spacer(Modifier.width(8.dp))
        }
        Text(label, Modifier.weight(1f), fontSize = 14.sp, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        trailing?.invoke()
    }
}

@Composable
fun CanvasMenuSection(label: String) {
    Text(
        label,
        Modifier.height(22.dp).padding(horizontal = 8.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = LingyeColors.Muted
    )
}

@Composable
fun CanvasMenuDivider() {
    Box(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(LingyeColors.Line))
    }
}

/**
 * Pure resolver, all values in dp. Kept separate from Compose so it can be unit-tested.
 */
fun resolveToolbarPlacement(
    anchorX: Float,
    anchorTop: Float,
    anchorBottom: Float,
    toolbarWidth: Float,
    toolbarHeight: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    imeBottom: Float = 0f,
    margin: Float = 12f,
    gap: Float = 8f,
    bottomReserve: Float = 92f,
    alignCenter: Boolean = false
): ToolbarPlacement {
    val safeBottom = min(
        viewportHeight - bottomReserve,
        if (imeBottom > 0f) viewportHeight - imeBottom - margin else viewportHeight
    ) - toolbarHeight
    val maxX = max(margin, viewportWidth - toolbarWidth - margin)
    val rawX = if (alignCenter) anchorX - toolbarWidth / 2f else anchorX
    val x = rawX.coerceIn(margin, maxX)
    val above = anchorTop - gap - toolbarHeight
    val below = anchorBottom + gap
    val y = if (above >= margin) {
        min(above, max(margin, safeBottom))
    } else {
        min(below, max(margin, safeBottom)).coerceAtLeast(margin)
    }
    return ToolbarPlacement(x = x, y = y, below = above < margin)
}

/**
 * Single host for every object context toolbar. It measures its content, then places a
 * uniform surface above the object (or below it when there is no room above), clamped to
 * the canvas bounds and the visible area above the IME.
 */
@Composable
fun ContextualToolbarHost(
    anchor: CanvasToolbarAnchor,
    viewportWidth: Dp,
    viewportHeight: Dp,
    imeBottom: Dp = 0.dp,
    objectKind: CanvasObjectKind? = null,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
    shape: Shape = RoundedCornerShape(ContextualToolbarTokens.radius),
    shadowElevation: Dp = ContextualToolbarTokens.shadow,
    content: @Composable () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val placement = if (size.width > 0 && viewportWidth.value > 0f && viewportHeight.value > 0f) {
        val widthDp = size.width / density.density
        val heightDp = size.height / density.density
        resolveToolbarPlacement(
            anchorX = anchor.x.value,
            anchorTop = anchor.y.value,
            anchorBottom = anchor.bottom.value,
            toolbarWidth = widthDp,
            toolbarHeight = heightDp,
            viewportWidth = viewportWidth.value,
            viewportHeight = viewportHeight.value,
            imeBottom = imeBottom.value,
            margin = ContextualToolbarTokens.margin.value,
            gap = ContextualToolbarTokens.gap.value,
            bottomReserve = ContextualToolbarTokens.bottomReserve.value,
            alignCenter = anchor.align == ToolbarAlign.Center
        )
    } else null
    Box(
        modifier
            .offset(
                x = placement?.x?.dp ?: anchor.x,
                y = placement?.y?.dp ?: (anchor.y - 54.dp)
            )
            .onSizeChanged { size = it }
            .testTag(objectKind?.let { "object-toolbar-${it.id}" } ?: "object-toolbar")
            .graphicsLayer { alpha = if (size.width > 0) 1f else 0f }
    ) {
        Surface(
            shape = shape,
            color = LingyeColors.SurfaceHigh.copy(alpha = 0.98f),
            border = border,
            shadowElevation = shadowElevation
        ) { content() }
    }
}
