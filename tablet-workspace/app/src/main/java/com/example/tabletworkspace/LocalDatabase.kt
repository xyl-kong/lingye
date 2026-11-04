package com.example.tabletworkspace
import java.io.*
import java.util.Base64
import java.util.Locale
import java.util.UUID
data class DatabaseField(val id:String,val name:String,val type:String="text")
data class DatabaseRow(val id:String,val title:String,val status:String,val values:Map<String,String> = emptyMap())
data class LocalDatabase(
 val name:String,
 val rows:List<DatabaseRow>,
 val fields:List<DatabaseField> = emptyList(),
 val viewMode:String = "table",
 val sortField:String = "",
 val descending:Boolean = false,
 val calendarField:String = ""
) {
 fun filter(query:String):List<DatabaseRow> { val q=query.trim().lowercase(Locale.ROOT); return rows.filter { it.title.lowercase(Locale.ROOT).contains(q) || it.status.lowercase(Locale.ROOT).contains(q) || it.values.values.any { v -> v.lowercase(Locale.ROOT).contains(q) } } }
 fun sortedRows(query:String, field:String, descending:Boolean):List<DatabaseRow> {
  val filtered=filter(query)
  if(field.isEmpty()) return filtered
  val definition=fields.firstOrNull { it.id==field }
  if(field !in setOf("@title","@status") && definition==null) return filtered
  fun value(row:DatabaseRow)=when(field) { "@title"->row.title; "@status"->row.status; else->row.values[field].orEmpty() }
  return filtered.sortedWith { a,b ->
   val x=value(a);val y=value(b)
   when {
    x.isBlank() && y.isBlank()->0
    x.isBlank()->1
    y.isBlank()->-1
    else->{ val cmp=if(definition?.type=="number") (x.toDoubleOrNull() ?: 0.0).compareTo(y.toDoubleOrNull() ?: 0.0) else x.compareTo(y,ignoreCase=true); if(descending) -cmp else cmp }
   }
  }
 }
 fun board():Map<String,List<DatabaseRow>> = rows.groupBy { it.status.ifBlank { "未设置" } }
 fun calendar(fieldId:String, month:java.time.YearMonth, query:String=""):Map<java.time.LocalDate,List<DatabaseRow>> {
  require(fields.any { it.id==fieldId && it.type=="date" })
  return filter(query).mapNotNull { row -> runCatching { java.time.LocalDate.parse(row.values[fieldId].orEmpty()) }.getOrNull()?.takeIf { java.time.YearMonth.from(it)==month }?.let { it to row } }.groupBy({it.first},{it.second})
 }
 fun renameField(id:String,name:String):LocalDatabase { require(name.isNotBlank() && name.trim().length<=100 && fields.any { it.id==id }); return copy(fields=fields.map { if(it.id==id) it.copy(name=name.trim()) else it }) }
 fun moveRow(id:String,status:String):LocalDatabase { require(status.length<=1000 && rows.any { it.id==id }); return copy(rows=rows.map { if(it.id==id) it.copy(status=status) else it }) }
 fun removeField(id:String)=copy(fields=fields.filterNot { it.id==id },rows=rows.map { it.copy(values=it.values-id) })
 fun moveField(id:String,offset:Int):LocalDatabase {
  val from=fields.indexOfFirst { it.id==id }; if(from<0) return this
  val to=(from+offset).coerceIn(0,fields.lastIndex); if(from==to) return this
  return copy(fields=fields.toMutableList().apply { add(to,removeAt(from)) })
 }
 fun moveRecord(id:String,offset:Int):LocalDatabase {
  val from=rows.indexOfFirst { it.id==id }; if(from<0) return this
  val to=(from+offset).coerceIn(0,rows.lastIndex); if(from==to) return this
  return copy(rows=rows.toMutableList().apply { add(to,removeAt(from)) })
 }
}
object DatabaseCodec {
 fun encode(db:LocalDatabase):String {
  require(db.rows.size<=10000 && db.name.length<=500 && db.fields.size<=40 && db.fields.all { it.id.isNotEmpty() && it.id.length<=100 && it.name.isNotBlank() && it.name.length<=100 && it.type in setOf("text","multiline","number","checkbox","date") } && db.fields.map { it.id }.distinct().size==db.fields.size && db.rows.all { it.id.isNotEmpty() && it.id.length<=100 && it.title.length<=1000 && it.status.length<=1000 && it.values.size<=40 && it.values.all { (k,v) -> k.length<=100 && v.length<=1000 && db.fields.any { f -> f.id==k } } } && db.rows.map { it.id }.distinct().size==db.rows.size && db.rows.all { row -> row.values.all { (k,v) -> val t=db.fields.first { it.id==k }.type; when(t) { "checkbox" -> v in setOf("", "true", "false"); "number" -> v.isEmpty() || v.toDoubleOrNull()?.isFinite()==true; "date" -> v.isEmpty() || runCatching { java.time.LocalDate.parse(v).toString()==v }.getOrDefault(false); else -> true } } } && db.viewMode in setOf("table","board","list","calendar") && (db.sortField.isEmpty() || db.sortField in setOf("@title","@status") || db.fields.any { it.id==db.sortField }) && (db.calendarField.isEmpty() || db.fields.any { it.id==db.calendarField && it.type=="date" }))
  val b=ByteArrayOutputStream(); DataOutputStream(b).use { out-> out.writeInt(4);out.writeUTF(db.name);out.writeInt(db.rows.size);db.rows.forEach { out.writeUTF(it.id);out.writeUTF(it.title);out.writeUTF(it.status) };out.writeInt(db.fields.size);db.fields.forEach { out.writeUTF(it.id);out.writeUTF(it.name) };db.rows.forEach { row -> out.writeInt(row.values.size);row.values.toSortedMap().forEach { (k,v) -> out.writeUTF(k);out.writeUTF(v) } };db.fields.forEach { out.writeUTF(it.type) };out.writeUTF(db.viewMode);out.writeUTF(db.sortField);out.writeBoolean(db.descending);out.writeUTF(db.calendarField) };return Base64.getEncoder().encodeToString(b.toByteArray())
 }
 fun decode(data:String):LocalDatabase { require(data.length<=10000000); return DataInputStream(ByteArrayInputStream(Base64.getDecoder().decode(data))).use { input -> val version=input.readInt();require(version in 1..4); val name=input.readUTF();val n=input.readInt();require(n in 0..10000);var rows=List(n) { DatabaseRow(input.readUTF(),input.readUTF(),input.readUTF()) };var fields=if(version>=2) { val count=input.readInt();require(count in 0..40);List(count) { DatabaseField(input.readUTF(),input.readUTF()) } } else emptyList();if(version>=2) rows=rows.map { row -> val count=input.readInt();require(count in 0..40);val values=mutableMapOf<String,String>();repeat(count) { val key=input.readUTF();require(!values.containsKey(key));values[key]=input.readUTF() };row.copy(values=values) };if(version>=3) fields=fields.map { it.copy(type=input.readUTF()) };val viewMode=if(version>=4) input.readUTF() else "table";val sortField=if(version>=4) input.readUTF() else "";val descending=if(version>=4) input.readBoolean() else false;val calendarField=if(version>=4) input.readUTF() else "";require(input.available()==0);val db=LocalDatabase(name,rows,fields,viewMode,sortField,descending,calendarField);encode(db);db } }
}
fun Page.addDatabase(name:String) { addBlock("database",DatabaseCodec.encode(LocalDatabase(name,emptyList()))) }
fun Page.addDatabaseAfter(index:Int,name:String) { val at=(index+1).coerceIn(0,blocks.size); blocks.add(at,Block("database",DatabaseCodec.encode(LocalDatabase(name,emptyList())))) }
fun LocalDatabase.addRow()=copy(rows=rows+DatabaseRow(UUID.randomUUID().toString(),"","待办"))
