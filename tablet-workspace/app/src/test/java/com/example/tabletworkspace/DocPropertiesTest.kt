package com.example.tabletworkspace

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DocPropertiesTest {
    @Test
    fun defaultsMatchOfficialOrder() {
        val defaults = DocPropertyCatalog.defaults(AppLanguage.ZH)
        assertEquals(listOf("tags", "docPrimaryMode", "journal", "template", "createdAt", "updatedAt", "createdBy", "edgelessTheme", "pageWidth"), defaults.map { it.type })
        assertEquals(DocPropertyCatalog.ALWAYS_SHOW, defaults.first { it.type == "tags" }.show)
        assertEquals(DocPropertyCatalog.ALWAYS_HIDE, defaults.first { it.type == "docPrimaryMode" }.show)
    }

    @Test
    fun encodeDecodeRoundTrips() {
        val properties = DocPropertyCatalog.defaults(AppLanguage.ZH) + DocPropertyInfo(
            "custom-1", "text", "备注", DocPropertyCatalog.HIDE_WHEN_EMPTY, 9, icon = "star", value = "已填写的内容"
        )
        val decoded = DocPropertyStore.decode(DocPropertyStore.encode(properties))
        assertEquals(properties, decoded)
    }

    @Test
    fun legacyFiveFieldRecordStillDecodes() {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out ->
            out.writeInt(1)
            out.writeUTF("builtin-tags")
            out.writeUTF("tags")
            out.writeUTF("标签")
            out.writeUTF(DocPropertyCatalog.ALWAYS_SHOW)
            out.writeInt(0)
        }
        val decoded = DocPropertyStore.decode(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.toByteArray()))
        assertEquals(1, decoded.size)
        assertEquals("", decoded.single().icon)
        assertEquals("", decoded.single().value)
    }

    @Test
    fun decodeNormalizesUnknownVisibility() {
        val encoded = DocPropertyStore.encode(listOf(DocPropertyInfo("a", "text", "A", "sometimes", 0)))
        assertEquals(DocPropertyCatalog.ALWAYS_SHOW, DocPropertyStore.decode(encoded).single().show)
    }

    @Test
    fun duplicateNamesGetNumericSuffix() {
        val existing = listOf(DocPropertyInfo("a", "text", "文本", order = 0), DocPropertyInfo("b", "text", "文本 2", order = 1))
        assertEquals("文本 3", DocPropertyStore.uniqueName("文本", existing))
        assertEquals("数字", DocPropertyStore.uniqueName("数字", existing))
    }

    @Test
    fun catalogHasAllOfficialTypes() {
        val types = DocPropertyCatalog.types.map { it.key }
        assertEquals(listOf("tags", "text", "number", "checkbox", "date", "createdBy", "updatedBy", "updatedAt", "createdAt", "docPrimaryMode", "journal", "edgelessTheme", "pageWidth", "template"), types)
        assertNotNull(DocPropertyCatalog.type("pageWidth"))
        assertTrue(DocPropertyCatalog.type("tags")!!.unique)
        assertTrue(DocPropertyCatalog.shows.contains(DocPropertyCatalog.HIDE_WHEN_EMPTY))
    }

    @Test
    fun valueKindsAndRenameabilityMatchOfficial() {
        assertEquals(DocPropertyCatalog.KIND_TEXT, DocPropertyCatalog.valueKind("text"))
        assertEquals(DocPropertyCatalog.KIND_NUMBER, DocPropertyCatalog.valueKind("number"))
        assertEquals(DocPropertyCatalog.KIND_CHECKBOX, DocPropertyCatalog.valueKind("checkbox"))
        assertEquals(DocPropertyCatalog.KIND_DATE, DocPropertyCatalog.valueKind("date"))
        assertEquals(DocPropertyCatalog.KIND_READONLY, DocPropertyCatalog.valueKind("createdAt"))
        assertFalse(DocPropertyCatalog.renameable("tags"))
        assertFalse(DocPropertyCatalog.renameable("createdAt"))
        assertFalse(DocPropertyCatalog.renameable("updatedAt"))
        assertTrue(DocPropertyCatalog.renameable("text"))
    }

    @Test
    fun customPropertiesExcludeBuiltins() {
        val defaults = DocPropertyCatalog.defaults(AppLanguage.ZH)
        assertFalse(defaults.any { DocPropertyCatalog.isCustom(it) })
        assertTrue(DocPropertyCatalog.isCustom(DocPropertyInfo("prop-text-x", "text", "备注")))
    }

    @Test
    fun reorderMovesFirstAfterLast() {
        val list = reorderFixture()
        val result = DocPropertyCatalog.reorder(list, "a", "c", after = true)
        assertEquals(listOf("b", "c", "a"), result.map { it.id })
        assertEquals(listOf(0, 1, 2), result.map { it.order })
    }

    @Test
    fun reorderMovesLastBeforeFirst() {
        val list = reorderFixture()
        val result = DocPropertyCatalog.reorder(list, "c", "a", after = false)
        assertEquals(listOf("c", "a", "b"), result.map { it.id })
        assertEquals(listOf(0, 1, 2), result.map { it.order })
    }

    @Test
    fun reorderMidItemAfterNext() {
        val list = reorderFixture()
        val result = DocPropertyCatalog.reorder(list, "a", "b", after = true)
        assertEquals(listOf("b", "a", "c"), result.map { it.id })
    }

    @Test
    fun reorderUnknownTargetKeepsList() {
        val list = reorderFixture()
        assertEquals(list, DocPropertyCatalog.reorder(list, "a", "missing", after = false))
        assertEquals(list, DocPropertyCatalog.reorder(list, "missing", "a", after = false))
    }

    private fun reorderFixture() = listOf(
        DocPropertyInfo("a", "text", "A", order = 0),
        DocPropertyInfo("b", "text", "B", order = 1),
        DocPropertyInfo("c", "text", "C", order = 2)
    )
}
