package com.example.tabletworkspace

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContextualToolbarHostTest {

    @get:Rule
    val compose = createComposeRule()

    private fun setHost(
        anchorTop: Float,
        anchorBottom: Float,
        imeBottom: Float = 0f
    ) {
        compose.setContent {
            LingyeTheme {
                Box(Modifier.size(600.dp, 400.dp)) {
                    ContextualToolbarHost(
                        anchor = CanvasToolbarAnchor(
                            x = 300.dp,
                            y = anchorTop.dp,
                            bottom = anchorBottom.dp
                        ),
                        viewportWidth = 600.dp,
                        viewportHeight = 400.dp,
                        imeBottom = imeBottom.dp,
                        objectKind = CanvasObjectKind.TEXT
                    ) {
                        Text("toolbar", Modifier.testTag("toolbar-content"))
                    }
                }
            }
        }
        compose.waitForIdle()
    }

    private fun bounds() = compose.onNodeWithTag("object-toolbar-text").fetchSemanticsNode().boundsInRoot

    @Test
    fun hostIsTaggedAndStaysInsideViewport() {
        setHost(anchorTop = 300f, anchorBottom = 350f)
        compose.onNodeWithTag("object-toolbar-text").assertIsDisplayed()
        compose.onNodeWithTag("toolbar-content").assertIsDisplayed()
        val b = bounds()
        val density = compose.density.density
        assertTrue(b.left >= 0f)
        assertTrue(b.right <= 600f * density + 1f)
        assertTrue(b.top >= 0f)
        assertTrue(b.bottom <= 400f * density)
    }

    @Test
    fun hostFlipsBelowWhenThereIsNoRoomAbove() {
        setHost(anchorTop = 10f, anchorBottom = 40f)
        val b = bounds()
        val density = compose.density.density
        assertTrue(b.top >= 40f * density - 1f)
    }

    @Test
    fun hostAvoidsImeArea() {
        setHost(anchorTop = 350f, anchorBottom = 390f, imeBottom = 150f)
        val b = bounds()
        val density = compose.density.density
        assertTrue(b.bottom <= 250f * density + 1f)
    }
}
