@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageStore

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*
import android.content.Context
import androidx.compose.ui.unit.IntRect
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun BoxScope.CanvasBottomBar(
    page: Page,
    store: PageStore,
    context: Context,
    templateScope: CoroutineScope,
    workspaceTemplates: List<CardTemplate>,
    templatesAvailable: Boolean,
    language: AppLanguage,
    settings: WorkspaceSettings,
    updateSettings: (WorkspaceSettings) -> Unit,
    focusMode: Boolean,
    setFocusMode: (Boolean) -> Unit,
    tool: String,
    setTool: (String) -> Unit,
    chooseTool: (String) -> Unit,
    enterConnectorTool: () -> Unit,
    dismissToolMenus: () -> Unit,
    selectMenuOption: (String) -> Unit,
    insertMindmap: (String) -> Unit,
    addImage: () -> Unit,
    changed: () -> Unit,
    undo: () -> Unit,
    redo: () -> Unit,
    startPresentation: (Int) -> Unit,
    penOnly: Boolean,
    setPenOnly: (Boolean) -> Unit,
    eraserArea: Boolean,
    setEraserArea: (Boolean) -> Unit,
    brushKind: String,
    setBrushKind: (String) -> Unit,
    highlighterColor: Int,
    setHighlighterColor: (Int) -> Unit,
    highlighterWidth: Float,
    setHighlighterWidth: (Float) -> Unit,
    inkColor: Int,
    setInkColor: (Int) -> Unit,
    inkWidth: Float,
    setInkWidth: (Float) -> Unit,
    selectedItems: List<CanvasSelection>,
    setSelection: (CanvasSelection?) -> Unit,
    setSelectedItems: (List<CanvasSelection>) -> Unit,
    multiSelect: Boolean,
    setMultiSelect: (Boolean) -> Unit,
    snapToGrid: Boolean,
    setSnapToGrid: (Boolean) -> Unit,
    templateGalleryOpen: Boolean,
    setTemplateGalleryOpen: (Boolean) -> Unit,
    templateGalleryCategory: String,
    contentButtonBounds: IntRect?,
    setContentButtonBounds: (IntRect?) -> Unit,
    setManageTemplates: (Boolean) -> Unit,
    moreToolsMenu: Boolean,
    setMoreToolsMenu: (Boolean) -> Unit,
    textToolsMenu: Boolean,
    setTextToolsMenu: (Boolean) -> Unit,
    pendingTextKind: String,
    setPendingTextKind: (String) -> Unit,
    setLinkedDocPicker: (Boolean) -> Unit,
    mindmapToolsMenu: Boolean,
    setMindmapToolsMenu: (Boolean) -> Unit,
    activeMenuButton: String?,
    setActiveMenuButton: (String?) -> Unit,
    toolbarMenuOpen: Boolean,
    frameRatio: String,
    setFrameRatio: (String) -> Unit,
    frameNavigator: Boolean,
    setFrameNavigator: (Boolean) -> Unit,
    setTemplateName: (String) -> Unit,
    setSaveSelectionTemplate: (Boolean) -> Unit,
    lastNavigationTool: String,
    selectedFrameIndex: Int?,
    setScale: (Float) -> Unit,
    setPan: (Offset) -> Unit,
    viewportWidthPx: Int,
    density: Float
) {
    val paramBarMaxWidth = CanvasLayout.parameterBarMaxWidth(viewportWidthPx / density).dp
    val frameBarShift = if (paramBarMaxWidth.value > 420f) (-110).dp else 0.dp
        Column(Modifier.align(Alignment.BottomCenter).padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = CanvasUiTokens.bottomBarBottomPadding), horizontalAlignment = Alignment.CenterHorizontally) {
           if (textToolsMenu) {
               val textParametersScroll = rememberScrollState()
               var textHeadingMenu by remember { mutableStateOf(false) }
               CanvasParamSurface(Modifier.widthIn(max = minOf(1040f, paramBarMaxWidth.value).dp)) {
                   Row(
                       Modifier.padding(start = 7.dp, end = 5.dp, top = 5.dp, bottom = 5.dp),
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       Row(
                           Modifier.weight(1f, fill = false).horizontalScroll(textParametersScroll),
                           horizontalArrangement = Arrangement.spacedBy(2.dp),
                           verticalAlignment = Alignment.CenterVertically
                       ) {
                           CanvasParameterChip(tr(language, "链接", "Link"), false, AppIcon.LINK, enabled = false) {}
                           CanvasParameterChip(tr(language, "附件", "Attach"), false, AppIcon.BACKUP, enabled = false) {}
                           CanvasToolbarDivider()
                           CanvasParameterChip(tr(language, "正文", "Body"), pendingTextKind == "body", AppIcon.TEXT) {
                               setPendingTextKind("body")
                           }
                           Box {
                               CanvasParameterChip("H", textHeadingMenu) { textHeadingMenu = true }
                               LingyeDropdownMenu(expanded = textHeadingMenu, onDismissRequest = { textHeadingMenu = false }) {
                                   (1..6).forEach { level ->
                                       DropdownMenuItem(
                                           text = {
                                               Text(
                                                   tr(
                                                       language,
                                                       "H$level  ${if (level == 1) "一级标题" else "$level 级标题"}",
                                                       "H$level  Heading $level"
                                                   )
                                               )
                                           },
                                           onClick = {
                                               setPendingTextKind("h$level")
                                               textHeadingMenu = false
                                           }
                                       )
                                   }
                               }
                           }
                           CanvasParameterChip(tr(language, "代码", "Code"), pendingTextKind == "code", AppIcon.CODE_BLOCK) { setPendingTextKind("code") }
                           CanvasParameterChip(tr(language, "引用", "Quote"), pendingTextKind == "quote", AppIcon.QUOTE) { setPendingTextKind("quote") }
                           CanvasParameterChip(tr(language, "项目符号", "Bullets"), pendingTextKind == "bulleted", AppIcon.BULLETED_LIST) { setPendingTextKind("bulleted") }
                           CanvasParameterChip(tr(language, "编号", "Numbered"), pendingTextKind == "numbered", AppIcon.NUMBERED_LIST) { setPendingTextKind("numbered") }
                           CanvasParameterChip(tr(language, "待办", "To-do"), pendingTextKind == "todo", AppIcon.CARD) { setPendingTextKind("todo") }
                       }
                   }
               }
               Spacer(Modifier.height(8.dp))
           }
           if (tool == "Frame" && !toolbarMenuOpen) {
               CanvasParamSurface(Modifier.widthIn(max = paramBarMaxWidth).offset(x = frameBarShift)) {
                   Row(
                       Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 7.dp, vertical = 5.dp),
                       horizontalArrangement = Arrangement.spacedBy(2.dp),
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       listOf("custom" to tr(language, "自定义", "Custom"), "1:1" to "1:1", "4:3" to "4:3", "16:9" to "16:9", "2:1" to "2:1").forEach { (ratio, label) ->
                           CanvasParameterChip(label, frameRatio == ratio) { setFrameRatio(ratio) }
                       }
                   }
               }
               Spacer(Modifier.height(8.dp))
           }
           if (tool == "连线" && !toolbarMenuOpen) {
               CanvasParamSurface(Modifier.widthIn(max = paramBarMaxWidth)) {
                   Row(
                       Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 7.dp, vertical = 5.dp),
                       horizontalArrangement = Arrangement.spacedBy(2.dp),
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       listOf("curve" to AppIcon.CURVE, "step" to AppIcon.STEP, "straight" to AppIcon.STRAIGHT).forEach { (style, icon) ->
                           ToolIconButton(icon, tr(language, if(style=="curve")"曲线" else if(style=="step")"折线" else "直线", style), settings.connectorPathStyle == style, false) {
                               updateSettings(settings.copy(connectorPathStyle = style))
                           }
                       }
                       CanvasToolbarDivider()
                       listOf(1f, 2f, 4f, 6f).forEach { width ->
                           CanvasParamWidthDot(width, settings.connectorWidth == width) { updateSettings(settings.copy(connectorWidth = width)) }
                       }
                       CanvasToolbarDivider()
                       CanvasConnectorColors(settings.connectorColor, language) { updateSettings(settings.copy(connectorColor = it)) }
                   }
               }
               Spacer(Modifier.height(8.dp))
           }
           if (tool in setOf("矩形", "圆角矩形", "圆形", "三角形", "菱形") && !toolbarMenuOpen) {
               CanvasParamSurface(Modifier.widthIn(max = paramBarMaxWidth)) {
                   Row(
                       Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 7.dp, vertical = 5.dp),
                       horizontalArrangement = Arrangement.spacedBy(2.dp),
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       listOf(
                           "矩形" to AppIcon.RECTANGLE,
                           "圆形" to AppIcon.ELLIPSE,
                           "菱形" to AppIcon.DIAMOND,
                           "三角形" to AppIcon.TRIANGLE,
                           "圆角矩形" to AppIcon.ROUNDED_RECTANGLE
                       ).forEach { (name, icon) ->
                           ToolIconButton(icon, name, tool == name, false) { setTool(name) }
                       }
                       CanvasToolbarDivider()
                       CanvasShapeColors(settings.shapeColor, language) { updateSettings(settings.copy(shapeColor = it)) }
                   }
               }
               Spacer(Modifier.height(8.dp))
           }
           if ((tool == "画笔" || tool == "橡皮") && !toolbarMenuOpen) {
               CanvasParamSurface(Modifier.widthIn(max = paramBarMaxWidth)) {
                   Row(
                       Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 7.dp, vertical = 5.dp),
                       horizontalArrangement = Arrangement.spacedBy(2.dp),
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       if (tool == "橡皮") {
                           CanvasParameterChip(tr(language, "整笔", "Stroke"), !eraserArea, AppIcon.CURVE) { setEraserArea(false) }
                           CanvasParameterChip(tr(language, "区域", "Area"), eraserArea, AppIcon.RECTANGLE) { setEraserArea(true) }
                           CanvasToolbarDivider()
                       }
                       if (tool == "画笔") {
                           CanvasParameterChip(tr(language, "画笔", "Pen"), brushKind == BrushSettings.KIND_BRUSH, AppIcon.PEN) { setBrushKind(BrushSettings.KIND_BRUSH) }
                           CanvasParameterChip(tr(language, "高亮", "Highlighter"), brushKind == BrushSettings.KIND_HIGHLIGHTER, AppIcon.SPARK) { setBrushKind(BrushSettings.KIND_HIGHLIGHTER) }
                           CanvasToolbarDivider()
                           val selectingHighlighter = brushKind == BrushSettings.KIND_HIGHLIGHTER
                           val chosenColor = if (selectingHighlighter) highlighterColor else inkColor
                           val onPickColor: (Int) -> Unit = if (selectingHighlighter) setHighlighterColor else setInkColor
                           ColorPaletteRow(selected = chosenColor, onPick = onPickColor)
                           val currentWidth = BrushSettings.paintWidth(brushKind, inkWidth, highlighterWidth)
                           BrushSettings.widthsFor(brushKind).forEach { width ->
                               CanvasParamWidthDot(width, currentWidth == width) {
                                   if (selectingHighlighter) setHighlighterWidth(width) else setInkWidth(width)
                               }
                           }
                       }
                       CanvasParameterChip(
                           tr(language, if (penOnly) "仅手写笔：开" else "仅手写笔", if (penOnly) "Stylus only: on" else "Stylus only"),
                           penOnly,
                           AppIcon.PEN
                       ) { setPenOnly(!penOnly) }
                   }
               }
               Spacer(Modifier.height(8.dp))
           }
          if (tool == "选择" && selectedItems.size > 1 && !toolbarMenuOpen) {
              var multiAlignMenu by remember { mutableStateOf(false) }
              CanvasParamSurface(Modifier.widthIn(max = paramBarMaxWidth)) {
                  Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 7.dp, vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                      CompactIconAction(AppIcon.COPY, tr(language, "复制选区", "Duplicate selection"), {
                          val copied = page.duplicateSelections(selectedItems)
                          setSelection(copied.lastOrNull())
                          setSelectedItems(copied)
                          changed()
                      })
                      Box {
                          CompactIconAction(AppIcon.LAYER_UP, tr(language, "对齐", "Align"), { multiAlignMenu = true })
                          LingyeDropdownMenu(expanded = multiAlignMenu, onDismissRequest = { multiAlignMenu = false }) {
                              listOf("left" to tr(language, "左对齐", "Align left"), "right" to tr(language, "右对齐", "Align right"), "top" to tr(language, "顶部对齐", "Align top"), "bottom" to tr(language, "底部对齐", "Align bottom")).forEach { (edge, label) ->
                                  DropdownMenuItem(text = { Text(label) }, onClick = { page.alignSelections(selectedItems, edge); multiAlignMenu = false; changed() })
                              }
                          }
                      }
                      CompactIconAction(AppIcon.OVERVIEW, tr(language, "组合", "Group"), {
                          page.groupSelections(selectedItems)
                          setSelectedItems(selectedItems.flatMap { page.selectionMembers(it) }.distinct())
                          changed()
                      })
                      if (selectedItems.any { page.selectionMembers(it).size > 1 }) {
                          CompactIconAction(AppIcon.MENU, tr(language, "取消组合", "Ungroup"), { page.ungroupSelections(selectedItems); changed() })
                      }
                      if (selectedItems.size == 2 && selectedItems.all { it.type == "shape" }) {
                          CompactIconAction(AppIcon.CONNECT, tr(language, "绑定连线", "Connect shapes"), { page.addBoundConnector(selectedItems[0].index, selectedItems[1].index); changed() })
                      }
                      CompactIconAction(AppIcon.CARD, tr(language, "保存选区模板", "Save selection as template"), { setTemplateName(""); setSaveSelectionTemplate(true) }, enabled = templatesAvailable)
                      CompactIconAction(AppIcon.TRASH, tr(language, "删除选中对象", "Delete selection"), {
                          val targets = selectedItems.flatMap { page.selectionMembers(it) }.distinct().sortedWith(compareBy<CanvasSelection> { it.type }.thenByDescending { it.index })
                          targets.forEach { page.deleteSelection(it) }
                          setSelection(null)
                          setSelectedItems(emptyList())
                          changed()
                      }, tint = LingyeColors.Coral)
                  }
              }
              Spacer(Modifier.height(6.dp))
          }
          Surface(
              modifier = Modifier.widthIn(max = 680.dp),
              shape = RoundedCornerShape(20.dp),
              color = LingyeColors.SurfaceHigh.copy(alpha = 0.97f),
              shadowElevation = 12.dp,
              border = BorderStroke(1.dp, LingyeColors.Line.copy(alpha = 0.9f))
          ) {
            BoxWithConstraints(Modifier) {
              val bottomScroll = rememberScrollState()
              Row(
                  Modifier
                      .horizontalScroll(bottomScroll)
                      .padding(horizontal = 7.dp, vertical = 5.dp),
                  horizontalArrangement = Arrangement.spacedBy(2.dp),
                  verticalAlignment = Alignment.CenterVertically
              ) {
                  val navigationActive = tool == "选择" || tool == "移动"
                  val displayedNavigation = if (navigationActive) tool else lastNavigationTool
                  val navigating = displayedNavigation == "移动"
                  ToolIconButton(
                      if (navigating) AppIcon.MOVE else AppIcon.SELECT,
                      when {
                          !navigationActive -> if (navigating) tr(language, "拖拽工具", "Hand tool") else tr(language, "选择工具", "Select tool")
                          navigating -> tr(language, "切换为选择", "Switch to select")
                          else -> tr(language, "切换为拖拽", "Switch to hand")
                      },
                      navigationActive && !toolbarMenuOpen && activeMenuButton == null,
                      false,
                      shortcut = if (navigating) "H" else "V"
                  ) {
                      if (navigationActive) chooseTool(if (tool == "移动") "选择" else "移动")
                      else chooseTool(lastNavigationTool)
                  }
                  ToolIconButton(AppIcon.FRAME, tr(language, "框选设置", "Frame settings"), tool == "Frame" && !toolbarMenuOpen, false, shortcut = "F") { chooseTool("Frame") }
                  ToolIconButton(AppIcon.CONNECT, tr(language, "连接参数", "Connector settings"), tool == "连线" && !toolbarMenuOpen, false, shortcut = "C") { enterConnectorTool() }
                  ToolIconButton(AppIcon.LINK, tr(language, "链接已有文档", "Link existing document"), false, false, shortcut = "@") {
                      dismissToolMenus()
                      setActiveMenuButton(null)
                      setLinkedDocPicker(true)
                  }
                  CanvasToolbarDivider()
                  IllustratedToolButton(R.drawable.canvas_tool_text_v2, tr(language, "文本工具", "Text tools"), textToolsMenu, shortcut = "T") {
                      if (textToolsMenu) {
                          setTextToolsMenu(false)
                          chooseTool("选择")
                      } else {
                          dismissToolMenus()
                          setActiveMenuButton(null)
                          setTool("文本")
                          setTextToolsMenu(true)
                      }
                  }
                  val penIsHighlighter = brushKind == BrushSettings.KIND_HIGHLIGHTER
                  IllustratedToolButton(
                      R.drawable.canvas_tool_pen_v2,
                      if (penIsHighlighter) tr(language, "高亮笔", "Highlighter") else tr(language, "画笔", "Pen"),
                      tool == "画笔" && !toolbarMenuOpen,
                      shortcut = if (penIsHighlighter) "⇧P" else "P"
                  ) { chooseTool("画笔") }
                  IllustratedToolButton(R.drawable.canvas_tool_eraser_v2, tr(language, "橡皮", "Eraser"), tool == "橡皮" && !toolbarMenuOpen, shortcut = "E") { chooseTool("橡皮") }
                  IllustratedToolButton(R.drawable.canvas_tool_shape_v2, tr(language, "形状", "Shapes"), tool in setOf("矩形", "圆角矩形", "圆形", "三角形", "菱形") && !toolbarMenuOpen, shortcut = "S") { chooseTool("矩形") }
                  Box {
                      IllustratedToolButton(R.drawable.canvas_tool_mindmap_v2, tr(language, "思维导图", "Mind map"), mindmapToolsMenu || activeMenuButton == "mindmap", shortcut = "M") {
                          if (mindmapToolsMenu) { setMindmapToolsMenu(false); setActiveMenuButton(null) } else {
                              selectMenuOption("mindmap")
                              setMindmapToolsMenu(true)
                          }
                      }
                      ContextualMenuHost(expanded = mindmapToolsMenu, onDismissRequest = { setMindmapToolsMenu(false) }, gap = ContextualToolbarTokens.bottomMenuGap) {
                          listOf("bubble" to tr(language,"气泡图","Bubble map"),"right" to tr(language,"向右树","Right tree"),"down" to tr(language,"向下树","Down tree"),"tree" to tr(language,"组织图","Organization"),"outline" to tr(language,"大纲","Outline")).forEach { (layout,label)->
                              CanvasMenuRow(
                                  label = label,
                                  icon = { MindmapLayoutPreview(layout, Modifier.size(20.dp)) }
                              ) { insertMindmap(layout);setTool("选择");setActiveMenuButton("mindmap");setMindmapToolsMenu(false) }
                          }
                      }
                  }
                  Box(Modifier.onGloballyPositioned { setContentButtonBounds(it.boundsInWindow().toIntRectPx()) }) {
                      IllustratedToolButton(R.drawable.canvas_tool_content_v2, tr(language, "内容与数据", "Content and data"), templateGalleryOpen || activeMenuButton == "content") {
                          if (templateGalleryOpen) { setTemplateGalleryOpen(false); setActiveMenuButton(null) } else {
                              selectMenuOption("content")
                              setTemplateGalleryOpen(true)
                          }
                      }
                  }
                  if (templateGalleryOpen) {
                      contentButtonBounds?.let { anchor ->
                          TemplateGalleryPopup(
                              anchor = anchor,
                              initialCategory = templateGalleryCategory,
                              language = language,
                              workspaceTemplates = workspaceTemplates,
                              pageTemplates = page.cardTemplates,
                              onAddImage = { setTemplateGalleryOpen(false); addImage() },
                              onAddDatabase = {
                                  setTemplateGalleryOpen(false)
                                  page.addDatabase(tr(language, "数据库", "Database"))
                                  changed()
                              },
                              onInsertWorkspace = { template ->
                                  setTemplateGalleryOpen(false)
                                  page.insertWorkspaceTemplate(template, 400f, 200f)
                                  changed()
                              },
                              onInsertPageTemplate = { index ->
                                  setTemplateGalleryOpen(false)
                                  page.insertCardTemplate(index, 400f, 200f)
                                  changed()
                              },
                              onManageWorkspaceTemplates = {
                                  setTemplateGalleryOpen(false)
                                  setManageTemplates(true)
                              },
                              onDismiss = { setTemplateGalleryOpen(false) },
                              onInsert = { template ->
                                  setTemplateGalleryOpen(false)
                                  templateScope.launch {
                                      try {
                                          val bytes = context.resources.openRawResource(template.resId).use { it.readBytes() }
                                          val name = withContext(Dispatchers.IO) { store.importImageBytes(bytes) }
                                          val width = if (template.aspect >= 1.4f) 320f else 240f
                                          val height = (width / template.aspect).coerceIn(40f, 700f)
                                          page.addCanvasImage(name, 400f, 140f, width, height)
                                          changed()
                                      } catch (e: Exception) {
                                          android.widget.Toast.makeText(
                                              context,
                                              tr(language, "模板插入失败", "Could not insert template"),
                                              android.widget.Toast.LENGTH_SHORT
                                          ).show()
                                      }
                                  }
                              },
                              onInsertCard = { preset ->
                                  setTemplateGalleryOpen(false)
                                  page.addCard(
                                      preset, 400f, 200f, language,
                                      settings.noteBackground, settings.noteCornerRadius,
                                      settings.noteBorderStyle, settings.noteBorderWidth, settings.noteShadow
                                  )
                                  changed()
                              }
                          )
                      }
                  }
                  CanvasToolbarDivider()
                  Box {
                      ToolIconButton(AppIcon.MORE, tr(language, "更多工具", "More tools"), moreToolsMenu || activeMenuButton == "more", false) {
                          if (moreToolsMenu) { setMoreToolsMenu(false); setActiveMenuButton(null) } else {
                              selectMenuOption("more")
                              setMoreToolsMenu(true)
                          }
                      }
                      ContextualMenuHost(expanded = moreToolsMenu, onDismissRequest = { setMoreToolsMenu(false) }, gap = ContextualToolbarTokens.bottomMenuGap) {
                          if (tool == "选择") {
                              CanvasMenuSection(tr(language, "选择操作", "Selection"))
                              CanvasMenuRow(
                                  label = if (multiSelect) tr(language, "多选：开", "Multi-select: on") else tr(language, "多行", "Multi-select"),
                                  icon = { WorkspaceIcon(AppIcon.SELECT, null, Modifier.size(18.dp), LingyeColors.Muted) },
                                  trailing = { if (multiSelect) Text("✓", color = LingyeColors.Primary) }
                              ) { setMultiSelect(!multiSelect); setSelectedItems(emptyList()); setSelection(null); setMoreToolsMenu(false) }
                              CanvasMenuRow(
                                  label = if (snapToGrid) tr(language, "网格吸附：开", "Grid snap: on") else tr(language, "网格吸附", "Grid snap"),
                                  icon = { WorkspaceIcon(AppIcon.OVERVIEW, null, Modifier.size(18.dp), LingyeColors.Muted) },
                                  trailing = { if (snapToGrid) Text("✓", color = LingyeColors.Primary) }
                              ) { setSnapToGrid(!snapToGrid); setMoreToolsMenu(false) }
                              CanvasMenuDivider()
                          }
                          CanvasMenuSection(tr(language, "视图", "View"))
                          CanvasMenuRow(
                              label = tr(language, if (focusMode) "退出全屏画布" else "全屏画布", if (focusMode) "Exit fullscreen canvas" else "Fullscreen canvas"),
                              icon = { WorkspaceIcon(if (focusMode) AppIcon.EXPAND_CLOSE else AppIcon.EXPAND_FULL, null, Modifier.size(18.dp), LingyeColors.Muted) }
                          ) { setFocusMode(!focusMode); setMoreToolsMenu(false) }
                          CanvasMenuRow(
                              label = tr(language, "复位视图", "Fit view"),
                              icon = { WorkspaceIcon(AppIcon.RESET, null, Modifier.size(18.dp), LingyeColors.Muted) }
                          ) { setScale(1f); setPan(Offset.Zero); setMoreToolsMenu(false) }
                          CanvasMenuRow(
                              label = tr(language, "Frame 导航", "Frame navigator"),
                              icon = { WorkspaceIcon(AppIcon.FRAME, null, Modifier.size(18.dp), LingyeColors.Muted) }
                          ) { setMoreToolsMenu(false); setFrameNavigator(true) }
                          CanvasMenuRow(
                              label = tr(language, "演示 Frame", "Present frame"),
                              icon = { WorkspaceIcon(AppIcon.EXPAND_FULL, null, Modifier.size(18.dp), LingyeColors.Muted) }
                          ) {
                              setMoreToolsMenu(false)
                              val target = selectedFrameIndex ?: page.shapes.indexOfFirst { it.type == "frame" }
                              if (target >= 0) startPresentation(target)
                          }
                      }
                  }
                  ToolIconButton(AppIcon.UNDO, tr(language, "撤销", "Undo"), false, false, shortcut = "Ctrl+Z", onClick = undo)
                  ToolIconButton(AppIcon.REDO, tr(language, "重做", "Redo"), false, false, shortcut = "Ctrl+Shift+Z", onClick = redo)
              }
            }
          }
        }
}
