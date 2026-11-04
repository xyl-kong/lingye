package com.example.tabletworkspace.data.metadata

import android.content.Context
import com.example.tabletworkspace.DocPropertyInfo
import com.example.tabletworkspace.DocPropertyStore
import com.example.tabletworkspace.Page

/**
 * Single entry point for page metadata kept outside the format-25 page file:
 * document properties, Journal dates and template flags.
 */
class PageMetadataRepository(context: Context) {
    private val prefs = context.getSharedPreferences("ink_preferences", Context.MODE_PRIVATE)
    private val store = PageMetadataStore(context)

    fun docProperties(pageId: String): List<DocPropertyInfo> = DocPropertyStore.load(prefs, pageId)

    fun saveDocProperties(pageId: String, properties: List<DocPropertyInfo>) {
        DocPropertyStore.save(prefs, pageId, properties)
    }

    fun journalDate(pageId: String): String? = store.journalDate(pageId)

    fun setJournalDate(pageId: String, date: String?) = store.setJournalDate(pageId, date)

    fun isTemplate(pageId: String): Boolean = store.isTemplate(pageId)

    fun setTemplate(pageId: String, value: Boolean) = store.setTemplate(pageId, value)

    fun journalDates(pages: List<Page>): Map<String, String?> =
        pages.associate { page -> page.id to store.journalDate(page.id) }

    fun templatePageIds(pages: List<Page>): Set<String> =
        pages.filter { store.isTemplate(it.id) }.map { it.id }.toSet()

    fun snapshot(pages: List<Page>): PageMetadataSnapshot = PageMetadataSnapshot(
        docProperties = pages.mapNotNull { page -> DocPropertyStore.raw(prefs, page.id)?.let { page.id to it } }.toMap(),
        journalDates = pages.mapNotNull { page -> store.journalDate(page.id)?.let { page.id to it } }.toMap(),
        templatePageIds = pages.filter { store.isTemplate(it.id) }.map { it.id }.toSet()
    )

    /**
     * Writes a restored backup's metadata onto the newly created page ids. Kept here so
     * PageStore.restore only knows about pages, not about how metadata is stored.
     */
    fun applyRestored(snapshot: PageMetadataSnapshot, original: List<Page>, imported: List<Page>) {
        val mapped = snapshot.remapToImported(original, imported)
        if (mapped.properties.isNotEmpty()) {
            val editor = prefs.edit()
            DocPropertyStore.putAll(editor, mapped.properties)
            check(editor.commit()) { "恢复文档属性失败" }
        }
        if (mapped.journal.isNotEmpty() || mapped.templates.isNotEmpty()) {
            check(store.writeBatch(mapped.journal, mapped.templates)) { "恢复页面标记失败" }
        }
    }
}

/** Metadata keyed by the original page ids, rewritten to the ids created by the restore. */
internal data class RestoredMetadataMap(
    val properties: Map<String, String>,
    val journal: Map<String, String?>,
    val templates: Map<String, Boolean>
)

internal fun PageMetadataSnapshot.remapToImported(original: List<Page>, imported: List<Page>): RestoredMetadataMap {
    val idMap = original.zip(imported).associate { (old, new) -> old.id to new.id }
    return RestoredMetadataMap(
        properties = docProperties.mapNotNull { (oldId, raw) -> idMap[oldId]?.let { it to raw } }.toMap(),
        journal = journalDates.mapNotNull { (oldId, date) -> idMap[oldId]?.let { it to date } }.toMap(),
        templates = templatePageIds.mapNotNull { idMap[it] }.associateWith { true }
    )
}
