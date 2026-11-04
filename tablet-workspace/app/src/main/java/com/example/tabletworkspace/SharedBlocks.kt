package com.example.tabletworkspace

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.UUID

data class SharedSpan(val start:Int,val end:Int,val bold:Boolean=false,val italic:Boolean=false,val underline:Boolean=false)
data class SharedSnapshot(val text:String,val kind:String,val bold:Boolean,val italic:Boolean,val underline:Boolean,val checked:Boolean,val spans:List<SharedSpan>)
data class SharedBlock(val id:String,val text:String,val kind:String="paragraph",val bold:Boolean=false,val italic:Boolean=false,val underline:Boolean=false,val checked:Boolean=false,val spans:List<SharedSpan> = emptyList(),val undoHistory:List<SharedSnapshot> = emptyList(),val redoHistory:List<SharedSnapshot> = emptyList())
fun SharedBlock.snapshot()=SharedSnapshot(text,kind,bold,italic,underline,checked,spans)
fun SharedBlock.imageNames():List<String> = (listOf(snapshot())+undoHistory+redoHistory).filter { it.kind=="image" }.map { it.text }
fun SharedBlock.remapImages(mapping:Map<String,String>):SharedBlock {
 fun remap(s:SharedSnapshot)=if(s.kind=="image") s.copy(text=mapping[s.text] ?: error("备份缺少共享图片")) else s
 val current=remap(snapshot());return copy(text=current.text,undoHistory=undoHistory.map(::remap),redoHistory=redoHistory.map(::remap))
}
fun SharedBlock.withHistoryFrom(old:SharedBlock):SharedBlock { require(id==old.id);if(snapshot()==old.snapshot()) return old;val history=(old.undoHistory+old.snapshot()).takeLast(30).toMutableList();while(history.size>1 && history.sumOf { it.text.toByteArray(Charsets.UTF_8).size.toLong() }>8_000_000) history.removeAt(0);return copy(undoHistory=history,redoHistory=emptyList()) }
private fun SharedBlock.restoreSnapshot(s:SharedSnapshot)=copy(text=s.text,kind=s.kind,bold=s.bold,italic=s.italic,underline=s.underline,checked=s.checked,spans=s.spans)
fun SharedBlock.undoShared():SharedBlock? {val old=undoHistory.lastOrNull() ?: return null;return restoreSnapshot(old).copy(undoHistory=undoHistory.dropLast(1),redoHistory=(redoHistory+snapshot()).takeLast(30))}
fun SharedBlock.redoShared():SharedBlock? {val next=redoHistory.lastOrNull() ?: return null;return restoreSnapshot(next).copy(undoHistory=(undoHistory+snapshot()).takeLast(30),redoHistory=redoHistory.dropLast(1))}
fun SharedBlock.toggleRange(start:Int,end:Int,style:String):SharedBlock {
 require(kind in setOf("paragraph","heading","todo") && style in setOf("bold","italic","underline") && start in 0..text.length && end in start..text.length)
 if(start==end)return this
 val from=if(start>0 && start<text.length && Character.isHighSurrogate(text[start-1]) && Character.isLowSurrogate(text[start])) start-1 else start
 val to=if(end>0 && end<text.length && Character.isHighSurrogate(text[end-1]) && Character.isLowSurrogate(text[end])) end+1 else end
 val points=(listOf(0,text.length,from,to)+spans.flatMap { listOf(it.start,it.end) }).distinct().sorted()
 val whole=when(style) {"bold"->bold;"italic"->italic;else->underline}
 fun active(s:SharedSpan?)=whole || when(style) {"bold"->s?.bold==true;"italic"->s?.italic==true;else->s?.underline==true}
 val disable=points.zipWithNext().filter { (a,b)->a>=from && b<=to && a<b }.all { (a,_)->active(spans.firstOrNull { it.start<=a && it.end>a }) }
 val result=mutableListOf<SharedSpan>()
 points.zipWithNext().forEach { (a,b)->if(a<b) {val old=spans.firstOrNull { it.start<=a && it.end>=b };var bold=old?.bold==true || (style=="bold" && whole);var italic=old?.italic==true || (style=="italic" && whole);var underline=old?.underline==true || (style=="underline" && whole)
  if(a>=from && b<=to) when(style) {"bold"->bold=!disable;"italic"->italic=!disable;else->underline=!disable}
  if(bold || italic || underline) {val prev=result.lastOrNull();if(prev!=null && prev.end==a && prev.bold==bold && prev.italic==italic && prev.underline==underline) result[result.lastIndex]=prev.copy(end=b) else result.add(SharedSpan(a,b,bold,italic,underline))}
 } }
 return copy(spans=result,bold=if(style=="bold") false else bold,italic=if(style=="italic") false else italic,underline=if(style=="underline") false else underline)
}
fun SharedBlock.withTextEdit(value:String):SharedBlock {
 if(value==text)return this
 var prefix=0;while(prefix<text.length && prefix<value.length && text[prefix]==value[prefix])prefix++
 var suffix=0;while(suffix<text.length-prefix && suffix<value.length-prefix && text[text.lastIndex-suffix]==value[value.lastIndex-suffix])suffix++
 if(prefix>0 && ((prefix<text.length && Character.isHighSurrogate(text[prefix-1]) && Character.isLowSurrogate(text[prefix])) || (prefix<value.length && Character.isHighSurrogate(value[prefix-1]) && Character.isLowSurrogate(value[prefix])))) prefix--
 fun splitAt(s:String,i:Int)=i>0 && i<s.length && Character.isHighSurrogate(s[i-1]) && Character.isLowSurrogate(s[i])
 if(suffix>0 && (splitAt(text,text.length-suffix) || splitAt(value,value.length-suffix))) suffix--
 val cut=text.length-suffix;val shift=value.length-text.length;val next=mutableListOf<SharedSpan>()
 spans.forEach { span -> if(span.start<prefix) {val end=minOf(span.end,prefix);if(end>span.start)next.add(span.copy(end=end))};if(span.end>cut) {val start=maxOf(span.start,cut);if(span.end>start)next.add(span.copy(start=start+shift,end=span.end+shift))} }
 return copy(text=value,spans=next)
}
fun SharedBlock.searchText():String = when(kind) { "image"->"";"database"->runCatching { DatabaseCodec.decode(text).let { db -> db.name+" "+db.rows.joinToString(" ") { row->row.title+" "+row.status+" "+row.values.values.joinToString(" ") } } }.getOrDefault("");else->text }
class SharedBlockHistory(initial:SharedBlock,private val limit:Int=100) {
 private var current=initial;private val previous=ArrayDeque<SharedBlock>();private val next=ArrayDeque<SharedBlock>()
 fun record(value:SharedBlock) {require(value.id==current.id);if(value==current)return;previous.addLast(current);if(previous.size>limit)previous.removeFirst();current=value;next.clear()}
 fun undo():SharedBlock? {if(previous.isEmpty())return null;next.addLast(current);current=previous.removeLast();return current}
 fun redo():SharedBlock? {if(next.isEmpty())return null;previous.addLast(current);current=next.removeLast();return current}
}
fun Page.remapSharedReferences(mapping:Map<String,String>) { blocks.filter { it.type=="shared" }.forEach { block -> block.content=mapping[block.content] ?: error("备份缺少共享块") } }
object SharedBlockCatalog {
 fun encode(items:List<SharedBlock>):ByteArray {
  fun valid(s:SharedSnapshot):Boolean = s.text.toByteArray(Charsets.UTF_8).size<=10_000_000 && s.kind in setOf("paragraph","heading","todo","image","database") && (s.kind!="image" || BackupArchive.validImage(s.text)) && (s.kind!="database" || runCatching { DatabaseCodec.decode(s.text) }.isSuccess) && s.spans.size<=10000 && s.spans.all { it.start>=0 && it.end>it.start && it.end<=s.text.length && (it.bold || it.italic || it.underline) } && s.spans.zipWithNext().all { (a,b)->a.end<=b.start } && (s.kind in setOf("paragraph","heading","todo") || s.spans.isEmpty())
  require(items.size<=10000 && items.map { it.id }.distinct().size==items.size && items.all { runCatching { UUID.fromString(it.id).toString()==it.id }.getOrDefault(false) && it.undoHistory.size<=30 && it.redoHistory.size<=30 && (listOf(it.snapshot())+it.undoHistory+it.redoHistory).all(::valid) })
  require(items.sumOf { item -> (listOf(item.snapshot())+item.undoHistory+item.redoHistory).sumOf { it.text.toByteArray(Charsets.UTF_8).size.toLong()+it.spans.size*16L+128L } } <= 19_000_000L) { "共享内容及历史过大" }
  val bytes=ByteArrayOutputStream();DataOutputStream(bytes).use { out ->
   fun writeSnapshot(s:SharedSnapshot) {val data=s.text.toByteArray(Charsets.UTF_8);out.writeInt(data.size);out.write(data);out.writeUTF(s.kind);out.writeBoolean(s.bold);out.writeBoolean(s.italic);out.writeBoolean(s.underline);out.writeBoolean(s.checked);out.writeInt(s.spans.size);s.spans.forEach { out.writeInt(it.start);out.writeInt(it.end);out.writeBoolean(it.bold);out.writeBoolean(it.italic);out.writeBoolean(it.underline) }}
   out.writeInt(3);out.writeInt(items.size);items.forEach {out.writeUTF(it.id);writeSnapshot(it.snapshot());out.writeInt(it.undoHistory.size);it.undoHistory.forEach(::writeSnapshot);out.writeInt(it.redoHistory.size);it.redoHistory.forEach(::writeSnapshot)}
  };return bytes.toByteArray()
 }
 fun decode(data:ByteArray):List<SharedBlock> = DataInputStream(ByteArrayInputStream(data)).use { input ->
  val version=input.readInt();require(version in 1..3);val count=input.readInt();require(count in 0..10000)
  fun readSnapshot():SharedSnapshot {val n=input.readInt();require(n in 0..10_000_000 && n<=input.available());val bytes=ByteArray(n);input.readFully(bytes);val text=bytes.toString(Charsets.UTF_8);if(version==1)return SharedSnapshot(text,"paragraph",false,false,false,false,emptyList());val kind=input.readUTF();val bold=input.readBoolean() || (version==2 && kind=="heading");val italic=input.readBoolean();val underline=input.readBoolean();val checked=input.readBoolean();val spans=if(version>=3) {val size=input.readInt();require(size in 0..10000);List(size) {SharedSpan(input.readInt(),input.readInt(),input.readBoolean(),input.readBoolean(),input.readBoolean())}} else emptyList();return SharedSnapshot(text,kind,bold,italic,underline,checked,spans)}
  val items=List(count) {val id=input.readUTF();val s=readSnapshot();val undo=if(version>=3) {val n=input.readInt();require(n in 0..30);List(n) {readSnapshot()}} else emptyList();val redo=if(version>=3) {val n=input.readInt();require(n in 0..30);List(n) {readSnapshot()}} else emptyList();SharedBlock(id,s.text,s.kind,s.bold,s.italic,s.underline,s.checked,s.spans,undo,redo) };require(input.available()==0);encode(items);items
 }
}
