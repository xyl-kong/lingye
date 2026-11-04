package com.example.tabletworkspace
import androidx.compose.runtime.*
class StableSelectionState(private val page: Page) {
 private var ids by mutableStateOf<List<String>>(emptyList())
 fun get():List<CanvasSelection> = ids.mapNotNull(page::findStableObject)
 fun set(items:List<CanvasSelection>) { ids=items.mapNotNull { item -> when(item.type) { "shape" -> page.shapes.getOrNull(item.index)?.id; "image" -> page.images.getOrNull(item.index)?.id; "connector" -> page.connectors.getOrNull(item.index)?.id; "stroke" -> page.strokeIds.getOrNull(item.index); else -> null } }.distinct() }
}
class StableSelectionDelegate(page:Page):MutableState<CanvasSelection?> {
 private val state=StableSelectionState(page)
 override var value:CanvasSelection? get()=state.get().firstOrNull(); set(v) {state.set(listOfNotNull(v))}
 override fun component1()=value
 override fun component2():(CanvasSelection?)->Unit = { value=it }
}
class StableSelectionListDelegate(page:Page):MutableState<List<CanvasSelection>> {
 private val state=StableSelectionState(page)
 override var value:List<CanvasSelection> get()=state.get(); set(v) {state.set(v)}
 override fun component1()=value
 override fun component2():(List<CanvasSelection>)->Unit = { value=it }
}
