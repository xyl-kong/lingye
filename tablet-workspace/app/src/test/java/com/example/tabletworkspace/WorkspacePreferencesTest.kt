package com.example.tabletworkspace

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkspacePreferencesTest {
    @Test
    fun autoDateNamingUsesIsoDate() {
        val settings = WorkspaceSettings(autoDateTitle = true)

        assertEquals(
            "2026-10-30",
            newPageTitle(AppLanguage.ZH, settings, LocalDate.of(2026, 10, 30))
        )
    }

    @Test
    fun manualNamingUsesLocalizedUntitledTitle() {
        val settings = WorkspaceSettings(autoDateTitle = false)
        val date = LocalDate.of(2026, 10, 30)

        assertEquals("未命名页面", newPageTitle(AppLanguage.ZH, settings, date))
        assertEquals("Untitled page", newPageTitle(AppLanguage.EN, settings, date))
    }

    @Test
    fun invalidEditorAndConnectorDefaultsAreNormalized() {
        val settings = WorkspaceSettings(
            editorFontSize = 99f,
            connectorPathStyle = "zigzag",
            connectorEndMarker = "square"
        ).normalized()

        assertEquals(24f, settings.editorFontSize)
        assertEquals("curve", settings.connectorPathStyle)
        assertEquals("arrow", settings.connectorEndMarker)
    }

    @Test
    fun themeModeFallsBackToSystemWhenUnknown() {
        assertEquals("system", WorkspaceSettings(themeMode = "neon").normalized().themeMode)
        assertEquals("light", WorkspaceSettings(themeMode = "light").normalized().themeMode)
        assertEquals("dark", WorkspaceSettings(themeMode = "dark").normalized().themeMode)
    }

    @Test
    fun newCanvasObjectsUseConfiguredDefaults() {
        val page = Page.create("Defaults")
        val color = 0xff123456.toInt()

        page.addCard(CardPreset.NOTE, 10f, 20f, AppLanguage.ZH, color)
        page.addShape("rect", 30f, 40f, 50f, 60f, color)
        page.addConnector(1f, 2f, 3f, 4f, color, "step", "arrow")

        assertEquals(color, page.shapes[0].color)
        assertEquals(color, page.shapes[1].color)
        assertEquals(color, page.connectors[0].color)
        assertEquals("step", page.connectors[0].pathStyle)
        assertEquals("arrow", page.connectors[0].endMarker)
    }
}
