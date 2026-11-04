package com.example.tabletworkspace

import java.io.*
import java.util.UUID

data class Point(val x: Float, val y: Float)

/** The format version stored in the first four bytes of an encoded page, or null when truncated. */
fun pageFormatVersion(bytes: ByteArray): Int? {
    if (bytes.size < 4) return null
    return ((bytes[0].toInt() and 0xff) shl 24) or ((bytes[1].toInt() and 0xff) shl 16) or
        ((bytes[2].toInt() and 0xff) shl 8) or (bytes[3].toInt() and 0xff)
}

data class InkStyle(val color: Int = 0xff27374b.toInt(), val width: Float = 3f)
data class Block(val type: String, var content: String, var checked: Boolean = false, var bold: Boolean = false, var italic: Boolean = false, var underline: Boolean = false)
object HeadingBlocks {
    private val LEVELED = Regex("heading([1-6])")

    /** `heading` is the original single-level type and is equivalent to H1. */
    fun level(type: String): Int? = if (type == "heading") 1 else LEVELED.matchEntire(type)?.groupValues?.get(1)?.toInt()
    fun isHeading(type: String): Boolean = level(type) != null
}
private val CONVERTIBLE_BLOCK_TYPES = setOf("paragraph", "heading", "todo", "bulleted", "numbered", "quote", "code", "divider")
private val FORMATTABLE_BLOCK_TYPES = setOf("paragraph", "heading", "todo", "bulleted", "numbered", "quote")
private fun isConvertibleBlockType(type: String) = type in CONVERTIBLE_BLOCK_TYPES || HeadingBlocks.isHeading(type)
private fun isFormattableBlockType(type: String) = type in FORMATTABLE_BLOCK_TYPES || HeadingBlocks.isHeading(type)
data class Shape(
    val type: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val color: Int = 0xffffd866.toInt(),
    val id: String = UUID.randomUUID().toString(),
    val group: String = "",
    val title: String = "",
    val body: String = "",
    val borderColor: Int = 0xffd9deec.toInt(),
    val borderStyle: String = "solid",
    val borderWidth: Float = 1f,
    val cornerRadius: Float = 12f,
    val shadowStyle: String = "box",
    val textColor: Int = 0xff20293a.toInt(),
    val locked: Boolean = false,
    val cardKind: String = "",
    val fontSize: Float = 16f,
    val textAlign: String = "left",
    val fontFamily: String = "sans",
    val fontStyle: String = "normal",
    val fontWeight: Int = 400,
    val textSpans: List<TextSpan> = emptyList()
)
data class CardTemplate(val name: String, val title: String, val body: String, val color: Int, val width: Float = 240f, val height: Float = 150f, val canvasData: String = "")
data class Connector(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val color: Int = 0xff62748a.toInt(), val width: Float = 2f, val startShape: String = "", val endShape: String = "", val startPort: String = "center", val endPort: String = "center", val animated: Boolean = false, val curved: Boolean = false, val id: String = UUID.randomUUID().toString(), val endMarker: String = "dot", val pathStyle: String = "curve", val startMarker: String = "none", val label: String = "")
private val CONNECTOR_MARKERS = setOf("dot", "arrow", "triangle", "diamond", "none")
data class CanvasImage(val name: String, val x: Float, val y: Float, val width: Float, val height: Float, val id: String = UUID.randomUUID().toString(), val group: String = "", val rotation:Int=0, val flipX:Boolean=false, val flipY:Boolean=false, val cropLeft:Float=0f,val cropTop:Float=0f,val cropRight:Float=1f,val cropBottom:Float=1f)
data class Viewport(val scale: Float, val offsetX: Float, val offsetY: Float) {
    fun toWorld(p: Point) = Point((p.x - offsetX) / scale, (p.y - offsetY) / scale)
    fun toScreen(p: Point) = Point(p.x * scale + offsetX, p.y * scale + offsetY)
}
class Page private constructor(val id: String, var title: String, var text: String, var cardX: Float, var cardY: Float, val strokes: MutableList<List<Point>>, val blocks: MutableList<Block>, val shapes: MutableList<Shape>, val connectors: MutableList<Connector>, val images: MutableList<CanvasImage>, val strokeStyles: MutableList<InkStyle>, folder: String = "", var trashed: Boolean = false, val cardTemplates: MutableList<CardTemplate> = mutableListOf(), val folders: MutableSet<String> = mutableSetOf(), val tags: MutableSet<String> = mutableSetOf(), var favorite: Boolean = false) {
    var icon: String = ""
    var folder: String
        get() = folders.firstOrNull().orEmpty()
        set(value) {
            folders.clear()
            if (value.isNotEmpty()) folders.add(value)
        }

    init {
        if (folder.isNotEmpty() && folder !in folders) folders.add(folder)
    }

    val strokeIds: MutableList<String> = MutableList(strokes.size) { UUID.randomUUID().toString() }
    val layerIds: MutableList<String> = mutableListOf()
    fun orderedLayerIds(): List<String> { val all=shapes.map { it.id }+connectors.map { it.id }+images.map { it.id }+strokeIds; layerIds.removeAll { it !in all }; all.filterNot { it in layerIds }.forEach { layerIds.add(it) };return layerIds.toList() }
    fun snapshot(): Page = Page(
        id,
        title,
        text,
        cardX,
        cardY,
        strokes.map { it.toList() }.toMutableList(),
        blocks.map { it.copy() }.toMutableList(),
        shapes.toMutableList(),
        connectors.toMutableList(),
        images.toMutableList(),
        strokeStyles.toMutableList(),
        folder,
        trashed,
        cardTemplates.toMutableList(),
        folders.toMutableSet(),
        tags.toMutableSet(),
        favorite
    ).also {
        it.icon = icon
        it.strokeIds.clear(); it.strokeIds.addAll(strokeIds)
        it.layerIds.clear(); it.layerIds.addAll(layerIds)
    }
    fun updateText(value: String) { text = value }
    fun duplicate(newTitle: String): Page = Page(
        UUID.randomUUID().toString(),
        newTitle,
        text,
        cardX,
        cardY,
        strokes.map { it.toList() }.toMutableList(),
        blocks.map { it.copy() }.toMutableList(),
        shapes.toMutableList(),
        connectors.toMutableList(),
        images.toMutableList(),
        strokeStyles.toMutableList(),
        folder,
        false,
        cardTemplates.toMutableList(),
        folders.toMutableSet(),
        tags.toMutableSet(),
        favorite
    ).also {
        it.icon = icon
        it.strokeIds.clear(); it.strokeIds.addAll(strokeIds)
        it.layerIds.clear(); it.layerIds.addAll(layerIds)
    }
    fun moveCard(x: Float, y: Float) { cardX = x; cardY = y }
    fun addStroke(points: List<Point>) { addStyledStroke(points, InkStyle().color, InkStyle().width) }
    fun addStyledStroke(points: List<Point>, color: Int, width: Float) { if (points.isNotEmpty()) { require(width.isFinite() && width in 0.5f..40f); strokes.add(points.toList()); strokeStyles.add(InkStyle(color, width)); strokeIds.add(UUID.randomUUID().toString()) } }
    fun addBlock(type: String, content: String) { blocks.add(Block(type, content)) }
    fun updateBlock(index: Int, content: String) { blocks.getOrNull(index)?.content = content }
    fun canChangeBlockType(index: Int): Boolean = index in blocks.indices && isConvertibleBlockType(blocks[index].type)
    fun canFormatBlockText(index: Int): Boolean = index in blocks.indices && isFormattableBlockType(blocks[index].type)
    fun changeBlockType(index: Int, type: String) {
        require(canChangeBlockType(index) && isConvertibleBlockType(type)) { "不支持的文档块类型" }
        val block = blocks[index]
        blocks[index] = block.copy(type = type)
    }
    fun moveBlock(from: Int, to: Int) { require(from in blocks.indices && to in blocks.indices); if (from != to) blocks.add(to, blocks.removeAt(from)) }
    fun addShape(type: String, x: Float, y: Float, width: Float, height: Float, color: Int = Shape(type, x, y, width, height).color) {
        shapes.add(Shape(type, x, y, width, height, color))
    }
    fun addConnector(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        color: Int = Connector(x1, y1, x2, y2).color,
        pathStyle: String = "curve",
        endMarker: String = "arrow",
        startMarker: String = "none",
        width: Float = 4f
    ) {
        require(pathStyle in setOf("straight", "curve", "step"))
        require(endMarker in CONNECTOR_MARKERS && startMarker in CONNECTOR_MARKERS)
        connectors.add(
            Connector(
                x1,
                y1,
                x2,
                y2,
                color = color,
                width = width.coerceIn(1f, 8f),
                curved = pathStyle == "curve",
                endMarker = endMarker,
                pathStyle = pathStyle,
                startMarker = startMarker
            )
        )
    }
    fun addCanvasImage(name: String, x: Float, y: Float, width: Float, height: Float) { images.add(CanvasImage(name, x, y, width, height)) }
    fun duplicate(newId: String, mapping: Map<String, String>): Page { val p = decode(encode()); p.blocks.forEach { if (it.type == "image") it.content = mapping[it.content] ?: it.content }; p.images.replaceAll { it.copy(name = mapping[it.name] ?: it.name) }; return Page(newId, p.title, p.text, p.cardX, p.cardY, p.strokes, p.blocks, p.shapes, p.connectors, p.images, p.strokeStyles, p.folder, p.trashed, p.cardTemplates, p.folders.toMutableSet(), p.tags.toMutableSet(), p.favorite).also { it.icon = p.icon; it.strokeIds.clear();it.strokeIds.addAll(p.strokeIds);it.layerIds.addAll(p.orderedLayerIds()) } }
    fun encode(): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out ->
            require(connectors.all { it.endMarker in CONNECTOR_MARKERS && it.startMarker in CONNECTOR_MARKERS && it.pathStyle in setOf("straight","curve","step") && it.label.length <= 200 }) { "连接线端点、路径或文字无效" }
            require(folders.size <= 1000 && folders.all(FolderCatalog::valid))
            require(tags.size <= 200 && tags.all(TagCatalog::valid))
            require(PageIcons.isValid(icon))
            require(shapes.all { it.shadowStyle in setOf("none", "box", "sticker") }) { "笔记阴影样式无效" }
            out.writeInt(25); out.writeUTF(id); out.writeUTF(title)
            out.writeString(text)
            out.writeFloat(cardX); out.writeFloat(cardY); out.writeInt(strokes.size)
            strokes.forEach { stroke -> out.writeInt(stroke.size); stroke.forEach { out.writeFloat(it.x); out.writeFloat(it.y) } }
            out.writeInt(blocks.size); blocks.forEach { out.writeUTF(it.type); out.writeString(it.content); out.writeBoolean(it.checked) }
            out.writeInt(shapes.size); shapes.forEach { out.writeUTF(it.type); out.writeFloat(it.x); out.writeFloat(it.y); out.writeFloat(it.width); out.writeFloat(it.height) }
            out.writeInt(connectors.size); connectors.forEach { out.writeFloat(it.x1); out.writeFloat(it.y1); out.writeFloat(it.x2); out.writeFloat(it.y2) }
            shapes.forEach { out.writeInt(it.color) }; connectors.forEach { out.writeInt(it.color); out.writeFloat(it.width) }
            out.writeInt(images.size); images.forEach { out.writeUTF(it.name); out.writeFloat(it.x); out.writeFloat(it.y); out.writeFloat(it.width); out.writeFloat(it.height) }
            require(strokes.size == strokeStyles.size); strokeStyles.forEach { out.writeInt(it.color); out.writeFloat(it.width) }
            blocks.forEach { out.writeBoolean(it.bold); out.writeBoolean(it.italic); out.writeBoolean(it.underline) }; out.writeString(folder); out.writeBoolean(trashed)
            shapes.forEach { out.writeUTF(it.id); out.writeUTF(it.group) }; images.forEach { out.writeUTF(it.id); out.writeUTF(it.group) }; connectors.forEach { out.writeUTF(it.startShape); out.writeUTF(it.endShape) }; connectors.forEach { out.writeUTF(it.startPort); out.writeUTF(it.endPort); out.writeBoolean(it.animated) }; shapes.forEach { out.writeString(it.title); out.writeString(it.body) }; connectors.forEach { out.writeBoolean(it.curved) }; out.writeInt(cardTemplates.size); cardTemplates.forEach { out.writeString(it.name); out.writeString(it.title); out.writeString(it.body); out.writeInt(it.color) }; connectors.forEach { out.writeUTF(it.id) }; connectors.forEach { out.writeUTF(it.endMarker) }; connectors.forEach { out.writeUTF(it.pathStyle) }
            require(strokeIds.size==strokes.size && strokeIds.all { it.isNotEmpty() } && (strokeIds + shapes.map { it.id } + images.map { it.id } + connectors.map { it.id }).distinct().size == strokeIds.size + shapes.size + images.size + connectors.size); strokeIds.forEach { out.writeUTF(it) }
            val layers=orderedLayerIds();out.writeInt(layers.size);layers.forEach(out::writeUTF)
            images.forEach { require(it.rotation in 0..3 && validImageCrop(it.cropLeft,it.cropTop,it.cropRight,it.cropBottom));out.writeInt(it.rotation);out.writeBoolean(it.flipX);out.writeBoolean(it.flipY);out.writeFloat(it.cropLeft);out.writeFloat(it.cropTop);out.writeFloat(it.cropRight);out.writeFloat(it.cropBottom) }
            shapes.forEach {
                require(it.borderStyle in setOf("solid", "dashed", "none") && it.borderWidth in 0f..12f && it.cornerRadius in 0f..64f)
                out.writeInt(it.borderColor); out.writeUTF(it.borderStyle); out.writeFloat(it.borderWidth); out.writeFloat(it.cornerRadius); out.writeInt(it.textColor)
            }
            shapes.forEach { out.writeBoolean(it.locked) }
            shapes.forEach {
                require(it.fontSize in 8f..96f && it.textAlign in setOf("left", "center", "right"))
                out.writeUTF(it.cardKind); out.writeFloat(it.fontSize); out.writeUTF(it.textAlign)
            }
            out.writeInt(folders.size); folders.forEach(out::writeUTF)
            out.writeInt(tags.size); tags.forEach(out::writeUTF)
            out.writeBoolean(favorite)
            connectors.forEach { out.writeUTF(it.startMarker); out.writeString(it.label) }
            out.writeString(icon)
            shapes.forEach { out.writeUTF(it.shadowStyle) }
            shapes.forEach {
                require(it.fontFamily in TextFormatting.FONT_FAMILIES) { "字体族无效" }
                require(it.fontStyle in TextFormatting.FONT_STYLES) { "字体样式无效" }
                require(TextFormatting.validWeight(it.fontWeight)) { "字重无效" }
                require(it.type == "text" || it.textSpans.isEmpty()) { "只有文本对象可以保存选区格式" }
                require(TextFormatting.isValid(it.textSpans, it.body.length)) { "文本选区格式无效" }
                val spans = it.textSpans
                out.writeUTF(it.fontFamily); out.writeUTF(it.fontStyle); out.writeInt(it.fontWeight)
                out.writeInt(spans.size)
                spans.forEach { span -> out.writeInt(span.start); out.writeInt(span.end); out.writeInt(span.flags) }
            }
        }
        return bytes.toByteArray()
    }
    companion object {
        fun create(title: String) = Page(UUID.randomUUID().toString(), title, "", 80f, 100f, mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())
        fun decode(bytes: ByteArray): Page = DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            val version = input.readInt(); require(version in 1..25) { "不支持的文件版本" }
            val id = input.readUTF(); val title = input.readUTF(); val text = input.readString()
            val x = input.readFloat(); val y = input.readFloat(); val count = input.readCount()
            val strokes = MutableList(count) { val n = input.readCount(1_000_000); List(n) { Point(input.readFloat(), input.readFloat()) } }
            val blocks = mutableListOf<Block>(); val shapes = mutableListOf<Shape>(); val connectors = mutableListOf<Connector>()
            if (version >= 2) {
                repeat(input.readCount()) { blocks.add(Block(input.readUTF(), input.readString(), input.readBoolean())) }
                repeat(input.readCount()) { shapes.add(Shape(input.readUTF(), input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat())) }
                repeat(input.readCount()) { connectors.add(Connector(input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat())) }
            }
            val images = mutableListOf<CanvasImage>()
            if (version >= 3) { for (i in shapes.indices) shapes[i] = shapes[i].copy(color = input.readInt()); for (i in connectors.indices) connectors[i] = connectors[i].copy(color = input.readInt(), width = input.readFloat()); repeat(input.readCount()) { images.add(CanvasImage(input.readUTF(), input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat())) } }
            val styles = MutableList(strokes.size) { if (version >= 4) { val color = input.readInt(); val width = input.readFloat(); require(width.isFinite() && width in 0.5f..40f); InkStyle(color, width) } else InkStyle() }
            var folder = ""; var trashed = false
            if (version >= 5) { blocks.forEach { it.bold = input.readBoolean(); it.italic = input.readBoolean(); it.underline = input.readBoolean() }; folder = input.readString(); require(folder.length <= 100); trashed = input.readBoolean() }
            if (version >= 6) { for (i in shapes.indices) shapes[i] = shapes[i].copy(id = input.readUTF(), group = input.readUTF()); for (i in images.indices) images[i] = images[i].copy(id = input.readUTF(), group = input.readUTF()); for (i in connectors.indices) connectors[i] = connectors[i].copy(startShape = input.readUTF(), endShape = input.readUTF()); val ids = shapes.map { it.id } + images.map { it.id }; require(ids.all { it.isNotEmpty() } && ids.distinct().size == ids.size); require(connectors.all { (it.startShape.isEmpty() || shapes.any { s -> s.id == it.startShape }) && (it.endShape.isEmpty() || shapes.any { s -> s.id == it.endShape }) }) }
            if (version >= 7) { for (i in connectors.indices) { val start = input.readUTF(); val end = input.readUTF(); require(start in setOf("center", "left", "right", "top", "bottom") && end in setOf("center", "left", "right", "top", "bottom")); connectors[i] = connectors[i].copy(startPort = start, endPort = end, animated = input.readBoolean()) } }
            val templates = mutableListOf<CardTemplate>()
            if (version >= 8) { for (i in shapes.indices) shapes[i]=shapes[i].copy(title=input.readString(),body=input.readString()); for (i in connectors.indices) { val curved=input.readBoolean(); connectors[i]=connectors[i].copy(curved=curved,pathStyle=if(curved)"curve" else "straight") }; repeat(input.readCount(1000)) { templates.add(CardTemplate(input.readString(),input.readString(),input.readString(),input.readInt())) } }
            if (version >= 9) { for(i in connectors.indices) connectors[i]=connectors[i].copy(id=input.readUTF()); val ids=shapes.map { it.id }+images.map { it.id }+connectors.map { it.id }; require(ids.all { it.isNotEmpty() } && ids.distinct().size==ids.size) }
            if (version >= 10) { for (i in connectors.indices) { val marker=input.readUTF(); require(marker in CONNECTOR_MARKERS); connectors[i]=connectors[i].copy(endMarker=marker) } }
            if (version >= 11) { for (i in connectors.indices) { val style=input.readUTF(); require(style in setOf("straight","curve","step")); connectors[i]=connectors[i].copy(pathStyle=style) } }
            val page=Page(id, title, text, x, y, strokes, blocks, shapes, connectors, images, styles, folder, trashed, templates)
            if(version>=12) { val ids=List(strokes.size) { input.readUTF() }; val all=ids+shapes.map { it.id }+images.map { it.id }+connectors.map { it.id }; require(all.all { it.isNotEmpty() } && all.distinct().size==all.size); page.strokeIds.clear();page.strokeIds.addAll(ids) }
            if(version>=13) { val count=input.readCount();val layers=List(count) { input.readUTF() };val all=page.orderedLayerIds();require(layers.size==all.size && layers.toSet()==all.toSet());page.layerIds.clear();page.layerIds.addAll(layers) }
            if(version>=17) { for(i in page.images.indices) {val rotation=input.readInt();val flipX=input.readBoolean();val flipY=input.readBoolean();val left=input.readFloat();val top=input.readFloat();val right=input.readFloat();val bottom=input.readFloat();require(rotation in 0..3 && validImageCrop(left,top,right,bottom));page.images[i]=page.images[i].copy(rotation=rotation,flipX=flipX,flipY=flipY,cropLeft=left,cropTop=top,cropRight=right,cropBottom=bottom)} }
            if(version>=18) { for(i in page.shapes.indices) { val borderColor=input.readInt(); val borderStyle=input.readUTF(); val borderWidth=input.readFloat(); val cornerRadius=input.readFloat(); val textColor=input.readInt(); require(borderStyle in setOf("solid","dashed","none") && borderWidth in 0f..12f && cornerRadius in 0f..64f); page.shapes[i]=page.shapes[i].copy(borderColor=borderColor,borderStyle=borderStyle,borderWidth=borderWidth,cornerRadius=cornerRadius,textColor=textColor) } }
            if(version>=19) { for(i in page.shapes.indices) page.shapes[i]=page.shapes[i].copy(locked=input.readBoolean()) }
            if(version>=20) { for(i in page.shapes.indices) { val kind=input.readUTF(); val fontSize=input.readFloat(); val textAlign=input.readUTF(); require(fontSize in 8f..96f && textAlign in setOf("left","center","right")); page.shapes[i]=page.shapes[i].copy(cardKind=kind,fontSize=fontSize,textAlign=textAlign) } }
            if(version>=21) {
                val folderCount=input.readCount(1000)
                val decodedFolders=List(folderCount) { input.readUTF() }
                require(decodedFolders.all(FolderCatalog::valid) && decodedFolders.distinct().size==decodedFolders.size)
                val tagCount=input.readCount(200)
                val decodedTags=List(tagCount) { input.readUTF() }
                require(decodedTags.all(TagCatalog::valid) && decodedTags.distinct().size==decodedTags.size)
                page.folders.clear(); page.folders.addAll(decodedFolders)
                page.tags.clear(); page.tags.addAll(decodedTags)
                page.favorite=input.readBoolean()
            }
            if(version>=22) {
                for(i in page.connectors.indices) {
                    val startMarker=input.readUTF()
                    val label=input.readString()
                    require(startMarker in CONNECTOR_MARKERS && label.length<=200)
                    page.connectors[i]=page.connectors[i].copy(startMarker=startMarker,label=label)
                }
            }
            if(version>=23) {
                val icon=input.readString()
                require(PageIcons.isValid(icon))
                page.icon=icon
            }
            if(version>=24) {
                for(i in page.shapes.indices) {
                    val shadowStyle=input.readUTF()
                    require(shadowStyle in setOf("none", "box", "sticker"))
                    page.shapes[i]=page.shapes[i].copy(shadowStyle=shadowStyle)
                }
            }
            if(version>=25) {
                for(i in page.shapes.indices) {
                    val family=input.readUTF()
                    val style=input.readUTF()
                    val weight=input.readInt()
                    require(family in TextFormatting.FONT_FAMILIES && style in TextFormatting.FONT_STYLES && TextFormatting.validWeight(weight)) { "字体属性无效" }
                    val spanCount=input.readCount(10_000)
                    val spans=List(spanCount) {
                        val start=input.readInt(); val end=input.readInt(); val flags=input.readInt()
                        require(flags and TextSpan.ALL.inv() == 0) { "文本选区标志无效" }
                        TextSpan(start,end,flags)
                    }
                    val shape=page.shapes[i]
                    require(shape.type == "text" || spans.isEmpty()) { "只有文本对象可以保存选区格式" }
                    require(TextFormatting.isValid(spans, shape.body.length)) { "文本选区格式无效" }
                    page.shapes[i]=shape.copy(fontFamily=family,fontStyle=style,fontWeight=weight,textSpans=TextFormatting.normalize(spans, shape.body.length))
                }
            }
            page
        }
    }
}
private fun DataOutputStream.writeString(value: String) { val data = value.toByteArray(Charsets.UTF_8); writeInt(data.size); write(data) }
private fun DataInputStream.readString(): String { val size = readInt(); require(size in 0..10_000_000) { "文本长度无效" }; val data = ByteArray(size); readFully(data); return data.toString(Charsets.UTF_8) }
private fun DataInputStream.readCount(max: Int = 100_000): Int { val count = readInt(); require(count in 0..max) { "数据数量无效" }; return count }




