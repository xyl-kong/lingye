package com.example.tabletworkspace

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceBootstrapTest {
    @Test fun pageFailureDoesNotPreventOtherWorkspaceDataFromLoading() = runBlocking {
        val page=Page.create("loaded")
        val result=loadWorkspaceBootstrap(
            loadPages={PageLoadResult(listOf(page),listOf("broken.page"))},
            loadFolders={listOf("Projects")},
            loadTemplates={throw IllegalStateException("template failure")},
            loadSharedBlocks={emptyList()}
        )
        assertEquals(listOf("loaded"),result.loadedPages.pages.map { it.title })
        assertEquals(listOf("broken.page"),result.loadedPages.failed)
        assertEquals(listOf("Projects"),result.folders)
        assertEquals("template failure",result.templateError)
        assertNull(result.folderError)
        assertNull(result.sharedError)
    }

    @Test fun allIndependentLoadFailuresAreReported() = runBlocking {
        val result=loadWorkspaceBootstrap(
            loadPages={PageLoadResult(emptyList(),emptyList())},
            loadFolders={error("folder failure")},
            loadTemplates={error("template failure")},
            loadSharedBlocks={error("shared failure")}
        )
        assertTrue(result.folderError!!.contains("folder"))
        assertTrue(result.templateError!!.contains("template"))
        assertTrue(result.sharedError!!.contains("shared"))
    }
}
