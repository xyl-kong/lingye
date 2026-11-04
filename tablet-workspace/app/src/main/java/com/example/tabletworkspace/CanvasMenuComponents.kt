package com.example.tabletworkspace

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Layer ordering labels shared by card, shape, selection and text toolbars. */
internal fun layerMenuEntries(language: AppLanguage): List<Pair<LayerMove, String>> = listOf(
    LayerMove.TO_FRONT to tr(language, "置于顶层", "Bring to front"),
    LayerMove.FORWARD to tr(language, "上移一层", "Bring forward"),
    LayerMove.BACKWARD to tr(language, "下移一层", "Send backward"),
    LayerMove.TO_BACK to tr(language, "置于底层", "Send to back")
)

/** Layer ordering entries shared by card, shape, selection and text toolbars. */
@Composable
internal fun LayerMenuItems(
    language: AppLanguage,
    showIcons: Boolean,
    onMove: (LayerMove) -> Unit
) {
    layerMenuEntries(language).forEachIndexed { index, (move, label) ->
        val icon = if (index < 2) AppIcon.LAYER_UP else AppIcon.LAYER_DOWN
        if (showIcons) {
            DropdownMenuItem(
                text = { Text(label) },
                leadingIcon = { WorkspaceIcon(icon, null, Modifier.size(17.dp), LingyeColors.Muted) },
                onClick = { onMove(move) }
            )
        } else {
            DropdownMenuItem(text = { Text(label) }, onClick = { onMove(move) })
        }
    }
}

/** Small uppercase-style group heading used inside canvas dropdown menus. */
@Composable
internal fun MenuSectionLabel(text: String) {
    Text(
        text,
        Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = LingyeColors.Muted
    )
}

/**
 * Shared dropdown row used by page, block and canvas menus: 18dp leading icon or custom
 * leading content, optional subtitle/trailing hint, disabled/danger/checked states.
 */
@Composable
internal fun MenuRow(
    label: String,
    onClick: () -> Unit,
    icon: AppIcon? = null,
    leading: (@Composable () -> Unit)? = null,
    subtitle: String? = null,
    trailing: String? = null,
    enabled: Boolean = true,
    danger: Boolean = false,
    checked: Boolean = false
) {
    val textColor = when {
        !enabled -> LingyeColors.Muted
        danger -> LingyeColors.Coral
        checked -> LingyeColors.Primary
        else -> LingyeColors.Ink
    }
    val iconTint = when {
        !enabled -> LingyeColors.Muted
        danger -> LingyeColors.Coral
        checked -> LingyeColors.Primary
        else -> LingyeColors.Muted
    }
    DropdownMenuItem(
        text = {
            val title = if (checked) "✓ $label" else label
            if (subtitle == null) {
                Text(title, color = textColor)
            } else {
                Column {
                    Text(title, color = textColor)
                    Text(subtitle, fontSize = 11.sp, color = LingyeColors.Muted)
                }
            }
        },
        leadingIcon = if (icon != null) {
            @Composable { WorkspaceIcon(icon, null, Modifier.size(18.dp), iconTint) }
        } else {
            leading
        },
        trailingIcon = trailing?.let { hint ->
            @Composable { Text(hint, fontSize = 11.sp, color = LingyeColors.Muted) }
        },
        enabled = enabled,
        onClick = onClick
    )
}

/** Menu row with a leading check mark for mutually exclusive style options. */
@Composable
internal fun MenuCheckItem(label: String, checked: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(if (checked) "✓ $label" else label) },
        onClick = onClick
    )
}
