package com.example.tabletworkspace.data.metadata

import android.content.Context

/**
 * Per-page flags that are not part of the format-25 page file.
 *
 * Journal dates are ISO yyyy-MM-dd values (one optional date per page);
 * Template is a boolean used to list the page in the template library.
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

    /** Atomically writes journal/template values; used when restoring a backup. */
    fun writeBatch(journalDates: Map<String, String?>, templateValues: Map<String, Boolean>): Boolean {
        val editor = prefs.edit()
        journalDates.forEach { (pageId, date) ->
            if (date == null) editor.remove("journal:$pageId") else editor.putString("journal:$pageId", date)
        }
        templateValues.forEach { (pageId, value) -> editor.putBoolean("template:$pageId", value) }
        return editor.commit()
    }
}
