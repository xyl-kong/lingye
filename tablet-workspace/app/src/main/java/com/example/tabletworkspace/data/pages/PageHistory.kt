package com.example.tabletworkspace.data.pages

import com.example.tabletworkspace.Page
import com.example.tabletworkspace.pageFormatVersion
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class PageHistory private constructor(
    private val limit: Int,
    private val byteLimit: Int,
    private var current: ByteArray,
    initialUndo: List<ByteArray>,
    initialRedo: List<ByteArray>
) {
    private val undo = ArrayDeque<ByteArray>()
    private val redo = ArrayDeque<ByteArray>()
    private var lastCoalesceKey: String? = null
    private var lastRecordAtMillis: Long? = null

    init {
        undo.addAll(initialUndo.takeLast(limit))
        redo.addAll(initialRedo.takeLast(limit))
        trim()
    }

    constructor(page: Page, limit: Int = 100, byteLimit: Int = DEFAULT_BYTE_LIMIT) :
        this(limit, byteLimit, page.encode(), emptyList(), emptyList())

    @Synchronized
    fun record(page: Page, coalesceKey: String? = null, nowMillis: Long = System.currentTimeMillis()) {
        val next = page.encode(); if (next.contentEquals(current)) return
        val coalesce = lastCoalesceKey != null &&
            coalesceKey == lastCoalesceKey &&
            lastRecordAtMillis?.let { previous ->
            nowMillis >= previous && nowMillis - previous <= CONTINUOUS_EDIT_WINDOW_MS
        } == true
        if (!coalesce) {
            undo.addLast(current)
            if (undo.size > limit) undo.removeFirst()
        }
        current = next
        redo.clear()
        lastCoalesceKey = coalesceKey
        lastRecordAtMillis = if (coalesceKey == null) null else nowMillis
        trim()
    }
    @Synchronized
    fun undo(): Page? {
        if (undo.isEmpty()) return null
        redo.addLast(current); current = undo.removeLast(); lastRecordAtMillis = null; lastCoalesceKey = null
        return Page.decode(current)
    }
    @Synchronized
    fun redo(): Page? {
        if (redo.isEmpty()) return null
        undo.addLast(current); current = redo.removeLast(); lastRecordAtMillis = null; lastCoalesceKey = null
        return Page.decode(current)
    }

    /** Number of older states kept for undo; 0 means no history yet. */
    @Synchronized
    fun undoCount(): Int = undo.size

    /** Decodes an older state by index from oldest (0) to newest. */
    @Synchronized
    fun undoVersion(index: Int): Page? =
        undo.toList().getOrNull(index)?.let { runCatching { Page.decode(it) }.getOrNull() }

    /** Undoes until [index] (oldest = 0) becomes current and returns that state. */
    @Synchronized
    fun undoTo(index: Int): Page? {
        if (index < 0 || index >= undo.size) return null
        val steps = undo.size - index
        var page: Page? = null
        repeat(steps) {
            page = undo()
        }
        return page
    }

    @Synchronized
    fun encode(): ByteArray {
        val output = ByteArrayOutputStream()
        DataOutputStream(output).use { data ->
            data.writeInt(FORMAT_VERSION)
            writeSnapshot(data, current)
            data.writeInt(undo.size)
            undo.forEach { writeSnapshot(data, it) }
            data.writeInt(redo.size)
            redo.forEach { writeSnapshot(data, it) }
        }
        require(output.size() <= MAX_FILE_BYTES) { "撤销历史文件过大" }
        return output.toByteArray()
    }

    private fun writeSnapshot(output: DataOutputStream, bytes: ByteArray) {
        require(bytes.isNotEmpty() && bytes.size <= MAX_SNAPSHOT_BYTES) { "撤销快照大小无效" }
        output.writeInt(bytes.size)
        output.write(bytes)
    }

    private fun trim() {
        while (undo.size > limit) undo.removeFirst()
        while (redo.size > limit) redo.removeFirst()
        while (undo.isNotEmpty() &&
            undo.size + redo.size + 1 > 1 &&
            undo.sumOf { it.size } + redo.sumOf { it.size } + current.size > byteLimit
        ) {
            if (redo.isNotEmpty()) redo.removeFirst() else undo.removeFirst()
        }
    }

    companion object {
        private const val FORMAT_VERSION = 1
        private const val MAX_FILE_BYTES = 20_000_000
        private const val MAX_SNAPSHOT_BYTES = 20_000_000
        private const val DEFAULT_BYTE_LIMIT = 16_000_000
        private const val CONTINUOUS_EDIT_WINDOW_MS = 750L

        fun fromEncoded(page: Page, data: ByteArray): PageHistory {
            require(data.size <= MAX_FILE_BYTES) { "撤销历史文件过大" }
            return runCatching {
                DataInputStream(ByteArrayInputStream(data)).use { input ->
                    require(input.readInt() == FORMAT_VERSION) { "撤销历史版本无效" }
                    val current = canonicalSnapshot(readSnapshot(input))
                    require(current.contentEquals(page.encode())) { "撤销历史与页面不一致" }
                    val undo = readSnapshots(input).map(::canonicalSnapshot)
                    val redo = readSnapshots(input).map(::canonicalSnapshot)
                    require(input.available() == 0) { "撤销历史包含多余数据" }
                    PageHistory(100, DEFAULT_BYTE_LIMIT, current, undo, redo)
                }
            }.getOrElse { PageHistory(page) }
        }

        /** Old snapshots stay readable; rewrite them so the next save stores format 25 throughout. */
        private fun canonicalSnapshot(bytes: ByteArray): ByteArray =
            if (pageFormatVersion(bytes) == 25) bytes else Page.decode(bytes).encode()

        private fun readSnapshots(input: DataInputStream): List<ByteArray> {
            val count = input.readInt()
            require(count in 0..100) { "撤销历史数量无效" }
            return List(count) {
                readSnapshot(input).also { Page.decode(it) }
            }
        }

        private fun readSnapshot(input: DataInputStream): ByteArray {
            val length = input.readInt()
            require(length in 1..MAX_SNAPSHOT_BYTES && length <= input.available()) {
                "撤销快照长度无效"
            }
            return ByteArray(length).also { input.readFully(it) }
        }
    }
}
