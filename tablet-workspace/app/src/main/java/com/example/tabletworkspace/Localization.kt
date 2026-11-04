package com.example.tabletworkspace

enum class AppLanguage(val code: String) {
    ZH("zh"),
    EN("en");

    fun toggle(): AppLanguage = if (this == ZH) EN else ZH

    companion object {
        fun fromCode(value: String?): AppLanguage =
            if (value?.trim()?.lowercase() == EN.code) EN else ZH
    }
}

fun tr(language: AppLanguage, chinese: String, english: String): String =
    if (language == AppLanguage.EN) english else chinese

fun statusLabel(language: AppLanguage, status: String): String = when (status) {
    "待办" -> tr(language, "待办", "To do")
    "进行中" -> tr(language, "进行中", "In progress")
    "完成" -> tr(language, "完成", "Done")
    "未设置" -> tr(language, "未设置", "Unset")
    else -> status
}

fun statusValue(language: AppLanguage, input: String): String {
    if (language != AppLanguage.EN) return input
    return when (input.trim()) {
        "To do" -> "待办"
        "In progress" -> "进行中"
        "Done" -> "完成"
        "Unset" -> ""
        else -> input
    }
}

fun localizedError(language: AppLanguage, error: Throwable): String {
    val message = error.message ?: return tr(language, "未知错误", "Unknown error")
    if (language == AppLanguage.ZH) return message
    return when (message) {
        "图片超过 20 MB" -> "Image exceeds 20 MB"
        "无法读取图片" -> "Unable to read image"
        "图片无法解码" -> "Unable to decode image"
        "文件不是有效图片" -> "The selected file is not a valid image"
        "图片不存在或过大" -> "Image is missing or too large"
        "图片总量超过备份限制" -> "Images exceed the backup limit"
        "备份中的图片无效" -> "The backup contains an invalid image"
        "备份中的文件夹名称无效" -> "The backup contains an invalid folder name"
        "无法写入备份" -> "Unable to write the backup"
        "备份文件过大" -> "Backup file is too large"
        "无法读取备份" -> "Unable to read the backup"
        "文本文件超过 20 MB" -> "Text file exceeds 20 MB"
        "无法写入文本文件" -> "Unable to write the text file"
        "无法读取文本文件" -> "Unable to read the text file"
        "没有可备份的页面" -> "There are no pages to back up"
        "备份内容过大" -> "Backup contents are too large"
        "备份包含无效路径" -> "Backup contains an invalid path"
        "备份条目无效" -> "Backup contains an invalid entry"
        "备份格式无效" -> "Backup format is invalid"
        "备份没有页面" -> "Backup contains no pages"
        "备份缺少图片" -> "Backup is missing required images"
        "不支持的文件版本" -> "This file version is not supported"
        "文本长度无效" -> "Text length is invalid"
        "数据数量无效" -> "Item count is invalid"
        "连接线端点或路径样式无效" -> "Connector endpoint or path style is invalid"
        else -> message
    }
}
