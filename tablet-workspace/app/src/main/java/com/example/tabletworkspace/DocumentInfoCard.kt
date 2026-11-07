package com.example.tabletworkspace
import com.example.tabletworkspace.core.formatDocTime
import com.example.tabletworkspace.core.journalDateLabel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DocumentInfoCard(
    page: Page,
    language: AppLanguage,
    properties: List<DocPropertyInfo>,
    onPropertiesChange: (List<DocPropertyInfo>) -> Unit,
    createdMillis: Long,
    updatedMillis: Long,
    canvasMode: Boolean,
    onCanvasModeChange: (Boolean) -> Unit,
    fullWidth: Boolean,
    onFullWidthChange: (Boolean) -> Unit,
    canvasTheme: String,
    onCanvasThemeChange: (String) -> Unit,
    onAddProperty: () -> Unit,
    onEditTags: () -> Unit,
    onEditIcon: () -> Unit,
    journalDate: String?,
    onJournalDateChange: (String?) -> Unit,
    onOpenJournal: (String) -> Unit,
    templatePage: Boolean,
    onTemplateChange: (Boolean) -> Unit
) {
    var expanded by remember(page.id) { mutableStateOf(true) }
    var propertiesExpanded by remember(page.id) { mutableStateOf(true) }
    var showMore by remember(page.id) { mutableStateOf(false) }
    val customProperties = properties.filter { DocPropertyCatalog.isCustom(it) }.sortedBy { it.order }
    fun displayValue(property: DocPropertyInfo) = docPropertyDisplayValue(
        property, page, canvasMode, fullWidth, canvasTheme, createdMillis, updatedMillis, language
    )
    val visibleCustom = customProperties.filter {
        it.show == DocPropertyCatalog.ALWAYS_SHOW ||
            (it.show == DocPropertyCatalog.HIDE_WHEN_EMPTY && displayValue(it).isNotBlank())
    }
    val hiddenCustom = customProperties.filterNot { it in visibleCustom }
    val hiddenPropertyCount = 6 + hiddenCustom.size
    Column(Modifier.fillMaxWidth().padding(top = 24.dp)) {
        Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(tr(language, "信息", "Info"), fontSize = 16.sp, color = LingyeColors.Ink)
            Spacer(Modifier.weight(1f))
            Text(if (expanded) "▾" else "▸", fontSize = 16.sp, color = LingyeColors.Muted)
        }
        HorizontalDivider(color = LingyeColors.Line)
        if (expanded) {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().clickable { propertiesExpanded = !propertiesExpanded }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(tr(language, "工作区属性", "Workspace properties"), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LingyeColors.Ink)
                Spacer(Modifier.width(6.dp))
                Text(if (propertiesExpanded) "▾" else "▸", fontSize = 14.sp, color = LingyeColors.Muted)
            }
            if (propertiesExpanded) {
                DocInfoValueRow(AppIcon.TAG, tr(language, "标签", "Tags"), page.tags.joinToString(" ") { "#$it" }.ifBlank { tr(language, "属性为空", "Empty") }, onEditTags)
                DocInfoControlRow(AppIcon.DOCUMENT, tr(language, "文档模式", "Document mode")) {
                    InfoSegmented(
                        listOf(false to tr(language, "页面", "Page"), true to tr(language, "无界", "Edgeless")),
                        canvasMode,
                        onCanvasModeChange
                    )
                }
                DocInfoControlRow(AppIcon.RECORD, "Journal") {
                    val context = LocalContext.current
                    if (journalDate == null) {
                        InfoCheckBox(checked = false) { onOpenJournal(java.time.LocalDate.now().toString()) }
                    } else {
                        val parsed = runCatching { java.time.LocalDate.parse(journalDate) }.getOrNull()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                journalDateLabel(journalDate, language),
                                Modifier.clip(RoundedCornerShape(7.dp))
                                    .clickable {
                                        if (parsed != null) {
                                            android.app.DatePickerDialog(
                                                context,
                                                { _, year, month, day -> onJournalDateChange(java.time.LocalDate.of(year, month + 1, day).toString()) },
                                                parsed.year, parsed.monthValue - 1, parsed.dayOfMonth
                                            ).show()
                                        }
                                    }
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                                fontSize = 13.sp,
                                color = LingyeColors.Primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "×",
                                Modifier.clip(RoundedCornerShape(5.dp)).clickable { onJournalDateChange(null) }.padding(horizontal = 5.dp, vertical = 2.dp),
                                fontSize = 14.sp,
                                color = LingyeColors.Muted
                            )
                        }
                    }
                }
                DocInfoControlRow(AppIcon.SAVE, tr(language, "模板", "Template")) {
                    InfoCheckBox(checked = templatePage) { onTemplateChange(!templatePage) }
                }
                DocInfoValueRow(AppIcon.RECORD, tr(language, "创建时间", "Created"), formatDocTime(createdMillis, language))
                DocInfoValueRow(AppIcon.RECORD, tr(language, "更新时间", "Updated"), formatDocTime(updatedMillis, language))
                DocInfoControlRow(AppIcon.CARD, tr(language, "创建者", "Created by")) {
                    Box(Modifier.size(22.dp).background(LingyeColors.Primary, RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
                        Text(
                            tr(language, "本", "L"),
                            Modifier.fillMaxWidth().offset(y = (-0.5).dp),
                            color = Color.White,
                            fontSize = 11.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            style = LocalTextStyle.current.copy(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(tr(language, "本机", "This device"), fontSize = 13.sp, color = LingyeColors.Ink)
                }
                DocInfoControlRow(AppIcon.SPARK, tr(language, "无界配色方案", "Edgeless palette")) {
                    InfoSegmented(
                        listOf("auto" to tr(language, "自动", "Auto"), "light" to tr(language, "浅色", "Light"), "dark" to tr(language, "深色", "Dark")),
                        canvasTheme,
                        onCanvasThemeChange
                    )
                }
                DocInfoControlRow(AppIcon.FRAME, tr(language, "页面宽度", "Page width")) {
                    InfoSegmented(
                        listOf(false to tr(language, "标准", "Standard"), true to tr(language, "全宽", "Full width")),
                        fullWidth,
                        onFullWidthChange
                    )
                }
                visibleCustom.forEach { property ->
                    key(property.id) {
                        DocInfoCustomPropertyRow(
                            language = language,
                            property = property,
                            displayValue = displayValue(property),
                            onValueChange = { value ->
                                onPropertiesChange(properties.map { if (it.id == property.id) it.copy(value = value) else it })
                            }
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth().clickable(onClick = onAddProperty).padding(top = 6.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WorkspaceIcon(AppIcon.ADD, null, Modifier.size(17.dp), LingyeColors.Muted)
                    Spacer(Modifier.width(8.dp))
                    Text(tr(language, "添加属性", "Add property"), fontSize = 13.sp, color = LingyeColors.Muted)
                }
                Row(Modifier.fillMaxWidth().clickable { showMore = !showMore }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (showMore) "▾" else "▸", fontSize = 13.sp, color = LingyeColors.Muted)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (showMore) {
                            tr(language, "隐藏了 $hiddenPropertyCount 个属性", "$hiddenPropertyCount properties hidden")
                        } else {
                            tr(language, "还有 $hiddenPropertyCount 个属性", "$hiddenPropertyCount more properties")
                        },
                        fontSize = 13.sp,
                        color = LingyeColors.Muted
                    )
                }
                if (showMore) {
                    DocInfoValueRow(AppIcon.FOLDER, tr(language, "文件夹", "Folder"), page.folder.ifBlank { tr(language, "未分类", "Unfiled") })
                    DocInfoValueRow(AppIcon.FAVORITE, tr(language, "收藏", "Favorite"), if (page.favorite) tr(language, "是", "Yes") else tr(language, "否", "No"))
                    DocInfoValueRow(AppIcon.CARD, tr(language, "页面图标", "Page icon"), page.icon.ifBlank { tr(language, "未设置", "Not set") }, onEditIcon)
                    DocInfoValueRow(
                        AppIcon.TEXT,
                        tr(language, "字符数", "Characters"),
                        (page.title.length + page.text.length + page.blocks.sumOf { it.content.length }).toString()
                    )
                    DocInfoValueRow(AppIcon.FIELD, tr(language, "块数", "Blocks"), page.blocks.size.toString())
                    DocInfoValueRow(
                        AppIcon.IMAGE,
                        tr(language, "画布对象", "Canvas objects"),
                        (page.shapes.size + page.connectors.size + page.images.size + page.strokes.size).toString()
                    )
                    hiddenCustom.forEach { property ->
                        key(property.id) {
                            DocInfoCustomPropertyRow(
                                language = language,
                                property = property,
                                displayValue = displayValue(property),
                                onValueChange = { value ->
                                    onPropertiesChange(properties.map { if (it.id == property.id) it.copy(value = value) else it })
                                }
                            )
                        }
                    }
                }
            }
            HorizontalDivider(Modifier.padding(top = 12.dp), color = LingyeColors.Line)
        }
    }
}

@Composable
private fun DocInfoValueRow(icon: AppIcon, label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WorkspaceIcon(icon, null, Modifier.size(17.dp), LingyeColors.Muted.copy(alpha = 0.75f))
        Spacer(Modifier.width(10.dp))
        Text(label, Modifier.width(112.dp), fontSize = 13.sp, color = LingyeColors.Muted)
        val empty = value == "属性为空" || value == "Empty" || value == "未设置" || value == "Not set"
        Text(value, Modifier.weight(1f), fontSize = 13.sp, color = if (empty) LingyeColors.Muted else LingyeColors.Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun DocInfoCustomPropertyRow(
    language: AppLanguage,
    property: DocPropertyInfo,
    displayValue: String,
    onValueChange: (String) -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        DocPropertyGlyph(property, LingyeColors.Muted.copy(alpha = 0.75f), 17.dp)
        Spacer(Modifier.width(10.dp))
        Text(
            property.name.ifBlank {
                DocPropertyCatalog.type(property.type)?.let { DocPropertyCatalog.name(it, language) } ?: property.type
            },
            Modifier.width(112.dp),
            fontSize = 13.sp,
            color = LingyeColors.Muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        when (DocPropertyCatalog.valueKind(property.type)) {
            DocPropertyCatalog.KIND_CHECKBOX -> {
                val checked = property.value == "true"
                InfoEditableCheckbox(checked) { onValueChange(if (it) "true" else "false") }
                Spacer(Modifier.width(8.dp))
                Text(
                    if (checked) tr(language, "已勾选", "Checked") else tr(language, "未勾选", "Unchecked"),
                    fontSize = 13.sp,
                    color = if (checked) LingyeColors.Ink else LingyeColors.Muted
                )
            }
            DocPropertyCatalog.KIND_TEXT,
            DocPropertyCatalog.KIND_NUMBER,
            DocPropertyCatalog.KIND_DATE -> DocInfoValueField(language, property, onValueChange, Modifier.weight(1f))
            else -> {
                val empty = displayValue.isBlank()
                Text(
                    displayValue.ifBlank { tr(language, "属性为空", "Empty") },
                    Modifier.weight(1f),
                    fontSize = 13.sp,
                    color = if (empty) LingyeColors.Muted else LingyeColors.Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DocInfoValueField(
    language: AppLanguage,
    property: DocPropertyInfo,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var draft by remember(property.id) { mutableStateOf(property.value) }
    BasicTextField(
        value = draft,
        onValueChange = { raw ->
            val next = when (property.type) {
                "number" -> raw.filter { it.isDigit() || it == '.' || it == '-' }.take(20)
                else -> raw.take(4000)
            }
            draft = next
            onValueChange(next)
        },
        modifier = modifier.heightIn(min = 22.dp),
        singleLine = true,
        textStyle = TextStyle(fontSize = 13.sp, color = LingyeColors.Ink),
        cursorBrush = SolidColor(LingyeColors.Primary),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (property.type == "number") KeyboardType.Decimal else KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (draft.isEmpty()) {
                    Text(
                        if (property.type == "date") "YYYY-MM-DD" else tr(language, "属性为空", "Empty"),
                        fontSize = 13.sp,
                        color = LingyeColors.Muted
                    )
                }
                inner()
            }
        }
    )
}

@Composable
private fun InfoEditableCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Box(
        Modifier.size(18.dp)
            .background(if (checked) LingyeColors.Primary else Color.Transparent, RoundedCornerShape(4.dp))
            .border(1.5.dp, if (checked) LingyeColors.Primary else LingyeColors.Line, RoundedCornerShape(4.dp))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) CheckGlyph(Modifier.size(11.dp))
    }
}

/**
 * Check mark drawn from geometry instead of a font glyph, so its bounding box is centered in
 * the parent box regardless of font metrics, density or text scaling.
 */
@Composable
private fun CheckGlyph(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.16f, h * 0.52f)
            lineTo(w * 0.40f, h * 0.76f)
            lineTo(w * 0.84f, h * 0.24f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = w * 0.16f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

private fun docPropertyDisplayValue(
    property: DocPropertyInfo,
    page: Page,
    canvasMode: Boolean,
    fullWidth: Boolean,
    canvasTheme: String,
    createdMillis: Long,
    updatedMillis: Long,
    language: AppLanguage
): String = when (property.type) {
    "tags" -> page.tags.joinToString(" ") { "#$it" }
    "createdBy", "updatedBy" -> tr(language, "本机", "This device")
    "createdAt" -> formatDocTime(createdMillis, language)
    "updatedAt" -> formatDocTime(updatedMillis, language)
    "docPrimaryMode" -> if (canvasMode) tr(language, "无界", "Edgeless") else tr(language, "页面", "Page")
    "journal" -> tr(language, "否", "No")
    "template" -> tr(language, "否", "No")
    "edgelessTheme" -> when (canvasTheme) {
        "light" -> tr(language, "浅色", "Light")
        "dark" -> tr(language, "深色", "Dark")
        else -> tr(language, "自动", "Auto")
    }
    "pageWidth" -> if (fullWidth) tr(language, "全宽", "Full width") else tr(language, "标准", "Standard")
    else -> property.value
}

@Composable
private fun DocInfoControlRow(icon: AppIcon, label: String, content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        WorkspaceIcon(icon, null, Modifier.size(17.dp), LingyeColors.Muted.copy(alpha = 0.75f))
        Spacer(Modifier.width(10.dp))
        Text(label, Modifier.width(112.dp), fontSize = 13.sp, color = LingyeColors.Muted)
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, content = content)
    }
}

@Composable
private fun <T> InfoSegmented(options: List<Pair<T, String>>, selected: T, onSelected: (T) -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier
            .shadow(5.dp, shape)
            .clip(shape)
            .background(LingyeColors.SurfaceHigh)
            .border(1.dp, LingyeColors.SegmentBorder, shape)
            .height(34.dp)
            .padding(horizontal = 3.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { (value, label) ->
            val active = value == selected
            Surface(
                onClick = { onSelected(value) },
                modifier = Modifier.fillMaxHeight(),
                shape = RoundedCornerShape(15.dp),
                color = if (active) LingyeColors.SegmentSelected else Color.Transparent
            ) {
                Box(
                    Modifier.padding(horizontal = 12.dp).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (active) LingyeColors.OnSegmentSelected else LingyeColors.SegmentInk,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCheckBox(checked: Boolean = false, onClick: () -> Unit = {}) {
    Box(
        Modifier.size(17.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (checked) LingyeColors.Primary else Color.Transparent)
            .border(1.5.dp, if (checked) LingyeColors.Primary else LingyeColors.Line, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (checked) CheckGlyph(Modifier.size(11.dp))
    }
}
