package com.example.tabletworkspace
import com.example.tabletworkspace.core.formatShortMonthDay
import com.example.tabletworkspace.core.journalDateLabel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
private fun LibrarySidebarToggle(show: Boolean, onToggle: () -> Unit) {
    if (!show) return
    Box(
        Modifier.size(38.dp).clip(RoundedCornerShape(9.dp)).clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        WorkspaceIcon(AppIcon.SIDEBAR, null, Modifier.size(20.dp), LingyeColors.Muted)
    }
}

@Composable
fun JournalsHome(
    pages: List<Page>,
    journalDates: Map<String, String?>,
    language: AppLanguage,
    selectedDate: String,
    onSelectDate: (String) -> Unit,
    onOpenPage: (Page) -> Unit,
    onCreateForDate: (String) -> Unit,
    showSidebarToggle: Boolean,
    onToggleSidebar: () -> Unit
) {
    val selected = remember(selectedDate) { runCatching { LocalDate.parse(selectedDate) }.getOrDefault(LocalDate.now()) }
    val weekStart = selected.minusDays((selected.dayOfWeek.value % 7).toLong())
    val days = (0..6).map { weekStart.plusDays(it.toLong()) }
    val today = LocalDate.now()
    val dayLabels = if (language == AppLanguage.EN) listOf("SUN","MON","TUE","WED","THU","FRI","SAT") else listOf("日","一","二","三","四","五","六")
    val entries = pages.filter { !it.trashed && journalDates[it.id] == selected.toString() }
    Column(Modifier.fillMaxSize().background(LingyeColors.Background).padding(horizontal = 28.dp, vertical = 20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LibrarySidebarToggle(showSidebarToggle, onToggleSidebar)
            Text(tr(language, "Journals", "Journals"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LingyeColors.Ink)
            Spacer(Modifier.width(12.dp))
            TextButton(onClick = { onSelectDate(today.toString()) }) {
                Text(tr(language, "今天", "Today"), color = LingyeColors.Primary, fontSize = 13.sp)
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { onSelectDate(selected.minusDays(7).toString()) }) { Text("‹", fontSize = 20.sp, color = LingyeColors.Muted) }
            TextButton(onClick = { onSelectDate(selected.plusDays(7).toString()) }) { Text("›", fontSize = 20.sp, color = LingyeColors.Muted) }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            days.forEachIndexed { index, day ->
                val isSelected = day == selected
                val isToday = day == today
                Surface(
                    onClick = { onSelectDate(day.toString()) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) LingyeColors.Primary else LingyeColors.SurfaceHigh,
                    border = if (!isSelected && isToday) BorderStroke(1.dp, LingyeColors.Primary) else BorderStroke(1.dp, LingyeColors.Line.copy(alpha = 0.7f))
                ) {
                    Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            dayLabels[index],
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else LingyeColors.Muted
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            day.dayOfMonth.toString(),
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else LingyeColors.Ink
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(journalDateLabel(selected.toString(), language), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = LingyeColors.Ink)
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = LingyeColors.Line)
        Spacer(Modifier.height(12.dp))
        if (entries.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    tr(language, "这一天还没有日志", "No journal for this day"),
                    fontSize = 14.sp,
                    color = LingyeColors.Muted
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { onCreateForDate(selected.toString()) }) {
                    Text(tr(language, "创建这一天的日志", "Create journal for this day"), color = LingyeColors.Primary)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries, key = { it.id }) { page ->
                    Surface(
                        onClick = { onOpenPage(page) },
                        shape = RoundedCornerShape(12.dp),
                        color = LingyeColors.SurfaceHigh,
                        border = BorderStroke(1.dp, LingyeColors.Line.copy(alpha = 0.7f))
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            WorkspaceIcon(AppIcon.RECORD, null, Modifier.size(20.dp), LingyeColors.Primary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    page.title.ifBlank { tr(language, "未命名页面", "Untitled page") },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LingyeColors.Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val preview = page.text.trim().replace('\n', ' ')
                                if (preview.isNotEmpty()) {
                                    Spacer(Modifier.height(3.dp))
                                    Text(preview, fontSize = 12.sp, color = LingyeColors.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Text(tr(language, "打开", "Open"), fontSize = 12.sp, color = LingyeColors.Primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemplatesHome(
    pages: List<Page>,
    templatePageIds: Set<String>,
    journalDates: Map<String, String?>,
    modifiedTimes: Map<String, Long>,
    language: AppLanguage,
    onOpenPage: (Page) -> Unit,
    onCreateTemplate: () -> Unit,
    onUseTemplate: (Page) -> Unit,
    showSidebarToggle: Boolean,
    onToggleSidebar: () -> Unit
) {
    val templates = pages.filter { it.id in templatePageIds && !it.trashed }
    Column(Modifier.fillMaxSize().background(LingyeColors.Background).padding(horizontal = 28.dp, vertical = 20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LibrarySidebarToggle(showSidebarToggle, onToggleSidebar)
            Text(tr(language, "模板", "Templates"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LingyeColors.Ink)
            Spacer(Modifier.width(12.dp))
            Text(tr(language, "页面模板", "Page templates"), fontSize = 12.sp, color = LingyeColors.Muted)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onCreateTemplate) {
                Text(tr(language, "新建模板", "New template"), color = LingyeColors.Primary, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = LingyeColors.Line)
        Spacer(Modifier.height(12.dp))
        if (templates.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(tr(language, "还没有页面模板", "No page templates yet"), fontSize = 14.sp, color = LingyeColors.Muted)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onCreateTemplate) {
                    Text(tr(language, "创建第一个模板", "Create the first template"), color = LingyeColors.Primary)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(templates, key = { it.id }) { page ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LingyeColors.SurfaceHigh,
                        border = BorderStroke(1.dp, LingyeColors.Line.copy(alpha = 0.7f))
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            WorkspaceIcon(AppIcon.CARD, null, Modifier.size(20.dp), LingyeColors.Primary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    journalDates[page.id]?.let { journalDateLabel(it, language) }
                                        ?: page.title.ifBlank { tr(language, "未命名模板", "Untitled template") },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LingyeColors.Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        tr(language, "模板", "Template"),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LingyeColors.Primary,
                                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(LingyeColors.SurfaceSoft).padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                    val modified = formatShortMonthDay(modifiedTimes[page.id] ?: 0L)
                                    if (modified.isNotEmpty()) {
                                        Spacer(Modifier.width(8.dp))
                                        Text(modified, fontSize = 11.sp, color = LingyeColors.Muted)
                                    }
                                }
                            }
                            TextButton(onClick = { onOpenPage(page) }) {
                                Text(tr(language, "打开", "Open"), color = LingyeColors.Ink, fontSize = 12.sp)
                            }
                            TextButton(onClick = { onUseTemplate(page) }) {
                                Text(tr(language, "使用模板", "Use template"), color = LingyeColors.Primary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
