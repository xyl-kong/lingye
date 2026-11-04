package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageRepository
import com.example.tabletworkspace.data.pages.PageHistory

import java.io.IOException
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageRepositoryTest {

    private class Recorder {
        val writes: MutableList<String> = Collections.synchronizedList(mutableListOf())
        var failNext = false
        var gate: CountDownLatch? = null
    }

    private fun repository(recorder: Recorder, onFailure: (Throwable) -> Unit = {}) = PageRepository(
        savePage = { page ->
            recorder.gate?.await(2, TimeUnit.SECONDS)
            if (recorder.failNext) throw IOException("disk full")
            recorder.writes += "page:${page.title}"
        },
        saveHistoryBytes = { id, _ -> recorder.writes += "history:$id" },
        deletePage = { id -> recorder.writes += "delete:$id" },
        onAsyncFailure = onFailure
    )

    @Test
    fun saveReturnsAfterWriteAndPropagatesFailure() {
        val recorder = Recorder()
        val repository = repository(recorder)
        val page = Page.create("first")
        repository.save(page)
        assertEquals(listOf("page:first"), recorder.writes)

        recorder.failNext = true
        var thrown: Throwable? = null
        try { repository.save(Page.create("second")) } catch (t: Throwable) { thrown = t }
        assertTrue(thrown is IOException)
    }

    @Test
    fun saveAllWritesPageThenHistory() {
        val recorder = Recorder()
        val repository = repository(recorder)
        val page = Page.create("p")
        val history = PageHistory(page)
        repository.saveAll(page, history)
        assertEquals("page:p", recorder.writes.first())
        assertEquals("history:${page.id}", recorder.writes.last())
    }

    @Test
    fun queuedWritesKeepSubmissionOrder() {
        val recorder = Recorder()
        val repository = repository(recorder)
        repository.save(Page.create("old"))

        val gate = CountDownLatch(1)
        recorder.gate = gate
        repository.saveAsync(Page.create("slow"))
        val newer = Page.create("new")
        val saver = Thread { repository.save(newer) }
        saver.start()
        Thread.sleep(50)
        gate.countDown()
        saver.join(2_000)

        assertEquals(listOf("page:old", "page:slow", "page:new"), recorder.writes)
    }

    @Test
    fun flushWaitsForAsyncWrites() {
        val recorder = Recorder()
        val repository = repository(recorder)
        repository.saveAsync(Page.create("a"))
        repository.saveAsync(Page.create("b"))
        assertTrue(repository.flush(2_000))
        assertEquals(2, recorder.writes.size)
        assertTrue(repository.flush(0))
    }

    @Test
    fun asyncFailureIsReportedButDoesNotThrow() {
        val recorder = Recorder()
        val reported = CountDownLatch(1)
        val repository = repository(recorder, onFailure = { reported.countDown() })
        recorder.failNext = true
        repository.saveAsync(Page.create("a"))
        assertTrue(reported.await(2, TimeUnit.SECONDS))
        assertTrue(recorder.writes.isEmpty())
    }

    @Test
    fun deleteIsQueuedBehindPendingSaves() {
        val recorder = Recorder()
        val repository = repository(recorder)
        val gate = CountDownLatch(1)
        recorder.gate = gate
        repository.saveAsync(Page.create("pending"))
        val deleter = Thread { repository.deletePermanently("id") }
        deleter.start()
        Thread.sleep(30)
        gate.countDown()
        deleter.join(2_000)
        assertEquals(listOf("page:pending", "delete:id"), recorder.writes)
    }

    @Test
    fun flushTimesOutWhileWriterIsBlocked() {
        val recorder = Recorder()
        val repository = repository(recorder)
        val gate = CountDownLatch(1)
        recorder.gate = gate
        repository.saveAsync(Page.create("blocked"))
        assertFalse(repository.flush(50))
        gate.countDown()
        assertTrue(repository.flush(2_000))
        assertEquals(listOf("page:blocked"), recorder.writes)
    }

    @Test
    fun saveHistoryEncodesBeforeQueueing() {
        val recorder = Recorder()
        val repository = repository(recorder)
        val page = Page.create("p")
        repository.saveHistory(page.id, PageHistory(page))
        assertEquals(listOf("history:${page.id}"), recorder.writes)
    }
}
