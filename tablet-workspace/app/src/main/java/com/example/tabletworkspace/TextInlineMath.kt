package com.example.tabletworkspace

/**
 * A small TeX-to-Unicode renderer for inline equations.
 * It intentionally supports common school-level notation rather than the full KaTeX grammar:
 * Greek letters, operators, superscripts, subscripts, fractions and square roots.
 */
object TextInlineMath {
    private val superscripts = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴', '5' to '⁵', '6' to '⁶', '7' to '⁷',
        '8' to '⁸', '9' to '⁹', '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'a' to 'ᵃ', 'b' to 'ᵇ', 'c' to 'ᶜ', 'd' to 'ᵈ', 'e' to 'ᵉ', 'f' to 'ᶠ', 'g' to 'ᵍ', 'h' to 'ʰ',
        'i' to 'ⁱ', 'j' to 'ʲ', 'k' to 'ᵏ', 'l' to 'ˡ', 'm' to 'ᵐ', 'n' to 'ⁿ', 'o' to 'ᵒ', 'p' to 'ᵖ',
        'r' to 'ʳ', 's' to 'ˢ', 't' to 'ᵗ', 'u' to 'ᵘ', 'v' to 'ᵛ', 'w' to 'ʷ', 'x' to 'ˣ', 'y' to 'ʸ',
        'z' to 'ᶻ', 'A' to 'ᴬ', 'B' to 'ᴮ', 'D' to 'ᴰ', 'E' to 'ᴱ', 'G' to 'ᴳ', 'H' to 'ᴴ', 'I' to 'ᴵ',
        'J' to 'ᴶ', 'K' to 'ᴷ', 'L' to 'ᴸ', 'M' to 'ᴹ', 'N' to 'ᴺ', 'O' to 'ᴼ', 'P' to 'ᴾ', 'R' to 'ᴿ',
        'T' to 'ᵀ', 'U' to 'ᵁ', 'V' to 'ⱽ', 'W' to 'ᵂ'
    )
    private val subscripts = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄', '5' to '₅', '6' to '₆', '7' to '₇',
        '8' to '₈', '9' to '₉', '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
        'a' to 'ₐ', 'e' to 'ₑ', 'h' to 'ₕ', 'i' to 'ᵢ', 'j' to 'ⱼ', 'k' to 'ₖ', 'l' to 'ₗ', 'm' to 'ₘ',
        'n' to 'ₙ', 'o' to 'ₒ', 'p' to 'ₚ', 'r' to 'ᵣ', 's' to 'ₛ', 't' to 'ₜ', 'u' to 'ᵤ', 'v' to 'ᵥ',
        'x' to 'ₓ'
    )
    private val commands = linkedMapOf(
        "\\alpha" to "α", "\\beta" to "β", "\\gamma" to "γ", "\\delta" to "δ", "\\epsilon" to "ε",
        "\\varepsilon" to "ε", "\\zeta" to "ζ", "\\eta" to "η", "\\theta" to "θ", "\\lambda" to "λ",
        "\\mu" to "μ", "\\nu" to "ν", "\\xi" to "ξ", "\\pi" to "π", "\\rho" to "ρ", "\\sigma" to "σ",
        "\\tau" to "τ", "\\phi" to "φ", "\\varphi" to "φ", "\\chi" to "χ", "\\psi" to "ψ", "\\omega" to "ω",
        "\\Gamma" to "Γ", "\\Delta" to "Δ", "\\Theta" to "Θ", "\\Lambda" to "Λ", "\\Sigma" to "Σ",
        "\\Phi" to "Φ", "\\Psi" to "Ψ", "\\Omega" to "Ω",
        "\\times" to "×", "\\cdot" to "·", "\\div" to "÷", "\\pm" to "±", "\\mp" to "∓",
        "\\le" to "≤", "\\leq" to "≤", "\\ge" to "≥", "\\geq" to "≥", "\\ne" to "≠", "\\neq" to "≠",
        "\\approx" to "≈", "\\equiv" to "≡", "\\sim" to "∼", "\\infty" to "∞",
        "\\sum" to "∑", "\\prod" to "∏", "\\int" to "∫", "\\partial" to "∂", "\\nabla" to "∇",
        "\\rightarrow" to "→", "\\to" to "→", "\\leftarrow" to "←", "\\Rightarrow" to "⇒",
        "\\degree" to "°", "\\circ" to "°", "\\prime" to "′", "\\ldots" to "…", "\\cdots" to "…",
        "\\sin" to "sin", "\\cos" to "cos", "\\tan" to "tan", "\\log" to "log", "\\ln" to "ln",
        "\\exp" to "exp", "\\lim" to "lim", "\\max" to "max", "\\min" to "min",
        "\\left" to "", "\\right" to "", "\\text" to "", "\\mathrm" to "", "\\mathbf" to "",
        "\\," to " ", "\\;" to " ", "\\!" to "", "\\quad" to "  ", "\\qquad" to "    "
    )

    fun render(tex: String): String {
        var value = tex.trim()
        if (value.length >= 2 && value.startsWith("$") && value.endsWith("$")) {
            value = value.substring(1, value.length - 1)
        }
        value = expandFractionsAndRoots(value)
        commands.entries.sortedByDescending { it.key.length }.forEach { (from, to) ->
            value = value.replace(from, to)
        }
        value = applyScripts(value, '^', superscripts)
        value = applyScripts(value, '_', subscripts)
        value = value.replace("{", "").replace("}", "").replace("\\", "")
        return value.replace(Regex("\\s+"), " ").trim()
    }

    private fun expandFractionsAndRoots(input: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < input.length) {
            val rest = input.substring(i)
            val frac = listOf("\\frac", "\\dfrac").firstOrNull { rest.startsWith(it) }
            if (frac != null) {
                val (a, afterA) = readGroup(input, i + frac.length)
                val (b, afterB) = readGroup(input, afterA)
                out.append('(').append(render(a)).append(")/(").append(render(b)).append(')')
                i = afterB
                continue
            }
            if (rest.startsWith("\\sqrt")) {
                var cursor = i + 5
                var index = ""
                while (cursor < input.length && input[cursor] == ' ') cursor++
                if (cursor < input.length && input[cursor] == '[') {
                    val close = input.indexOf(']', cursor)
                    if (close > cursor) {
                        index = input.substring(cursor + 1, close)
                        cursor = close + 1
                    }
                }
                val (content, after) = readGroup(input, cursor)
                out.append(if (index.isBlank()) "√" else render(index) + "√")
                out.append('(').append(render(content)).append(')')
                i = after
                continue
            }
            out.append(input[i])
            i++
        }
        return out.toString()
    }

    private fun readGroup(input: String, start: Int): Pair<String, Int> {
        var i = start
        while (i < input.length && input[i] == ' ') i++
        if (i >= input.length) return "" to i
        if (input[i] != '{') return input[i].toString() to (i + 1)
        var depth = 0
        val begin = i + 1
        while (i < input.length) {
            when (input[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return input.substring(begin, i) to (i + 1)
                }
            }
            i++
        }
        return input.substring(begin) to input.length
    }

    private fun applyScripts(input: String, marker: Char, map: Map<Char, Char>): String {
        val out = StringBuilder()
        var i = 0
        while (i < input.length) {
            if (input[i] != marker) {
                out.append(input[i])
                i++
                continue
            }
            val (content, after) = readGroup(input, i + 1)
            val mapped = content.map { map[it] }
            if (mapped.all { it != null }) {
                mapped.forEach { out.append(it) }
            } else {
                out.append(if (marker == '^') "⁽" else "₍")
                out.append(content)
                out.append(if (marker == '^') "⁾" else "₎")
            }
            i = after
        }
        return out.toString()
    }
}
