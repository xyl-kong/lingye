package com.example.tabletworkspace

import kotlin.math.abs

object BlockReorderCalculator {
    fun targetIndex(from: Int, pointerY: Float, centers: Map<Int, Float>): Int {
        if (centers.isEmpty()) return from
        return centers.entries
            .minWithOrNull(
                compareBy<Map.Entry<Int, Float>> { abs(it.value - pointerY) }
                    .thenBy { if (it.key == from) 0 else 1 }
                    .thenBy { it.key }
            )
            ?.key ?: from
    }
}
