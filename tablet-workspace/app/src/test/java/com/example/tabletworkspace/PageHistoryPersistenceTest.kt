package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageHistory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PageHistoryPersistenceTest {
    @Test
    fun rapidTextSnapshotsCoalesceIntoOneUndoStep() {
        val page = Page.create("start")
        val history = PageHistory(page)

        page.title = "s"
        history.record(page, "title", 1_000)
        page.title = "su"
        history.record(page, "title", 1_100)
        page.title = "sun"
        history.record(page, "title", 1_200)

        assertEquals("start", history.undo()!!.title)
        assertNull(history.undo())
    }

    @Test
    fun pausingStartsANewUndoStep() {
        val page = Page.create("start")
        val history = PageHistory(page)

        page.title = "first"
        history.record(page, "title", 1_000)
        page.title = "second"
        history.record(page, "title", 2_000)

        assertEquals("first", history.undo()!!.title)
        assertEquals("start", history.undo()!!.title)
    }

    @Test
    fun undoToRestoresAnOlderVersion() {
        val page = Page.create("start")
        val history = PageHistory(page)
        page.title = "second"
        history.record(page)
        page.title = "third"
        history.record(page)

        assertEquals(2, history.undoCount())
        assertEquals("start", history.undoVersion(0)!!.title)
        assertEquals("second", history.undoVersion(1)!!.title)
        assertEquals("start", history.undoTo(0)!!.title)
        assertEquals(0, history.undoCount())
    }

    @Test
    fun encodedHistoryRestoresUndoAndRedoStacks() {
        val page = Page.create("persistent history")
        val history = PageHistory(page)
        page.addShape("rect", 10f, 20f, 30f, 40f)
        history.record(page)
        page.addConnector(0f, 0f, 20f, 20f)
        history.record(page)

        val restored = PageHistory.fromEncoded(page, history.encode())

        assertEquals(0, restored.undo()!!.connectors.size)
        assertEquals(1, restored.redo()!!.connectors.size)
    }

    @Test
    fun historyIsDiscardedWhenStoredCurrentDoesNotMatchPage() {
        val stored = Page.create("same id")
        stored.addShape("rect", 0f, 0f, 10f, 10f)
        val history = PageHistory(stored)
        stored.addBlock("paragraph", "newer")
        history.record(stored)

        val mismatched = Page.decode(stored.encode()).also {
            it.addBlock("paragraph", "different")
        }
        val restored = PageHistory.fromEncoded(mismatched, history.encode())

        assertNull(restored.undo())
        assertNull(restored.redo())
    }
}
