package com.example.tabletworkspace

import java.util.Locale

data class SlashCommand(
    val type: String,
    val zhLabel: String,
    val enLabel: String,
    val keywords: Set<String>
)

object SlashCommandCatalog {
    val commands = listOf(
        SlashCommand("paragraph", "段落", "Paragraph", setOf("paragraph", "text", "段落", "正文")),
        SlashCommand("heading", "标题", "Heading", setOf("heading", "title", "h1", "标题")),
        SlashCommand("todo", "待办", "To-do", setOf("todo", "task", "checkbox", "待办", "任务")),
        SlashCommand("bulleted", "项目符号", "Bulleted list", setOf("bullet", "bulleted", "list", "项目符号", "列表")),
        SlashCommand("numbered", "编号列表", "Numbered list", setOf("number", "numbered", "ordered", "编号", "有序")),
        SlashCommand("quote", "引用", "Quote", setOf("quote", "blockquote", "引用")),
        SlashCommand("code", "代码块", "Code", setOf("code", "snippet", "代码", "代码块")),
        SlashCommand("divider", "分隔线", "Divider", setOf("divider", "separator", "line", "分隔线", "分割线")),
        SlashCommand("image", "图片", "Image", setOf("image", "photo", "picture", "图片", "照片")),
        SlashCommand("database", "数据库", "Database", setOf("database", "table", "数据库", "表格")),
        SlashCommand("shared", "共享块", "Shared block", setOf("shared", "sync", "共享", "同步块"))
    )

    fun query(text: String): String? {
        if (!text.startsWith("/") || text.length > 32) return null
        val value = text.drop(1)
        return value.takeIf { suffix -> suffix.none { it.isWhitespace() } }
    }

    fun matches(query: String): List<SlashCommand> {
        val normalized = query.trim().lowercase(Locale.ROOT)
        if (normalized.isEmpty()) return commands
        return commands.filter { command ->
            command.zhLabel.contains(normalized, ignoreCase = true) ||
                command.enLabel.contains(normalized, ignoreCase = true) ||
                command.keywords.any { it.contains(normalized, ignoreCase = true) }
        }
    }
}
