package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Test

class BackNavigationPolicyTest {
    @Test
    fun inlineEditingClosesBeforeCanvasNavigation() {
        val action = BackNavigationPolicy.resolve(
            BackState(
                inlineEditing = true,
                fullscreen = true,
                selectionActive = true,
                view = WorkspaceView.WHITEBOARD
            )
        )

        assertEquals(BackAction.FINISH_INLINE_EDIT, action)
    }

    @Test
    fun emptyCanvasReturnsToDocumentBeforeExiting() {
        val action = BackNavigationPolicy.resolve(
            BackState(view = WorkspaceView.WHITEBOARD)
        )

        assertEquals(BackAction.LEAVE_WHITEBOARD, action)
    }

    @Test
    fun settingsReturnsToDocumentBeforeExiting() {
        val action = BackNavigationPolicy.resolve(
            BackState(view = WorkspaceView.SETTINGS)
        )

        assertEquals(BackAction.LEAVE_SETTINGS, action)
    }

    @Test
    fun documentRootAllowsSystemExit() {
        val action = BackNavigationPolicy.resolve(
            BackState(view = WorkspaceView.DOCUMENT)
        )

        assertEquals(BackAction.EXIT_APP, action)
    }
}
