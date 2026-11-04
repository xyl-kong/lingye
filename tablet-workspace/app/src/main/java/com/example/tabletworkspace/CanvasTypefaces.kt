package com.example.tabletworkspace

import android.graphics.Typeface
import android.os.Build

/**
 * Caches the Typeface objects the canvas needs for styled text runs. Creating one via
 * [Typeface.create] on every run of every frame was the most expensive part of drawing
 * formatted text; the set of family/weight/italic combinations is tiny, so they are
 * created once and reused.
 */
internal object CanvasTypefaces {
    private val bases = arrayOf(Typeface.SANS_SERIF, Typeface.SERIF, Typeface.MONOSPACE)
    private val cache = HashMap<Int, Typeface>()

    fun familyIndex(family: String): Int = when (family) {
        "serif" -> 1
        "mono" -> 2
        else -> 0
    }

    /** [weight] is the requested weight; below API 28 it collapses to bold/normal. */
    fun get(familyIndex: Int, weight: Int, italic: Boolean): Typeface {
        val base = bases[familyIndex.coerceIn(0, bases.lastIndex)]
        val effectiveWeight = if (Build.VERSION.SDK_INT >= 28) weight else if (weight >= 600) 700 else 400
        val key = (familyIndex shl 16) or ((effectiveWeight / 100) shl 1) or (if (italic) 1 else 0)
        synchronized(cache) {
            cache[key]?.let { return it }
            val typeface = if (Build.VERSION.SDK_INT >= 28) {
                Typeface.create(base, effectiveWeight, italic)
            } else {
                Typeface.create(
                    base,
                    (if (effectiveWeight >= 600) Typeface.BOLD else Typeface.NORMAL) or
                        (if (italic) Typeface.ITALIC else 0)
                )
            }
            cache[key] = typeface
            return typeface
        }
    }
}
