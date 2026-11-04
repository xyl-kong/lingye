package com.example.tabletworkspace
import com.example.tabletworkspace.ui.model.PageRowModel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun PageRowActions(
    model: PageRowModel,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    language: AppLanguage,
    onRename: () -> Unit,
    onInfo: () -> Unit,
    onAddLinkedDoc: () -> Unit,
    onDuplicate: () -> Unit,
    onOpenInNewTab: () -> Unit,
    onFavorite: () -> Unit,
    onRemoveFolder: () -> Unit,
    onTrash: () -> Unit
) {
    Box {
        CompactIconAction(
            AppIcon.MORE,
            tr(language, "页面选项：", "Page options: ") + model.title.ifBlank { tr(language, "未命名页面", "Untitled page") },
            { onExpandedChange(true) },
            LingyeColors.Muted,
            selected = expanded
        )
        LingyeDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.width(232.dp),
            offset = DpOffset(10.dp, 6.dp),
            shape = RoundedCornerShape(12.dp),
            containerColor = LingyeColors.SurfaceHigh,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, LingyeColors.Line)
        ) {
            MenuRow(tr(language, "重命名", "Rename"), onRename, icon = AppIcon.EDIT)
            MenuRow(tr(language, "查看信息", "View info"), onInfo, icon = AppIcon.OVERVIEW)
            HorizontalDivider(Modifier.padding(vertical = 5.dp), color = LingyeColors.Line)
            MenuRow(tr(language, "添加链接文档", "Add linked document"), onAddLinkedDoc, icon = AppIcon.CONNECT, enabled = !model.trashed)
            MenuRow(tr(language, "复制", "Duplicate"), onDuplicate, icon = AppIcon.COPY, enabled = !model.trashed)
            MenuRow(tr(language, "在新标签页打开", "Open in new tab"), onOpenInNewTab, icon = AppIcon.EXPAND_FULL, enabled = !model.trashed)
            HorizontalDivider(Modifier.padding(vertical = 5.dp), color = LingyeColors.Line)
            MenuRow(
                if (model.favorite) tr(language, "取消收藏", "Remove from saved") else tr(language, "加入收藏", "Add to saved"),
                onFavorite,
                icon = if (model.favorite) AppIcon.FAVORITED else AppIcon.FAVORITE
            )
            if (model.folders.isNotEmpty()) {
                MenuRow(tr(language, "从文件夹移出", "Remove from folder"), onRemoveFolder, icon = AppIcon.FOLDER)
            }
            HorizontalDivider(Modifier.padding(vertical = 5.dp), color = LingyeColors.Line)
            MenuRow(tr(language, "移动到回收站", "Move to trash"), onTrash, icon = AppIcon.TRASH, danger = true)
        }
    }
}

@Composable
internal fun PageTabStrip(
    tabs: List<PageRowModel>,
    activeId: String,
    language: AppLanguage,
    onSelect: (PageRowModel) -> Unit,
    onClose: (PageRowModel) -> Unit
) {
    Column(Modifier.fillMaxWidth().background(LingyeColors.Surface).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().height(42.dp).horizontalScroll(rememberScrollState()).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val active = tab.id == activeId
                Surface(
                    onClick = { onSelect(tab) },
                    modifier = Modifier.padding(horizontal = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (active) LingyeColors.Subtle else Color.Transparent
                ) {
                    Row(
                        Modifier.height(32.dp).padding(start = 10.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            tab.title.ifBlank { tr(language, "未命名页面", "Untitled page") },
                            modifier = Modifier.widthIn(max = 150.dp),
                            fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                            color = if (active) LingyeColors.Ink else LingyeColors.Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            Modifier.size(22.dp).clickable { onClose(tab) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("×", fontSize = 14.sp, color = LingyeColors.Muted)
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = LingyeColors.Line.copy(alpha = 0.7f))
    }
}
