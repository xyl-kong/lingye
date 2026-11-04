package com.example.tabletworkspace
import com.example.tabletworkspace.core.PageSearchIndex
import com.example.tabletworkspace.core.filterPagesIndexed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageSearchIndexTest {

    private fun page(title: String, text: String = "", block: String? = null, card: Pair<String, String>? = null): Page {
        val page = Page.create(title)
        page.text = text
        block?.let { page.addBlock("paragraph", it) }
        card?.let { (titleText, body) ->
            page.shapes.add(Shape("card", 0f, 0f, 100f, 100f).copy(title = titleText, body = body))
        }
        return page
    }

    @Test
    fun matchesTitleTextBlocksCardsAndSharedContent() {
        val shared = SharedBlock(id = "shared-1", text = "shared keyword", kind = "paragraph")
        val pages = listOf(
            page("Alpha Title"),
            page("Plain", text = "Body Match"),
            page("Blocks", block = "Block Match"),
            page("Cards", card = "CardTitle" to "CardBody"),
            (Page.create("Linked").also { it.addBlock("shared", "shared-1") })
        )
        val index = PageSearchIndex()
        index.prepare(pages, listOf(shared), 1)

        assertTrue(index.contains(pages[0].id, "alpha title"))
        assertTrue(index.contains(pages[1].id, "body match"))
        assertTrue(index.contains(pages[2].id, "block match"))
        assertTrue(index.contains(pages[3].id, "cardtitle"))
        assertTrue(index.contains(pages[3].id, "cardbody"))
        assertTrue(index.contains(pages[4].id, "shared keyword"))
        assertFalse(index.contains(pages[0].id, "missing"))
    }

    @Test
    fun reusesIndexUntilRevisionOrSharedSetChanges() {
        val pages = listOf(page("One"))
        val index = PageSearchIndex()
        index.prepare(pages, emptyList(), 1)
        index.prepare(pages, emptyList(), 1)
        assertEquals(1, index.buildCount)

        index.prepare(pages, emptyList(), 2)
        assertEquals(2, index.buildCount)

        index.prepare(pages, listOf(SharedBlock(id = "s", text = "x")), 2)
        assertEquals(3, index.buildCount)
    }

    @Test
    fun indexedFilterMatchesUnindexedFilter() {
        val a = page("Alpha Notes", text = "important idea")
        val b = page("Beta", text = "nothing")
        b.trashed = true
        val c = page("Gamma", block = "important task")
        c.favorite = true
        val pages = listOf(a, b, c)
        val index = PageSearchIndex()
        index.prepare(pages, emptyList(), 1)

        listOf("important", "alpha", "gamma").forEach { query ->
            assertEquals(
                filterPages(pages, query, null, false).map { it.id },
                filterPagesIndexed(pages, index, query, null, false).map { it.id }
            )
        }
        // Trash and favorites filters stay identical too.
        assertEquals(
            filterPages(pages, "important", null, true).map { it.id },
            filterPagesIndexed(pages, index, "important", null, true).map { it.id }
        )
        assertEquals(
            filterPages(pages, "", null, false, favoritesOnly = true).map { it.id },
            filterPagesIndexed(pages, index, "", null, false, favoritesOnly = true).map { it.id }
        )
    }

    @Test
    fun blankQueryDoesNotNeedTheIndex() {
        val pages = listOf(page("Anything"))
        val index = PageSearchIndex()
        assertEquals(pages.map { it.id }, filterPagesIndexed(pages, index, "  ", null, false).map { it.id })
        assertEquals(0, index.buildCount)
    }
}
