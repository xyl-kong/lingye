package com.example.tabletworkspace

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CanvasEditorCommandsTest {

    private fun state(page: Page) = CanvasEditorState(page, 1f, 0f, 0f)

    private fun commands(
        page: Page,
        state: CanvasEditorState,
        setTool: (String) -> Unit = {},
        setFocusMode: (Boolean) -> Unit = {},
        changed: () -> Unit = {}
    ) = CanvasEditorCommands(
        page = page,
        state = state,
        density = 2f,
        focusMode = false,
        tool = "选择",
        setTool = setTool,
        setFocusMode = setFocusMode,
        changed = changed,
        closeOverlays = {}
    )

    @Test
    fun selectionTargetsMapSingleObjectByType() {
        val page = Page.create("p")
        page.addShape("rect", 0f, 0f, 10f, 10f)
        page.addTextBox(20f, 0f, "text")
        page.addCard(CardPreset.NOTE, 40f, 0f)
        page.addShape("frame", 60f, 0f, 100f, 100f)
        page.addCanvasImage("missing.img", 0f, 300f, 10f, 10f)

        val rect = CanvasSelectionTargets.of(page, CanvasSelection("shape", 0), 1)
        assertEquals(0, rect.shape)
        assertNull(rect.card)
        assertEquals(1, CanvasSelectionTargets.of(page, CanvasSelection("shape", 1), 1).text)
        assertEquals(2, CanvasSelectionTargets.of(page, CanvasSelection("shape", 2), 1).card)
        assertEquals(3, CanvasSelectionTargets.of(page, CanvasSelection("shape", 3), 1).frame)
        assertEquals(0, CanvasSelectionTargets.of(page, CanvasSelection("image", 0), 1).image)
    }

    @Test
    fun selectionTargetsAreEmptyForMultiSelectionOrOutOfRange() {
        val page = Page.create("p")
        page.addShape("rect", 0f, 0f, 10f, 10f)
        val multi = CanvasSelectionTargets.of(page, CanvasSelection("shape", 0), 2)
        assertNull(multi.shape)
        assertNull(multi.card)
        val stale = CanvasSelectionTargets.of(page, CanvasSelection("shape", 9), 1)
        assertNull(stale.shape)
        assertEquals(CanvasSelectionTargets(null, null, null, null, null, null, null, null, null),
            CanvasSelectionTargets.of(page, null, 0))
    }

    @Test
    fun deleteSelectionRemovesObjectsAndClearsState() {
        val page = Page.create("p")
        page.addShape("rect", 0f, 0f, 10f, 10f)
        page.addTextBox(40f, 0f, "text")
        val st = state(page)
        st.selection.value = CanvasSelection("shape", 0)
        st.selectedItems.value = listOf(CanvasSelection("shape", 0))
        st.cardEdit.value = 1
        var changed = 0
        commands(page, st, changed = { changed++ }).deleteSelection()
        assertEquals(1, page.shapes.size)
        assertNull(st.selection.value)
        assertTrue(st.selectedItems.value.isEmpty())
        assertNull(st.cardEdit.value)
        assertEquals(1, changed)
    }

    @Test
    fun zoomKeepsViewportCenterFixed() {
        val page = Page.create("p")
        val st = state(page)
        st.viewportSizePx.value = IntSize(800, 600)
        commands(page, st).zoomBy(1.25f)
        assertEquals(1.25f, st.scale.floatValue, 0.0001f)
        assertEquals(Offset(-100f, -75f), st.pan.value)
    }

    @Test
    fun selectAllSkipsLockedShapesAndIncludesOtherLayers() {
        val page = Page.create("p")
        page.addShape("rect", 0f, 0f, 10f, 10f)
        page.addTextBox(20f, 0f, "text")
        page.shapes[1] = page.shapes[1].copy(locked = true)
        page.addConnector(0f, 0f, 10f, 10f)
        page.addStyledStroke(listOf(Point(0f, 0f), Point(4f, 4f)), 0xff000000.toInt(), 2f)
        val st = state(page)
        commands(page, st).selectAll()
        assertEquals(
            listOf(CanvasSelection("shape", 0), CanvasSelection("connector", 0), CanvasSelection("stroke", 0)),
            st.selectedItems.value
        )
        assertEquals(CanvasSelection("shape", 0), st.selection.value)
    }

    @Test
    fun startAndStopPresentationRestoreToolScaleAndPan() {
        val page = Page.create("p")
        page.addShape("frame", 0f, 0f, 400f, 300f)
        val st = state(page)
        st.viewportSizePx.value = IntSize(800, 600)
        st.presentationFitMode.value = "fit"
        var tool = "选择"
        var focus = false
        val cmds = commands(page, st, setTool = { tool = it }, setFocusMode = { focus = it })
        cmds.startPresentation(0)
        assertTrue(st.presentingFrameId.value != null)
        assertTrue(focus)
        assertTrue(st.scale.floatValue < 1f)
        cmds.stopPresentation()
        assertNull(st.presentingFrameId.value)
        assertFalse(focus)
        assertEquals(1f, st.scale.floatValue, 0.0001f)
        assertEquals(Offset.Zero, st.pan.value)
        assertEquals("选择", tool)
    }

    @Test
    fun fitFrameViewportSelectsTheFrame() {
        val page = Page.create("p")
        page.addShape("frame", 0f, 0f, 400f, 300f)
        val st = state(page)
        st.viewportSizePx.value = IntSize(800, 600)
        st.selection.value = null
        st.selectedItems.value = emptyList()
        commands(page, st).fitFrameViewport(page.shapes[0])
        assertEquals(CanvasSelection("shape", 0), st.selection.value)
        assertEquals(listOf(CanvasSelection("shape", 0)), st.selectedItems.value)
        assertTrue(st.scale.floatValue in FrameSemantics.MIN_SCALE..FrameSemantics.MAX_SCALE)
        assertNotNull(st.pan.value)
    }
}
