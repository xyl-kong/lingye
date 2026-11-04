package com.example.tabletworkspace
import com.example.tabletworkspace.data.images.ImageCacheStats
import com.example.tabletworkspace.platform.image.ImageSampleBuckets

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageSampleBucketsTest {

    @Test
    fun roundsUpToTheNextDecodeBucket() {
        assertEquals(256, ImageSampleBuckets.bucketFor(1f))
        assertEquals(256, ImageSampleBuckets.bucketFor(256f))
        assertEquals(512, ImageSampleBuckets.bucketFor(256.1f))
        assertEquals(512, ImageSampleBuckets.bucketFor(400f))
        assertEquals(1024, ImageSampleBuckets.bucketFor(800f))
        assertEquals(1600, ImageSampleBuckets.bucketFor(1200f))
    }

    @Test
    fun veryLargeTargetsStayAtTheMaximumBucket() {
        assertEquals(1600, ImageSampleBuckets.bucketFor(1600f))
        assertEquals(1600, ImageSampleBuckets.bucketFor(99_999f))
    }

    @Test
    fun invalidTargetsFallBackToTheSmallestBucket() {
        assertEquals(256, ImageSampleBuckets.bucketFor(0f))
        assertEquals(256, ImageSampleBuckets.bucketFor(-5f))
        assertEquals(256, ImageSampleBuckets.bucketFor(Float.NaN))
        assertEquals(256, ImageSampleBuckets.bucketFor(Float.POSITIVE_INFINITY))
    }

    @Test
    fun cacheHitRateHandlesEmptyCounters() {
        assertEquals(0f, ImageCacheStats(0, 0, 0, 0, 0).hitRate, 0f)
        assertEquals(0.75f, ImageCacheStats(3, 0, 0, 75, 25).hitRate, 0.0001f)
        assertEquals(0.5f, ImageCacheStats(1, 0, 0, 1, 1).hitRate, 0.0001f)
    }

    @Test
    fun bucketSizesAreAscending() {
        val sizes = ImageSampleBuckets.SIZES
        assertEquals(256, sizes.first())
        assertEquals(1600, sizes.last())
        for (i in 1 until sizes.size) assertEquals(true, sizes[i] > sizes[i - 1])
    }
}
