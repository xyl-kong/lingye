package com.example.tabletworkspace.data.metadata

import com.example.tabletworkspace.DocPropertyStore
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.UUID

/**
 * Per-page metadata that lives outside the format-25 page file.
 * Used by backup/restore so document properties and journal/template flags survive.
 */
data class PageMetadataSnapshot(
    val docProperties: Map<String, String> = emptyMap(),
    val journalDates: Map<String, String> = emptyMap(),
    val templatePageIds: Set<String> = emptySet()
) {
    fun isEmpty(): Boolean = docProperties.isEmpty() && journalDates.isEmpty() && templatePageIds.isEmpty()
}

object PageMetadataSnapshotCodec {
    private const val MAGIC = 0x4C504D31
    private const val MAX_PAGES = 100_000
    private const val MAX_STRING = 2_000_000
    private const val MAX_TOTAL = 20_000_000
    private val DATE = Regex("\\d{4}-\\d{2}-\\d{2}")

    fun encode(snapshot: PageMetadataSnapshot): ByteArray {
        snapshot.journalDates.forEach { (_, date) -> require(date.matches(DATE)) { "无效 Journal 日期" } }
        snapshot.docProperties.forEach { (_, raw) -> DocPropertyStore.decode(raw) }
        val ids = (snapshot.docProperties.keys + snapshot.journalDates.keys + snapshot.templatePageIds)
            .distinct().sorted()
        require(ids.size <= MAX_PAGES) { "元数据页面过多" }
        val out = ByteArrayOutputStream()
        DataOutputStream(out).use { data ->
            var total = 0
            fun writeString(value: String) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                require(bytes.size <= MAX_STRING) { "元数据项过大" }
                total += bytes.size
                require(total <= MAX_TOTAL) { "元数据过大" }
                data.writeInt(bytes.size)
                data.write(bytes)
            }
            data.writeInt(MAGIC)
            data.writeInt(ids.size)
            ids.forEach { id ->
                require(runCatching { UUID.fromString(id).toString() == id }.getOrDefault(false)) { "无效页面 ID" }
                data.writeUTF(id)
                writeString(snapshot.journalDates[id].orEmpty())
                data.writeBoolean(id in snapshot.templatePageIds)
                writeString(snapshot.docProperties[id].orEmpty())
            }
        }
        return out.toByteArray()
    }

    fun decode(bytes: ByteArray): PageMetadataSnapshot {
        require(bytes.size <= MAX_TOTAL) { "元数据过大" }
        var total = 0
        fun readString(input: DataInputStream): String {
            val length = input.readInt()
            require(length in 0..MAX_STRING) { "元数据项无效" }
            total += length
            require(total <= MAX_TOTAL) { "元数据过大" }
            val data = ByteArray(length)
            input.readFully(data)
            return String(data, Charsets.UTF_8)
        }
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            require(input.readInt() == MAGIC) { "元数据格式无效" }
            val count = input.readInt()
            require(count in 0..MAX_PAGES) { "元数据页面数无效" }
            val docProperties = mutableMapOf<String, String>()
            val journalDates = mutableMapOf<String, String>()
            val templates = mutableSetOf<String>()
            repeat(count) {
                val id = input.readUTF()
                require(runCatching { UUID.fromString(id).toString() == id }.getOrDefault(false)) { "无效页面 ID" }
                val journal = readString(input)
                if (journal.isNotEmpty()) {
                    require(journal.matches(DATE)) { "无效 Journal 日期" }
                    journalDates[id] = journal
                }
                if (input.readBoolean()) templates.add(id)
                val raw = readString(input)
                if (raw.isNotEmpty()) {
                    DocPropertyStore.decode(raw)
                    docProperties[id] = raw
                }
            }
            require(input.read() == -1) { "元数据尾部无效" }
            return PageMetadataSnapshot(docProperties, journalDates, templates)
        }
    }
}
