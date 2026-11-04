package com.example.tabletworkspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Frame 演示模式的底部控制条：上一页/下一页、适应/填充切换和退出。 */
@Composable
internal fun CanvasPresentationBar(
    language: AppLanguage,
    currentIndex: Int,
    total: Int,
    title: String,
    fitMode: String,
    onShift: (Int) -> Unit,
    onToggleFit: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier.padding(bottom = 24.dp),
        shape = RoundedCornerShape(18.dp),
        color = LingyeColors.SurfaceHigh.copy(alpha = 0.97f),
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, LingyeColors.Line.copy(alpha = 0.9f))
    ) {
        Row(Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onShift(-1) }) { Text("‹", fontSize = 22.sp) }
            Text("${currentIndex + 1} / $total", color = LingyeColors.Muted, fontSize = 13.sp)
            Spacer(Modifier.width(10.dp))
            Text(
                title.ifBlank { tr(language, "框架", "Frame") },
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 220.dp)
            )
            Spacer(Modifier.width(10.dp))
            TextButton(onClick = onToggleFit) {
                Text(
                    tr(language, if (fitMode == "fill") "适应" else "填充", if (fitMode == "fill") "Fit" else "Fill"),
                    color = LingyeColors.Primary
                )
            }
            TextButton(onClick = { onShift(1) }) { Text("›", fontSize = 22.sp) }
            CompactIconAction(AppIcon.CLOSE, tr(language, "退出演示", "Exit presentation"), onExit, LingyeColors.Muted)
        }
    }
}
