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
        val wide = Page.create("wide")
        val narrow = Page.create("narrow")
        val properties = DocPropertyCatalog.defaults(AppLanguage.ZH)
            .map { if (it.type == "text") it.copy(value = "42") else it }
        val snapshot = PageMetadataSnapshot(
            docProperties = mapOf(page.id to DocPropertyStore.encode(properties)),
            journalDates = mapOf(page.id to "2026-11-03"),
            templatePageIds = setOf(page.id),
            pageWidths = mapOf(wide.id to true, narrow.id to false)
        )
        val decoded = PageMetadataSnapshotCodec.decode(PageMetadataSnapshotCodec.encode(snapshot))
        assertEquals(snapshot.journalDates, decoded.journalDates)
        assertEquals(snapshot.templatePageIds, decoded.templatePageIds)
        assertEquals(snapshot.pageWidths, decoded.pageWidths)
        assertEquals(
            DocPropertyStore.decode(snapshot.docProperties.getValue(page.id)).size,
            DocPropertyStore.decode(decoded.docProperties.getValue(page.id)).size
        )
    }

    @Test fun legacyV1MetadataDecodesWithoutWidthOverrides() {
        val page = Page.create("metadata")
        val legacy = PageMetadataSnapshot(
            journalDates = mapOf(page.id to "2026-11-03"),
            templatePageIds = setOf(page.id),
            pageWidths = mapOf(page.id to true)
        )
        val decoded = PageMetadataSnapshotCodec.decode(PageMetadataSnapshotCodec.encodeLegacyV1(legacy))
        assertEquals(legacy.journalDates, decoded.journalDates)
        assertEquals(legacy.templatePageIds, decoded.templatePageIds)
        assertTrue(decoded.pageWidths.isEmpty())
    }

    @Test fun missingWidthMeansFollowWorkspaceDefault() {
        val page = Page.create("metadata")
        val decoded = PageMetadataSnapshotCodec.decode(
            PageMetadataSnapshotCodec.encode(PageMetadataSnapshot(journalDates = mapOf(page.id to "2026-11-03")))
        )
        assertTrue(decoded.pageWidths.isEmpty())
    }

    @Test fun rejectsInvalidWidthMarker() {
        val page = Page.create("metadata")
        val bytes = PageMetadataSnapshotCodec.encode(PageMetadataSnapshot(pageWidths = mapOf(page.id to true)))
        bytes[bytes.lastIndex] = 9
        try {
            PageMetadataSnapshotCodec.decode(bytes)
            fail("invalid width accepted")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun backupCarriesMetadataAndOldBackupHasNone() {
        val page = Page.create("metadata")
        val snapshot = PageMetadataSnapshot(
            journalDates = mapOf(page.id to "2026-11-03"),
            pageWidths = mapOf(page.id to true)
        )
        val withMetadata = BackupArchive.decode(BackupArchive.encode(listOf(page), emptyMap(), metadata = snapshot))
        assertEquals(snapshot.journalDates, withMetadata.metadata?.journalDates)
        assertEquals(snapshot.pageWidths, withMetadata.metadata?.pageWidths)
        val withoutMetadata = BackupArchive.decode(BackupArchive.encode(listOf(page), emptyMap()))
        assertNull(withoutMetadata.metadata)
    }

    @Test fun restoredMetadataRemapsOntoImportedPageIds() {
        val original = listOf(Page.create("a"), Page.create("b"))
        val imported = listOf(Page.create("new-a"), Page.create("new-b"))
        val snapshot = PageMetadataSnapshot(
            docProperties = mapOf(original[0].id to "props"),
            journalDates = mapOf(original[1].id to "2026-11-04"),
            templatePageIds = setOf(original[0].id),
            pageWidths = mapOf(original[1].id to true, original[0].id to false)
        )
        val mapped = snapshot.remapToImported(original, imported)
        assertEquals(mapOf(imported[0].id to "props"), mapped.properties)
        assertEquals(mapOf(imported[1].id to "2026-11-04"), mapped.journal)
        assertEquals(mapOf(imported[0].id to true), mapped.templates)
        assertEquals(mapOf(imported[1].id to true, imported[0].id to false), mapped.widths)
    }

    @Test fun restoredMetadataDropsEntriesForUnknownPages() {
        val original = listOf(Page.create("a"))
        val imported = listOf(Page.create("new-a"))
        val missing = Page.create("missing")
        val snapshot = PageMetadataSnapshot(
            journalDates = mapOf(missing.id to "2026-11-04"),
            templatePageIds = setOf(missing.id),
            pageWidths = mapOf(missing.id to true)
        )
        val mapped = snapshot.remapToImported(original, imported)
        assertTrue(mapped.journal.isEmpty())
        assertTrue(mapped.templates.isEmpty())
        assertTrue(mapped.properties.isEmpty())
        assertTrue(mapped.widths.isEmpty())
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
