package com.example.tabletworkspace

/**
 * Fixed app-level and document-editing key maps. Canvas tool keys live in [CanvasShortcuts].
 * `Ctrl` also accepts the macOS Command key at the call sites.
 */
object WorkspaceShortcuts {
    enum class AppAction { SEARCH, NEW_DOCUMENT, NEW_WHITEBOARD, SETTINGS }

    enum class DocumentAction {
        BOLD, ITALIC, UNDERLINE, PARAGRAPH,
        HEADING1, HEADING2, HEADING3, HEADING4, HEADING5, HEADING6
    }

    fun appActionFor(letter: Char, ctrl: Boolean, shift: Boolean, alt: Boolean): AppAction? {
        if (!ctrl || alt) return null
        return when (letter.uppercaseChar()) {
            'K' -> if (shift) null else AppAction.SEARCH
            'N' -> if (shift) AppAction.NEW_WHITEBOARD else AppAction.NEW_DOCUMENT
            ',' -> if (shift) null else AppAction.SETTINGS
            else -> null
        }
    }

    fun documentActionFor(letter: Char, ctrl: Boolean, alt: Boolean): DocumentAction? {
        if (!ctrl) return null
        if (alt) {
            return when (letter) {
                '0' -> DocumentAction.PARAGRAPH
                '1' -> DocumentAction.HEADING1
                '2' -> DocumentAction.HEADING2
                '3' -> DocumentAction.HEADING3
                '4' -> DocumentAction.HEADING4
                '5' -> DocumentAction.HEADING5
                '6' -> DocumentAction.HEADING6
                else -> null
            }
        }
        return when (letter.uppercaseChar()) {
            'B' -> DocumentAction.BOLD
            'I' -> DocumentAction.ITALIC
            'U' -> DocumentAction.UNDERLINE
            else -> null
        }
    }

    fun headingLevel(action: DocumentAction): Int? = when (action) {
        DocumentAction.HEADING1 -> 1
        DocumentAction.HEADING2 -> 2
        DocumentAction.HEADING3 -> 3
        DocumentAction.HEADING4 -> 4
        DocumentAction.HEADING5 -> 5
        DocumentAction.HEADING6 -> 6
        else -> null
    }

    fun headingType(level: Int): String = "heading${level.coerceIn(1, 6)}"
}
