package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageMetadataTest {
    @Test
    fun foldersTagsAndFavoriteSurviveRoundTrip() {
        val page = Page.create("Metadata")
        page.folders.addAll(listOf("项目", "资料"))
        page.tags.addAll(listOf("重要", "设计"))
        page.favorite = true

        val restored = Page.decode(page.encode())

        assertEquals(setOf("项目", "资料"), restored.folders)
        assertEquals(setOf("重要", "设计"), restored.tags)
        assertTrue(restored.favorite)
    }

    @Test
    fun legacyFolderSetterUsesSingleFolderMembership() {
        val page = Page.create("Legacy")
        page.folders.add("old")
        page.folder = "new"

        assertEquals(setOf("new"), page.folders)
        assertEquals("new", page.folder)
    }

    @Test
    fun searchCanFilterByOneOfSeveralFoldersAndTags() {
        val page = Page.create("Design notes")
        page.folders.addAll(listOf("项目", "资料"))
        page.tags.add("重要")
        page.favorite = true
        val other = Page.create("Other")
        other.folders.add("项目")

        assertEquals(
            listOf(page.id),
            filterPages(
                listOf(page, other),
                query = "design",
                folder = "资料",
                trash = false,
                tag = "重要",
                favoritesOnly = true
            ).map { it.id }
        )
    }

    @Test
    fun deletingMetadatavaluesDoesNotDeleteThePage() {
        val page = Page.create("Keep")
        page.folders.add("folder")
        page.tags.add("tag")

        page.folders.clear()
        page.tags.clear()

        assertEquals("", page.folder)
        assertFalse(page.favorite)
        assertEquals("Keep", page.title)
    }

    @Test
    fun canceledGestureRestoresMetadata() {
        val page = Page.create("Gesture")
        page.folders.add("folder")
        page.tags.add("tag")
        page.favorite = true
        val gesture = PageGesture(page)

        page.folders.clear()
        page.tags.clear()
        page.favorite = false
        gesture.cancel()

        assertEquals(setOf("folder"), page.folders)
        assertEquals(setOf("tag"), page.tags)
        assertTrue(page.favorite)
    }

    @Test
    fun localBackupPreservesMetadata() {
        val page = Page.create("Backup")
        page.folders.addAll(listOf("one", "two"))
        page.tags.addAll(listOf("alpha", "beta"))
        page.favorite = true

        val restored = BackupArchive.decode(
            BackupArchive.encode(listOf(page), emptyMap())
        ).pages.single()

        assertEquals(setOf("one", "two"), restored.folders)
        assertEquals(setOf("alpha", "beta"), restored.tags)
        assertTrue(restored.favorite)
    }
}
