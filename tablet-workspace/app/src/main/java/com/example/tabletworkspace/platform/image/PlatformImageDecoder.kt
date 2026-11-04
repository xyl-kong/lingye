package com.example.tabletworkspace.platform.image

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import java.io.File
import java.nio.ByteBuffer

/**
 * Image decoding that prefers BitmapFactory and falls back to [ImageDecoder] on API 28+.
 * The fallback matters for HEIF/HEIC (and AVIF on newer systems), where BitmapFactory is
 * device-dependent but ImageDecoder is the documented platform path. Decoded hardware
 * bitmaps are forced to software because the canvas draws them with a native software
 * Canvas.
 */
@SuppressLint("NewApi")
internal object PlatformImageDecoder {    data class Size(val width: Int, val height: Int)

    fun size(bytes: ByteArray): Size? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth > 0 && bounds.outHeight > 0) return Size(bounds.outWidth, bounds.outHeight)
        if (Build.VERSION.SDK_INT < 28) return null
        return runCatching {
            var result: Size? = null
            val source = ImageDecoder.createSource(ByteBuffer.wrap(bytes))
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                result = Size(info.size.width, info.size.height)
                decoder.setTargetSize(1, 1)
            }
            result
        }.getOrNull()
    }

    fun decodeFile(file: File, maxDimension: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        if (bounds.outWidth > 0 && bounds.outHeight > 0) {
            var sample = 1
            while (bounds.outWidth / sample > maxDimension || bounds.outHeight / sample > maxDimension) sample *= 2
            BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })?.let { return it }
        }
        if (Build.VERSION.SDK_INT < 28) return null
        return runCatching {
            val source = ImageDecoder.createSource(file)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val width = info.size.width
                val height = info.size.height
                val scale = minOf(1f, maxDimension.toFloat() / maxOf(width, height))
                if (scale < 1f) {
                    decoder.setTargetSize(
                        (width * scale).toInt().coerceAtLeast(1),
                        (height * scale).toInt().coerceAtLeast(1)
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }.getOrNull()
    }
}

/**
 * Rounds a desired on-screen pixel size up to one of a few decode buckets. Canvas images
 * keep one decoded bitmap per bucket, so small images are not held at 1600 px and zooming
 * only re-decodes when crossing a bucket boundary.
 */
internal object ImageSampleBuckets {
    val SIZES = intArrayOf(256, 512, 1024, 1600)

    fun bucketFor(desiredPx: Float): Int {
        if (!desiredPx.isFinite() || desiredPx <= 0f) return SIZES.first()
        val need = kotlin.math.ceil(desiredPx).toInt()
        return SIZES.firstOrNull { it >= need } ?: SIZES.last()
    }
}

