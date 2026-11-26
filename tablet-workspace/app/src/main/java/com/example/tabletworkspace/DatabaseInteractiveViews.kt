package com.example.tabletworkspace

import android.app.DatePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@Composable
internal fun DatabaseBoardView(
    db: LocalDatabase,
    rows: List<DatabaseRow>,
    language: AppLanguage,
    onUpdate: (LocalDatabase) -> Unit
) {
    val statuses = (listOf("待办", "进行中", "完成") + rows.map { it.status.ifBlank { "未设置" } }).distinct()
    val columnBounds = remember { mutableStateMapOf<String, Rect>() }
    val cardBounds = remember { mutableStateMapOf<String, Rect>() }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragPosition by remember { mutableStateOf<Offset?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var targetStatus by remember { mutableStateOf<String?>(null) }
    var moveMenuId by remember { mutableStateOf<String?>(null) }
    fun resetDrag() { draggingId = null; dragPosition = null; dragOffset = Offset.Zero; targetStatus = null }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        statuses.forEach { status ->
            val active = targetStatus == status
            Column(
                Modifier.width(236.dp).heightIn(min = 180.dp).padding(4.dp)
                    .onGloballyPositioned { columnBounds[status] = it.boundsInWindow() }
            ) {
                Text(statusLabel(language, status), fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                rows.filter { it.status.ifBlank { "未设置" } == status }.forEach { row ->
                    val dragging = draggingId == row.id
                    Surface(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            .onGloballyPositioned { cardBounds[row.id] = it.boundsInWindow() }
                            .zIndex(if (dragging) 2f else 0f)
                            .graphicsLayer {
                                translationX = if (dragging) dragOffset.x else 0f
                                translationY = if (dragging) dragOffset.y else 0f
                                alpha = if (dragging) 0.88f else 1f
                            }
                            .pointerInput(row.id, statuses) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { start ->
                                        draggingId = row.id
                                        dragOffset = Offset.Zero
                                        dragPosition = cardBounds[row.id]?.topLeft?.plus(start)
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragOffset += amount
                                        dragPosition = dragPosition?.plus(amount)
                                        targetStatus = dragPosition?.let { point ->
                                            statuses.firstOrNull { columnBounds[it]?.contains(point) == true }
                                        }
                                    },
                                    onDragEnd = {
                                        val destination = targetStatus
                                        if (destination != null && destination != status) {
                                            onUpdate(db.moveRow(row.id, if (destination == "未设置") "" else destination))
                                        }
                                        resetDrag()
                                    },
                                    onDragCancel = { resetDrag() }
                                )
                            }
                            .testTag("board-card-${row.id}")
                            .semantics {
                                contentDescription = tr(language, "记录 ${row.title}，长按拖到其他列", "Record ${row.title}, long press to move to another column")
                            },
                        shape = MaterialTheme.shapes.small,
                        color = if (active) LingyeColors.SurfaceSoft else LingyeColors.SurfaceBlue
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text(row.title.ifBlank { tr(language, "未命名文档", "Untitled") }, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Box {
                                TextButton(
                                    onClick = { moveMenuId = row.id },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) { Text(tr(language, "移至其他列", "Move to column")) }
                                LingyeDropdownMenu(expanded = moveMenuId == row.id, onDismissRequest = { moveMenuId = null }) {
                                    statuses.filterNot { it == status }.forEach { destination ->
                                        DropdownMenuItem(
                                            text = { Text(statusLabel(language, destination)) },
                                            modifier = Modifier.testTag("board-move-$destination"),
                                            onClick = {
                                                onUpdate(db.moveRow(row.id, if (destination == "未设置") "" else destination))
                                                moveMenuId = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (active) Text(tr(language, "松开以移动到此列", "Release to move here"), Modifier.padding(8.dp), color = LingyeColors.Muted)
            }
        }
    }
}

@Composable
internal fun DatabaseCalendarView(
    db: LocalDatabase,
    query: String,
    language: AppLanguage,
    onUpdate: (LocalDatabase) -> Unit
) {
    val context = LocalContext.current
    val dateFields = db.fields.filter { it.type == "date" }
    val selectedField = dateFields.firstOrNull { it.id == db.calendarField } ?: dateFields.firstOrNull()
    var month by remember { mutableStateOf(YearMonth.now()) }
    var fieldMenu by remember { mutableStateOf(false) }
    var createDate by remember { mutableStateOf<LocalDate?>(null) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var draftTitle by remember { mutableStateOf("") }
    var draftDate by remember { mutableStateOf(LocalDate.now()) }
    val editingRow = db.rows.firstOrNull { it.id == editingId }

    if (selectedField == null) {
        Text(tr(language, "请先添加日期字段。", "Add a date field first."))
        TextButton(onClick = {
            val field = DatabaseField(UUID.randomUUID().toString(), tr(language, "日期", "Date"), "date")
            onUpdate(db.copy(fields = db.fields + field, calendarField = field.id))
        }, enabled = db.fields.size < 40) { Text(tr(language, "添加日期字段", "Add date field")) }
        return
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { month = month.minusMonths(1) }, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) { Text("‹") }
        Text(month.toString())
        TextButton(onClick = { month = month.plusMonths(1) }, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) { Text("›") }
        Box {
            TextButton(onClick = { fieldMenu = true }) { Text(selectedField.name + " ▾") }
            LingyeDropdownMenu(expanded = fieldMenu, onDismissRequest = { fieldMenu = false }) {
                dateFields.forEach { field -> DropdownMenuItem(text = { Text(field.name) }, onClick = {
                    onUpdate(db.copy(calendarField = field.id)); fieldMenu = false
                }) }
            }
        }
    }
    val entries = db.calendar(selectedField.id, month, query)
    val first = month.atDay(1).dayOfWeek.value - 1
    val count = month.lengthOfMonth()
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val calendarWidth = maxWidth.coerceAtLeast(840.dp)
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            (0 until ((first + count + 6) / 7)).forEach { week ->
                Row(Modifier.width(calendarWidth)) {
                    (0..6).forEach { day ->
                        val number = week * 7 + day - first + 1
                        Column(Modifier.weight(1f).heightIn(min = 108.dp).padding(3.dp)) {
                            if (number in 1..count) {
                                val date = month.atDay(number)
                                TextButton(
                                    onClick = { draftTitle = ""; draftDate = date; createDate = date },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                        .testTag("calendar-day-$date")
                                        .semantics { contentDescription = tr(language, "$date，新建记录", "$date, add record") }
                                ) { Text("$number  +") }
                                entries[date].orEmpty().forEach { row ->
                                    TextButton(
                                        onClick = {
                                            editingId = row.id
                                            draftTitle = row.title
                                            draftDate = date
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                            .testTag("calendar-record-${row.id}")
                                            .semantics { contentDescription = tr(language, "编辑 ${row.title}，$date", "Edit ${row.title}, $date") }
                                    ) { Text(row.title.ifBlank { tr(language, "未命名文档", "Untitled") }, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (createDate != null || editingRow != null) {
        AlertDialog(
            onDismissRequest = { createDate = null; editingId = null },
            title = { Text(if (editingRow == null) tr(language, "新建记录", "New record") else tr(language, "编辑记录", "Edit record")) },
            text = { Column {
                OutlinedTextField(draftTitle, { draftTitle = it.take(1000) }, modifier = Modifier.testTag("calendar-title-draft"), label = { Text(tr(language, "名称", "Name")) }, singleLine = true)
                TextButton(onClick = {
                    DatePickerDialog(context, { _, year, monthIndex, day ->
                        draftDate = LocalDate.of(year, monthIndex + 1, day)
                    }, draftDate.year, draftDate.monthValue - 1, draftDate.dayOfMonth).show()
                }, modifier = Modifier.heightIn(min = 48.dp)) { Text(tr(language, "日期：", "Date: ") + draftDate) }
            } },
            confirmButton = { TextButton(onClick = {
                val row = editingRow
                if (row == null) onUpdate(db.addRowOnDate(selectedField.id, draftDate, draftTitle))
                else onUpdate(db.copy(rows = db.rows.map { if (it.id == row.id) it.copy(title = draftTitle, values = it.values + (selectedField.id to draftDate.toString())) else it }))
                month = YearMonth.from(draftDate)
                createDate = null; editingId = null
            }, modifier = Modifier.testTag("calendar-save")) { Text(tr(language, "保存", "Save")) } },
            dismissButton = { TextButton(onClick = { createDate = null; editingId = null }) { Text(tr(language, "取消", "Cancel")) } }
        )
    }
}
