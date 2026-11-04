package com.example.tabletworkspace

import java.nio.ByteBuffer
import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectorMarkerTest {
    @Test fun newConnectorsUseArrowMarkerByDefault() {
        val page = Page.create("p")
        page.addConnector(0f, 0f, 100f, 0f)
        assertEquals("arrow", page.connectors.single().endMarker)
    }

    @Test fun markerRoundTripsAndRejectsUnknownValues() {
        val page = Page.create("p")
        page.addConnector(0f, 0f, 100f, 0f)
        page.connectors[0] = page.connectors[0].copy(endMarker = "arrow")
        assertEquals("arrow", Page.decode(page.encode()).connectors.single().endMarker)
        page.connectors[0] = page.connectors[0].copy(endMarker = "hexagon")
        try {
            page.encode()
            throw AssertionError("unknown marker accepted")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test fun legacyPageDefaultsToDotMarker() {
        val page = Page.create("p")
        page.addConnector(0f, 0f, 100f, 0f)
        page.connectors[0] = page.connectors[0].copy(endMarker = "arrow")
        val legacy = page.encode().also { ByteBuffer.wrap(it).putInt(0, 9) }
        assertEquals("dot", Page.decode(legacy).connectors.single().endMarker)
    }

    @Test fun startAndEndMarkersRoundTripIndependently() {
        val page = Page.create("p")
        page.addConnector(0f, 0f, 100f, 0f, startMarker = "arrow")
        page.connectors[0] = page.connectors[0].copy(startMarker = "arrow", endMarker = "dot")

        val restored = Page.decode(page.encode()).connectors.single()

        assertEquals("arrow", restored.startMarker)
        assertEquals("dot", restored.endMarker)
    }

    @Test fun allSupportedEndpointShapesRoundTrip() {
        val page = Page.create("p")
        page.addConnector(0f, 0f, 100f, 0f)
        for (marker in listOf("dot", "arrow", "triangle", "diamond", "none")) {
            page.connectors[0] = page.connectors[0].copy(startMarker = marker, endMarker = marker)
            val restored = Page.decode(page.encode()).connectors.single()
            assertEquals(marker, restored.startMarker)
            assertEquals(marker, restored.endMarker)
        }
    }
}
