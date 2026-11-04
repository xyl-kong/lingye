package com.example.tabletworkspace
import com.example.tabletworkspace.data.metadata.PageMetadataSnapshot
import com.example.tabletworkspace.data.metadata.PageMetadataSnapshotCodec
import com.example.tabletworkspace.data.metadata.remapToImported

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PageMetadataSnapshotTest {
    @Test fun roundTripKeepsPropertiesJournalAndTemplate() {
        val page = Page.create("metadata")
        val properties = DocPropertyCatalog.defaults(AppLanguage.ZH)
            .map { if (it.type == "text") it.copy(value = "42") else it }
        val snapshot = PageMetadataSnapshot(
            docProperties = mapOf(page.id to DocPropertyStore.encode(properties)),
            journalDates = mapOf(page.id to "2026-11-03"),
            templatePageIds = setOf(page.id)
        )
        val decoded = PageMetadataSnapshotCodec.decode(PageMetadataSnapshotCodec.encode(snapshot))
        assertEquals(snapshot.journalDates, decoded.journalDates)
        assertEquals(snapshot.templatePageIds, decoded.templatePageIds)
        assertEquals(
            DocPropertyStore.decode(snapshot.docProperties.getValue(page.id)).size,
            DocPropertyStore.decode(decoded.docProperties.getValue(page.id)).size
        )
    }

    @Test fun backupCarriesMetadataAndOldBackupHasNone() {
        val page = Page.create("metadata")
        val snapshot = PageMetadataSnapshot(journalDates = mapOf(page.id to "2026-11-03"))
        val withMetadata = BackupArchive.decode(BackupArchive.encode(listOf(page), emptyMap(), metadata = snapshot))
        assertEquals(snapshot.journalDates, withMetadata.metadata?.journalDates)
        val withoutMetadata = BackupArchive.decode(BackupArchive.encode(listOf(page), emptyMap()))
        assertNull(withoutMetadata.metadata)
    }

    @Test fun restoredMetadataRemapsOntoImportedPageIds() {
        val original = listOf(Page.create("a"), Page.create("b"))
        val imported = listOf(Page.create("new-a"), Page.create("new-b"))
        val snapshot = PageMetadataSnapshot(
            docProperties = mapOf(original[0].id to "props"),
            journalDates = mapOf(original[1].id to "2026-11-04"),
            templatePageIds = setOf(original[0].id)
        )
        val mapped = snapshot.remapToImported(original, imported)
        assertEquals(mapOf(imported[0].id to "props"), mapped.properties)
        assertEquals(mapOf(imported[1].id to "2026-11-04"), mapped.journal)
        assertEquals(mapOf(imported[0].id to true), mapped.templates)
    }

    @Test fun restoredMetadataDropsEntriesForUnknownPages() {
        val original = listOf(Page.create("a"))
        val imported = listOf(Page.create("new-a"))
        val missing = Page.create("missing")
        val snapshot = PageMetadataSnapshot(
            journalDates = mapOf(missing.id to "2026-11-04"),
            templatePageIds = setOf(missing.id)
        )
        val mapped = snapshot.remapToImported(original, imported)
        assertTrue(mapped.journal.isEmpty())
        assertTrue(mapped.templates.isEmpty())
        assertTrue(mapped.properties.isEmpty())
    }

    @Test fun rejectsInvalidDateAndCorruptMagic() {
        val page = Page.create("metadata")
        try {
            PageMetadataSnapshotCodec.encode(PageMetadataSnapshot(journalDates = mapOf(page.id to "11/03/2026")))
            fail("invalid date accepted")
        } catch (_: IllegalArgumentException) { }
        val bytes = PageMetadataSnapshotCodec.encode(PageMetadataSnapshot(templatePageIds = setOf(page.id)))
        bytes[0] = 0
        try {
            PageMetadataSnapshotCodec.decode(bytes)
            fail("corrupt metadata accepted")
        } catch (_: IllegalArgumentException) { }
    }
}
