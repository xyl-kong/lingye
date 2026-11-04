package com.example.tabletworkspace.core

import com.example.tabletworkspace.Connector
import com.example.tabletworkspace.Point
import com.example.tabletworkspace.connectorSamples

/**
 * Bounded LRU cache for [connectorSamples]. A connector is a data class, so the key covers
 * endpoints, ports and path style; while a canvas is static, each connector is sampled
 * once instead of 41 curve points per frame. Dragging or re-binding a connector produces
 * a new key, so stale geometry is never served.
 */
internal class ConnectorSampleCache(private val maxEntries: Int = 256) {
    private val entries = object : LinkedHashMap<Connector, List<Point>>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Connector, List<Point>>): Boolean =
            size > maxEntries
    }

    var hits: Int = 0
        private set
    var misses: Int = 0
        private set

    fun samples(connector: Connector): List<Point> {
        entries[connector]?.let {
            hits++
            return it
        }
        misses++
        return connectorSamples(connector).also { entries[connector] = it }
    }

    fun clear() {
        entries.clear()
        hits = 0
        misses = 0
    }

    val size: Int
        get() = entries.size

    companion object {
        val shared = ConnectorSampleCache()
    }
}
