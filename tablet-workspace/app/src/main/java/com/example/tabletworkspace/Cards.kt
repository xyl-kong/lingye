package com.example.tabletworkspace

enum class CardPreset(val legacyName: String) {
 NOTE("笔记"), TODO("待办"), PROJECT("项目计划"), MEETING("会议记录"), IDEA("灵感");
 companion object { fun fromLegacyName(name: String): CardPreset = entries.firstOrNull { it.legacyName == name } ?: NOTE }
}

data class CardPresetContent(val title: String, val body: String)

fun CardPreset.content(language: AppLanguage): CardPresetContent = when (this) {
 CardPreset.NOTE -> CardPresetContent(tr(language, "笔记", "Note"), tr(language, "写下你的想法…", "Write down your idea…"))
 CardPreset.TODO -> CardPresetContent(tr(language, "待办", "To-do"), tr(language, "☐ 待办事项", "☐ To-do item"))
 CardPreset.PROJECT -> CardPresetContent(tr(language, "项目计划", "Project plan"), tr(language, "目标：\n任务：\n进度：", "Goal:\nTasks:\nProgress:"))
 CardPreset.MEETING -> CardPresetContent(tr(language, "会议记录", "Meeting notes"), tr(language, "议题：\n结论：\n行动项：", "Topics:\nDecisions:\nAction items:"))
 CardPreset.IDEA -> CardPresetContent(tr(language, "灵感", "Idea"), tr(language, "描述：\n标签：", "Description:\nTags:"))
}

fun Page.addCard(
    preset: CardPreset,
    x: Float,
    y: Float,
    language: AppLanguage = AppLanguage.ZH,
    color: Int = 0xfff2f5ff.toInt(),
    cornerRadius: Float = 12f,
    borderStyle: String = "solid",
    borderWidth: Float = 1f,
    shadowStyle: String = "box"
) {
 val content = preset.content(language)
 shapes.add(
     Shape(
         "card", x, y, 240f, 150f, color, title = content.title, body = content.body,
         cardKind = preset.name.lowercase(), cornerRadius = cornerRadius,
         borderStyle = borderStyle, borderWidth = borderWidth, shadowStyle = shadowStyle
     )
 )
}
fun Page.addCard(preset: String,x: Float,y: Float) = addCard(CardPreset.fromLegacyName(preset),x,y)
fun Page.updatePageCardText(title: String, text: String) {
 this.title=title.take(500)
 this.text=text.take(100_000)
}
fun Page.updateShapeCardText(index: Int, title: String, body: String) {
 val card=shapes.getOrNull(index) ?: return
 require(card.type=="card") { "Selected shape is not a card" }
 shapes[index]=card.copy(title=title.take(500),body=body.take(100_000))
}
fun Page.addTextBox(
    x: Float,
    y: Float,
    text: String,
    width: Float = 260f,
    height: Float = 120f,
    textColor: Int = 0xff20293a.toInt(),
    fontSize: Float = 16f,
    textAlign: String = "left"
) {
    shapes.add(
        Shape(
            "text", x, y, width, height,
            color = if (LingyeColors.dark) 0xff252525.toInt() else 0xffffffff.toInt(),
            title = "", body = text,
            shadowStyle = "box",
            borderStyle = "none", borderWidth = 0f, cornerRadius = 8f,
            textColor = if (LingyeColors.dark && textColor == 0xff20293a.toInt()) 0xffeaeaea.toInt() else textColor,
            fontSize = fontSize, textAlign = textAlign
        )
    )
}
fun Page.addCanvasTextBlock(
    kind: String,
    x: Float,
    y: Float,
    language: AppLanguage,
    textColor: Int = 0xff20293a.toInt(),
    width: Float? = null,
    height: Float? = null
) {
    val headingLevel = Regex("h([1-6])").matchEntire(kind)?.groupValues?.get(1)?.toInt()
    val fontSize = when (headingLevel) {
        1 -> 32f; 2 -> 28f; 3 -> 24f; 4 -> 20f; 5 -> 18f; 6 -> 16f
        else -> 16f
    }
    val text = when (headingLevel) {
        1 -> tr(language, "一级标题", "Heading 1")
        2 -> tr(language, "二级标题", "Heading 2")
        3 -> tr(language, "三级标题", "Heading 3")
        4 -> tr(language, "四级标题", "Heading 4")
        5 -> tr(language, "五级标题", "Heading 5")
        6 -> tr(language, "六级标题", "Heading 6")
        else -> when (kind) {
            "code" -> tr(language, "代码", "Code")
            "quote" -> tr(language, "“引用文本”", "“Quote text”")
            "bulleted" -> tr(language, "• 项目符号", "• Bulleted item")
            "numbered" -> tr(language, "1. 编号列表", "1. Numbered item")
            "todo" -> tr(language, "☐ 待办事项", "☐ To-do item")
            else -> tr(language, "双击编辑文本", "Double-tap to edit")
        }
    }
    addTextBox(
        x, y, text,
        width = width ?: if (headingLevel != null) 320f else 260f,
        height = height ?: if (headingLevel != null) 72f else 120f,
        textColor = textColor,
        fontSize = fontSize
    )
}
fun Page.checkNextTodoItem(index: Int): Boolean {
 val card=shapes.getOrNull(index) ?: return false
 if(card.type!="card" || card.cardKind!="todo") return false
 val lines=card.body.lines().toMutableList()
 val next=lines.indexOfFirst { it.startsWith("☐") }
 if(next<0) return false
 lines[next]=lines[next].replaceFirst("☐","☑")
 shapes[index]=card.copy(body=lines.joinToString("\n"))
 return true
}
fun Page.saveCardTemplate(index: Int,name: String) { require(name.isNotBlank() && name.length <= 100); val card=shapes[index]; require(card.type=="card"); cardTemplates.add(CardTemplate(name.trim(),card.title,card.body,card.color)) }
fun Page.insertCardTemplate(index: Int,x: Float,y: Float) { val t=cardTemplates[index]; shapes.add(Shape("card",x,y,240f,150f,t.color,title=t.title,body=t.body)) }

fun Page.renameCardTemplate(index: Int,name: String) { require(name.isNotBlank() && name.trim().length <= 100); cardTemplates[index]=cardTemplates[index].copy(name=name.trim()) }
fun Page.deleteCardTemplate(index: Int) { cardTemplates.removeAt(index) }

fun Page.toggleCardTodo(index: Int, lineIndex: Int): Boolean {
 val card=shapes.getOrNull(index) ?: return false; if(card.type!="card") return false
 val lines=card.body.split("\n").toMutableList(); val line=lines.getOrNull(lineIndex) ?: return false
 val checked=todoChecked(line) ?: return false
 lines[lineIndex]=setTodoChecked(line,!checked)
 shapes[index]=card.copy(body=lines.joinToString("\n")); return true
}

internal fun todoChecked(line: String): Boolean? = when {
    line.startsWith("☐") || line.startsWith("[ ]") || line.startsWith("[]") -> false
    line.startsWith("☑") || line.startsWith("[x]") || line.startsWith("[X]") -> true
    else -> null
}

internal fun todoContent(line: String): String = when {
    line.startsWith("☐") || line.startsWith("☑") -> line.drop(1).trimStart()
    line.startsWith("[ ]") -> line.drop(3).trimStart()
    line.startsWith("[]") -> line.drop(2).trimStart()
    line.startsWith("[x]") || line.startsWith("[X]") -> line.drop(3).trimStart()
    else -> line
}

private fun setTodoChecked(line: String, checked: Boolean): String = when {
    line.startsWith("☐") -> "☑" + line.drop(1)
    line.startsWith("☑") -> "☐" + line.drop(1)
    line.startsWith("[ ]") -> "[" + (if (checked) "x" else " ") + "]" + line.drop(3)
    line.startsWith("[]") -> "[" + (if (checked) "x" else " ") + "]" + line.drop(2)
    line.startsWith("[x]") || line.startsWith("[X]") -> "[" + (if (checked) "x" else " ") + "]" + line.drop(3)
    else -> line
}

/** Finds a visible card todo line using the same screen-space layout as the canvas renderer. */
fun Page.cardTodoAtScreen(
    x: Float,
    y: Float,
    cardLeft: (Int) -> Float,
    cardTop: (Int) -> Float,
    unit: Float
): CardTodoTarget? {
    for (index in shapes.indices.reversed()) {
        val card = shapes[index]
        if (card.type != "card" || card.locked) continue
        val left = cardLeft(index)
        val top = cardTop(index)
        if (x < left + 6f * unit || x > left + card.width * unit - 6f * unit) continue
        val relativeY = y - top
        if (relativeY < 38f * unit) continue
        val lineIndex = ((relativeY - 38f * unit) / (20f * unit)).toInt()
        if (lineIndex !in 0..4) continue
        if (kotlin.math.abs(relativeY - (50f + 20f * lineIndex) * unit) > 12f * unit) continue
        val line = card.body.lines().getOrNull(lineIndex) ?: continue
        if (todoChecked(line) != null) return CardTodoTarget(index, lineIndex)
    }
    return null
}

data class CardTodoTarget(val shapeIndex: Int, val lineIndex: Int)

/** Finds the visible note-card todo line under a world-space tap. */
fun Page.cardTodoAt(point: Point): CardTodoTarget? {
    for (index in shapes.indices.reversed()) {
        val card = shapes[index]
        if (card.type != "card" || card.locked) continue
        if (point.x < card.x + 6f || point.x > card.x + card.width - 6f) continue
        val relativeY = point.y - card.y
        if (relativeY < 38f) continue
        val lineIndex = ((relativeY - 38f) / 20f).toInt()
        if (lineIndex !in 0..4) continue
        if (kotlin.math.abs(relativeY - (50f + 20f * lineIndex)) > 12f) continue
        val line = card.body.lines().getOrNull(lineIndex) ?: continue
        if (todoChecked(line) != null) return CardTodoTarget(index, lineIndex)
    }
    return null
}
