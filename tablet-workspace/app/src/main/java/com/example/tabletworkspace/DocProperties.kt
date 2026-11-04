package com.example.tabletworkspace

import android.content.SharedPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.Base64

/** One property definition shown in the document properties sidebar. */
data class DocPropertyInfo(
    val id: String,
    val type: String,
    val name: String,
    val show: String = DocPropertyCatalog.ALWAYS_SHOW,
    val order: Int = 0,
    val icon: String = "",
    val value: String = ""
)

data class DocPropertyType(
    val key: String,
    val nameZh: String,
    val nameEn: String,
    val unique: Boolean,
    val defaultShow: String?,
    val renameable: Boolean = true
)

object DocPropertyCatalog {
    const val ALWAYS_SHOW = "always-show"
    const val HIDE_WHEN_EMPTY = "hide-when-empty"
    const val ALWAYS_HIDE = "always-hide"
    val shows = setOf(ALWAYS_SHOW, HIDE_WHEN_EMPTY, ALWAYS_HIDE)

    const val KIND_TEXT = "text"
    const val KIND_NUMBER = "number"
    const val KIND_CHECKBOX = "checkbox"
    const val KIND_DATE = "date"
    const val KIND_READONLY = "readonly"

    /**
     * Official add-property order (see workspace-property-types/index.ts).
     * `tags`, `createdAt` and `updatedAt` are not renameable in the official edit menu.
     */
    val types = listOf(
        DocPropertyType("tags", "标签", "Tags", unique = true, defaultShow = ALWAYS_SHOW, renameable = false),
        DocPropertyType("text", "文本", "Text", unique = false, defaultShow = null),
        DocPropertyType("number", "数字", "Number", unique = false, defaultShow = null),
        DocPropertyType("checkbox", "复选框", "Checkbox", unique = false, defaultShow = null),
        DocPropertyType("date", "日期", "Date", unique = false, defaultShow = null),
        DocPropertyType("createdBy", "创建者", "Created by", unique = false, defaultShow = ALWAYS_HIDE),
        DocPropertyType("updatedBy", "最后编辑者", "Last edited by", unique = false, defaultShow = null),
        DocPropertyType("updatedAt", "更新时间", "Updated", unique = false, defaultShow = ALWAYS_SHOW, renameable = false),
        DocPropertyType("createdAt", "创建时间", "Created", unique = false, defaultShow = ALWAYS_SHOW, renameable = false),
        DocPropertyType("docPrimaryMode", "文档模式", "Document mode", unique = false, defaultShow = ALWAYS_HIDE),
        DocPropertyType("journal", "Journal", "Journal", unique = false, defaultShow = ALWAYS_HIDE),
        DocPropertyType("edgelessTheme", "无界配色方案", "Edgeless theme", unique = false, defaultShow = ALWAYS_HIDE),
        DocPropertyType("pageWidth", "页面宽度", "Page width", unique = false, defaultShow = ALWAYS_HIDE),
        DocPropertyType("template", "模板", "Template", unique = false, defaultShow = ALWAYS_HIDE)
    )

    fun type(key: String): DocPropertyType? = types.firstOrNull { it.key == key }

    fun name(type: DocPropertyType, language: AppLanguage): String =
        if (language == AppLanguage.ZH) type.nameZh else type.nameEn

    fun showLabel(show: String, language: AppLanguage): String = when (show) {
        HIDE_WHEN_EMPTY -> if (language == AppLanguage.ZH) "空时隐藏" else "Hide when empty"
        ALWAYS_HIDE -> if (language == AppLanguage.ZH) "始终隐藏" else "Always hide"
        else -> if (language == AppLanguage.ZH) "始终显示" else "Always show"
    }

    fun renameable(type: String): Boolean = type(type)?.renameable ?: true

    /** Editable value kind; system-derived properties stay read-only. */
    fun valueKind(type: String): String = when (type) {
        "text" -> KIND_TEXT
        "number" -> KIND_NUMBER
        "checkbox" -> KIND_CHECKBOX
        "date" -> KIND_DATE
        else -> KIND_READONLY
    }

    /** Properties added from the sidebar; built-in defaults keep their builtin- ids. */
    fun isCustom(property: DocPropertyInfo): Boolean = !property.id.startsWith("builtin-")

    /**
     * Moves [draggedId] next to [targetId] (before it, or after it when [after] is true)
     * and renumbers every order field from 0.
     */
    fun reorder(
        properties: List<DocPropertyInfo>,
        draggedId: String,
        targetId: String,
        after: Boolean
    ): List<DocPropertyInfo> {
        if (draggedId == targetId) return properties
        val ordered = properties.sortedBy { it.order }
        val dragged = ordered.firstOrNull { it.id == draggedId } ?: return properties
        val others = ordered.filterNot { it.id == draggedId }
        val targetIndex = others.indexOfFirst { it.id == targetId }
        if (targetIndex < 0) return properties
        val insert = (targetIndex + if (after) 1 else 0).coerceIn(0, others.size)
        val moved = others.toMutableList().apply { add(insert, dragged) }
        return moved.mapIndexed { index, property -> property.copy(order = index) }
    }

    /** The nine default properties from the official info sidebar. */
    fun defaults(language: AppLanguage): List<DocPropertyInfo> {
        val order = listOf(
            "tags", "docPrimaryMode", "journal", "template",
            "createdAt", "updatedAt", "createdBy", "edgelessTheme", "pageWidth"
        )
        return order.mapIndexed { index, key ->
            val definition = type(key) ?: error("missing property type $key")
            DocPropertyInfo(
                id = "builtin-$key",
                type = key,
                name = name(definition, language),
                show = definition.defaultShow ?: ALWAYS_SHOW,
                order = index
            )
        }
    }
}

/**
 * Serialized in SharedPreferences per page; page format 25 is unchanged.
 * New records start with a magic int so older 5-field records still decode.
 */
object DocPropertyStore {
    private const val PREFIX = "doc_properties_"
    private const val MAX_PROPERTIES = 100
    private const val MAX_NAME = 100
    private const val MAX_ICON = 40
    private const val MAX_VALUE = 4000
    private const val FORMAT_MAGIC = 0x4C505032

    fun load(preferences: SharedPreferences, pageId: String): List<DocPropertyInfo> {
        val raw = preferences.getString(PREFIX + pageId, null) ?: return DocPropertyCatalog.defaults(AppLanguage.ZH)
        val decoded = runCatching { decode(raw) }.getOrNull() ?: return DocPropertyCatalog.defaults(AppLanguage.ZH)
        return decoded.ifEmpty { DocPropertyCatalog.defaults(AppLanguage.ZH) }
    }

    fun save(preferences: SharedPreferences, pageId: String, properties: List<DocPropertyInfo>) {
        preferences.edit().putString(PREFIX + pageId, encode(properties)).apply()
    }

    /** Stored payload only when the page has explicitly saved properties. */
    fun raw(preferences: SharedPreferences, pageId: String): String? =
        preferences.getString(PREFIX + pageId, null)

    fun putAll(editor: SharedPreferences.Editor, byPage: Map<String, String>) {
        byPage.forEach { (pageId, raw) -> editor.putString(PREFIX + pageId, raw) }
    }

    fun encode(properties: List<DocPropertyInfo>): String {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out ->
            out.writeInt(FORMAT_MAGIC)
            out.writeInt(properties.size.coerceAtMost(MAX_PROPERTIES))
            properties.take(MAX_PROPERTIES).forEach { property ->
                out.writeUTF(property.id.take(80))
                out.writeUTF(property.type)
                out.writeUTF(property.name.take(MAX_NAME))
                out.writeUTF(property.show)
                out.writeUTF(property.icon.take(MAX_ICON))
                out.writeUTF(property.value.take(MAX_VALUE))
                out.writeInt(property.order)
            }
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.toByteArray())
    }

    fun decode(raw: String): List<DocPropertyInfo> {
        val bytes = Base64.getUrlDecoder().decode(raw)
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            val first = input.readInt()
            val versioned = first == FORMAT_MAGIC
            val count = if (versioned) input.readInt() else first
            require(count in 0..MAX_PROPERTIES) { "invalid property count" }
            val result = mutableListOf<DocPropertyInfo>()
            repeat(count) {
                val id = input.readUTF()
                val type = input.readUTF()
                val name = input.readUTF()
                val show = input.readUTF()
                val icon = if (versioned) input.readUTF() else ""
                val value = if (versioned) input.readUTF() else ""
                val order = input.readInt()
                require(id.isNotEmpty() && DocPropertyCatalog.type(type) != null) { "invalid property" }
                require(name.isNotBlank() && name.length <= MAX_NAME) { "invalid property name" }
                require(icon.length <= MAX_ICON && value.length <= MAX_VALUE) { "invalid property value" }
                result.add(
                    DocPropertyInfo(
                        id = id,
                        type = type,
                        name = name,
                        show = show.takeIf { it in DocPropertyCatalog.shows } ?: DocPropertyCatalog.ALWAYS_SHOW,
                        order = order,
                        icon = icon,
                        value = value
                    )
                )
            }
            return result
        }
    }

    /** Appends " 2", " 3"... until the name is unique among [existing]. */
    fun uniqueName(base: String, existing: List<DocPropertyInfo>): String {
        val names = existing.map { it.name }.toSet()
        if (base !in names) return base
        var index = 2
        while ("$base $index" in names) index++
        return "$base $index"
    }
}
