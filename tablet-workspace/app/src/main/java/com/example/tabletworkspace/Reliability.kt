package com.example.tabletworkspace
class PageGesture(private val page: Page) {
 private var original: ByteArray? = page.encode()
 fun cancel() { val data=original ?: return; val old=Page.decode(data); require(old.id==page.id)
  page.title=old.title; page.text=old.text; page.cardX=old.cardX; page.cardY=old.cardY; page.trashed=old.trashed
  page.folders.clear(); page.folders.addAll(old.folders); page.tags.clear(); page.tags.addAll(old.tags); page.favorite=old.favorite
  page.strokes.clear(); page.strokes.addAll(old.strokes); page.strokeStyles.clear(); page.strokeStyles.addAll(old.strokeStyles)
  page.strokeIds.clear(); page.strokeIds.addAll(old.strokeIds)
  page.layerIds.clear(); page.layerIds.addAll(old.orderedLayerIds())
  page.blocks.clear(); page.blocks.addAll(old.blocks); page.shapes.clear(); page.shapes.addAll(old.shapes); page.images.clear(); page.images.addAll(old.images); page.connectors.clear(); page.connectors.addAll(old.connectors); page.cardTemplates.clear(); page.cardTemplates.addAll(old.cardTemplates); original=null
 }
 fun commit() { original=null }
}
data class PageLoadResult(val pages: List<Page>, val failed: List<String>)
fun pageFileNames(names: List<String>): List<String> = names.mapNotNull { name ->
    when {
        name.endsWith(".page.bak") -> name.removeSuffix(".bak")
        name.endsWith(".page") -> name
        else -> null
    }
}.distinct().sorted()

fun loadPageEntries(names: List<String>, read: (String) -> ByteArray): PageLoadResult {
 val pages=mutableListOf<Page>(); val failed=mutableListOf<String>()
 names.forEach { name -> try { pages.add(Page.decode(read(name))) } catch (_: Exception) { failed.add(name) } }
 return PageLoadResult(pages,failed)
}
