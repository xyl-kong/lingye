package com.example.tabletworkspace

/**
 * Single-selection index per canvas toolbar category. Computing them in one place keeps
 * the toolbars, the bottom bar and the frame/presentation controls from re-deriving the
 * same "selected shape is of type X" checks.
 */
internal data class CanvasSelectionTargets(
    val card: Int?,
    val shape: Int?,
    val frame: Int?,
    val pageLink: Int?,
    val mindNode: Int?,
    val text: Int?,
    val image: Int?,
    val stroke: Int?,
    val connector: Int?
) {
    companion object {
        fun of(page: Page, selection: CanvasSelection?, selectedCount: Int): CanvasSelectionTargets {
            fun shapeOf(predicate: (Shape) -> Boolean): Int? =
                selection?.takeIf { it.type == "shape" && selectedCount == 1 }?.index
                    ?.takeIf { index -> page.shapes.getOrNull(index)?.let(predicate) == true }
            return CanvasSelectionTargets(
                card = shapeOf { it.type == "card" },
                shape = shapeOf { it.type in setOf("rect", "roundRect", "ellipse") },
                frame = shapeOf { it.type == "frame" },
                pageLink = shapeOf { it.type == PageLinks.SHAPE_TYPE },
                mindNode = shapeOf { it.type == Mindmap.NODE_TYPE },
                text = shapeOf { it.type == "text" },
                image = selection?.takeIf { it.type == "image" && selectedCount == 1 }?.index
                    ?.takeIf { it in page.images.indices },
                stroke = selection?.takeIf { it.type == "stroke" && selectedCount == 1 }?.index
                    ?.takeIf { it in page.strokes.indices },
                connector = selection?.takeIf { it.type == "connector" && selectedCount == 1 }?.index
                    ?.takeIf { it in page.connectors.indices }
            )
        }
    }
}
