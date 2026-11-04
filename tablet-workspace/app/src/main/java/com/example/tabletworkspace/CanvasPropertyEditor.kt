package com.example.tabletworkspace

/**
 * Single write path for object properties changed from a context toolbar.
 *
 * It always reads the current object, so a menu that was opened before a concurrent change
 * (undo, another toolbar, programmatic edit) does not write a stale copy over newer fields.
 * Every successful write calls the page's change callback exactly once.
 */
object CanvasPropertyEditor {

    fun updateShape(
        page: Page,
        index: Int,
        changed: () -> Unit,
        transform: (Shape) -> Shape
    ): Boolean {
        val current = page.shapes.getOrNull(index) ?: return false
        if (current.locked) return false
        page.shapes[index] = transform(current)
        changed()
        return true
    }

    fun updateImage(
        page: Page,
        index: Int,
        changed: () -> Unit,
        transform: (CanvasImage) -> CanvasImage
    ): Boolean {
        val current = page.images.getOrNull(index) ?: return false
        page.images[index] = transform(current)
        changed()
        return true
    }

    fun updateConnector(
        page: Page,
        index: Int,
        changed: () -> Unit,
        transform: (Connector) -> Connector
    ): Boolean {
        val current = page.connectors.getOrNull(index) ?: return false
        page.connectors[index] = transform(current)
        changed()
        return true
    }

    fun updateStrokeStyle(
        page: Page,
        index: Int,
        changed: () -> Unit,
        transform: (InkStyle) -> InkStyle
    ): Boolean {
        val current = page.strokeStyles.getOrNull(index) ?: return false
        page.strokeStyles[index] = transform(current)
        changed()
        return true
    }
}
