package com.example.tabletworkspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** Small emoji-style page icons stored inside the page file (format 23+). */
object PageIcons {
    const val MAX_LENGTH = 16

    val choices = listOf(
        "📄", "📝", "📌", "📚", "💡", "🎯", "✅", "⭐",
        "🔥", "🧠", "🗂️", "📊", "🧪", "🎨", "🖼️", "🎵",
        "🎬", "💻", "🛠️", "🌱", "☕", "🚀", "🧩", "🗺️"
    )

    fun isValid(value: String): Boolean =
        value.isEmpty() || (value.length <= MAX_LENGTH && value.none { it.code < 32 || it.code == 127 })
}

@Composable
fun PageGlyph(page: Page, size: Dp, tint: androidx.compose.ui.graphics.Color = LingyeColors.Muted) {
    PageGlyph(page.icon, PageLinks.opensAsWhiteboard(page, false), size, tint)
}

@Composable
fun PageGlyph(icon: String, canvasIcon: Boolean, size: Dp, tint: androidx.compose.ui.graphics.Color = LingyeColors.Muted) {
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        if (icon.isNotBlank()) {
            Text(icon, fontSize = (size.value * 0.72f).sp, textAlign = TextAlign.Center)
        } else {
            WorkspaceIcon(
                if (canvasIcon) AppIcon.CANVAS else AppIcon.DOCUMENT,
                null,
                Modifier.size(size * 0.88f),
                tint
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PageIconPicker(page: Page, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            Modifier.widthIn(max = 430.dp).fillMaxWidth(0.86f),
            shape = RoundedCornerShape(22.dp),
            color = LingyeColors.SurfaceHigh,
            shadowElevation = 20.dp
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("页面图标", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = LingyeColors.Ink)
                Text("选择一个表情作为页面图标；图标会随页面数据保存和备份。", fontSize = 12.sp, color = LingyeColors.Muted)
                Spacer(Modifier.height(14.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PageIcons.choices.forEach { icon ->
                        val selected = page.icon == icon
                        Surface(
                            onClick = { onSelect(icon) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) LingyeColors.PrimarySoft else LingyeColors.Chip,
                            border = if (selected) BorderStroke(1.dp, LingyeColors.Primary) else null,
                            modifier = Modifier.semantics { contentDescription = "页面图标 $icon" }
                        ) {
                            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                Text(icon, fontSize = 22.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onSelect("") }) {
                        Text("移除图标", color = LingyeColors.Muted)
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("取消") }
                }
            }
        }
    }
}
