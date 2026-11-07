package com.example.tabletworkspace.data.metadata

import com.example.tabletworkspace.DocPropertyStore
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.UUID

/**
 * Per-page metadata that lives outside the format-25 page file.
 * Used by backup/restore so document properties, journal/template flags and
 * per-page width overrides survive.
 */
data class PageMetadataSnapshot(
    val docProperties: Map<String, String> = emptyMap(),
    val journalDates: Map<String, String> = emptyMap(),
    val templatePageIds: Set<String> = emptySet(),
    val pageWidths: Map<String, Boolean> = emptyMap()
) {
    fun isEmpty(): Boolean =
        docProperties.isEmpty() && journalDates.isEmpty() && templatePageIds.isEmpty() && pageWidths.isEmpty()
}

object PageMetadataSnapshotCodec {
    private const val MAGIC_V1 = 0x4C504D31
    private const val MAGIC_V2 = 0x4C504D32
    private const val MAX_PAGES = 100_000
    private const val MAX_STRING = 2_000_000
    private const val MAX_TOTAL = 20_000_000
    private val DATE = Regex("\\d{4}-\\d{2}-\\d{2}")

    fun encode(snapshot: PageMetadataSnapshot): ByteArray {
        snapshot.journalDates.forEach { (_, date) -> require(date.matches(DATE)) { "无效 Journal 日期" } }
        snapshot.docProperties.forEach { (_, raw) -> DocPropertyStore.decode(raw) }
        val ids = (snapshot.docProperties.keys + snapshot.journalDates.keys + snapshot.templatePageIds + snapshot.pageWidths.keys)
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
            data.writeInt(MAGIC_V2)
            data.writeInt(ids.size)
            ids.forEach { id ->
                require(runCatching { UUID.fromString(id).toString() == id }.getOrDefault(false)) { "无效页面 ID" }
                data.writeUTF(id)
                writeString(snapshot.journalDates[id].orEmpty())
                data.writeBoolean(id in snapshot.templatePageIds)
                writeString(snapshot.docProperties[id].orEmpty())
                data.writeByte(when (snapshot.pageWidths[id]) {
                    null -> WIDTH_UNSET
                    false -> WIDTH_STANDARD
                    true -> WIDTH_FULL
                })
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
            val magic = input.readInt()
            val version = when (magic) {
                MAGIC_V1 -> 1
                MAGIC_V2 -> 2
                else -> throw IllegalArgumentException("元数据格式无效")
            }
            val count = input.readInt()
            require(count in 0..MAX_PAGES) { "元数据页面数无效" }
            val docProperties = mutableMapOf<String, String>()
            val journalDates = mutableMapOf<String, String>()
            val templates = mutableSetOf<String>()
            val pageWidths = mutableMapOf<String, Boolean>()
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
                if (version >= 2) {
                    when (val width = input.readUnsignedByte()) {
                        WIDTH_UNSET -> Unit
                        WIDTH_STANDARD -> pageWidths[id] = false
                        WIDTH_FULL -> pageWidths[id] = true
                        else -> throw IllegalArgumentException("页面宽度标记无效")
                    }
                }
            }
            require(input.read() == -1) { "元数据尾部无效" }
            return PageMetadataSnapshot(docProperties, journalDates, templates, pageWidths)
        }
    }

    private const val WIDTH_UNSET = 0
    private const val WIDTH_STANDARD = 1
    private const val WIDTH_FULL = 2

    /** Encodes the previous format; used by compatibility tests. */
    internal fun encodeLegacyV1(snapshot: PageMetadataSnapshot): ByteArray {
        snapshot.journalDates.forEach { (_, date) -> require(date.matches(DATE)) { "无效 Journal 日期" } }
        val ids = (snapshot.docProperties.keys + snapshot.journalDates.keys + snapshot.templatePageIds)
            .distinct().sorted()
        val out = ByteArrayOutputStream()
        DataOutputStream(out).use { data ->
            data.writeInt(MAGIC_V1)
            data.writeInt(ids.size)
            ids.forEach { id ->
                require(runCatching { UUID.fromString(id).toString() == id }.getOrDefault(false)) { "无效页面 ID" }
                data.writeUTF(id)
                val journal = snapshot.journalDates[id].orEmpty().toByteArray(Charsets.UTF_8)
                data.writeInt(journal.size)
                data.write(journal)
                data.writeBoolean(id in snapshot.templatePageIds)
                val raw = snapshot.docProperties[id].orEmpty().toByteArray(Charsets.UTF_8)
                data.writeInt(raw.size)
                data.write(raw)
            }
        }
        return out.toByteArray()
    }
}
