package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkspaceSessionTest {
    @Test
    fun storedWhiteboardSessionRoundTrips() {
        val session = WorkspaceSession(
            pageId = "page-1",
            view = WorkspaceView.WHITEBOARD,
            tool = "画笔",
            canvasScale = 2.5f,
            canvasOffsetX = -120f,
            canvasOffsetY = 48f,
            selectedObjectIds = listOf("shape-1", "connector-2"),
            documentScroll = 420,
            documentScrollIndex = 2,
            pageListIndex = 4,
            pageListOffset = 13
        )

        assertEquals(session, WorkspaceSessionCodec.decode(session.encode()))
    }

    @Test
    fun unknownViewAndToolFallBackSafely() {
        val session = WorkspaceSessionCodec.decode(
            mapOf(
                WorkspaceSessionCodec.PAGE_ID to "page-2",
                WorkspaceSessionCodec.VIEW to "future-view",
                WorkspaceSessionCodec.TOOL to "future-tool"
            )
        )

        assertEquals("page-2", session.pageId)
        assertEquals(WorkspaceView.DOCUMENT, session.view)
        assertEquals("移动", session.tool)
    }

    @Test
    fun invalidViewportFallsBackToReadableDefaults() {
        val session = WorkspaceSessionCodec.decode(
            mapOf(
                WorkspaceSessionCodec.SCALE to "NaN",
                WorkspaceSessionCodec.OFFSET_X to "Infinity",
                WorkspaceSessionCodec.OFFSET_Y to "not-a-number"
            )
        )

        assertEquals(1f, session.canvasScale)
        assertEquals(0f, session.canvasOffsetX)
        assertEquals(0f, session.canvasOffsetY)
    }

    @Test
    fun storedTenPercentZoomIsKept() {
        val session = WorkspaceSessionCodec.decode(
            mapOf(WorkspaceSessionCodec.SCALE to "0.1")
        )

        assertEquals(0.1f, session.canvasScale, 0.0001f)
    }

    @Test
    fun storedZoomBelowTenPercentIsClamped() {
        val session = WorkspaceSessionCodec.decode(
            mapOf(WorkspaceSessionCodec.SCALE to "0.02")
        )

        assertEquals(CanvasZoom.MIN, session.canvasScale, 0.0001f)
    }

    @Test
    fun storedSelectionIdsAreDeduplicatedAndBlankEntriesRemoved() {        val session = WorkspaceSessionCodec.decode(
            mapOf(
                WorkspaceSessionCodec.SELECTION_IDS to "a,,b,a, "
            )
        )

        assertEquals(listOf("a", "b"), session.selectedObjectIds)
    }

    @Test
    fun invalidScrollValuesFallBackToTop() {
        val session = WorkspaceSessionCodec.decode(
            mapOf(
                WorkspaceSessionCodec.DOCUMENT_SCROLL to "-1",
                WorkspaceSessionCodec.DOCUMENT_SCROLL_INDEX to "-4",
                WorkspaceSessionCodec.PAGE_LIST_INDEX to "-2",
                WorkspaceSessionCodec.PAGE_LIST_OFFSET to "-3"
            )
        )

        assertEquals(0, session.documentScroll)
        assertEquals(0, session.documentScrollIndex)
        assertEquals(0, session.pageListIndex)
        assertEquals(0, session.pageListOffset)
    }
}
