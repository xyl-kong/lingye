package com.example.tabletworkspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class ImportFormat(
    val key: String,
    val nameZh: String,
    val nameEn: String,
    val icon: AppIcon,
    val enabled: Boolean,
    val helpZh: String,
    val helpEn: String
) {
    MARKDOWN("markdown", "Markdown 文件 (.md)", "Markdown file (.md)", AppIcon.DOCUMENT, true,
        "导入单个 Markdown 文件为页面。", "Imports one Markdown file as a page."),
    MARKDOWN_ZIP("markdownZip", "Markdown 文件（带媒体文件）(.zip)", "Markdown with media (.zip)", AppIcon.BACKUP, true,
        "导入压缩包内的 Markdown 文件；图片等媒体暂不导入。", "Imports Markdown files inside the zip; media files are not imported yet."),
    HTML("html", "HTML", "HTML", AppIcon.CODE_BLOCK, true,
        "导入 HTML 文件的正文内容。", "Imports the body text of an HTML file."),
    NOTION("notion", "Notion", "Notion", AppIcon.CARD, true,
        "支持 Notion 导出的 HTML 或包含 HTML 的压缩包。", "Supports Notion HTML exports and zips containing HTML."),
    OBSIDIAN("obsidian", "Obsidian 仓库", "Obsidian vault", AppIcon.FOLDER, true,
        "导入 Obsidian 仓库压缩包内的 Markdown 文件；目录结构、附件与 wiki 链接不会保留。", "Imports Markdown files from an Obsidian vault zip; folders, attachments and wiki links are not preserved."),
    BEAR("bear", "Bear（.bear2bk）", "Bear (.bear2bk)", AppIcon.DOCUMENT, true,
        "导入 Bear 备份中的 Markdown 或文本文件；附件与标签不会保留。", "Imports Markdown or text files from a Bear backup; attachments and tags are not preserved."),
    ONENOTE("onenote", "OneNote（实验性）", "OneNote (experimental)", AppIcon.TABLE, false,
        "OneNote 导入暂不支持。", "OneNote import is not supported yet."),
    DOCX("docx", "Microsoft Word (.docx)", "Microsoft Word (.docx)", AppIcon.DOCUMENT, true,
        "提取 Word 文档的段落文字并导入为页面。", "Extracts paragraph text from a Word document."),
    SNAPSHOT("snapshot", "快照", "Snapshot", AppIcon.RECORD, true,
        "导入灵页快照 JSON；也支持包含 text 字段的快照文件。", "Imports a Lingye snapshot JSON, or a snapshot containing text fields.")
}

@Composable
fun ImportDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSelect: (ImportFormat) -> Unit
) {
    fun text(zh: String, en: String) = tr(language, zh, en)
    var help by remember { mutableStateOf<ImportFormat?>(null) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                Modifier.width(560.dp),
                shape = RoundedCornerShape(24.dp),
                color = LingyeColors.SurfaceHigh,
                shadowElevation = 18.dp
            ) {
                Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                    Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text("导入", "Import"), Modifier.weight(1f), fontSize = 21.sp, fontWeight = FontWeight.SemiBold, color = LingyeColors.Ink)
                        CompactIconAction(AppIcon.CLOSE, text("关闭", "Close"), onDismiss, LingyeColors.Muted)
                    }
                    Spacer(Modifier.height(10.dp))
                    Column(
                        Modifier.heightIn(max = 620.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ImportFormat.values().forEach { format ->
                            ImportFormatRow(
                                format = format,
                                language = language,
                                showHelp = format != ImportFormat.MARKDOWN,
                                onSelect = { if (format.enabled) onSelect(format) },
                                onHelp = { help = format }
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text(
                            "如果您希望请求支持其他文件类型，请随时告诉我们 Discord.",
                            "If you would like us to support another file type, let us know on Discord."
                        ),
                        fontSize = 12.sp,
                        color = LingyeColors.Muted
                    )
                }
            }
        }
    }
    help?.let { format ->
        AlertDialog(
            onDismissRequest = { help = null },
            title = { Text(if (language == AppLanguage.ZH) format.nameZh else format.nameEn, fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
            text = { Text(if (language == AppLanguage.ZH) format.helpZh else format.helpEn, fontSize = 14.sp) },
            confirmButton = { TextButton(onClick = { help = null }) { Text(text("关闭", "Close")) } }
        )
    }
}

@Composable
private fun ImportFormatRow(
    format: ImportFormat,
    language: AppLanguage,
    showHelp: Boolean,
    onSelect: () -> Unit,
    onHelp: () -> Unit
) {
    val enabled = format.enabled
    val ink = if (enabled) LingyeColors.Ink else LingyeColors.Muted.copy(alpha = 0.45f)
    Row(
        Modifier.fillMaxWidth().height(56.dp)
            .background(LingyeColors.Surface, RoundedCornerShape(12.dp))
            .border(1.dp, LingyeColors.Line.copy(alpha = if (enabled) 1f else 0.55f), RoundedCornerShape(12.dp))
            .then(if (enabled) Modifier.clickable(onClick = onSelect) else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WorkspaceIcon(format.icon, null, Modifier.size(20.dp), if (enabled) LingyeColors.Ink else LingyeColors.Muted.copy(alpha = 0.45f))
        Spacer(Modifier.width(14.dp))
        Text(
            if (language == AppLanguage.ZH) format.nameZh else format.nameEn,
            Modifier.weight(1f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (showHelp) {
            Box(
                Modifier.size(20.dp).clickable(onClick = onHelp),
                contentAlignment = Alignment.Center
            ) {
                HelpGlyph(
                    diameter = 17.dp,
                    tint = if (enabled) LingyeColors.Muted else LingyeColors.Muted.copy(alpha = 0.45f),
                    outlined = true
                )
            }
        }
    }
}
