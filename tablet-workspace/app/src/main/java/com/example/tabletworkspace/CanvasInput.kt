package com.example.tabletworkspace

import androidx.compose.ui.geometry.Offset

internal fun handleCanvasDoubleTap(
    tap: Offset,
    page: Page,
    scale: Float,
    worldPoint: Point,
    onOpenLinkedPage: (String) -> Unit,
    onSelect: (CanvasSelection?, List<CanvasSelection>) -> Unit,
    onEditCard: (Int) -> Unit
) {
    val hit = page.hitTest(worldPoint, 12f / scale)
    val shape = hit?.takeIf { it.type == "shape" }?.let { page.shapes.getOrNull(it.index) }
    when {
        shape?.type == PageLinks.SHAPE_TYPE -> PageLinks.targetId(shape)?.let(onOpenLinkedPage)
        shape?.type in setOf("card", "text", Mindmap.NODE_TYPE) && hit != null -> {
            onSelect(hit, listOf(hit))
            onEditCard(hit.index)
        }
    }
}

internal fun handleCanvasTap(
    tap: Offset,
    page: Page,
    scale: Float,
    density: Float,
    viewport: Viewport,
    worldPoint: Point,
    selection: CanvasSelection?,
    selectedItems: List<CanvasSelection>,
    multiSelect: Boolean,
    onFocusCanvas: () -> Unit,
    onCardEditChange: (Int?) -> Unit,
    onOpenTextLink: (String) -> Unit,
    onSelect: (CanvasSelection?, List<CanvasSelection>) -> Unit,
    onChanged: () -> Unit
) {
    onFocusCanvas()
    onCardEditChange(null)
    val tappedShape = page.hitTest(worldPoint, 12f / scale)?.takeIf { it.type == "shape" }?.let { page.shapes.getOrNull(it.index) }
    val tappedLink = tappedShape?.takeIf { it.type == "text" }?.let { shape ->
        TextLinkHitTest.linkAt(shape, worldPoint.x - shape.x, worldPoint.y - shape.y)
    }
    if (tappedLink != null) {
        onOpenTextLink(tappedLink.url)
        return
    }
    if (tappedShape?.type in setOf("card", "text", Mindmap.NODE_TYPE)) {
        val hit = page.hitTest(worldPoint, 12f / scale)
        if (hit != null && hit == selection) {
            onSelect(hit, listOf(hit))
            onCardEditChange(hit.index)
            return
        }
    }
    val unit = density * scale
    val todo = page.cardTodoAtScreen(
        tap.x,
        tap.y,
        { i -> viewport.toScreen(Point(page.shapes[i].x, page.shapes[i].y)).x * density },
        { i -> viewport.toScreen(Point(page.shapes[i].x, page.shapes[i].y)).y * density },
        unit
    )
    if (todo != null && page.toggleCardTodo(todo.shapeIndex, todo.lineIndex)) {
        val target = CanvasSelection("shape", todo.shapeIndex)
        onSelect(target, listOf(target))
        onChanged()
        return
    }
    val hit = page.hitTest(worldPoint, 12f / scale)
    if (multiSelect && hit != null) {
        val items = if (hit in selectedItems) selectedItems - hit else (selectedItems + page.selectionMembers(hit)).distinct()
        onSelect(items.lastOrNull(), items)
    } else {
        onSelect(hit, hit?.let { page.selectionMembers(it) } ?: emptyList())
    }
}
