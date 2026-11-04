package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageStore
import com.example.tabletworkspace.data.pages.PageHistory

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

data class WorkspaceBootstrap(
    val loadedPages: PageLoadResult,
    val folders: List<String>,
    val templates: List<CardTemplate>,
    val sharedBlocks: List<SharedBlock>,
    val histories: Map<String, PageHistory>,
    val folderError: String?,
    val templateError: String?,
    val sharedError: String?
)

suspend fun loadWorkspaceBootstrap(store: PageStore): WorkspaceBootstrap =
    loadWorkspaceBootstrap(
        loadPages = store::loadAll,
        loadFolders = store::loadFolders,
        loadTemplates = store::loadTemplates,
        loadSharedBlocks = store::loadSharedBlocks,
        loadHistory = store::loadHistory
    )

internal suspend fun loadWorkspaceBootstrap(
    loadPages: () -> PageLoadResult,
    loadFolders: () -> List<String>,
    loadTemplates: () -> List<CardTemplate>,
    loadSharedBlocks: () -> List<SharedBlock>,
    loadHistory: (Page) -> PageHistory? = { null }
): WorkspaceBootstrap = coroutineScope {
    val pages = async(Dispatchers.IO) { loadPages() }
    val folders = async(Dispatchers.IO) { runCatching(loadFolders) }
    val templates = async(Dispatchers.IO) { runCatching(loadTemplates) }
    val shared = async(Dispatchers.IO) { runCatching(loadSharedBlocks) }
    awaitAll(pages, folders, templates, shared)
    val loadedPages = pages.await()
    val histories = withContext(Dispatchers.IO) {
        loadedPages.pages.mapNotNull { page ->
            runCatching { loadHistory(page) }.getOrNull()?.let { page.id to it }
        }.toMap()
    }
    WorkspaceBootstrap(
        loadedPages = loadedPages,
        folders = folders.await().getOrDefault(emptyList()),
        templates = templates.await().getOrDefault(emptyList()),
        sharedBlocks = shared.await().getOrDefault(emptyList()),
        histories = histories,
        folderError = folders.await().exceptionOrNull()?.message,
        templateError = templates.await().exceptionOrNull()?.message,
        sharedError = shared.await().exceptionOrNull()?.message
    )
}
