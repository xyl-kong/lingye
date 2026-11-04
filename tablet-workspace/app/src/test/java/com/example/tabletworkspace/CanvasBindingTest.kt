package com.example.tabletworkspace

import org.junit.Assert.*
import org.junit.Test

class CanvasBindingTest {
    @Test fun boundConnectorFollowsShapeAndSurvivesRoundTrip() {
        val page = Page.create("board")
        page.addShape("rect", 10f, 20f, 100f, 80f)
        page.addShape("rect", 300f, 20f, 100f, 80f)
        page.addBoundConnector(0, 1)
        val before = page.resolvedConnector(0)
        page.moveSelection(CanvasSelection("shape", 0), 50f, 30f)
        val after = page.resolvedConnector(0)
        assertEquals(before.x1 + 50f, after.x1, 0.001f)
        assertEquals(before.y1 + 30f, after.y1, 0.001f)
        val saved = Page.decode(page.encode())
        assertEquals(after, saved.resolvedConnector(0))
    }
    @Test fun groupMovesMembersTogetherAndSurvivesRoundTrip() {
        val page = Page.create("board")
        page.addShape("rect", 0f, 0f, 50f, 50f)
        page.addShape("ellipse", 100f, 0f, 50f, 50f)
        page.groupSelections(listOf(CanvasSelection("shape", 0), CanvasSelection("shape", 1)))
        val saved = Page.decode(page.encode())
        saved.moveSelection(CanvasSelection("shape", 0), 20f, 30f)
        assertEquals(120f, saved.shapes[1].x, 0.001f)
    }
    @Test fun deletingBoundShapeDetachesAtLastPosition() {
        val p=Page.create("x"); p.addShape("rect",0f,0f,20f,20f); p.addShape("rect",100f,0f,20f,20f); p.addBoundConnector(0,1)
        p.moveSelection(CanvasSelection("shape",0), 20f, 10f); val before=p.resolvedConnector(0)
        p.deleteSelection(CanvasSelection("shape",0)); val after=Page.decode(p.encode()).resolvedConnector(0)
        assertEquals(before.x1,after.x1,0.001f); assertEquals(before.y1,after.y1,0.001f)
        assertEquals("",after.startShape); assertNotEquals("",after.endShape)
    }
    @Test fun ungroupRestoresIndependentMovement() {
        val p=Page.create("x"); p.addShape("rect",0f,0f,20f,20f); p.addShape("rect",100f,0f,20f,20f)
        val items=listOf(CanvasSelection("shape",0),CanvasSelection("shape",1)); p.groupSelections(items); p.ungroupSelections(listOf(items[0]))
        p.moveSelection(items[0],20f,0f); assertEquals(100f,p.shapes[1].x,0.001f)
    }}

