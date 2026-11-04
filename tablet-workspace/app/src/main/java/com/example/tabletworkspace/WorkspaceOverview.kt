package com.example.tabletworkspace
import com.example.tabletworkspace.core.formatOverviewDate

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun WorkspaceOverview(
    pages: List<Page>,
    folders: Int,
    templates: Int,
    modifiedTimes: Map<String, Long>,
    storageBytes: Long,
    language: AppLanguage,
    onBack: () -> Unit,
    onOpenPage: (Page) -> Unit
) {
    val insight = remember(pages, folders, templates) { workspaceInsight(pages, folders, templates) }
    val activity = remember(modifiedTimes) { activityBuckets(modifiedTimes.values, System.currentTimeMillis()) }
    fun text(zh: String, en: String) = tr(language, zh, en)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 34.dp, vertical = 28.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OverviewBackButton(text("返回", "Back"), onBack)
            Spacer(Modifier.width(12.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = LingyeColors.PrimarySoft) {
                WorkspaceIcon(AppIcon.OVERVIEW, null, Modifier.padding(12.dp).size(26.dp), LingyeColors.Primary)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(text("工作区概览","Workspace overview"), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = LingyeColors.Ink)
                Text(text("灵页本地空间 · 离线优先","Lingye local space · offline first"), color = LingyeColors.Muted, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(28.dp))
        Surface(shape = RoundedCornerShape(24.dp), color = LingyeColors.Surface, shadowElevation = 5.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                OverviewStat(text("页面","Pages"), insight.pages.toString(), text("未在回收站","Not in trash"))
                OverviewDivider()
                OverviewStat(text("内容字符","Characters"), formatNumber(insight.contentCharacters), text("不包括数据库编码","Database data excluded"))
                OverviewDivider()
                OverviewStat(text("画布对象","Canvas objects"), formatNumber(insight.canvasObjects), text("笔迹、图形、图片和连线","Ink, shapes, images and links"))
                OverviewDivider()
                OverviewStat(text("本地存储","Local storage"), formatBytes(storageBytes), text("页面和引用素材","Pages and referenced media"))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(text("内容构成","Content mix"), fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = LingyeColors.Ink)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniMetric(AppIcon.PEN, text("笔迹","Strokes"), insight.strokes, LingyeColors.Mint, Modifier.weight(1f))
            MiniMetric(AppIcon.CARD, text("卡片","Cards"), insight.cards, LingyeColors.Primary, Modifier.weight(1f))
            MiniMetric(AppIcon.DATABASE, text("数据库","Databases"), insight.databases, Color(0xFFD68A16), Modifier.weight(1f))
            MiniMetric(AppIcon.IMAGE, text("图片","Images"), insight.images, Color(0xFFE05A76), Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        Surface(shape = RoundedCornerShape(24.dp), color = LingyeColors.Surface, shadowElevation = 5.dp) {
            Column(Modifier.fillMaxWidth().padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column { Text(text("编辑活动","Editing activity"), fontSize = 18.sp, fontWeight = FontWeight.SemiBold); Text(text("过去 365 天","Last 365 days"), fontSize = 12.sp, color=LingyeColors.Muted) }
                    Spacer(Modifier.weight(1f))
                    Text(text("${activity.count { it > 0 }} 个活跃日","${activity.count { it > 0 }} active days"), fontSize = 12.sp, color=LingyeColors.Muted)
                }
                Spacer(Modifier.height(18.dp))
                ActivityHeatmap(activity)
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(Modifier.weight(1f), shape=RoundedCornerShape(22.dp), color=LingyeColors.Surface, shadowElevation=4.dp) {
                Column(Modifier.padding(22.dp)) {
                    Text(text("资料状态","Library status"), fontSize=18.sp, fontWeight=FontWeight.SemiBold)
                    Spacer(Modifier.height(14.dp))
                    OverviewLine(AppIcon.FOLDER,text("文件夹","Folders"),insight.folders.toString())
                    OverviewLine(AppIcon.CARD,text("工作区模板","Workspace templates"),insight.templates.toString())
                    OverviewLine(AppIcon.TRASH,text("回收站页面","Pages in trash"),insight.trashedPages.toString())
                    OverviewLine(AppIcon.DOCUMENT,text("文档块","Document blocks"),insight.blocks.toString())
                }
            }
            Surface(Modifier.weight(1.4f), shape=RoundedCornerShape(22.dp), color=LingyeColors.Surface, shadowElevation=4.dp) {
                Column(Modifier.padding(22.dp)) {
                    Text(text("最近页面","Recent pages"), fontSize=18.sp, fontWeight=FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    val recent=pages.filterNot { it.trashed }.sortedByDescending { modifiedTimes[it.id] ?: 0L }.take(5)
                    if(recent.isEmpty()) Text(text("还没有页面","No pages yet"),color=LingyeColors.Muted,modifier=Modifier.padding(vertical=16.dp))
                    recent.forEach { page ->
                        Surface(onClick={onOpenPage(page)},shape=RoundedCornerShape(12.dp),color=Color.Transparent) {
                            Row(Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically) {
                                WorkspaceIcon(AppIcon.DOCUMENT,null,Modifier.size(17.dp),LingyeColors.Primary)
                                Spacer(Modifier.width(10.dp))
                                Text(page.title.ifBlank{text("未命名页面","Untitled page")},Modifier.weight(1f),maxLines=1,overflow=TextOverflow.Ellipsis)
                                Text(formatOverviewDate(modifiedTimes[page.id],language),fontSize=11.sp,color=LingyeColors.Muted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewBackButton(description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(18.dp)) {
            val strokeWidth = 2.dp.toPx()
            drawLine(
                color = LingyeColors.Ink,
                start = Offset(size.width * 0.62f, size.height * 0.2f),
                end = Offset(size.width * 0.36f, size.height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = LingyeColors.Ink,
                start = Offset(size.width * 0.36f, size.height * 0.5f),
                end = Offset(size.width * 0.62f, size.height * 0.8f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun OverviewStat(label: String, value: String, detail: String) {
    Column(Modifier.widthIn(min = 150.dp)) {
        Text(label, color = LingyeColors.Muted, fontSize = 12.sp)
        Text(value, fontSize = 27.sp, fontWeight = FontWeight.Bold, color = LingyeColors.Ink)
        Text(detail, color = LingyeColors.Muted, fontSize = 11.sp)
    }
}

@Composable
private fun OverviewDivider() {
    Box(Modifier.width(1.dp).height(64.dp).background(LingyeColors.Line))
}

@Composable
private fun MiniMetric(icon: AppIcon, label: String, value: Int, tint: Color, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), color = LingyeColors.Surface, shadowElevation = 3.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = tint.copy(alpha = 0.12f)) {
                WorkspaceIcon(icon, null, Modifier.padding(9.dp).size(19.dp), tint)
            }
            Spacer(Modifier.width(10.dp))
            Column { Text(label, fontSize=12.sp,color=LingyeColors.Muted); Text(formatNumber(value),fontSize=21.sp,fontWeight=FontWeight.Bold) }
        }
    }
}

@Composable
private fun ActivityHeatmap(values: List<Int>) {
    val weeks = (values.size + 6) / 7
    val palette = if (LingyeColors.dark) listOf(
        Color(0xFF232329), Color(0xFF2E2A50), Color(0xFF443C7A), Color(0xFF5D51A8), Color(0xFF7C6FE0)
    ) else listOf(Color(0xFFEEF0F5), Color(0xFFDCD8FA), Color(0xFFBEB4F4), Color(0xFF8E7EE8), Color(0xFF6255D9))
    Row(verticalAlignment = Alignment.Bottom) {
        Column(Modifier.padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("一","三","五").forEach { Text(it, color=LingyeColors.Muted, fontSize=9.sp) }
        }
        Canvas(Modifier.fillMaxWidth().height(108.dp)) {
            val gap = 3.dp.toPx()
            val cell = ((size.width - gap * (weeks - 1)) / weeks).coerceAtMost(12.dp.toPx())
            values.forEachIndexed { index, count ->
                val column = index / 7; val row = index % 7
                val color = palette[when { count <= 0 -> 0; count == 1 -> 1; count <= 3 -> 2; count <= 6 -> 3; else -> 4 }]
                drawRoundRect(color, Offset(column * (cell + gap), row * (cell + gap)), Size(cell, cell), CornerRadius(cell * 0.22f))
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth().padding(end=4.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        Text("少", color=LingyeColors.Muted, fontSize=10.sp); Spacer(Modifier.width(5.dp))
        palette.forEach { Box(Modifier.padding(horizontal=2.dp).size(10.dp).background(it,RoundedCornerShape(3.dp))) }
        Spacer(Modifier.width(5.dp)); Text("多", color=LingyeColors.Muted, fontSize=10.sp)
    }
}

@Composable
private fun OverviewLine(icon: AppIcon, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical=8.dp), verticalAlignment=Alignment.CenterVertically) {
        WorkspaceIcon(icon,null,Modifier.size(17.dp),LingyeColors.Muted)
        Spacer(Modifier.width(10.dp))
        Text(label,Modifier.weight(1f),color=LingyeColors.Muted,fontSize=13.sp)
        Text(value,fontWeight=FontWeight.SemiBold,fontSize=13.sp)
    }
}

private fun formatNumber(value: Int): String = String.format(Locale.US, "%,d", value)

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000_000L -> String.format(Locale.US, "%.1f GB", bytes / 1_000_000_000.0)
    bytes >= 1_000_000L -> String.format(Locale.US, "%.1f MB", bytes / 1_000_000.0)
    bytes >= 1_000L -> String.format(Locale.US, "%.1f KB", bytes / 1_000.0)
    else -> "$bytes B"
}

