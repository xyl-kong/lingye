package com.example.tabletworkspace

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CanvasTwoFingerInputTest {
    @get:Rule val compose = createComposeRule()

    @Test fun pinchInPenModeNavigatesAndReleasesLock() {
        val page = Page.create("gesture test")
        val state = CanvasEditorState(page, 1f, 0f, 0f)
        var cancellations = 0
        compose.setContent {
            Box(
                Modifier.size(400.dp)
                    .testTag("canvas")
                    .canvasTwoFingerNavigationInput(page, "画笔", null, state) { cancellations++ }
            )
        }
        compose.onNodeWithTag("canvas").performTouchInput {
            down(0, Offset(100f, 100f))
            down(1, Offset(200f, 100f))
            updatePointerTo(0, Offset(90f, 110f))
            updatePointerTo(1, Offset(210f, 110f))
            move()
            up(0)
            up(1)
        }
        compose.runOnIdle {
            assertEquals(1, cancellations)
            assertEquals(1, state.viewportGestureGeneration.intValue)
            assertEquals(1.2f, state.scale.floatValue, 0.001f)
            assertEquals(-30f, state.pan.value.x, 0.001f)
            assertEquals(-10f, state.pan.value.y, 0.001f)
            assertFalse(state.viewportGestureActive.value)
        }
    }
}
