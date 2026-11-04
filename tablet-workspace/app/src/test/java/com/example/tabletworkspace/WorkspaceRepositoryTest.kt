package com.example.tabletworkspace

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceRepositoryTest {

    private class FakePreferences : WorkspacePreferenceStore {
        val values = mutableMapOf<String, Any>()
        override fun string(key: String, fallback: String?): String? = values[key] as? String ?: fallback
        override fun boolean(key: String, fallback: Boolean): Boolean = values[key] as? Boolean ?: fallback
        override fun int(key: String, fallback: Int): Int = values[key] as? Int ?: fallback
        override fun float(key: String, fallback: Float): Float = values[key] as? Float ?: fallback
        override fun putString(key: String, value: String) { values[key] = value }
        override fun putBoolean(key: String, value: Boolean) { values[key] = value }
        override fun putInt(key: String, value: Int) { values[key] = value }
        override fun putFloat(key: String, value: Float) { values[key] = value }
        override fun write(block: (WorkspacePreferenceEditor) -> Unit) {
            block(object : WorkspacePreferenceEditor {
                override fun string(key: String, value: String) { values[key] = value }
                override fun boolean(key: String, value: Boolean) { values[key] = value }
                override fun int(key: String, value: Int) { values[key] = value }
                override fun float(key: String, value: Float) { values[key] = value }
            })
        }
    }

    private fun repository(prefs: FakePreferences) =
        WorkspaceRepository(loadBootstrap = { error("not used") }, preferences = prefs)

    @Test
    fun sessionRoundTripsThroughPreferences() {
        val prefs = FakePreferences()
        val repository = repository(prefs)
        val session = WorkspaceSession(
            pageId = "p1",
            view = WorkspaceView.WHITEBOARD,
            tool = "画笔",
            canvasScale = 1.5f,
            canvasOffsetX = 12f,
            canvasOffsetY = -8f,
            selectedObjectIds = listOf("a", "b"),
            documentScroll = 120,
            documentScrollIndex = 3,
            pageListIndex = 4,
            pageListOffset = 40
        )
        repository.writeSession(session)
        assertEquals(session, repository.readSession())
    }

    @Test
    fun sessionDefaultsWhenNothingStored() {
        val session = repository(FakePreferences()).readSession()
        assertEquals("", session.pageId)
        assertEquals(WorkspaceView.DOCUMENT, session.view)
        assertEquals("移动", session.tool)
        assertEquals(1f, session.canvasScale, 0f)
        assertTrue(session.selectedObjectIds.isEmpty())
    }

    @Test
    fun languageFallsBackToSystemAndPersistsChoice() {
        val prefs = FakePreferences()
        val repository = repository(prefs)
        assertEquals(AppLanguage.ZH, repository.readLanguage("zh"))
        assertEquals(AppLanguage.EN, repository.readLanguage("en"))
        repository.writeLanguage(AppLanguage.EN)
        assertEquals(AppLanguage.EN, repository.readLanguage("zh"))
    }

    @Test
    fun canvasToolsUseDefaultsAndClampStoredOutliers() {
        val defaults = repository(FakePreferences()).readCanvasTools()
        assertEquals(false, defaults.penOnly)
        assertEquals(false, defaults.eraserArea)
        assertEquals(BrushSettings.KIND_BRUSH, defaults.brushKind)
        assertEquals(22f, defaults.highlighterWidth, 0f)
        assertEquals(3f, defaults.inkWidth, 0f)

        val prefs = FakePreferences().apply {
            putFloat("highlighter_width", 5f)
            putFloat("width", 99f)
            putString("brush_kind", "highlighter")
        }
        val clamped = repository(prefs).readCanvasTools()
        assertEquals(10f, clamped.highlighterWidth, 0f)
        assertEquals(12f, clamped.inkWidth, 0f)
        assertEquals("highlighter", clamped.brushKind)
    }

    @Test
    fun toolSavesAreReadBack() {
        val prefs = FakePreferences()
        val repository = repository(prefs)
        repository.savePenOnly(true)
        repository.saveEraserArea(true)
        repository.saveBrushKind("highlighter")
        repository.saveHighlighterColor(0xff112233.toInt())
        repository.saveHighlighterWidth(18f)
        repository.saveInkColor(0xff445566.toInt())
        repository.saveInkWidth(6f)
        val tools = repository.readCanvasTools()
        assertTrue(tools.penOnly)
        assertTrue(tools.eraserArea)
        assertEquals("highlighter", tools.brushKind)
        assertEquals(0xff112233.toInt(), tools.highlighterColor)
        assertEquals(18f, tools.highlighterWidth, 0f)
        assertEquals(0xff445566.toInt(), tools.inkColor)
        assertEquals(6f, tools.inkWidth, 0f)
    }

    @Test
    fun loadDelegatesToBootstrapLoader() = runBlocking {
        val page = Page.create("p")
        val expected = WorkspaceBootstrap(
            loadedPages = PageLoadResult(listOf(page), emptyList()),
            folders = listOf("Folder"),
            templates = emptyList(),
            sharedBlocks = emptyList(),
            histories = emptyMap(),
            folderError = null,
            templateError = null,
            sharedError = null
        )
        val repository = WorkspaceRepository(loadBootstrap = { expected }, preferences = FakePreferences())
        val result = repository.load()
        assertEquals(listOf(page), result.loadedPages.pages)
        assertEquals(listOf("Folder"), result.folders)
    }

    @Test
    fun settingsRoundTripThroughStore() {
        val repository = repository(FakePreferences())
        val settings = WorkspaceSettings(
            themeMode = "dark",
            autoDateTitle = true,
            editorFontSize = 18f,
            editorFontFamily = "serif",
            workspaceName = "Studio",
            connectorWidth = 6f
        ).normalized()
        repository.writeSettings(settings)
        assertEquals(settings, repository.readSettings())
    }

    @Test
    fun legacyMotionFallbackAppliesOnlyWhenKeyMissing() {
        val repository = repository(FakePreferences())
        assertFalse(repository.readSettings(legacyMotionEnabled = false).motionEnabled)
        assertTrue(repository.readSettings(legacyMotionEnabled = true).motionEnabled)

        val stored = FakePreferences().apply { putBoolean("motion_enabled", false) }
        assertFalse(repository(stored).readSettings(legacyMotionEnabled = true).motionEnabled)
    }

    @Test
    fun settingsAreNormalizedOnWrite() {
        val repository = repository(FakePreferences())
        repository.writeSettings(WorkspaceSettings(editorFontSize = 99f, defaultTextSize = 1f))
        val read = repository.readSettings()
        assertEquals(24f, read.editorFontSize, 0f)
        assertEquals(12f, read.defaultTextSize, 0f)
    }
}
