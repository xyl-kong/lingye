package com.example.tabletworkspace

import java.nio.ByteBuffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectorStyleTest {
    @Test fun pathStyleRoundTripsAndRejectsUnknownValues() {
        val page = Page.create("p")
        page.addConnector(0f, 0f, 100f, 80f)
        page.connectors[0] = page.connectors[0].copy(pathStyle = "step")
        assertEquals("step", Page.decode(page.encode()).connectors.single().pathStyle)
        page.connectors[0] = page.connectors[0].copy(pathStyle = "spiral")
        try {
            page.encode()
            throw AssertionError("unknown path style accepted")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test fun pendingStyleMigrationKeepsCurvedFlag() {
        val page = Page.create("p")
        page.addConnector(0f, 0f, 100f, 80f)
        val legacy = page.encode().also { ByteBuffer.wrap(it).putInt(0, 10) }
        assertEquals("curve", Page.decode(legacy).connectors.single().pathStyle)
    }

    @Test fun stepConnectorUsesOrthogonalMiddleRoute() {
        val connector = Connector(0f, 0f, 100f, 80f, curved = true, pathStyle = "step")
        val samples = connectorSamples(connector)
        assertEquals(0f, samples.first().y, 0.001f)
        assertEquals(80f, samples.last().y, 0.001f)
        assertTrue(samples.any { it.x in 49f..51f && it.y in 1f..79f })
    }

    @Test fun reverseConnectorSwapsPortsAndDirectionalMarker() {
        val page = Page.create("p")
        page.addShape("card", 0f, 0f, 100f, 80f)
        page.addShape("card", 300f, 0f, 100f, 80f)
        page.addConnector(100f, 40f, 300f, 40f)
        page.connectors[0] = page.connectors[0].copy(
            startShape = page.shapes[0].id,
            startPort = "right",
            endShape = page.shapes[1].id,
            endPort = "left",
            startMarker = "dot",
            endMarker = "arrow"
        )
        page.reverseConnector(0)
        val reversed = page.connectors[0]
        assertEquals(page.shapes[1].id, reversed.startShape)
        assertEquals("left", reversed.startPort)
        assertEquals(page.shapes[0].id, reversed.endShape)
        assertEquals("right", reversed.endPort)
        assertEquals("arrow", reversed.startMarker)
        assertEquals("dot", reversed.endMarker)
    }

    @Test fun connectorLabelSurvivesRoundTrip() {
        val page = Page.create("p")
        page.addConnector(0f, 0f, 100f, 80f)
        page.connectors[0] = page.connectors[0].copy(label = "depends on")

        assertEquals("depends on", Page.decode(page.encode()).connectors.single().label)
    }
}
