package com.example.tabletworkspace

/**
 * Single source of truth for values that can be written into a page.
 *
 * UI panels, settings normalization, page validation and tests refer to these
 * sets so a visible option can never drift away from the persisted model.
 */
object EditorStyleCatalog {
    val shadowStyles = linkedSetOf("none", "box", "sticker", "paper", "float", "film")
    val connectorMarkers = setOf("dot", "arrow", "triangle", "diamond", "none")
    val connectorPathStyles = setOf("straight", "curve", "step")
    val borderStyles = setOf("solid", "dashed", "none")
    val textAligns = setOf("left", "center", "right")
    val fontFamilies = setOf("sans", "serif", "mono")
    val fontStyles = setOf("normal", "italic")
    val fontWeights = setOf(400, 700)
}
