package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PageLinksTest {
    @Test
    fun linkedDocStoresStableTargetIdAndSurvivesReopen() {
        val source = Page.create("Source")
        val target = Page.create("Target")

        source.addLinkedDoc(target, 10f, 20f)
        val restored = Page.decode(source.encode())
        val link = restored.shapes.single()

        assertEquals(PageLinks.SHAPE_TYPE, link.type)
        assertEquals(target.id, link.body)
        assertEquals(target.id, PageLinks.targetId(link))
        assertEquals("Target", PageLinks.resolve(link, listOf(source, target)).title)
        assertFalse(PageLinks.resolve(link, listOf(source, target)).missing)
    }

    @Test
    fun renamedTargetShowsLiveTitleWithoutRewritingTheLink() {
        val source = Page.create("Source")
        val target = Page.create("Old title")
        source.addLinkedDoc(target, 10f, 20f)
        target.title = "New title"

        val resolved = PageLinks.resolve(source.shapes.single(), listOf(source, target))

        assertEquals("New title", resolved.title)
        assertFalse(resolved.missing)
        assertEquals("Old title", source.shapes.single().title)
    }

    @Test
    fun deletedOrTrashedTargetIsReportedAsMissing() {
        val source = Page.create("Source")
        val target = Page.create("Target")
        source.addLinkedDoc(target, 10f, 20f)
        val link = source.shapes.single()

        assertTrue(PageLinks.resolve(link, listOf(source)).missing)
        target.trashed = true
        assertTrue(PageLinks.resolve(link, listOf(source, target)).missing)
        assertEquals("Target", PageLinks.resolve(link, listOf(source)).title)
    }

    @Test
    fun blankOrMalformedTargetBodyIsMissing() {
        val page = Page.create("Source")
        page.shapes.add(Shape(PageLinks.SHAPE_TYPE, 0f, 0f, 200f, 100f, body = ""))
        page.shapes.add(Shape(PageLinks.SHAPE_TYPE, 0f, 0f, 200f, 100f, body = "not-a-uuid"))

        page.shapes.forEach { link ->
            assertNull(PageLinks.targetId(link))
            assertTrue(PageLinks.resolve(link, listOf(page)).missing)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun pageCannotLinkToItself() {
        val page = Page.create("Source")
        page.addLinkedDoc(page, 0f, 0f)
    }

    @Test
    fun onlyCanvasContentOpensAsWhiteboard() {
        val empty = Page.create("Empty")
        assertTrue(PageLinks.opensAsWhiteboard(empty, true))
        assertFalse(PageLinks.opensAsWhiteboard(empty, false))

        val doc = Page.create("Doc").apply { addBlock("paragraph", "text") }
        assertFalse(PageLinks.opensAsWhiteboard(doc, true))

        val board = Page.create("Board").apply { addShape("rect", 0f, 0f, 10f, 10f) }
        assertTrue(PageLinks.opensAsWhiteboard(board, false))
    }

    @Test
    fun backlinksFindLiveSourcePagesOnly() {
        val target = Page.create("Target")
        val sourceA = Page.create("A").apply { addLinkedDoc(target, 0f, 0f) }
        val sourceB = Page.create("B").apply { addLinkedDoc(target, 0f, 0f) }
        val trashed = Page.create("Trashed").apply { addLinkedDoc(target, 0f, 0f); trashed = true }
        val unrelated = Page.create("Unrelated")

        val backlinks = PageLinks.backlinks(target, listOf(target, sourceA, sourceB, trashed, unrelated))

        assertEquals(setOf(sourceA.id, sourceB.id), backlinks.map { it.id }.toSet())
        assertTrue(PageLinks.backlinks(target, listOf(target)).isEmpty())
    }

    @Test
    fun duplicatingALinkKeepsTheSameTarget() {
        val source = Page.create("Source")
        val target = Page.create("Target")
        val index = source.addLinkedDoc(target, 10f, 20f)

        val created = source.duplicateSelections(listOf(CanvasSelection("shape", index)))

        val copy = source.shapes[created.single().index]
        assertEquals(target.id, copy.body)
        assertEquals(PageLinks.SHAPE_TYPE, copy.type)
    }
}
