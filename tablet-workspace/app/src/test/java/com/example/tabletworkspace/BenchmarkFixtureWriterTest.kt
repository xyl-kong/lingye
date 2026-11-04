package com.example.tabletworkspace

import java.io.DataInputStream
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Writes optional large-page fixtures under build/benchmark-fixtures for emulator stress tests. */
class BenchmarkFixtureWriterTest {
    @Test
    fun writesLargeCanvasAndDocumentFixtures() {
        val dir = File("build/benchmark-fixtures").apply { mkdirs() }
        val canvas = Page.create("Benchmark Canvas")
        repeat(1000) { i ->
            val column = i % 50
            val row = i / 50
            canvas.shapes.add(
                Shape(
                    "rect",
                    80f + column * 120f,
                    80f + row * 90f,
                    80f,
                    50f,
                    color = if (i % 2 == 0) 0xffffd866.toInt() else 0xff8ab4f8.toInt()
                )
            )
        }
        val canvasFile = File(dir, "canvas.page").apply { writeBytes(canvas.encode()) }

        val document = Page.create("Benchmark Document")
        repeat(1000) { i ->
            document.addBlock("paragraph", "Paragraph $i: a long document used to test scrolling, editing and autosave.")
        }
        val documentFile = File(dir, "document.page").apply { writeBytes(document.encode()) }

        val canvasEncodeStart = System.nanoTime()
        val canvasBytes = canvas.encode()
        val canvasEncodeMs = (System.nanoTime() - canvasEncodeStart) / 1_000_000.0
        val canvasDecodeStart = System.nanoTime()
        Page.decode(canvasBytes)
        val canvasDecodeMs = (System.nanoTime() - canvasDecodeStart) / 1_000_000.0
        val documentEncodeStart = System.nanoTime()
        val documentBytes = document.encode()
        val documentEncodeMs = (System.nanoTime() - documentEncodeStart) / 1_000_000.0
        val documentDecodeStart = System.nanoTime()
        Page.decode(documentBytes)
        val documentDecodeMs = (System.nanoTime() - documentDecodeStart) / 1_000_000.0
        File(dir, "timings.txt").writeText(
            """
            canvas bytes=${canvasBytes.size}
            canvas encodeMs=${"%.2f".format(canvasEncodeMs)}
            canvas decodeMs=${"%.2f".format(canvasDecodeMs)}
            document bytes=${documentBytes.size}
            document encodeMs=${"%.2f".format(documentEncodeMs)}
            document decodeMs=${"%.2f".format(documentDecodeMs)}
            """.trimIndent()
        )

        fun pageId(file: File): String = DataInputStream(file.inputStream()).use {
            it.readInt()
            it.readUTF()
        }
        File(dir, "index.txt").writeText("canvas=${pageId(canvasFile)}\ndocument=${pageId(documentFile)}\n")

        assertTrue(canvasFile.length() > 50_000)
        assertTrue(documentFile.length() > 50_000)
        assertEquals(1000, Page.decode(canvasFile.readBytes()).shapes.size)
        assertEquals(1000, Page.decode(documentFile.readBytes()).blocks.size)
        assertEquals(25, pageFormatVersion(documentFile.readBytes()))
    }
}
