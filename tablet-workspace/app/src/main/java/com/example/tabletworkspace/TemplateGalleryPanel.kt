package com.example.tabletworkspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

private class TemplateGalleryPositionProvider(
    private val anchor: IntRect,
    private val gapPx: Int
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(8)
        val x = (anchor.right - popupContentSize.width).coerceIn(8, maxX)
        val above = anchor.top - popupContentSize.height - gapPx
        val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(8)
        val y = if (above >= 8) above else (anchor.bottom + gapPx).coerceIn(8, maxY)
        return IntOffset(x, y)
    }
}

@Composable
private fun GalleryIconButton(icon: AppIcon, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        WorkspaceIcon(icon, label, Modifier.size(18.dp), LingyeColors.Muted)
    }
}

@Composable
fun TemplateGalleryPopup(
    anchor: IntRect,
    initialCategory: String,
    language: AppLanguage,
    workspaceTemplates: List<CardTemplate>,
    pageTemplates: List<CardTemplate>,
    onDismiss: () -> Unit,
    onInsert: (GalleryTemplate) -> Unit,
    onInsertCard: (CardPreset) -> Unit,
    onAddImage: () -> Unit,
    onAddDatabase: () -> Unit,
    onInsertWorkspace: (CardTemplate) -> Unit,
    onInsertPageTemplate: (Int) -> Unit,
    onManageWorkspaceTemplates: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val panelWidth = minOf(620.dp, configuration.screenWidthDp.dp * 0.94f)
    val panelHeight = minOf(650.dp, configuration.screenHeightDp.dp * 0.82f)
    val gapPx = with(density) { 8.dp.roundToPx() }
    var categoryKey by remember(initialCategory) { mutableStateOf(initialCategory) }
    var query by remember { mutableStateOf("") }
    val cardsTab = categoryKey == "__cards"
    val workspaceTab = categoryKey == "__workspace"
    val hasWorkspace = workspaceTemplates.isNotEmpty() || pageTemplates.isNotEmpty()
    val category = TemplateGalleryData.category(categoryKey)
    val templates = remember(categoryKey, query) {
        if (query.isBlank()) category.templates
        else TemplateGalleryData.categories.flatMap { c -> c.templates.filter { it.title.contains(query, ignoreCase = true) || c.name.contains(query, ignoreCase = true) } }
    }
    val cards = remember(cardsTab, query, language) {
        CardPreset.entries.filter { preset ->
            val content = preset.content(language)
            query.isBlank() || content.title.contains(query, ignoreCase = true) || content.body.contains(query, ignoreCase = true)
        }
    }
    val provider = remember(anchor, gapPx) { TemplateGalleryPositionProvider(anchor, gapPx) }
    Popup(
        popupPositionProvider = provider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            Modifier.width(panelWidth).height(panelHeight),
            shape = RoundedCornerShape(18.dp),
            color = LingyeColors.Subtle,
            border = BorderStroke(1.dp, LingyeColors.Line.copy(alpha = 0.8f)),
            shadowElevation = 14.dp
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    Modifier.fillMaxWidth().height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WorkspaceIcon(AppIcon.SEARCH, null, Modifier.size(18.dp), LingyeColors.Muted)
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                tr(language, "搜索模板或文件…", "Search file or anything…"),
                                color = LingyeColors.Muted, fontSize = 14.sp, maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it.take(80) },
                            singleLine = true,
                            textStyle = TextStyle(color = LingyeColors.Ink, fontSize = 14.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (query.isNotEmpty()) {
                        Text(
                            "×", fontSize = 18.sp, color = LingyeColors.Muted,
                            modifier = Modifier.clickable { query = "" }.padding(horizontal = 4.dp)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    GalleryIconButton(AppIcon.IMAGE, tr(language, "图片", "Image"), onAddImage)
                    Spacer(Modifier.width(2.dp))
                    GalleryIconButton(AppIcon.DATABASE, tr(language, "数据库", "Database"), onAddDatabase)
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TemplateGalleryData.categories.forEach { item ->
                        val selected = item.key == categoryKey && query.isBlank()
                        Surface(
                            onClick = { categoryKey = item.key; query = "" },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) Color.White else Color.Transparent,
                            shadowElevation = if (selected) 1.dp else 0.dp
                        ) {
                            Text(
                                item.name,
                                Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) LingyeColors.Ink else LingyeColors.Muted,
                                maxLines = 1
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    Surface(
                        onClick = { categoryKey = "__cards"; query = "" },
                        shape = RoundedCornerShape(8.dp),
                        color = if (cardsTab) Color.White else Color.Transparent,
                        shadowElevation = if (cardsTab) 1.dp else 0.dp
                    ) {
                        Text(
                            "Cards",
                            Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            fontSize = 12.sp,
                            fontWeight = if (cardsTab) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (cardsTab) LingyeColors.Ink else LingyeColors.Muted,
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    if (hasWorkspace) {
                        Surface(
                            onClick = { categoryKey = "__workspace"; query = "" },
                            shape = RoundedCornerShape(8.dp),
                            color = if (workspaceTab) Color.White else Color.Transparent,
                            shadowElevation = if (workspaceTab) 1.dp else 0.dp
                        ) {
                            Text(
                                "Workspace",
                                Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                fontSize = 12.sp,
                                fontWeight = if (workspaceTab) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (workspaceTab) LingyeColors.Ink else LingyeColors.Muted,
                                maxLines = 1
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
                if (workspaceTab) {
                    Column(
                        Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (workspaceTemplates.isNotEmpty()) {
                            Text(
                                tr(language, "工作区模板", "Workspace templates"),
                                Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LingyeColors.Muted
                            )
                            workspaceTemplates.forEach { template ->
                                CanvasMenuRow(tr(language, "工作区：", "Workspace: ") + template.name) {
                                    onInsertWorkspace(template)
                                }
                            }
                        }
                        if (pageTemplates.isNotEmpty()) {
                            Text(
                                tr(language, "旧页面模板", "Page templates"),
                                Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LingyeColors.Muted
                            )
                            pageTemplates.forEachIndexed { index, template ->
                                CanvasMenuRow(tr(language, "页面模板：", "Page template: ") + template.name) {
                                    onInsertPageTemplate(index)
                                }
                            }
                        }
                        if (workspaceTemplates.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            CanvasMenuRow(tr(language, "管理工作区模板", "Manage workspace templates")) {
                                onManageWorkspaceTemplates()
                            }
                        }
                    }
                } else if (cardsTab) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(cards, key = { it.name }) { preset ->
                            val content = preset.content(language)
                            Surface(
                                onClick = { onInsertCard(preset) },
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, LingyeColors.Line.copy(alpha = 0.55f))
                            ) {
                                Column(Modifier.fillMaxWidth().height(104.dp).padding(12.dp)) {
                                    Text(
                                        content.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LingyeColors.Ink,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        content.body.replace("\n", " · "),
                                        fontSize = 11.sp,
                                        color = LingyeColors.Muted,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } else if (templates.isEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            tr(language, "没有匹配的模板", "No templates found"),
                            color = LingyeColors.Muted, fontSize = 13.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(templates, key = { "${categoryKey}:${it.resId}" }) { template ->
                            Surface(
                                onClick = { onInsert(template) },
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, LingyeColors.Line.copy(alpha = 0.55f))
                            ) {
                                Box(
                                    Modifier.fillMaxWidth().height(104.dp).padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painterResource(template.resId),
                                        contentDescription = template.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
