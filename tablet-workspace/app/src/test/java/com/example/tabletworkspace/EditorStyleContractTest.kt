package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Test

class EditorStyleContractTest {
    @Test
    fun everyShadowStyleCanBeSavedAndReloaded() {
        val page = Page.create("shadows")
        EditorStyleCatalog.shadowStyles.forEachIndexed { index, style ->
            page.shapes.add(Shape("text", index * 10f, 0f, 80f, 40f, shadowStyle = style))
        }

        val restored = Page.decode(page.encode())
        assertEquals(EditorStyleCatalog.shadowStyles, restored.shapes.map { it.shadowStyle }.toSet())
    }

    @Test
    fun everyConnectorMarkerAndPathCanBeSavedAndReloaded() {
        val page = Page.create("connectors")
        var index = 0
        EditorStyleCatalog.connectorMarkers.forEach { start ->
            EditorStyleCatalog.connectorMarkers.forEach { end ->
                EditorStyleCatalog.connectorPathStyles.forEach { path ->
                    page.addConnector(
                        index * 10f, 0f, index * 10f + 80f, 40f,
                        pathStyle = path, endMarker = end, startMarker = start
                    )
                    index++
                }
            }
        }

        val restored = Page.decode(page.encode())
        assertEquals(index, restored.connectors.size)
        assertEquals(
            EditorStyleCatalog.connectorMarkers,
            restored.connectors.map { it.startMarker }.toSet()
        )
        assertEquals(
            EditorStyleCatalog.connectorPathStyles,
            restored.connectors.map { it.pathStyle }.toSet()
        )
    }

    @Test
    fun everyShapeStyleCanBeSavedAndReloaded() {
        val page = Page.create("shape styles")
        var index = 0
        EditorStyleCatalog.borderStyles.forEach { border ->
            EditorStyleCatalog.textAligns.forEach { align ->
                EditorStyleCatalog.fontFamilies.forEach { family ->
                    EditorStyleCatalog.fontStyles.forEach { style ->
                        EditorStyleCatalog.fontWeights.forEach { weight ->
                            page.shapes.add(
                                Shape(
                                    "rect", index * 10f, 0f, 60f, 40f,
                                    borderStyle = border,
                                    textAlign = align,
                                    fontFamily = family,
                                    fontStyle = style,
                                    fontWeight = weight
                                )
                            )
                            index++
                        }
                    }
                }
            }
        }

        val restored = Page.decode(page.encode())
        assertEquals(index, restored.shapes.size)
        assertEquals(EditorStyleCatalog.borderStyles, restored.shapes.map { it.borderStyle }.toSet())
        assertEquals(EditorStyleCatalog.textAligns, restored.shapes.map { it.textAlign }.toSet())
        assertEquals(EditorStyleCatalog.fontFamilies, restored.shapes.map { it.fontFamily }.toSet())
        assertEquals(EditorStyleCatalog.fontStyles, restored.shapes.map { it.fontStyle }.toSet())
        assertEquals(EditorStyleCatalog.fontWeights, restored.shapes.map { it.fontWeight }.toSet())
    }

    @Test
    fun shapeDefaultsAreAppliedAndRoundTrip() {
        val page = Page.create("shape defaults")
        page.addShape(
            "rect", 10f, 20f, 120f, 80f,
            color = 0xff112233.toInt(),
            borderColor = 0xff445566.toInt(),
            borderStyle = "dashed",
            borderWidth = 6f,
            textColor = 0xff778899.toInt(),
            fontSize = 24f,
            textAlign = "center",
            fontFamily = "serif",
            fontStyle = "italic",
            fontWeight = 700
        )

        val shape = Page.decode(page.encode()).shapes.single()
        assertEquals(0xff445566.toInt(), shape.borderColor)
        assertEquals("dashed", shape.borderStyle)
        assertEquals(6f, shape.borderWidth)
        assertEquals(0xff778899.toInt(), shape.textColor)
        assertEquals(24f, shape.fontSize)
        assertEquals("center", shape.textAlign)
        assertEquals("serif", shape.fontFamily)
        assertEquals("italic", shape.fontStyle)
        assertEquals(700, shape.fontWeight)
    }

    @Test
    fun textBoxFontDefaultsRoundTrip() {
        val page = Page.create("text defaults")
        page.addTextBox(
            10f, 20f, "hello",
            textColor = 0xff222222.toInt(),
            fontSize = 22f,
            textAlign = "right",
            fontFamily = "mono",
            fontStyle = "italic",
            fontWeight = 700
        )

        val shape = Page.decode(page.encode()).shapes.single()
        assertEquals("mono", shape.fontFamily)
        assertEquals("italic", shape.fontStyle)
        assertEquals(700, shape.fontWeight)
        assertEquals("right", shape.textAlign)
        assertEquals(22f, shape.fontSize)
    }

    @Test
    fun workspaceDefaultsNormalizeAgainstCatalog() {
        val settings = WorkspaceSettings(
            shapeBorderStyle = "double",
            shapeBorderWidth = 99f,
            shapeTextSize = 1f,
            shapeTextAlign = "justify",
            shapeFontFamily = "comic",
            shapeFontStyle = "oblique",
            shapeFontWeight = 500,
            defaultTextFontFamily = "comic",
            defaultTextFontStyle = "oblique",
            defaultTextFontWeight = 500,
            connectorStartMarker = "square"
        ).normalized()

        assertEquals("solid", settings.shapeBorderStyle)
        assertEquals(12f, settings.shapeBorderWidth)
        assertEquals(8f, settings.shapeTextSize)
        assertEquals("left", settings.shapeTextAlign)
        assertEquals("sans", settings.shapeFontFamily)
        assertEquals("normal", settings.shapeFontStyle)
        assertEquals(400, settings.shapeFontWeight)
        assertEquals("sans", settings.defaultTextFontFamily)
        assertEquals("normal", settings.defaultTextFontStyle)
        assertEquals(400, settings.defaultTextFontWeight)
        assertEquals("none", settings.connectorStartMarker)
    }
}
