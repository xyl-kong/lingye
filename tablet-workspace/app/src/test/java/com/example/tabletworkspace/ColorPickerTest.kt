package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorPickerTest {
    @Test
    fun rgbHsvRoundTripKeepsRgbValues() {
        listOf(
            0xffef7076.toInt(),
            0xfff08c43.toInt(),
            0xff57b85e.toInt(),
            0xff79b8ef.toInt(),
            0xff111111.toInt(),
            0xffffffff.toInt()
        ).forEach { color ->
            val (h, s, v) = rgbToHsv(color)
            val back = hsvToRgb(h, s, v)
            assertEquals(color and 0xFFFFFF, back and 0xFFFFFF)
        }
    }

    @Test
    fun hexIgnoresAlphaAndFormatsSixDigits() {
        assertEquals("#EF7076", toHexRgb(0x80EF7076.toInt()))
        assertEquals("#000000", toHexRgb(0xff000000.toInt()))
    }

    @Test
    fun sameRgbComparesColorsWithoutAlpha() {
        assertTrue(sameRgb(0x80EF7076.toInt(), 0xffEF7076.toInt()))
        assertFalse(sameRgb(0xffEF7076.toInt(), 0xffF08C43.toInt()))
    }

    @Test
    fun customPaletteDedupesIgnoresAlphaAndCapsSize() {
        CustomColorStore.clear()

        assertTrue(CustomColorStore.add(0x80112233.toInt()))
        assertFalse(CustomColorStore.add(0xff112233.toInt()))
        repeat(20) { CustomColorStore.add(0xff000000.toInt() + it) }

        assertTrue(CustomColorStore.colors.size <= 12)
        assertEquals(CustomColorStore.colors.map { it and 0xFFFFFF }.distinct().size, CustomColorStore.colors.size)

        CustomColorStore.clear()
        assertTrue(CustomColorStore.colors.isEmpty())
    }

    @Test
    fun colorSequenceParsesHexRgbAndPaletteIndex() {
        assertEquals(0xFF893F3F.toInt(), parseColorSequence("#893F3F"))
        assertEquals(0xFF893F3F.toInt(), parseColorSequence("893F3F"))
        assertEquals(0xFFEF7076.toInt(), parseColorSequence("#80EF7076"))
        assertEquals(0xFFAABBCC.toInt(), parseColorSequence("#abc"))
        assertEquals(0xFF893F3F.toInt(), parseColorSequence("137,63,63"))
        assertEquals(0xFF893F3F.toInt(), parseColorSequence("137 63 63"))
        assertEquals(0xFF893F3F.toInt(), parseColorSequence("rgb(137,63,63)"))
        assertEquals(0xFF112233.toInt(), parseColorSequence("1", listOf(0xFF112233.toInt())))
        assertEquals(0xFF445566.toInt(), parseColorSequence("2", listOf(0xFF112233.toInt(), 0xFF445566.toInt())))
    }

    @Test
    fun colorSequenceRejectsInvalidInput() {
        assertNull(parseColorSequence(""))
        assertNull(parseColorSequence("not-a-color"))
        assertNull(parseColorSequence("300,0,0"))
        assertNull(parseColorSequence("0", listOf(0xFF112233.toInt())))
        assertNull(parseColorSequence("2", listOf(0xFF112233.toInt())))
    }
}
