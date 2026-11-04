package com.example.tabletworkspace
import com.example.tabletworkspace.core.ConnectorSampleCache

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectorSampleCacheTest {

    private fun connector(): Connector {
        val page = Page.create("connectors")
        page.addConnector(0f, 0f, 100f, 40f, pathStyle = "curve")
        return page.connectors[0]
    }

    @Test
    fun reusesSamplesForIdenticalGeometry() {
        val cache = ConnectorSampleCache(maxEntries = 8)
        val c = connector()
        val first = cache.samples(c)
        val second = cache.samples(c)
        assertSame(first, second)
        assertEquals(41, first.size)
        assertEquals(1, cache.hits)
        assertEquals(1, cache.misses)
    }

    @Test
    fun changedGeometryIsNotReused() {
        val cache = ConnectorSampleCache(maxEntries = 8)
        val c = connector()
        cache.samples(c)
        val moved = c.copy(x2 = c.x2 + 25f)
        val movedSamples = cache.samples(moved)
        assertNotSame(cache.samples(c), movedSamples)
        assertEquals(2, cache.misses)
        assertEquals(1, cache.hits)
        assertEquals(41, movedSamples.size)
    }

    @Test
    fun evictsLeastRecentlyUsedEntry() {
        val cache = ConnectorSampleCache(maxEntries = 2)
        val a = connector()
        val b = a.copy(x2 = a.x2 + 10f)
        val c = a.copy(x2 = a.x2 + 20f)
        cache.samples(a)
        cache.samples(b)
        cache.samples(a)
        cache.samples(c)
        val missesBefore = cache.misses
        cache.samples(b)
        assertTrue(cache.misses > missesBefore)
        assertEquals(2, cache.size)
    }
}
