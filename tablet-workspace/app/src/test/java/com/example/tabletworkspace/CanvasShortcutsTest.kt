package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CanvasShortcutsTest {
    @Test
    fun toolLettersMatchOfficialTips() {
        assertEquals("选择", CanvasShortcuts.toolFor('V'))
        assertEquals("移动", CanvasShortcuts.toolFor('H'))
        assertEquals("Frame", CanvasShortcuts.toolFor('f'))
        assertEquals("连线", CanvasShortcuts.toolFor('C'))
        assertEquals("文本", CanvasShortcuts.toolFor('T'))
        assertEquals("画笔", CanvasShortcuts.toolFor('P'))
        assertEquals("高亮笔", CanvasShortcuts.toolFor('p', shift = true))
        assertEquals("橡皮", CanvasShortcuts.toolFor('E'))
        assertEquals("矩形", CanvasShortcuts.toolFor('S'))
        assertEquals("思维导图", CanvasShortcuts.toolFor('M'))
        assertEquals("链接", CanvasShortcuts.toolFor('L'))
        assertNull(CanvasShortcuts.toolFor('Q'))
    }

    @Test
    fun commandShortcuts() {
        assertEquals(CanvasShortcuts.Action.UNDO, CanvasShortcuts.actionFor('z', true, false))
        assertEquals(CanvasShortcuts.Action.REDO, CanvasShortcuts.actionFor('Z', true, true))
        assertEquals(CanvasShortcuts.Action.REDO, CanvasShortcuts.actionFor('y', true, false))
        assertEquals(CanvasShortcuts.Action.SELECT_ALL, CanvasShortcuts.actionFor('a', true, false))
        assertEquals(CanvasShortcuts.Action.ZOOM_IN, CanvasShortcuts.actionFor('=', true, false))
        assertEquals(CanvasShortcuts.Action.ZOOM_OUT, CanvasShortcuts.actionFor('-', true, false))
        assertEquals(CanvasShortcuts.Action.ZOOM_RESET, CanvasShortcuts.actionFor('0', true, false))
        assertNull("plain Z is not undo", CanvasShortcuts.actionFor('z', false, false))
    }

    @Test
    fun editingKeys() {
        assertEquals(CanvasShortcuts.Action.DELETE, CanvasShortcuts.actionFor('\u007F', false, false))
        assertEquals(CanvasShortcuts.Action.DELETE, CanvasShortcuts.actionFor('\b', false, false))
        assertEquals(CanvasShortcuts.Action.ESCAPE, CanvasShortcuts.actionFor('\u001B', false, false))
        assertEquals("arrows are passed explicitly", CanvasShortcuts.Action.NUDGE_LEFT, CanvasShortcuts.actionFor('x', false, false, CanvasShortcuts.Action.NUDGE_LEFT))
    }

    @Test
    fun tooltipLabels() {
        assertEquals("Ctrl+Z", CanvasShortcuts.label('z', true, false))
        assertEquals("Ctrl+Shift+Z", CanvasShortcuts.label('z', true, true))
        assertEquals("Ctrl+Y", CanvasShortcuts.label('y', true, false))
        assertNull(CanvasShortcuts.label('v', false, false))
    }
}
