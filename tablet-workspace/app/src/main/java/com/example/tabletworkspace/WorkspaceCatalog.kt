package com.example.tabletworkspace

import java.io.*
import java.util.Locale

fun filterPages(
    pages: List<Page>,
    query: String,
    folder: String?,
    trash: Boolean,
    sharedBlocks: List<SharedBlock> = emptyList(),
    tag: String? = null,
    favoritesOnly: Boolean = false
): List<Page> {
    val needle = query.trim().lowercase(Locale.ROOT)
    return pages.filter { page ->
        page.trashed == trash &&
        (!favoritesOnly || page.favorite) &&
        (folder == null || if (folder.isEmpty()) page.folders.isEmpty() else folder in page.folders) &&
        (tag == null || tag in page.tags) &&
        (needle.isEmpty() || page.title.lowercase(Locale.ROOT).contains(needle) || page.text.lowercase(Locale.ROOT).contains(needle) || page.blocks.any { block -> when(block.type) { "image"->false;"shared"->sharedBlocks.firstOrNull { it.id==block.content }?.searchText()?.lowercase(Locale.ROOT)?.contains(needle)==true;else->block.content.lowercase(Locale.ROOT).contains(needle) } } || page.shapes.any { it.type == "card" && (it.title.lowercase(Locale.ROOT).contains(needle) || it.body.lowercase(Locale.ROOT).contains(needle)) })
    }
}
object TagCatalog {
    fun valid(name: String): Boolean =
        name.isNotBlank() && name == name.trim() && name.length <= 40 && name.none { it.isISOControl() }
}
object FolderCatalog {
    fun valid(name: String): Boolean = name.isNotBlank() && name == name.trim() && name.length <= 100 && name.none { it.isISOControl() }
    fun encode(folders: List<String>): ByteArray {
        require(folders.size <= 1000 && folders.all(::valid) && folders.map { it.lowercase(Locale.ROOT) }.distinct().size == folders.size)
        val bytes = ByteArrayOutputStream(); DataOutputStream(bytes).use { out -> out.writeInt(1); out.writeInt(folders.size); folders.forEach { out.writeUTF(it) } }; return bytes.toByteArray()
    }
    fun decode(data: ByteArray): List<String> = DataInputStream(ByteArrayInputStream(data)).use { input ->
        require(input.readInt() == 1); val count = input.readInt(); require(count in 0..1000); val names = List(count) { input.readUTF() }; require(names.all(::valid) && names.map { it.lowercase(Locale.ROOT) }.distinct().size == names.size); require(input.available() == 0); names
    }
}
