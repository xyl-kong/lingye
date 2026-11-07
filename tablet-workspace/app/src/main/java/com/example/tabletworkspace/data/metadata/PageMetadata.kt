package com.example.tabletworkspace.data.metadata

import android.content.Context

/**
 * Per-page flags that are not part of the format-25 page file.
 *
 * Journal dates are ISO yyyy-MM-dd values (one optional date per page);
 * Template is a boolean used to list the page in the template library;
 * Page width is an optional per-page override of the workspace default.
 */
class PageMetadataStore(context: Context) {
    private val prefs = context.getSharedPreferences("page_metadata", Context.MODE_PRIVATE)

    fun journalDate(pageId: String): String? =
        prefs.getString("journal:$pageId", null)?.takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }

    fun setJournalDate(pageId: String, date: String?) {
        prefs.edit().apply {
            if (date == null) remove("journal:$pageId") else putString("journal:$pageId", date)
        }.apply()
    }

    fun isTemplate(pageId: String): Boolean = prefs.getBoolean("template:$pageId", false)

    fun setTemplate(pageId: String, value: Boolean) {
        prefs.edit().putBoolean("template:$pageId", value).apply()
    }

    /** Per-page width override; null means "follow the workspace default". */
    fun pageWidth(pageId: String): Boolean? = when (prefs.getString("width:$pageId", null)) {
        "full" -> true
        "standard" -> false
        else -> null
    }

    fun setPageWidth(pageId: String, fullWidth: Boolean?) {
        prefs.edit().apply {
            when (fullWidth) {
                null -> remove("width:$pageId")
                true -> putString("width:$pageId", "full")
                false -> putString("width:$pageId", "standard")
            }
        }.apply()
    }

    /** Atomically writes journal/template/width values; used when restoring a backup. */
    fun writeBatch(
        journalDates: Map<String, String?>,
        templateValues: Map<String, Boolean>,
        pageWidths: Map<String, Boolean?> = emptyMap()
    ): Boolean {
        val editor = prefs.edit()
        journalDates.forEach { (pageId, date) ->
            if (date == null) editor.remove("journal:$pageId") else editor.putString("journal:$pageId", date)
        }
        templateValues.forEach { (pageId, value) -> editor.putBoolean("template:$pageId", value) }
        pageWidths.forEach { (pageId, fullWidth) ->
            when (fullWidth) {
                null -> editor.remove("width:$pageId")
                true -> editor.putString("width:$pageId", "full")
                false -> editor.putString("width:$pageId", "standard")
            }
        }
        return editor.commit()
    }
}
