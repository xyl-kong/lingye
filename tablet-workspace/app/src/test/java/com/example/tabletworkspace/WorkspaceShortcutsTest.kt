package com.example.tabletworkspace

import com.example.tabletworkspace.WorkspaceShortcuts.AppAction
import com.example.tabletworkspace.WorkspaceShortcuts.DocumentAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkspaceShortcutsTest {
    @Test
    fun appShortcutsRequireCtrlAndMapShiftAsExpected() {
        assertEquals(AppAction.SEARCH, WorkspaceShortcuts.appActionFor('K', ctrl = true, shift = false, alt = false))
        assertEquals(AppAction.NEW_DOCUMENT, WorkspaceShortcuts.appActionFor('n', ctrl = true, shift = false, alt = false))
        assertEquals(AppAction.NEW_WHITEBOARD, WorkspaceShortcuts.appActionFor('N', ctrl = true, shift = true, alt = false))
        assertEquals(AppAction.SETTINGS, WorkspaceShortcuts.appActionFor(',', ctrl = true, shift = false, alt = false))
        assertNull(WorkspaceShortcuts.appActionFor('K', ctrl = false, shift = false, alt = false))
        assertNull(WorkspaceShortcuts.appActionFor('K', ctrl = true, shift = false, alt = true))
    }

    @Test
    fun documentShortcutsMapFormattingAndHeadings() {
        assertEquals(DocumentAction.BOLD, WorkspaceShortcuts.documentActionFor('B', ctrl = true, alt = false))
        assertEquals(DocumentAction.ITALIC, WorkspaceShortcuts.documentActionFor('i', ctrl = true, alt = false))
        assertEquals(DocumentAction.UNDERLINE, WorkspaceShortcuts.documentActionFor('U', ctrl = true, alt = false))
        assertEquals(DocumentAction.HEADING1, WorkspaceShortcuts.documentActionFor('1', ctrl = true, alt = true))
        assertEquals(DocumentAction.HEADING6, WorkspaceShortcuts.documentActionFor('6', ctrl = true, alt = true))
        assertEquals(DocumentAction.PARAGRAPH, WorkspaceShortcuts.documentActionFor('0', ctrl = true, alt = true))
        assertNull(WorkspaceShortcuts.documentActionFor('B', ctrl = false, alt = false))
        assertNull(WorkspaceShortcuts.documentActionFor('7', ctrl = true, alt = true))
    }

    @Test
    fun headingLevelsAndTypesRoundTrip() {
        assertEquals(1, WorkspaceShortcuts.headingLevel(DocumentAction.HEADING1))
        assertEquals(6, WorkspaceShortcuts.headingLevel(DocumentAction.HEADING6))
        assertNull(WorkspaceShortcuts.headingLevel(DocumentAction.BOLD))
        assertEquals("heading3", WorkspaceShortcuts.headingType(3))
        assertEquals("heading6", WorkspaceShortcuts.headingType(9))
    }
}
