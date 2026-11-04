package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Base64

class TextRichFeaturesTest {
    @Test
    fun rendersCommonInlineMath() {
        assertEquals("x² + 1", TextInlineMath.render("x^2 + 1"))
        assertEquals("(1)/(2)", TextInlineMath.render("\\frac{1}{2}"))
        assertEquals("√(x+1)", TextInlineMath.render("\\sqrt{x+1}"))
        assertEquals("α + β ≤ γ", TextInlineMath.render("\\alpha + \\beta \\le \\gamma"))
        assertEquals("x₁₀", TextInlineMath.render("x_{10}"))
    }

    @Test
    fun commentsRoundTripAndAdjust() {
        val card = "scale:100;comment:1:4:${b64("Need a source")}"
        assertEquals(listOf(TextComment(1, 4, "Need a source")), TextComments.parse(card))
        assertEquals(1, TextComments.at(card, 2).size)
        assertNull(TextComments.at(card, 4).firstOrNull())
        val adjusted = TextComments.adjust(card, "abcdef", "abXXcdef")
        assertEquals(listOf(TextComment(1, 6, "Need a source")), TextComments.parse(adjusted))
        val removed = TextComments.withoutComment(card, TextComment(1, 4, "Need a source"))
        assertEquals(emptyList<TextComment>(), TextComments.parse(removed))
    }

    @Test
    fun multipleCommentsOnDifferentRanges() {
        var card = TextComments.withComment("scale:100", 0, 2, "A")
        card = TextComments.withComment(card, 5, 7, "B")
        assertEquals(listOf(TextComment(0, 2, "A"), TextComment(5, 7, "B")), TextComments.parse(card))
        assertEquals(0, TextLinks.parse(card).size)
    }

    @Test
    fun equationsStoreTexSeparatelyFromRenderedText() {
        val card = TextEquations.withEquation("scale:100", 2, 6, "x^2")
        assertEquals(listOf(TextEquation(2, 6, "x^2")), TextEquations.parse(card))
        assertEquals("x^2", TextEquations.at(card, 3)?.tex)
        val adjusted = TextEquations.adjust(card, "abx²cd", "abx²Xcd")
        assertEquals(listOf(TextEquation(2, 7, "x^2")), TextEquations.parse(adjusted))
        assertEquals(emptyList<TextEquation>(), TextEquations.parse(TextEquations.withoutOverlapping(card, 0, 10)))
    }

    @Test
    fun textExtrasPreserveLinkCommentAndEquationTokens() {
        var card = TextLinks.withLink("scale:100", 0, 1, "https://a")
        card = TextComments.withComment(card, 1, 2, "note")
        card = TextEquations.withEquation(card, 2, 3, "y")
        val preserved = TextExtras.tokens(card)
        assertEquals(3, preserved.split(';').count { it.isNotBlank() })
        assertEquals(1, TextLinks.parse("scale:100$preserved").size)
        assertEquals(1, TextComments.parse("scale:100$preserved").size)
        assertEquals(1, TextEquations.parse("scale:100$preserved").size)
    }

    private fun b64(value: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))
}
