package com.example.tabletworkspace
import java.io.*
import java.util.Locale
object TemplateCatalog {
 fun encode(items: List<CardTemplate>): ByteArray {
  require(items.size<=1000 && items.all { it.name.isNotBlank() && it.name.length<=100 && it.title.length<=10000 && it.body.length<=1000000 && it.canvasData.length<=10_000_000 && it.width.isFinite() && it.height.isFinite() && it.width in 12f..2000f && it.height in 12f..2000f } && items.map { it.name.lowercase(Locale.ROOT) }.distinct().size==items.size)
  items.forEach { t -> t.canvasPage()?.let { p -> require(p.blocks.isEmpty() && p.cardTemplates.isEmpty() && p.images.all { BackupArchive.validImage(it.name) }) { "模板内容无效" } } }
  val b=ByteArrayOutputStream(); DataOutputStream(b).use { out -> out.writeInt(3); out.writeInt(items.size); items.forEach { out.writeUTF(it.name); val title=it.title.toByteArray(Charsets.UTF_8); val body=it.body.toByteArray(Charsets.UTF_8); out.writeInt(title.size); out.write(title); out.writeInt(body.size); out.write(body); out.writeInt(it.color); out.writeFloat(it.width); out.writeFloat(it.height);val data=it.canvasData.toByteArray(Charsets.UTF_8);out.writeInt(data.size);out.write(data) } }; return b.toByteArray()
 }
 fun decode(data: ByteArray): List<CardTemplate> = DataInputStream(ByteArrayInputStream(data)).use { input ->
  val version=input.readInt(); require(version in 1..3); val n=input.readInt(); require(n in 0..1000)
  fun text():String { val n=input.readInt(); require(n in 0..4000000 && n<=input.available()); val b=ByteArray(n); input.readFully(b); return b.toString(Charsets.UTF_8) }
  val items=List(n) { val name=input.readUTF(); val title=text(); val body=text(); val color=input.readInt(); val width=if(version>=2) input.readFloat() else 240f;val height=if(version>=2) input.readFloat() else 150f;val canvas=if(version>=3) { val size=input.readInt();require(size in 0..10_000_000 && size<=input.available());ByteArray(size).also(input::readFully).toString(Charsets.UTF_8) } else "";CardTemplate(name,title,body,color,width,height,canvas) }; require(input.available()==0); encode(items); items
 }
}
