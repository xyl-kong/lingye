package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkspaceInsightsTest {
    @Test fun overviewCountsVisibleContentAndCanvasObjects() {
        val page = Page.create("Page")
        page.text = "abc"
        page.addBlock("paragraph", "hello")
        page.addBlock("database", "encoded")
        page.addCard("笔记", 0f, 0f)
        page.addStroke(listOf(Point(0f, 0f), Point(1f, 1f)))
        val trashed = Page.create("Trash").apply { trashed = true }
        val insight = workspaceInsight(listOf(page, trashed), folders = 2, templates = 3)
        assertEquals(1, insight.pages)
        assertEquals(1, insight.trashedPages)
        assertEquals(21, insight.contentCharacters)
        assertEquals(2, insight.blocks)
        assertEquals(2, insight.canvasObjects)
        assertEquals(1, insight.strokes)
        assertEquals(1, insight.cards)
        assertEquals(1, insight.databases)
        assertEquals(2, insight.folders)
        assertEquals(3, insight.templates)
    }

    @Test fun activityUsesLast365LocalDayBuckets() {
        val day = 86_400_000L
        val now = day * 100 + 1_000L
        val buckets = activityBuckets(listOf(now, now - 2 * day, now - 400 * day), now)
        assertEquals(365, buckets.size)
        assertEquals(1, buckets.last())
        assertEquals(1, buckets[362])
        assertEquals(2, buckets.sum())
    }
}
