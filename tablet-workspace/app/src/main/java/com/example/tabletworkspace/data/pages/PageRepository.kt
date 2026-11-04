package com.example.tabletworkspace.data.pages

import com.example.tabletworkspace.Page
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Serializes every page/history write on one background thread so the UI only takes a
 * snapshot on the main thread and never encodes or touches disk there.
 *
 * Synchronous calls wait for their queued write (and rethrow its failure) to keep the
 * existing "saved before navigating" guarantees; autosave uses the async variants and
 * reports failures through [onAsyncFailure]. A single queue keeps writes in submission
 * order, so a late autosave snapshot can never overwrite a newer rename/structural save.
 */
class PageRepository(
    private val savePage: (Page) -> Unit,
    private val saveHistoryBytes: (String, ByteArray) -> Unit,
    private val deletePage: (String) -> Unit,
    private val onAsyncFailure: (Throwable) -> Unit = {}
) {
    constructor(store: PageStore, onAsyncFailure: (Throwable) -> Unit = {}) : this(
        savePage = { store.save(it) },
        saveHistoryBytes = { id, bytes -> store.saveHistoryEncoded(id, bytes) },
        deletePage = { store.deletePermanently(it) },
        onAsyncFailure = onAsyncFailure
    )

    private val writer = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "lingye-page-writer").apply { isDaemon = true }
    }
    private val lock = ReentrantLock()
    private val idle = lock.newCondition()
    private var pending = 0

    /** Snapshots, encodes and writes the page, then returns once it is on disk. */
    fun save(page: Page) {
        val snapshot = page.snapshot()
        await(enqueue { savePage(snapshot) })
    }

    /** Saves page and history in one batch; returns after both are on disk. */
    fun saveAll(page: Page, history: PageHistory?) {
        val snapshot = page.snapshot()
        val historyBytes = history?.encode()
        val pageTask = enqueue { savePage(snapshot) }
        val historyTask = historyBytes?.let { bytes -> enqueue { saveHistoryBytes(page.id, bytes) } }
        await(pageTask)
        historyTask?.let { await(it) }
    }

    fun saveHistory(pageId: String, history: PageHistory) {
        val bytes = history.encode()
        await(enqueue { saveHistoryBytes(pageId, bytes) })
    }

    /** Queues a page snapshot without waiting; [onSaved] runs on the writer thread. */
    fun saveAsync(page: Page, onSaved: () -> Unit = {}) {
        val snapshot = page.snapshot()
        enqueueAsync {
            savePage(snapshot)
            onSaved()
        }
    }

    fun saveHistoryAsync(pageId: String, history: PageHistory) {
        val bytes = history.encode()
        enqueueAsync { saveHistoryBytes(pageId, bytes) }
    }

    fun deletePermanently(pageId: String) {
        await(enqueue { deletePage(pageId) })
    }

    /**
     * Waits until every queued write finished. Returns false when [timeoutMillis]
     * elapsed first; the caller decides whether that is an error.
     */
    fun flush(timeoutMillis: Long = DEFAULT_TIMEOUT_MS): Boolean {
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis)
        lock.lock()
        try {
            while (pending > 0) {
                val remaining = deadline - System.nanoTime()
                if (remaining <= 0) return false
                idle.awaitNanos(remaining)
            }
            return true
        } finally {
            lock.unlock()
        }
    }

    private fun enqueue(task: () -> Unit): Future<*> {
        lock.withLock { pending++ }
        return try {
            writer.submit {
                try { task() } finally { completeOne() }
            }
        } catch (t: Throwable) {
            completeOne()
            throw t
        }
    }

    private fun enqueueAsync(task: () -> Unit) {
        enqueue {
            try { task() } catch (t: Throwable) { onAsyncFailure(t) }
        }
    }

    private fun completeOne() {
        lock.withLock {
            pending--
            if (pending == 0) idle.signalAll()
        }
    }

    private fun await(future: Future<*>) {
        try {
            future.get(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        } catch (e: TimeoutException) {
            throw IllegalStateException("保存超时：写入队列未在限定时间内完成", e)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("保存被中断", e)
        }
    }

    companion object {
        const val DEFAULT_TIMEOUT_MS = 15_000L
    }
}
