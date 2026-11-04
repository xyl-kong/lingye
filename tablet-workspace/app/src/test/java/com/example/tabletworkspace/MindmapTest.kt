package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class MindmapTest {
    private fun map(): Pair<Page, Shape> {
        val page = Page.create("Mindmap")
        val root = Mindmap.createRoot(page, 400f, 300f, "Root", 0xffffd866.toInt(), "right")
        repeat(3) { Mindmap.addNode(page, root, "Child $it") }
        return page to root
    }

    @Test
    fun nodesAndEdgesAreCreatedWithStableMarkers() {
        val (page, root) = map()
        assertEquals(4, page.shapes.count { it.type == Mindmap.NODE_TYPE })
        assertEquals(3, page.connectors.count { Mindmap.isEdge(it) })
        assertTrue(page.connectors.filter { Mindmap.isEdge(it) }.all { it.startShape == root.id && it.endShape.isNotEmpty() })
        val mapId = Mindmap.mapIdOf(root)!!
        assertTrue(page.shapes.all { Mindmap.mapIdOf(it) == mapId && it.group.isEmpty() })
        assertEquals("right", Mindmap.layoutOf(root))
    }

    @Test
    fun parentAndChildrenTraversal() {
        val (page, root) = map()
        val children = Mindmap.children(page, root)
        assertEquals(3, children.size)
        assertEquals(root.id, Mindmap.parent(page, children.first())?.id)
        assertNull(Mindmap.parent(page, root))
        assertEquals(root.id, Mindmap.root(page, children.first()).id)
        assertTrue(Mindmap.descendants(page, root).map { it.id }.containsAll(children.map { it.id }))
    }

    @Test
    fun siblingKeepsParentAndRootSiblingBecomesChild() {
        val (page, root) = map()
        val child = Mindmap.children(page, root).first()
        val sibling = Mindmap.addSibling(page, child, "Sibling")
        assertEquals(root.id, Mindmap.parent(page, sibling)?.id)

        val rootSibling = Mindmap.addSibling(page, root, "Root child")
        assertEquals(root.id, Mindmap.parent(page, rootSibling)?.id)
    }

    @Test
    fun reparentMovesEdgeAndRejectsCycles() {
        val (page, root) = map()
        val first = Mindmap.children(page, root)[0]
        val second = Mindmap.children(page, root)[1]
        val grandchild = Mindmap.addNode(page, first, "Grandchild")

        assertTrue(Mindmap.reparent(page, second, first))
        val secondNow = page.shapes.first { it.id == second.id }
        assertEquals(first.id, Mindmap.parent(page, secondNow)?.id)
        assertEquals(1, page.connectors.count { Mindmap.isEdge(it) && it.endShape == second.id })

        assertFalse("cannot reparent under own descendant", Mindmap.reparent(page, first, grandchild))
        assertFalse("same parent is a no-op", Mindmap.reparent(page, second, first))
    }

    @Test
    fun deleteSubtreeRemovesNodesAndEdgesOnlyInBranch() {
        val (page, root) = map()
        val first = Mindmap.children(page, root)[0]
        val second = Mindmap.children(page, root)[1]
        Mindmap.addNode(page, first, "A")
        Mindmap.addNode(page, first, "B")

        val removed = Mindmap.deleteSubtree(page, first)

        assertEquals(3, removed)
        assertEquals(3, page.shapes.count { it.type == Mindmap.NODE_TYPE })
        assertTrue(page.shapes.any { it.id == second.id })
        assertEquals(2, page.connectors.count { Mindmap.isEdge(it) })
    }

    @Test
    fun rightLayoutPlacesChildrenToTheRight() {
        val (page, root) = map()
        Mindmap.reflow(page, root, "right")
        val rootNow = page.shapes.first { it.id == root.id }
        Mindmap.children(page, rootNow).forEach { child ->
            assertTrue("child x ${child.x} > root x ${rootNow.x}", child.x > rootNow.x + rootNow.width)
        }
        val ys = Mindmap.children(page, rootNow).map { it.y }
        assertEquals(ys.size, ys.distinct().size)
    }

    @Test
    fun downAndOutlineLayoutsMoveNodesAlongTheirAxes() {
        val (page, root) = map()
        Mindmap.reflow(page, root, "down")
        var rootNow = page.shapes.first { it.id == root.id }
        Mindmap.children(page, rootNow).forEach { child -> assertTrue(child.y > rootNow.y + rootNow.height) }

        Mindmap.reflow(page, root, "outline")
        rootNow = page.shapes.first { it.id == root.id }
        val firstChild = Mindmap.children(page, rootNow).first()
        assertTrue(firstChild.x > rootNow.x)
        assertTrue(firstChild.y > rootNow.y)
        assertEquals("outline", Mindmap.layoutOf(rootNow))
    }

    @Test
    fun addedNodeJoinsTheLayoutWithoutOverlappingSiblings() {
        val (page, root) = map()
        Mindmap.reflow(page, root, "right")
        Mindmap.addNode(page, root, "Fourth")
        Mindmap.reflow(page, root, "right")

        val ys = Mindmap.children(page, root).map { it.y }.sorted()
        assertEquals(4, ys.size)
        ys.zipWithNext().forEach { (a, b) -> assertTrue("sibling overlap: $a / $b", b - a >= 60f) }
    }

    @Test
    fun radialLayoutPlacesChildrenAroundTheRoot() {
        val (page, root) = map()
        Mindmap.reflow(page, root, "bubble")
        val rootNow = page.shapes.first { it.id == root.id }
        val cx = rootNow.x + rootNow.width / 2f
        val cy = rootNow.y + rootNow.height / 2f
        Mindmap.children(page, rootNow).forEach { child ->
            val d = hypot(child.x + child.width / 2f - cx, child.y + child.height / 2f - cy)
            assertTrue("radial distance $d", d in 250f..350f)
        }
    }

    @Test
    fun mindNodesAndEdgesSurviveEncodeDecode() {
        val (page, root) = map()
        Mindmap.reflow(page, root, "tree")

        val restored = Page.decode(page.encode())

        assertEquals(4, restored.shapes.count { it.type == Mindmap.NODE_TYPE })
        val restoredRoot = restored.shapes.first { it.id == root.id }
        assertEquals("tree", Mindmap.layoutOf(restoredRoot))
        assertEquals(3, restored.connectors.count { Mindmap.isEdge(it) })
        assertNotNull(restored.connectors.firstOrNull { Mindmap.isEdge(it) && it.startShape == root.id })
    }
}
