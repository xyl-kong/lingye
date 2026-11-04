package com.example.tabletworkspace
import com.example.tabletworkspace.core.formatDocTime
import com.example.tabletworkspace.core.formatModified
import com.example.tabletworkspace.core.formatOverviewDate
import com.example.tabletworkspace.core.formatShortMonthDay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateTimeFormatsTest {
    @Test fun docTimeUsesTodayAndUnknown() {
        val now = 1_700_000_000_000L
        assertEquals("今天", formatDocTime(now, AppLanguage.ZH, now))
        assertEquals("Today", formatDocTime(now, AppLanguage.EN, now))
        assertEquals("未知", formatDocTime(0L, AppLanguage.ZH))
        assertEquals("Unknown", formatDocTime(0L, AppLanguage.EN))
    }

    @Test fun modifiedTimeHandlesMissingAndPositiveValues() {
        assertEquals("未记录", formatModified(null, AppLanguage.ZH))
        assertEquals("Not recorded", formatModified(0L, AppLanguage.EN))
        assertTrue(formatModified(1_700_000_000_000L, AppLanguage.ZH).matches(Regex("\\d{2}-\\d{2} \\d{2}:\\d{2}")))
    }

    @Test fun overviewDateAndShortMonthDayHandleMissingValues() {
        assertEquals("未知", formatOverviewDate(null, AppLanguage.ZH))
        assertEquals("Unknown", formatOverviewDate(0L, AppLanguage.EN))
        assertEquals("", formatShortMonthDay(0L))
        assertTrue(formatShortMonthDay(1_700_000_000_000L).matches(Regex("\\d{1,2}/\\d{1,2}")))
    }
}
