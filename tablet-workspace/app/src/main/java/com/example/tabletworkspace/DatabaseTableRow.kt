package com.example.tabletworkspace

import android.app.DatePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
internal fun DatabaseTableRow(
    db: LocalDatabase,
    row: DatabaseRow,
    language: AppLanguage,
    onEdit: (DatabaseRow) -> Unit,
    onUpdate: (LocalDatabase) -> Unit
) {
    val context = LocalContext.current
    val index = db.rows.indexOfFirst { it.id == row.id }
    val actions: @Composable () -> Unit = {
        Row(Modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = { onUpdate(db.moveRecord(row.id, -1)) },
                enabled = db.sortField.isEmpty() && index > 0,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            ) { Text(tr(language, "上移", "Move up")) }
            TextButton(
                onClick = { onUpdate(db.moveRecord(row.id, 1)) },
                enabled = db.sortField.isEmpty() && index in 0 until db.rows.lastIndex,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            ) { Text(tr(language, "下移", "Move down")) }
            TextButton(
                onClick = { onUpdate(db.copy(rows = db.rows.filterNot { it.id == row.id })) },
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            ) { Text(tr(language, "删除", "Delete")) }
        }
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth < 720.dp) {
            Column {
                OutlinedTextField(row.title, { onEdit(row.copy(title = it.take(1000))) }, Modifier.fillMaxWidth(), label = { Text(tr(language, "名称", "Name")) }, singleLine = true)
                OutlinedTextField(statusLabel(language, row.status), { onEdit(row.copy(status = statusValue(language, it).take(1000))) }, Modifier.fillMaxWidth(), label = { Text(tr(language, "状态", "Status")) }, singleLine = true)
                actions()
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(row.title, { onEdit(row.copy(title = it.take(1000))) }, Modifier.weight(1f), label = { Text(tr(language, "名称", "Name")) }, singleLine = true)
                OutlinedTextField(statusLabel(language, row.status), { onEdit(row.copy(status = statusValue(language, it).take(1000))) }, Modifier.weight(1f), label = { Text(tr(language, "状态", "Status")) }, singleLine = true)
                actions()
            }
        }
    }
    db.fields.forEach { field ->
        when (field.type) {
            "date" -> Row(verticalAlignment = Alignment.CenterVertically) {
                Text(field.name + ": ")
                TextButton(onClick = {
                    val current = runCatching { LocalDate.parse(row.values[field.id].orEmpty()) }.getOrDefault(LocalDate.now())
                    DatePickerDialog(context, { _, year, month, day ->
                        onEdit(row.copy(values = row.values + (field.id to LocalDate.of(year, month + 1, day).toString())))
                    }, current.year, current.monthValue - 1, current.dayOfMonth).show()
                }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(row.values[field.id]?.ifEmpty { tr(language, "选择日期", "Select date") } ?: tr(language, "选择日期", "Select date"))
                }
                TextButton(onClick = { onEdit(row.copy(values = row.values + (field.id to ""))) }, modifier = Modifier.heightIn(min = 48.dp)) { Text(tr(language, "清除", "Clear")) }
            }
            "checkbox" -> Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(row.values[field.id] == "true", { onEdit(row.copy(values = row.values + (field.id to it.toString()))) })
                Text(field.name)
            }
            "number" -> NumericDatabaseField(row.values[field.id] ?: "", field.name) { value -> onEdit(row.copy(values = row.values + (field.id to value))) }
            "multiline" -> OutlinedTextField(row.values[field.id] ?: "", { value -> onEdit(row.copy(values = row.values + (field.id to value.take(1000)))) }, Modifier.fillMaxWidth(), label = { Text(field.name) }, minLines = 3, maxLines = 6)
            else -> OutlinedTextField(row.values[field.id] ?: "", { value -> onEdit(row.copy(values = row.values + (field.id to value.take(1000)))) }, Modifier.fillMaxWidth(), label = { Text(field.name) }, singleLine = true)
        }
    }
}
