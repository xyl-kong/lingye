package com.example.tabletworkspace

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseInteractiveViewsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun boardMoveMenuReachesAnyColumn() {
        var db by mutableStateOf(LocalDatabase("Tasks", listOf(DatabaseRow("r1", "Task", "待办")), viewMode = "board"))
        compose.setContent { LingyeTheme { Box(Modifier.size(850.dp, 480.dp)) {
            DatabaseBoardView(db, db.rows, AppLanguage.ZH) { db = it }
        } } }
        compose.onNodeWithTag("board-card-r1").assertIsDisplayed()
        compose.onNodeWithText("移至其他列").performClick()
        compose.onNodeWithTag("board-move-完成").performClick()
        compose.runOnIdle { assertEquals("完成", db.rows.single().status) }
    }

    @Test fun boardLongPressDragMovesToAdjacentColumn() {
        var db by mutableStateOf(LocalDatabase("Tasks", listOf(DatabaseRow("r1", "Task", "待办")), viewMode = "board"))
        val movePixels = with(compose.density) { 236.dp.toPx() }
        compose.setContent { LingyeTheme { Box(Modifier.size(850.dp, 480.dp)) {
            DatabaseBoardView(db, db.rows, AppLanguage.ZH) { db = it }
        } } }
        compose.onNodeWithTag("board-card-r1").performTouchInput {
            down(center)
            advanceEventTime(700)
            moveBy(Offset(movePixels, 0f))
            up()
        }
        compose.runOnIdle { assertEquals("进行中", db.rows.single().status) }
    }

    @Test fun boardCanCreateCustomColumnAndRecord() {
        var db by mutableStateOf(LocalDatabase("Tasks", emptyList(), viewMode = "board"))
        compose.setContent { LingyeTheme { Box(Modifier.size(1000.dp, 480.dp)) {
            DatabaseBoardView(db, db.rows, AppLanguage.ZH) { db = it }
        } } }
        compose.onNodeWithTag("board-add-column").performClick()
        compose.onNodeWithTag("board-name-draft").performTextInput("Review")
        compose.onNodeWithTag("board-save").performClick()
        compose.runOnIdle { assertEquals(listOf("Review"), db.boardColumns) }
        compose.onNodeWithContentDescription("Review 列操作").assertExists()
        compose.onNodeWithTag("board-add-row-Review").performClick()
        compose.onNodeWithTag("board-name-draft").performTextInput("Inspect")
        compose.onNodeWithTag("board-save").performClick()
        compose.runOnIdle {
            assertEquals("Review", db.rows.single().status)
            assertEquals("Inspect", db.rows.single().title)
        }
    }

    @Test fun boardDropOutsideColumnsCancelsMove() {
        var db by mutableStateOf(LocalDatabase("Tasks", listOf(DatabaseRow("r1", "Task", "待办")), viewMode = "board"))
        compose.setContent { LingyeTheme { Box(Modifier.size(850.dp, 480.dp)) {
            DatabaseBoardView(db, db.rows, AppLanguage.ZH) { db = it }
        } } }
        compose.onNodeWithTag("board-card-r1").performTouchInput {
            down(center)
            advanceEventTime(700)
            moveBy(Offset(0f, -with(compose.density) { 300.dp.toPx() }))
            up()
        }
        compose.runOnIdle { assertEquals("待办", db.rows.single().status) }
    }

    @Test fun calendarCreatesAndEditsRecordInDayCell() {
        val field = DatabaseField("d", "日期", "date")
        var db by mutableStateOf(LocalDatabase("Tasks", emptyList(), listOf(field), viewMode = "calendar", calendarField = "d"))
        val day = YearMonth.now().atDay(1)
        compose.setContent { LingyeTheme { Box(Modifier.size(1000.dp, 650.dp)) {
            DatabaseCalendarView(db, "", AppLanguage.ZH, { db = it })
        } } }
        compose.onNodeWithTag("calendar-day-$day").performClick()
        compose.onNodeWithTag("calendar-title-draft").performTextInput("Release")
        compose.onNodeWithTag("calendar-save").performClick()
        compose.runOnIdle {
            assertEquals("Release", db.rows.single().title)
            assertEquals(day.toString(), db.rows.single().values["d"])
        }
        compose.onNodeWithTag("calendar-record-${db.rows.single().id}").performClick()
        compose.onNodeWithTag("calendar-title-draft").performTextReplacement("Release 2")
        compose.onNodeWithTag("calendar-save").performClick()
        compose.runOnIdle { assertEquals("Release 2", db.rows.single().title) }
    }

    @Test fun calendarLongPressDragMovesRecordAcrossMonthBoundary() {
        val field = DatabaseField("d", "日期", "date")
        var db by mutableStateOf(LocalDatabase("Tasks", listOf(DatabaseRow("r1", "Release", "待办", mapOf("d" to "2026-11-30"))), listOf(field), viewMode = "calendar", calendarField = "d"))
        compose.setContent { LingyeTheme { Box(Modifier.size(1000.dp, 800.dp)) {
            DatabaseCalendarView(db, "", AppLanguage.ZH, { db = it }, YearMonth.of(2026, 11))
        } } }
        val dayWidthPx = with(compose.density) { (1000.dp / 7).toPx() }
        compose.onNodeWithTag("calendar-record-r1").performTouchInput {
            down(center)
            advanceEventTime(700)
            moveBy(Offset(dayWidthPx, 0f))
            up()
        }
        compose.runOnIdle { assertEquals("2026-12-01", db.rows.single().values["d"]) }
    }

    @Test fun calendarShowsNextMonthDropTargetWhenMonthEndsOnSunday() {
        val field = DatabaseField("d", "日期", "date")
        val db = LocalDatabase("Tasks", listOf(DatabaseRow("r1", "Release", "待办", mapOf("d" to "2026-05-31"))), listOf(field), viewMode = "calendar", calendarField = "d")
        compose.setContent { LingyeTheme { Box(Modifier.size(1000.dp, 800.dp)) {
            DatabaseCalendarView(db, "", AppLanguage.ZH, {}, YearMonth.of(2026, 5))
        } } }
        compose.onNodeWithTag("calendar-day-2026-06-01").assertExists()
        compose.onNodeWithContentDescription("上个月").assertExists()
        compose.onNodeWithContentDescription("下个月").assertExists()
        compose.onNodeWithContentDescription("日历日期字段：日期").assertExists()
    }

    @Test fun narrowTableAtLargeFontKeepsActionsReachable() {
        val row = DatabaseRow("r1", "Task", "待办")
        val db = LocalDatabase("Tasks", listOf(row))
        compose.setContent { LingyeTheme {
            CompositionLocalProvider(LocalDensity provides Density(compose.density.density, 2f)) {
                Box(Modifier.size(480.dp, 900.dp)) {
                    DatabaseTableRow(db, row, AppLanguage.ZH, {}, {})
                }
            }
        } }
        compose.onNodeWithText("名称").assertIsDisplayed()
        compose.onNodeWithText("状态").assertIsDisplayed()
        compose.onNodeWithText("删除").assertIsDisplayed()
        val bounds = compose.onNodeWithText("删除").fetchSemanticsNode().boundsInRoot
        val width = with(compose.density) { 480.dp.toPx() }
        org.junit.Assert.assertTrue(bounds.right <= width + 1f)
    }
}
