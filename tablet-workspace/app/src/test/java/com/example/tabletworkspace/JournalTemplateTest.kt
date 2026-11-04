package com.example.tabletworkspace
import com.example.tabletworkspace.core.journalDateLabel

import org.junit.Assert.assertEquals
import org.junit.Test

class JournalTemplateTest {
    @Test fun journalDateLabelsUseLocale() {
        assertEquals("2026年11月3日", journalDateLabel("2026-11-03", AppLanguage.ZH))
        assertEquals("Nov 3, 2026", journalDateLabel("2026-11-03", AppLanguage.EN))
    }

    @Test fun invalidJournalDateFallsBackToInput() {
        assertEquals("not-a-date", journalDateLabel("not-a-date", AppLanguage.ZH))
    }
}
