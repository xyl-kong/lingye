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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
internal fun CanvasDialogHost(
    page: Page,
    pages: List<Page>,
    language: AppLanguage,
    store: PageStore,
    workspaceTemplates: List<CardTemplate>,
    templatesAvailable: Boolean,
    replaceTemplates: (List<CardTemplate>) -> Unit,
    selectedItems: List<CanvasSelection>,
    state: CanvasEditorState,
    onChanged: () -> Unit,
    onPresentFrame: (Int) -> Unit,
    onFitFrame: (Shape) -> Unit
) {
    var selection by state.selection
    var selectedItems by state.selectedItems
    var mindmapParentPicker by state.mindmapParentPicker
    var mindmapDeleteId by state.mindmapDeleteId
    var linkedDocPicker by state.linkedDocPicker
    var frameNavigator by state.frameNavigator
    var frameRename by state.frameRename
    var frameTitleText by state.frameTitleText
    var manageTemplates by state.manageTemplates
    var renameTemplate by state.renameTemplate
    var removeTemplate by state.removeTemplate
    var templateSave by state.templateSave
    var templateName by state.templateName
    var saveSelectionTemplate by state.saveSelectionTemplate
    var cropTarget by state.cropTarget
    var cropLeft by state.cropLeft
    var cropTop by state.cropTop
    var cropRight by state.cropRight
    var cropBottom by state.cropBottom
    fun changed() = onChanged()
    fun startPresentation(i: Int) = onPresentFrame(i)
    fun fitFrameViewport(frame: Shape) = onFitFrame(frame)
        mindmapParentPicker?.let { nodeId ->
            val node = page.shapes.firstOrNull { it.id == nodeId }
            val candidates = node?.let { n -> Mindmap.nodes(page, Mindmap.mapIdOf(n).orEmpty()).filter { it.id != n.id && Mindmap.descendants(page, n).none { d -> d.id == it.id } } }.orEmpty()
            val currentParent = node?.let { Mindmap.parent(page, it)?.id }
            AlertDialog(
                onDismissRequest = { mindmapParentPicker = null },
                title = { Text(tr(language, "改接上级节点", "Reparent node")) },
                text = {
                    Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                node?.let { n ->
                                    page.connectors.removeAll { Mindmap.isEdge(it) && it.endShape == n.id }
                                    Mindmap.reflow(page, n)
                                }
                                changed(); mindmapParentPicker = null
                            }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WorkspaceIcon(AppIcon.FRAME, null, Modifier.size(18.dp), LingyeColors.Muted)
                            Spacer(Modifier.width(10.dp))
                            Text(tr(language, "设为根节点", "Make root"), Modifier.weight(1f))
                        }
                        candidates.filter { it.id != currentParent }.forEach { candidate ->
                            Row(
                                Modifier.fillMaxWidth().clickable {
                                    node?.let { n ->
                                        if (Mindmap.reparent(page, n, candidate)) Mindmap.reflow(page, n)
                                    }
                                    changed(); mindmapParentPicker = null
                                }.padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                WorkspaceIcon(AppIcon.CONNECT, null, Modifier.size(18.dp), LingyeColors.Muted)
                                Spacer(Modifier.width(10.dp))
                                Text(candidate.title.ifBlank { tr(language, "未命名节点", "Untitled node") }, Modifier.weight(1f), maxLines = 1)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { mindmapParentPicker = null }) { Text(tr(language, "关闭", "Close")) } }
            )
        }
        mindmapDeleteId?.let { id ->
            val node = page.shapes.firstOrNull { it.id == id }
            val count = node?.let { Mindmap.descendants(page, it).size + 1 } ?: 0
            AlertDialog(
                onDismissRequest = { mindmapDeleteId = null },
                title = { Text(tr(language, "删除节点？", "Delete node?")) },
                text = { Text(tr(language, "将删除该节点及其子节点，共 $count 个节点。", "This removes the node and its descendants ($count nodes total).")) },
                confirmButton = {
                    TextButton(onClick = {
                        node?.let { Mindmap.deleteSubtree(page, it) }
                        selection = null; selectedItems = emptyList(); changed(); mindmapDeleteId = null
                    }) { Text(tr(language, "删除", "Delete")) }
                },
                dismissButton = { TextButton(onClick = { mindmapDeleteId = null }) { Text(tr(language, "取消", "Cancel")) } }
            )
        }
        if (linkedDocPicker) {
            val targets = pages.filter { it.id != page.id && !it.trashed }.sortedBy { it.title.lowercase() }
            val nextIndex = page.shapes.count { it.type == PageLinks.SHAPE_TYPE }
            AlertDialog(
                onDismissRequest = { linkedDocPicker = false },
                title = { Text(tr(language, "链接已有文档", "Link existing document")) },
                text = {
                    Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                        if (targets.isEmpty()) Text(tr(language, "没有其他可链接的页面。", "There are no other pages to link."))
                        targets.forEach { target ->
                            Row(
                                Modifier.fillMaxWidth()
                                    .clickable {
                                        val index = page.addLinkedDoc(target, 420f + 30f * (nextIndex % 5), 200f + 30f * (nextIndex % 5))
                                        selection = CanvasSelection("shape", index)
                                        selectedItems = listOf(CanvasSelection("shape", index))
                                        linkedDocPicker = false
                                        changed()
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                WorkspaceIcon(AppIcon.DOCUMENT, null, Modifier.size(18.dp), LingyeColors.Muted)
                                Spacer(Modifier.width(10.dp))
                                Text(target.title.ifBlank { tr(language, "未命名页面", "Untitled page") }, Modifier.weight(1f), maxLines = 1)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { linkedDocPicker = false }) { Text(tr(language, "关闭", "Close")) } }
            )
        }
        if (frameNavigator) {
            val frames = page.shapes.indices.filter { page.shapes[it].type == "frame" }
            AlertDialog(
                onDismissRequest = { frameNavigator = false },
                title = { Text(tr(language, "Frame 导航", "Frame navigator")) },
                text = {
                    Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                        if (frames.isEmpty()) Text(tr(language, "当前页面还没有 Frame。", "This page has no frames yet."))
                        frames.forEach { i ->
                            val frame = page.shapes.getOrNull(i) ?: return@forEach
                            val count = page.frameMembers(i).size
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(frame.title.ifBlank { tr(language, "框架", "Frame") }, fontWeight = FontWeight.SemiBold)
                                    Text(tr(language, "$count 个对象", "$count objects"), color = LingyeColors.Muted, fontSize = 12.sp)
                                }
                                TextButton(onClick = { frameNavigator = false; startPresentation(i) }) { Text(tr(language, "演示", "Present"), color = LingyeColors.Primary) }
                                TextButton(onClick = { fitFrameViewport(frame); frameNavigator = false }) { Text(tr(language, "定位", "Locate"), color = LingyeColors.Primary) }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { frameNavigator = false }) { Text(tr(language, "关闭", "Close")) } }
            )
        }
        frameRename?.let { index ->
            AlertDialog(
                onDismissRequest = { frameRename = null },
                title = { Text(tr(language, "重命名 Frame", "Rename frame")) },
                text = { OutlinedTextField(frameTitleText, { frameTitleText = it.take(100) }, label = { Text(tr(language, "Frame 名称", "Frame name")) }, singleLine = true) },
                confirmButton = {
                    TextButton(enabled = frameTitleText.isNotBlank(), onClick = {
                        page.shapes.getOrNull(index)?.let { page.shapes[index] = it.copy(title = frameTitleText.trim()); changed() }
                        frameRename = null
                    }) { Text(tr(language, "保存", "Save")) }
                },
                dismissButton = { TextButton(onClick = { frameRename = null }) { Text(tr(language, "取消", "Cancel")) } }
            )
        }
        if(manageTemplates) AlertDialog(onDismissRequest={manageTemplates=false},title={Text(tr(language,"管理工作区模板","Manage workspace templates"))},text={Column(Modifier.heightIn(max=400.dp).verticalScroll(rememberScrollState())) { workspaceTemplates.forEachIndexed { i,t -> Row(verticalAlignment=Alignment.CenterVertically) { Text(t.name,Modifier.weight(1f)); TextButton(onClick={templateName=t.name; renameTemplate=i; manageTemplates=false}) {Text(tr(language,"重命名","Rename"))}; TextButton(onClick={removeTemplate=i; manageTemplates=false}) {Text(tr(language,"删除","Delete"))} } } }},confirmButton={TextButton(onClick={manageTemplates=false}) {Text(tr(language,"完成","Done"))}})
        renameTemplate?.let { i -> AlertDialog(onDismissRequest={renameTemplate=null},title={Text(tr(language,"重命名模板","Rename template"))},text={OutlinedTextField(templateName,{templateName=it.take(100)},singleLine=true)},confirmButton={TextButton(enabled=templateName.isNotBlank(),onClick={replaceTemplates(workspaceTemplates.toMutableList().apply { this[i]=this[i].copy(name=templateName.trim()) }); renameTemplate=null}) {Text(tr(language,"保存","Save"))}},dismissButton={TextButton(onClick={renameTemplate=null}) {Text(tr(language,"取消","Cancel"))}}) }
        removeTemplate?.let { i -> AlertDialog(onDismissRequest={removeTemplate=null},title={Text(tr(language,"删除模板？","Delete template?"))},text={Text(tr(language,"已创建的卡片不会受到影响。","Existing cards will not be affected."))},confirmButton={TextButton(onClick={replaceTemplates(workspaceTemplates.toMutableList().apply { removeAt(i) }); removeTemplate=null}) {Text(tr(language,"删除","Delete"))}},dismissButton={TextButton(onClick={removeTemplate=null}) {Text(tr(language,"取消","Cancel"))}}) }
        templateSave?.let { index -> AlertDialog(onDismissRequest={templateSave=null},title={Text(tr(language,"保存为模板","Save as template"))},text={OutlinedTextField(templateName,{templateName=it.take(100)},label={Text(tr(language,"模板名称","Template name"))},singleLine=true)},confirmButton={TextButton(enabled=templatesAvailable && templateName.isNotBlank() && workspaceTemplates.none { it.name.equals(templateName.trim(),true) },onClick={val card=page.shapes[index]; replaceTemplates(workspaceTemplates + CardTemplate(templateName.trim(),card.title,card.body,card.color,card.width,card.height)); templateSave=null}) {Text(tr(language,"保存","Save"))}},dismissButton={TextButton(onClick={templateSave=null}) {Text(tr(language,"取消","Cancel"))}}) }
        if(saveSelectionTemplate) AlertDialog(onDismissRequest={saveSelectionTemplate=false},title={Text(tr(language,"保存选区模板","Save selection as template"))},text={Column { Text(tr(language,"包含选中对象以及选中图形之间的连线。","Includes selected objects and connections between selected shapes."));OutlinedTextField(templateName,{templateName=it.take(100)},label={Text(tr(language,"模板名称","Template name"))},singleLine=true) }},confirmButton={TextButton(enabled=templateName.isNotBlank() && workspaceTemplates.none { it.name.equals(templateName.trim(),true) },onClick={replaceTemplates(workspaceTemplates+page.makeCanvasTemplate(templateName,selectedItems));saveSelectionTemplate=false}) {Text(tr(language,"保存","Save"))}},dismissButton={TextButton(onClick={saveSelectionTemplate=false}) {Text(tr(language,"取消","Cancel"))}})
        cropTarget?.let { id -> val image=page.images.firstOrNull { it.id==id };if(image!=null) { val bitmap=rememberStoredImage(store,image.name);AlertDialog(onDismissRequest={cropTarget=null},title={Text(tr(language,"裁剪图片","Crop image"))},text={Column(Modifier.widthIn(max=540.dp).verticalScroll(rememberScrollState())) { Text(tr(language,"调整原图的四条边，预览不包含旋转和翻转。","Adjust the original image edges; preview excludes rotation and flips."));if(bitmap!=null) Canvas(Modifier.fillMaxWidth().height(180.dp).background(LingyeColors.PreviewBg)) { val sx=(bitmap.width*cropLeft).toInt().coerceIn(0,bitmap.width-1);val sy=(bitmap.height*cropTop).toInt().coerceIn(0,bitmap.height-1);val ex=(bitmap.width*cropRight).roundToInt().coerceIn(sx+1,bitmap.width);val ey=(bitmap.height*cropBottom).roundToInt().coerceIn(sy+1,bitmap.height);val factor=min(size.width/(ex-sx),size.height/(ey-sy));val w=((ex-sx)*factor).roundToInt().coerceAtLeast(1);val h=((ey-sy)*factor).roundToInt().coerceAtLeast(1);drawImage(bitmap,srcOffset=IntOffset(sx,sy),srcSize=IntSize(ex-sx,ey-sy),dstOffset=IntOffset(((size.width-w)/2).roundToInt(),((size.height-h)/2).roundToInt()),dstSize=IntSize(w,h)) };Text(tr(language,"左边","Left")+" ${(cropLeft*100).roundToInt()}%");CompactSlider(cropLeft,{cropLeft=it.coerceAtMost((cropRight-0.06f).coerceAtLeast(0f))},valueRange=0f..0.94f);Text(tr(language,"右边","Right")+" ${(cropRight*100).roundToInt()}%");CompactSlider(cropRight,{cropRight=it.coerceAtLeast((cropLeft+0.06f).coerceAtMost(1f))},valueRange=0.06f..1f);Text(tr(language,"上边","Top")+" ${(cropTop*100).roundToInt()}%");CompactSlider(cropTop,{cropTop=it.coerceAtMost((cropBottom-0.06f).coerceAtLeast(0f))},valueRange=0f..0.94f);Text(tr(language,"下边","Bottom")+" ${(cropBottom*100).roundToInt()}%");CompactSlider(cropBottom,{cropBottom=it.coerceAtLeast((cropTop+0.06f).coerceAtMost(1f))},valueRange=0.06f..1f);TextButton(onClick={cropLeft=0f;cropTop=0f;cropRight=1f;cropBottom=1f}) {Text(tr(language,"恢复原图范围","Reset crop"))} }},confirmButton={TextButton(onClick={val index=page.images.indexOfFirst { it.id==id };if(index>=0) {page.cropCanvasImage(index,cropLeft,cropTop,cropRight,cropBottom);changed()};cropTarget=null}) {Text(tr(language,"应用","Apply"))}},dismissButton={TextButton(onClick={cropTarget=null}) {Text(tr(language,"取消","Cancel"))}}) } }
}
