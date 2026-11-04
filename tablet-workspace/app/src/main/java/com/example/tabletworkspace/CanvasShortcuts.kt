package com.example.tabletworkspace

/**
 * Keyboard shortcuts for the edgeless canvas. Letters follow the official BlockSuite tool tips
 * (Hand H, Select V, Frame F, Connector C, Link @, Pen P, Eraser E, Shape S, Text T).
 */
object CanvasShortcuts {
    enum class Action { UNDO, REDO, DELETE, ESCAPE, ZOOM_IN, ZOOM_OUT, ZOOM_RESET, NUDGE_LEFT, NUDGE_RIGHT, NUDGE_UP, NUDGE_DOWN, SELECT_ALL }

    /** Canonical tool names used by the canvas; Shift+P selects the highlighter. */
    fun toolFor(letter: Char, shift: Boolean = false): String? = when (letter.uppercaseChar()) {
        'V' -> "选择"
        'H' -> "移动"
        'F' -> "Frame"
        'C' -> "连线"
        'T' -> "文本"
        'P' -> if (shift) "高亮笔" else "画笔"
        'E' -> "橡皮"
        'S' -> "矩形"
        'M' -> "思维导图"
        'L' -> "链接"
        else -> null
    }

    fun actionFor(letter: Char, ctrl: Boolean, shift: Boolean, arrow: Action? = null): Action? {
        if (arrow != null) return arrow
        if (ctrl) {
            return when (letter.uppercaseChar()) {
                'Z' -> if (shift) Action.REDO else Action.UNDO
                'Y' -> Action.REDO
                'A' -> Action.SELECT_ALL
                '+' -> Action.ZOOM_IN
                '=' -> Action.ZOOM_IN
                '-' -> Action.ZOOM_OUT
                '0' -> Action.ZOOM_RESET
                else -> null
            }
        }
        return when (letter) {
            '\u001B' -> Action.ESCAPE
            '\u007F', '\b' -> Action.DELETE
            else -> null
        }
    }

    /** Shortcut shown in tooltips, e.g. `Ctrl+Z`. */
    fun label(letter: Char, ctrl: Boolean, shift: Boolean): String? {
        if (!ctrl) return null
        val key = letter.uppercaseChar()
        if (key !in setOf('Z', 'Y', 'A', '=', '-', '0')) return null
        return "Ctrl+" + (if (shift && key == 'Z') "Shift+" else "") + key
    }
}
