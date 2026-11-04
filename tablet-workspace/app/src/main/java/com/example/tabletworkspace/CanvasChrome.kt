package com.example.tabletworkspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
internal fun CanvasToolbarDivider() {
    Box(Modifier.padding(horizontal = 3.dp).width(1.dp).height(22.dp).background(LingyeColors.Line.copy(alpha = 0.85f)))
}

@Composable
internal fun CanvasParamSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(CanvasUiTokens.parameterSurfaceRadius),
        color = LingyeColors.SurfaceHigh.copy(alpha = 0.98f),
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, LingyeColors.Line.copy(alpha = 0.9f))
    ) { content() }
}

@Composable
internal fun CanvasParamWidthDot(width: Float, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(CanvasUiTokens.parameterWidthDot).clip(CircleShape)
            .background(if (selected) LingyeColors.Subtle else Color.Transparent)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "${width.toInt()} px" },
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size((4f + width * 0.7f).coerceAtMost(18f).dp).background(if (selected) LingyeColors.Ink else LingyeColors.DotInactive, CircleShape))
    }
}

@Composable
internal fun CanvasParameterChip(
    label: String,
    selected: Boolean,
    icon: AppIcon? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) LingyeColors.Subtle else Color.Transparent
    ) {
        Row(
            Modifier.heightIn(min = CanvasUiTokens.parameterChipHeight).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            icon?.let { WorkspaceIcon(it, null, Modifier.size(16.dp), if (selected) LingyeColors.Primary else LingyeColors.Muted.copy(alpha = if (enabled) 1f else 0.42f)) }
            Text(
                label,
                color = if (selected) LingyeColors.Primary else LingyeColors.Muted.copy(alpha = if (enabled) 1f else 0.42f),
                fontSize = 11.5.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
internal fun CanvasParameterColor(color: Int, selected: Boolean, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(CanvasUiTokens.parameterWidthDot).clip(CircleShape).clickable(onClick = onClick).semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(16.dp).background(Color(color), CircleShape).then(
                if (selected) Modifier.border(2.dp, LingyeColors.Primary, CircleShape) else Modifier.border(1.dp, LingyeColors.Line, CircleShape)
            )
        )
    }
}

@Composable
internal fun CanvasConnectorColors(selected: Int, language: AppLanguage, onSelect: (Int) -> Unit) {
    ColorPaletteRow(selected = selected, onPick = onSelect)
}

@Composable
internal fun CanvasShapeColors(selected: Int, language: AppLanguage, onSelect: (Int) -> Unit) {
    ColorPaletteRow(selected = selected, onPick = onSelect)
}

@Composable
internal fun CanvasZoomControls(
    language: AppLanguage,
    scale: Float,
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        CompactIconAction(AppIcon.MINUS, tr(language, "缩小", "Zoom out"), onZoomOut, LingyeColors.Muted)
        Box(
            Modifier
                .defaultMinSize(minWidth = 56.dp, minHeight = 36.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onReset)
                .semantics { contentDescription = tr(language, "重置缩放为 100%", "Reset zoom to 100%") },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${(scale * 100).roundToInt()}%",
                fontSize = 12.sp,
                color = LingyeColors.Ink,
                fontFamily = FontFamily.Monospace
            )
        }
        CompactIconAction(AppIcon.ADD, tr(language, "放大", "Zoom in"), onZoomIn, LingyeColors.Muted)
    }
}
