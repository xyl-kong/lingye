@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.tabletworkspace
import com.example.tabletworkspace.data.images.ImageCacheStats

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class SettingsCategory {
    APPEARANCE, EDITOR, SHORTCUTS, PREFERENCES, STORAGE, ABOUT
}

@Composable
fun SettingsPage(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    settings: WorkspaceSettings,
    onSettingsChange: (WorkspaceSettings) -> Unit,
    penOnly: Boolean,
    onPenOnlyChange: (Boolean) -> Unit,
    inkColor: Int,
    onInkColorChange: (Int) -> Unit,
    inkWidth: Float,
    onInkWidthChange: (Float) -> Unit,
    pageCount: Int,
    trashedPageCount: Int,
    folderCount: Int,
    templateCount: Int,
    storageBytes: Long,
    unusedImageCount: Int,
    unusedImageBytes: Long,
    imageCache: ImageCacheStats,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onImportMarkdown: () -> Unit,
    onExportMarkdown: () -> Unit,
    onCleanupImages: () -> Unit,
    onClose: () -> Unit
) {
    fun text(zh: String, en: String) = tr(language, zh, en)
    var category by remember { mutableStateOf(SettingsCategory.APPEARANCE) }
    var compactDetail by remember { mutableStateOf(false) }

    val categoryContent: @Composable () -> Unit = {
        when (category) {
            SettingsCategory.APPEARANCE -> AppearanceSettings(language, onLanguageChange, settings, onSettingsChange)
            SettingsCategory.EDITOR -> EditorSettings(
                language, settings, onSettingsChange, penOnly, onPenOnlyChange,
                inkColor, onInkColorChange, inkWidth, onInkWidthChange
            )
            SettingsCategory.SHORTCUTS -> ShortcutSettings(language)
            SettingsCategory.PREFERENCES -> WorkspaceSettingsContent(language, settings, onSettingsChange)
            SettingsCategory.STORAGE -> DataSettings(
                language, pageCount, trashedPageCount, folderCount, templateCount,
                storageBytes, unusedImageCount, unusedImageBytes, imageCache,
                onBackup, onRestore, onImportMarkdown, onExportMarkdown, onCleanupImages
            )
            SettingsCategory.ABOUT -> AboutSettings(language, storageBytes)
        }
    }
    val footer: @Composable () -> Unit = {
        Text(
            text("灵页 · 离线优先的文档与白板工作区", "Lingye · offline-first documents and whiteboard"),
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            textAlign = TextAlign.Center,
            color = LingyeColors.Muted.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxWidth < 520.dp
        val narrow = maxWidth < 720.dp
        val sidebarWidth = if (narrow) 176.dp else 248.dp
        val contentStart = if (narrow) 18.dp else 28.dp
        val contentEnd = if (narrow) 46.dp else 44.dp

        if (compact) {
            BackHandler(enabled = compactDetail) { compactDetail = false }
            if (compactDetail) {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsBackButton(text("返回", "Back")) { compactDetail = false }
                        Spacer(Modifier.width(4.dp))
                        Text(text("设置", "Settings"), fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = LingyeColors.Ink)
                        Spacer(Modifier.weight(1f))
                        SettingsCloseButton(onClose, text("关闭设置", "Close settings"))
                    }
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState())
                            .padding(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 8.dp)
                    ) {
                        categoryContent()
                    }
                    footer()
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text("设置", "Settings"), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = LingyeColors.Ink)
                        Spacer(Modifier.weight(1f))
                        SettingsCloseButton(onClose, text("关闭设置", "Close settings"))
                    }
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            Spacer(Modifier.height(6.dp))
                            SettingsWorkspaceHeader(settings, language, iconSize = 38.dp)
                            Spacer(Modifier.height(18.dp))
                            SettingsNavGroups(language, category, compact = true) {
                                category = it
                                compactDetail = true
                            }
                            Spacer(Modifier.height(18.dp))
                        }
                    }
                    footer()
                }
            }
        } else {
            Row(Modifier.fillMaxSize()) {
                Column(
                    Modifier.width(sidebarWidth).fillMaxHeight().background(LingyeColors.SettingsSidebar)
                        .padding(horizontal = if (narrow) 8.dp else 10.dp, vertical = 10.dp)
                ) {
                    Text(text("设置", "Settings"), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = LingyeColors.Ink)
                    Spacer(Modifier.height(10.dp))
                    SettingsWorkspaceHeader(settings, language, iconSize = if (narrow) 34.dp else 38.dp)
                    Spacer(Modifier.height(18.dp))
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        SettingsNavGroups(language, category, compact = false) { category = it }
                    }
                }
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    SettingsCloseButton(
                        onClose,
                        text("关闭设置", "Close settings"),
                        Modifier.align(Alignment.TopEnd).padding(8.dp).zIndex(2f)
                    )
                    Column(Modifier.fillMaxSize()) {
                        Column(
                            Modifier.weight(1f).verticalScroll(rememberScrollState())
                                .padding(start = contentStart, end = contentEnd, top = 12.dp, bottom = 8.dp)
                        ) {
                            categoryContent()
                        }
                        footer()
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsWorkspaceHeader(settings: WorkspaceSettings, language: AppLanguage, iconSize: Dp) {
    fun text(zh: String, en: String) = tr(language, zh, en)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(iconSize).clip(RoundedCornerShape(11.dp))) {
            Image(painterResource(R.drawable.ic_launcher_background), contentDescription = null, modifier = Modifier.fillMaxSize())
            Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(settings.workspaceName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LingyeColors.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Spacer(Modifier.width(6.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = LingyeColors.PrimarySoft) {
                    Text(text("本机", "Local"), Modifier.padding(horizontal = 5.dp, vertical = 2.dp), fontSize = 9.sp, color = LingyeColors.Primary)
                }
            }
            Text(settings.workspaceSubtitle, fontSize = 11.sp, color = LingyeColors.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SettingsNavGroups(
    language: AppLanguage,
    category: SettingsCategory,
    compact: Boolean,
    onSelect: (SettingsCategory) -> Unit
) {
    fun text(zh: String, en: String) = tr(language, zh, en)
    SettingsNavGroupLabel(text("常规", "General"))
    SettingsNavItem(text("外观", "Appearance"), AppIcon.LANGUAGE, category == SettingsCategory.APPEARANCE, compact) { onSelect(SettingsCategory.APPEARANCE) }
    SettingsNavItem(text("编辑器", "Editor"), AppIcon.EDIT, category == SettingsCategory.EDITOR, compact) { onSelect(SettingsCategory.EDITOR) }
    SettingsNavItem(text("键盘快捷键", "Keyboard shortcuts"), AppIcon.MENU, category == SettingsCategory.SHORTCUTS, compact) { onSelect(SettingsCategory.SHORTCUTS) }
    SettingsNavItem(text("关于灵页", "About Lingye"), AppIcon.INFO, category == SettingsCategory.ABOUT, compact) { onSelect(SettingsCategory.ABOUT) }
    Spacer(Modifier.height(12.dp))
    SettingsNavGroupLabel(text("工作区", "Workspace"))
    SettingsNavItem(text("偏好设置", "Preferences"), AppIcon.SETTINGS, category == SettingsCategory.PREFERENCES, compact) { onSelect(SettingsCategory.PREFERENCES) }
    SettingsNavItem(text("储存", "Storage"), AppIcon.BACKUP, category == SettingsCategory.STORAGE, compact) { onSelect(SettingsCategory.STORAGE) }
}

@Composable
private fun SettingsBackButton(description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(18.dp)) {
            val strokeWidth = 2.dp.toPx()
            drawLine(
                color = LingyeColors.Ink,
                start = Offset(size.width * 0.62f, size.height * 0.2f),
                end = Offset(size.width * 0.36f, size.height * 0.5f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = LingyeColors.Ink,
                start = Offset(size.width * 0.36f, size.height * 0.5f),
                end = Offset(size.width * 0.62f, size.height * 0.8f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun SettingsCloseButton(onClick: () -> Unit, description: String, modifier: Modifier = Modifier) {
    Box(
        modifier.size(32.dp)
            .shadow(4.dp, CircleShape)
            .background(LingyeColors.SurfaceHigh, CircleShape)
            .border(1.dp, LingyeColors.Line.copy(alpha = 0.7f), CircleShape)
            .clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("✕", fontSize = 18.sp, color = LingyeColors.Muted, modifier = Modifier.semantics { contentDescription = description })
    }
}

@Composable
private fun SettingsNavGroupLabel(label: String) {
    Text(
        label,
        Modifier.padding(start = 12.dp, top = 10.dp, bottom = 4.dp),
        color = LingyeColors.Muted.copy(alpha = 0.75f),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SettingsNavItem(label: String, icon: AppIcon, selected: Boolean, compact: Boolean = false, onClick: () -> Unit) {
    val background by animateColorAsState(
        if (selected) LingyeColors.NavSelected else Color.Transparent,
        tween(160),
        label = "settings-nav"
    )
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = background,
        shadowElevation = 0.dp
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = if (compact) 11.dp else 8.dp), verticalAlignment = Alignment.CenterVertically) {
            WorkspaceIcon(icon, null, Modifier.size(17.dp), if (selected) LingyeColors.Primary else LingyeColors.Muted)
            Spacer(Modifier.width(8.dp))
            Text(
                label,
                color = if (selected) LingyeColors.Ink else LingyeColors.Muted,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = if (compact) 14.sp else 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AppearanceSettings(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    settings: WorkspaceSettings,
    onSettingsChange: (WorkspaceSettings) -> Unit
) {
    fun text(zh: String, en: String) = tr(language, zh, en)
    SettingsHeading(text("外观设置", "Appearance"), text("灵页界面个性化设置。", "Personalize the Lingye interface."))
    SettingsGroup(text("主题", "Theme")) {
        SettingsControlRow(
            text("界面主题", "Interface theme"),
            text("跟随系统或手动选择浅色、深色。", "Follow the system, or choose light or dark."),
            help = text("深色主题应用到侧栏、文档、画布工具栏和设置；画布内容底色仍由“编辑器 → 白板 → 画布主题”单独控制。", "Dark theme applies to the sidebar, documents, canvas toolbars and settings. Canvas content colors remain controlled by Editor → Whiteboard → Canvas theme.")
        ) {
            SegmentedChoice(
                listOf(
                    "system" to text("跟随系统", "System"),
                    "light" to text("浅色", "Light"),
                    "dark" to text("深色", "Dark")
                ),
                settings.themeMode
            ) { onSettingsChange(settings.copy(themeMode = it).normalized()) }
        }
    }
    SettingsDivider()
    SettingsGroup(text("语言", "Language")) {
        SettingsControlRow(
            text("显示语言", "Display language"),
            text("选择界面语言。", "Choose the interface language."),
            help = text("语言只影响应用界面与设置文案，不会翻译你输入的标题、正文或画布文字。", "Language only affects the app UI and settings. Your titles, text and canvas content are not translated.")
        ) {
            SettingsDropdown(
                listOf(AppLanguage.ZH to "简体中文", AppLanguage.EN to "English"),
                language
            ) { onLanguageChange(it) }
        }
    }
    SettingsDivider()
    SettingsGroup(text("界面", "Interface")) {
        SettingsToggleRow(
            text("动效", "Motion effects"),
            text("控制连线光点和界面切换动效。", "Controls connector highlights and transition motion."),
            settings.motionEnabled,
            help = text("关闭后减少连线光点与切换动画，适合追求稳定或省电的场景；不影响编辑功能。", "Turn off to reduce connector highlights and transitions; editing is unaffected.")
        ) { onSettingsChange(settings.copy(motionEnabled = it)) }
    }
    SettingsDivider()
    SettingsGroup(text("图片", "Images")) {
        SettingsToggleRow(
            text("平滑图片渲染", "Smooth image rendering"),
            text("关闭后，画布缩放时优先保持像素边缘。", "When off, canvas images use nearest-neighbor scaling."),
            settings.smoothImages,
            help = text("开启时缩放图片使用平滑采样，适合照片；关闭时保留像素边缘，适合截图和线稿。", "On uses smooth sampling for photos; off keeps crisp pixel edges for screenshots and line art.")
        ) { onSettingsChange(settings.copy(smoothImages = it)) }
    }
    SettingsDivider()
    SettingsGroup(text("链接", "Links"), help = text("控制从画布链接卡片进入其他页面后的返回行为。", "Controls how returning works after opening a linked page.")) {
        SettingsControlRow(
            text("打开页面链接", "Open page links"),
            text("选择打开后是否保留返回来源。", "Choose whether opening a link keeps a return path."),
            help = text("“返回来源”会记录来源页面与视图，按返回键可回到原画布；“直接打开”不记录来源。", "Return to source remembers the origin page and view so Back returns there; Direct open does not.")
        ) {
            SettingsDropdown(
                listOf(true to text("返回来源", "Return to source"), false to text("直接打开", "Direct open")),
                settings.linkReturnToSource
            ) { onSettingsChange(settings.copy(linkReturnToSource = it)) }
        }
    }
    SettingsDivider()
    SettingsGroup(text("侧边栏", "Sidebar")) {
        SettingsToggleRow(
            text("在侧边栏中显示链接的文档", "Show linked documents in sidebar"),
            text("当前页面的链接卡片指向的文档会出现在侧栏。", "Documents referenced by this page's link cards appear in the sidebar."),
            settings.showLinkedDocs,
            help = text("只列出当前页面画布上“链接已有文档”卡片指向且未删除的页面；不会额外写入页面数据。", "Lists pages referenced by this page's link cards that are not in trash; no extra page data is written.")
        ) { onSettingsChange(settings.copy(showLinkedDocs = it)) }
    }
}

@Composable
private fun EditorSettings(
    language: AppLanguage,
    settings: WorkspaceSettings,
    onSettingsChange: (WorkspaceSettings) -> Unit,
    penOnly: Boolean,
    onPenOnlyChange: (Boolean) -> Unit,
    inkColor: Int,
    onInkColorChange: (Int) -> Unit,
    inkWidth: Float,
    onInkWidthChange: (Float) -> Unit
) {
    fun text(zh: String, en: String) = tr(language, zh, en)
    var editorTab by remember { mutableStateOf("document") }
    SettingsHeading(
        text("编辑器设置", "Editor settings"),
        text("文档与白板的默认值；修改只影响之后新建的内容。", "Defaults for documents and whiteboards; changes apply to new content.")
    )
    SegmentedChoice(
        listOf("document" to text("文档", "Document"), "whiteboard" to text("白板", "Whiteboard")),
        editorTab
    ) { editorTab = it }
    Spacer(Modifier.height(20.dp))
    if (editorTab == "document") {
        SettingsGroup(text("排版", "Typography")) {
            SettingsStackRow(
                text("正文字号", "Base font size"),
                text("应用于标题、正文和文本块。", "Applies to title, body and text blocks."),
                help = text("字号只影响文档阅读与编辑，不改变画布对象和已有图片。", "Font size only affects document reading and editing; canvas objects and images are unchanged.")
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    CompactSlider(
                        value = settings.editorFontSize,
                        onValueChange = { onSettingsChange(settings.copy(editorFontSize = it).normalized()) },
                        valueRange = 12f..24f,
                        steps = 11,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("${settings.editorFontSize.roundToInt()} px", color = LingyeColors.Primary, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(8.dp))
            SettingsControlRow(
                text("正文字体", "Body font"),
                text("用于标题、正文和文本块。", "Used for title, body and text blocks."),
                help = text("无衬线适合屏幕阅读，衬线适合长文，等宽适合代码和对齐；代码块始终使用等宽字体。", "Sans suits screen reading, serif suits long-form text, mono suits code and alignment. Code blocks always use mono.")
            ) {
                SegmentedChoice(
                    listOf(
                        "sans" to text("无衬线", "Sans"),
                        "serif" to text("衬线", "Serif"),
                        "mono" to text("等宽", "Mono")
                    ),
                    settings.editorFontFamily
                ) { onSettingsChange(settings.copy(editorFontFamily = it).normalized()) }
            }
        }
        SettingsDivider()
        SettingsGroup(text("页面", "Page")) {
            SettingsControlRow(
                text("页面宽度", "Page width"),
                text("标准居中阅读，全宽铺满。", "Standard centers the page; full width fills the editor."),
                help = text("只影响文档阅读宽度，不改变页面内容和画布。", "Only affects document reading width; page content and canvas are unchanged.")
            ) {
                SegmentedChoice(
                    listOf(false to text("标准", "Standard"), true to text("全宽", "Full width")),
                    settings.documentFullWidth
                ) { onSettingsChange(settings.copy(documentFullWidth = it)) }
            }
            Spacer(Modifier.height(8.dp))
            SettingsToggleRow(
                text("显示页面信息", "Show page info"),
                text("在顶栏显示信息按钮。", "Show the info button in the header."),
                settings.displayDocInfo,
                help = text("关闭后仍可通过页面选项菜单查看信息。", "When off, page info remains available from the page options menu.")
            ) { onSettingsChange(settings.copy(displayDocInfo = it)) }
            SettingsToggleRow(
                text("代码块行号", "Code block line numbers"),
                text("代码块左侧显示行号。", "Show line numbers beside code blocks."),
                settings.codeBlockLineNumbers,
                help = text("只影响代码块显示，不改变代码内容或导出文本。", "Display only; code content and exported text are unchanged.")
            ) { onSettingsChange(settings.copy(codeBlockLineNumbers = it)) }
        }
        SettingsDivider()
        SettingsGroup(text("新页面", "New pages")) {
            SettingsControlRow(
                text("默认打开方式", "Default opening mode"),
                text("新页面默认视图。", "Default view for new pages."),
                help = text("只影响新建页面的初始视图；已有页面仍可在顶栏切换文档和白板。", "Only affects the initial view of new pages; existing pages can still switch modes in the header.")
            ) {
                SegmentedChoice(
                    listOf(
                        DefaultPageMode.DOCUMENT to text("文档", "Document"),
                        DefaultPageMode.WHITEBOARD to text("白板", "Whiteboard")
                    ),
                    settings.defaultPageMode
                ) { onSettingsChange(settings.copy(defaultPageMode = it)) }
            }
            Spacer(Modifier.height(10.dp))
            SettingsToggleRow(
                text("用日期自动命名", "Name with current date"),
                text("按所选格式生成日期标题。", "Use the selected date format for new titles."),
                settings.autoDateTitle,
                help = text("开启后新建页面标题为当天日期，之后仍可手动重命名。", "New pages are titled with today's date; you can still rename them later.")
            ) { onSettingsChange(settings.copy(autoDateTitle = it)) }
            if (settings.autoDateTitle) {
                Spacer(Modifier.height(8.dp))
                SettingsStackRow(
                    text("日期格式", "Date format"),
                    text("自动命名使用的日期格式。", "Date format used for automatic titles."),
                    help = text("仅影响自动生成的新页面标题；已有标题不会变化。", "Only affects automatically generated new page titles; existing titles are unchanged.")
                ) {
                    DateFormatChoice(
                        settings.dateTitleFormat,
                        listOf(
                            "iso" to "2026-10-31",
                            "slash" to "2026/10/31",
                            "monthDay" to "10-31",
                            "chinese" to "10月31日"
                        )
                    ) { onSettingsChange(settings.copy(dateTitleFormat = it).normalized()) }
                }
            }
        }
    } else {
        CanvasSettings(
            language, settings, onSettingsChange,
            penOnly, onPenOnlyChange, inkColor, onInkColorChange, inkWidth, onInkWidthChange
        )
    }
}

@Composable
private fun CanvasSettings(
    language: AppLanguage,
    settings: WorkspaceSettings,
    onSettingsChange: (WorkspaceSettings) -> Unit,
    penOnly: Boolean,
    onPenOnlyChange: (Boolean) -> Unit,
    inkColor: Int,
    onInkColorChange: (Int) -> Unit,
    inkWidth: Float,
    onInkWidthChange: (Float) -> Unit
) {
    fun text(zh: String, en: String) = tr(language, zh, en)
    Text(
        text("这些值会用于之后新建的卡片、图形、连线和笔迹。", "These values apply to newly created cards, shapes, connectors and ink."),
        color = LingyeColors.Muted,
        fontSize = 13.sp
    )
    Spacer(Modifier.height(18.dp))
    SettingsGroup(text("画布主题", "Canvas theme")) {
        SettingsControlRow(
            text("配色", "Color"),
            text("浅色使用下方背景色；深色使用深色底与浅色网格。", "Light uses the background below; dark uses a dark surface and light grid."),
            help = text("只影响画布显示，不改变已有对象颜色；导出与页面数据不受影响。", "Display only; existing object colors and page data are unchanged.")
        ) {
            SegmentedChoice(
                listOf("light" to text("浅色", "Light"), "dark" to text("深色", "Dark")),
                settings.canvasTheme
            ) { onSettingsChange(settings.copy(canvasTheme = it).normalized()) }
        }
        Spacer(Modifier.height(8.dp))
        SettingsToggleRow(
            text("按钮缩放", "Zoom buttons"),
            text("在画布右下角显示缩小/百分比/放大控件。", "Show zoom out, percentage and zoom in controls at the bottom-right."),
            settings.showZoomButtons,
            help = text("替代鼠标滚轮缩放，适合平板点按；百分比可点击复位视图。", "Replaces mouse-wheel zoom for tablet tapping; tap the percentage to reset the view.")
        ) { onSettingsChange(settings.copy(showZoomButtons = it)) }
    }
    SettingsDivider()
    SettingsGroup(text("背景与卡片", "Background and cards")) {
        SettingsLabel(
            text("画布背景", "Canvas background"),
            text("只影响画布显示，不改变已有对象。", "Changes the canvas surface without altering existing objects."),
            help = text("背景色只用于画布表面；已有图形、卡片和页面卡片保留自己的颜色。", "The background only affects the canvas surface; existing shapes, cards and page cards keep their own colors.")
        )
        ColorChoiceRow(
            listOf(
                text("纸白", "Paper") to 0xffffffff.toInt(),
                text("暖白", "Warm") to 0xfffffbf5.toInt(),
                text("薄荷", "Mint") to 0xfff5fbfa.toInt(),
                text("淡紫", "Lavender") to 0xfff8f7ff.toInt()
            ),
            settings.canvasBackground
        ) { onSettingsChange(settings.copy(canvasBackground = it)) }
        Spacer(Modifier.height(18.dp))
        SettingsLabel(
            text("新卡片颜色", "New card color"),
            text("用于卡片预设和工作区模板之外的新卡片。", "Used for newly inserted cards."),
            help = text("只影响之后新建的卡片；模板和已有卡片的颜色保持不变。", "Only affects newly inserted cards; templates and existing cards keep their colors.")
        )
        ColorChoiceRow(
            listOf(
                text("雾蓝", "Mist") to 0xfff2f5ff.toInt(),
                text("白色", "White") to 0xffffffff.toInt(),
                text("暖黄", "Warm yellow") to 0xfffff5d6.toInt(),
                text("淡粉", "Rose") to 0xffffeef3.toInt()
            ),
            settings.cardColor
        ) { onSettingsChange(settings.copy(cardColor = it)) }
        Spacer(Modifier.height(18.dp))
        SettingsLabel(
            text("新 Frame 背景", "New frame background"),
            text("新建 Frame 使用此背景色。", "New frames use this background color."),
            help = text("只影响之后新建的 Frame；已有 Frame 可在选中后的工具条中改色。", "Only affects new frames; select an existing frame to recolor it.")
        )
        ColorChoiceRow(
            listOf(
                text("白", "White") to 0xffffffff.toInt(),
                text("浅灰", "Light gray") to 0xfff4f5f7.toInt(),
                text("浅蓝", "Light blue") to 0xffeaf2ff.toInt(),
                text("浅绿", "Light green") to 0xffe9f8ef.toInt(),
                text("浅黄", "Light yellow") to 0xfffff8e3.toInt(),
                text("浅紫", "Light purple") to 0xfff3efff.toInt()
            ),
            settings.frameColor
        ) { onSettingsChange(settings.copy(frameColor = it)) }
    }
    SettingsDivider()
    SettingsGroup(text("笔记", "Notes")) {
        Text(text("笔记预览", "Note preview"), fontWeight = FontWeight.Medium, color = LingyeColors.Ink)
        Spacer(Modifier.height(8.dp))
        val previewCorner = RoundedCornerShape(settings.noteCornerRadius.dp)
        val dashedBorder = settings.noteBorderStyle == "dashed"
        Surface(
            Modifier
                .fillMaxWidth()
                .height(112.dp)
                .then(
                    if (dashedBorder) Modifier.drawWithContent {
                        drawContent()
                        drawRoundRect(
                            color = LingyeColors.Ink.copy(alpha = 0.25f),
                            cornerRadius = CornerRadius(settings.noteCornerRadius.dp.toPx()),
                            style = Stroke(
                                width = settings.noteBorderWidth.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(7.dp.toPx(), 5.dp.toPx())
                                )
                            )
                        )
                    } else Modifier
                ),
            shape = previewCorner,
            color = Color(settings.noteBackground or 0xFF000000.toInt()),
            border = if (settings.noteBorderStyle == "solid")
                BorderStroke(settings.noteBorderWidth.dp, LingyeColors.Ink.copy(alpha = 0.25f)) else null,
            shadowElevation = when (settings.noteShadow) {
                "none" -> 0.dp
                "sticker" -> 10.dp
                else -> 5.dp
            }
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    text("新建笔记", "New note"),
                    color = LingyeColors.Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text("记录想法、待办和灵感。", "Capture ideas, tasks and inspiration."),
                    color = LingyeColors.Ink.copy(alpha = 0.72f),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text("☐ 整理今天的想法", "☐ Collect today's ideas"),
                    color = LingyeColors.Ink.copy(alpha = 0.55f),
                    fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        SettingsLabel(
            text("笔记背景", "Note background"),
            text("新笔记使用此背景色。", "New notes use this background color."),
            help = text("只影响之后新建的笔记；已有笔记可在画布工具条中改色。", "Only affects new notes; existing notes can be recolored from the canvas toolbar.")
        )
        ColorChoiceRow(
            listOf(
                text("暖黄", "Warm yellow") to 0xfffff8e3.toInt(),
                text("雾蓝", "Mist blue") to 0xffeef4ff.toInt(),
                text("薄荷", "Mint") to 0xffeaf8f1.toInt(),
                text("淡粉", "Rose") to 0xffffeef2.toInt(),
                text("白", "White") to 0xffffffff.toInt()
            ),
            settings.noteBackground
        ) { onSettingsChange(settings.copy(noteBackground = it)) }
        Spacer(Modifier.height(10.dp))
        SettingsControlRow(
            text("圆角", "Corners"),
            text("新建笔记的圆角。", "Corner size for new notes."),
            help = text("只影响新笔记；选中已有笔记后，可在其工具条的“图层与更多”中单独调整圆角。", "Only affects new notes; select an existing note and adjust it from “Layers and more” in its toolbar."),
            controlWidth = 330.dp
        ) {
            SegmentedChoice(
                listOf(
                    "none" to text("无", "None"),
                    "small" to text("小", "Small"),
                    "medium" to text("中", "Medium"),
                    "large" to text("大", "Large"),
                    "huge" to text("超大", "Huge")
                ),
                settings.noteCorner
            ) { onSettingsChange(settings.copy(noteCorner = it).normalized()) }
        }
        SettingsControlRow(
            text("阴影样式", "Shadow style"),
            text("新建笔记的阴影样式。", "Shadow style for new notes."),
            help = text("只影响新笔记；选中已有笔记后，可在其工具条的“图层与更多”中单独调整阴影。", "Only affects new notes; select an existing note and adjust it from “Layers and more” in its toolbar.")
        ) {
            SegmentedChoice(
                listOf(
                    "none" to text("无", "None"),
                    "box" to text("卡片", "Box"),
                    "sticker" to text("贴纸", "Sticker")
                ),
                settings.noteShadow
            ) { onSettingsChange(settings.copy(noteShadow = it).normalized()) }
        }
        SettingsControlRow(
            text("边框样式", "Border style"),
            text("笔记卡片边框。", "Border style for note cards."),
            help = text("只影响新笔记的边框；已有笔记可在工具条中调整。", "Only affects new note borders; existing notes can be adjusted in their toolbar.")
        ) {
            SegmentedChoice(
                listOf(
                    "solid" to text("实线", "Solid"),
                    "dashed" to text("虚线", "Dashed"),
                    "none" to text("无边框", "None")
                ),
                settings.noteBorderStyle
            ) { onSettingsChange(settings.copy(noteBorderStyle = it).normalized()) }
        }
        SettingsStackRow(
            text("边框厚度", "Border thickness"),
            text("当前 ${settings.noteBorderWidth.roundToInt()} px。", "Current ${settings.noteBorderWidth.roundToInt()} px."),
            help = text("只影响新笔记；无边框时不显示线条。", "Only affects new notes; hidden when the border is off.")
        ) {
            CompactSlider(
                value = settings.noteBorderWidth,
                onValueChange = { onSettingsChange(settings.copy(noteBorderWidth = it).normalized()) },
                valueRange = 2f..12f,
                steps = 4
            )
        }
    }
    SettingsDivider()
    SettingsGroup(text("图形", "Shapes")) {
        SettingsLabel(
            text("新图形填充色", "New shape fill"),
            text("矩形、圆形等新建图形使用此颜色。", "Used when inserting new rectangles and ellipses."),
            help = text("只影响新图形；选中已有图形后可在画布工具条中单独改色。", "Only affects new shapes; select an existing shape to recolor it from the canvas toolbar.")
        )
        ColorChoiceRow(
            listOf(
                text("黄", "Yellow") to 0xffffd866.toInt(),
                text("蓝", "Blue") to 0xff8ab4f8.toInt(),
                text("绿", "Green") to 0xff9de2c2.toInt(),
                text("紫", "Purple") to 0xffc7bdf8.toInt()
            ),
            settings.shapeColor
        ) { onSettingsChange(settings.copy(shapeColor = it)) }
    }
    SettingsDivider()
    SettingsGroup(text("网格与吸附", "Grid and snapping")) {
        SettingsToggleRow(
            text("显示网格", "Show grid"),
            text("画布上的点阵背景，仅用于对齐参考。", "Dot grid for alignment reference only."),
            settings.showGrid,
            help = text("网格只影响显示，不会写入页面数据；缩放过小时会自动隐藏。", "The grid is display-only and is hidden automatically when zoomed out.")
        ) { onSettingsChange(settings.copy(showGrid = it)) }
        SettingsControlRow(
            text("网格大小", "Grid size"),
            text("小 20 / 中 28 / 大 36（世界坐标）。", "Small 20 / Medium 28 / Large 36 world units."),
            help = text("网格大小同时决定“网格吸附”的对齐间距。", "Grid size also sets the snapping interval.")
        ) {
            SegmentedChoice(
                listOf("small" to text("小", "Small"), "medium" to text("中", "Medium"), "large" to text("大", "Large")),
                settings.gridSize
            ) { onSettingsChange(settings.copy(gridSize = it).normalized()) }
        }
        Spacer(Modifier.height(6.dp))
        SettingsToggleRow(
            text("默认吸附网格", "Snap to grid by default"),
            text("新建白板会话默认开启网格吸附，可在“更多”中临时切换。", "New whiteboard sessions start with grid snapping; toggle it temporarily from More."),
            settings.snapToGridDefault,
            help = text("只设置默认值；当前白板中仍可通过“更多 → 网格吸附”临时开关。", "Sets the default only; use More → Grid snap to toggle the current whiteboard.")
        ) { onSettingsChange(settings.copy(snapToGridDefault = it)) }
    }
    SettingsDivider()
    SettingsGroup(text("连线", "Connectors")) {
        SettingsControlRow(
            text("默认路径", "Default path"),
            text("优先保持 AFFiNE 风格的曲线连接。", "Curve is the default to match AFFiNE-style connectors."),
            help = text("只影响新连线；选中已有连线后可在参数条中改路径。", "Only affects new connectors; select an existing connector to change its path.")
        ) {
            SegmentedChoice(
                listOf("straight" to text("直线", "Straight"), "curve" to text("曲线", "Curve"), "step" to text("折线", "Step")),
                settings.connectorPathStyle
            ) { onSettingsChange(settings.copy(connectorPathStyle = it).normalized()) }
        }
        SettingsControlRow(
            text("默认终点", "Default endpoint"),
            text("优先使用圆点，也可以选箭头或无端点。", "Use a dot by default; arrows and no marker remain available."),
            help = text("终点样式只影响新连线；圆点适合流程说明，箭头适合方向关系。", "Only affects new connectors; dots suit flow notes and arrows suit direction.")
        ) {
            SegmentedChoice(
                listOf("dot" to text("圆点", "Dot"), "arrow" to text("箭头", "Arrow"), "none" to text("无", "None")),
                settings.connectorEndMarker
            ) { onSettingsChange(settings.copy(connectorEndMarker = it).normalized()) }
        }
        SettingsControlRow(
            text("默认线宽", "Default line width"),
            text("新连线使用的线宽。", "Width used by new connectors."),
            help = text("只影响新连线；已有连线可在参数条中调整。", "Only affects new connectors; existing ones can be adjusted in their toolbar."),
            controlWidth = 220.dp
        ) {
            SegmentedChoice(
                listOf(1f to "1", 2f to "2", 4f to "4", 6f to "6"),
                settings.connectorWidth
            ) { onSettingsChange(settings.copy(connectorWidth = it).normalized()) }
        }
        Spacer(Modifier.height(6.dp))
        SettingsToggleRow(
            text("手绘连线", "Hand-drawn connectors"),
            text("所有连线使用轻微手绘抖动。", "Apply a subtle hand-drawn wobble to connectors."),
            settings.roughConnectors,
            help = text("仅影响绘制外观，不修改连线路径数据；适合草图和白板记录。", "Visual only; connector path data is unchanged. Suits sketches and whiteboard notes.")
        ) { onSettingsChange(settings.copy(roughConnectors = it)) }
        Spacer(Modifier.height(14.dp))
        SettingsLabel(
            text("连线颜色", "Connector color"),
            text("新连线会使用当前颜色。", "New connectors use this color."),
            help = text("默认颜色只用于新连线；已有连线可在选中后的参数条中改色。", "The default color applies to new connectors; existing ones can be recolored in their toolbar.")
        )
        ColorChoiceRow(
            listOf(
                text("灰", "Gray") to 0xff62748a.toInt(),
                text("蓝", "Blue") to 0xff2563eb.toInt(),
                text("紫", "Purple") to 0xff6255d9.toInt(),
                text("绿", "Green") to 0xff15803d.toInt()
            ),
            settings.connectorColor
        ) { onSettingsChange(settings.copy(connectorColor = it)) }
    }
    SettingsDivider()
    SettingsGroup(text("文本默认样式", "Text defaults")) {
        SettingsLabel(
            text("默认文字颜色", "Default text color"),
            text("新文本框使用此颜色。", "New text boxes use this color."),
            help = text("只影响新文本框；已有文本可在选中后的工具条中改色。", "Only new text boxes; existing text can be recolored from its toolbar.")
        )
        ColorChoiceRow(
            listOf(
                text("墨黑", "Ink") to 0xff20293a.toInt(),
                text("蓝", "Blue") to 0xff2563eb.toInt(),
                text("红", "Red") to 0xffdc2626.toInt(),
                text("绿", "Green") to 0xff15803d.toInt()
            ),
            settings.defaultTextColor
        ) { onSettingsChange(settings.copy(defaultTextColor = it)) }
        Spacer(Modifier.height(10.dp))
        SettingsStackRow(
            text("默认字号", "Default text size"),
            text("当前 ${settings.defaultTextSize.roundToInt()} px。", "Current ${settings.defaultTextSize.roundToInt()} px."),
            help = text("只影响新文本框，不改变已有文本和文档字号。", "Only new text boxes; existing text and document font size are unchanged.")
        ) {
            CompactSlider(
                value = settings.defaultTextSize,
                onValueChange = { onSettingsChange(settings.copy(defaultTextSize = it).normalized()) },
                valueRange = 12f..32f,
                steps = 19
            )
        }
        SettingsControlRow(
            text("默认对齐", "Default alignment"),
            text("新文本框的文字对齐方式。", "Text alignment for new text boxes."),
            help = text("只影响新文本框；已有文本可在工具条中调整。", "Only new text boxes; existing text can be adjusted in its toolbar.")
        ) {
            SegmentedChoice(
                listOf(
                    "left" to text("左对齐", "Left"),
                    "center" to text("居中", "Center"),
                    "right" to text("右对齐", "Right")
                ),
                settings.defaultTextAlign
            ) { onSettingsChange(settings.copy(defaultTextAlign = it).normalized()) }
        }
    }
    SettingsDivider()
    SettingsGroup(text("笔", "Pen")) {
        SettingsStackRow(
            text("画笔粗细", "Brush width"),
            text("当前 ${inkWidth.roundToInt()} px", "Current ${inkWidth.roundToInt()} px"),
            help = text("粗细只影响新笔迹；画笔和高亮在画布参数条中分别记忆各自的粗细。", "Width only affects new ink; pen and highlighter remember their own widths in the canvas bar.")
        ) {
            CompactSlider(value = inkWidth, onValueChange = onInkWidthChange, valueRange = 1f..12f, steps = 22)
        }
        Spacer(Modifier.height(6.dp))
        SettingsToggleRow(
            text("仅手写笔", "Stylus only"),
            text("开启后只响应手写笔，手指不会绘制。", "When on, only a stylus draws; finger touch does not."),
            penOnly,
            help = text("适合掌托防误触；关闭后手指也可以绘制。", "Useful for palm rejection; turn off to draw with a finger.")
        ) { onPenOnlyChange(it) }
        SettingsLabel(
            text("画笔颜色", "Brush color"),
            text("新建笔迹使用此颜色。", "New ink strokes use this color."),
            help = text("只影响新笔迹；已有笔迹需要在画布中选中后改色。", "Only affects new ink; select existing strokes to recolor them.")
        )
        ColorChoiceRow(
            listOf(
                text("墨黑", "Ink") to 0xff27374b.toInt(),
                text("蓝", "Blue") to 0xff2563eb.toInt(),
                text("红", "Red") to 0xffdc2626.toInt(),
                text("绿", "Green") to 0xff15803d.toInt()
            ),
            inkColor
        ) { onInkColorChange(it) }
    }
}

@Composable
private fun ShortcutSettings(language: AppLanguage) {
    fun text(zh: String, en: String) = tr(language, zh, en)
    SettingsHeading(
        text("键盘快捷键", "Keyboard shortcuts"),
        text("白板工具、应用与文档编辑的固定按键；连接硬件键盘时生效。", "Fixed keys for whiteboard tools, app actions and document editing; applies with a hardware keyboard.")
    )
    SettingsGroup(text("应用", "App"), help = text("应用快捷键在未打开设置弹窗时生效；Ctrl 在 macOS 外接键盘上对应 Command 键同样可用。", "App shortcut keys work unless the settings dialog is open; Command on a macOS keyboard is also accepted.")) {
        listOf(
            text("快速搜索", "Quick search") to "Ctrl+K",
            text("新建文档", "New document") to "Ctrl+N",
            text("新建白板", "New whiteboard") to "Ctrl+Shift+N",
            text("打开设置", "Open settings") to "Ctrl+,"
        ).forEach { (label, key) -> SettingsShortcutRow(label, key) }
    }
    SettingsDivider()
    SettingsGroup(text("文档编辑", "Document editing"), help = text("在文档文本块获得焦点时生效，作用于当前块；标题快捷键使用 Ctrl+Alt+数字。", "These apply to the focused document text block; heading keys use Ctrl+Alt+number.")) {
        listOf(
            text("粗体", "Bold") to "Ctrl+B",
            text("斜体", "Italic") to "Ctrl+I",
            text("下划线", "Underline") to "Ctrl+U",
            text("标题 1–6", "Heading 1–6") to "Ctrl+Alt+1…6",
            text("正文", "Paragraph") to "Ctrl+Alt+0"
        ).forEach { (label, key) -> SettingsShortcutRow(label, key) }
    }
    SettingsDivider()
    SettingsGroup(text("工具", "Tools"), help = text("工具键在白板画布获得焦点时生效；输入文字或打开菜单时不会拦截按键。", "Tool keys work while the whiteboard canvas has focus; typing or open menus are not intercepted.")) {
        listOf(
            "V" to text("选择", "Select"),
            "H" to text("拖拽", "Hand"),
            "F" to text("Frame", "Frame"),
            "C" to text("连线", "Connector"),
            "@" to text("链接", "Link"),
            "T" to text("文本", "Text"),
            "P" to text("画笔", "Pen"),
            "⇧P" to text("高亮笔", "Highlighter"),
            "E" to text("橡皮", "Eraser"),
            "S" to text("形状", "Shapes"),
            "M" to text("思维导图", "Mind map")
        ).forEach { (key, label) -> SettingsShortcutRow(label, key) }
    }
    SettingsDivider()
    SettingsGroup(text("命令", "Commands"), help = text("命令键在画布聚焦时生效；Ctrl 在 macOS 外接键盘上对应 Command 键同样可用。", "Command keys work while the canvas is focused; Command on a macOS keyboard is also accepted.")) {
        listOf(
            text("撤销", "Undo") to "Ctrl+Z",
            text("重做", "Redo") to "Ctrl+Shift+Z",
            text("重做（备用）", "Redo (alt)") to "Ctrl+Y",
            text("全选", "Select all") to "Ctrl+A",
            text("删除", "Delete") to "Delete",
            text("取消 / 退出", "Cancel / exit") to "Esc",
            text("放大", "Zoom in") to "Ctrl+=",
            text("缩小", "Zoom out") to "Ctrl+-",
            text("复位缩放", "Reset zoom") to "Ctrl+0"
        ).forEach { (label, key) -> SettingsShortcutRow(label, key) }
    }
    SettingsDivider()
    SettingsGroup(text("对象操作", "Object actions"), help = text("方向键移动选中对象；按住 Shift 可微调。", "Arrow keys move the selection; hold Shift to nudge.")) {
        listOf(
            text("移动对象", "Move object") to "方向键 / Arrows",
            text("微调对象", "Nudge object") to "Shift+方向键 / Arrows"
        ).forEach { (label, key) -> SettingsShortcutRow(label, key) }
    }
    SettingsDivider()
    SettingsGroup(text("选择与编组", "Selection and grouping"), help = text("以下快捷键在白板画布获得焦点且存在选中对象时生效。", "These work while the whiteboard canvas is focused and objects are selected.")) {
        listOf(
            text("复制选中对象", "Duplicate selection") to "Ctrl+D",
            text("编组", "Group") to "Ctrl+G",
            text("取消编组", "Ungroup") to "Ctrl+Shift+G",
            text("置于顶层", "Bring to front") to "Ctrl+]",
            text("置于底层", "Send to back") to "Ctrl+[",
            text("左对齐", "Align left") to "Ctrl+Shift+L",
            text("右对齐", "Align right") to "Ctrl+Shift+R",
            text("顶部对齐", "Align top") to "Ctrl+Shift+T",
            text("底部对齐", "Align bottom") to "Ctrl+Shift+B"
        ).forEach { (label, key) -> SettingsShortcutRow(label, key) }
    }
    SettingsNote(text("快捷键目前为固定映射，自定义绑定尚未提供。应用级新建页、搜索与文档格式快捷键已接入；画布快捷键在文字输入或菜单打开时不会拦截。", "Shortcuts are fixed for now; custom bindings are not yet available. App-level new-page, search and document-format shortcuts are wired up; canvas shortcuts are not intercepted while typing or when a menu is open."))
}

@Composable
private fun SettingsShortcutRow(label: String, keys: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = LingyeColors.Ink)
        Surface(shape = RoundedCornerShape(7.dp), color = LingyeColors.Chip) {
            Text(
                keys,
                Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = LingyeColors.Ink
            )
        }
    }
}

@Composable
private fun WorkspaceSettingsContent(
    language: AppLanguage,
    settings: WorkspaceSettings,
    onSettingsChange: (WorkspaceSettings) -> Unit
) {
    fun text(zh: String, en: String) = tr(language, zh, en)
    var nameDraft by remember { mutableStateOf(settings.workspaceName) }
    var subtitleDraft by remember { mutableStateOf(settings.workspaceSubtitle) }
    SettingsHeading(
        text("偏好设置", "Preferences"),
        text("设置当前工作区的名称、副标题与本机信息。", "Name, subtitle and local information for this workspace.")
    )
    SettingsGroup(text("基本信息", "Basic info")) {
        SettingsStackRow(
            text("工作区名称", "Workspace name"),
            text("显示在侧栏左上角和设置卡片中。", "Shown in the sidebar header and settings card."),
            help = text("名称只影响本机显示，不会改变页面内容或链接；留空时恢复为“灵页”。", "The name only affects local display and never changes page content or links. Empty falls back to Lingye.")
        ) {
            OutlinedTextField(
                value = nameDraft,
                onValueChange = {
                    nameDraft = it.take(40)
                    onSettingsChange(settings.copy(workspaceName = nameDraft).normalized())
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text("名称", "Name")) }
            )
        }
        SettingsStackRow(
            text("副标题", "Subtitle"),
            text("用于补充说明工作区用途。", "A short line describing the workspace."),
            help = text("副标题同样只保存在本机，留空时恢复为“离线创意空间”。", "The subtitle is stored locally too; empty falls back to Offline creative space.")
        ) {
            OutlinedTextField(
                value = subtitleDraft,
                onValueChange = {
                    subtitleDraft = it.take(60)
                    onSettingsChange(settings.copy(workspaceSubtitle = subtitleDraft).normalized())
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text("副标题", "Subtitle")) }
            )
        }
    }
    SettingsDivider()
    SettingsGroup(text("存储与隐私", "Storage and privacy")) {
        SettingsInfoLine(text("存储位置", "Storage"), text("应用私有存储", "Private app storage"))
        SettingsInfoLine(text("云端同步", "Cloud sync"), text("未启用", "Not enabled"))
        SettingsInfoLine(text("数据上传", "Data upload"), text("无", "None"))
    }
    SettingsNote(text("名称与副标题仅保存在本机，不会上传；修改后侧栏立即更新。", "Name and subtitle stay on this device and update the sidebar immediately."))
}

@Composable
private fun DataSettings(
    language: AppLanguage,
    pageCount: Int,
    trashedPageCount: Int,
    folderCount: Int,
    templateCount: Int,
    storageBytes: Long,
    unusedImageCount: Int,
    unusedImageBytes: Long,
    imageCache: ImageCacheStats,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onImportMarkdown: () -> Unit,
    onExportMarkdown: () -> Unit,
    onCleanupImages: () -> Unit
) {
    fun text(zh: String, en: String) = tr(language, zh, en)
    SettingsHeading(text("储存", "Storage"), text("灵页的数据保存在应用私有目录，可在本机导出完整备份。", "Lingye stores data in private app storage and can export a complete local backup."))
    SettingsGroup(text("本机内容", "Local content"), help = text("统计来自当前应用私有目录；占用空间包含页面、图片、历史、模板与共享内容。", "Counts come from private app storage; usage includes pages, images, history, templates and shared content.")) {
        SettingsInfoLine(text("页面", "Pages"), pageCount.toString())
        SettingsInfoLine(text("回收站页面", "Pages in trash"), trashedPageCount.toString())
        SettingsInfoLine(text("文件夹", "Folders"), folderCount.toString())
        SettingsInfoLine(text("工作区模板", "Workspace templates"), templateCount.toString())
        SettingsInfoLine(text("占用空间", "Storage used"), formatStorage(storageBytes, language))
        SettingsInfoLine(
            text("未使用图片", "Unused images"),
            if (unusedImageCount == 0) text("无", "None")
            else "$unusedImageCount · ${formatStorage(unusedImageBytes, language)}"
        )
        SettingsInfoLine(text("图片格式", "Image formats"), "JPEG · PNG · WebP · GIF · BMP · HEIF · AVIF")
        SettingsInfoLine(
            text("图片缓存", "Image cache"),
            if (imageCache.entries == 0) text("空", "Empty")
            else "${imageCache.entries} · ${formatStorage(imageCache.bytes, language)} / " +
                "${formatStorage(imageCache.maxBytes, language)} · ${(imageCache.hitRate * 100).roundToInt()}%"
        )
    }
    if (unusedImageCount > 0) {
        SettingsDivider()
        SettingsGroup(text("图片清理", "Image cleanup"), help = text("只删除未被任何页面、模板、共享块或页面撤销历史引用的图片文件。删除后不可恢复，建议先导出备份。", "Removes only image files not referenced by any page, template, shared block or page undo history. This cannot be undone, so export a backup first.")) {
            SettingsActionButton(text("清理未使用图片", "Clean up unused images"), AppIcon.TRASH, onCleanupImages)
        }
    }
    SettingsDivider()
    SettingsGroup(text("备份与恢复", "Backup and restore"), help = text("备份是完整 ZIP；恢复会复制为新页面，不覆盖当前内容。导入/导出 Markdown 只处理单个页面的可读文本与块结构。", "Backups are full ZIP files; restoring copies pages instead of overwriting. Markdown handles one page's readable text and block structure.")) {
        Text(text("备份包含页面、图片、文件夹、模板和共享内容。恢复时会复制为新页面，不覆盖已有内容。", "Backups include pages, images, folders, templates and shared content. Restoring creates new pages and does not overwrite existing content."), color = LingyeColors.Muted, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SettingsActionButton(text("导出备份", "Export backup"), AppIcon.BACKUP, onBackup)
            SettingsActionButton(text("恢复备份", "Restore backup"), AppIcon.RESTORE, onRestore)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SettingsActionButton(text("导入 Markdown", "Import Markdown"), AppIcon.DOCUMENT, onImportMarkdown)
            SettingsActionButton(text("导出 Markdown", "Export Markdown"), AppIcon.SAVE, onExportMarkdown)
        }
    }
    SettingsNote(text("云同步与多人协作尚未启用，因此没有提供无法兑现的同步选项。", "Cloud sync and collaboration are not enabled, so no non-functional sync options are shown."))
}

@Composable
private fun AboutSettings(language: AppLanguage, storageBytes: Long) {
    fun text(zh: String, en: String) = tr(language, zh, en)
    SettingsHeading(text("关于灵页", "About Lingye"), text("离线优先的 Android 平板文档与白板工作区。", "An offline-first document and whiteboard workspace for Android tablets."))
    SettingsGroup(text("版本信息", "Version information"), help = text("页面格式 25 是当前本机保存格式；旧格式页面可直接读取，并在打开时自动升级。", "Page format 25 is the current local save format; older pages remain readable and upgrade automatically when opened.")) {
        SettingsInfoLine(text("版本", "Version"), BuildConfig.VERSION_NAME)
        SettingsInfoLine(text("页面格式", "Page format"), "25")
        SettingsInfoLine(text("数据位置", "Data location"), text("应用私有存储", "Private app storage"))
        SettingsInfoLine(text("本机数据量", "Local data"), formatStorage(storageBytes, language))
        SettingsInfoLine(text("云端同步", "Cloud sync"), text("未启用", "Not enabled"))
    }
    SettingsDivider()
    SettingsGroup(text("设计原则", "Design principles")) {
        Text(text("文档和白板共用同一页面、对象 ID、撤销历史与本地备份。平板端优先保留触控、手写笔与离线可靠性，不加入桌面专属设置。", "Documents and whiteboards share pages, object IDs, undo history and local backups. The tablet edition prioritizes touch, stylus and offline reliability, without desktop-only settings."), color = LingyeColors.Muted, fontSize = 13.sp)
    }
}

@Composable
private fun SettingsHeading(title: String, description: String) {
    Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = LingyeColors.Ink)
    Spacer(Modifier.height(4.dp))
    Text(description, color = LingyeColors.Muted, fontSize = 13.sp)
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun SettingsGroup(title: String, help: String? = null, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = LingyeColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        if (help != null) {
            Spacer(Modifier.width(6.dp))
            SettingsHelp(help)
        }
    }
    Spacer(Modifier.height(13.dp))
    content()
}

@Composable
private fun SettingsHelp(help: String) {
    var expanded by remember { mutableStateOf(false) }
    Box {
            Box(
                Modifier.size(30.dp)
                    .clickable { expanded = true }
                    .semantics { contentDescription = "帮助" },
                contentAlignment = Alignment.Center
            ) {
                HelpGlyph()
            }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(280.dp),
            shape = RoundedCornerShape(12.dp),
            containerColor = LingyeColors.SurfaceHigh,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Text(help, Modifier.padding(14.dp), fontSize = 12.sp, color = LingyeColors.Ink, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = LingyeColors.Line.copy(alpha = 0.8f), modifier = Modifier.padding(vertical = 20.dp))
}

@Composable
private fun SettingsLabel(title: String, description: String, help: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontWeight = FontWeight.Medium, color = LingyeColors.Ink)
        if (help != null) {
            Spacer(Modifier.width(6.dp))
            SettingsHelp(help)
        }
    }
    Text(description, color = LingyeColors.Muted, fontSize = 12.sp)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun SettingsControlRow(
    title: String,
    description: String,
    help: String? = null,
    controlWidth: Dp = 240.dp,
    control: @Composable () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        val label: @Composable () -> Unit = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Medium, color = LingyeColors.Ink)
                    if (help != null) {
                        Spacer(Modifier.width(6.dp))
                        SettingsHelp(help)
                    }
                }
                Text(description, color = LingyeColors.Muted, fontSize = 12.sp)
            }
        }
        if (maxWidth < 460.dp) {
            Column(Modifier.fillMaxWidth()) {
                label()
                Spacer(Modifier.height(10.dp))
                control()
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { label() }
                Spacer(Modifier.width(16.dp))
                Box(Modifier.width(controlWidth), contentAlignment = Alignment.CenterEnd) {
                    control()
                }
            }
        }
    }
}

@Composable
private fun SettingsStackRow(
    title: String,
    description: String,
    help: String? = null,
    content: @Composable () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontWeight = FontWeight.Medium, color = LingyeColors.Ink)
            if (help != null) {
                Spacer(Modifier.width(6.dp))
                SettingsHelp(help)
            }
        }
        Text(description, color = LingyeColors.Muted, fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    help: String? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Medium)
                if (help != null) {
                    Spacer(Modifier.width(6.dp))
                    SettingsHelp(help)
                }
            }
            Text(description, color = LingyeColors.Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.width(16.dp))
        CompactSwitch(checked = checked)
    }
}

@Composable
private fun <T> SegmentedChoice(options: List<Pair<T, String>>, selected: T, disabled: Set<T> = emptySet(), onSelected: (T) -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    val density = LocalDensity.current
    val insetX = with(density) { 3.dp.toPx() }
    val insetY = with(density) { 2.dp.toPx() }
    val itemBounds = remember { mutableStateMapOf<T, Rect>() }
    val indicatorLeft = remember { Animatable(0f) }
    val indicatorWidth = remember { Animatable(0f) }
    val selectedBounds = itemBounds[selected]
    val targetLeft = selectedBounds?.let { it.left + insetX }
    val targetWidth = selectedBounds?.let { (it.width - insetX * 2f).coerceAtLeast(0f) }
    val shownLeft = if (indicatorWidth.value <= 0f) targetLeft ?: 0f else indicatorLeft.value
    val shownWidth = if (indicatorWidth.value <= 0f) targetWidth ?: 0f else indicatorWidth.value

    LaunchedEffect(selectedBounds) {
        val target = selectedBounds ?: return@LaunchedEffect
        val newLeft = target.left + insetX
        val newWidth = (target.width - insetX * 2f).coerceAtLeast(0f)
        if (indicatorWidth.value <= 0f) {
            indicatorLeft.snapTo(newLeft)
            indicatorWidth.snapTo(newWidth)
        } else {
            launch { indicatorLeft.animateTo(newLeft, tween(220, easing = FastOutSlowInEasing)) }
            launch { indicatorWidth.animateTo(newWidth, tween(220, easing = FastOutSlowInEasing)) }
        }
    }

    Box(
        Modifier
            .shadow(5.dp, shape)
            .clip(shape)
            .background(LingyeColors.SurfaceHigh)
            .border(1.dp, LingyeColors.SegmentBorder, shape)
            .height(34.dp)
    ) {
        if (shownWidth > 0f) {
            Box(
                Modifier
                    .offset { IntOffset(shownLeft.roundToInt(), insetY.roundToInt()) }
                    .width(with(density) { shownWidth.toDp() })
                    .height(30.dp)
                    .background(LingyeColors.SegmentSelected, RoundedCornerShape(15.dp))
            )
        }
        Row(horizontalArrangement = Arrangement.Start) {
            options.forEach { (value, label) ->
                val active = value == selected
                val isDisabled = value in disabled
                val bounds = itemBounds[value]
                val indicatorCenter = shownLeft + shownWidth / 2f
                val covered = bounds != null && shownWidth > 0f &&
                    indicatorCenter >= bounds.left + insetX && indicatorCenter <= bounds.right - insetX
                Box(Modifier.onGloballyPositioned { itemBounds[value] = it.boundsInParent() }) {
                    Surface(
                        onClick = { if (!isDisabled) onSelected(value) },
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp).fillMaxHeight(),
                        shape = RoundedCornerShape(15.dp),
                        color = Color.Transparent,
                        enabled = !isDisabled
                    ) {
                        Box(
                            Modifier.padding(horizontal = 12.dp).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = when {
                                    isDisabled -> LingyeColors.DisabledInk
                                    covered -> LingyeColors.OnSegmentSelected
                                    else -> LingyeColors.SegmentInk
                                },
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateFormatChoice(selected: String, options: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = LingyeColors.SurfaceHigh,
        border = BorderStroke(1.dp, LingyeColors.SegmentBorder),
        shadowElevation = 5.dp
    ) {
        FlowRow(
            Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            options.forEach { (value, label) ->
                val active = value == selected
                Surface(
                    onClick = { onSelect(value) },
                    shape = RoundedCornerShape(15.dp),
                    color = if (active) LingyeColors.SegmentSelected else Color.Transparent
                ) {
                    Text(
                        label,
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 11.5.sp,
                        color = if (active) LingyeColors.OnSegmentSelected else LingyeColors.SegmentInk,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> SettingsDropdown(
    options: List<Pair<T, String>>,
    selected: T,
    modifier: Modifier = Modifier,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = options.firstOrNull { it.first == selected }?.second ?: options.firstOrNull()?.second.orEmpty()
    Box(modifier) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(9.dp),
            color = LingyeColors.SurfaceHigh,
            border = BorderStroke(1.dp, LingyeColors.Line)
        ) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(current, Modifier.weight(1f), fontSize = 12.5.sp, color = LingyeColors.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("⌄", color = LingyeColors.Muted, fontSize = 12.sp)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(220.dp),
            shape = RoundedCornerShape(12.dp),
            containerColor = LingyeColors.SurfaceHigh
        ) {
            options.forEach { (value, label) ->
                MenuRow(label, { expanded = false; onSelected(value) }, checked = value == selected)
            }
        }
    }
}

@Composable
private fun ColorChoiceRow(options: List<Pair<String, Int>>, selected: Int, onSelected: (Int) -> Unit) {
    ColorPaletteRow(
        selected = selected,
        onPick = onSelected,
        modifier = Modifier.fillMaxWidth(),
        leading = options.map { it.second to it.first }
    )
}

@Composable
private fun SettingsActionButton(label: String, icon: AppIcon, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = LingyeColors.PrimarySoft
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            WorkspaceIcon(icon, null, Modifier.size(16.dp), LingyeColors.Primary)
            Spacer(Modifier.width(6.dp))
            Text(label, color = LingyeColors.Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
        }
    }
}

@Composable
private fun SettingsInfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = LingyeColors.Muted, fontSize = 13.sp)
        Text(value, color = LingyeColors.Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SettingsNote(text: String) {
    Spacer(Modifier.height(18.dp))
    Surface(shape = RoundedCornerShape(14.dp), color = LingyeColors.SubtleAlt) {
        Text(text, Modifier.padding(horizontal = 14.dp, vertical = 11.dp), color = LingyeColors.Muted, fontSize = 12.sp)
    }
}

internal fun formatStorage(bytes: Long, language: AppLanguage): String {
    val megabytes = bytes / (1024.0 * 1024.0)
    return if (megabytes >= 1.0) {
        "${String.format(java.util.Locale.US, "%.1f", megabytes)} MB"
    } else {
        val kilobytes = (bytes / 1024.0).coerceAtLeast(0.1)
        "${String.format(java.util.Locale.US, "%.0f", kilobytes)} KB"
    }
}
