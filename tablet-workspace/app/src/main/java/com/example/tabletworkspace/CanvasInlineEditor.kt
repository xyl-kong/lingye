@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.tabletworkspace

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

@Composable
internal fun CanvasInlineEditor(
    page: Page,
    language: AppLanguage,
    settings: WorkspaceSettings,
    scale: Float,
    viewport: Viewport,
    state: CanvasEditorState,
    focusRequester: FocusRequester,
    changedText: (String?) -> Unit,
    onExitEdit: () -> Unit
) {
    var cardEdit by state.cardEdit
    var textEditValue by state.textEditValue
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    fun handleInlineKey(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) return false
        if (event.key == Key.Escape || (event.key == Key.Enter && event.isCtrlPressed)) {
            focusManager.clearFocus()
            onExitEdit()
            return true
        }
        return false
    }
        cardEdit?.let { index -> page.shapes.getOrNull(index)?.takeIf { it.type in setOf("card", "text", Mindmap.NODE_TYPE) }?.let { card ->
            val at = viewport.toScreen(Point(card.x, card.y))
            Surface(
                Modifier.offset(x = at.x.dp.coerceAtLeast(8.dp), y = at.y.dp.coerceAtLeast(8.dp)).width((card.width * scale).dp).heightIn(min = (card.height * scale).dp, max = 620.dp)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                            onExitEdit()
                            true
                        } else false
                    },
                shape = RoundedCornerShape(if (card.type == "text") card.cornerRadius.dp else CanvasUiTokens.parameterSurfaceRadius),
                color = when {
                    card.type == "text" && Color(card.color).alpha > 0f -> Color(card.color)
                    card.type == "text" -> LingyeColors.SurfaceHigh.copy(alpha = 0.97f)
                    else -> Color(card.color)
                },
                shadowElevation = if (card.type == "text") 0.dp else 18.dp
            ) {
                Column(Modifier.padding(if (card.type == "text") 24.dp * scale else 12.dp)) {
                    if (card.type == "card") {
                        BasicTextField(
                            card.title,
                            { page.updateShapeCardText(index, it, page.shapes[index].body); changedText("shape-title:${card.id}") },
                            Modifier.fillMaxWidth().focusRequester(focusRequester).onPreviewKeyEvent(::handleInlineKey),
                            textStyle = LocalTextStyle.current.copy(fontSize = (settings.editorFontSize + 2).sp, fontWeight = FontWeight.Bold, color = Color(card.textColor)),
                            singleLine = true,
                            cursorBrush = SolidColor(LingyeColors.Primary)
                        )
                    }
                    if (card.type == Mindmap.NODE_TYPE) {
                        BasicTextField(
                            card.title,
                            { page.shapes[index] = page.shapes[index].copy(title = it.take(500)); changedText("mind-node:${card.id}") },
                            Modifier.fillMaxWidth().focusRequester(focusRequester).onPreviewKeyEvent(::handleInlineKey),
                            textStyle = LocalTextStyle.current.copy(fontSize = settings.editorFontSize.sp, fontWeight = FontWeight.Bold, color = Color(card.textColor)),
                            singleLine = true,
                            cursorBrush = SolidColor(LingyeColors.Primary)
                        )
                    }
                    if (card.type == "text") {
                        val value = textEditValue ?: TextFieldValue(card.body, androidx.compose.ui.text.TextRange(card.body.length))
                        val baseFamily = when (card.fontFamily) {
                            "serif" -> FontFamily.Serif
                            "mono" -> FontFamily.Monospace
                            else -> FontFamily.SansSerif
                        }
                        BasicTextField(
                            value,
                            { new ->
                                val current = page.shapes[index]
                                val spans = TextFormatting.adjustForEdit(current.textSpans, current.body, new.text)
                                var updated = current.copy(
                                    body = new.text,
                                    textSpans = spans,
                                    cardKind = TextEquations.adjust(
                                        TextComments.adjust(
                                            TextLinks.adjust(current.cardKind, current.body, new.text),
                                            current.body, new.text
                                        ),
                                        current.body, new.text
                                    )
                                )
                                if (textAutoHeight(updated)) {
                                    updated = updated.copy(height = autoFitTextHeight(updated.body, updated.width, updated.fontSize))
                                }
                                page.shapes[index] = updated
                                textEditValue = new
                                changedText("shape-text:${card.id}")
                            },
                            Modifier.fillMaxWidth().focusRequester(focusRequester).onPreviewKeyEvent(::handleInlineKey).heightIn(
                                min = ((card.height * scale).dp - (48.dp * scale))
                                    .coerceAtLeast((40.dp * scale).coerceAtLeast(8.dp))
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = (card.fontSize * scale).sp,
                                color = Color(card.textColor),
                                fontFamily = baseFamily,
                                fontStyle = if (card.fontStyle == "italic") FontStyle.Italic else FontStyle.Normal,
                                fontWeight = FontWeight(card.fontWeight),
                                textAlign = when (card.textAlign) {
                                    "center" -> TextAlign.Center
                                    "right" -> TextAlign.End
                                    else -> TextAlign.Start
                                },
                                textDecoration = TextDecoration.None
                            ),
                            cursorBrush = SolidColor(LingyeColors.Primary),
                            visualTransformation = TextSpanVisualTransformation(card.textSpans)
                        )
                    } else if (card.type == "card") {
                        BasicTextField(
                            card.body,
                            { page.updateShapeCardText(index, page.shapes[index].title, it); changedText("shape-text:${card.id}") },
                            Modifier.fillMaxWidth().focusRequester(focusRequester).onPreviewKeyEvent(::handleInlineKey).padding(top = 8.dp).heightIn(min = (card.height * scale * 0.56f).dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = (settings.editorFontSize - 3).sp, color = Color(card.textColor)),
                            cursorBrush = SolidColor(LingyeColors.Primary)
                        )
                    }
                }
            }
        } }
}
