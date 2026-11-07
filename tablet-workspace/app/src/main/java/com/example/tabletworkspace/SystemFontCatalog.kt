package com.example.tabletworkspace

import android.graphics.Typeface
import android.os.Build
import androidx.compose.ui.text.font.FontFamily
import java.io.File

data class SystemFontChoice(
    val name: String,
    val displayName: String,
    val path: String? = null,
    val limited: Boolean = false
)

/**
 * Reads fonts already installed on the device. No font is downloaded or bundled.
 *
 * Android does not expose a public family-name list for every installed font, so
 * the picker is built from font files. File names are used as stable ids and are
 * cleaned up for display; users can also type a family name manually.
 */
object SystemFontCatalog {
    @Volatile
    private var cached: List<SystemFontChoice>? = null
    private val familyCache = mutableMapOf<String, FontFamily?>()

    val limitedEnumeration: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    fun choices(refresh: Boolean = false): List<SystemFontChoice> {
        cached?.takeIf { !refresh }?.let { return it }
        if (refresh) synchronized(familyCache) { familyCache.clear() }
        val files = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            android.graphics.fonts.SystemFonts.getAvailableFonts()
                .mapNotNull { font -> runCatching { font.file }.getOrNull() }
        } else {
            File("/system/fonts").listFiles().orEmpty().asList()
        }
        val result = files
            .filter { it.isFile && it.extension.lowercase() in setOf("ttf", "otf", "ttc") }
            .map { file ->
                SystemFontChoice(
                    name = file.nameWithoutExtension,
                    displayName = displayNameOf(file.nameWithoutExtension),
                    path = file.absolutePath,
                    limited = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                )
            }
            .sortedWith(compareBy({ weightOf(it.path) }, { it.displayName.lowercase() }))
            .distinctBy { it.displayName.lowercase() }
            .sortedBy { it.displayName.lowercase() }
            .take(300)
        cached = result
        return result
    }

    fun isAvailable(name: String): Boolean {
        val target = name.trim()
        if (target.isEmpty()) return false
        if (choices().any { it.name.equals(target, ignoreCase = true) || it.displayName.equals(target, ignoreCase = true) }) {
            return true
        }
        return target.lowercase() in GENERIC_FAMILIES
    }

    fun fontFamily(name: String): FontFamily? {
        val target = name.trim()
        if (target.isEmpty()) return null
        val key = target.lowercase()
        synchronized(familyCache) {
            if (familyCache.containsKey(key)) return familyCache[key]
        }
        val choice = choices().firstOrNull {
            it.name.equals(target, ignoreCase = true) || it.displayName.equals(target, ignoreCase = true)
        }
        val typeface = runCatching {
            choice?.path?.let { Typeface.createFromFile(it) } ?: Typeface.create(target, Typeface.NORMAL)
        }.getOrNull()
        val family = typeface?.let { runCatching { FontFamily(it) }.getOrNull() }
        synchronized(familyCache) { familyCache[key] = family }
        return family
    }

    private fun displayNameOf(fileName: String): String {
        val cleaned = fileName
            .replace(Regex("(?i)[-_](thin|extralight|ultralight|light|regular|normal|medium|semibold|demibold|bold|extrabold|black)$"), "")
            .replace(Regex("(?i)[-_](italic|oblique)$"), "")
            .replace('-', ' ')
            .replace('_', ' ')
            .trim()
        return cleaned.ifBlank { fileName }
    }

    private fun weightOf(path: String?): Int {
        val file = path?.substringAfterLast('/')?.lowercase().orEmpty()
        return when {
            file.contains("thin") -> 100
            file.contains("light") -> 300
            file.contains("regular") || file.contains("normal") -> 400
            file.contains("medium") -> 500
            file.contains("bold") -> 700
            else -> 450
        }
    }

    private val GENERIC_FAMILIES = setOf(
        "sans", "sans-serif", "serif", "mono", "monospace", "serif-monospace",
        "cursive", "casual", "sans-serif-condensed", "sans-serif-smallcaps"
    )
}
