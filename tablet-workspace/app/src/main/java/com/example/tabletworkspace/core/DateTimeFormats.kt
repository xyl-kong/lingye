package com.example.tabletworkspace.core

import com.example.tabletworkspace.AppLanguage
import com.example.tabletworkspace.tr
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/** Centralized date/time formatting so list, info, overview and journal views stay consistent. */
fun formatDocTime(millis: Long, language: AppLanguage, nowMillis: Long = System.currentTimeMillis()): String {
    if (millis <= 0L) return tr(language, "未知", "Unknown")
    val now = Date(nowMillis)
    val value = Date(millis)
    val day = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    if (day.format(now) == day.format(value)) return tr(language, "今天", "Today")
    val pattern = if (language == AppLanguage.ZH) "yyyy年M月d日" else "MMM d, yyyy"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(value)
}

fun formatModified(timestamp: Long?, language: AppLanguage): String {
    if (timestamp == null || timestamp <= 0L) return tr(language, "未记录", "Not recorded")
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

fun formatOverviewDate(timestamp: Long?, language: AppLanguage): String {
    if (timestamp == null || timestamp <= 0L) return tr(language, "未知", "Unknown")
    val pattern = if (language == AppLanguage.ZH) "M月d日" else "MMM d"
    return SimpleDateFormat(pattern, Locale.US).format(Date(timestamp))
}

fun formatShortMonthDay(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val date = Date(timestamp)
    return SimpleDateFormat("M/d", Locale.US).format(date)
}

fun journalDateLabel(iso: String, language: AppLanguage): String {
    val date = runCatching { LocalDate.parse(iso) }.getOrNull() ?: return iso
    return if (language == AppLanguage.EN) {
        date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
    } else {
        "${date.year}年${date.monthValue}月${date.dayOfMonth}日"
    }
}
