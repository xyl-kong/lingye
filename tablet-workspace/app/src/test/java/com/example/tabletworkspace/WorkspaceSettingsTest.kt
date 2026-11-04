package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceSettingsTest {
    @Test
    fun defaultsMatchTheOfflineTabletWorkspace() {
        val settings = WorkspaceSettings()

        assertEquals("灵页", settings.workspaceName)
        assertEquals("离线创意空间", settings.workspaceSubtitle)
        assertEquals("sans", settings.editorFontFamily)
        assertEquals("medium", settings.gridSize)
        assertEquals(28f, settings.gridStep, 0.001f)
        assertTrue(settings.showGrid)
        assertTrue(settings.showLinkedDocs)
        assertTrue(settings.linkReturnToSource)
        assertFalse(settings.snapToGridDefault)
        assertEquals("iso", settings.dateTitleFormat)
        assertFalse(settings.codeBlockLineNumbers)
        assertFalse(settings.documentFullWidth)
        assertTrue(settings.displayDocInfo)
    }

    @Test
    fun canvasAndTextDefaultsAreNormalized() {
        val settings = WorkspaceSettings(
            canvasTheme = "neon",
            roughConnectors = true,
            defaultTextSize = 99f,
            defaultTextAlign = "justify"
        ).normalized()

        assertEquals("light", settings.canvasTheme)
        assertTrue(settings.roughConnectors)
        assertEquals(32f, settings.defaultTextSize, 0.001f)
        assertEquals("left", settings.defaultTextAlign)
    }

    @Test
    fun noteDefaultsNormalizeAndExposeCornerRadius() {
        val settings = WorkspaceSettings(
            noteCorner = "rounded?",
            noteShadow = "glow",
            noteBorderStyle = "double",
            noteBorderWidth = 99f
        ).normalized()

        assertEquals("medium", settings.noteCorner)
        assertEquals("box", settings.noteShadow)
        assertEquals("solid", settings.noteBorderStyle)
        assertEquals(12f, settings.noteBorderWidth, 0.001f)
        assertEquals(16f, settings.noteCornerRadius, 0.001f)
        assertTrue(settings.showZoomButtons)
        assertEquals(32f, WorkspaceSettings(noteCorner = "huge").noteCornerRadius, 0.001f)
        assertEquals(0f, WorkspaceSettings(noteCorner = "none").noteCornerRadius, 0.001f)
    }

    @Test
    fun dateTitleFormatsProduceTheExpectedPrefix() {
        val date = java.time.LocalDate.of(2026, 10, 31)

        assertEquals("2026-10-31", formatDateTitle("iso", date))
        assertEquals("2026/10/31", formatDateTitle("slash", date))
        assertEquals("10-31", formatDateTitle("monthDay", date))
        assertEquals("10月31日", formatDateTitle("chinese", date))
        assertEquals("2026-10-31", formatDateTitle("unknown", date))
    }

    @Test
    fun normalizesBlankNamesAndLengths() {
        val settings = WorkspaceSettings(
            workspaceName = "   ",
            workspaceSubtitle = "  ",
            connectorPathStyle = "invalid",
            connectorEndMarker = "invalid"
        ).normalized()

        assertEquals("灵页", settings.workspaceName)
        assertEquals("离线创意空间", settings.workspaceSubtitle)
        assertEquals("curve", settings.connectorPathStyle)
        assertEquals("arrow", settings.connectorEndMarker)
        assertEquals(40, WorkspaceSettings(workspaceName = "x".repeat(80)).normalized().workspaceName.length)
        assertEquals(60, WorkspaceSettings(workspaceSubtitle = "y".repeat(90)).normalized().workspaceSubtitle.length)
    }

    @Test
    fun rejectsUnknownFontAndGridValues() {
        val settings = WorkspaceSettings(editorFontFamily = "comic", gridSize = "huge").normalized()

        assertEquals("sans", settings.editorFontFamily)
        assertEquals("medium", settings.gridSize)
        assertEquals(28f, settings.gridStep, 0.001f)
    }

    @Test
    fun gridStepsFollowTheSelectedSize() {
        assertEquals(20f, WorkspaceSettings(gridSize = "small").gridStep, 0.001f)
        assertEquals(28f, WorkspaceSettings(gridSize = "medium").gridStep, 0.001f)
        assertEquals(36f, WorkspaceSettings(gridSize = "large").gridStep, 0.001f)
    }
}
