package com.example.tabletworkspace

import kotlin.math.floor

data class WorkspaceInsight(
    val pages: Int,
    val trashedPages: Int,
    val contentCharacters: Int,
    val blocks: Int,
    val canvasObjects: Int,
    val strokes: Int,
    val cards: Int,
    val databases: Int,
    val images: Int,
    val folders: Int,
    val templates: Int
)

fun workspaceInsight(pages: List<Page>, folders: Int, templates: Int): WorkspaceInsight {
    val visible = pages.filterNot { it.trashed }
    return WorkspaceInsight(
        pages = visible.size,
        trashedPages = pages.count { it.trashed },
        contentCharacters = visible.sumOf { page ->
            page.title.length + page.text.length +
                page.blocks.filter { it.type != "image" && it.type != "database" }.sumOf { it.content.length } +
                page.shapes.filter { it.type == "card" }.sumOf { it.title.length + it.body.length }
        },
        blocks = visible.sumOf { it.blocks.size },
        canvasObjects = visible.sumOf { it.strokes.size + it.shapes.size + it.images.size + it.connectors.size },
        strokes = visible.sumOf { it.strokes.size },
        cards = visible.sumOf { page -> page.shapes.count { it.type == "card" } },
        databases = visible.sumOf { page -> page.blocks.count { it.type == "database" } },
        images = visible.sumOf { page -> page.images.size + page.blocks.count { it.type == "image" } },
        folders = folders,
        templates = templates
    )
}

fun activityBuckets(modifiedTimes: Collection<Long>, nowMillis: Long, days: Int = 365, dayMillis: Long = 86_400_000L): List<Int> {
    require(days in 1..730)
    require(dayMillis > 0)
    val today = floor(nowMillis.toDouble() / dayMillis).toLong()
    val first = today - days + 1
    val counts = IntArray(days)
    modifiedTimes.forEach { timestamp ->
        val day = floor(timestamp.toDouble() / dayMillis).toLong()
        val index = (day - first).toInt()
        if (index in counts.indices) counts[index]++
    }
    return counts.toList()
}
